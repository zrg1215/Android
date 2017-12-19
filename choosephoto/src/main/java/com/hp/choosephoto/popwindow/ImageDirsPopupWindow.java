package com.hp.choosephoto.popwindow;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hp.choosephoto.R;
import com.hp.choosephoto.model.ImageFloder;

import java.util.List;

/**
 * Created by zrg on 2017/3/9.
 */

public class ImageDirsPopupWindow extends PopupWindow {
    private RecyclerView mRecyclerView;
    public MyAdapter mAdapter;

    public ImageDirsPopupWindow(Context context, int width, int height,
                                List<ImageFloder> mData,
                                IPopuWindowListener listener) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.from(context).inflate(R.layout.popup_image_dirs, null);
        setContentView(view);

        setWidth(width);
        setHeight(height);

        //设置能否获取到焦点
        setFocusable(true);
        //设置PopupWindow进入和退出时的动画效果
        setAnimationStyle(R.style.ActionSheetDialogAnimation);
        setTouchable(true); // 默认是true，设置为false，所有touch事件无响应，而被PopupWindow覆盖的Activity部分会响应点击
        // 设置弹窗外可点击,此时点击PopupWindow外的范围，Popupwindow不会消失
        setOutsideTouchable(true);
        //外部是否可以点击，设置Drawable原因可以参考：http://blog.csdn.net/harvic880925/article/details/49278705
        setBackgroundDrawable(new BitmapDrawable());
        // 设置弹窗的布局界面

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        //设置Item增加、移除动画
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mAdapter = new MyAdapter(context, mData, listener);
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * 显示弹窗列表界面
     */
    public void show(View view) {
        showAtLocation(view, Gravity.BOTTOM, 0, 0);
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    class MyAdapter extends RecyclerView.Adapter {
        private Context mContext;
        private List<ImageFloder> mData;
        private IPopuWindowListener mListener;

        public MyAdapter(Context context, List<ImageFloder> data, IPopuWindowListener listener) {
            mContext = context;
            mData = data;
            mListener = listener;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.popup_item_image_dirs, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof ItemViewHolder) {
                ImageFloder imageFloder = mData.get(position);
                if (imageFloder != null) {
                    ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
                    Glide.with(mContext)
                            .load("file://" + imageFloder.getFirstImagePath())
                            .placeholder(R.drawable.default_empty)
                            .error(R.drawable.default_empty)
                            .into(itemViewHolder.mImgDirImage);
                    itemViewHolder.mTvDirName.setText(imageFloder.getName());
                    if (position != 0) {
                        itemViewHolder.mTvDirCount.setVisibility(View.VISIBLE);
                        itemViewHolder.mTvDirCount.setText(imageFloder.getCount() + "");
                    } else {
                        itemViewHolder.mTvDirCount.setVisibility(View.GONE);
                    }

                    if (imageFloder.flag) {
                        itemViewHolder.mImgDirSelected.setVisibility(View.VISIBLE);
                    } else {
                        itemViewHolder.mImgDirSelected.setVisibility(View.GONE);
                    }

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mListener != null) {
                                mListener.OnClickListener(position);
                            }
                        }
                    });
                }
            }
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {
            ImageView mImgDirImage;
            TextView mTvDirName;
            TextView mTvDirCount;
            ImageView mImgDirSelected;

            ItemViewHolder(View view) {
                super(view);
                mImgDirImage = (ImageView) view.findViewById(R.id.img_dir_image);
                mTvDirName = (TextView) view.findViewById(R.id.tv_dir_name);
                mTvDirCount = (TextView) view.findViewById(R.id.tv_dir_count);
                mImgDirSelected = (ImageView) view.findViewById(R.id.img_dir_selected);
            }
        }
    }

    /**
     * @param
     * @author ldm
     * @description 点击事件回调处理接口
     * @time 2016/7/29 15:30
     */
    public interface IPopuWindowListener {
        void OnClickListener(int position);
    }
}
