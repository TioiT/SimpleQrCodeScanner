/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qrcode.qr_code_scan_core;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.sdk.config.DecodeFormat;
import com.google.sdk.decode.DecodeFormatManager;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.ResultPointCallback;
import com.qrcode.qr_code.QrCodeScanActivity;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * This thread does all the heavy lifting of decoding the images.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
final class QrDecodeThread extends Thread  {

  public static final String BARCODE_BITMAP = "barcode_bitmap";
  public static final String BARCODE_SCALED_FACTOR = "barcode_scaled_factor";

  private final QrCodeScanActivity activity;
  private final Map<DecodeHintType,Object> hints;
  private Handler handler;
  private final CountDownLatch handlerInitLatch;

  QrDecodeThread(QrCodeScanActivity activity,
                 Collection<BarcodeFormat> decodeFormats,
                 ResultPointCallback resultPointCallback) {

    this.activity = activity;
    handlerInitLatch = new CountDownLatch(1);

    hints = new EnumMap<>(DecodeHintType.class);

    // The prefs can't change while the thread is running, so pick them up once here.
    if (decodeFormats == null || decodeFormats.isEmpty()) {
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
      decodeFormats = EnumSet.noneOf(BarcodeFormat.class);
      //是否是全格式支持，若是则不再往下判断
      if(prefs.getBoolean(DecodeFormat.DECODE_All_CODE,false)) {
          decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
          decodeFormats.addAll(DecodeFormatManager.TWO_D_FORMATS);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, resultPointCallback);
        return;
      }
      if(prefs.getBoolean(DecodeFormat.DECODE_All_1D_CODE,false)) {
          decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
      }else {
          if (prefs.getBoolean(DecodeFormat.DECODE_1D_PRODUCT, false)) {
              decodeFormats.addAll(DecodeFormatManager.PRODUCT_FORMATS);
          }
          if (prefs.getBoolean(DecodeFormat.DECODE_1D_INDUSTRIAL, false)) {
              decodeFormats.addAll(DecodeFormatManager.INDUSTRIAL_FORMATS);
          }
      }
      if(prefs.getBoolean(DecodeFormat.DECODE_ALL_2D_CODE,false)) {
          decodeFormats.addAll(DecodeFormatManager.TWO_D_FORMATS);
      }else{
          if (prefs.getBoolean(DecodeFormat.DECODE_QR, true)) {
              decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
          }
          if (prefs.getBoolean(DecodeFormat.DECODE_DATA_MATRIX, false)) {
              decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
          }
          if (prefs.getBoolean(DecodeFormat.DECODE_AZTEC, false)) {
              decodeFormats.addAll(DecodeFormatManager.AZTEC_FORMATS);
          }
          if (prefs.getBoolean(DecodeFormat.DECODE_PDF417, false)) {
              decodeFormats.addAll(DecodeFormatManager.PDF417_FORMATS);
          }
      }

    }
    hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

    hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, resultPointCallback);
    Log.i("QrDecodeThread", "Hints: " + hints);
  }

  Handler getHandler() {
    try {
      handlerInitLatch.await();
    } catch (InterruptedException ie) {
      // continue?
    }
    return handler;
  }

  @Override
  public void run() {
    Looper.prepare();
    handler = new QrDecodeHandler(activity, hints);
    handlerInitLatch.countDown();
    Looper.loop();
  }

}
