package org.urbcomp.startdb.compress.elf;

import org.urbcomp.startdb.compress.delta.compressor.DeltaCompressor;
import org.urbcomp.startdb.compress.elf.compressor.*;
import org.urbcomp.startdb.compress.elf.decompressor.*;
import org.urbcomp.startdb.compress.elf.utils.Erase;
import sun.misc.DoubleConsts;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println(Long.toBinaryString(DoubleConsts.SIGNIF_BIT_MASK));
        System.out.println(Long.toBinaryString(DoubleConsts.EXP_BIT_MASK));
        DeltaCompressor deltaCompressor = new DeltaCompressor();
        double a = 29.686912;
        double b = 29.686855;
        deltaCompressor.addValue(a);
        deltaCompressor.addValue(b);
        System.out.println(Long.toBinaryString(Double.doubleToRawLongBits(a - b)));
        long xor = Double.doubleToRawLongBits(a) ^ Double.doubleToRawLongBits(b);
        System.out.println(Long.numberOfTrailingZeros(xor));
        System.out.println(Long.toBinaryString(Double.doubleToRawLongBits(a)));
        System.out.println(Long.toBinaryString(Double.doubleToRawLongBits(b)));
        System.out.println(Long.toBinaryString(xor));
        Erase storedVal = new Erase(32.6639);
        Erase value = new Erase(2.321351);
        double delta = storedVal.getErasedValue() - value.getErasedValue();
        System.out.println("--------------------------------------------------------------");
        long deltaLong = Double.doubleToRawLongBits(delta);
        int fAlpha = Math.max(storedVal.getfAlpha(), value.getfAlpha());
        int eDelta = (((int) (deltaLong >> 52)) & 0x7ff) - 1023;
        System.out.println(storedVal.getE() - 1023);
        int trailZeros = fAlpha - eDelta;
        System.out.println("e sValue: " + (storedVal.getE() - 1023));
        System.out.println("e value : " + (value.getE() - 1023));
        System.out.println("Falphas : " + storedVal.getfAlpha());
        System.out.println("Falphav : " + value.getfAlpha());
        System.out.println("svalu eb: " + storedVal.getEraseBits());
        System.out.println("value eb: " + value.getEraseBits());
        System.out.println(delta);
        System.out.println(Long.toBinaryString(deltaLong));
        System.out.println(eDelta);
        System.out.println(fAlpha);
        System.out.println(trailZeros);
        System.out.println(Long.toBinaryString(deltaLong >>> (52-trailZeros)));
        System.out.println(storedVal.getE() - 1023);
        Erase d = new Erase(0.001545);
        System.out.println(Long.toBinaryString(d.getLongErasedValue()));
//        System.out.println(Long.numberOfTrailingZeros()erase.getLongErasedValue());


    }
}
