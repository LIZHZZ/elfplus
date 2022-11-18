package org.urbcomp.startdb.compress.elf.xorcompressor;

import gr.aueb.delorean.chimp.OutputBitStream;

import java.util.Arrays;

public class ElfXORCompressor {
    private int storedLeadingZeros = Integer.MAX_VALUE;
    private final long[] storedValues;
    private boolean first = true;
    private int size;
    private final int threshold;
    private final static long END_SIGN = 0x0000000000000001L;
    public final static short[] leadingRepresentation =
                    {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7,
                                    7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
                                    7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7};

    public final static short[] leadingRound =
                    {0, 0, 0, 0, 0, 0, 0, 0, 8, 8, 8, 8, 12, 12, 12, 12, 16, 16, 18, 18, 20, 20, 22,
                                    22, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
                                    24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
                                    24, 24, 24, 24, 24, 24, 24, 24, 24};

    private final OutputBitStream out;
    private final int previousValues;

    private final long setLsb;

    private final int shiftCount;
    private final int[] indices;
    private int index = 0;
    private int current = 0;
    private final int flagOneSize;
    private final int flagZeroSize;

    // We should have access to the series?
    public ElfXORCompressor(int previousValues) {
        out = new OutputBitStream(
                        new byte[8125]); // for elf, we need one more bit for each at the worst case
        size = 0;
        this.previousValues = previousValues;
        int previousValuesLog2 = (int) (Math.log(previousValues) / Math.log(2));
        this.threshold = 6 + previousValuesLog2;
        this.shiftCount = 64 - threshold - 1;
        this.setLsb = ((long) Math.pow(2, threshold + 1) - 1) << shiftCount;
        this.indices = new int[(int) Math.pow(2, threshold + 1)];
        Arrays.fill(this.indices, -1);
        this.storedValues = new long[previousValues];
        this.flagZeroSize = previousValuesLog2 + 2;
        this.flagOneSize = previousValuesLog2 + 11;
    }

    public OutputBitStream getOutputStream() {
        return out;
    }

    public byte[] getOut() {
        return out.getBuffer();
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
        storedValues[current] = value;
        int trailingZeros = Long.numberOfTrailingZeros(value);
        out.writeInt(trailingZeros, 6);
        out.writeLong(storedValues[current] >>> trailingZeros, 64 - trailingZeros);
        int key = (int) ((value & setLsb) >>> shiftCount);
        indices[key] = index;
        size += 70 - trailingZeros;
        return 70 - trailingZeros;
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
        int key = (int) ((value & setLsb) >>> shiftCount);
        long xor;
        int previousIndex;
        int trailingZeros = 0;
        int currIndex = indices[key];
        if (currIndex!= -1 && (index - currIndex) < previousValues) {
            long tempXor = value ^ storedValues[currIndex % previousValues];
            trailingZeros = Long.numberOfTrailingZeros(tempXor);
            if (trailingZeros > threshold) {
                previousIndex = currIndex % previousValues;
                xor = tempXor;
            } else {
                previousIndex = index % previousValues;
                xor = storedValues[previousIndex] ^ value;
            }
        } else {
            previousIndex = index % previousValues;
            xor = storedValues[previousIndex] ^ value;
        }

        if (xor == 0) {
            // case 00
            out.writeInt(previousIndex, this.flagZeroSize);
            size += this.flagZeroSize;
            thisSize += this.flagZeroSize;
            storedLeadingZeros = 65;
        } else {
            int leadingZeros = leadingRound[Long.numberOfLeadingZeros(xor)];

            if (trailingZeros > threshold) {
                // case 01
                int significantBits = 64 - leadingZeros - trailingZeros;
                out.writeInt(512 * (previousValues + previousIndex)
                                                + 64 * leadingRepresentation[leadingZeros] + significantBits,
                                this.flagOneSize);
                out.writeLong(xor >>> trailingZeros,
                                significantBits); // Store the meaningful bits of XOR
                size += significantBits + this.flagOneSize;
                thisSize += significantBits + this.flagOneSize;
                storedLeadingZeros = 65;
            } else if (leadingZeros == storedLeadingZeros) {
                // case 10
                out.writeInt(2, 2);
                trailingZeros = Long.numberOfTrailingZeros(xor);
                int significantBits = 64 - leadingZeros - trailingZeros;
                out.writeInt(significantBits, 6);
                out.writeLong(xor >>> trailingZeros, significantBits);
                size += 8 + significantBits;
                thisSize += 8 + significantBits;
            } else {
                // case 11
                storedLeadingZeros = leadingZeros;
                int significantBits = 64 - leadingZeros;
                out.writeInt(3, 2);
                out.writeInt(leadingRepresentation[leadingZeros], 3);
                out.writeLong(xor, significantBits);
                size += 5 + significantBits;
                thisSize += 5 + significantBits;
            }
        }
        current = (current + 1) % previousValues;
        storedValues[current] = value;
        index++;
        indices[key] = index;
        return thisSize;
    }

    public int getSize() {
        return size;
    }

}