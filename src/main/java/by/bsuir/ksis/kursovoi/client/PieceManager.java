package by.bsuir.ksis.kursovoi.client;

import by.bsuir.ksis.kursovoi.Utils;
import by.bsuir.ksis.kursovoi.data.Bitfield;
import by.bsuir.ksis.kursovoi.data.Block;
import by.bsuir.ksis.kursovoi.data.Piece;
import by.bsuir.ksis.kursovoi.data.TorrentMetaInfo;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import lombok.SneakyThrows;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.*;

/**
 *  The PieceManager is responsible for keeping track of
 *  all the available pieces for the connected peers
 *  as well as the pieces we have available for other peers.
 *
 *  The strategy on which piece to request is made
 *  as simple as possible in this implementation.
 */
public class PieceManager implements AutoCloseable {

    private static final long MAX_PENDING_TIME = 300 * 1000;
    private static final Logger LOGGER = Logger.getRootLogger();

    private final TorrentMetaInfo torrent;
    private final PieceWriter pieceWriter;

    private final ObservableList<Block> pendingBlocks = FXCollections.observableArrayList();
    private final ObservableList<Piece> missingPieces = FXCollections.observableArrayList();
    private final ObservableList<Piece> ongoingPieces = FXCollections.observableArrayList();

    private final ObservableList<Piece> havePieces = FXCollections.observableArrayList();

    private final int totalPieces;

    private final Map<String, Bitfield> peersBitfields = new HashMap<>();

    private static final int REQUEST_SIZE = 1 << 14;

    @SneakyThrows
    public PieceManager(TorrentMetaInfo torrent) {
        this.torrent = torrent;
        this.totalPieces = torrent.getPieceHashes().length;
        pieceWriter = new PieceWriter(torrent.getName(), torrent, this);
    }


    private void checkDownloadedPieces() throws IOException {
        File file = new File("H:/downloads/" + torrent.getName());
        if (!file.exists()) {
            LOGGER.info("FILE DOESNT EXIST");
            return;
        }
        FileInputStream fileInputStream = new FileInputStream(file);
        //file.setLength(torrent.getTotalSize());

        byte[][] pieceHashes = torrent.getPieceHashes();
        int totalPieces = pieceHashes.length;
        int READ_PIECES = 32;
        int pieceLength = torrent.getPieceLength();

        byte[] buffer = new byte[pieceLength * READ_PIECES];
        int read;
        int currentPieceIndex = 0;
        while((read = fileInputStream.read(buffer)) != -1) {
            LOGGER.info("READ " + read + " bytes");
            for (int i = 0; i < read; i += pieceLength) {
                int currentPieceLength = currentPieceIndex != pieceHashes.length - 1 ? pieceLength : (int) (torrent.getTotalSize() % pieceLength);
                byte[] data = new byte[currentPieceLength];
                System.arraycopy(buffer, i, data, 0, currentPieceLength);
                String shaSum = Utils.SHAsum(data);
                if (shaSum.equals(Utils.byteArray2Hex(pieceHashes[currentPieceIndex]))) {
                    int finalCurrentPieceIndex = currentPieceIndex;
                    Piece havePiece = missingPieces.stream().filter(piece -> finalCurrentPieceIndex == piece.getIndex()).findFirst().get();
                    missingPieces.remove(havePiece);
                    havePieces.add(havePiece);
                }
                currentPieceIndex++;
            }
        }
        fileInputStream.close();
    }

