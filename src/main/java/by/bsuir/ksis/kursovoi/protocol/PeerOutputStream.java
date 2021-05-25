package by.bsuir.ksis.kursovoi.protocol;

import by.bsuir.ksis.kursovoi.Utils;
import by.bsuir.ksis.kursovoi.protocol.messages.PeerMessage;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;

public class PeerOutputStream extends OutputStream {

    private static final Logger LOGGER = Logger.getRootLogger();

    private final OutputStream messageOutputStream;

    public PeerOutputStream(OutputStream messageOutputStream) {
        this.messageOutputStream = messageOutputStream;
    }

    @Override
    public void write(int b) throws IOException {
        messageOutputStream.write(b);
    }

    public void writeMessage(PeerMessage peerMessage) throws IOException {
        //LOGGER.debug("SENDING MESSAGE TYPE " + peerMessage.getType());
        //LOGGER.debug("DATA: " + Utils.byteArray2Hex(peerMessage.encode()));
        messageOutputStream.write(peerMessage.encode());
        messageOutputStream.flush();
    }


}
