package org.urbcomp.startdb.compress.elf;

import org.urbcomp.startdb.compress.delta.compressor.DeltaCompressor;
import org.urbcomp.startdb.compress.elf.compressor.*;
import org.urbcomp.startdb.compress.elf.decompressor.*;
import org.urbcomp.startdb.compress.elf.utils.Erase;
import org.urbcomp.startdb.compress.elf.xorcompressor.ElfDeltaCompressor;
import org.urbcomp.startdb.compress.elf.xordecompressor.ElfDeltaDecompressor;
import sun.misc.DoubleConsts;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
//        System.out.println(Long.toBinaryString(DoubleConsts.SIGNIF_BIT_MASK));
//        System.out.println(Long.toBinaryString(DoubleConsts.EXP_BIT_MASK));
        DeltaCompressor deltaCompressor = new DeltaCompressor();
        double[] list = new double[]{0.08,0.09,0.05,0.02,};
        double a = 0.08;
        double b = 0.09;
        double c = 0.05;
        double d = 0.02;
        double e = 0.14;
        DeltaOnElfCompressor deltaOnElfCompressor = new DeltaOnElfCompressor();
        for(int i=0;i<list.length;i++){
            deltaOnElfCompressor.addValue(list[i]);
        }
        deltaOnElfCompressor.close();
        byte[] result = deltaOnElfCompressor.getBytes();
        DeltaOnElfDecompressor deltaOnElfDecompressor = new DeltaOnElfDecompressor(result);
        deltaOnElfDecompressor.decompress();

//
//        System.out.println(Long.toBinaryString(Double.doubleToRawLongBits(a - b)));
//        long xor = Double.doubleToRawLongBits(a) ^ Double.doubleToRawLongBits(b);
//        System.out.println(Long.numberOfTrailingZeros(xor));
//        System.out.println(Long.toBinaryString(Double.doubleToRawLongBits(a)));
//        System.out.println(Long.toBinaryString(Double.doubleToRawLongBits(b)));
//        System.out.println(Long.toBinaryString(xor));
//        Erase storedVal = new Erase(32.6639);
//        Erase value = new Erase(29.575368);
//        Erase value2 = new Erase(29.659312);
//        double delta = storedVal.getErasedValue() - value.getErasedValue();
//        System.out.println("--------------------------------------------------------------");
//        long deltaLong = Double.doubleToRawLongBits(delta);
//        int fAlpha = Math.max(storedVal.getfAlpha(), value.getfAlpha());
//        int eDelta = (((int) (deltaLong >> 52)) & 0x7ff) - 1023;
//        System.out.println(storedVal.getE() - 1023);
//        int trailZeros = fAlpha - eDelta;
//        System.out.println("e sValue: " + (storedVal.getE() - 1023));
//        System.out.println("e value : " + (value.getE() - 1023));
//        System.out.println("Falphas : " + storedVal.getfAlpha());
//        System.out.println("Falphav : " + value.getfAlpha());
//        System.out.println("svalu eb: " + storedVal.getEraseBits());
//        System.out.println("value eb: " + value.getEraseBits());
//        System.out.println("deltaE  : " + Long.toBinaryString(deltaLong >>> 52));
//        System.out.println(delta);
//        System.out.println(Long.toBinaryString(deltaLong));
//        System.out.println(eDelta);
//        System.out.println(fAlpha);
//        System.out.println(trailZeros);
//        System.out.println(Long.toBinaryString(deltaLong >>> (52 - trailZeros)));
//        System.out.println(storedVal.getE() - 1023);
//        Erase d = new Erase(0.001545);
//        System.out.println(Long.toBinaryString(d.getLongErasedValue()));
////        System.out.println(Long.numberOfTrailingZeros()erase.getLongErasedValue());
//
//
//        ElfDeltaCompressor elfDeltaCompressor = new ElfDeltaCompressor();
//        elfDeltaCompressor.addValue(storedVal.getLongErasedValue(), 0);
//        elfDeltaCompressor.addValue(value.getLongErasedValue(), value.getfAlpha());
//        elfDeltaCompressor.addValue(value2.getLongErasedValue(), value2.getfAlpha());
//        elfDeltaCompressor.close();
//        ElfDeltaDecompressor elfDeltaDecompressor = new ElfDeltaDecompressor(elfDeltaCompressor.getOut());
//
//        System.out.println(elfDeltaDecompressor.getInputStream().readLong(2));
//        System.out.println(elfDeltaDecompressor.getInputStream().readLong(1));
//        long e = elfDeltaDecompressor.getInputStream().readLong(11);
//        System.out.println(e);
//        long m = elfDeltaDecompressor.getInputStream().readLong(52);
//        System.out.println(m);
//        System.out.println(e << 52 | m);
//        System.out.println(Double.longBitsToDouble(e << 52 | m));
    }
}
