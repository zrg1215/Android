package com.hp.choosephoto.app;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by 95 on 2016/11/28.
 */
public class AppConfigsInfo {

    private static AppConfigsInfo _instance = new AppConfigsInfo();

    public AppConfigsInfo() {
    }

    public static AppConfigsInfo getInstance() {
        return _instance;
    }

    public void initConfig() {
        cleanAllImages();
    }


    //region 选择照片时，选择全部照片预览时，图片数量较大，传值会报ANR

    private List<String> mAllImages;

    public void initAllImages() {
        if (mAllImages == null) {
            mAllImages = new ArrayList<>();
        } else {
            mAllImages.clear();
        }
    }

    public void setAllImages(List<String> allImages) {
        if (allImages != null) {
            initAllImages();
            mAllImages.addAll(allImages);
        }
    }

    public List<String> getAllImages() {
        return mAllImages;
    }

    /**
     * 只需要在AlbumActivity onDestory()执行，不需要在GalleryActiv执行
     */
    public void cleanAllImages() {
        if (mAllImages != null) {
            mAllImages.clear();
            mAllImages = null;
        }
    }

    //endregion

}
