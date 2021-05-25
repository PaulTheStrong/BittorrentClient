package by.bsuir.ksis.kursovoi.data;

import java.util.Arrays;

/**
 * Represent the bitfield of the peer. Each bit of array of
 * bytes represents that peer has (1) or doesn't have the piece
 */
public class Bitfield {

    private final byte[] bitfield;

    public Bitfield(byte[] bitfield) {
        this.bitfield = bitfield;
    }

    public boolean hasPiece(int index) {
        int arrayIndex = index / 8;
        int bitIndex = index % 8;

        return (bitfield[arrayIndex] & (0b10000000 >> bitIndex)) != 0;
    }

    public void setPiece(int index, boolean value) {
        int arrayIndex = index / 8;
        int bitIndex = index % 8;

        if (value) {
            bitfield[arrayIndex] |= (0b10000000 >> bitIndex);
        } else {
            bitfield[arrayIndex] &= 0b11111111 ^ (0b10000000 >> bitIndex);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bitfield.length; i++) {
            for (int j = 0; j < 8; j++) {
                sb.append((i * 8 + j)).append(" bit is ").append(hasPiece(i * 8 + j)).append("\n");
            }
        }
        return sb.toString();
    }
}
