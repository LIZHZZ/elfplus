package org.urbcomp.startdb.compress.elf.utils;

public class Erase {
    private final static int[] f =
            new int[]{0, 4, 7, 10, 14, 17, 20, 24, 27, 30, 34, 37, 40, 44, 47, 50, 54, 57,
                    60, 64, 67};

    private final static double[] map10iP =
            new double[]{1.0, 1.0E1, 1.0E2, 1.0E3, 1.0E4, 1.0E5, 1.0E6, 1.0E7,
                    1.0E8, 1.0E9, 1.0E10, 1.0E11, 1.0E12, 1.0E13, 1.0E14,
                    1.0E15, 1.0E16, 1.0E17, 1.0E18, 1.0E19, 1.0E20};
    private final static double LOG_2_10 = Math.log(10) / Math.log(2);

    private int e;

    private int alpha;

    private int betaStar;

    private long mask;

    private int fAlpha;

    private int gAlpha;

    private double erasedValue;

    private int eraseBits;

    private int state;


    public int getE() {
        return e;
    }

    public int getAlpha() {
        return alpha;
    }

    public int getBetaStar() {
        return betaStar;
    }

    public long getMask() {
        return mask;
    }

    public int getfAlpha() {
        return fAlpha;
    }

    public int getgAlpha() {
        return gAlpha;
    }

    public double getErasedValue() {
        return erasedValue;
    }
    public long getLongErasedValue(){
        return Double.doubleToRawLongBits(erasedValue);
    }

    public long getDeltaOfMPN() {
        return deltaOfMPN;
    }

    private long deltaOfMPN;

    public int getEraseBits() {
        return eraseBits;
    }

    public Erase(double v){
        long vLong = Double.doubleToRawLongBits(v);
        long vPrimeLong;

        if (v == 0.0 || Double.isInfinite(v)) {
//            size += writeBit(false);
            vPrimeLong = vLong;
        } else if (Double.isNaN(v)) {
//            size += writeBit(false);
            vPrimeLong = 0xfff8000000000000L & vLong;
        } else {
            int[] alphaAndBetaStar = getAlphaAndBetaStar(v);
            e = ((int) (vLong >> 52)) & 0x7ff;
            fAlpha = getFAlpha(alphaAndBetaStar[0]);
            gAlpha =  fAlpha + e - 1023;
            eraseBits = 52 - gAlpha;
            mask = 0xffffffffffffffffL << eraseBits;
            deltaOfMPN = (~mask) & vLong;
            if (alphaAndBetaStar[1] < 16 && deltaOfMPN != 0 && eraseBits > 4) {
//                size += writeInt(alphaAndBetaStar[1] | 0x10, 5);
                vPrimeLong = mask & vLong;
            } else {
//                size += writeBit(false);
                vPrimeLong = vLong;
            }
        }
//        size += xorCompress(vPrimeLong);
        erasedValue = Double.longBitsToDouble(vPrimeLong);
    }

    public static double erase(double v) {
        long vLong = Double.doubleToRawLongBits(v);
        long vPrimeLong;

        if (v == 0.0 || Double.isInfinite(v)) {
//            size += writeBit(false);
            vPrimeLong = vLong;
        } else if (Double.isNaN(v)) {
//            size += writeBit(false);
            vPrimeLong = 0xfff8000000000000L & vLong;
        } else {
            int[] alphaAndBetaStar = getAlphaAndBetaStar(v);
            System.out.println("alpha" + alphaAndBetaStar[0]);
            int e = ((int) (vLong >> 52)) & 0x7ff;
            int gAlpha = getFAlpha(alphaAndBetaStar[0]) + e - 1023;
            System.out.println("gAlpha" + gAlpha);
            int eraseBits = 52 - gAlpha;
            System.out.println("eraseBits " + eraseBits);
            long mask = 0xffffffffffffffffL << eraseBits;
            long delta = (~mask) & vLong;
            if (alphaAndBetaStar[1] < 16 && delta != 0 && eraseBits > 4) {
//                size += writeInt(alphaAndBetaStar[1] | 0x10, 5);
                vPrimeLong = mask & vLong;
            } else {
//                size += writeBit(false);
                vPrimeLong = vLong;
            }
        }
//        size += xorCompress(vPrimeLong);
        System.out.println(Long.toBinaryString(vPrimeLong));
        return Double.longBitsToDouble(vPrimeLong);
    }

    private static int getFAlpha(int alpha) {
        if (alpha <= 0) {
            throw new IllegalArgumentException("The argument should be greater than 0");
        }
        if (alpha >= f.length) {
            return (int) Math.ceil(alpha * LOG_2_10);
        } else {
            return f[alpha];
        }
    }

    private static int[] getAlphaAndBetaStar(double v) {
        if (v < 0) {
            v = -v;
        }
        int[] alphaAndBetaStar = new int[2];
        double log10v = Math.log10(v);
        int sp = (int) Math.floor(log10v);
        System.out.println("sp: " + sp);
        int beta = getSignificantCount(v, sp);
        alphaAndBetaStar[0] = beta - sp - 1;
        alphaAndBetaStar[1] = (v < 1 && sp == log10v) ? 0 : beta;
        return alphaAndBetaStar;
    }

    private static int getSignificantCount(double v, int sp) {
        int i;
        if (sp >= 0) {
            i = 1;
        } else {
            i = -sp;
        }
        double temp = v * get10iP(i);
        while ((long) temp != temp) {
            i++;
            temp = v * get10iP(i);
        }
        // There are some bugs for those with high significand, i.e., 0.23911204406033099
        // So we should further check
        if (temp / get10iP(i) != v) {
            return 17;
        } else {
            return sp + i + 1;
        }
    }

    private static double get10iP(int i) {
        if (i <= 0) {
            throw new IllegalArgumentException("The argument should be greater than 0");
        }
        if (i >= map10iP.length) {
            return Double.parseDouble("1.0E" + i);
        } else {
            return map10iP[i];
        }
    }

    public static void main(String[] args) {
        System.out.println(erase(31.988432));
        System.out.println(erase(32.001277));
        System.out.println("----------------------------------------------------------------------------------");
        System.out.println(erase(29.66239));
        System.out.println(erase(32.00123));
        System.out.println("----------------------------------------------------------------------------------");
        System.out.println(erase(29.6623));
        System.out.println(erase(29.6639));
        System.out.println("----------------------------------------------------------------------------------");
        System.out.println(erase(29.662));
        System.out.println(erase(29.663));
        System.out.println("----------------------------------------------------------------------------------");
        System.out.println(erase(29.66));
        System.out.println(erase(29.66));
        System.out.println(31.988432 - 32.001277);

        System.out.println(32.001277*10);

        System.out.println(Long.toBinaryString(Double.doubleToRawLongBits(erase(31.988432) - erase(32.001277))));
    }
}
