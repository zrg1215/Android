package com.hp.choosephoto;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.hp.choosephoto.photoview.PhotoViewAttacher;

public class ImageDetailFragment extends Fragment {
    private static final String TAG = ImageDetailFragment.class.getSimpleName();
    private static final String ARG_URL = "img_url";
    private static final String ARG_IS_CLOSE = "arg_is_close";

    private ImageView mImg;
    private ProgressBar mProgressBar;

    private String mImageUrl;
    private boolean mIsClose;
    private PhotoViewAttacher mAttacher;

    /**
     * @param imageUrl 图片的URI
     * @param isClose  点击图片是否关闭页面
     */
    public static ImageDetailFragment newInstance(String imageUrl, boolean isClose) {
        final ImageDetailFragment f = new ImageDetailFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_URL, imageUrl);
        args.putBoolean(ARG_IS_CLOSE, isClose);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mImageUrl = getArguments().getString(ARG_URL);
            mIsClose = getArguments().getBoolean(ARG_IS_CLOSE);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_image_pager, container, false);
        mImg = (ImageView) v.findViewById(R.id.image);
        mProgressBar = (ProgressBar) v.findViewById(R.id.loading);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mProgressBar.setVisibility(View.VISIBLE);

        mAttacher = new PhotoViewAttacher(mImg);

        //setOnViewTapListener 整个PhotoViewAttacher的点击事件处理
        // setOnPhotoTapListener显示图片的点击事件处理
        mAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                if (!mIsClose && mHideOrShowToolbar != null) {
                    mHideOrShowToolbar.iHideOrShowToolbar();
                    return;
                }
                if (mIsClose && getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        //加载本地或者图片
        if (!TextUtils.isEmpty(mImageUrl)) {
            if (mImageUrl.startsWith("http") || mImageUrl.startsWith("https")) {
                showNetPicture(mImageUrl);
            } else {
                showNetPicture("file://" + mImageUrl);
            }
        } else {
            mImg.setImageResource(R.drawable.default_empty);
        }
    }

    /**
     * 加载 显示图片
     *
     * @param imageUrl 图片的url
     */
    private void showNetPicture(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.default_empty)
                .error(R.drawable.default_empty)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        mProgressBar.setVisibility(View.GONE);
                        Log.e(TAG, "onException() returned: ", e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        mProgressBar.setVisibility(View.GONE);
                        mImg.setImageDrawable(resource);
                        mAttacher.update();
                        return false;
                    }
                })
                .into(mImg);
    }

    interface IHideOrShowToolbar {
        void iHideOrShowToolbar();
    }

    private IHideOrShowToolbar mHideOrShowToolbar;

    public void setHideOrShowToolbar(IHideOrShowToolbar hideOrShowToolbar) {
        mHideOrShowToolbar = hideOrShowToolbar;
    }
}
