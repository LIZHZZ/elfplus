package org.urbcomp.startdb.compress.elf.compressor;

import org.urbcomp.startdb.compress.elf.utils.Erase;

public class DeltaOnElfCompressor implements ICompressor {

    private Erase storeVal;

    private boolean first;

    public DeltaOnElfCompressor() {
        first = true;
    }

    @Override
    public void addValue(double v) {
//        if (first) {
//            writeFirst(v);
//        } else {
//            return compressValue(v);
//        }
    }

    private void writeFirst(double v){

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
