package com.google.sdk.config;

public class CommonSet {


    /**
     * Contains type constants used when sending Intents.
     */
    public static final class Type {
        /**
         * Plain text. Use Intent.putExtra(DATA, string). This can be used for URLs too, but string
         * must include "http://" or "https://".
         */
        public static final String TEXT = "TEXT_TYPE";
    }

    public interface IntentData{
//        String SCAN_RESULT = "SCAN_RESULT";
//        int REQUEST_SCAN_RESULT =9001;
    }
//    public static final String KEY_DECODE_All_CODE = "preferences_decode_ALL_CODE";
//    public static final String KEY_DECODE_All_1D_CODE = "preferences_decode_ALL_1D_CODE";
//    public static final String KEY_DECODE_ALL_2D_CODE = "preferences_decode_ALL_2D_CODE";
//    public static final String KEY_DECODE_1D_PRODUCT = "preferences_decode_1D_product";
//    public static final String KEY_DECODE_1D_INDUSTRIAL = "preferences_decode_1D_industrial";
//    public static final String KEY_DECODE_QR = "preferences_decode_QR";
//    public static final String KEY_DECODE_DATA_MATRIX = "preferences_decode_Data_Matrix";
//    public static final String KEY_DECODE_AZTEC = "preferences_decode_Aztec";
//    public static final String KEY_DECODE_PDF417 = "preferences_decode_PDF417";


    public static final String KEY_PLAY_BEEP = "preferences_play_beep";
    public static final String KEY_VIBRATE = "preferences_vibrate";

//    public static final String KEY_FRONT_LIGHT_MODE = "preferences_front_light_mode";
//    public static final String LIGHT_MODE_ON = "ON";
//    public static final String LIGHT_MODE_AUTO = "AUTO";
//    public static final String LIGHT_MODE_OFF = "OFF";
    public static final String KEY_AUTO_FOCUS = "preferences_auto_focus";
}
