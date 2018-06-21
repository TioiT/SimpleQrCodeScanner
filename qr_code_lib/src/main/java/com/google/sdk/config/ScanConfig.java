package com.google.sdk.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * 设置扫描配置
 * 若不进行设置，默认情况下只支持 QRCode
 * 识别成功后响一声，但是不会震动
 */
public class ScanConfig {
    private SharedPreferences.Editor edit;
    public ScanConfig(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        edit = preferences.edit();
    }

    /**
     * 设置所支持的解码类型
     * @param decodeFormat  解码类型
     * @return
     */
    public ScanConfig setDecodeFormat(String ...decodeFormat){

        for (String format : decodeFormat) {
            edit.putBoolean(format,true).apply();
        }
        return this;
    }

    /**
     * 设置识别成功后是否响一声
     * @param playOrNot     true 播放声音， false 不播放声音
     * @return
     */
    public ScanConfig setPlayBeep(boolean playOrNot){
        edit.putBoolean(CommonSet.KEY_PLAY_BEEP,playOrNot).apply();
        return this;
    }

    /**
     * 设置识别成功后是否震动
     * @param vibrateOrNot  true 震动， false 不震动
     * @return
     */
    public ScanConfig setVibrate(boolean vibrateOrNot){
        edit.putBoolean(CommonSet.KEY_VIBRATE,vibrateOrNot).apply();
        return this;
    }

    /**
     * 设置闪关灯模式，
     * 常亮   ：FlashLightMode.LIGHT_MODE_ON
     * 自动   ：FlashLightMode.LIGHT_MODE_AUTO
     * 常关   ：FlashLightMode.LIGHT_MODE_OFF
     * @param mode      闪光灯模式
     * @return
     */
    public ScanConfig setFlashLightMode(String mode){
        edit.putString(FlashLightMode.KEY_FRONT_LIGHT_MODE,mode);
        return this;
    }
}
