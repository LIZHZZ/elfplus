package org.urbcomp.startdb.compress.elf.xordecompressor;

import gr.aueb.delorean.chimp.InputBitStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ElfXORDecompressor {

    private long storedVal = 0;
    private final long[] storedValues;
    private int current = 0;
    private boolean first = true;
    private boolean endOfStream = false;

    private final InputBitStream in;
    private final int previousValues;
    private final int previousValuesLog2;

    public final static short[] leadingRepresentation = {0, 8, 12, 16, 18, 20, 22, 24};

    private final static long END_SIGN = 0x0000000000000001L;

    public ElfXORDecompressor(byte[] bs, int previousValues) {
        in = new InputBitStream(bs);
        this.previousValues = previousValues;
        this.previousValuesLog2 =  (int)(Math.log(previousValues) / Math.log(2));
        this.storedValues = new long[previousValues];
    }

    public InputBitStream getInputStream() {
        return in;
    }

    /**
     * Returns the next pair in the time series, if available.
     *
     * @return Pair if there's next value, null if series is done.
     */
    public Double readValue() {
        try {
            next();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        if(endOfStream) {
            return null;
        }
        return Double.longBitsToDouble(storedVal);
    }

    public List<Double> getValues() {
        List<Double> list = new ArrayList<>(1024);
        Double value = readValue();
        while (value != null) {
            list.add(value);
            value = readValue();
        }
        return list;
    }


    private void next() throws IOException {
        if (first) {
            first = false;
            int trailingZeros = in.readInt(6);
            storedVal = in.readLong(64 - trailingZeros) << trailingZeros;
            storedValues[current] = storedVal;
            if (storedValues[current] == END_SIGN) {
                endOfStream = true;
            }

        } else {
            nextValue();
        }
    }

    private void nextValue() throws IOException {
        // Read value
        int flag = in.readInt(1);
        long value;
        int centerBits;
        int trailingZeros;
        int leadingZeros;
        int m;

        if (flag == 1) {
            // case 1
            leadingZeros = leadingRepresentation[in.readInt(3)];
            centerBits = in.readInt(6);
            if(centerBits == 0) {
                centerBits = 64;
            }
            trailingZeros = 64 - leadingZeros - centerBits;
            value = in.readLong(centerBits) << trailingZeros;
            value = storedVal ^ value;

            if (value == END_SIGN) {
                endOfStream = true;
            } else {
                storedVal = value;
                current = (current + 1) % previousValues;
                storedValues[current] = storedVal;
            }
        } else {
            flag = in.readInt(1);
            if(flag == 1) {
                m = in.readInt(previousValuesLog2);
                storedVal = storedValues[m];
                leadingZeros = leadingRepresentation[in.readInt(3)];

                if (in.readInt(1) == 0) {
                    centerBits = in.readInt(4);
                    if (centerBits == 0) {
                        centerBits = 16;
                    }
                } else {
                    centerBits = in.readInt(6);
                    if (centerBits == 0) {
                        centerBits = 64;
                    }
                }

                trailingZeros = 64 - leadingZeros - centerBits;
                value = in.readLong(centerBits) << trailingZeros;

                value = storedVal ^ value;
                if (value == END_SIGN) {
                    endOfStream = true;
                } else {
                    storedVal = value;
                    current = (current + 1) % previousValues;
                    storedValues[current] = storedVal;
                }
            } else {
                // else -> same value as before
                storedVal = storedValues[(int) in.readLong(previousValuesLog2)];
                current = (current + 1) % previousValues;
                storedValues[current] = storedVal;
            }
        }
    }
}
