package by.bsuir.ksis.kursovoi.client;

import by.bsuir.ksis.kursovoi.client.callbacks.OnBlockRetrievedCallBackFunction;
import by.bsuir.ksis.kursovoi.data.TorrentMetaInfo;
import by.bsuir.ksis.kursovoi.data.TrackerResponse;
import by.bsuir.ksis.kursovoi.protocol.Peer;
import by.bsuir.ksis.kursovoi.protocol.PeerConnection;
import lombok.SneakyThrows;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 *  The torrent client is the local peer that holds peer-to-peer
 *  connections to download and upload pieces for a given torrent.
 *  <br/>
 *  Once started, the client makes periodic announce calls to the tracker
 *  registered in the torrent meta-data. These calls results in a list of
 *  peers that should be tried in order to exchange pieces.
 *  </br>
 *  Each received peer is kept in a queue that a pool of PeerConnection
 *  objects consume. There is a fix number of PeerConnections that can have
 *  a connection open to a peer. Since we are not creating expensive threads
 *  (or worse yet processes) we can create them all at once and they will
 *  be waiting until there is a peer to consume in the queue.
 */
public class TorrentClient {

    private static final Logger LOGGER = Logger.getRootLogger();

    private static final int MAX_PEER_CONNECTIONS = 30;

    private final Tracker tracker;

    /** The list of potential peers is the work queue, consumed
     * by the PeerConnections */
    private final BlockingQueue<Peer> availablePeers;

    /**
     * The list of peers is the list of workers that *might* be
     * connected to a peer. Else they are waiting to consume new
     * remote peers from the `available_peers` queue. These are
     * our workers!
     */
    private final List<PeerConnection> peers;

    /** The piece manager implements the strategy on which pieces
     * to request, as well as the logic to persist received pieces
     * to disk. */
    private final PieceManager pieceManager;
    private boolean abort;

    public TorrentClient(TorrentMetaInfo torrent) {
        this.peers = new ArrayList<>();
        this.tracker = new Tracker(torrent);
        this.availablePeers = new ArrayBlockingQueue<>(MAX_PEER_CONNECTIONS);
        this.pieceManager = new PieceManager(torrent);
    }

    /**
     *
     * Start downloading the torrent held by this client.
     *
     * This results in connecting to the tracker to retrieve the
     * list of peers to communicate with. Once the torrent is fully
     * downloaded or if the download is aborted this method will
     * complete.
     *
     */
    @SneakyThrows
    public void start() {
        pieceManager.init();
        ExecutorService exec = Executors.newFixedThreadPool(MAX_PEER_CONNECTIONS);
        for (int i = 0; i < MAX_PEER_CONNECTIONS; i++) {
            TorrentMetaInfo torrent = tracker.getTorrent();
            PeerConnection peerConnection = new PeerConnection(availablePeers, torrent.getInfoHash(), torrent.getPeerId(), pieceManager, new OnBlockRetrievedCallBackFunction(this));
            peers.add(peerConnection);
            exec.submit(peerConnection::start);
        }

        long previous = 0;
        long interval = 30 * 60;
        while (true) {
            if (pieceManager.isComplete()) {
                FileSplitter fileSplitter = new FileSplitter(tracker.getTorrent(), tracker.getTorrent().getName());
                fileSplitter.splitIntoFiles();
                tracker.connect(false, 0, pieceManager.getBytesDownloaded());
                LOGGER.info("Torrent has been downloaded!");
                exec.shutdownNow();
                break;
            }
            if (abort) {
                LOGGER.info("Aborting download");
                break;
            }

            long current = System.currentTimeMillis() / 1000;
            if (previous == 0 || previous + interval < current) {
                TrackerResponse response = tracker
                        .connect(previous == 0,
                                pieceManager.getBytesUploaded(),
                                pieceManager.getBytesDownloaded());
                if (response.getFailure() == null) {
                    previous = current;
                    interval = response.getInterval();
                    clearAvailablePeers();
                    for (Peer peer : response.getPeers()) {
                        availablePeers.put(peer);
                    }
                }
            } else {
                TimeUnit.SECONDS.sleep(5);
            }
        }
    }

    public void stop() throws Exception {
        abort = true;
        for (PeerConnection peerConnection : peers) {
            peerConnection.stop();
        }
        pieceManager.close();
    }

    public void clearAvailablePeers() {
        if (!availablePeers.isEmpty()) {
            availablePeers.clear();
        }
    }

    /**
     * Callback function called by the `PeerConnection` when a block is
     * retrieved from a peer.
     * @param peerId: The id of the peer the block was retrieved from
     * @param pieceIndex: The piece index this block is a part of
     * @param blockOffset: The block offset within its piece
     * @param data: The binary data retrieved
     */
    public void onBlockRetrieved(String peerId, long pieceIndex, long blockOffset, byte[] data) throws IOException {
        pieceManager.blockReceived(peerId, pieceIndex, blockOffset, data);
    }

    public PieceManager getPieceManager() {
        return pieceManager;
    }
}
