package com.hp.choosephoto.finals;


/**
 * 发布版本配置
 * <p>
 * shengwang
 * 2017.05.31.15.50: version 1.10.1
 * <p>
 * rongyun
 * 2017.05.31.15.38: version 2.8.12
 * 2017.08.28.09.55: version 2.8.16
 * <p>
 * baidu dingwei
 * 2017.05.31.15.38: version 7.1
 * <p>
 * jpush:
 * jpush:2017-05-08 version v3.0.6
 * jcore:2017-05-08 version v1.1.3
 * <p>
 * otto
 * 已经停止更新
 * <p>
 * sharesdk
 * 2017.06.08.14.30
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
