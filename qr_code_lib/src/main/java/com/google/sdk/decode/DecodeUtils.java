package com.google.sdk.decode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;

public class DecodeUtils {
    /**
     * 最关键在此，把options.inJustDecodeBounds = true;
     * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
     */
    public static BitmapFactory.Options getBitmapOption(String imagePath){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options); // 此时返回的bitmap为null
        options.inJustDecodeBounds = false;
        options.inSampleSize = DecodeUtils.calculateInSampleSize(options,256,256);
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return options;
    }

    /**
     * 将 bitmap 的
     * @param data   The YUV preview frame.
     * @param width  The width of the bitmap.
     * @param height The height of the bitmap.
     */
    public static Result decode(byte[] data, int width, int height) {

        Result rawResult = null;
        // 构造基于平面的YUV亮度源，即包含二维码区域的数据源
        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, width, height, 0, 0,width, height, false);
        if (source != null) {
            // 构造二值图像比特流，使用HybridBinarizer算法解析数据源
//            MultiFormatReader multiFormatReader = prepareDecodeConfig();
//            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            BinaryBitmap bitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
            try {
                // 采用MultiFormatReader解析图像，可以解析多种数据格式
//                rawResult = multiFormatReader.decodeWithState(bitmap);
                QRCodeReader qrCodeReader = new QRCodeMultiReader();
                rawResult = qrCodeReader.decode(bitmap);
            } catch (ReaderException re) {
                // continue
            }
        }
        if (rawResult != null) {
            // Don't log the barcode contents for security.

            DecodeResult decodeResult = new DecodeResult(rawResult);
            return rawResult;
        } else {
            return null;
        }
    }

    private static MultiFormatReader prepareDecodeConfig() {
        MultiFormatReader multiFormatReader = new MultiFormatReader();
        QRCodeReader qrCodeReader = new QRCodeReader();
        QRCodeMultiReader qrCodeMultiReader = new QRCodeMultiReader();
        EnumSet<BarcodeFormat> decodeFormats = EnumSet.noneOf(BarcodeFormat.class);
        decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
        EnumMap<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        multiFormatReader.setHints(hints);
        return multiFormatReader;
    }

    private static byte[] yuvs;
    /**
     * 根据Bitmap的ARGB值生成YUV420SP数据。
     *
     * @param inputWidth image width
     * @param inputHeight image height
     * @param scaled bmp
     * @return YUV420SP数组
     */
    public static byte[] getYUV420sp(int inputWidth, int inputHeight, Bitmap scaled) {
        int[] argb = new int[inputWidth * inputHeight];

        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        /**
         * 需要转换成偶数的像素点，否则编码YUV420的时候有可能导致分配的空间大小不够而溢出。
         */
        int requiredWidth = inputWidth % 2 == 0 ? inputWidth : inputWidth + 1;
        int requiredHeight = inputHeight % 2 == 0 ? inputHeight : inputHeight + 1;

        int byteLength = requiredWidth * requiredHeight * 3 / 2;
        if (yuvs == null || yuvs.length < byteLength) {
            yuvs = new byte[byteLength];
        } else {
            Arrays.fill(yuvs, (byte) 0);
        }

        encodeYUV420SP(yuvs, argb, inputWidth, inputHeight);

        scaled.recycle();

        return yuvs;
    }
    /**
     * RGB转YUV420sp
     *
     * @param yuv420sp inputWidth * inputHeight * 3 / 2
     * @param argb inputWidth * inputHeight
     * @param width image width
     * @param height image height
     */
    private static void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        // 帧图片的像素大小
        final int frameSize = width * height;
        // ---YUV数据---
        int Y, U, V;
        // Y的index从0开始
        int yIndex = 0;
        // UV的index从frameSize开始
        int uvIndex = frameSize;

        // ---颜色数据---
        int R, G, B;
        int rgbIndex = 0;

        // ---循环所有像素点，RGB转YUV---
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                R = (argb[rgbIndex] & 0xff0000) >> 16;
                G = (argb[rgbIndex] & 0xff00) >> 8;
                B = (argb[rgbIndex] & 0xff);
                //
                rgbIndex++;

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                Y = Math.max(0, Math.min(Y, 255));
                U = Math.max(0, Math.min(U, 255));
                V = Math.max(0, Math.min(V, 255));

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                // meaning for every 4 Y pixels there are 1 V and 1 U. Note the sampling is every other
                // pixel AND every other scan line.
                // ---Y---
                yuv420sp[yIndex++] = (byte) Y;
                // ---UV---
                if ((j % 2 == 0) && (i % 2 == 0)) {
                    //
                    yuv420sp[uvIndex++] = (byte) V;
                    //
                    yuv420sp[uvIndex++] = (byte) U;
                }
            }
        }
    }

    /**
     * 根据给定的宽度和高度动态计算图片压缩比率
     *
     * @param options Bitmap配置文件
     * @param reqWidth 需要压缩到的宽度
     * @param reqHeight 需要压缩到的高度
     * @return 压缩比
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
