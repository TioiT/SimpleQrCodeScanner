package com.google.sdk;

import android.content.Context;

import com.google.sdk.config.ScanConfig;
import com.google.sdk.decode.DecodeAsyncTask;
import com.google.sdk.decode.DecodeResultListener;
import com.google.sdk.encode.EncodeAsyncTask;
import com.google.sdk.encode.EncodeResultListener;

public class ScanCodeSDK {
    private ScanCodeSDK() {}
    private static class ScanCodeHolder{
        private static final ScanCodeSDK scanCode = new ScanCodeSDK();
    }

    /**
     * 获取ScanCode 的实例
     * @return  ScanCodeSDK
     */
    public static ScanCodeSDK getInstance (){
        return ScanCodeHolder.scanCode;
    }

    /**
     * 设置扫描的配置文件
     * @param context   上下文
     * @return ScanConfig 配置文件
     */
    public ScanConfig buildScanConfig(Context context){
       return new ScanConfig(context);
    }


    /**
     * 生成指定内容的二维码
     * 生成过程在子线程，回调在主线程
     * @param context       上下文
     * @param content       要生成的内容
     * @param size          生成 bitmap 的大小比例（取值范围0~1）
     * @param encodeResultListener    生成二维码图片的回调
     */
    public void EncodeAsQRCodeImage(Context context, String content, double size, EncodeResultListener encodeResultListener){
        EncodeAsyncTask encodeAsynvTask = new EncodeAsyncTask(context, size, encodeResultListener);
        encodeAsynvTask.execute(content);
    }


    /**
     * 根据参数路径，从图片中解析二维码内容
     * 解析在子线程，回调再主线程
     * @param imagePath                 图片的路径
     * @param decodeResultListener      解析结果的回调
     */
    public void DecodeFromImage( String imagePath,  DecodeResultListener decodeResultListener){
        DecodeAsyncTask decodeAsyncTask = new DecodeAsyncTask(decodeResultListener);
        decodeAsyncTask.execute(imagePath);

    }


}
