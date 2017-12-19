package com.hp.choosephoto.utils;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by zrg on 2017/3/18.
 */

public class ProgressDialogLoading extends ProgressDialog {
    private static ProgressDialogLoading _instance;

    private ProgressDialogLoading(Context context) {
        super(context);
    }

    private ProgressDialogLoading(Context context, boolean... isCancelable) {
        super(context);
        this.setIndeterminate(true);
        if (isCancelable != null && isCancelable.length > 0) {
            this.setCancelable(isCancelable[0]);
        } else {
            this.setCancelable(false);
        }
    }


    public static ProgressDialogLoading createDialog(Context context, boolean... isCancelable) {
        if (_instance == null) {
            _instance = new ProgressDialogLoading(context, isCancelable);
        }
        return _instance;
    }

    public ProgressDialogLoading showMessage(String message) {
        if (_instance != null) {
            _instance.setMessage(message);
        }
        return _instance;
    }

    /**
     */
    public static void cancelDialog() {
        if (_instance != null && _instance.isShowing()) {
            _instance.setCanceledOnTouchOutside(true);
        } else {
            _instance = null;
        }
    }

    public static void dismissDialog() {
        if (_instance != null && _instance.isShowing()) {
            _instance.dismiss();
            _instance = null;
        } else {
            _instance = null;
        }
    }
}
