package com.google.sdk.decode;

public interface DecodeResultListener {

    void decodeSuccess(DecodeResult decodeResult);

    void decodeFail();
}
