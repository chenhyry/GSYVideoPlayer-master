package com.hxty.schoolnet.ui.adapter;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hxty.schoolnet.R;
import com.hxty.schoolnet.entity.Program;

import java.util.List;


/**
 * Created by chen on 2017/3/28.
 */

public class MainSrcAdapter extends RecyclerView.Adapter<MainSrcAdapter.ViewHolder> implements View.OnClickListener {

    private LayoutInflater mInflater;
    private List<Program> mDatas;

    public MainSrcAdapter(Context context, List<Program> datats) {
        mInflater = LayoutInflater.from(context);
        mDatas = datats;
    }

    private OnItemClickListener mOnItemClickListener = null;

    public class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View view) {
            super(view);
            title =  view.findViewById(R.id.item_tv);
            container = view.findViewById(R.id.container);
        }

        TextView title;
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
    public MainSrcAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = mInflater.inflate(R.layout.adapter_main_scr_name, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setOnClickListener(this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final MainSrcAdapter.ViewHolder viewHolder, final int position) {
        viewHolder.title.setText(mDatas.get(position).getProgramTitle());
        viewHolder.container.setTag(position);
    }
}