    private void initiatePieces() {
        int stdPieceBlocks = (torrent.getPieceLength() + REQUEST_SIZE - 1) / REQUEST_SIZE;
        for (int i = 0; i < totalPieces; i++) {
            List<Block> blocks = new ArrayList<>();
            if (i < totalPieces - 1) {
                for (int j = 0; j < stdPieceBlocks; j++) {
                    blocks.add(new Block(i, j * REQUEST_SIZE, REQUEST_SIZE));
                }
            } else {
                long lastLength = torrent.getTotalSize() % torrent.getPieceLength();
                int numBlocks = (int) ((lastLength + REQUEST_SIZE - 1) / REQUEST_SIZE);
                for (int j = 0; j < numBlocks; j++) {
                    blocks.add(new Block(i, j * REQUEST_SIZE, REQUEST_SIZE));
                }
                if (lastLength % REQUEST_SIZE > 0) {
                    blocks.get(blocks.size() - 1).setLength((int) (lastLength % REQUEST_SIZE));
                }
            }
            missingPieces.add(new Piece(i, blocks, Utils.byteArray2Hex(torrent.getPieceHashes()[i])));
        }
    }

    @SneakyThrows
    public void init() {
        initiatePieces();
        checkDownloadedPieces();
        new Thread(pieceWriter).start();
    }

    /** Closes the connection to a referred file. */
    @Override
    public synchronized void close() throws Exception {
        pieceWriter.setComplete(true);
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
        int pieceLength = torrent.getPieceLength();
        long downloaded = (long) havePieces.size() * pieceLength;
        boolean present = havePieces.stream().anyMatch(piece -> piece.getIndex() == totalPieces - 1);
        if (present) {
            downloaded -= pieceLength;
            downloaded += torrent.getTotalSize() % pieceLength;
        }
        return downloaded;
    }

    /** TODO Get the number of bytes uploaded */
    public long getBytesUploaded() {
        return 0;
        //throw new UnsupportedOperationException();
    }

    /**
     * Add a peer and the bitfield representing the
     * pieces the peer has.
     */
    public void addPeer(String peerId, byte[] bitfield) {
        peersBitfields.put(peerId, new Bitfield(bitfield));
    }

    /** Update the information about which pieces a peer has
     * (reflects a Have message)
     */
    public void updatePeer(String peerId, int index) {
        if (peersBitfields.containsKey(peerId)) {
            peersBitfields.get(peerId).setPiece(index, true);
        }
    }

    /** Removes a previously added peer (e.g used if a peer
     * connection is dropped
     */
    public synchronized void removePeer(String peerId) {
        peersBitfields.remove(peerId);
    }

