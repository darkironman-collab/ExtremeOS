package com.brouken.player;

public enum DvTargetProfile {
    AUTO("Auto → Dolby Vision P8.4", 4),
    ORIGINAL("Original / Passthrough", -1),
    P5("Dolby Vision Profile 5", -2),
    P7_MEL("Dolby Vision Profile 7 MEL", 1),
    P7_FEL("Dolby Vision Profile 7 FEL", -3),
    P81("Dolby Vision Profile 8.1", 2),
    P84("Dolby Vision Profile 8.4", 4),
    HDR10("HDR10 fallback", -4),
    HLG("HLG fallback", -5),
    SDR("SDR fallback", -6);

    public final String label;
    public final int doviToolMode;

    DvTargetProfile(String label, int doviToolMode) {
        this.label = label;
        this.doviToolMode = doviToolMode;
    }

    public static DvTargetProfile fromOrdinal(int ordinal) {
        DvTargetProfile[] all = values();
        if (ordinal < 0 || ordinal >= all.length) return AUTO;
        return all[ordinal];
    }

    @Override
    public String toString() {
        return label;
    }
}
