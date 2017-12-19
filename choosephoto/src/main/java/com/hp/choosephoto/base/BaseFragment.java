package com.hp.choosephoto.base;

import android.app.Activity;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;


/**
 * desc:BaseFragment
 * Author: Daniel
 * Date: 2017-02-23 11:12
 */
public class BaseFragment extends Fragment {


    @Override
    public void onDestroy() {
        super.onDestroy();
        //TODO HDL 测试
       /* RefWatcher refWatcher = WDApp.getRefWatcher(getActivity());
        refWatcher.watch(this);*/
    }

    //region Toast
    protected Toast tipsToast;

    @SuppressWarnings("ResourceType")
    public void showToastByID(int msgResId, int... duration) {
        if (tipsToast != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                tipsToast.cancel();
            }
        } else {
            int _duration = (duration == null || duration.length == 0) ? 1000
                    : duration[0];
            tipsToast = Toast.makeText(getActivity().getBaseContext(),
                    msgResId, _duration);
        }
        tipsToast.show();
        tipsToast.setText(msgResId);
    }

    @SuppressWarnings("ResourceType")
    public void showToastByStr(String msg, int... duration) {
        if (tipsToast != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                tipsToast.cancel();
            }
        } else {
            int _duration = (duration == null || duration.length == 0) ? 1000
                    : duration[0];
            tipsToast = Toast.makeText(getActivity().getBaseContext(),
                    msg, _duration);
        }
        tipsToast.show();
        tipsToast.setText(msg);
    }

    @SuppressWarnings("ResourceType")
    public void showToastByStrForTest(String msg, int... duration) {
        if (tipsToast != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                tipsToast.cancel();
            }
        } else {
            int _duration = (duration == null || duration.length == 0) ? 1000
                    : duration[0];
            tipsToast = Toast.makeText(getActivity().getBaseContext(),
                    msg, _duration);
        }
        tipsToast.show();
        tipsToast.setText(msg);

    }
    //endregion

    protected boolean checkActivityAttached() {
        if (getActivity() != null && !getActivity().isFinishing() && isAdded()) {
            return true;
        }
        return false;
    }

    /**
     * 隐藏软键盘输入
     */
    public void hideSoftInputView() {
        if (checkActivityAttached()) {
            InputMethodManager manager = ((InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE));
            if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
                if (getActivity().getCurrentFocus() != null && manager != null) {
                    manager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        }

    }

}
