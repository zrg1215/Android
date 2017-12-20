package com.hp.choosephoto.base;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.hp.choosephoto.R;
import com.hp.choosephoto.utils.CheckPermissionUtils;
import com.hp.choosephoto.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * desc:BaseActivity
 * Author: Daniel
 * Date: 2017-02-23 11:10
 */
public class BaseActivity extends AppCompatActivity {
    Handler mHandler;
    Handler mThreadHandler;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setDisplayHomeAsUpEnabled(isShowBackArrow());
            getSupportActionBar().setDisplayShowTitleEnabled(isShowTitle());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 是否显示返回键,默认显示
     *
     * @return true表示显示，false表示隐藏
     */
    protected boolean isShowBackArrow() {
        return true;
    }

    /**
     * 是否显示标题,默认显示
     *
     * @return true表示显示，false表示隐藏
     */
    protected boolean isShowTitle() {
        return true;
    }

    public boolean isFinishingActivity() {
        return this.isFinishing();
    }

    //region 弹出拍照/选择照片对话框

    //选择相册--包括本地和拍照
    public static final int CHOOSE_MULTIPLE_PHOTO_CODE = 10;
    //拍照
    public static final int CHOOSE_TAKE_PICTURE_CODE = 12;
    //剪切图片--头像选择需要
    protected static final int REQUEST_CODE_CROP_IMAGE = 13;

    //选择照片的结果，返回的是一个list
    public static final String EXTRA_CHOOSE_PHONE = "extra_choose_phone";

    //是否需要剪切图片，如果需要，请重写此方法
    protected boolean isNeedCropImage() {
        return false;
    }

    //endregion

    //region 检查相机权限

    protected void checkCamarePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            //6.0以上使用系统API判断相机权限
            if (!CheckPermissionUtils.getInstance().checkPermission23(this, CheckPermissionUtils.CAMERA)) {
                //如果没有相机权限
                ActivityCompat.requestPermissions(this,
                        new String[]{CheckPermissionUtils.CAMERA},
                        CheckPermissionUtils.CAMERA_PERMISSION_CODE);
            } else {
                startCamare();
            }
        } else {
            //6.0以下相机暂不处理
            startCamare();
        }
    }

    protected void startCamare() {

    }


    //endregion

    //region 权限 返回结果 处理
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if(requestCode == CheckPermissionUtils.CAMERA_PERMISSION_CODE){
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamare();
            } else {
                new ToastUtils(this).showToastByStr("没有权限");
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    //endregion

    public Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler(getMainLooper());
        }
        return mHandler;
    }

    public Handler getThreadHandler() {
        if (mThreadHandler == null) {
            mThreadHandler = new Handler(getWorkLooper());
        }
        return mThreadHandler;
    }

    private Looper getWorkLooper(){
        HandlerThread handlerThread = new HandlerThread("choose_photo_thread");
        handlerThread.start();
        return handlerThread.getLooper();
    }
}
