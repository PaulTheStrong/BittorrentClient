package by.bsuir.ksis.kursovoi.protocol;

import by.bsuir.ksis.kursovoi.client.PieceManager;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;

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

    /** The async Queue containing available peers */
    private BlockingQueue<Peer> queue;

    private Set<ConnectionState> peerState = new HashSet<>();
    private Set<ConnectionState> myState = new HashSet<>();

    /** The SHA1 hash for the meta-data's info */
    private String infoHash;

    /** Our peer ID used to to identify ourselves */
    private String peerId;
    private String remoteId;
    private InputStream inputStream;
    private OutputStream outputStream;

    /**  The manager responsible to determine which pieces
     to request */
    private PieceManager pieceManager;

    public PeerConnection(BlockingQueue<Peer> queue, String infoHash, String peerId, PieceManager pieceManager) {
        this.queue = queue;
        this.infoHash = infoHash;
        this.peerId = peerId;
        this.pieceManager = pieceManager;
    }

    public void start() {
        while (!myState.contains(ConnectionState.STOPPED)) {

        }
    }

    /** Sends the cancel message to the remote peer and closes the
     * connection. */
    public void cancel() {

    }


}
