package by.bsuir.ksis.kursovoi.protocol;

import by.bsuir.ksis.kursovoi.protocol.messages.HandshakeMessage;
import by.bsuir.ksis.kursovoi.protocol.messages.PeerMessage;
import by.bsuir.ksis.kursovoi.protocol.messages.PeerMessageFactory;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class PeerInputStream extends InputStream {

    private final InputStream messageStream;
    private final DataInputStream dataInputStream;

    public PeerInputStream(InputStream is) {
        BufferedInputStream bufStream = new BufferedInputStream(is);
        this.messageStream = bufStream;
        dataInputStream = new DataInputStream(bufStream);
    }
    @Override
    public int read() throws IOException {
        return messageStream.read();
    }

    public PeerMessage readNextMessage() throws IOException, InterruptedException, ProtocolException {
        int size = dataInputStream.readInt();
        int counter = 0;
        while (messageStream.available() < size) {
            Thread.yield();
        }
        byte[] data = new byte[size];
        int read = messageStream.read(data, 0, size);
        if (read != size) {
            throw new IOException("Cannot read whole message");
        }
        return PeerMessageFactory.decodeData(data);
    }

    public HandshakeMessage readHandshake() throws IOException, ProtocolException, InterruptedException {
        byte[] data = new byte[68];
        while (messageStream.available() < 68) {
            TimeUnit.MILLISECONDS.sleep(100);
        }
        int read = messageStream.read(data);
        if (read != 68) {
            throw new ProtocolException("Wrong handshake message length");
        }
        return HandshakeMessage.decode(data);
    }

    @Override
    public int available() throws IOException {
        return messageStream.available();
    }
}
