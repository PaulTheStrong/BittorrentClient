package by.bsuir.ksis.kursovoi.data;

/**
 * The block is a partial piece, this is what is requested
 * and transferred between peers.
 *
 * A block is most often of the same size as the REQUEST_SIZE,
 * except for the final block which might (most likely)
 * is smaller than REQUEST_SIZE.
 */
public class Block {

    public enum Status {
        MISSING, PENDING, RETRIEVED
    }

    private long piece;
    private long offset;
    private long length;

    private Status status;
    private byte[] data;

    public Block(long piece, long offset, long length) {
        this.piece = piece;
        this.offset = offset;
        this.length = length;
        this.status = Status.MISSING;
    }

    public long getPiece() {
        return piece;
    }

    public void setPiece(long piece) {
        this.piece = piece;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
