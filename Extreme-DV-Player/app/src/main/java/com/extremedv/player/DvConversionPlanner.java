package com.extremedv.player;

public final class DvConversionPlanner {
    private DvConversionPlanner() {}

    public static String plan(int sourceProfile, DvTargetProfile target) {
        if (target == DvTargetProfile.ORIGINAL) return "Passthrough: original stream";
        if (target == DvTargetProfile.AUTO) target = DvTargetProfile.P84;

        switch (target) {
            case P84:
                return sourceProfile > 0
                    ? "Live RPU path: libdovi mode 4 → P8.4; video essence stays HEVC when compatible"
                    : "P8.4 needs DV RPU metadata; SDR/HDR source requires metadata generation/transcode";
            case P81:
                return sourceProfile > 0
                    ? "Live RPU path: libdovi mode 2 → P8.1; P7 FEL enhancement video is not preserved in single-layer output"
                    : "P8.1 from non-DV needs new RPU metadata + compatible HDR10 base layer";
            case P7_MEL:
                return sourceProfile == 7 || sourceProfile == 8
                    ? "RPU MEL-compatible path: libdovi mode 1"
                    : "Creating genuine P7 MEL requires a compatible enhancement-layer workflow";
            case P7_FEL:
                return sourceProfile == 7
                    ? "FEL can only be retained when source actually contains FEL enhancement data"
                    : "Genuine P7 FEL cannot be synthesized from a source that has no FEL enhancement layer";
            case P5:
                return "P5 changes base-layer color representation; full transcoding is required for genuine conversion";
            case HDR10:
                return "HDR10 fallback: use compatible base layer / tone-map when source is not HDR10-compatible";
            case HLG:
                return "HLG fallback: HLG transfer conversion may require pixel transcoding";
            case SDR:
                return "SDR fallback: tone-map to SDR when needed";
            default:
                return "Profile planner";
        }
    }
}
