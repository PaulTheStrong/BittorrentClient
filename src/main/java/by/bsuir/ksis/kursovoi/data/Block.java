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

    private int piece;
    private int offset;
    private int length;

    private Status status;
    private byte[] data;

    private long addedTime;

    public Block(int piece, int offset, int length) {
        this.piece = piece;
        this.offset = offset;
        this.length = length;
        this.status = Status.MISSING;
        this.addedTime = System.currentTimeMillis();
    }

    public int getPiece() {
        return piece;
    }

    public void setPiece(int piece) {
        this.piece = piece;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
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

    public long getAddedTime() {
        return addedTime;
    }

    public void setAddedTime(long addedTime) {
        this.addedTime = addedTime;
    }

    @Override
    public String toString() {
        return "Block{" +
                "piece=" + piece +
                ", offset=" + offset +
                ", length=" + length +
                ", status=" + status +
                '}';
    }
}
