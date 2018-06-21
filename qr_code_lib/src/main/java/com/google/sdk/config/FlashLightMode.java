package com.google.sdk.config;

/**
 *  闪关灯模式，常开，常关，自动
 */
public interface FlashLightMode {
    String KEY_FRONT_LIGHT_MODE = "preferences_front_light_mode";
    String LIGHT_MODE_ON = "ON";
    String LIGHT_MODE_AUTO = "AUTO";
    String LIGHT_MODE_OFF = "OFF";
}
