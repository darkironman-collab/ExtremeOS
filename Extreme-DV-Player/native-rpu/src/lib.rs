use dolby_vision::rpu::dovi_rpu::DoviRpu;
use jni::objects::{JByteArray, JClass};
use jni::sys::{jbyteArray, jint};
use jni::JNIEnv;
use std::ptr;

#[no_mangle]
pub extern "system" fn Java_com_extremedv_player_DoviNative_convertRpuNalu(
    mut env: JNIEnv,
    _class: JClass,
    input: JByteArray,
    mode: jint,
) -> jbyteArray {
    let result = (|| {
        let bytes = env.convert_byte_array(&input).ok()?;
        let mut rpu = DoviRpu::parse_unspec62_nalu(&bytes).ok()?;
        // Java/UI uses dovi_tool CLI mode numbers. The crate ConversionMode is compact:
        // 0 lossless, 1 MEL, 2 P8.1, 3 P8.4, 4 P8.1 mapping-preserved.
        let crate_mode: u8 = match mode {
            0 => 0,
            1 => 1,
            2 | 3 => 2,
            4 => 3,
            5 => 4,
            _ => return None,
        };
        rpu.convert_with_mode(crate_mode).ok()?;
        let out = rpu.write_hevc_unspec62_nalu().ok()?;
        env.byte_array_from_slice(&out).ok()
    })();

    result.map(|a| a.into_raw()).unwrap_or(ptr::null_mut())
}
