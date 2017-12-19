package com.hp.choosephoto.finals;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileContants {

    //SD卡目录
    public final static String SD = getSDPath();

    //缓存地址目录
    public final static String TEMP = SD + "/" + "com.hp.choosephoto";

    /**
     * 获取sd卡的路径
     *
     * @return 路径的字符串
     */
    public static String getSDPath() {
        File sdDir = null;
        //判断sd卡是否存在
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取外存目录
        }
        if (sdDir == null) {
            return "";
        }
        return sdDir.getPath();
    }

    private static String filePathBasic = null;

    public static String getFilePathBasic() {
        if (DebugConfig.FileContants_debug) {
            //debug环境popon SD卡基础地址
            if (TextUtils.isEmpty(filePathBasic)) {
                filePathBasic = TEMP + "/debug00/";
            }
        } else {
            //正式环境popon SD卡基础地址
            if (TextUtils.isEmpty(filePathBasic)) {
                filePathBasic = TEMP + "/release/";
            }
        }
        return filePathBasic;
    }


    //region 可以被清理缓存
    // 临时文件地址
    public static final String FilePathTmp = getFilePathBasic() + "tmp/";

    //图片文件地址
    public static final String FilePathPic = getFilePathBasic() + "img/";

    //日志文件地址
    public final static String LOG_PATH = getFilePathBasic() + "log/";

    //网络请求缓存目录
    public final static String HTTPCACHE = FileContants.getFilePathBasic() + "/httpCache";
    //endregion

    //下载文件保存目录
    public final static String DOWNLOAD = FileContants.getFilePathBasic() + "/download";

    //下载新版本
    public final static String DOWNLOAD_NEW_APK = DOWNLOAD + "/popon_update.apk";

    //登录用户自己的文件存储地址
    public static String getLoginUserFilePath(String loingUserId) {
        return getFilePathBasic() + "login/" + loingUserId + "/";
    }

    //图片下载文件地址
    public static final String FilePathDownloadPic = getFilePathBasic() + "popon/";

    //音频文件地址
    public static final String FilePathVoice = getFilePathBasic() + "voice/";

    //视频文件地址
    public static final String FilePathVideo = getFilePathBasic() + "video/";

    //服务音频文件地址
    public static final String FilePathVoiceAgora = FilePathVoice + "agora/";

    //用户头像文件地址
    public static final String FilePathUHeader = getFilePathBasic() + "user/head/";

    //临时图片缓存地址目录--不需要了，为了清理老版本的缓存
    public final static String TEMP_IMAGE_FILE = getFilePathBasic() + "imgtemp/";

    //崩溃日志文件地址
    public final static String LOG_CRASH_PATH = getFilePathBasic() + "crash/";

    //日志文件命名
    public final static String LOG = LOG_PATH + "log_" + getCurrentTime() + ".txt";

    //刷新token接口 日志文件地址
    public final static String LOG_REFRESH_TOKEN_PATH = LOG_PATH + "refreshtoken/";

    //刷新token接口 日志文件命名
    public final static String LOG_REFRESH_TOKEN_NAME = LOG_REFRESH_TOKEN_PATH + "log_" + getCurrentTime() + ".log";

    //声网文件地址
    public static final String LOG_AGORA_PATH = getFilePathBasic() + "agora/";

    //语音服务流转文件地址
    public static final String LOG_AGORA_SVR_PATH = getFilePathBasic() + "agora_svr/";

    //服务接单（B端收到）日志路径
    public static final String LOG_RONG_BILL_PATH = getFilePathBasic() + "rong_bill/";

    //ASR 语音识别，音频路径
    public static final String FILEPATH_ASR_VOICE = getFilePathBasic() + "asr/";

    //服务接单（B端收到）日志的文件名
    public static String getLogRongBillPathName(String loingUserId) {
        return loingUserId + FileContants.LAST_NAME_LOG;
    }

    public static String getCurrentTime() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh");
        return sDateFormat.format(new Date(System.currentTimeMillis()));
    }

    // qi niu文件域名
    public final static String SVR_QI_NIU_CDN = "http://7xk8ky.com1.z0.glb.clouddn.com/";

    public final static String LAST_NAME_LOG = ".log";
    public final static String LAST_NAME_TXT = ".txt";
}
