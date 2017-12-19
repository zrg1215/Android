package com.hp.secletimage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.hp.choosephoto.AlbumActivity;
import com.hp.choosephoto.GalleryActivity;
import com.hp.choosephoto.base.BaseActivity;
import com.hp.choosephoto.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements
        ImageAdapter.CustomClickListener,
        BaseActivity.IGetImagePathListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int MAX_IMAGES = 9;

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;

    private ImageAdapter mAdapter;
    private List<String> mImagePaths;

    private boolean mIsDeleteImage;//选中照片、预览（可删除）

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setIGetImagePathListener(this);

        mImagePaths = new ArrayList<>();

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("仿微信选择照片");
        setSupportActionBar(mToolbar);

        GridLayoutManager manager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new ImageAdapter(this, mImagePaths);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getIGetImagePathListener() instanceof MainActivity) {
            setIGetImagePathListener(null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_choose_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_choose) {
            startSelectPhoto();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected boolean isShowBackArrow() {
        return false;
    }

    @Override
    public void getImagePathListener(List<String> path) {
        if (path != null) {
            if (mIsDeleteImage && mImagePaths.size() > 0) {
                if (mImagePaths.size() == path.size()) {
                    return;
                }
                //清空所有图片，兵清理本地缓存，兵删除本地压缩图片
                mImagePaths.clear();
            }
            mImagePaths.addAll(path);
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    //region Adapter回调监听
    @Override
    public void OnFootClickListener() {
        startSelectPhoto();
    }

    @Override
    public void OnItemClickListener(int position) {
        mIsDeleteImage = true;
        Intent intent = new Intent(this, GalleryActivity.class);
        intent.putExtra(GalleryActivity.EXTRA_TYPE, GalleryActivity.PREVIEW_PUBLISH);
        intent.putExtra(GalleryActivity.EXTRA_INDEX, position);
        intent.putStringArrayListExtra(GalleryActivity.EXTRA_IMAGE_URLS, (ArrayList<String>) mImagePaths);
        intent.putStringArrayListExtra(GalleryActivity.EXTRA_IMAGE_SELECTED_URLS, (ArrayList<String>) mImagePaths);
        startActivityForResult(intent, BaseActivity.CHOOSE_MULTIPLE_PHOTO_CODE);
    }

    @Override
    public void OnAddFailure() {
        new ToastUtils(this).showToastByStr(getString(R.string.not_found_photo));
    }
    //endregion


    private void startSelectPhoto() {
        mIsDeleteImage = false;
        if (mImagePaths != null && mImagePaths.size() < MAX_IMAGES) {
            AlbumActivity.startAlbum(MainActivity.this, MAX_IMAGES - mImagePaths.size());
        } else {
            new ToastUtils(this).showToastByStr(getString(R.string.select_image_max_numbers, MAX_IMAGES + ""));
        }
    }
}
