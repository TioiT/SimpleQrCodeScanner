package com.tckj.qr_code;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.sdk.ScanCodeSDK;
import com.google.sdk.decode.DecodeResult;
import com.google.sdk.encode.EncodeResultListener;
import com.tckj.config.QrCodeConstant;
import com.tckj.utils.StatusBarUtils;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * 当此 Module 作为单独的 App 运行时，此界面为主界面。
 * 包含可测试的三个功能
 *          1.扫码
 *          2.根据指定的内容生成二维码
 *          3.解码所选择的图片
 */
public class QrCodeMainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button startScan;
    private Button generateBitmap;
    private Button decodeBitmap;
    private TextView showDecode;
    private EditText content_to_show;
    private EditText imageName;
    private ImageView showQr;
    private ProgressDialog progressDialog;
    private Button decodeBitmapPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.compat(this, StatusBarUtils.COLOR_DEFAULT);
        setContentView(R.layout.activity_qr_code_main);
        initView();
        initListener();

    }

    private void initView() {
        startScan = findViewById(R.id.qrcode_start_scan);
        showQr = findViewById(R.id.qrcode_show_qr);
        generateBitmap = findViewById(R.id.qrcode_generate_bitmap);
        decodeBitmap = findViewById(R.id.qrcode_decode_bitmap_select);
        decodeBitmapPath = findViewById(R.id.qrcode_decode_bitmap_path);
        showDecode = findViewById(R.id.qrcode_show_decode);
        content_to_show = findViewById(R.id.qrcode_content_to_show);
        imageName = findViewById(R.id.qrcode_image_name);
    }

    private void initListener() {
        startScan.setOnClickListener(this);
        generateBitmap.setOnClickListener(this);
        decodeBitmap.setOnClickListener(this);
        decodeBitmapPath.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //扫码
            case R.id.qrcode_start_scan :
                ScanCodeSDK.getInstance().buildScanConfig(this)
                        .setPlayBeep(true)
                        .setVibrate(true);
                QrCodeScanActivity.startScan(this);
                break;
            //生成二维码
            case R.id.qrcode_generate_bitmap:
                String content = content_to_show.getText().toString().trim();
                if(!TextUtils.isEmpty(content)) {
                    QrCodeGenerateActivity.startGenerate(this,content,null);
                }else{
                    Toast.makeText(QrCodeMainActivity.this, "请先输入二维码的内容", Toast.LENGTH_SHORT).show();
                }
                break;
            //解码选择的图片
            case R.id.qrcode_decode_bitmap_select:
                QrCodeDecodeActivity.startDecode(QrCodeMainActivity.this,null);
                break;
            case R.id.qrcode_decode_bitmap_path:
                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() +File.separator+"二维码"+ File.separator + "vcard2.jpg";
                QrCodeDecodeActivity.startDecode(QrCodeMainActivity.this,path);
                break;
        }
    }



    /**
     * 根据指定的内容和大小生成二维码
     * @param content   二维码内容
     * @param size      二维码的大小
     */
    private void generateQrCode(String content,double size) {
        showProgressDialog("正在生成二维码，请稍等。。。",false);
        ScanCodeSDK.getInstance().EncodeAsQRCodeImage(QrCodeMainActivity.this, content, size, new EncodeResultListener() {
            @Override
            public void encodeSuccess(Bitmap bitmap) {
                dismissProgressDialog();
                showQr.setScaleType(ImageView.ScaleType.CENTER);
                showQr.setImageBitmap(bitmap);
            }

            @Override
            public void encodeFail() {
                dismissProgressDialog();
            }
        });
    }


    /**
     * 接收扫描到的二维码内容
     * 在哪个界面启动扫描界面，就在哪个界面获取扫描结果
     * @param requestCode   请求码
     * @param resultCode    结果码
     * @param data          二维码的数据，包含二维码的类型，格式，数据内容 和 扫码时间
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DecodeResult decodeResult = null;
        if(requestCode == QrCodeConstant.QR_CODE_REQUEST_SCAN_RESULT && resultCode == RESULT_OK) {
            decodeResult = data.getParcelableExtra(QrCodeConstant.QR_CODE_SCAN_RESULT);
        }
        if(requestCode == QrCodeConstant.QR_CODE_REQUEST_DECODE_RESULT && resultCode == RESULT_OK) {
            decodeResult  = data.getParcelableExtra(QrCodeConstant.QR_CODE_DECODE_RESULT);
        }
            if(decodeResult != null) {
                Toast.makeText(QrCodeMainActivity.this, decodeResult.getContent(), Toast.LENGTH_SHORT).show();
                String content = decodeResult.getContent();
                String format = decodeResult.getFormat();
                String type = decodeResult.getType();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                long date = decodeResult.getDate();
                Log.i("lish","扫描时间 = " + simpleDateFormat.format(date));
                Log.i("lish","扫描内容 = " + content);
                Log.i("lish","扫描格式 = " + format);
                Log.i("lish","扫描类型 = " + type);
                showDecode.setText(decodeResult.getContent());
            }


    }
    /**
     * 弹出进度框，点击返回键不会消失
     *
     * @param text
     * @param isCancelable
     */
    public void showProgressDialog(String text, boolean isCancelable) {
        dismissProgressDialog();
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(isCancelable);
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.setMessage(text);
        progressDialog.show();
    }

    /**
     * 隐藏进度框
     */
    public void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
