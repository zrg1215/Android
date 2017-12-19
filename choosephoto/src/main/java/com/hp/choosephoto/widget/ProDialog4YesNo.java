package com.hp.choosephoto.widget;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.hp.choosephoto.R;


/**
 * create by zrg 2017.9.25
 * 如果有需求dialog点击后不消失，可以设定一个Boolean参数放在Builder中，让外界传值
 * update by zrg 2017.9.29 点击事件不需要加双击判断，否则会出现问题
 */
public class ProDialog4YesNo extends Dialog {
    private Builder mBuilder;

    private ProDialog4YesNo(Builder builder) {
        this(builder, R.style.FullHeightDialog);
    }

    private ProDialog4YesNo(Builder builder, int theme) {
        super(builder.mContext, theme);
        mBuilder = builder;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.customprogressdialog4);

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams p = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);
        // lp

        WindowManager m = (WindowManager) mBuilder.mContext.getSystemService(Context.WINDOW_SERVICE);
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        //获取对话框当前的参数值
        p.height = LayoutParams.WRAP_CONTENT;
        p.width = (int) (d.getWidth() * 0.8); // 宽度设置为屏幕的0.8
        dialogWindow.setAttributes(p);

        setCancelable(mBuilder.mCancelTouchOut);

        TextView tvOk = (TextView) findViewById(R.id.tv_ok);
        TextView tvCancel = (TextView) findViewById(R.id.tv_cancle);
        TextView tvMessage = (TextView) findViewById(R.id.tv_dialog_info);

        if (!TextUtils.isEmpty(mBuilder.mOkStr)) {
            tvOk.setText(mBuilder.mOkStr);
        }

        if (!TextUtils.isEmpty(mBuilder.mCancelStr)) {
            tvCancel.setText(mBuilder.mCancelStr);
        }

        if (!TextUtils.isEmpty(mBuilder.mMessage)) {
            tvMessage.setText(mBuilder.mMessage);
        }

        tvMessage.setGravity(mBuilder.mMessageGravity);

        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mBuilder.mListener != null) {
                    mBuilder.mListener.doConfirm();
                }
            }
        });
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mBuilder.mListener != null) {
                    mBuilder.mListener.doCancle();
                }
            }
        });
    }

    public static class Builder {
        private Context mContext;
        private String mOkStr;
        private String mCancelStr;
        private String mMessage;
        private int mMessageGravity = Gravity.CENTER;
        private boolean mCancelTouchOut;
        private ClickListenerInterface mListener;

        public Builder(Context context) {
            mContext = context;
        }

        public Builder setOkStr(String okStr) {
            mOkStr = okStr;
            return this;
        }

        public Builder setCancelStr(String cancelStr) {
            mCancelStr = cancelStr;
            return this;
        }

        public Builder setMessage(String message) {
            mMessage = message;
            return this;
        }

        public Builder setMessageGravity(int messageGravity) {
            mMessageGravity = messageGravity;
            return this;
        }

        public Builder setCancelTouchOut(boolean cancelTouchOut) {
            mCancelTouchOut = cancelTouchOut;
            return this;
        }

        public Builder setListener(ClickListenerInterface listener) {
            mListener = listener;
            return this;
        }

        public ProDialog4YesNo build() {
            return new ProDialog4YesNo(this);
        }
    }

    public interface ClickListenerInterface {
        void doConfirm();

        void doCancle();
    }
}