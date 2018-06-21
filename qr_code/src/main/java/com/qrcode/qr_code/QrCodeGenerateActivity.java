package com.qrcode.qr_code;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.sdk.ScanCodeSDK;
import com.google.sdk.encode.EncodeResultListener;
import com.qrcode.config.QrCodeConstant;
import com.qrcode.utils.SettingUtils;
import com.qrcode.utils.StatusBarUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * 生成 二维码 界面
 *
 * 注意 ： 请确保使用保存二维码时，有了读写内存卡的权限。
 *
 * 跳转该界面时，使用 startGenerate 方法，方法参数参照方法说明
 *
 * 功能包括：
 * 1. 展示生成的 QRCode 二维码
 * 2. 保存 生成的二维码到指定路径，未指定路径时，保存至默认路径 /storage/emulated/0/Pictures/二维码
 * 保存的二维码图片已时间命名2018-06-06 12：00：00.jpg
 */
public class QrCodeGenerateActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView showCode;
    private String content;
    private ProgressDialog progressDialog;
    private ImageView generateBack;
    private ImageView generateMore;
    private Dialog saveDialog;
    public Bitmap qrCodeBitmap;
    private FileOutputStream fileOutputStream;

    /**
     * 启动生成二维码界面
     *
     * @param activity       从哪个 activity 开始跳转
     * @param content        生成二维码的内容
     * @param saveQrCodePath 保存生成的二维码的文件夹路径
     *                       可以传 null，传 null 时，选择保存时，保存至默认路径 /storage/emulated/0/Pictures/二维码
     */
    public static void startGenerate(Activity activity, String content, String saveQrCodePath) {
        Intent intent1 = new Intent(activity, QrCodeGenerateActivity.class);
        intent1.putExtra(QrCodeConstant.QR_CODE_GENERATE_CONTENT, content);
        intent1.putExtra(QrCodeConstant.QR_CODE_GENERATE_SAVE_PATH, saveQrCodePath);
        activity.startActivity(intent1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_generate);
        StatusBarUtils.compat(this, StatusBarUtils.COLOR_DEFAULT);
        initView();
        initData();
    }

    private void initView() {
        showCode = findViewById(R.id.qr_code_show_genetate_code);
        generateBack = findViewById(R.id.qr_code_generate_back);
        generateMore = findViewById(R.id.qr_code_generate_more);
        generateBack.setOnClickListener(this);
        generateMore.setOnClickListener(this);
    }

    private void initData() {
        Intent intent = getIntent();
        content = intent.getStringExtra(QrCodeConstant.QR_CODE_GENERATE_CONTENT);
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(QrCodeGenerateActivity.this, "请先设置要生成二维码的内容", Toast.LENGTH_SHORT).show();
            return;
        }
        showProgressDialog("正在生成二维码，请稍后。。。", true);
        ScanCodeSDK.getInstance().EncodeAsQRCodeImage(this, content, 0.8, new EncodeResultListener() {
            @Override
            public void encodeSuccess(Bitmap bitmap) {
                dismissProgressDialog();
                if (bitmap != null) {
                    qrCodeBitmap = bitmap;
                    showCode.setScaleType(ImageView.ScaleType.CENTER);
                    showCode.setImageBitmap(qrCodeBitmap);
                }
            }

            @Override
            public void encodeFail() {
                dismissProgressDialog();
                Toast.makeText(QrCodeGenerateActivity.this, "生成二维码失败，请重试", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.qr_code_generate_back:
                finish();
                break;
            case R.id.qr_code_generate_more:
                showSaveDialog();

                break;
            case R.id.qr_code_save_qrcode_btn:
                if (saveDialog.isShowing()) {
                    saveDialog.dismiss();
                }
                saveQrCodeBitmap();
                break;
        }
    }

    /**
     * 保存生成的 QrCode
     * 若没有设置 保存路径，保存至默认路径 /storage/emulated/0/Pictures/二维码
     */
    private void saveQrCodeBitmap() {
        showProgressDialog("正在保存二维码，请稍后。。。", true);
        String savePath = getIntent().getStringExtra(QrCodeConstant.QR_CODE_GENERATE_SAVE_PATH);
        if (TextUtils.isEmpty(savePath)) {
            //保存至默认路径
            String rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()+File.separator+"二维码";
            saveQrCode(rootPath);
        } else {
            saveQrCode(savePath);
        }
    }

    private void saveQrCode(String rootPath) {

        try {
            File file = new File(rootPath);
            if (!file.exists()) {
                file.mkdirs();
            }
            String fileName = getFileName();
            File qrCode = new File(rootPath, fileName);
            fileOutputStream = new FileOutputStream(qrCode);
            qrCodeBitmap.compress(Bitmap.CompressFormat.JPEG, 50, fileOutputStream);
            Toast.makeText(QrCodeGenerateActivity.this, "二维码已保存至 :" + rootPath, Toast.LENGTH_LONG).show();
            sendBroadcastUpdate(qrCode);
        } catch (Exception e) {
            e.printStackTrace();
            displaySaveBugMessageAndExit();
            Toast.makeText(QrCodeGenerateActivity.this, "二维码保存失败", Toast.LENGTH_SHORT).show();
        }
        try {
            if(fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        dismissProgressDialog();
    }

    private void sendBroadcastUpdate(File qrCode) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(qrCode);
        intent.setData(uri);
        sendBroadcast(intent);
    }

    private void displaySaveBugMessageAndExit() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示 ：");
        builder.setMessage(getString(R.string.qr_code_save_error_msg));
        builder.setPositiveButton(R.string.qr_code_error_button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.qr_code_error_button_give_right, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SettingUtils.startTOAppDetails(QrCodeGenerateActivity.this);
            }
        });
        builder.show();
    }
    private String getFileName() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long timeMillis = System.currentTimeMillis();
        return simpleDateFormat.format(timeMillis) + ".jpg";
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

    /**
     * 从底部弹出 保存的 dialog
     */
    private void showSaveDialog() {
        saveDialog = new Dialog(this, R.style.QrCodeGenerateDialog);
        RelativeLayout root = (RelativeLayout) LayoutInflater.from(this).inflate(
                R.layout.qr_code_dialog_layout, null);
        //初始化视图
        root.findViewById(R.id.qr_code_save_qrcode_btn).setOnClickListener(this);
        saveDialog.setContentView(root);
        Window dialogWindow = saveDialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        lp.x = 0; // 新位置X坐标
        lp.y = 0; // 新位置Y坐标
        lp.width = (int) getResources().getDisplayMetrics().widthPixels; // 宽度
        root.measure(0, 0);
        lp.height = root.getMeasuredHeight();

        dialogWindow.setAttributes(lp);
        saveDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseBitmap(qrCodeBitmap);
    }

    private void releaseBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            bitmap.recycle();
        }
    }
}
