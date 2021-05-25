package by.bsuir.ksis.kursovoi.utils;

import by.bsuir.ksis.kursovoi.data.FileDescription;
import by.bsuir.ksis.kursovoi.data.TorrentMetaInfo;
import by.bsuir.ksis.kursovoi.data.TrackerResponse;
import by.bsuir.ksis.kursovoi.protocol.Peer;
import com.github.cdefgah.bencoder4j.BencodeFormatException;
import com.github.cdefgah.bencoder4j.CircularReferenceException;
import com.github.cdefgah.bencoder4j.io.BencodeStreamIterator;
import com.github.cdefgah.bencoder4j.model.*;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static by.bsuir.ksis.kursovoi.Utils.SHAsum;

public class BencoderParser {

    public static final String ANNOUNCE = "announce";
    public static final String NAME = "name";
    public static final String INFO = "info";
    public static final String FILES = "files";
    public static final String LENGTH = "length";
    public static final String PATH = "path";
    public static final String PIECE_LENGTH = "piece length";

    public String bencodedByteSequenceToString(BencodedByteSequence sequence) {
        return new String(sequence.getByteSequence());
    }

    public TorrentMetaInfo parseInfoDictionary(String path) throws IOException, BencodeFormatException, NoSuchAlgorithmException {
        BencodeStreamIterator bencodeStreamIterator = new BencodeStreamIterator(new FileInputStream(path));
        BencodedDictionary dictionary = (BencodedDictionary) bencodeStreamIterator.next();

        String announce = bencodedByteSequenceToString((BencodedByteSequence) dictionary.get(ANNOUNCE));

        List<String> announceList = new ArrayList<>();
        announceList.add(announce);
        if (dictionary.containsKey("announce-list")) {
            BencodedList bencodedObjects = (BencodedList) dictionary.get("announce-list");
            for (BencodedObject bencodedAnnounce : bencodedObjects) {
                announceList.add(bencodedByteSequenceToString((BencodedByteSequence) ((BencodedList) bencodedAnnounce).get(0)));
            }
        }

        BencodedDictionary info = (BencodedDictionary) dictionary.get(INFO);

        String name = bencodedByteSequenceToString((BencodedByteSequence) info.get(NAME));
        long pieceLength = ((BencodedInteger) info.get(PIECE_LENGTH)).getValue();
        byte[] piecesString = ((BencodedByteSequence) info.get("pieces")).getByteSequence();

        int piecesCount = piecesString.length / 20;
        byte[][] pieces = new byte[piecesCount][20];
        for (int i = 0; i < piecesCount; i ++) {
            pieces[i] = new byte[20];
            System.arraycopy(piecesString, i * 20, pieces[i], 0, 20);
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            info.writeObject(os);
        } catch (CircularReferenceException e) {
            e.printStackTrace();
        }


        String infoHash = SHAsum(os.toByteArray());

        System.out.println(infoHash);

        List<FileDescription> descriptions = new ArrayList<>();
        if (info.containsKey(FILES)) {
            BencodedList files = (BencodedList) info.get(FILES);
            for (int i = 0; i < files.size(); i++) {
                BencodedDictionary fileInfo = (BencodedDictionary) files.get(i);
                long fileLength = ((BencodedInteger) fileInfo.get(LENGTH)).getValue();
                StringBuilder fileName = new StringBuilder();
                BencodedList fileNamesList = (BencodedList) fileInfo.get(PATH);
                for (int j = 0; j < fileNamesList.size(); j++) {
                    fileName.append("/").append(bencodedByteSequenceToString((BencodedByteSequence) fileNamesList.get(j)));
                }
                descriptions.add(new FileDescription(fileName.toString(), fileLength));
            }
        } else {
            long fileLength = ((BencodedInteger) info.get(LENGTH)).getValue();
            descriptions.add(new FileDescription(name, fileLength));
        }

        return new TorrentMetaInfo(announce, announceList, name, descriptions, pieces, (int) pieceLength, infoHash);
    }

    public TrackerResponse parseTrackerResponse(String response) throws IOException, BencodeFormatException {
        BencodeStreamIterator bencodeStreamIterator = new BencodeStreamIterator(new StringBufferInputStream(response));
        BencodedDictionary bencodedResponse = (BencodedDictionary) bencodeStreamIterator.next();

        String failureReason = bencodedResponse.get("failure reason") != null ? bencodedByteSequenceToString(((BencodedByteSequence) bencodedResponse.get("failure reason"))) : null;

        if (failureReason == null) {
            String warningMessage = bencodedResponse.get("warning message") != null ? bencodedByteSequenceToString(((BencodedByteSequence) bencodedResponse.get("warning message"))) : null;
            String trackerId = bencodedResponse.get("tracker id") != null ? bencodedByteSequenceToString(((BencodedByteSequence) bencodedResponse.get("tracker id"))) : null;

            long interval = ((BencodedInteger) bencodedResponse.get("interval")).getValue();
            long minInterval = ((BencodedInteger) bencodedResponse.get("min interval")).getValue();
            long complete = bencodedResponse.get("complete") != null ? ((BencodedInteger) bencodedResponse.get("complete")).getValue() : -1;
            long incomplete = bencodedResponse.get("incomplete") != null ? ((BencodedInteger) bencodedResponse.get("incomplete")).getValue() : -1;

            byte[] peersBytes = ((BencodedByteSequence) bencodedResponse.get("peers")).getByteSequence();

            List<Peer> peers = new ArrayList<>();

            for (int i = 0; i < peersBytes.length; i += 6) {
                int ip0 = peersBytes[i] < 0 ? 128 - peersBytes[i] : peersBytes[i];
                int ip1 = peersBytes[i + 1] < 0 ? 256 + peersBytes[i + 1] : peersBytes[i + 1];
                int ip2 = peersBytes[i + 2] < 0 ? 256 + peersBytes[i + 2] : peersBytes[i + 2];
                int ip3 = peersBytes[i + 3] < 0 ? 256 + peersBytes[i + 3] : peersBytes[i + 3];

                int port0 = peersBytes[i + 4] < 0 ? 256 + peersBytes[i + 4] : peersBytes[i + 4];
                int port1 = peersBytes[i + 5] < 0 ? 256 + peersBytes[i + 5] : peersBytes[i + 5];

                String ip = ip0 + "." + ip1 + "." + ip2 + "." + ip3;
                int port = port0 * 256 + port1;
                peers.add(new Peer(ip, port));
            }

            return new TrackerResponse(null, warningMessage, interval, minInterval, trackerId, complete, incomplete, peers);
        } else {
            return TrackerResponse.failure(failureReason);
        }
    }

}
