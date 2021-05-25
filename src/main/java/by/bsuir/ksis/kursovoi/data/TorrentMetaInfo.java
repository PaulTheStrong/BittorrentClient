package by.bsuir.ksis.kursovoi.data;

import by.bsuir.ksis.kursovoi.Utils;

import java.util.List;
import java.util.Random;

/**
 * Parsed info placed in torrent file.
 */
public class TorrentMetaInfo {

    /** Link to the tracker */
    private String announce;

    /** List of availableTrackers */
    private List<String> announceList;

    /** The name of the directory, where files shall be
     *  placed if many. Otherwise filename.
     */
    private String name;

    /** Random unique identifies of user, that downloads
     * file in hexadecimal numbers.
     */
    private String peerId;

    /** List of filenames and lengths, associated with them. */
    private List<FileDescription> files;

    /** 20 bytes SHA1 hashsums for each piece. */
    private byte[][] pieceHashes;

    /** The length of each piece in bytes */
    private int pieceLength;

    /** URLEncoded 20byte SHA1 hash of the <i>value</i>
     * of the <i>key</i> from metainfo file. Shall be
     * precalculated.
     */
    private String infoHash;

    public TorrentMetaInfo(String announce, List<String> announceList, String name, List<FileDescription> files, byte[][] pieceHashes, int pieceLength, String infoHash) {
        this.announce = announce;
        this.announceList = announceList;
        this.name = name;
        this.files = files;
        this.pieceHashes = pieceHashes;
        this.pieceLength = pieceLength;
        this.infoHash = infoHash;
        this.peerId = generatePeerId();
    }

    public String generatePeerId() {
        Random random = new Random();
        byte[] bytes = new byte[20];
        for (int i = 0; i < 20; i++) {
            bytes[i] = (byte) ((byte) random.nextInt(256) - 128);
        }
        return Utils.byteArray2Hex(bytes);
    }

    public String getAnnounce() {
        return announce;
    }

    public void setAnnounce(String announce) {
        this.announce = announce;
    }

    public List<String> getAnnounceList() {
        return announceList;
    }

    public void setAnnounceList(List<String> announceList) {
        this.announceList = announceList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FileDescription> getFiles() {
        return files;
    }

    public void setFiles(List<FileDescription> files) {
        this.files = files;
    }

    public byte[][] getPieceHashes() {
        return pieceHashes;
    }

    public void setPieceHashes(byte[][] pieceHashes) {
        this.pieceHashes = pieceHashes;
    }

    public int getPieceLength() {
        return pieceLength;
    }

    public void setPieceLength(int pieceLength) {
        this.pieceLength = pieceLength;
    }

    public String getInfoHash() {
        return infoHash;
    }

    public void setInfoHash(String infoHash) {
        this.infoHash = infoHash;
    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public long getTotalSize() {
        return files.stream().mapToLong(FileDescription::getLength).sum();
    }
}
