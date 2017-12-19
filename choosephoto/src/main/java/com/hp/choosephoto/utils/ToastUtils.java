package com.hp.choosephoto.utils;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import com.hp.choosephoto.finals.DebugConfig;


/**
 * desc:ToastUtils
 * Author: zrg
 * Date: 2017-02-17 09:41
 */
public class ToastUtils {
    private Context mContext;
    Toast mToast;

    public ToastUtils(Context context) {
        mContext = context;
    }

    @SuppressWarnings("ResourceType")
    public void showToastByID(int msgResId, int... duration) {
        if (mToast != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mToast.cancel();
            }
        } else {
            int _duration = (duration == null || duration.length == 0) ? 1000
                    : duration[0];
            mToast = mToast.makeText(mContext, msgResId, _duration);
        }
        mToast.show();
        mToast.setText(msgResId);
    }

    @SuppressWarnings("ResourceType")
    public void showToastByStr(String msg, int... duration) {
        if (mToast != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mToast.cancel();
            }
        } else {
            int _duration = (duration == null || duration.length == 0) ? 1000
                    : duration[0];
            mToast = mToast.makeText(mContext, msg, _duration);
        }
        mToast.show();
        mToast.setText(msg);
    }

    @SuppressWarnings("ResourceType")
    public void showToastByStrForTest(String msg, int... duration) {
        if (DebugConfig.getToast_IsDebug()) {
            if (mToast != null) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    mToast.cancel();
                }
            } else {
                int _duration = (duration == null || duration.length == 0) ? 1000
                        : duration[0];
                mToast = mToast.makeText(mContext, msg, _duration);
            }
            mToast.show();
            mToast.setText(msg);
        }
    }

}