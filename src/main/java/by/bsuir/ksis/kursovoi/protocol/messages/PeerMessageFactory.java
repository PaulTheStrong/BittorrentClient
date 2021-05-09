package by.bsuir.ksis.kursovoi.protocol.messages;

public class PeerMessageFactory {

    public static PeerMessage decodeData(byte[] data) {
        if (data.length == 0) {
            return new KeepAliveMessage();
        } else {
            byte type = data[0];
            return MessageType.values()[type].decode(data);
        }
    }

}
