package org.urbcomp.startdb.compress.elf.utils;

public class DecompressBuffer {

    private int point;
    private long buffer;

    public DecompressBuffer() {
        point = 0;
    }

    public void writeLong(long v, int len) {
        long mask = ~(~0L << len);

        buffer = (buffer << len) | (v & mask);
        point += len;
    }

    public long readLong(int len) {
        long result = buffer >> (point - len);
        long mask = (~0L << point) ^ (~0L << (point - len));
        buffer = buffer & ~mask;
        point -= len;
        return result;
    }


    public long getBuffer() {
        return buffer;
    }

    public int getLen() {
        return point;
    }

    public boolean isEmpty() {
        return point == 0;
    }

    public static void main(String[] args) {
        DecompressBuffer buffer = new DecompressBuffer();
        buffer.writeLong(2451111931195307L, 50);
        System.out.println(Long.toBinaryString(buffer.getBuffer()));
        long a = buffer.readLong(20);
        System.out.println(Long.toBinaryString(a));
        System.out.println(Long.toBinaryString(buffer.getBuffer()));
        buffer.writeLong(165L, 5);
        System.out.println(Long.toBinaryString(buffer.getBuffer()));
        buffer.readLong(buffer.getLen());
        System.out.println(buffer.isEmpty());
        System.out.println(Long.toBinaryString(165L));
    }

}
