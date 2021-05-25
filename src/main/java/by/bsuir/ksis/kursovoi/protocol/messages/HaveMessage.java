package by.bsuir.ksis.kursovoi.protocol.messages;

import java.nio.ByteBuffer;

/**
 *  Represents a piece successfully downloaded by the remote peer.
 *  The piece is a zero based index of the torrents pieces
 *
 *  Message format:
 *  {len=0005}{id=4}{piece index}
 */
public class HaveMessage extends PeerMessage {

    private static final int LENGTH = 5;

    private final int index;

    public HaveMessage(int index) {
        this.index = index;
    }

    @Override
    public byte[] encode() {
        byte[] result = new byte[4 + 1 + 4];
        result[3] = LENGTH;
        result[4] = (byte) MessageType.HAVE.ordinal();
        ByteBuffer.wrap(result, 5, 4).putInt(index);
        return result;
    }

    public static HaveMessage decode(byte[] bytes) {

        int index = ByteBuffer.wrap(bytes, 1, 4).getInt();

        return new HaveMessage(index);
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return "HaveMessage{" +
                "index=" + index +
                '}';
    }

    @Override
    public MessageType getType() {
        return MessageType.HAVE;
    }

}
