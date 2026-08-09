package com.brouken.player;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

public final class DvCapabilityScanner {
    private DvCapabilityScanner() {}

    public static String scan() {
        if (Build.VERSION.SDK_INT < 24) {
            return "Dolby Vision MediaCodec API requires Android 7.0+";
        }

        List<String> decoders = new ArrayList<>();
        MediaCodecInfo[] infos = new MediaCodecList(MediaCodecList.ALL_CODECS).getCodecInfos();
        for (MediaCodecInfo info : infos) {
            if (info.isEncoder()) continue;
            for (String type : info.getSupportedTypes()) {
                if (!MediaFormat.MIMETYPE_VIDEO_DOLBY_VISION.equalsIgnoreCase(type)) continue;

                StringBuilder line = new StringBuilder(info.getName());
                try {
                    MediaCodecInfo.CodecCapabilities caps = info.getCapabilitiesForType(type);
                    line.append("\nprofiles: ");
                    for (MediaCodecInfo.CodecProfileLevel level : caps.profileLevels) {
                        line.append(profileName(level.profile)).append('/').append(level.level).append(' ');
                    }
                } catch (Throwable ignored) {
                }
                decoders.add(line.toString().trim());
            }
        }

        return decoders.isEmpty()
                ? "No hardware Dolby Vision decoder advertised"
                : join(decoders);
    }

    private static String join(List<String> items) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) out.append("\n\n");
            out.append(items.get(i));
        }
        return out.toString();
    }

    private static String profileName(int profile) {
        if (profile == MediaCodecInfo.CodecProfileLevel.DolbyVisionProfileDvavPer) return "DvavPer";
        if (profile == MediaCodecInfo.CodecProfileLevel.DolbyVisionProfileDvavPen) return "DvavPen";
        if (profile == MediaCodecInfo.CodecProfileLevel.DolbyVisionProfileDvheDer) return "DvheDer";
        if (profile == MediaCodecInfo.CodecProfileLevel.DolbyVisionProfileDvheDen) return "DvheDen";
        if (profile == MediaCodecInfo.CodecProfileLevel.DolbyVisionProfileDvheDtr) return "DvheDtr";
        if (profile == MediaCodecInfo.CodecProfileLevel.DolbyVisionProfileDvheStn) return "DvheStn";
        if (profile == MediaCodecInfo.CodecProfileLevel.DolbyVisionProfileDvheDth) return "DvheDth";
        if (profile == MediaCodecInfo.CodecProfileLevel.DolbyVisionProfileDvheDtb) return "DvheDtb";
        if (Build.VERSION.SDK_INT >= 27 && profile == MediaCodecInfo.CodecProfileLevel.DolbyVisionProfileDvheSt) return "DvheSt";
        if (Build.VERSION.SDK_INT >= 27 && profile == MediaCodecInfo.CodecProfileLevel.DolbyVisionProfileDvavSe) return "DvavSe";
        if (Build.VERSION.SDK_INT >= 30 && profile == MediaCodecInfo.CodecProfileLevel.DolbyVisionProfileDvav110) return "Dvav110";
        return Integer.toString(profile);
    }
}
