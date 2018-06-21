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

package com.tckj.qr_code_scan_core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.sdk.camera.CameraManager;
import com.tckj.qr_code.R;

import static android.graphics.PixelFormat.OPAQUE;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {

  private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
  private static final long ANIMATION_DELAY = 100L;
  private static final int CURRENT_POINT_OPACITY = 0xA0;
  private static final int MAX_RESULT_POINTS = 20;
  private static final int POINT_SIZE = 6;

  private final Paint paint;
  private final int mFrameColor;
  private int screenWidth;
  private final int maskColor;
  private final int textColor;
  private final int laserColor;
  private int scannerAlpha;
  private CameraManager cameraManager;
  private  int mAngleLength;
  private  int mFocusThick;
  private int mAngleThick;
  private int mScannerAlpha;
  private Rect mFrameRect;

  // This constructor is used when the class is built from an XML resource.
  public ViewfinderView(Context context, AttributeSet attrs) {
    super(context, attrs);

    // Initialize these once for performance rather than calling them every time in onDraw().
    //在构造函数中初始化一次常用值，不要在 onDraw 中每次使用，每次初始化
    paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Resources resources = getResources();
    mFrameColor = resources.getColor(com.tckj.qr_code.R.color.qr_code_finder_frame);
    maskColor = resources.getColor(R.color.qr_code_finder_mask);
    textColor = resources.getColor(R.color.qr_code_white);
    laserColor = resources.getColor(R.color.qr_code_finder_laser);

//    resultPointColor = resources.getColor(R.color.possible_result_points_green);
//    possibleResultPoints = new ArrayList<>(5);
//    lastPossibleResultPoints = null;

    initDefaultValue(context);
  }

  /**
   * 初始化默认值
   * @param context
   */
  private void initDefaultValue(Context context) {
    mAngleLength = 40;
    mFocusThick = 1;
    mAngleThick = 8;
    mScannerAlpha = 0;
    DisplayMetrics dm = context.getResources().getDisplayMetrics();
    screenWidth = dm.widthPixels;

    LayoutInflater inflater = LayoutInflater.from(context);
    RelativeLayout relativeLayout = (RelativeLayout) inflater.inflate(R.layout.layout_qr_code_scanner,null);
    FrameLayout frameLayout = relativeLayout.findViewById(com.tckj.qr_code.R.id.qr_code_fl_scanner);
    mFrameRect = new Rect();
    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) frameLayout.getLayoutParams();
    mFrameRect.left = (screenWidth - layoutParams.width) / 2;
    mFrameRect.top = layoutParams.topMargin;
    mFrameRect.right = mFrameRect.left + layoutParams.width;
    mFrameRect.bottom = mFrameRect.top + layoutParams.height;
  }

  public void setCameraManager(CameraManager cameraManager) {
    this.cameraManager = cameraManager;
  }

  @SuppressLint("DrawAllocation")
  @Override
  public void onDraw(Canvas canvas) {
    if (cameraManager == null) {
      return; // not ready yet, early draw before done configuring
    }
//    Rect frame = cameraManager.getFramingRect();
//    Rect previewFrame = cameraManager.getFramingRectInPreview();
//    if (frame == null || previewFrame == null) {
//      return;
//    }
    Rect frame = mFrameRect;
    if (frame == null) {
      return;
    }
    int width = canvas.getWidth();
    int height = canvas.getHeight();
    // 绘制聚焦框外的暗色透明层
    paint.setColor(maskColor);
    canvas.drawRect(0, 0, width, frame.top, paint);
    canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
    canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
    canvas.drawRect(0, frame.bottom + 1, width, height, paint);

//      // 画一根绿色的激光线表示二维码解码正在进行
//      // Draw a red "laser scanner" line through the middle to show decoding is active
//      paint.setColor(laserColor);
//      paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
//      scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
//      int middle = frame.height() / 2 + frame.top;
//      canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1, middle +2, paint);
//
//      float scaleX = frame.width() / (float) previewFrame.width();
//      float scaleY = frame.height() / (float) previewFrame.height();
//
//      List<ResultPoint> currentPossible = possibleResultPoints;
//      List<ResultPoint> currentLast = lastPossibleResultPoints;
//      int frameLeft = frame.left;
//      int frameTop = frame.top;
//      // 绘制解析过程中可能扫描到的关键点，使用绿色小圆点表示
//      if (currentPossible.isEmpty()) {
//        lastPossibleResultPoints = null;
//      } else {
//        possibleResultPoints = new ArrayList<>(5);
//        lastPossibleResultPoints = currentPossible;
//        paint.setAlpha(CURRENT_POINT_OPACITY);
//        paint.setColor(resultPointColor);
//        synchronized (currentPossible) {
//          for (ResultPoint point : currentPossible) {
//            canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
//                              frameTop + (int) (point.getY() * scaleY),
//                              POINT_SIZE, paint);
//          }
//        }
//      }
//      if (currentLast != null) {
//        paint.setAlpha(CURRENT_POINT_OPACITY / 2);
//        paint.setColor(resultPointColor);
//        synchronized (currentLast) {
//          float radius = POINT_SIZE / 2.0f;
//          for (ResultPoint point : currentLast) {
//            canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
//                              frameTop + (int) (point.getY() * scaleY),
//                              radius, paint);
//          }
//        }
//      }
      //绘制矩形白色边框
      drawFocusRect(canvas,frame);
      //绘制绿色四角
      drawAngle(canvas,frame);
      drawLaser(canvas,frame);
      drawText(canvas, frame);
      // Request another update at the animation interval, but only repaint the laser line,
      // not the entire viewfinder mask.
      // 重绘聚焦框里的内容，不需要重绘整个界面。
//      postInvalidateDelayed(ANIMATION_DELAY,
//                            frame.left - POINT_SIZE,
//                            frame.top - POINT_SIZE,
//                            frame.right + POINT_SIZE,
//                            frame.bottom + POINT_SIZE);
      postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);

  }

