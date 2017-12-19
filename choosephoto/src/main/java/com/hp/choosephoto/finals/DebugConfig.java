package com.hp.choosephoto.finals;


/**
 * 发布版本配置
 */
public class DebugConfig {
    //true为测试环境，false正式环境
    public static final boolean ServerFinal_debug = false;
    public static final boolean FileContants_debug = false;

    // 标识是否显示测试 Toast.
    private static short Toast_IsDebug = -1;

    public static boolean getToast_IsDebug() {

        return true;
    }

    public static void setToast_IsDebug(boolean isChecked) {


    }

}
