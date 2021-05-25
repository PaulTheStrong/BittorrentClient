package by.bsuir.ksis.kursovoi.protocol.messages;

/**
 * The not interested message is fix length and has no payload
 * other than the message identifier. It is used to notify each
 * other that there is no interest to download pieces.
 * Message format:
 *     <len=0001><id=3>
 */
public class NotInterested extends PeerMessage {

    private static final int LENGTH = 1;

    @Override
    public byte[] encode() {
        byte[] result = new byte[5];
        result[3] = LENGTH;
        result[4] = (byte) MessageType.NOT_INTERESTED.ordinal();
        return result;
    }

    public NotInterested decode(byte[] data) {
        return new NotInterested();
    }

    @Override
    public String toString() {
        return "NotInterested{}";
    }

    @Override
    public MessageType getType() {
        return MessageType.NOT_INTERESTED;
    }
}
