package by.bsuir.ksis.kursovoi.protocol.messages;

import by.bsuir.ksis.kursovoi.Utils;
import org.apache.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

/**
 * The handshake message is the first message sent and then received from a
 * remote peer.
 * The messages is always 68 bytes long (for this version of BitTorrent
 * protocol).
 * Message format:<br/>
 *    {pstrlen}{pstr}{reserved}{info_hash}{peer_id}<br/>
 * In version 1.0 of the BitTorrent protocol:<br/>
 *    pstrlen = 19<br/>
 *    pstr = "BitTorrent protocol".<br/>
 * Thus length is:<br/>
 *    49 + len(pstr) = 68 bytes long.
 */
public class HandshakeMessage extends PeerMessage {

    private static final Logger LOGGER = Logger.getRootLogger();

    private static final int LENGTH = 49 + 19;
    private static final byte[] pstr = "BitTorrent protocol".getBytes(StandardCharsets.UTF_8);
    private static final byte pstrlen = 19;

    private final byte[] infoHash;
    private final byte[] peerId;

    public HandshakeMessage(byte[] infoHash, byte[] peerId) {
        this.infoHash = infoHash;
        this.peerId = peerId;
    }

    @Override
    public byte[] encode() {
        byte[] result = new byte[LENGTH];
        result[0] = pstrlen;
        System.arraycopy(pstr, 0, result, 1, pstrlen);
        System.arraycopy(infoHash, 0, result, 20 + 8, 20);
        System.arraycopy(peerId, 0, result, 40 + 8, 20);
        return result;
    }

    /**
     * Decodes the given BitTorrent message into a handshake message,
     * if not a valid message, Optional.empty() is returned.
     */
    public static HandshakeMessage decode(byte[] data) {
        LOGGER.debug("Decoding Handshake if length" + data.length);
        byte[] infoHash = new byte[20];
        byte[] peerId = new byte[20];

        System.arraycopy(data, 20, infoHash, 0, 20);
        System.arraycopy(data, 40, peerId, 0, 20);

        return new HandshakeMessage(infoHash, peerId);
    }

    public byte[] getInfoHash() {
        return infoHash;
    }

    public byte[] getPeerId() {
        return peerId;
    }

    @Override
    public String toString() {
        return "HandshakeMessage{" +
                "infoHash=" + Utils.byteArray2Hex(infoHash) +
                ", peerId=" + Utils.byteArray2Hex(peerId) +
                '}';
    }
}
