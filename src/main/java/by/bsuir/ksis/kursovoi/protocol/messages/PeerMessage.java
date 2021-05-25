package by.bsuir.ksis.kursovoi.protocol.messages;


import java.util.Optional;

/**
 *
 * A message between two peers.
 * All of the remaining messages in the protocol take the form of:
 * <length prefix><message ID><payload>
 * - The length prefix is a four byte big-endian value.
 * - The message ID is a single decimal byte.
 * - The payload is message dependent.
 * NOTE: The Handshake message is different in layout compared to
 * the other messages.
 * Read more:
 * https://wiki.theory.org/BitTorrentSpecification#Messages
 */
public abstract class PeerMessage {

    /**
     * Encodes this object instance to the raw bytes representing
     * the entire message (ready to be transmitted).
     */
    public abstract byte[] encode();

    public abstract MessageType getType();

}
