/*
 * Copyright (C) 2010 ZXing authors
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

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.tc.zxingcorelibrary.R;
import com.qrcode.qr_code.QrCodeScanActivity;

import java.io.ByteArrayOutputStream;
import java.util.Map;

final class QrDecodeHandler extends Handler {

  private static final String TAG = QrDecodeHandler.class.getSimpleName();

  private final QrCodeScanActivity activity;
  private final MultiFormatReader multiFormatReader;
  private boolean running = true;

  QrDecodeHandler(QrCodeScanActivity activity, Map<DecodeHintType,Object> hints) {
    multiFormatReader = new MultiFormatReader();
    multiFormatReader.setHints(hints);
    this.activity = activity;
  }

  @Override
  public void handleMessage(Message message) {
    if (message == null || !running) {
      return;
    }
    if (message.what == R.id.decode) {
      decode((byte[]) message.obj, message.arg1, message.arg2);

    } else if (message.what == R.id.quit) {
      running = false;
      Looper.myLooper().quit();

    }
  }

  /**
   * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
   * reuse the same reader objects from one decode to the next.
   *
   * @param data   The YUV preview frame.
   * @param width  The width of the preview frame.
   * @param height The height of the preview frame.
   */
  private void decode(byte[] data, int width, int height) {
    long start = System.currentTimeMillis();
    Result rawResult = null;

    byte[] rotatedData = new byte[data.length];
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++)
        rotatedData[x * height + height - y - 1] = data[x + y * width];
    }
    int tmp = width;
    width = height;
    height = tmp;
    data = rotatedData;
    // 构造基于平面的YUV亮度源，即包含二维码区域的数据源

    PlanarYUVLuminanceSource source = activity.getCameraManager().buildLuminanceSource(data, width, height);
    if (source != null) {
      // 构造二值图像比特流，使用HybridBinarizer算法解析数据源
      BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
      try {
        // 采用MultiFormatReader解析图像，可以解析多种数据格式
        rawResult = multiFormatReader.decodeWithState(bitmap);
      } catch (ReaderException re) {
        // continue
      } finally {
        multiFormatReader.reset();
      }
    }
        // Hanlder处理解析失败或成功的结果
    Handler handler = activity.getHandler();
    if (rawResult != null) {
      // Don't log the barcode contents for security.
      long end = System.currentTimeMillis();
      Log.d(TAG, "Found barcode in " + (end - start) + " ms");
      if (handler != null) {
        Message message = Message.obtain(handler, R.id.decode_succeeded, rawResult);
        message.sendToTarget();
      }
    } else {
      if (handler != null) {
        Message message = Message.obtain(handler, R.id.decode_failed);
        message.sendToTarget();
      }
    }
  }

  private static void bundleThumbnail(PlanarYUVLuminanceSource source, Bundle bundle) {
    int[] pixels = source.renderThumbnail();
    int width = source.getThumbnailWidth();
    int height = source.getThumbnailHeight();
    Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
    ByteArrayOutputStream out = new ByteArrayOutputStream();    
    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
    bundle.putByteArray(QrDecodeThread.BARCODE_BITMAP, out.toByteArray());
    bundle.putFloat(QrDecodeThread.BARCODE_SCALED_FACTOR, (float) width / source.getWidth());
  }

}
