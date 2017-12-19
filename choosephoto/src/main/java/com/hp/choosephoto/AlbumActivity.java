package com.hp.choosephoto;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hp.choosephoto.adapter.AlbumAdapter;
import com.hp.choosephoto.app.AppConfigsInfo;
import com.hp.choosephoto.base.BaseActivity;
import com.hp.choosephoto.finals.FileContants;
import com.hp.choosephoto.model.ImageFloder;
import com.hp.choosephoto.model.ImageInfo;
import com.hp.choosephoto.popwindow.ImageDirsPopupWindow;
import com.hp.choosephoto.utils.FileUtil;
import com.hp.choosephoto.utils.MeasureUtils;
import com.hp.choosephoto.utils.ProgressDialogLoading;
import com.hp.choosephoto.utils.ToastUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zrg on 2017/3/9.
 * 最多图片的 展示
 */
public class AlbumActivity extends BaseActivity implements AlbumAdapter.CustomOnClickListener,
        ImageDirsPopupWindow.IPopuWindowListener,
        View.OnClickListener {
    private static final String TAG = AlbumActivity.class.getSimpleName();

    public final static String EXTRA_MAX_IMAGE_NUM = "extra_max_image_num";
    public final static String EXTRA_IS_CLOSE = "extra_is_close";//是否关闭此页面，由于是三级页面转跳，3到2不需要2关闭
    //从预览界面回来，有两种情况：预览点击返回(不关闭AlbumActivity)、预览点击确定(需要关闭AlbumActivity)
    public final static int REQUEST_CODE_ALBUM = 111;
    private final int MAX_LOAD_IMAGE_NUMBER = 30;//每次加载

    //region 控件定义
    Toolbar mToolbar;
    RecyclerView mRecyclerView;
    TextView mTvCheckAll;
    TextView mTvPreview;
    RelativeLayout mRelaPopup;
    //endregion

    // PopopWindow 选定 的 图片文件夹
    private File mImgDir;
    //PopopWindow 选定 的 图片文件夹 名称
    private String mImgName;
    //目录下的所有的图片，按照最近排序
    private List<String> mDirAllImages;
    //所有的图片，按照最近排序
    private List<String> mAllImages;
    //扫描的第一张图片，展示全部图片需要
    private String mFirstPicture;
    //临时的辅助类，用于防止同一个文件夹的多次扫描
    private HashSet<String> mDirPaths = new HashSet<>();
    //扫描拿到所有的图片文件夹
    private List<ImageFloder> mImageFloders = new ArrayList<>();
    //扫描图片的总时长
    private long mScanTime;
    //图片的总张数
    private int totalCount = 0;
    //用户选择的图片，存储为图片的完整路径
    private List<String> mSelectedImages;
    private AlbumAdapter mAdapter;
    private ImageDirsPopupWindow mPopupWindow;
    //选择照片的数量，startAlbum()传值，默认1
    private int mMaxImageNum;

    private MenuItem mMenuItem;

    //region 选中图片后压缩图片相关
    boolean mIsFirstLoad = true;//是否是第一次加载数据，如果是，预先加载 一定数量的照片
    boolean mIsRunning = false;//是否正在压缩图片,如果是，就不让开启子线程
    private ExecutorService executorService = Executors.newFixedThreadPool(3);//加载图片，点击确定压缩图片
    //endregion

    //region 生命周期
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_images_activity_album);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mTvCheckAll = (TextView) findViewById(R.id.tv_check_all);
        mTvPreview = (TextView) findViewById(R.id.tv_preview);
        mRelaPopup = (RelativeLayout) findViewById(R.id.rela_popup);

        mTvCheckAll.setOnClickListener(this);
        mTvPreview.setOnClickListener(this);

        mToolbar.setTitle(getString(R.string.select_image_all_image));
        setSupportActionBar(mToolbar);
        mMaxImageNum = getIntent().getIntExtra(EXTRA_MAX_IMAGE_NUM, 1);
        mSelectedImages = new ArrayList<>();
        mDirAllImages = new ArrayList<>();
        mAllImages = new ArrayList<>();
        AppConfigsInfo.getInstance().initAllImages();

        initView();
        initListDirPopupWindow();
        getImages();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();//任务执行完毕，关闭线程池
        }
        AppConfigsInfo.getInstance().cleanAllImages();
    }

    //endregion

    //region 菜单

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_confirm, menu);
        mMenuItem = menu.findItem(R.id.action_confirm);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_confirm) {
            if (mSelectedImages.size() == 0) {
                new ToastUtils(this).showToastByID(R.string.select_image_no_one);
                return true;
            }
            compressImageFromFile(mSelectedImages);
        }
        return super.onOptionsItemSelected(item);
    }
    //endregion

    //region 点击事件处理，onActivityResult
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.tv_check_all) {
            showPopupWindow();
        } else if (id == R.id.tv_preview) {
            if (mSelectedImages.size() > 0) {
                startGallertActivity(GalleryActivity.PREVIEW_SELECT, 0, mSelectedImages, mSelectedImages, mMaxImageNum);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CHOOSE_TAKE_PICTURE_CODE) {//拍照
                File f = new File(FileContants.FilePathTmp, mLocalTempImageFileName);
                if (!TextUtils.isEmpty(f.getAbsolutePath())) {
                    mImagePath = f.getAbsolutePath();
                } else if (!TextUtils.isEmpty(f.getPath())) {
                    mImagePath = f.getPath();
                } else {
                    new ToastUtils(this).showToastByStr(getString(R.string.not_found_photo));
                }
                if (!TextUtils.isEmpty(mImagePath)) {
                    if (mSelectedImages != null) {
                        mSelectedImages.add(mImagePath);
                        compressImageFromFile(mSelectedImages);
                    }
                }
            } else if (requestCode == REQUEST_CODE_ALBUM) {
                if (data != null) {
                    ArrayList<String> extra = data.getStringArrayListExtra(EXTRA_CHOOSE_PHONE);
                    boolean isClose = data.getBooleanExtra(EXTRA_IS_CLOSE, false);
                    if (isClose) {
                        setResult(RESULT_OK, data);
                        finish();
                    } else {
                        //更新UI
                        if (mSelectedImages != null) {
                            mSelectedImages.clear();
                        }
                        mSelectedImages.addAll(extra);
                        showPreview();
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }
    //endregion

    //region Adapter 回调监听
    @Override
    public void OnTakePictureListener() {
        if (!isSlectImageLimit()) {
            checkCamarePermission();
        }
    }

    @Override
    public void OnBtnClickListener(String imagePath, ImageView itemImage, ImageView itemSelect) {
        if (mSelectedImages.contains(imagePath)) {
            itemSelect.setSelected(false);
            itemImage.setColorFilter(null);
            mSelectedImages.remove(imagePath);
            showPreview();
        } else {
            if (!isSlectImageLimit()) {
                itemSelect.setSelected(true);
                itemImage.setColorFilter(Color.parseColor("#77000000"));
                mSelectedImages.add(imagePath);
                showPreview();
            }
        }
    }

    @Override
    public void OnImgClickListener(int position) {
        //考虑到用户照片很多，传值List<String>过大，故保存在单例中
        AppConfigsInfo.getInstance().setAllImages(mDirAllImages);
        startGallertActivity(GalleryActivity.PREVIEW_SELECT, position, null, mSelectedImages, mMaxImageNum);
    }
    //endregion

    //region PopupWindow回调监听
    @Override
    public void OnClickListener(int position) {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
        if (mImageFloders == null || mImageFloders.size() < position) return;

        for (int i = 0; i < mImageFloders.size(); i++) {
            mImageFloders.get(i).flag = false;
        }

        final ImageFloder imageFloder = mImageFloders.get(position);
        if (imageFloder != null) {
            imageFloder.flag = true;
            mImgName = imageFloder.getName();
            mToolbar.setTitle(mImgName);
            if (position == 0) {
                //全部图片目录
                mDirAllImages.clear();
                mDirAllImages.addAll(mAllImages);
                mAdapter.notifyDataSetChanged();
            } else {
                //正常图片目录--使用线程进行图片筛选和排序
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mImgDir = new File(imageFloder.getDir());
                            File[] files = mImgDir.listFiles(new FileFilter() {
                                public boolean accept(File file) {
                                    String filename = file.getName().toLowerCase();
                                    if (filename.endsWith(".jpg")
                                            || filename.endsWith(".png")
                                            || filename.endsWith(".jpeg")) {
                                        return true;
                                    }
                                    return false;
                                }
                            });
                            final List<ImageInfo> imageInfos = new ArrayList<>();

                            for (int i = 0; i < files.length; i++) {
                                File file = files[i];
                                ImageInfo imageInfo = new ImageInfo();
                                imageInfo.setName(file.getName());
                                imageInfo.setPath(file.getPath());
                                imageInfo.setLastModified(file.lastModified());
                                imageInfos.add(imageInfo);
                            }

                            Collections.sort(imageInfos, new FileComparator());

                            getHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    mDirAllImages.clear();
                                    for (int i = 0; i < imageInfos.size(); i++) {
                                        mDirAllImages.add(i, imageInfos.get(i).getPath());
                                    }
                                    mAdapter.notifyDataSetChanged();
                                }
                            });

                        } catch (Exception e) {
                            Log.e(TAG, "run: OnClickListener", e);
                        }
                    }
                });
            }

        }
    }
    //endregion

    //region 私有方法
    private void initView() {
        mAdapter = new AlbumAdapter(this, mDirAllImages, mSelectedImages);
        mAdapter.setListener(this);
        GridLayoutManager manager = new GridLayoutManager(this, 4);
        //设置Item增加、移除动画
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);
    }

    //获取相册列表

    /**
     * 扫描本地照片
     */
    private void getImages() {
        if (!FileUtil.checkSDCardExist()) {
            new ToastUtils(this).showToastByID(R.string.select_image_sdcard_error);
            return;
        }

        ProgressDialogLoading.createDialog(this, true)
                .showMessage(getString(R.string.progress_dialog_loading))
                .show();

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Cursor mCursor = null;
                try {
                    long currentTimeMillis = System.currentTimeMillis();

                    // 只查询jpeg和png的图片
                    ContentResolver mContentResolver = getContentResolver();
                    Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    String[] projection = {MediaStore.Images.Media._ID,
                            MediaStore.Images.Media.DATA,
                            MediaStore.Images.Media.SIZE};//跟下面查询有关
                    String selection = MediaStore.Images.Media.MIME_TYPE + "=? OR " + MediaStore.Images.Media.MIME_TYPE + "=?";
                    String[] selectionArgs = new String[]{"image/jpeg", "image/png"};
                    String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " desc";// 按图片ID降序排列

                    mCursor = mContentResolver.query(mImageUri, projection, selection, selectionArgs, sortOrder);

                    while (mCursor.moveToNext()) {
                        if (isFinishingActivity()) {
                            break;
                        }

                        // 获取图片的路径
                        String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));

                        if (TextUtils.isEmpty(path)) {
                            continue;
                        }

                        //过滤掉已被删除的文件
                        File file = new File(path);
                        if (!file.exists()) {
                            continue;
                        }

                        // 获取 文件 的 大小
                        long fileSize = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Images.Media.SIZE));
                        //过滤掉 小于1Kb 的文件
                        if (fileSize < 1024) {
                            continue;
                        }

                        if (path.toLowerCase().contains("http:") || path.toLowerCase().contains("https:")) {
                            continue;
                        }

                        if (path.toLowerCase().contains(".9.png")) {
                            continue;
                        }

                        mDirAllImages.add(path);
                        mAllImages.add(path);//把所有图片单独添加一个list

                        if (mIsFirstLoad && mDirAllImages.size() > MAX_LOAD_IMAGE_NUMBER) {
                            mIsFirstLoad = false;
                            showImages();
                        }

                        // 拿到第一张图片的路径
                        if (mFirstPicture == null) {
                            mFirstPicture = path;
                            ImageFloder imageFloder = new ImageFloder();
                            imageFloder.setDir("/" + getString(R.string.select_image_all_image));
                            imageFloder.setFirstImagePath(mFirstPicture);
                            mImageFloders.add(0, imageFloder);
                        }
                        // 获取该图片的父路径名
                        File parentFile = new File(path).getParentFile();
                        if (parentFile == null) {
                            continue;
                        }
                        String dirPath = parentFile.getAbsolutePath();
                        ImageFloder imageFloder = null;

                        // 利用一个HashSet防止多次扫描同一个文件夹（不加这个判断，图片多起来还是相当恐怖的~~）
                        if (mDirPaths.contains(dirPath.toLowerCase())) {
                            continue;
                        } else {
                            mDirPaths.add(dirPath.toLowerCase());
                            // 初始化imageFloder
                            imageFloder = new ImageFloder();
                            imageFloder.setDir(dirPath);
                            imageFloder.setFirstImagePath(path);
                        }
                        String[] parentFileImgs = parentFile.list(new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String filename) {
                                if (TextUtils.isEmpty(filename)) return false;

                                filename = filename.toLowerCase();
                                if (filename.endsWith(".jpg")
                                        || filename.endsWith(".png")
                                        || filename.endsWith(".jpeg"))
                                    return true;
                                return false;
                            }
                        });
                        if (parentFileImgs == null || parentFileImgs.length == 0) {
                            //文件 已被删除,防止造成空指针异常
                            continue;
                        }
                        int picSize = parentFileImgs.length;
                        totalCount += picSize;

                        imageFloder.setCount(picSize);
                        mImageFloders.add(imageFloder);
                    }
                    long currentTimeMillis1 = System.currentTimeMillis();
                    mScanTime = currentTimeMillis1 - currentTimeMillis;

                    // 扫描完成，辅助的HashSet也就可以释放内存了
                    mDirPaths = null;

                    // 通知Handler扫描图片完成
                    scanFinish();
                } catch (Exception e) {
                    Log.e(TAG, "getImages: ", e);
                    ProgressDialogLoading.dismissDialog();
                } finally {
                    if (mCursor != null) {
                        mCursor.close();
                    }
                }
            }
        });

    }

    /**
     * 显示图片
     */
    private void showImages() {
        if (isFinishing()) return;
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                ProgressDialogLoading.dismissDialog();
                mAdapter.notifyDataSetChanged();
            }
        });

    }

    /**
     * 扫描完成
     */
    private void scanFinish() {
        if (isFinishingActivity()) return;
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                ProgressDialogLoading.dismissDialog();
                mAdapter.notifyDataSetChanged();
                new ToastUtils(AlbumActivity.this).showToastByStrForTest("扫描完成，一共" + totalCount + "张图片,总耗时" + mScanTime + "毫秒");
            }
        });
    }

    private void initListDirPopupWindow() {
        int[] screen = MeasureUtils.measureScreen(this);
        mPopupWindow = new ImageDirsPopupWindow(this, screen[0], (int) (screen[1] * 0.8), mImageFloders, this);
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                // 设置背景颜色变暗
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1.0f;
                getWindow().setAttributes(lp);
            }
        });
    }

    private void showPopupWindow() {
        if (mPopupWindow == null) {
            return;
        }
        mPopupWindow.show(mRelaPopup);
        // 设置背景颜色变暗
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 0.3f;
        getWindow().setAttributes(params);
    }

    /**
     * 显示预览的状态
     */
    private void showPreview() {
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
     * 选择照片限制
     */
    private boolean isSlectImageLimit() {
        if (mSelectedImages.size() >= mMaxImageNum) {
            if (mMaxImageNum > 1) {
                new ToastUtils(this).showToastByStr(getString(R.string.select_image_max_numbers, mMaxImageNum + ""));
            } else {
                new ToastUtils(this).showToastByStr(getString(R.string.select_image_max_number, mMaxImageNum + ""));
            }
            return true;
        }
        return false;
    }

    /**
     * 压缩照片
     *
     * @param selectedImages 被压缩的照片
     */
    private void compressImageFromFile(final List<String> selectedImages) {
        if (selectedImages != null && selectedImages.size() > 0) {
            if (mIsRunning) {
                return;
            }

            ProgressDialogLoading.createDialog(this, true)
                    .showMessage(getString(R.string.progress_dialog_loading))
                    .show();

            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        mIsRunning = true;
                        final ArrayList<String> images = new ArrayList<>();
                        for (int i = 0; i < selectedImages.size(); i++) {
                            String imagePath = selectedImages.get(i);
                            Bitmap bitmap = FileUtil.compressImageFromFile(imagePath);
                            String tagetpath = FileContants.FilePathTmp + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss",
                                    Locale.getDefault()).format(new Date()) + "_" + i;//需要添加后缀区分，不然压缩太快，名称相同了
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
                                setResultActivity(images);
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "run: ", e);
                    }

                }
            });
        } else {
            setResultActivity(selectedImages);
        }
    }

    /**
     * 返回到上一级，并将 压缩后 的List 集合传值
     *
     * @param images 压缩后 的List
     */
    private void setResultActivity(List<String> images) {
        Intent iiResult = new Intent();
        iiResult.putStringArrayListExtra(BaseActivity.EXTRA_CHOOSE_PHONE, (ArrayList<String>) images);
        setResult(RESULT_OK, iiResult);
        finish();
    }

    /**
     * 启动到预览页面
     *
     * @param type         类型，本类为 GalleryActivity.PREVIEW_SELECT
     * @param index        预览图片的坐标
     * @param images       要预览的所有图片--如果为null，传值太大，保存在AppConfigInfo中
     * @param selectImages 要预览的已经被选择的图片
     * @param maxImageNum  图片最大选择数量
     */
    private void startGallertActivity(String type,
                                      int index,
                                      List<String> images,
                                      List<String> selectImages,
                                      int maxImageNum) {
        Intent intent = new Intent(this, GalleryActivity.class);
        intent.putExtra(GalleryActivity.EXTRA_TYPE, type);
        intent.putExtra(GalleryActivity.EXTRA_INDEX, index);
        intent.putStringArrayListExtra(GalleryActivity.EXTRA_IMAGE_URLS, (ArrayList<String>) images);
        intent.putStringArrayListExtra(GalleryActivity.EXTRA_IMAGE_SELECTED_URLS, (ArrayList<String>) selectImages);
        intent.putExtra(EXTRA_MAX_IMAGE_NUM, maxImageNum);
        startActivityForResult(intent, REQUEST_CODE_ALBUM);
    }

    //endregion

    //region 拍照相关
    //拍照临时存放的文件夹
    private String mLocalTempImageFileName;
    //拍照或选择相册照片的路径
    protected String mImagePath;

    //region 拍照处理
    protected void startCamare() {
        if (FileUtil.checkSDCardExist()) {
            try {
                mLocalTempImageFileName = String.valueOf((new Date()).getTime()) + ".jpg";
                File filePath = new File(FileContants.FilePathTmp);
                if (!filePath.exists()) {
                    filePath.mkdirs();
                }
                File f = new File(filePath, mLocalTempImageFileName);
                Uri u = Uri.fromFile(f);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.Images.ImageColumns.ORIENTATION, 0);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, u);
                startActivityForResult(intent, CHOOSE_TAKE_PICTURE_CODE);
            } catch (Exception e) {
                Log.e(TAG, "startCamare: ", e);
            }
        }
    }

    //endregion

    //endregion

    //region 文件排序
    class FileComparator implements Comparator<ImageInfo> {

        @Override
        public int compare(ImageInfo o1, ImageInfo o2) {
            if (o1.getLastModified() > o2.getLastModified()) {
                return -1;
            }
            return 1;
        }
    }

    //endregion

    /**
     * 跳转到该页面的方法
     * @param activity 从哪跳转的页面
     * @param maxImageNum 选招照片的最大数量
     */
    public static void startAlbum(Activity activity, int maxImageNum) {
        Intent intent = new Intent(activity, AlbumActivity.class);
        intent.putExtra(AlbumActivity.EXTRA_MAX_IMAGE_NUM, maxImageNum);
        activity.startActivityForResult(intent, CHOOSE_MULTIPLE_PHOTO_CODE);
    }
}
