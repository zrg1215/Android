package com.hp.choosephoto.utils;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Method;

/**
 * Desc: 权限处理
 * Update by znq on 2016/11/21.
 */
public class CheckPermissionUtils {
    private static final String TAG = "CheckPermissionUtils";
    private static CheckPermissionUtils mInstance;

    //region 存储读写权限清单
    public static final int STORAGE_PERMISSION_CODE = 59;
    public static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    //endregion

    //region 手机设备权限清单
    /**
     * 获取手机状态回调code
     */
    public static final int PHONE_PERMISSION_CODE = 51;
    /**
     * 读取手机状态
     */
    public static final String READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
    //endregion

    //region 录音权限
    //获取录音回调code
    public static final int RECORD_PERMISSION_CODE = 27;

    // 读取录音权限
    public static final String RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    //endregion

    //region 定位权限
    public static final int LOCATION_PERMISSION_CODE = 58;
    //    public static final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    //endregion

    //region 定位权限
    public static final int CAMERA_PERMISSION_CODE = 26;
    public static final String CAMERA = Manifest.permission.CAMERA;
    //endregion

    //region 写日历权限
    public static final int READ_CALENDAR_PERMISSION_CODE = 41;
    public static final String READ_CALENDAR = Manifest.permission.READ_CALENDAR;

    //endregion

    private AudioRecord mAudioRecord = null;

    private short[] buffer;

    private CheckPermissionUtils() {
    }

    public static CheckPermissionUtils getInstance() {
        if (mInstance == null) {
            mInstance = new CheckPermissionUtils();
        }
        return mInstance;
    }

    /**
     * 6.0以上的权限判断
     *
     * @param permission Manifest.permission中权限的code
     * @param context    context.getApplicationContext() 使用全局的context防止内存泄漏
     */
    public boolean checkPermission23(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context.getApplicationContext(), permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    //初始化录音
    private void initAudioRecord() {
        //音频获取源
        int audioSource = MediaRecorder.AudioSource.MIC;
        // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
        int sampleRateInHz = 44100;
        // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
        int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
        // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        // 缓冲区字节大小
        int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfig, audioFormat);
        mAudioRecord = new AudioRecord(audioSource, sampleRateInHz,
                channelConfig, audioFormat, bufferSizeInBytes);
        //定义缓冲  
        buffer = new short[bufferSizeInBytes];
    }

    //释放录音
    private void releaseRecord() {
        //停止录制
        try {
            // 防止某些手机崩溃，例如联想
            if (mAudioRecord != null) {
                mAudioRecord.stop();
                // 彻底释放资源
                mAudioRecord.release();
                mAudioRecord = null;
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "releaseRecord:release fail ");
        }
    }

    /**
     * 检查
     */
    public boolean CheckRecordPermission(Context context) {
        if (1 == checkPermission(RECORD_PERMISSION_CODE, context)) {
            return false;
        }
        initAudioRecord();
        try {
            mAudioRecord.startRecording();
        } catch (IllegalStateException e) {
            releaseRecord();
            return false;
        }
        if (mAudioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            releaseRecord();
            return false;
        }
        int readSize = mAudioRecord.read(buffer, 0, buffer.length);
        if (readSize <= 0) {
            releaseRecord();
            return false;
        } else {
            releaseRecord();
            return true;
        }
    }

    /**
     * 反射调用系统权限,判断权限是否打开
     *
     * @param permissionCode 相应的权限所对应的code
     * @see {@link AppOpsManager }
     */
    private int checkPermission(int permissionCode, Context context) {
        int checkPermission = 0;
        if (Build.VERSION.SDK_INT >= 19) {
            AppOpsManager _manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            try {
                Class<?>[] types = new Class[]{int.class, int.class, String.class};
                Object[] args = new Object[]{permissionCode, Binder.getCallingUid(), context.getPackageName()};
                Method method = _manager.getClass().getDeclaredMethod("noteOp", types);
                method.setAccessible(true);
                Object _o = method.invoke(_manager, args);
                Log.i(TAG, "反射权限小米3会等于1么:= " + _o);
                if ((_o instanceof Integer)) {
                    checkPermission = (Integer) _o;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            checkPermission = 0;
        }
        return checkPermission;
    }

//    private PermissionDialog _dialog;

    //权限6.0以上弹窗
    public void showPermissionDialog23(final Context context, String per, String content) {
        /*String per1 = String.format(context.getString(R.string.permission), content, content);
        if (_dialog == null) {
            _dialog = new PermissionDialog(context, per + per1);
        }
        _dialog.setOnPositiveListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _dialog.dismiss();
                _dialog = null;

            }
        });
        _dialog.setOnNegativeListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _dialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                intent.setData(uri);
                context.startActivity(intent);
                _dialog = null;
            }
        });
        _dialog.setCanceledOnTouchOutside(false);
        _dialog.show();*/
    }

    //权限6.0以下弹窗
//    private ProDialog4Yes proDialog;

    public void showPermissionDialog(final Context context, String per, String content) {
        /*String per1 = String.format(context.getString(R.string.permission), content, content);
        String per2 = context.getString(R.string.low_permission, content);
        String per3 = context.getString(R.string.uninstall_per);
        ProDialog4Yes dialog = new ProDialog4Yes.Builder(context)
                .setMessage(per + per1 + per2 + per3)
                .setOkStr(context.getString(R.string.i_know))
                .build();
        dialog.show();*/
    }
}
