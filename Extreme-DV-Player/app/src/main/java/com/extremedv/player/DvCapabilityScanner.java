package com.extremedv.player;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;

import java.util.ArrayList;
import java.util.List;

public final class DvCapabilityScanner {
    private DvCapabilityScanner() {}

    public static String scan() {
        if (android.os.Build.VERSION.SDK_INT < 24) return "Dolby Vision MediaCodec API needs Android 7.0+";
        List<String> decoders = new ArrayList<>();
        MediaCodecInfo[] infos = new MediaCodecList(MediaCodecList.ALL_CODECS).getCodecInfos();
        for (MediaCodecInfo info : infos) {
            if (info.isEncoder()) continue;
            for (String type : info.getSupportedTypes()) {
                if (MediaFormat.MIMETYPE_VIDEO_DOLBY_VISION.equalsIgnoreCase(type)) {
                    StringBuilder sb = new StringBuilder(info.getName());
                    try {
                        MediaCodecInfo.CodecCapabilities caps = info.getCapabilitiesForType(type);
                        sb.append(" profiles=");
                        for (MediaCodecInfo.CodecProfileLevel pl : caps.profileLevels) {
                            sb.append(profileName(pl.profile)).append('/').append(pl.level).append(' ');
                        }
                    } catch (Throwable ignored) {}
                    decoders.add(sb.toString().trim());
                }
            }
        }
        if (decoders.isEmpty()) return "No hardware Dolby Vision decoder advertised";
        return String.join("\n", decoders);
    }

    private static String profileName(int p) {
        if (p == MediaCodecInfo.CodecProfileLevel.DolbyVisionProfileDvavPer) return "DvavPer";
        if (p == MediaCodecInfo.CodecProfileLevel.DolbyVisionProfileDvavPen) return "DvavPen";
        if (p == MediaCodecInfo.CodecProfileLevel.DolbyVisionProfileDvheDer) return "DvheDer";
        if (p == MediaCodecInfo.CodecProfileLevel.DolbyVisionProfileDvheDen) return "DvheDen";
        if (p == MediaCodecInfo.CodecProfileLevel.DolbyVisionProfileDvheDtr) return "DvheDtr";
        if (p == MediaCodecInfo.CodecProfileLevel.DolbyVisionProfileDvheStn) return "DvheStn";
        if (p == MediaCodecInfo.CodecProfileLevel.DolbyVisionProfileDvheDth) return "DvheDth";
        if (p == MediaCodecInfo.CodecProfileLevel.DolbyVisionProfileDvheDtb) return "DvheDtb";
        if (android.os.Build.VERSION.SDK_INT >= 27 && p == MediaCodecInfo.CodecProfileLevel.DolbyVisionProfileDvheSt) return "DvheSt";
        if (android.os.Build.VERSION.SDK_INT >= 27 && p == MediaCodecInfo.CodecProfileLevel.DolbyVisionProfileDvavSe) return "DvavSe";
        if (android.os.Build.VERSION.SDK_INT >= 30 && p == MediaCodecInfo.CodecProfileLevel.DolbyVisionProfileDvav110) return "Dvav110";
        return String.valueOf(p);
    }
}
