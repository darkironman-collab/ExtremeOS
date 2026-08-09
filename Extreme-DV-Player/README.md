# Extreme DV Player 0.3

Standalone Android Dolby Vision test/player project. It does not modify the Windows VLC-DolbyVision project.

## Implemented
- Local file picker
- Direct HTTP/HTTPS/content/file URL playback
- HLS and DASH via AndroidX Media3/ExoPlayer
- Dolby Vision codec/profile detection (`dvhe`, `dvh1`, `dvav`, `dva1`)
- Device `video/dolby-vision` MediaCodec capability scan
- Target modes: Auto/P5/P7 MEL/P7 FEL/P8.1/P8.4/HDR10/HLG/SDR
- Auto defaults to P8.4 compatibility
- Rust JNI RPU converter built from `quietvoid/dovi_tool` / `dolby_vision` crate

## Conversion limits
The native layer converts existing Dolby Vision RPU metadata. Genuine SDR/HDR10/HLG → Dolby Vision requires valid DV metadata generation and, depending on target, pixel/color-space transcoding. Genuine P7 FEL requires real FEL enhancement data and cannot be created by relabeling.

## Current playback pipeline
Media3 handles playback and profile detection. `DoviNative.convertRpuNalu()` provides the native conversion primitive. The remaining pipeline step for true on-the-fly playback conversion is interception/reinjection of transformed RPU NAL units before MediaCodec input; the UI reports the selected conversion plan rather than falsely claiming unsupported streams were converted.
