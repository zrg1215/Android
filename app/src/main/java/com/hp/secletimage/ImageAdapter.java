package com.hp.secletimage;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * desc:ImageAdapter
 * Author: zrg
 * Date: 2017-03-07 14:03
 */
public class ImageAdapter extends RecyclerView.Adapter {
    private final int TYPR_FOOT = -1;

    private Context mContext;
    private List<String> mImages;
    private CustomClickListener mListener;

    public ImageAdapter(Context context, List<String> images) {
        mContext = context;
        mImages = images;
    }

    public void setListener(CustomClickListener listener) {
        mListener = listener;
    }


    @Override
    public int getItemCount() {
        if (mImages == null || mImages.size() == 0) {
            return 0;
        }
        if (mImages.size() < MainActivity.MAX_IMAGES) {
            return mImages.size() + 1;
        }
        return mImages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mImages.size() < MainActivity.MAX_IMAGES && (position == getItemCount() - 1)) {
            return TYPR_FOOT;
        }
        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPR_FOOT) {
            View view = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_publish_image, parent, false);
            return new FootViewHolder(view);
        }
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.item_publish_image, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ItemViewHolder) {
            setImage(mImages.get(position), ((ItemViewHolder) holder).mImageShow);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.OnItemClickListener(position);
                    }
                }
            });
        } else if (holder instanceof FootViewHolder) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.OnFootClickListener();
                    }
                }
            });
        }
    }

    /**
     * 设置图片
     *
     * @param path 图片路径
     */
    private void setImage(String path, ImageView imageView) {
        if (!TextUtils.isEmpty(path)) {
            Glide.with(mContext)
                    .load("file://" + path)
                    .placeholder(com.hp.choosephoto.R.drawable.default_empty)
                    .error(com.hp.choosephoto.R.drawable.default_empty)
                    .into(imageView);
        } else {
            if (mListener != null) {
                mListener.OnAddFailure();
            }
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView mImageShow;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mImageShow = (ImageView) itemView.findViewById(R.id.img_show);
        }
    }

    class FootViewHolder extends RecyclerView.ViewHolder {
        public FootViewHolder(View itemView) {
            super(itemView);
        }
    }

    interface CustomClickListener {
        void OnFootClickListener();

        void OnItemClickListener(int position);

        void OnAddFailure();
    }

}