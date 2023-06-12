package org.urbcomp.startdb.compress.elf.xordecompressor;

import gr.aueb.delorean.chimp.InputBitStream;
import org.urbcomp.startdb.compress.elf.utils.DecompressBuffer;
import org.urbcomp.startdb.compress.elf.utils.Elf64Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ElfDeltaDecompressor {
    private long storedLongVal = 0;
    private double storedVal = 0;
    private boolean first = true;
    private boolean endOfStream = false;

    private final InputBitStream in;
    private final static long END_SIGN = Double.doubleToLongBits(Double.NaN);

    private final static short[] leadingRepresentation = {0, 8, 12, 16, 18, 20, 22, 24};

    public ElfDeltaDecompressor(byte[] bs) {
        in = new InputBitStream(bs);
    }

    public InputBitStream getInputStream() {
        return in;
    }

    /**
     * Returns the next pair in the time series, if available.
     *
     * @return Pair if there's next value, null if series is done.
     */
    public Double readValue(int betaStar) {
        try {
            next(betaStar);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        if (endOfStream) {
            return null;
        }
        System.out.println(storedVal);
        return storedVal;
    }

    private void next(int betaStar) throws IOException {
        if (first) {
            first = false;
            int trailingZeros = in.readInt(7);
            if (trailingZeros < 64) {
                storedLongVal = ((in.readLong(63 - trailingZeros) << 1) + 1) << trailingZeros;
                System.out.println(Long.toBinaryString(storedLongVal));
            } else {
                storedLongVal = 0;
            }
            if (storedLongVal == END_SIGN) {
                endOfStream = true;
            }
        } else {
            nextValue(betaStar);
        }
        storedVal = Double.longBitsToDouble(storedLongVal);
    }

    private DecompressBuffer buffer = new DecompressBuffer();


    private int storedFAlpha;
    private long storedEDelta;

    private void nextValue(int betaStar) throws IOException {
        long value;
        long sign;
        long eDelta;
        long mDelta;
        int flag = readInt(2);
        System.out.println("flag: "+flag);
        System.out.println("buffer: "+ buffer.getLen());
        switch (flag) {

            case 3:
                sign = readBit();
                eDelta = readLong(11);
                mDelta = readLong(52);
                computeV(betaStar,sign, eDelta, mDelta);
                break;
            case 2:
                sign = readBit();
                eDelta = storedEDelta;
                mDelta = readLong(52);
                computeV(betaStar,sign, eDelta, mDelta);
                break;
            case 0:
                System.out.println(Long.toBinaryString(buffer.getBuffer()));
                endOfStream = true;
                break;
            default:
                break;
        }
        System.out.println("buffer2: "+ buffer.getLen());
    }

    private void computeV(int betaStar, long sign, long eDelta, long mDelta) {
        long value;
        long fuzzyDeltaLong = sign << 63 | eDelta << 52 | mDelta;
        double fuzzyDelta = Double.longBitsToDouble(fuzzyDeltaLong);
        double fuzzyV = fuzzyDelta + storedVal;
        int alpha;
        int sp = Elf64Utils.getSP(Math.abs(fuzzyV));
        System.out.println("sp"+sp);
        if (betaStar == 0) {
            alpha = 1;
        } else {
            alpha = betaStar - sp - 1;
        }
        int fAlpha = Elf64Utils.getFAlpha(alpha);
        int maxFAlpha = Math.max(storedFAlpha, fAlpha);

        System.out.println("maxFAlpha "+maxFAlpha);
        int trailZeros = (int) (52 - maxFAlpha - (eDelta - 1023));
        if(trailZeros>52){
            trailZeros=52;
        }
        buffer.writeLong(fuzzyDeltaLong, trailZeros);
        System.out.println("trailZeros " +trailZeros);
        long mask = ~0L << trailZeros;
        long deltaLong = fuzzyDeltaLong & mask;
        double delta = Double.longBitsToDouble(deltaLong);
        System.out.println("delta :" +delta );
        value = Double.doubleToRawLongBits (delta + storedVal);

        if (value == END_SIGN) {
            endOfStream = true;
        } else {
            storedLongVal = value;
            storedVal = Double.longBitsToDouble(storedLongVal);
            storedEDelta = eDelta;
            storedFAlpha = fAlpha;
        }
    }

    public long readLong(int len) throws IOException {
        if (!buffer.isEmpty() && buffer.getLen() >= len) {
            // 如果缓冲区不为空且长度大于等于len，则从缓冲区中读取
            return buffer.readLong(len);
        } else {
            int remainingLen = len - buffer.getLen();
            return (buffer.readLong(buffer.getLen()) << remainingLen) | in.readLong(remainingLen);
        }
    }

    private int readInt(int len) throws IOException {
        return (int) readLong(len);
    }

    private int readBit() throws IOException {
        return readInt(1);
    }
}
