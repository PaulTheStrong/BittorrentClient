package by.bsuir.ksis.kursovoi.protocol.messages;

/**
 * The choke message is used to tell the other peer to stop
 * send request messages until unchoked.
 * Message format:
 *     {len=0001}{id=0}
 */
public class ChokeMessage extends PeerMessage {

    private static final int LENGTH = 1;

    @Override
    public byte[] encode() {
        byte[] result = new byte[5];
        result[3] = LENGTH;
        result[4] = (byte) MessageType.CHOKE.ordinal();
        return result;
    }

    public static ChokeMessage decode(byte[] data) {
        return new ChokeMessage();
    }

    @Override
    public String toString() {
        return "ChokeMessage{}";
    }
}
