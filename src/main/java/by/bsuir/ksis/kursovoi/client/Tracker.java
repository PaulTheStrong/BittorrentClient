package by.bsuir.ksis.kursovoi.client;

import by.bsuir.ksis.kursovoi.data.FileDescription;
import by.bsuir.ksis.kursovoi.data.TorrentMetaInfo;
import by.bsuir.ksis.kursovoi.data.TrackerResponse;
import by.bsuir.ksis.kursovoi.http.HttpRequest;
import by.bsuir.ksis.kursovoi.http.HttpResponse;
import by.bsuir.ksis.kursovoi.utils.BencoderParser;
import com.github.cdefgah.bencoder4j.BencodeFormatException;

import java.io.IOException;
import java.util.List;

/**
 * Represents the connection to a tracker for a given Torrent
 * that is either under download or seeding state.
 */
public class Tracker {

    private static final BencoderParser parser = new BencoderParser();


    private final TorrentMetaInfo torrent;

    public Tracker(TorrentMetaInfo torrent) {
        this.torrent = torrent;
    }

    public TrackerResponse connect(boolean isFirst, long uploaded, long downloaded) throws IOException, BencodeFormatException {
        List<FileDescription> pieces = torrent.getFiles();
        String announceName = torrent.getAnnounce();

        String infoHash = torrent.getInfoHash();
        String peerId = torrent.getPeerId();
        int port = 6881;
        Long left = pieces.stream().map(FileDescription::getLength).reduce(Long::sum).get() - downloaded;
        int compact = 1;

        String requestString = announceName.substring(announceName.lastIndexOf('/')) +
                "?info_hash=" + infoHash +
                "&peer_id=" + peerId +
                "&port=" + port +
                "&uploaded=" + uploaded +
                "&downloaded=" + downloaded +
                "&left=" + left +
                "&compact=" + compact;
        if (isFirst) {
            requestString += "&event=started";
        }

        String announce = torrent.getAnnounce();
        String announceHost = announce.substring(announce.indexOf("//") + 2, announce.lastIndexOf("/"));

        HttpRequest request = new HttpRequest(announceHost, 80);
        HttpResponse responseString = request.get(requestString);

        return parser.parseTrackerResponse(responseString.getContent().trim());
    }
}