    /**
     * Get the next Bock that should be requested from the
     * giver peer.
     *
     * If there are no more blocks left to retreive or if
     * this peer does not have any of the missing pieces
     * return Optional.empty().
     *
     *

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
    public synchronized Optional<Block> nextRequest(String peerId) {
        if (!peersBitfields.containsKey(peerId)) {
            return Optional.empty();
        }
        Optional<Block> block = expiredRequests(peerId);
        if (!block.isPresent()) {
            block = nextOngoing(peerId);
            if (!block.isPresent()) {
                Optional<Piece> rarestPiece = getRarestPiece(peerId);
                if (rarestPiece.isPresent()) {
                    block = rarestPiece.get().nextRequest();
                } else {
                    block = nextMissing(peerId);
                }
            }
        }
        return block;
    }

    /**
     * This method must be called when a block has
     * successfully been retrieved by a peer.
     *
     * Once a full piece have been retrieved, a SHA1 hash
     * control is made. If the check fails all the pieces
     * blocks are put back in missing state to be fetched
     * again. If the hash succeeds the partial piece is
     * written to disk and the piece is indicated as Have.
     */
    @SneakyThrows
    public synchronized void blockReceived(String peerId, long pieceIndex, long blockOffset, byte[] data) throws IOException {
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
            piece.blockReceived(blockOffset, data);
            if (piece.isComplete()) {
                if (piece.isHashMatching()) {
                    havePieces.add(piece);
                    ongoingPieces.remove(piece);
                    if (isComplete()) {
                        close();
                    }
                    pieceWriter.addPiece(piece);
                    long complete = havePieces.size();
                    LOGGER.info(complete + " / " + totalPieces + " pieces downloaded");
                } else {
                    LOGGER.info("Discarding corrupt piece #" + piece.getIndex());
                    piece.reset();
                }
            }
        }
    }

    /**
     * Go through previously requested blocks, if any one
     * have been in the requested state for longer than
     * `MAX_PENDING_TIME` return the block to be re-requested.
     * If no pending blocks exist, Optional.empty() is returned
     */
    public synchronized Optional<Block> expiredRequests(String peerId) {

        long current = System.currentTimeMillis();
        for (Block request : pendingBlocks) {
            int pieceNumber = request.getPiece();
            Bitfield currentBitfield = peersBitfields.get(peerId);
            if (currentBitfield != null && currentBitfield.hasPiece(pieceNumber)) {
                if (request.getAddedTime() + MAX_PENDING_TIME < current) {
                    LOGGER.info("Re-requesting block " + request.getOffset() + " for piece " + request.getPiece());
                    request.setAddedTime(current);
                    return Optional.of(request);
                }
            }
        } return Optional.empty();
    }

    /**
     * Go through the ongoing pieces and return the next
     * block to be requested or Optioanl.empty() if no
     * block is left to be requested.
     */
    public synchronized Optional<Block> nextOngoing(String peerId) {
        for (Piece piece : ongoingPieces) {
            int pieceNumber = piece.getIndex();
            Bitfield currentBitfield = peersBitfields.get(peerId);
            if (currentBitfield != null && currentBitfield.hasPiece(pieceNumber)) {
                Optional<Block> block = piece.nextRequest();
                if (block.isPresent()) {
                    pendingBlocks.add(block.get());
                    return block;
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Given the current list of missing pieces, get the
     * rarest one first (i.e. a piece which fewest of its
     * neighboring peers have)
     */
    public synchronized Optional<Piece> getRarestPiece(String peerId) {
        Map<Piece, Integer> pieceCount = new HashMap<>();

        for (Piece piece : missingPieces) {
            Bitfield bitfield = peersBitfields.get(peerId);
            if (bitfield.hasPiece(piece.getIndex())) {
                for (Map.Entry<String, Bitfield> p : peersBitfields.entrySet()) {
                    Bitfield bitfieldOther = peersBitfields.get(p.getKey());
                    if (bitfieldOther.hasPiece(piece.getIndex())) {
                        pieceCount.computeIfPresent(piece, (k, v) -> v + 1);
                        pieceCount.putIfAbsent(piece, 1);
                    }
                }
            }
        }

        if (pieceCount.isEmpty()) {
            return Optional.empty();
        }
        Piece rarestPiece = Collections
                .min(pieceCount.entrySet(),
                        Comparator.comparingInt(Map.Entry::getValue))
                .getKey();
        missingPieces.remove(rarestPiece);
        ongoingPieces.add(rarestPiece);
        return Optional.of(rarestPiece);
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
    public synchronized Optional<Block> nextMissing(String peerId) {
        for (Piece piece : missingPieces) {
            Bitfield bitfield = peersBitfields.get(peerId);
            if (bitfield.hasPiece(piece.getIndex())) {
                missingPieces.remove(piece);
                ongoingPieces.add(piece);
                return piece.nextRequest();
            }
        }
        return Optional.empty();
    }

    public int getPendingBlocksSize() {
        return pendingBlocks.size();
    }

    public void addMissingPiecesListListener(ListChangeListener<Piece> pieceListChangeListener) {
         missingPieces.addListener(pieceListChangeListener);
    }

    public void addHavePiecesListListener(ListChangeListener<Piece> pieceListChangeListener) {
        havePieces.addListener(pieceListChangeListener);
    }

    public void addOngoingPiecesListListener(ListChangeListener<Piece> pieceListChangeListener) {
        ongoingPieces.addListener(pieceListChangeListener);
    }

    public void addPendingBlockListListener(ListChangeListener<Block> pieceListChangeListener) {
        pendingBlocks.addListener(pieceListChangeListener);
    }
}
