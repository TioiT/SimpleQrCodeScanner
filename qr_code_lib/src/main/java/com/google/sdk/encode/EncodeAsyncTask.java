package com.google.sdk.encode;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.google.zxing.WriterException;

/**
 * 根据指定的内容生成 二维码图片
 */
public class EncodeAsyncTask extends AsyncTask<String ,Void,Bitmap> {
    private Context mContext;
    private double  mSize;
    private EncodeResultListener mEncodeResultListener;
    public EncodeAsyncTask(Context context, double size, EncodeResultListener encodeResultListener){
        this.mContext = context;
        this.mSize = size;
        this.mEncodeResultListener = encodeResultListener;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        int smallerDimension = EncodeUtils.getSmallScreenSize(mContext);
        double defaultSize = 0.875;
        if(mSize <= 0 || mSize >1) {
            mSize = defaultSize;
        }
        smallerDimension = (int) (smallerDimension * mSize);
        Intent intent = EncodeUtils.getQrIntent(strings[0]);
        QRCodeEncoder qrCodeEncoder = null;
        try {
            qrCodeEncoder = new QRCodeEncoder(intent, smallerDimension, false);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if(bitmap != null) {
            mEncodeResultListener.encodeSuccess(bitmap);
        }else{
            mEncodeResultListener.encodeFail();
        }
    }
}
