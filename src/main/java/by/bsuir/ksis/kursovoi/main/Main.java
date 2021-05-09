package by.bsuir.ksis.kursovoi.main;

import by.bsuir.ksis.kursovoi.Utils;
import by.bsuir.ksis.kursovoi.client.Tracker;
import by.bsuir.ksis.kursovoi.protocol.Peer;
import by.bsuir.ksis.kursovoi.protocol.PeerInputStream;
import by.bsuir.ksis.kursovoi.protocol.PeerOutputStream;
import by.bsuir.ksis.kursovoi.protocol.ProtocolException;
import by.bsuir.ksis.kursovoi.protocol.messages.HandshakeMessage;
import by.bsuir.ksis.kursovoi.protocol.messages.PeerMessage;
import by.bsuir.ksis.kursovoi.utils.BencoderParser;
import by.bsuir.ksis.kursovoi.data.TorrentMetaInfo;
import by.bsuir.ksis.kursovoi.data.TrackerResponse;
import com.github.cdefgah.bencoder4j.BencodeFormatException;
import lombok.SneakyThrows;

import java.io.*;
import java.net.Socket;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {

    @SneakyThrows
    public static void main(String[] args) {
        BencoderParser parser = new BencoderParser();
        TorrentMetaInfo torrent = parser.parseInfoDictionary("src/main/resources/anime.torrent");
        Tracker tracker = new Tracker(torrent);
        TrackerResponse trackerResponse = tracker.connect(true, 0, 0);

        System.out.println(trackerResponse);

        List<Peer> peers = trackerResponse.getPeers();
        Peer peer = peers.get(3);
        Socket socket = new Socket(peer.getIp(), peer.getPort());
        HandshakeMessage me = new HandshakeMessage(Utils.hex2ByteArray(torrent.getInfoHash()), Utils.hex2ByteArray(torrent.getPeerId()));
        PeerInputStream peerInputStream = new PeerInputStream(socket.getInputStream());
        PeerOutputStream peerOutputStream = new PeerOutputStream(socket.getOutputStream());

        peerOutputStream.writeMessage(me);
        HandshakeMessage handshake = peerInputStream.readHandshake();

        while (true) {
            if (peerInputStream.available() != 0) {
                PeerMessage message = peerInputStream.readNextMessage();
                System.out.println(message);
            } else {
                System.out.println("sleeping");
                TimeUnit.SECONDS.sleep(1);
            }
        }

        //System.out.println(trackerResponse);
        //socket.close();
    }

}
