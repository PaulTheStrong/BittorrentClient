package by.bsuir.ksis.kursovoi.protocol.messages;

import java.nio.ByteBuffer;

/**
 * The cancel message is fixed length, and is used to cancel
 * block requests. The payload is identical to that of the
 * "request" message. It is typically used during "End Game"
 *
 * Message format: <br/>
 * cancel: {len=0013}{id=8}{index}{begin}{length}
 */
public class CancelMessage extends PeerMessage {

    private static final int LENGTH = 13;

    /** integer specifying the zero-based piece index */
    private final int index;

    /** integer specifying the zero-based byte offset
     * within the piece */
    private final int begin;

    /** integer specifying the requested length. */
    private final int length;

    public CancelMessage(int index, int begin, int length) {
        this.index = index;
        this.begin = begin;
        this.length = length;
    }

    @Override
    public byte[] encode() {
        byte[] result = new byte[4 + 1 + LENGTH];
        result[3] = LENGTH;
        result[4] = (byte) MessageType.CANCEL.ordinal();
        ByteBuffer.wrap(result, 5, 4).putInt(index);
        ByteBuffer.wrap(result, 9, 4).putInt(begin);
        ByteBuffer.wrap(result, 13, 4).putInt(length);
        return result;
    }

    public static CancelMessage decode(byte[] bytes) {

        int index = ByteBuffer.wrap(bytes, 1, 4).getInt();
        int begin = ByteBuffer.wrap(bytes, 5, 4).getInt();
        int length = ByteBuffer.wrap(bytes, 9, 4).getInt();

        return new CancelMessage(index, begin, length);
    }

    @Override
    public String toString() {
        return "CancelMessage{" +
                "index=" + index +
                ", begin=" + begin +
                ", length=" + length +
                '}';
    }
}
