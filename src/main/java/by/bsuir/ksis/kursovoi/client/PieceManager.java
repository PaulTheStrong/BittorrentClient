package by.bsuir.ksis.kursovoi.client;

import by.bsuir.ksis.kursovoi.Utils;
import by.bsuir.ksis.kursovoi.data.Block;
import by.bsuir.ksis.kursovoi.data.Piece;
import by.bsuir.ksis.kursovoi.data.TorrentMetaInfo;
import lombok.SneakyThrows;
import org.apache.log4j.Logger;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 *  The PieceManager is responsible for keeping track of
 *  all the available pieces for the connected peers
 *  as well as the pieces we have available for other peers.
 *
 *  The strategy on which piece to request is made
 *  as simple as possible in this implementation.
 */
public class PieceManager implements AutoCloseable {

    private static final int MAX_PENDING_TIME = 300;
    private static final Logger LOGGER = Logger.getRootLogger();

    private TorrentMetaInfo torrent;
    private List<Block> pendingBlocks = new ArrayList<>();
    private List<Piece> missingPieces;
    private List<Piece> ongoingPieces = new ArrayList<>();
    private List<Piece> havePieces = new ArrayList<>();

    private long totalPieces;
    private FileOutputStream fileOutputStream;

    /** TODO not sure if its String - Integer */
    private Map<String, Map<String, Object>> peers = new HashMap<>();

    private static final long REQUEST_SIZE = (long) Math.pow(2, 14);

    @SneakyThrows
    public PieceManager(TorrentMetaInfo torrent) {
        this.torrent = torrent;
        this.totalPieces = torrent.getPieceHashes().length;
        this.missingPieces = initiatePieces();
        fileOutputStream = new FileOutputStream(torrent.getName());
    }

    private List<Piece> initiatePieces() {

        List<Piece> pieces = new ArrayList<>();
        long pieceBlocks = (torrent.getPieceLength() + REQUEST_SIZE - 1) / REQUEST_SIZE;
        IntStream.range(0, (int) totalPieces - 1).forEach(index -> {
            List<Block> blocks = new ArrayList<>();
            IntStream.range(0, (int) pieceBlocks)
                    .forEach(offset -> blocks.add(
                    new Block(index,
                            offset * REQUEST_SIZE,
                            REQUEST_SIZE)));
            byte[] pieceHash = torrent.getPieceHashes()[index];
            String hash = Utils.byteArray2Hex(pieceHash);
            pieces.add(new Piece(index, blocks, hash));
        });
        long lastLength = torrent.getTotalSize() % torrent.getPieceLength();
        int blockCount = (int) ((lastLength + REQUEST_SIZE - 1) / REQUEST_SIZE);
        List<Block> blocks = new ArrayList<>();
        IntStream.range(0, blockCount - 1)
                .forEach(offset -> blocks.add(
                        new Block(totalPieces - 1,
                                offset * REQUEST_SIZE,
                                REQUEST_SIZE)));
        blocks.add(new Block(totalPieces - 1, (totalPieces - 1) * REQUEST_SIZE, lastLength % REQUEST_SIZE));
        byte[] pieceHash = torrent.getPieceHashes()[(int) (totalPieces - 1)];
        String hash = Utils.byteArray2Hex(pieceHash);
        pieces.add(new Piece(totalPieces - 1, blocks, hash));
        return pieces;
    }

    /** Closes the connection to a referred file. */
    @Override
    public void close() throws Exception {
        fileOutputStream.close();
    }

    /**
     * Checks whether or not the all pieces are downloaded for
     * this piece.
     * @return True if all pieces are fully downloaded.
     */
    public boolean isComplete() {
        return havePieces.size() == totalPieces;
    }

    /**
     * Get the number of bytes downloaded.
     * This method Only counts full, verified, pieces,
     * not single blocks.
     */
    public long getBytesDownloaded() {
        return havePieces.size() * torrent.getPieceLength();
    }

    /** TODO Get the number of bytes uploaded */
    public long getBytesUploaded() {
        throw new UnsupportedOperationException();
    }

    /** Add a peer and the bitfield representing the
     * pieces the peer has.
     */
    public void addPeer(String peerId, Integer bitfield) {
        peers.get(peerId).put(peerId, bitfield);
    }

    /** Update the information about which pieces a peer has
     * (reflects a Have message)
     */
    public void updatePeer(String peerId) {
        if (peers.containsKey(peerId)) {
            peers.get(peerId).put("index", 1);
        }
    }

    /** Removes a previously added peer (e.g used if a peer
     * connection is dropped
     */
    public void removePeer(String peerId) {
        peers.remove(peerId);
    }

