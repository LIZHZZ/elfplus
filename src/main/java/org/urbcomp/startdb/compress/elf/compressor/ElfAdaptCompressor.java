package org.urbcomp.startdb.compress.elf.compressor;

import gr.aueb.delorean.chimp.OutputBitStream;
import org.urbcomp.startdb.compress.elf.xorcompressor.ElfAdaptXORCompressor;
import org.urbcomp.startdb.compress.elf.xorcompressor.ElfXORCompressor;

public class ElfAdaptCompressor extends AbstractElfCompressor {
    private final ElfAdaptXORCompressor xorCompressor;

    public ElfAdaptCompressor() {
        xorCompressor = new ElfAdaptXORCompressor();
    }

    @Override protected int writeInt(int n, int len) {
        OutputBitStream os = xorCompressor.getOutputStream();
        os.writeInt(n, len);
        return len;
    }

    @Override protected int writeBit(boolean bit) {
        OutputBitStream os = xorCompressor.getOutputStream();
        os.writeBit(bit);
        return 1;
    }

    @Override protected int xorCompress(long vPrimeLong) {
        return xorCompressor.addValue(vPrimeLong);
    }

    @Override public byte[] getBytes() {
        return xorCompressor.getOut();
    }

    @Override public void close() {
        // we write one more bit here, for marking an end of the stream.
        writeInt(2,2);  // case 10
        xorCompressor.close();
    }
}
