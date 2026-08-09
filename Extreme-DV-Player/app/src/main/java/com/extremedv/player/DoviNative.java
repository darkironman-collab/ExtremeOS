package com.extremedv.player;

public final class DoviNative {
    private static final boolean AVAILABLE;
    static {
        boolean ok;
        try {
            System.loadLibrary("extreme_dovi");
            ok = true;
        } catch (Throwable t) {
            ok = false;
        }
        AVAILABLE = ok;
    }

    private DoviNative() {}

    public static boolean isAvailable() { return AVAILABLE; }

    /** Converts one complete HEVC UNSPEC62 Dolby Vision RPU NAL unit using libdovi conversion modes. */
    public static native byte[] convertRpuNalu(byte[] nalu, int mode);
}
