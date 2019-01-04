package com.dengjunwu.server;

import java.util.Random;

public class TraceId {
    private long id;

    public TraceId(long id) {
        this.id = id;
    }

    private char[] byteToChars(Byte b) {
        return idc[b + 128];
    }

    private static char[][] idc = lut();

    private static char[][] lut() {
        char[][] rv = new char[256][2];
        int idx = 0;
        for (byte b = Byte.MIN_VALUE; b >= Byte.MIN_VALUE && b <= Byte.MAX_VALUE && idx < 256; b++, idx++) {
            byte bb;
            if (b < 0) {
                bb = (byte) (b + 256);
            } else
                bb = b;
            String s = String.format("%02x", bb);
            rv[idx] = new char[] { s.charAt(0), s.charAt(1) };
        }
        return rv;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(16);
        b.append(byteToChars((byte) (id >> 56 & 0xff)));
        b.append(byteToChars((byte) (id >> 48 & 0xff)));
        b.append(byteToChars((byte) (id >> 40 & 0xff)));
        b.append(byteToChars((byte) (id >> 32 & 0xff)));
        b.append(byteToChars((byte) (id >> 24 & 0xff)));
        b.append(byteToChars((byte) (id >> 16 & 0xff)));
        b.append(byteToChars((byte) (id >> 8 & 0xff)));
        b.append(byteToChars((byte) (id & 0xff)));
        return b.toString();
    }

    private static Random rng = new Random();

    public static String id() {
        return new TraceId(rng.nextLong()).toString();
    }
}
