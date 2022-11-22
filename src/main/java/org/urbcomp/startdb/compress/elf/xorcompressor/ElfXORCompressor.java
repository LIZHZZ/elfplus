package org.urbcomp.startdb.compress.elf.xorcompressor;

import gr.aueb.delorean.chimp.OutputBitStream;

public class ElfXORCompressor {
    private int storedLeadingZeros = Integer.MAX_VALUE;

    private int storedTrailingZeros = Integer.MAX_VALUE;
    private long storedVal = 0;
    private boolean first = true;
    private int size;
    private final static long END_SIGN = Double.doubleToLongBits(Double.NaN);

    public final static short[] leadingRepresentation = {0, 0, 0, 0, 0, 0, 0, 0,
                    1, 1, 1, 1, 2, 2, 2, 2,
                    3, 3, 4, 4, 5, 5, 6, 6,
                    7, 7, 7, 7, 7, 7, 7, 7,
                    7, 7, 7, 7, 7, 7, 7, 7,
                    7, 7, 7, 7, 7, 7, 7, 7,
                    7, 7, 7, 7, 7, 7, 7, 7,
                    7, 7, 7, 7, 7, 7, 7, 7
    };

    public final static short[] leadingRound = {0, 0, 0, 0, 0, 0, 0, 0,
                    8, 8, 8, 8, 12, 12, 12, 12,
                    16, 16, 18, 18, 20, 20, 22, 22,
                    24, 24, 24, 24, 24, 24, 24, 24,
                    24, 24, 24, 24, 24, 24, 24, 24,
                    24, 24, 24, 24, 24, 24, 24, 24,
                    24, 24, 24, 24, 24, 24, 24, 24,
                    24, 24, 24, 24, 24, 24, 24, 24
    };
    //    public final static short FIRST_DELTA_BITS = 27;

    private final OutputBitStream out;

    public ElfXORCompressor() {
        out = new OutputBitStream(
                        new byte[8125]);  // for elf, we need one more bit for each at the worst case
        size = 0;
    }

    public OutputBitStream getOutputStream() {
        return this.out;
    }

    /**
     * Adds a new long value to the series. Note, values must be inserted in order.
     *
     * @param value next floating point value in the series
     */
    public int addValue(long value) {
        if (first) {
            return writeFirst(value);
        } else {
            return compressValue(value);
        }
    }

    /**
     * Adds a new double value to the series. Note, values must be inserted in order.
     *
     * @param value next floating point value in the series
     */
    public int addValue(double value) {
        if (first) {
            return writeFirst(Double.doubleToRawLongBits(value));
        } else {
            return compressValue(Double.doubleToRawLongBits(value));
        }
    }

    private int writeFirst(long value) {
        first = false;
        storedVal = value;
        int trailingZeros = Long.numberOfTrailingZeros(value);
        out.writeInt(trailingZeros, 7);
        out.writeLong(storedVal >>> trailingZeros, 64 - trailingZeros);

        size += 71 - trailingZeros;
        return 71 - trailingZeros;
    }

    /**
     * Closes the block and writes the remaining stuff to the BitOutput.
     */
    public void close() {
        addValue(END_SIGN);
        out.writeBit(false);
        out.flush();
    }

    private int compressValue(long value) {
        int thisSize = 0;
        long xor = storedVal ^ value;

        if (xor == 0) {
            // case 10
            out.writeInt(2, 2);

            size += 2;
            thisSize += 2;

            storedLeadingZeros = 65;
            storedTrailingZeros = 65;
        } else {
            int leadingZeros = leadingRound[Long.numberOfLeadingZeros(xor)];
            int trailingZeros = Long.numberOfTrailingZeros(xor);

            if (leadingZeros == storedLeadingZeros && trailingZeros >= storedTrailingZeros) {
                // case 11
                int centerBits = 64 - storedLeadingZeros - storedTrailingZeros;
                out.writeInt(3, 2);
                out.writeLong(xor >>> storedTrailingZeros, centerBits);

                size += 2 + centerBits;
                thisSize += 2 + centerBits;
            } else {
                storedLeadingZeros = leadingZeros;
                storedTrailingZeros = trailingZeros;
                int centerBits = 64 - storedLeadingZeros - storedTrailingZeros;

                if (centerBits <= 16) {
                    // case 00
                    out.writeInt(leadingRepresentation[storedLeadingZeros], 5); // 2 + 3
                    out.writeInt(centerBits, 4);
                    out.writeLong(xor >>> storedTrailingZeros, centerBits);

                    size += 9 + centerBits;
                    thisSize += 9 + centerBits;
                } else {
                    // case 01
                    out.writeInt((leadingRepresentation[storedLeadingZeros] & 0x7) + 8, 5); // 2 + 3
                    out.writeInt(centerBits, 6);
                    out.writeLong(xor >>> storedTrailingZeros, centerBits);

                    size += 11 + centerBits;
                    thisSize += 11 + centerBits;
                }
            }

            storedVal = value;
        }

        return thisSize;
    }

    public int getSize() {
        return size;
    }

    public byte[] getOut() {
        return out.getBuffer();
    }
}
