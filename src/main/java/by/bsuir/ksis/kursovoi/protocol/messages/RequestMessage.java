package by.bsuir.ksis.kursovoi.protocol.messages;

import java.nio.ByteBuffer;

/**
 * The message used to request a block of a piece (i.e. a partial
 * piece).
 *
 * The request size for each block is 2^14 bytes, except the final
 * block that might be smaller (since not all pieces might be
 * evenly divided by the request size).
 * Message format:
 *     {len=0013}{id=6}{index}{begin}{length}
 */
public class RequestMessage extends PeerMessage {

    private static final int LENGTH = 13;

    /** integer specifying the zero-based piece index */
    private final int index;

    /** integer specifying the zero-based byte offset
     * within the piece */
    private final int begin;

    /** integer specifying the requested length. */
    private final int length;

    public RequestMessage(int index, int begin, int length) {
        this.index = index;
        this.begin = begin;
        this.length = length;
    }

    @Override
    public byte[] encode() {
        byte[] result = new byte[4 + 1 + LENGTH];
        result[3] = LENGTH;
        result[4] = (byte) MessageType.REQUEST.ordinal();
        ByteBuffer.wrap(result, 5, 4).putInt(index);
        ByteBuffer.wrap(result, 9, 4).putInt(begin);
        ByteBuffer.wrap(result, 13, 4).putInt(length);
        return result;
    }

    public static RequestMessage decode(byte[] data) {
        int index = ByteBuffer.wrap(data, 1, 4).getInt();
        int begin = ByteBuffer.wrap(data, 4, 4).getInt();
        int length = ByteBuffer.wrap(data, 9, 4).getInt();

        return new RequestMessage(index, begin, length);
    }

    public int getIndex() {
        return index;
    }

    public int getBegin() {
        return begin;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "RequestMessage{" +
                "index=" + index +
                ", begin=" + begin +
                ", length=" + length +
                '}';
    }
}
