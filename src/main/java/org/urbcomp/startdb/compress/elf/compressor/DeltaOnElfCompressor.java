package org.urbcomp.startdb.compress.elf.compressor;

import gr.aueb.delorean.chimp.OutputBitStream;
import org.urbcomp.startdb.compress.elf.utils.Elf64Utils;
import org.urbcomp.startdb.compress.elf.utils.Erase;
import org.urbcomp.startdb.compress.elf.xorcompressor.ElfDeltaCompressor;

import java.time.chrono.Era;

public class DeltaOnElfCompressor implements ICompressor {

    private boolean first;

    private int size;

    private final ElfDeltaCompressor deltaCompressor;

    public DeltaOnElfCompressor() {
        first = true;
        deltaCompressor = new ElfDeltaCompressor();
    }


//    @Override
//    public void addValue(double v) {
//        long vLong = Double.doubleToRawLongBits(v);
//        long vPrimeLong;
//        Erase erasedV = new Erase(v);
//
//        if (v == 0.0 || Double.isInfinite(v)) {
//            size += writeInt(2, 2); // case 10
//            vPrimeLong = vLong;
//        } else if (Double.isNaN(v)) {
//            size += writeInt(2, 2); // case 10
//            vPrimeLong = 0xfff8000000000000L & vLong;
//        } else {
//            if (erasedV.getDeltaOfMPN() != 0 && erasedV.getEraseBits() > 4) {
//                if (erasedV.getBetaStar() == storeVal.getBetaStar()) {
//                    size += writeBit(false);// case 0
//                } else {
//                    size += writeInt(erasedV.getBetaStar() | 0x30, 6); // case 11, 2 + 4 = 6
//                    storeVal = erasedV;
//                }
//            } else {
//                size += writeInt(2, 2);// case 10
//            }
//        }
//        size += deltaCompress(erasedV);
//    }

    private int lastBetaStar = Integer.MAX_VALUE;


    public void addValue(double v) {
        long vLong = Double.doubleToRawLongBits(v);
        long vPrimeLong;
        int fAlpha = 0;


        if (v == 0.0 || Double.isInfinite(v)) {
            size += writeInt(2, 2); // case 10
            vPrimeLong = vLong;
        } else if (Double.isNaN(v)) {
            size += writeInt(2, 2); // case 10
            vPrimeLong = 0xfff8000000000000L & vLong;
        } else {
            // C1: v is a normal or subnormal
            int[] alphaAndBetaStar = Elf64Utils.getAlphaAndBetaStar(v, lastBetaStar);
            int e = ((int) (vLong >> 52)) & 0x7ff;
            fAlpha = Elf64Utils.getFAlpha(alphaAndBetaStar[0]);
            int gAlpha = fAlpha + e - 1023;

            int eraseBits = 52 - gAlpha;
            long mask = 0xffffffffffffffffL << eraseBits;
            long delta = (~mask) & vLong;
            if (delta != 0 && eraseBits > 4) {  // C2
                if (alphaAndBetaStar[1] == lastBetaStar) {
                    size += writeBit(false);    // case 0
                } else {
                    size += writeInt(alphaAndBetaStar[1] | 0x30, 6);  // case 11, 2 + 4 = 6
                    lastBetaStar = alphaAndBetaStar[1];
                }
                vPrimeLong = mask & vLong;
            } else {
                size += writeInt(2, 2); // case 10
                vPrimeLong = vLong;
            }
        }
        size += deltaCompress(vPrimeLong, fAlpha);
    }


    private void writeFirst(double v) {

    }

    public int writeInt(int n, int len) {
        OutputBitStream os = deltaCompressor.getOutputStream();
        os.writeInt(n, len);
        return len;
    }

    public int writeBit(boolean bit) {
        OutputBitStream os = deltaCompressor.getOutputStream();
        os.writeBit(bit);
        return 1;
    }

    public int deltaCompress(long v, int fAlpha) {
        return deltaCompressor.addValue(v, fAlpha);
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public byte[] getBytes() {
        return new byte[0];
    }

    @Override
    public void close() {

    }
}
