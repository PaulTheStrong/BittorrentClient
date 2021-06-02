package by.bsuir.ksis.kursovoi.protocol;

import by.bsuir.ksis.kursovoi.Utils;
import by.bsuir.ksis.kursovoi.client.PieceManager;
import by.bsuir.ksis.kursovoi.client.callbacks.ClientCallBackFunction;
import by.bsuir.ksis.kursovoi.data.Block;
import by.bsuir.ksis.kursovoi.protocol.messages.*;
import lombok.SneakyThrows;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import static by.bsuir.ksis.kursovoi.Utils.byteArray2Hex;

/**
 * A peer connection used to download and upload pieces.
 *
 * The peer connection will consume one available peer from the
 * given queue. Based on the peer details the PeerConnection will
 * try to open a connection and perform a BitTorrent handshake.
 *
 * After a successful handshake, the PeerConnection will be in a
 * *choked* state, not allowed to request any data from the remote
 * peer. After sending an interested message the PeerConnection will
 * be waiting to get *unchoked*.
 *
 * Once the remote peer unchoked us, we can start requesting pieces.
 * The PeerConnection will continue to request pieces for as long as
 * there are pieces left to request, or until the remote peer disconnects.
 *
 * If the connection with a remote peer drops, the PeerConnection
 * will consume the next available peer from off the queue and try
 * to connect to that one instead.
 */
public class PeerConnection {

    private static final Logger LOGGER = Logger.getRootLogger();
    public static final int PIECES_PER_REQUEST = 50;

    /** The async Queue containing available peers */
    private final BlockingQueue<Peer> availablePeers;

    private final Set<ConnectionState> peerState = new HashSet<>();
    private final Set<ConnectionState> myState = new HashSet<>();

    /** The SHA1 hash for the meta-data's info */
    private final String infoHash;

    /** Our peer ID used to to identify ourselves */
    private String peerId;
    private String remoteId;
    private PeerInputStream peerInputStream;
    private PeerOutputStream peerOutputStream;
    private ClientCallBackFunction callBackFunction;

    /**  The manager responsible to determine which pieces
     to request */
    private final PieceManager pieceManager;

    public PeerConnection(BlockingQueue<Peer> queue, String infoHash, String peerId, PieceManager pieceManager, ClientCallBackFunction callBack) {
        this.availablePeers = queue;
        this.infoHash = infoHash;
        this.peerId = peerId;
        this.pieceManager = pieceManager;
        this.callBackFunction = callBack;
    }

