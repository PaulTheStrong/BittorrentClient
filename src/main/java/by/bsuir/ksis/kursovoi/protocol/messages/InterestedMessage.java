package by.bsuir.ksis.kursovoi.protocol.messages;

/**
 *  The interested message is fix length and has no payload
 *  other than the message identifiers. It is used to notify
 *  each other about interest in downloading pieces.
 *  Message format:
 *      {len=0001}{id=2}
 */
public class InterestedMessage extends PeerMessage {

    private static final int LENGTH = 1;

    @Override
    public byte[] encode() {
        byte[] result = new byte[5];
        result[3] = LENGTH;
        result[4] = (byte) MessageType.INTERESTED.ordinal();
        return result;
    }

    public InterestedMessage decode(byte[] data) {
        return new InterestedMessage();
    }

    @Override
    public String toString() {
        return "InterestedMessage{}";
    }

    @Override
    public MessageType getType() {
        return MessageType.INTERESTED;
    }
}
