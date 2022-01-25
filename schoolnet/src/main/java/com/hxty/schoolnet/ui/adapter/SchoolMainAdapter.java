package com.hxty.schoolnet.ui.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.hxty.schoolnet.R;
import com.hxty.schoolnet.entity.FirstLevelMenu;
import com.hxty.schoolnet.net.Constants;

import java.util.List;

public class SchoolMainAdapter extends RecyclerView.Adapter<SchoolMainAdapter.DiscoveryView> implements View.OnClickListener {

    private List<FirstLevelMenu> datas;
    private Context mContext;
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;
    private StaggeredGridLayoutManager layoutManager;

    public SchoolMainAdapter(Context mContext, List<FirstLevelMenu> datas, StaggeredGridLayoutManager layoutManager) {
        this.datas = datas;
        this.mContext = mContext;
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }


    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v, (int) v.getTag());
        }
    }

    @Override
    public DiscoveryView onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_school_main, viewGroup, false);
        view.setOnClickListener(this);
        return new DiscoveryView(view);
    }

    @Override
    public void onBindViewHolder(DiscoveryView discoveryView, int position) {

        Glide.with(mContext).load(Constants.IMAGE_URL + datas.get(position).getImgUrl1()).transform(new CenterCrop(mContext)).into(discoveryView.imageView);
        discoveryView.container.setTag(position);
    }

    @Override
    public int getItemCount() {
        return datas == null ? 0 : datas.size();
    }

    public static class DiscoveryView extends RecyclerView.ViewHolder {
        ImageView imageView;
        View container;

        public DiscoveryView(View itemView) {
            super(itemView);
            imageView =  itemView.findViewById(R.id.imageView);
            container = itemView.findViewById(R.id.container);
        }
    }

    /**
     * 设置瀑布流布局中的某个item，独占一行、占一列、占两列、等等
     *
     * @param mStaggeredGridLayoutManager
     * @param position                    目标item所在的位置
     * @param TARGET_ITEM_TYPE            目标item的条目类型
     * @param parentView                  该item的整个布局
     */
    private void setStaggeredItemSpanCount(StaggeredGridLayoutManager mStaggeredGridLayoutManager, int position, int TARGET_ITEM_TYPE, View parentView) {
        int type = getItemViewType(position);
        if (type == TARGET_ITEM_TYPE) {
            StaggeredGridLayoutManager.LayoutParams layoutParams =
                    new StaggeredGridLayoutManager.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setFullSpan(true);
            parentView.setLayoutParams(layoutParams);
        }
    }
}