    @SneakyThrows
    public void start() {
        while (!myState.contains(ConnectionState.STOPPED)) {
            Peer peer = availablePeers.poll();
            if (peer == null) {
                Thread.sleep(10000);
                continue;
            }
            String ip = peer.getIp();
            int port = peer.getPort();
            LOGGER.info("Got peer " + ip + ":" + port);
            try (Socket socket = new Socket(ip, port)){
                LOGGER.info("Connected to the peer " + ip + ":" + port);
                peerInputStream = new PeerInputStream(socket.getInputStream());
                peerOutputStream = new PeerOutputStream(socket.getOutputStream());

                /* It's our responsibility to initiate the handshake. */
                sendHandshake();

                /*  The default state for a connection is that peer is
                not interested and we are choked */
                myState.add(ConnectionState.CHOKED);
                sendInterested();
                myState.add(ConnectionState.INTERESTED);
                int requestedBlocks = 0;
                PeerMessage unchokeMessage = new UnchokeMessage();
                peerOutputStream.writeMessage(unchokeMessage);

                while (!myState.contains(ConnectionState.STOPPED)) {
                    PeerMessage message = peerInputStream.readNextMessage();
                    MessageType type = message.getType();
                    LOGGER.debug("Received Message type: " + type.name());
                    switch (type) {
                        case BITFIELD:
                            BitFieldMessage bitfieldMessage = (BitFieldMessage) message;
                            byte[] bitfield = bitfieldMessage.getBitField();
                            pieceManager.addPeer(remoteId, bitfield);
                            break;
                        case INTERESTED:
                            peerState.add(ConnectionState.INTERESTED);
                            break;
                        case NOT_INTERESTED:
                            peerState.remove(ConnectionState.INTERESTED);
                            break;
                        case CHOKE:
                            myState.add(ConnectionState.CHOKED);
                            break;
                        case UNCHOKE:
                            myState.remove(ConnectionState.CHOKED);
                            break;
                        case HAVE:
                            HaveMessage haveMessage = (HaveMessage) message;
                            pieceManager.updatePeer(remoteId, haveMessage.getIndex());
                            break;
                        case KEEPALIVE:
                            break;
                        case PIECE:
                            if (--requestedBlocks == 1) {
                                myState.remove(ConnectionState.PENDING_REQUEST);
                            }
                            PieceMessage pieceMessage = (PieceMessage) message;
                            LOGGER.debug("RECEIVED block for piece " + pieceMessage.getIndex() + " offset " + pieceMessage.getBegin() + ". requestedBlocks = " + requestedBlocks);
                            callBackFunction.callBack(remoteId, pieceMessage.getIndex(), pieceMessage.getBegin(), pieceMessage.getData());
                            break;
                        case REQUEST:
                            LOGGER.info("Ignoring the received Request message");
                            break;
                        case CANCEL:
                            LOGGER.info("Ignoring the received Cancel message");
                            break;
                        case PORT:
                            LOGGER.info("PORT NOT IMPLEMENTED");
                            break;
                    }
                    if (!myState.contains(ConnectionState.CHOKED)) {
                        if (myState.contains(ConnectionState.INTERESTED)) {
                            if (!myState.contains(ConnectionState.PENDING_REQUEST)) {
                                myState.add(ConnectionState.PENDING_REQUEST);
                                for (int i = 0; i < PIECES_PER_REQUEST; i++) {
                                    if (!requestPiece()) {
                                        break;
                                    } else {
                                        requestedBlocks++;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (ConnectException | BindException e) {
                LOGGER.info("Cannot connect to " + ip + ":" + port + " " + e.getMessage());
            }
            catch (Exception e) {
                //e.printStackTrace();
                LOGGER.warn("Something went wrong during transfer: " + e);
                e.printStackTrace();
                //availablePeers.put(new Peer(ip, port));
                //stop();
            }
        }
        LOGGER.info("Exit peerConnection.start()");
    }

    /** Sends the cancel message to the remote peer and closes the
     * connection. */
    public void cancel() {
        LOGGER.info("Closing connection with peer " + remoteId);
    }

    /** Stop this connection from the current peer (if a connection exist) and
     from connecting to any new peer. */
    public synchronized void stop() {
        myState.add(ConnectionState.STOPPED);

    }

    public boolean requestPiece() throws IOException {
        Optional<Block> blockOpt = pieceManager.nextRequest(remoteId);
        if (blockOpt.isPresent()) {
            Block block = blockOpt.get();
            RequestMessage requestMessage = new RequestMessage(block.getPiece(), block.getOffset(), block.getLength());
            LOGGER.debug("Requesting block " + block.getOffset() +
                    " for piece " + block.getPiece() +
                    " of " + block.getLength() +
                    " bytes from peer " + remoteId);

            peerOutputStream.writeMessage(requestMessage);
        }
        return blockOpt.isPresent();
    }

    /**
     * Send the initial handshake to the remote peer and wait for
     * the peer to respond with its handshake.
     */
    public void sendHandshake() throws ProtocolException, IOException, InterruptedException {

        HandshakeMessage handshakeMessage = new HandshakeMessage(Utils.hex2ByteArray(infoHash), Utils.hex2ByteArray(peerId));
        peerOutputStream.writeMessage(handshakeMessage);
        HandshakeMessage responseHandshake = peerInputStream.readHandshake();
        if (!byteArray2Hex(responseHandshake.getInfoHash()).equals(infoHash)){
            throw new ProtocolException("Handshake with invalid info hash");
        }
        this.remoteId = byteArray2Hex(responseHandshake.getPeerId());
        LOGGER.info("Handshake was successful");
    }

    public void sendInterested() throws IOException {
        PeerMessage interested = new InterestedMessage();
        peerOutputStream.writeMessage(interested);
    }


}
