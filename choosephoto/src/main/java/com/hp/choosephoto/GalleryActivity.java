package com.hp.choosephoto;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hp.choosephoto.app.AppConfigsInfo;
import com.hp.choosephoto.base.BaseActivity;
import com.hp.choosephoto.finals.FileContants;
import com.hp.choosephoto.utils.FileUtil;
import com.hp.choosephoto.utils.ProgressDialogLoading;
import com.hp.choosephoto.utils.ToastUtils;
import com.hp.choosephoto.widget.HackyViewPager;
import com.hp.choosephoto.widget.ProDialog4YesNo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by zrg on 2017/3/9.
 * 选择图片 预览
 */
public class GalleryActivity extends BaseActivity implements ViewPager.OnPageChangeListener,
        ImageDetailFragment.IHideOrShowToolbar,
        View.OnClickListener {
    private static final String TAG = "GalleryActivity";

    public static final String PREVIEW_SELECT = "preview_select";//选择 照片 预览--可以选中/取消选中
    public static final String PREVIEW_PUBLISH = "preview_publish";//发布 照片 预览--可以删除

    /**
     * PREVIEW_SELECT----选择 照片 预览--可以选中/取消选中
     * PREVIEW_PUBLISH----发布 照片 预览--可以删除
     */
    public static final String EXTRA_TYPE = "extra_type";
    /**
     * 要展示的照片的位置
     */
    public static final String EXTRA_INDEX = "extra_index";
    /**
     * 被选中的图片
     */
    public static final String EXTRA_IMAGE_SELECTED_URLS = "extra_image_selected_urls";
    /**
     * 需要展示的所有图片
     */
    public static final String EXTRA_IMAGE_URLS = "extra_image_urls";

    //region 控件定义
    Toolbar mToolbar;
    TextView mTvChoose;
    ImageView mImgSelect;
    RelativeLayout mRelaPopup;
    HackyViewPager mViewpager;
    //endregion

    //类型
    private String mType;
    //图片位置
    private int mIndex;
    //选择图片的最大数量
    private int mMaxImageNum;
    //需要展示的所有图片
    private List<String> mImagesPath;
    //预览 被选中的图片
    private List<String> mSelectedImagesPath;
    //最终 被选中的图片
    private List<String> mSelectedImages;
    //当前展示的图片位置
    private int mCurrentIndex;
    private ImagePagerAdapter mAdapter;
    private MenuItem mMenuItem;

    //是否正在压缩图片,如果是，就不让开启子线程
    boolean mIsRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_images_activity_gallery);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTvChoose = (TextView) findViewById(R.id.tv_choose);
        mImgSelect = (ImageView) findViewById(R.id.img_select);
        mRelaPopup = (RelativeLayout) findViewById(R.id.rela_popup);
        mViewpager = (HackyViewPager) findViewById(R.id.viewpager);

        mImgSelect.setOnClickListener(this);

        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);

        mType = getIntent().getStringExtra(EXTRA_TYPE);
        mIndex = getIntent().getIntExtra(EXTRA_INDEX, 0);
        mMaxImageNum = getIntent().getIntExtra(AlbumActivity.EXTRA_MAX_IMAGE_NUM, 1);
        mImagesPath = new ArrayList<>();
        mSelectedImagesPath = new ArrayList<>();
        mSelectedImages = new ArrayList<>();
        mCurrentIndex = mIndex;

        if (TextUtils.equals(PREVIEW_SELECT, mType)) {
            mRelaPopup.setVisibility(View.GONE);
            mImgSelect.setVisibility(View.VISIBLE);
            mImagesPath = getIntent().getStringArrayListExtra(EXTRA_IMAGE_URLS);
            mSelectedImagesPath = getIntent().getStringArrayListExtra(EXTRA_IMAGE_SELECTED_URLS);
            if (mSelectedImagesPath != null) {
                mSelectedImages.addAll(mSelectedImagesPath);
            }
            //初始化选择的图片
            if (mImagesPath == null) {
                //如果为空，会存在本地，不能进行传值
                mImagesPath = AppConfigsInfo.getInstance().getAllImages();
            }
            if (mImagesPath != null && mImagesPath.size() > 0) {
                //文件夹所有 图片 预览
                mToolbar.setTitle((mCurrentIndex + 1) + "/" + mImagesPath.size());
                if (mSelectedImages != null && mSelectedImages.size() > 0) {
                    if (mSelectedImages.contains(mImagesPath.get(mCurrentIndex))) {
                        setPictrueState(true);
                    } else {
                        setPictrueState(false);
                    }
                } else {
                    setPictrueState(false);
                }
            }
        } else if (TextUtils.equals(PREVIEW_PUBLISH, mType)) {
            mRelaPopup.setVisibility(View.GONE);
            mImgSelect.setVisibility(View.GONE);
            mImagesPath = getIntent().getStringArrayListExtra(EXTRA_IMAGE_URLS);
            mSelectedImagesPath = getIntent().getStringArrayListExtra(EXTRA_IMAGE_SELECTED_URLS);
            if (mSelectedImagesPath != null) {
                mSelectedImages.addAll(mSelectedImagesPath);
            }
            if (mImagesPath != null && mImagesPath.size() > 0) {
                mToolbar.setTitle((mCurrentIndex + 1) + "/" + mImagesPath.size());
            }
        }

        mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), mImagesPath);
        mAdapter.setHideOrShowToolbar(this);
        mViewpager.setAdapter(mAdapter);
        mViewpager.setCurrentItem(mIndex);
        mViewpager.addOnPageChangeListener(this);
    }

    //region 菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (TextUtils.equals(PREVIEW_SELECT, mType)) {
            getMenuInflater().inflate(R.menu.menu_confirm, menu);
            mMenuItem = menu.findItem(R.id.action_confirm);
            if (mSelectedImages != null && mSelectedImages.size() > 0) {
                mMenuItem.setTitle(getString(R.string.select_image_preview_number,
                        "" + mSelectedImages.size(), "" + mMaxImageNum));
            }
        } else if (TextUtils.equals(PREVIEW_PUBLISH, mType)) {
            getMenuInflater().inflate(R.menu.menu_delete, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            onBackPressed();
        }else if(id == R.id.action_confirm){
            if (mSelectedImages.size() == 0) {
                new ToastUtils(this).showToastByID(R.string.select_image_no_one);
                return true;
            }
            setResultImages(true);
        }else if(id == R.id.action_delete){
            showYesNoDialog();
        }
        return super.onOptionsItemSelected(item);
    }
    //endregion

    @Override
    public void onBackPressed() {
        if (TextUtils.equals(PREVIEW_SELECT, mType)) {
            //选择图片预览 返回上一个页面
            setResultImages(false);
            return;
        } else if (TextUtils.equals(PREVIEW_PUBLISH, mType)) {
            //发布动态预览图片 --返回发布动态
            sendImages();
            return;
        }
        super.onBackPressed();
    }

    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.img_select){
            setImageChoose();
        }
    }

    //region addOnPageChangeListener监听
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mCurrentIndex = position;
        if (mImagesPath != null && mImagesPath.size() > 0) {
            mToolbar.setTitle((mCurrentIndex + 1) + "/" + mImagesPath.size());
            if (mSelectedImages.contains(mImagesPath.get(position))) {
                setPictrueState(true);
            } else {
                setPictrueState(false);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
    //endregion

    //region 私有方法
    /**
     * 选择的点击事件
     */
    private void setImageChoose() {
        if (!TextUtils.equals(PREVIEW_SELECT, mType)) return;
        if (mImagesPath == null || mImagesPath.size() == 0) {
            return;
        }
        if (mSelectedImages.contains(mImagesPath.get(mCurrentIndex))) {
            //如果已经选择，再次点击取消选择
            setPictrueState(false);
            mSelectedImages.remove(mImagesPath.get(mCurrentIndex));
        } else {
            if (mSelectedImages.size() >= mMaxImageNum) {
                if (mMaxImageNum > 1) {
                    new ToastUtils(this).showToastByStr(getString(R.string.select_image_max_numbers, mMaxImageNum + ""));
                } else {
                    new ToastUtils(this).showToastByStr(getString(R.string.select_image_max_number, mMaxImageNum + ""));
                }
                return;
            }
            setPictrueState(true);
            mSelectedImages.add(mImagesPath.get(mCurrentIndex));
        }
        if (mMenuItem != null) {
            if (mSelectedImages != null && mSelectedImages.size() > 0) {
                mMenuItem.setTitle(getString(R.string.select_image_preview_number,
                        "" + mSelectedImages.size(), "" + mMaxImageNum));
            } else {
                mMenuItem.setTitle(getString(R.string.dialog_confirm));
            }
        }
    }

    /**
     * @param isSelect ture:选中状态
     *                 false:未选中状态
     */
    private void setPictrueState(boolean isSelect) {
        if (isSelect) {
            mImgSelect.setImageResource(R.drawable.picture_selected);
        } else {
            mImgSelect.setImageResource(R.drawable.picture_unselected);
        }
    }

    private void showYesNoDialog() {
        ProDialog4YesNo dialog = new ProDialog4YesNo.Builder(this)
                .setMessage(getString(R.string.select_image_delete_image))
                .setOkStr(getString(R.string.dialog_confirm))
                .setCancelStr(getString(R.string.dialog_cancel))
                .setListener(new ProDialog4YesNo.ClickListenerInterface() {
                    @Override
                    public void doConfirm() {
                        //删除，更新adapter
                        deleteImage();
                    }

                    @Override
                    public void doCancle() {

                    }
                })
                .build();
        dialog.show();
    }

    /**
     * 删除照片
     */
    private void deleteImage() {
        if (mImagesPath != null && mImagesPath.size() > mCurrentIndex) {
            if (mImagesPath.size() > 1) {
                String imagePath = mImagesPath.get(mCurrentIndex);
                mImagesPath.remove(imagePath);
                mSelectedImages.remove(imagePath);
                //删除本地图片
                FileUtil.delFile(imagePath);
                if (mCurrentIndex == mSelectedImages.size()) {
                    //删除的是末尾
                    mToolbar.setTitle((mCurrentIndex) + "/" + mImagesPath.size());
                } else {
                    //删除的不是是末尾
                    mToolbar.setTitle((mCurrentIndex + 1) + "/" + mImagesPath.size());
                }
                //刷新Viewpager
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
            } else {
                mSelectedImages.clear();
                sendImages();
            }
        }
    }

    /**
     * 发送图片
     *
     * @param isCloseAlbum 是否关闭 AlbumActivity 页面,true:关闭，false 不关闭
     *                     可以当做type  = 0理解
     */
    private void setResultImages(boolean isCloseAlbum) {
        if (isCloseAlbum) {
            compressImageFromFile(mSelectedImages);
        } else {
            setResultActivity(mSelectedImages, false);
        }
    }

    /**
     * 发布动态 预览选择的图片，点击返回，直接传值回去，并且finish
     * 可以当做type  = 3理解
     */
    private void sendImages() {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(BaseActivity.EXTRA_CHOOSE_PHONE, (ArrayList<String>) mSelectedImages);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * 压缩图片
     */
    private void compressImageFromFile(final List<String> selectedImages) {
        if (selectedImages != null && selectedImages.size() > 0) {
            if (mIsRunning) {
                return;
            }
            ProgressDialogLoading.createDialog(this, true)
                    .showMessage(getString(R.string.progress_dialog_loading))
                    .show();


            getThreadHandler().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mIsRunning = true;
                        final ArrayList<String> images = new ArrayList<>();
                        for (int i = 0; i < selectedImages.size(); i++) {
                            String imagePath = selectedImages.get(i);
                            Bitmap bitmap = FileUtil.compressImageFromFile(imagePath);
                            String tagetpath = FileContants.FilePathTmp + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss",
                                    Locale.getDefault()).format(new Date()) + "_" + i;//需要添加后缀，不然压缩太快，名称相同了
                            if (bitmap != null) {
                                FileUtil.saveBitmapToSD(tagetpath, bitmap);
                                imagePath = tagetpath;
                                images.add(imagePath);
                            }
                        }

                        getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                if (isFinishingActivity()) return;
                                ProgressDialogLoading.dismissDialog();
                                setResultActivity(images, true);
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "run: ", e);
                    }
                }
            });
        } else {
            setResultActivity(selectedImages, true);
        }
    }

    /**
     * 返回到上一级，并将 压缩后 的List 集合传值
     *
     * @param images 压缩后 的List
     */
    private void setResultActivity(List<String> images, boolean isCloseAlbum) {
        Intent iiResult = new Intent();
        iiResult.putStringArrayListExtra(BaseActivity.EXTRA_CHOOSE_PHONE, (ArrayList<String>) images);
        iiResult.putExtra(AlbumActivity.EXTRA_IS_CLOSE, isCloseAlbum);
        setResult(RESULT_OK, iiResult);
        finish();
    }

    //endregion

    //region 隐藏Toolbar
    private boolean mIsHidden;

    @Override
    public void iHideOrShowToolbar() {
        mToolbar.animate()
                .translationY(mIsHidden ? 0 : -mToolbar.getHeight())
                .setInterpolator(new DecelerateInterpolator(2))
                .start();
        mIsHidden = !mIsHidden;
    }
    //endregion

    //region Gallery 的Adapter
    private class ImagePagerAdapter extends FragmentStatePagerAdapter {
        private List<String> mImagesUrl;
        private ImageDetailFragment.IHideOrShowToolbar mHideOrShowToolbar;

        public void setHideOrShowToolbar(ImageDetailFragment.IHideOrShowToolbar hideOrShowToolbar) {
            mHideOrShowToolbar = hideOrShowToolbar;
        }

        public ImagePagerAdapter(FragmentManager fm, List<String> imagesUrl) {
            super(fm);
            mImagesUrl = imagesUrl;
        }

        @Override
        public int getCount() {
            return mImagesUrl == null ? 0 : mImagesUrl.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            ImageDetailFragment imageDetailFragment = ImageDetailFragment.newInstance(mImagesUrl.get(position), false);
            imageDetailFragment.setHideOrShowToolbar(mHideOrShowToolbar);
            return imageDetailFragment;
        }
    }
    //endregion
}
