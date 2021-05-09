package by.bsuir.ksis.kursovoi.protocol.messages;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Optional;

/**
 * The BitField is a message with variable length where the payload
 * is a bit array representing all the pieces a peer have (1) or does
 * not have (0).
 * Message format:
 *      {len=0001+X}{id=5}{bitfield}
 */
public class BitFieldMessage extends PeerMessage {

    private final byte[] bitField;

    public BitFieldMessage(byte[] bitField) {
        this.bitField = bitField;
    }

    @Override
    public byte[] encode() {
        byte[] result = new byte[bitField.length + 1 + 4];
        ByteBuffer.wrap(result, 0, 4).putInt(bitField.length + 1);
        result[4] = (byte) MessageType.BITFIELD.ordinal();
        System.arraycopy(bitField, 0, result, 5, bitField.length);
        return result;
    }

    public static BitFieldMessage decode(byte[] message) {
        byte[] bitField = new byte[message.length - 1];
        System.arraycopy(message, 1, bitField, 0, message.length - 1);
        return new BitFieldMessage(bitField);
    }

    public byte[] getBitField() {
        return bitField;
    }

    @Override
    public String toString() {
        return "BitFieldMessage{" +
                "bitField=" + Arrays.toString(bitField) +
                '}';
    }
}
