package by.bsuir.ksis.kursovoi.protocol;

import by.bsuir.ksis.kursovoi.protocol.messages.PeerMessage;

import java.io.IOException;
import java.io.OutputStream;

public class PeerOutputStream extends OutputStream {

    private final OutputStream messageOutputStream;

    public PeerOutputStream(OutputStream messageOutputStream) {
        this.messageOutputStream = messageOutputStream;
    }

    @Override
    public void write(int b) throws IOException {
        messageOutputStream.write(b);
    }

    public void writeMessage(PeerMessage peerMessage) throws IOException {
        messageOutputStream.write(peerMessage.encode());
        messageOutputStream.flush();
    }


}
