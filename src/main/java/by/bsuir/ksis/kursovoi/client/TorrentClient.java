package by.bsuir.ksis.kursovoi.client;

import by.bsuir.ksis.kursovoi.protocol.Peer;

import java.util.List;
import java.util.Queue;

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

    private Tracker tracker;
    private Queue<Peer> availablePeers;
    private List<Peer> peers;

    private PieceManager pieceManager;
    private boolean abort;

    public void start() {

    }

}
