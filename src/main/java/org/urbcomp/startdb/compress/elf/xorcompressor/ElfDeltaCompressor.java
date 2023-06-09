package org.urbcomp.startdb.compress.elf.xorcompressor;

import gr.aueb.delorean.chimp.OutputBitStream;
import org.urbcomp.startdb.compress.elf.utils.Erase;

public class ElfDeltaCompressor {

    private int size;

    private final OutputBitStream out;

    private Erase storedVal;

    private final static long END_SIGN = Double.doubleToLongBits(Double.NaN);

    private boolean first = true;

    public ElfDeltaCompressor() {
        out = new OutputBitStream(
                new byte[10000]);  // for elf, we need one more bit for each at the worst case
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
    public int addValue(Erase value) {
        if (first) {
            return writeFirst(value);
        } else {
            return compressValue(value);
        }
    }


    private int writeFirst(Erase value) {
        first = false;
        storedVal = value;
        int trailingZeros = Long.numberOfTrailingZeros(value.getLongErasedValue());
        out.writeInt(trailingZeros, 7);
        if (trailingZeros < 64) {
            out.writeLong(storedVal.getLongErasedValue() >>> (trailingZeros + 1), 63 - trailingZeros);
            size += 70 - trailingZeros;
            return 70 - trailingZeros;
        } else {
            size += 7;
            return 7;
        }
    }

    public void close() {
//        addValue(END_SIGN);
        out.writeBit(false);
        out.flush();
    }


    private int compressValue(Erase value) {
        int thisSize = 0;
        double delta = storedVal.getErasedValue() - value.getErasedValue();
        long deltaLong = Double.doubleToRawLongBits(delta);

        if (delta == 0) {
            //case 01
            out.writeInt(1, 2);
            size += 2;
            thisSize += 2;
        } else {
            //sign 1
            if (delta < 0) {
                int fAlpha = Math.max(storedVal.getfAlpha(), value.getfAlpha());

                int eDelta = (((int) (deltaLong >> 52)) & 0x7ff) - 1023;
                int trailZeros = fAlpha - eDelta;

                out.writeBit(true);// 符号位
                out.writeLong(deltaLong >>> (12 + trailZeros), (52 - trailZeros));
            }
        }
        return 0;
    }


}
