package com.qrcode.qr_code;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.qrcode.config.QrCodeConstant;
import com.qrcode.utils.StatusBarUtils;

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
                //设置扫码的配置
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
                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() +File.separator+"二维码"+ File.separator + "自定义图片.jpg";
                Toast.makeText(QrCodeMainActivity.this, "想啥呢，图片路径需要你自己设置啊。亲", Toast.LENGTH_SHORT).show();
                //QrCodeDecodeActivity.startDecode(QrCodeMainActivity.this,path);
                break;
        }
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
                showDecode.setText(decodeResult.getContent());
            }


    }

}
