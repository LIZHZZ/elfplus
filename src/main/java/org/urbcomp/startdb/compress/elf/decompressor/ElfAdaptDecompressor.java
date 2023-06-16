package org.urbcomp.startdb.compress.elf.decompressor;

import gr.aueb.delorean.chimp.InputBitStream;
import org.urbcomp.startdb.compress.elf.xordecompressor.ElfAdaptXORDecompressor;
import org.urbcomp.startdb.compress.elf.xordecompressor.ElfXORDecompressor;

import java.io.IOException;

public class ElfAdaptDecompressor extends AbstractElfDecompressor {
    private final ElfAdaptXORDecompressor xorDecompressor;

    public ElfAdaptDecompressor(byte[] bytes) {
        xorDecompressor = new ElfAdaptXORDecompressor(bytes);
    }

    @Override protected Double xorDecompress() {
        return xorDecompressor.readValue();
    }

    @Override protected int readInt(int len) {
        InputBitStream in = xorDecompressor.getInputStream();
        try {
            return in.readInt(len);
        } catch (IOException e) {
            throw new RuntimeException("IO error: " + e.getMessage());
        }
    }
}