    /**
     * TODO Get the next Bock that should be requested from the
     * giver peer.
     *
     * If there are no more blocks left to retreive or if
     * this peer does not have any of the missing pieces
     * return Optional.empty().
     *
     * TODO write algo explanation.

    # The algorithm implemented for which piece to retrieve is a simple
    # one. This should preferably be TODO <b>replaced</b> with an
    # implementation of "rarest-piece-first" algorithm instead.
    #
    # The algorithm tries to download the pieces in sequence and will try
    # to finish started pieces before starting with new pieces.

    # 1. Check any pending blocks to see if any request should be reissued
    #    due to timeout
    # 2. Check the ongoing pieces to get the next block to request
    # 3. Check if this peer have any of the missing pieces not yet started
     */
    public Optional<Block> nextRequest(String peerId) {
        if (!peers.containsKey(peerId)) {
            return Optional.empty();
        }
        Optional<Block> block = expiredRequests(peerId);
        if (block.isEmpty()) {
            block = nextOngoing(peerId);
            if (block.isEmpty()) {
                block = getRarestPiece(peerId).nextRequest();
            }
        }
        return block;
    }

    /**
     * TODO This method must be called when a block has
     * successfully been retrieved by a peer.
     *
     * Once a full piece have been retrieved, a SHA1 hash
     * control is made. If the check fails all the pieces
     * blocks are put back in missing state to be fetched
     * again. If the hash succeeds the partial piece is
     * written to disk and the piece is indicated as Have.
     */
    public void blockReceived(String peerId, long pieceIndex, long blockOffset, byte[] data) {
        LOGGER.debug("Received block " + blockOffset + " for piece " + pieceIndex
                    + " from peer " + peerId);
        for (int index = 0; index < pendingBlocks.size(); index++) {
            Block pendingBlock = pendingBlocks.get(index);
            if (pendingBlock.getPiece() == pieceIndex && pendingBlock.getOffset() == blockOffset) {
                pendingBlocks.remove(index);
                break;
            }
        }
        Optional<Piece> pieceOpt = ongoingPieces.stream()
                .filter(p -> p.getIndex() == pieceIndex)
                .findFirst();
        if (pieceOpt.isPresent()) {
            Piece piece = pieceOpt.get();
            if (piece.isHashMatching()) {
                write(piece);
                ongoingPieces.remove(piece);
                havePieces.add(piece);
                long complete = totalPieces - missingPieces.size() - ongoingPieces.size();
                LOGGER.info(complete + " / " + totalPieces + " pieces downloaded");
            } else {
                LOGGER.info("Discarding corrupt piece #" + piece.getIndex());
                piece.reset();
            }
        }
    }

    /**
     * Go through previously requested blocks, if any one
     * have been in the requested state for longer than
     * `MAX_PENDING_TIME` return the block to be re-requested.
     * If no pending blocks exist, Optional.empty() is returned
     */
    public Optional<Block> expiredRequests(String peerId) {
/*
        long current = System.currentTimeMillis() / 1000;
        for (Block request : pendingBlocks) {
            if (peers.get(peerId).containsKey(request.getPiece())) {
            }
        }
*/
        throw new UnsupportedOperationException();
    }

    /**
     * Go through the ongoing pieces and return the next
     * block to be requested or Optioanl.empty() if no
     * block is left to be requested.
     */
    public Optional<Block> nextOngoing(String peerId) {
        throw new UnsupportedOperationException();
    }

    /**
     * Given the current list of missing pieces, get the
     * rarest one first (i.e. a piece which fewest of its
     * neighboring peers have)
     */
    public Piece getRarestPiece(String peerId) {
        Map<String, Integer> pieceCount = new HashMap<>();
        for (Piece piece : missingPieces) {
            if (peers.get(peerId).containsKey(piece.getIndex())) {
                for (String p : peers.keySet()) {
                    if (peers.get(p).containsKey(piece.getIndex())) {
                        pieceCount.computeIfPresent(p, (k, v) -> v + 1);
                        pieceCount.putIfAbsent(p, 1);
                    }
                }
            }
        }
        Integer rarestPieceIndex = Collections.min(pieceCount.values());
        //Piece rarestPiece = missingPieces.remove(rarestPiece);
        //ongoingPieces.add();
        throw new UnsupportedOperationException();
    }

    /**
     * Go through the missing pieces and return the next block
     * to request or Optional.empty() if no block is left to
     * be requested.
     *
     * This will change the state of the piece from missing to
     * ongoing - thus the next call to this function will not
     * continue with the blocks for that piece, rather get
     * the next missing piece.
     */
    public Optional<Block> nextMissing(String peerId) {
        throw new UnsupportedOperationException();
    }

    /** Write a given piece to disk */
    public void write(Piece piece) {
        throw new UnsupportedOperationException();
    }
}
