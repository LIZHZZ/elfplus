package org.urbcomp.startdb.compress.elf.decompressor;

import gr.aueb.delorean.chimp.InputBitStream;
import org.urbcomp.startdb.compress.elf.utils.Elf64Utils;
import org.urbcomp.startdb.compress.elf.xorcompressor.ElfDeltaCompressor;
import org.urbcomp.startdb.compress.elf.xordecompressor.ElfDeltaDecompressor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeltaOnElfDecompressor implements IDecompressor{
    private int lastBetaStar = Integer.MAX_VALUE;
    private ElfDeltaDecompressor deltaDecompressor;

    public DeltaOnElfDecompressor(byte[]bytes){
        deltaDecompressor = new ElfDeltaDecompressor(bytes);
    }


    public List<Double> decompress() {
        List<Double> values = new ArrayList<>(1024);
        Double value;
        int index=0;
        while ((value = nextValue()) != null) {
            values.add(value);
            System.out.println(value);
            index++;
            System.out.println("index :"+index);

        }
        return values;
    }

    private Double nextValue() {
        System.out.println("----------------------------------");
        Double v;
        if(readInt(1) == 0) {
            System.out.println("flag 0");
            v = recoverVByBetaStar();// case 0
            System.out.println("betastar:  "+ lastBetaStar);
        } else if (readInt(1) == 0) {
            System.out.println("flag 01");
            v = deltaDecompress(0);                    // case 10
        } else {
            System.out.println("11");
            lastBetaStar = readInt(4);          // case 11
            System.out.println("betastar:  "+ lastBetaStar);
            v = recoverVByBetaStar();
        }
        return v;
    }

    private Double recoverVByBetaStar() {
        double v;
        Double vPrime = deltaDecompress(lastBetaStar);
        System.out.println("vPrime: "+(vPrime));
        System.out.println(Long.toBinaryString(Double.doubleToRawLongBits(vPrime)));
        int sp = Elf64Utils.getSP(Math.abs(vPrime));
        if (lastBetaStar == 0) {
            v = Elf64Utils.get10iN(-sp - 1);
            if (vPrime < 0) {
                v = -v;
            }
        } else {
            int alpha = lastBetaStar - sp - 1;
            v = Elf64Utils.roundUp(vPrime, alpha);
        }
        return v;
    }



    public Double deltaDecompress(int betaStar){

        return deltaDecompressor.readValue(betaStar);
    };

    public int readInt(int len){
        try{
            return (int) deltaDecompressor.readLong(len);
        }catch (IOException e){
            throw new RuntimeException("IO error: " + e.getMessage());
        }
    };
}
