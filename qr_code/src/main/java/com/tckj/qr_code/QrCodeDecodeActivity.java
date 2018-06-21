package com.tckj.qr_code;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import com.google.sdk.decode.DecodeResultListener;
import com.tckj.config.QrCodeConstant;
import com.tckj.utils.StatusBarUtils;

import java.io.File;

public class QrCodeDecodeActivity extends AppCompatActivity implements View.OnClickListener {

    private Button decodeBtn;
    private ImageView headBack;
    private TextView result;
    private EditText imageName;
    private ProgressDialog progressDialog;

    public static void startDecode(Activity activity,String imagePath){
        Intent intent = new Intent(activity,QrCodeDecodeActivity.class);
        intent.putExtra(QrCodeConstant.QR_CODE_DECODE_IMAGE_PATH,imagePath);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.startActivityForResult(intent,QrCodeConstant.QR_CODE_REQUEST_DECODE_RESULT);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_decode);
        StatusBarUtils.compat(this, Color.parseColor("#01000000"));
        initView();
        initData();
        
    }

    private void initData() {
        final String imagePath = getIntent().getStringExtra(QrCodeConstant.QR_CODE_DECODE_IMAGE_PATH);
        if(!TextUtils.isEmpty(imagePath)) {
            showProgressDialog("正在解析，请稍后",false);
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    decodeQrCode(imagePath,null);
//                }
//            }).start();
            decodeQrCode(imagePath,null);
        }else {
            startSelectImage();
        }
    }

    private void startSelectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, QrCodeConstant.QR_CODE_SELECT_DECODE_IMAGE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //获取图片路径
        if (requestCode == QrCodeConstant.QR_CODE_SELECT_DECODE_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            showProgressDialog("正在解析，请稍后",false);
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            final String imagePath = c.getString(columnIndex);
            c.close();
            Log.i("lish","imagePath = " + imagePath);
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    decodeQrCode(imagePath,null);
//                }
//            }).start();
            decodeQrCode(imagePath,null);
        }else{
            closeDecodeActivity();
        }
    }
    private void initView() {
        decodeBtn = findViewById(R.id.decode_qr_code_btn);
        headBack = findViewById(R.id.decode_qr_code_scan_back);
        result = findViewById(R.id.decode_qrcode_show_result);
        imageName = findViewById(R.id.decode_qr_code_image_name);
        headBack.setOnClickListener(this);
        decodeBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.decode_qr_code_scan_back :
                finish();
                break;
            case R.id.decode_qr_code_btn:
                String name = imageName.getText().toString().trim();
                if(!TextUtils.isEmpty(name)) {
                    decodeQrCode(null,name);
                }else{
                    Toast.makeText(QrCodeDecodeActivity.this, "请先输入图片名称", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * 解析二维码
     * @param imageName     二维码名称
     */
    private void decodeQrCode(String imagePath,String imageName) {
        String path = null;
        if(!TextUtils.isEmpty(imageName)) {
            path = Environment.getExternalStorageDirectory()+ File.separator+"Pictures/"+imageName+".jpg";
        }else {
            path = imagePath;
        }
        File file = new File(path);
        boolean exists = file.exists();
        if(!exists) {
            Toast.makeText(QrCodeDecodeActivity.this, "图片不存在", Toast.LENGTH_SHORT).show();
            closeDecodeActivity();
            return;
        }

        ScanCodeSDK.getInstance().DecodeFromImage(path, new DecodeResultListener() {
            @Override
            public void decodeSuccess(final DecodeResult decodeResult) {
//                result.setText(decodeResult.getContent()+'\n'+decodeResult.getType());
                dismissProgressDialog();
                Intent intent = new Intent();
                intent.putExtra(QrCodeConstant.QR_CODE_DECODE_RESULT,decodeResult);
                setResult(RESULT_OK,intent);
                closeDecodeActivity();
            }

            @Override
            public void decodeFail() {
//                result.setText("解析错误，请重试");
                Toast.makeText(QrCodeDecodeActivity.this, "解析错误，请重试", Toast.LENGTH_SHORT).show();
                dismissProgressDialog();
                closeDecodeActivity();
            }
        });
    }

    private void closeDecodeActivity() {
        finish();
        overridePendingTransition(0,0);
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
