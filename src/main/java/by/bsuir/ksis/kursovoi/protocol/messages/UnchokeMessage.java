package by.bsuir.ksis.kursovoi.protocol.messages;

public class UnchokeMessage extends PeerMessage {

    private static final int LENGTH = 1;

    @Override
    public byte[] encode() {
        byte[] result = new byte[5];
        result[3] = LENGTH;
        result[4] = (byte) MessageType.UNCHOKE.ordinal();
        return result;
    }

    public UnchokeMessage decode(byte[] data) {
        return new UnchokeMessage();
    }

    @Override
    public String toString() {
        return "UnchokeMessage{}";
    }
}
