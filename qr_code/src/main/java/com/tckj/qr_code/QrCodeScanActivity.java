package com.tckj.qr_code;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.sdk.camera.CameraManager;
import com.google.sdk.decode.DecodeResult;
import com.google.sdk.manager.AmbientLightManager;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.tckj.config.QrCodeConstant;
import com.tckj.qr_code_scan_core.QrBeepManager;
import com.tckj.qr_code_scan_core.QrCaptureActivityHandler;
import com.tckj.qr_code_scan_core.QrFinishListener;
import com.tckj.qr_code_scan_core.ViewfinderView;
import com.tckj.utils.SettingUtils;
import com.tckj.utils.StatusBarUtils;

import java.io.IOException;
import java.util.Collection;

/**
 * 二维码扫描界面
 *      注意 ： 请确保使用扫码时获得了相机权限
 *      1. 单独使用扫描界面时，QrCodeScanActivity.startScan(Activity activity); 直接跳转到扫码界面
 *
 *      2. 扫码结束后，在接受数据的界面（跳转前的界面） 重写 onActivityResult 方法，可以获取二维码内容
 *      DecodeResult decodeResult = data.getParcelableExtra(QrCodeConstant.QR_CODE_SCAN_RESULT);
 *      decodeResult 中包含：
 *          1.二维码的类型　　String type = decodeResult.getType();
 *          2.格式　　　　　　String format = decodeResult.getFormat();
 *          3.数据内容　　　　String content = decodeResult.getContent();
 *          4.扫码时间　　　　long date = decodeResult.getDate();
 */
public class QrCodeScanActivity extends AppCompatActivity implements SurfaceHolder.Callback {


    private static final String TAG = QrCodeScanActivity.class.getSimpleName();
    private CameraManager cameraManager;
    private QrCaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private TextView statusView;
    private Result lastResult;
    private boolean hasSurface;
    private Collection<BarcodeFormat> decodeFormats;
    private QrBeepManager beepManager;
    private AmbientLightManager ambientLightManager;
    private SurfaceView surfaceView;
    private ImageView scanBack;

    /**
     * 打开本扫描 activity
     * @param activity      从哪个界面跳转过来
     */
    public static void startScan(Activity activity){
        Intent intent = new Intent(activity, QrCodeScanActivity.class);
        activity.startActivityForResult(intent, QrCodeConstant.QR_CODE_REQUEST_SCAN_RESULT);
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }


    @Override
    public void onCreate(Bundle icicle) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(icicle);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_qr_code_scan);
        StatusBarUtils.compat(this, StatusBarUtils.COLOR_DEFAULT);
        hasSurface = false;
        beepManager = new QrBeepManager(this);
        ambientLightManager = new AmbientLightManager(this);
        initView();
    }

    private void initView() {
        viewfinderView = findViewById(R.id.viewfinder_view);
        surfaceView = findViewById(R.id.preview_view);
        scanBack = findViewById(R.id.qr_code_scan_back);
        scanBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
        // want to open the camera driver and measure the screen size if we're going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
        // off screen.
        cameraManager = new CameraManager(getApplication());
        viewfinderView.setCameraManager(cameraManager);

        handler = null;
        lastResult = null;

        resetStatusView();

        beepManager.updatePrefs();
        ambientLightManager.start(cameraManager);

        decodeFormats = null;

        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
        }
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        ambientLightManager.stop();
        beepManager.close();
        cameraManager.closeDriver();
        //historyManager = null; // Keep for onActivityResult
        if (!hasSurface) {
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if ( lastResult != null) {
                    restartPreviewAfterDelay(0L);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_FOCUS:
            case KeyEvent.KEYCODE_CAMERA:
                // Handle these events so they don't launch the Camera app
                return true;
            // Use volume up/down to turn on light
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                cameraManager.setTorch(false);
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                cameraManager.setTorch(true);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // do nothing
    }

    /**
     * A valid barcode has been found, so give an indication of success and show the results.
     *
     * @param rawResult The contents of the barcode.
     */
    public void handleDecode(Result rawResult) {
        lastResult = rawResult;

        // Then not from history, so beep/vibrate and we have an image to draw on
        beepManager.playBeepSoundAndVibrate();
        handleDecodeInternally(rawResult);
    }


    // Put up our own UI for how to handle the decoded contents.
    private void handleDecodeInternally(Result rawResult) {
        DecodeResult decodeResult = new DecodeResult(rawResult);
        Intent intent = new Intent();
        intent.putExtra(QrCodeConstant.QR_CODE_SCAN_RESULT,decodeResult);
        setResult(RESULT_OK,intent);
        finish();
    }


    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (handler == null) {
                handler = new QrCaptureActivityHandler(this, decodeFormats, cameraManager);
            }
        } catch (IOException ioe) {
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            displayFrameworkBugMessageAndExit();
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示 ：");
        builder.setMessage(getString(R.string.qr_code_camera_error_msg));
        builder.setPositiveButton(R.string.qr_code_error_button_ok, new QrFinishListener(this));
        builder.setNegativeButton(R.string.qr_code_error_button_give_right, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SettingUtils.startTOAppDetails(QrCodeScanActivity.this);
            }
        });
        builder.setOnCancelListener(new QrFinishListener(this));
        builder.show();
    }



    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(com.tc.zxingcorelibrary.R.id.restart_preview, delayMS);
        }
        resetStatusView();
    }

    private void resetStatusView() {
//        statusView.setText(com.tc.zxingcorelibrary.R.string.msg_default_status);
//        statusView.setVisibility(View.GONE);
        viewfinderView.setVisibility(View.VISIBLE);
        lastResult = null;
    }

    public void drawViewfinder() {
        //viewfinderView.drawViewfinder();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
