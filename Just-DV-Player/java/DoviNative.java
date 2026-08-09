package com.brouken.player;

public final class DoviNative {
    private static final boolean AVAILABLE;

    static {
        boolean loaded;
        try {
            System.loadLibrary("extreme_dovi");
            loaded = true;
        } catch (Throwable ignored) {
            loaded = false;
        }
        AVAILABLE = loaded;
    }

    private DoviNative() {}

    public static boolean isAvailable() {
        return AVAILABLE;
    }

    public static native byte[] convertRpuNalu(byte[] nalu, int mode);
}
