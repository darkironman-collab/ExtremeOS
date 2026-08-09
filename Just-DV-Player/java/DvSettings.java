package com.brouken.player;

import android.content.Context;
import android.content.SharedPreferences;

public final class DvSettings {
    private static final String PREFS = "extreme_dv";
    private static final String KEY_TARGET = "target_profile";

    private DvSettings() {}

    public static DvTargetProfile getTarget(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return DvTargetProfile.fromOrdinal(prefs.getInt(KEY_TARGET, DvTargetProfile.AUTO.ordinal()));
    }

    public static void setTarget(Context context, DvTargetProfile target) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_TARGET, target.ordinal())
                .apply();
    }

    public static boolean mapDv7ToHevc(Context context, boolean justPlayerSetting) {
        DvTargetProfile target = getTarget(context);
        if (target == DvTargetProfile.HDR10) return true;
        if (target == DvTargetProfile.ORIGINAL) return justPlayerSetting;
        return false;
    }
}
