package by.bsuir.ksis.kursovoi.main;

import by.bsuir.ksis.kursovoi.Utils;
import by.bsuir.ksis.kursovoi.client.TorrentClient;
import by.bsuir.ksis.kursovoi.data.TorrentMetaInfo;
import by.bsuir.ksis.kursovoi.protocol.messages.HandshakeMessage;
import by.bsuir.ksis.kursovoi.utils.BencoderParser;
import lombok.SneakyThrows;

import java.net.*;

public class Main {

    @SneakyThrows
    public static void main(String[] args) {
        BencoderParser parser = new BencoderParser();

        TorrentMetaInfo torrent = parser.parseInfoDictionary("src/main/resources/many.torrent");
        //TorrentMetaInfo one = parser.parseInfoDictionary("src/main/resources/one.torrent");
        //TorrentMetaInfo anime = parser.parseInfoDictionary("src/main/resources/anime.torrent");

        TorrentClient firstClient = new TorrentClient(torrent);
        //TorrentClient secondClient = new TorrentClient(one);
        //TorrentClient thirdClient = new TorrentClient(anime);
        //new Thread(secondClient::start).start();
        firstClient.start();
        //new Thread(thirdClient::start).start();

        //System.out.println(trackerResponse);
        //socket.close();
    }

}
