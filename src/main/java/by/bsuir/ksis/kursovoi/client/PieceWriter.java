package by.bsuir.ksis.kursovoi.client;

import by.bsuir.ksis.kursovoi.data.Piece;
import by.bsuir.ksis.kursovoi.data.TorrentMetaInfo;
import lombok.SneakyThrows;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PieceWriter implements Runnable {

    private static final Logger LOGGER = Logger.getRootLogger();

    private final RandomAccessFile fileOutputStream;
    private final BlockingQueue<Piece> piecesToWrite = new LinkedBlockingQueue<Piece>();
    private final TorrentMetaInfo torrent;
    private final PieceManager pieceManager;

    private boolean isComplete = false;

    @SneakyThrows
    public PieceWriter(String filename, TorrentMetaInfo torrent, PieceManager pieceManager) {
        this.torrent = torrent;
        fileOutputStream = new RandomAccessFile("H:/downloads/" + filename, "rws");
        this.pieceManager = pieceManager;
    }

    public void close() throws IOException {
        fileOutputStream.close();
    }

    public synchronized void addPiece(Piece piece) throws InterruptedException {
        piecesToWrite.put(piece);
    }

    /** Write a given piece to disk */
    public void write(Piece piece) throws IOException {
        LOGGER.info("WRITING PIECE :" + piece.getIndex() + ". Pieces remain: " + piecesToWrite.size() + " IsComplete: "+ isComplete);
        fileOutputStream.getChannel().write(ByteBuffer.wrap(piece.getData()), (long) piece.getIndex() * torrent.getPieceLength());
    }

    @SneakyThrows
    @Override
    public void run() {
        while (!isComplete || !piecesToWrite.isEmpty()) {
            Piece piece = piecesToWrite.take();
            write(piece);
            piece.clear();
        }
        close();
        LOGGER.info("ENDING WRITING THREAD");
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }
}
