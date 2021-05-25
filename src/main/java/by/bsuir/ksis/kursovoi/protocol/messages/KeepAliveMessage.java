package by.bsuir.ksis.kursovoi.protocol.messages;


/**
 * The Keep-Alive message has no payload and length is set to
 * zero.
 * Message format:
 *  {len=0000}
 */
public class KeepAliveMessage extends PeerMessage {

    @Override
    public byte[] encode() {
        return new byte[4];
    }

    @Override
    public String toString() {
        return "KeepAliveMessage{}";
    }

    @Override
    public MessageType getType() {
        return MessageType.KEEPALIVE;
    }
}
