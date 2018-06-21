package com.google.sdk.decode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.google.zxing.Result;

/**
 * 根据路径，从图片中解析二维码数据
 */
public class DecodeAsyncTask extends AsyncTask<String,Void,DecodeResult> {
    private DecodeResultListener mDecodeResultListener;
    public DecodeAsyncTask(DecodeResultListener decodeResultListener){
        this.mDecodeResultListener = decodeResultListener;
    }
    @Override
    protected DecodeResult doInBackground(String... strings) {
        BitmapFactory.Options bitmapOption = DecodeUtils.getBitmapOption(strings[0]);

        Bitmap bitmap = BitmapFactory.decodeFile(strings[0],bitmapOption);
        int outWidth = bitmapOption.outWidth;
        int outHeight = bitmapOption.outHeight;
        byte[] yuv420sp = DecodeUtils.getYUV420sp(outWidth, outHeight, bitmap);
        Result decode = DecodeUtils.decode(yuv420sp, outWidth, outHeight);
        if(decode != null) {
            releaseBitmap(bitmap);
            return new DecodeResult(decode);
        }else{
            return null;
        }

    }

    private void releaseBitmap(Bitmap bitmap) {
        if(bitmap != null&& !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    @Override
    protected void onPostExecute(DecodeResult decodeResult) {
        super.onPostExecute(decodeResult);
        if(decodeResult != null) {
            mDecodeResultListener.decodeSuccess(decodeResult);
        }else{
            mDecodeResultListener.decodeFail();
        }
    }
}
