package by.bsuir.ksis.kursovoi.data;

import lombok.SneakyThrows;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static by.bsuir.ksis.kursovoi.Utils.SHAsum;

/**
 * The piece is a part of of the torrents content.
 * Each piece except the final piece for a torrent
 * has the same length (the final piece might be shorter).
 *
 * A piece is what is defined in the torrent meta-data.
 * However, when sharing data between peers a smaller unit
 * is used - this smaller piece is refereed to as `Block`
 * by the unofficial specification (the official specification
 * uses piece for this one as well, which is slightly confusing).
 */
public class Piece {

    private static final Logger LOGGER = Logger.getRootLogger();

    private long index;
    private List<Block> blocks;
    private String hash;


    public Piece(long index, List<Block> blocks, String hash) {
        this.index = index;
        this.blocks = blocks;
        this.hash = hash;
    }

    /** Reset all blocks to MISSING regardless of current state */
    public void reset() {
        blocks.forEach(block -> block.setStatus(Block.Status.MISSING));
    }

    /** Get the next Block to be requested */
    public Optional<Block> nextRequest() {
        Optional<Block> first = blocks.stream().filter(block -> block.getStatus() == Block.Status.MISSING).findFirst();
        first.ifPresent(block -> block.setStatus(Block.Status.PENDING));
        return first;
    }

    /** Update Block information that the given block
     * is now received.
     * @param offset the block offset within the piece.
     * @param data the block data.
     */
    public void blockReceived(long offset, byte[] data) {
        Optional<Block> first = blocks.stream().filter(block -> block.getOffset() == offset).findFirst();
        if (first.isPresent()) {
            Block block = first.get();
            block.setStatus(Block.Status.RETRIEVED);
            block.setData(data);
        } else {
            LOGGER.warn("Trying to complete a non-existing block " + offset);
        }
    }

    /** Check if all blocks for the pieces has been retreived. */
    public boolean isComplete() {
        return blocks.stream().allMatch(block -> block.getStatus() != Block.Status.RETRIEVED);
    }

    /** Check if SHA1 hash for all received block match the
     * piece hash from the torrent meta-info file.
     * @return true if matches. False otherwise.
     */
    @SneakyThrows
    public boolean isHashMatching() {
        return hash.equals(SHAsum(getData()));
    }

    /** Returns data of the piece as one array */
    public byte[] getData() {
        int size = blocks.stream().mapToInt(b -> b.getData().length).sum();
        byte[] combined = new byte[size];
        int counter = 0;
        for (Block block : blocks) {
            byte[] data = block.getData();
            System.arraycopy(data, 0, combined, counter, data.length);
            counter += data.length;
        }
        return combined;
    }

    public long getIndex() {
        return index;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public String getHash() {
        return hash;
    }
}
