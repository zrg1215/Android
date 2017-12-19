package com.hp.choosephoto.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.hp.choosephoto.R;

import java.util.List;

/**
 * Created by zrg on 2017/3/9.
 */

public class AlbumAdapter extends RecyclerView.Adapter {
    private static final String TAG = "AlbumAdapter";
    private static final int TYPE_EMPTY = 0x110;
    private static final int TYPE_IMAGE = 0x111;
    private Context mContext;
    private List<String> mImages;
    private List<String> mSelectImages;

    private CustomOnClickListener mListener;

    public AlbumAdapter(Context context, List<String> images, List<String> selectImages) {
        mContext = context;
        mImages = images;
        mSelectImages = selectImages;
    }

    public void setListener(CustomOnClickListener listener) {
        mListener = listener;
    }

    @Override
    public int getItemCount() {
        return mImages == null ? 1 : mImages.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_EMPTY;
        }
        return TYPE_IMAGE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_EMPTY) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.select_images_item_empty_album, parent, false);
            return new ItemEmptyViewHolder(view);
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.select_images_item_album, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ItemEmptyViewHolder) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.OnTakePictureListener();
                    }
                }
            });
        } else if (holder instanceof ItemViewHolder) {
            final String imagePath = mImages.get(position - 1);

            Glide.with(mContext)
                    .load("file://" + imagePath)
                    .placeholder(R.drawable.default_empty)
                    .error(R.drawable.default_empty)
                    .into(((ItemViewHolder) holder).mItemImage);

            if (mSelectImages != null && mSelectImages.contains(imagePath)) {
                ((ItemViewHolder) holder).mItemSelect.setSelected(true);
                ((ItemViewHolder) holder).mItemImage.setColorFilter(Color.parseColor("#77000000"));
            } else {
                ((ItemViewHolder) holder).mItemSelect.setSelected(false);
                ((ItemViewHolder) holder).mItemImage.setColorFilter(null);
            }

            ((ItemViewHolder) holder).mItemImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.OnImgClickListener(position - 1);
                    }
                }
            });

            ((ItemViewHolder) holder).mItemSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.OnBtnClickListener(imagePath, ((ItemViewHolder) holder).mItemImage,
                                ((ItemViewHolder) holder).mItemSelect);
                    }
                }
            });
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView mItemImage;
        ImageView mItemSelect;

        ItemViewHolder(View view) {
            super(view);
            mItemImage = (ImageView) view.findViewById(R.id.id_item_image);
            mItemSelect = (ImageView) view.findViewById(R.id.id_item_select);
        }
    }

    class ItemEmptyViewHolder extends RecyclerView.ViewHolder {

        ImageView mItemImage;

        ItemEmptyViewHolder(View view) {
            super(view);
            mItemImage = (ImageView) view.findViewById(R.id.id_item_image);
        }
    }

    public interface CustomOnClickListener {
        void OnTakePictureListener();

        void OnBtnClickListener(String imagePath, ImageView itemImage, ImageView itemSelect);

        void OnImgClickListener(int position);

    }
}
