package com.qrcode.config;

public interface QrCodeConstant {
    //扫描结果内容
    String QR_CODE_SCAN_RESULT = "QR_CODE_SCAN_RESULT";
    //要生成二维码的 内容
    String QR_CODE_GENERATE_CONTENT = "QR_CODE_GENERATE_CONTENT";
    //保存二维码的文件夹路径
    String QR_CODE_GENERATE_SAVE_PATH = "QR_CODE_GENERATE_SAVE_PATH";
    //要解码的二维码的图片路径
    String QR_CODE_DECODE_IMAGE_PATH = "QR_CODE_DECODE_IMAGE_PATH";
    //二维码的解码结果
    String QR_CODE_DECODE_RESULT = "QR_CODE_DECODE_RESULT";
    //扫码的请求码
    int QR_CODE_REQUEST_SCAN_RESULT =9001;
    //进入解码界面的请求码
    int QR_CODE_REQUEST_DECODE_RESULT =9002;
    //相册选择二维码图片的请求
    int QR_CODE_SELECT_DECODE_IMAGE =9003;
}
