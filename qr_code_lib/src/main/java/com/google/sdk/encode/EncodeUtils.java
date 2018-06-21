package com.google.sdk.encode;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import com.google.sdk.Intents;
import com.google.sdk.config.CommonSet;
import com.google.zxing.BarcodeFormat;

import static android.content.Context.WINDOW_SERVICE;

public class EncodeUtils {
    public static int getSmallScreenSize(Context context){

        WindowManager manager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        int width = displaySize.x;
        int height = displaySize.y;
        int smallerDimension = width < height ? width : height;
        return smallerDimension;
    }

    public static  Intent getQrIntent(String content) {
        Intent intent = new Intent(Intents.Encode.ACTION);
        intent.addFlags(Intents.FLAG_NEW_DOC);
        intent.putExtra(Intents.Encode.TYPE, CommonSet.Type.TEXT);
        intent.putExtra(Intents.Encode.DATA, content);
        intent.putExtra(Intents.Encode.FORMAT, BarcodeFormat.QR_CODE.toString());
        return intent;
    }
}