//  public void drawViewfinder() {
//    Bitmap resultBitmap = this.resultBitmap;
//    this.resultBitmap = null;
//    if (resultBitmap != null) {
//      resultBitmap.recycle();
//    }
//    invalidate();
//  }
  /**
   * 画聚焦框，白色的
   *
   * @param canvas
   * @param rect
   */
  private void drawFocusRect(Canvas canvas, Rect rect) {

    // 绘制焦点框
    paint.setColor(mFrameColor);
    // 上
    canvas.drawRect(rect.left + mAngleLength, rect.top, rect.right - mAngleLength, rect.top + mFocusThick, paint);
    // 左
    canvas.drawRect(rect.left, rect.top + mAngleLength, rect.left + mFocusThick, rect.bottom - mAngleLength,
            paint);
    // 右
    canvas.drawRect(rect.right - mFocusThick, rect.top + mAngleLength, rect.right, rect.bottom - mAngleLength,
            paint);
    // 下
    canvas.drawRect(rect.left + mAngleLength, rect.bottom - mFocusThick, rect.right - mAngleLength, rect.bottom,
            paint);
  }
  /**
   * 画绿色的四个角
   *
   * @param canvas
   * @param rect
   */
  private void drawAngle(Canvas canvas, Rect rect) {
//    float mAngleThick = (float) 6.0;
    paint.setColor(laserColor);
    paint.setAlpha(OPAQUE);
    paint.setStyle(Paint.Style.FILL);
    paint.setStrokeWidth(mAngleThick);
    int left = rect.left;
    int top = rect.top;
    int right = rect.right;
    int bottom = rect.bottom;
    // 左上角
    canvas.drawRect(left, top, left + mAngleLength, top + mAngleThick, paint);
    canvas.drawRect(left, top, left + mAngleThick, top + mAngleLength, paint);
    // 右上角
    canvas.drawRect(right - mAngleLength, top, right, top + mAngleThick, paint);
    canvas.drawRect(right - mAngleThick, top, right, top + mAngleLength, paint);
    // 左下角
    canvas.drawRect(left, bottom - mAngleLength, left + mAngleThick, bottom, paint);
    canvas.drawRect(left, bottom - mAngleThick, left + mAngleLength, bottom, paint);
    // 右下角
    canvas.drawRect(right - mAngleLength, bottom - mAngleThick, right, bottom, paint);
    canvas.drawRect(right - mAngleThick, bottom - mAngleLength, right, bottom, paint);
  }

  /**
   * 绘制文字提示
   * @param canvas
   * @param rect
   */
  private void drawText(Canvas canvas, Rect rect) {
    int margin = 40;
    paint.setColor(mFrameColor);
    paint.setTextSize(getResources().getDimension(com.tckj.qr_code.R.dimen.qr_code_text_size_13sp));
    String text = getResources().getString(com.tckj.qr_code.R.string.qr_code_auto_scan_notification);
    Paint.FontMetrics fontMetrics = paint.getFontMetrics();
    float fontTotalHeight = fontMetrics.bottom - fontMetrics.top;
    float offY = fontTotalHeight / 2 - fontMetrics.bottom;
    float newY = rect.bottom + margin + offY;
    Log.i("lish","screenWidth = " + screenWidth);
    float left = (screenWidth - paint.getTextSize() * text.length()) / 2;
    canvas.drawText(text, left, newY, paint);
  }

  private void drawLaser(Canvas canvas, Rect rect) {
    // 绘制焦点框内固定的一条扫描线
    paint.setColor(laserColor);
    paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
    scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
    int middle = rect.height() / 2 + rect.top;
    canvas.drawRect(rect.left + 2, middle - 1, rect.right - 1, middle + 2, paint);
  }
//  public void addPossibleResultPoint(ResultPoint point) {
//    List<ResultPoint> points = possibleResultPoints;
//    synchronized (points) {
//      points.add(point);
//      int size = points.size();
//      if (size > MAX_RESULT_POINTS) {
//        // trim it
//        points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
//      }
//    }
//  }

}
