package by.bsuir.ksis.kursovoi.protocol.messages;

import by.bsuir.ksis.kursovoi.protocol.Peer;
import lombok.SneakyThrows;

import java.lang.reflect.Method;

public enum MessageType {
    CHOKE(ChokeMessage.class),
    UNCHOKE(UnchokeMessage.class),
    INTERESTED(InterestedMessage.class),
    NOT_INTERESTED(NotInterested.class),
    HAVE(HaveMessage.class),
    BITFIELD(BitFieldMessage.class),
    REQUEST(RequestMessage.class),
    PIECE(PieceMessage.class),
    CANCEL(CancelMessage.class),
    PORT(null),
    HANDSHAKE(null),
    KEEPALIVE(null);

    private Class<? extends PeerMessage> messageClass;

    MessageType(Class<? extends PeerMessage> messageClass) {
        this.messageClass = messageClass;
    }

    @SneakyThrows
    public PeerMessage decode(byte[] data) {
        Method decode = messageClass.getMethod("decode", byte[].class);
        return (PeerMessage) decode.invoke(null, data);
    }

}