package com.google.sdk.encode;

import android.graphics.Bitmap;

public interface EncodeResultListener {

    void encodeSuccess(Bitmap bitmap);

    void encodeFail();
}
