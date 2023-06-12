package org.urbcomp.startdb.compress.elf.xorcompressor;

import gr.aueb.delorean.chimp.OutputBitStream;
import org.urbcomp.startdb.compress.elf.utils.Erase;

public class ElfDeltaCompressor {

    private int size;

    private final OutputBitStream out;

    private long storedVal;

    private final static long END_SIGN = Double.doubleToLongBits(Double.NaN);

    private double storedDeltaE;

    private int storedFAlpha;

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
    public int addValue(long value, int fAlpha) {
        if (first) {
            return writeFirst(value);
        } else {
            return compressValue(value, fAlpha);
        }
    }


    private int writeFirst(long value) {
        first = false;
        storedVal = value;
        int trailingZeros = Long.numberOfTrailingZeros(value);
        out.writeInt(trailingZeros, 7);
        if (trailingZeros < 64) {
            System.out.println(Long.toBinaryString(storedVal >>> (trailingZeros + 1)));
            out.writeLong(storedVal >>> (trailingZeros + 1), 63 - trailingZeros);
            size += 70 - trailingZeros;
            return 70 - trailingZeros;
        } else {
            size += 7;
            return 7;
        }
    }

    public void close() {
        out.writeInt(0, 2);
        out.flush();
    }


    private int compressValue(long value, int fAlpha) {
        int thisSize = 0;
        double delta = Double.longBitsToDouble(value) - Double.longBitsToDouble(storedVal);
        long deltaLong = Double.doubleToRawLongBits(delta);

        if (delta == 0) {
            //case 01
            out.writeInt(1, 2);
            System.out.println("flag 01");
            size += 2;
            thisSize += 2;
        } else {
            int maxFAlpha = Math.max(storedFAlpha, fAlpha);
            System.out.println("store :"+ storedFAlpha);
            storedFAlpha = fAlpha;
            int eDelta = (((int) (deltaLong >> 52)) & 0x7ff) - 1023;
            int trailZeros = 52 - maxFAlpha - eDelta;
            System.out.println("maxFAlpha :"+maxFAlpha);
            System.out.println("eDelta: "+eDelta);
            if(trailZeros>52){
                trailZeros=52;
            }
            if (eDelta == storedDeltaE) {
                out.writeInt(2, 2); // 10 deltaE = deltaE store


                //sign 1
                out.writeBit(delta < 0);    // 符号位

                out.writeLong(deltaLong >>> trailZeros, 52 - trailZeros);

                size += 3 + 52 - trailZeros;
                thisSize += 3 + 52 - trailZeros;
                System.out.println("flag 10");
                System.out.println("sign :" + (delta < 0));
                System.out.println(Long.toBinaryString(deltaLong >>> trailZeros));
                System.out.println("centerBits: " + (52 - trailZeros));

            } else {
                storedDeltaE = eDelta;
                out.writeInt(3, 2); // 11 deltaE != deltaE store
                //sign 1
                out.writeBit(delta < 0);    // 符号位
                out.writeLong(deltaLong >>> 52, 11);    //e bits
                out.writeLong(deltaLong >>> trailZeros, 52 - trailZeros);
                size += 3 + 11 + 52 - trailZeros;
                thisSize += 3 + 11 + 52 - trailZeros;
                System.out.println("flag 11");
                System.out.println("sign :" + (delta < 0));
                System.out.println(Long.toBinaryString(deltaLong >>> 52));
                System.out.println(Long.toBinaryString(deltaLong >>> trailZeros));
                System.out.println("centerBits: " + (52 - trailZeros));
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
