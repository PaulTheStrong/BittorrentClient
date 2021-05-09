package by.bsuir.ksis.kursovoi.protocol.messages;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Piece message format: <br/>
 *  {len=0009+X}{id=7}{index}{begin}{block}
 *
 * The piece message is variable length, where X is the length
 * of the block.
 */
public class PieceMessage extends PeerMessage {

    /** integer specifying the zero-based piece index */
    private final int index;

    /** integer specifying the zero-based byte offset
     * within the piece */
    private final int begin;

    /** block of data, which is a subset of the piece
     *  specified by index */
    private final byte[] data;

    public PieceMessage(int index, int begin, byte[] data) {
        this.index = index;
        this.begin = begin;
        this.data = data;
    }

    @Override
    public byte[] encode() {
        byte[] result = new byte[4 + 9 + data.length];
        ByteBuffer.wrap(result, 0, 4).putInt(9 + data.length);
        result[4] = (byte) MessageType.PIECE.ordinal();
        ByteBuffer.wrap(result, 5, 4).putInt(index);
        ByteBuffer.wrap(result, 9, 4).putInt(begin);
        System.arraycopy(data, 0, result, 13, data.length);

        return result;
    }

    public static PieceMessage decode(byte[] bytes) {

        int index = ByteBuffer.wrap(bytes, 1, 4).getInt();
        int begin = ByteBuffer.wrap(bytes, 5, 4).getInt();
        int dataLength = bytes.length - 9;
        byte[] data = new byte[dataLength];
        System.arraycopy(bytes, 9, data, 0, dataLength);

        return new PieceMessage(index, begin, data);
    }

    public int getIndex() {
        return index;
    }

    public int getBegin() {
        return begin;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "PieceMessage{" +
                "index=" + index +
                ", begin=" + begin +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
