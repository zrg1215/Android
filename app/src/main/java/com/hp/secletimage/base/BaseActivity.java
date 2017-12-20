package com.hp.secletimage.base;

import android.content.Intent;

import com.hp.choosephoto.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zrg on 2017/12/20.
 *
 */

public class BaseActivity extends com.hp.choosephoto.base.BaseActivity {

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CHOOSE_MULTIPLE_PHOTO_CODE://选择照片完成后，传递给需要用的页面
                    if (data != null) {
                        final ArrayList<String> imagesPath = data.getStringArrayListExtra(EXTRA_CHOOSE_PHONE);
                        if (imagesPath != null) {
                            if (iGetImagePathListener != null) {
                                iGetImagePathListener.getImagePathListener(imagesPath);
                            }
                        }
                    } else {
                        new ToastUtils(this).showToastByStr(getString(com.hp.choosephoto.R.string.not_found_photo));
                    }
                    break;
                case REQUEST_CODE_CROP_IMAGE://剪切图片
                    if (data != null) {
                        if (iGetImagePathListener != null) {
                            //iGetImagePathListener.getImagePathListener(data.getStringExtra(IconCropImageActivity.EXTRA_IMAGE_PATH));
                        }
                    } else {
                        new ToastUtils(this).showToastByStr(getString(com.hp.choosephoto.R.string.not_found_photo));
                    }
                    break;
            }
        }
    }

    //region 得到图片的路径的监听
    //实现接口 要在destory中销毁
    private IGetImagePathListener iGetImagePathListener;

    protected void setIGetImagePathListener(IGetImagePathListener listener) {
        this.iGetImagePathListener = listener;
    }

    public IGetImagePathListener getIGetImagePathListener() {
        return this.iGetImagePathListener;
    }

    public interface IGetImagePathListener {

        /**
         * 选择照片和拍照 返回的图片路径
         */
        void getImagePathListener(List<String> path);
    }
    //endregion
}
