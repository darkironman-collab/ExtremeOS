package com.brouken.player;

import androidx.media3.common.C;
import androidx.media3.common.ColorInfo;
import androidx.media3.common.Format;
import androidx.media3.common.MimeTypes;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DvProfileDetector {
    private static final Pattern DV_CODEC = Pattern.compile("(?:dvhe|dvh1|dvav|dva1)\\.(\\d{2})\\.(\\d{2})", Pattern.CASE_INSENSITIVE);

    private DvProfileDetector() {}

    public static String describe(Format format) {
        if (format == null) return "No video format detected yet";
        String codec = format.codecs == null ? "" : format.codecs;
        Matcher matcher = DV_CODEC.matcher(codec);
        if (matcher.find()) {
            int profile = Integer.parseInt(matcher.group(1));
            int level = Integer.parseInt(matcher.group(2));
            return String.format(Locale.US, "Dolby Vision P%d • level %02d • %s • %dx%d",
                    profile, level, codec, format.width, format.height);
        }
        if (MimeTypes.VIDEO_DOLBY_VISION.equals(format.sampleMimeType)) {
            return "Dolby Vision • profile not exposed by container • " + safe(codec);
        }
        ColorInfo colorInfo = format.colorInfo;
        if (colorInfo != null) {
            if (colorInfo.colorTransfer == C.COLOR_TRANSFER_ST2084) {
                return "HDR10/PQ • " + safe(codec) + " • " + format.width + "x" + format.height;
            }
            if (colorInfo.colorTransfer == C.COLOR_TRANSFER_HLG) {
                return "HLG • " + safe(codec) + " • " + format.width + "x" + format.height;
            }
        }
        return "SDR/Unknown HDR • " + safe(codec) + " • " + format.width + "x" + format.height;
    }

    public static int profileNumber(Format format) {
        if (format == null || format.codecs == null) return -1;
        Matcher matcher = DV_CODEC.matcher(format.codecs);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    private static String safe(String value) {
        return value == null || value.isEmpty() ? "codec unknown" : value;
    }
}
