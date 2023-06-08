package org.urbcomp.startdb.compress.elf.compressor;

import gr.aueb.delorean.chimp.OutputBitStream;
import org.urbcomp.startdb.compress.elf.utils.Erase;
import org.urbcomp.startdb.compress.elf.xorcompressor.ElfDeltaCompressor;

import java.time.chrono.Era;

public class DeltaOnElfCompressor implements ICompressor {

    private Erase storeVal;

    private boolean first;

    private int size;

    private final ElfDeltaCompressor deltaCompressor;

    public DeltaOnElfCompressor() {
        first = true;
        deltaCompressor = new ElfDeltaCompressor();
    }


    @Override
    public void addValue(double v) {
        long vLong = Double.doubleToRawLongBits(v);
        long vPrimeLong;
        Erase erasedV = new Erase(v);

        if (v == 0.0 || Double.isInfinite(v)) {
            size += writeInt(2, 2); // case 10
            vPrimeLong = vLong;
        } else if (Double.isNaN(v)) {
            size += writeInt(2, 2); // case 10
            vPrimeLong = 0xfff8000000000000L & vLong;
        } else {
            if (erasedV.getDeltaOfMPN() != 0 && erasedV.getEraseBits() > 4) {
                if (erasedV.getBetaStar() == storeVal.getBetaStar()) {
                    size += writeBit(false);
                } else {
                    size += writeInt(erasedV.getBetaStar() | 0x30, 4);
                    storeVal = erasedV;
                }
            } else {
                size += writeInt(2, 2);
            }
        }
        size += deltaCompress(erasedV);
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

    public  int deltaCompress(Erase v){
        return deltaCompressor.addValue(v);
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
