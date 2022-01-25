package com.hxty.schoolnet.ui.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hxty.schoolnet.R;
import com.hxty.schoolnet.entity.Program;
import com.hxty.schoolnet.utils.DensityUtil;

import java.util.List;


/**
 * Created by chen on 2017/3/28.
 */

public class TuiGuangAdapter extends RecyclerView.Adapter<TuiGuangAdapter.ViewHolder> implements View.OnClickListener {

    private LayoutInflater mInflater;
    private List<Program> mDatas;
    private Context mConxtext;

    public TuiGuangAdapter(Context context, List<Program> datats) {
        mInflater = LayoutInflater.from(context);
        mDatas = datats;
        mConxtext = context;
    }

    private OnItemClickListener mOnItemClickListener = null;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View view) {
            super(view);
        }

        ImageView imageView;
        TextView title;
        ImageView newTv;
        View container;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v, (Integer) v.getTag());
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public int getItemViewType(int position) {
        if ((position + 1) % 5 == 0) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public TuiGuangAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = null;
        if (viewType == 0) {
            view = mInflater.inflate(R.layout.adapter_tuiguang, viewGroup, false);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = DensityUtil.dip2px(mConxtext, 334);
            layoutParams.height = DensityUtil.dip2px(mConxtext, 221);
            view.setLayoutParams(layoutParams);
        } else {
            view = mInflater.inflate(R.layout.adapter_tuiguang2, viewGroup, false);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = DensityUtil.dip2px(mConxtext, 490);
            layoutParams.height = DensityUtil.dip2px(mConxtext, 221);
            view.setLayoutParams(layoutParams);
        }
        TuiGuangAdapter.ViewHolder viewHolder = new TuiGuangAdapter.ViewHolder(view);

        viewHolder.container = view.findViewById(R.id.container);
        viewHolder.imageView =  view.findViewById(R.id.item_iv);
        viewHolder.title =  view.findViewById(R.id.item_tv);
        viewHolder.newTv =  view.findViewById(R.id.newTv);
        view.setOnClickListener(this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final TuiGuangAdapter.ViewHolder viewHolder, final int position) {
        viewHolder.title.setText(mDatas.get(position).getProgramTitle());
        if (mDatas.get(position).isNew()) {
            viewHolder.newTv.setVisibility(View.VISIBLE);
        } else {
            viewHolder.newTv.setVisibility(View.GONE);
        }
        Glide.with(mConxtext).load(mDatas.get(position).getImgUrl()).placeholder(R.drawable.default_img).into(viewHolder.imageView);
        viewHolder.container.setTag(position);
    }
}
