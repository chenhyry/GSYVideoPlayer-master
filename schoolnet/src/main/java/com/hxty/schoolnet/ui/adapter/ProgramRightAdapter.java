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

public class ProgramRightAdapter extends RecyclerView.Adapter<ProgramRightAdapter.ViewHolder> implements View.OnClickListener {

    private LayoutInflater mInflater;
    private List<Program> mDatas;
    private Context mConxtext;

    public ProgramRightAdapter(Context context, List<Program> datats) {
        mInflater = LayoutInflater.from(context);
        mDatas = datats;
        mConxtext = context;
    }

    private OnItemClickListener mOnItemClickListener = null;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View view) {
            super(view);
            imageView =  view.findViewById(R.id.item_iv);
            title =  view.findViewById(R.id.item_tv);
            newTv =  view.findViewById(R.id.newTv);
            container = view.findViewById(R.id.container);
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

    public boolean isHeader(int position) {
        return position == 0;
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public ProgramRightAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = mInflater.inflate(R.layout.adapter_program_right, viewGroup, false);
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
        layoutParams.width = DensityUtil.dip2px(mConxtext, 332);
        layoutParams.height = DensityUtil.dip2px(mConxtext, 212);
        view.setLayoutParams(layoutParams);
        ProgramRightAdapter.ViewHolder viewHolder = new ProgramRightAdapter.ViewHolder(view);
        view.setTag(i);
        view.setOnClickListener(this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ProgramRightAdapter.ViewHolder viewHolder, final int position) {
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
