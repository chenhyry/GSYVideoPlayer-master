package com.hxty.schoolnet.ui.fragment;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hxty.schoolnet.R;
import com.hxty.schoolnet.ui.adapter.ProgramRightAdapter;
import com.hxty.schoolnet.entity.BaseResponse;
import com.hxty.schoolnet.entity.Program;
import com.hxty.schoolnet.entity.VideoPic;
import com.hxty.schoolnet.net.Constants;
import com.hxty.schoolnet.net.JsonCallback;
import com.hxty.schoolnet.net.RequestManager;
import com.hxty.schoolnet.ui.PlayVideoActivity;
import com.hxty.schoolnet.ui.WebViewActivity;
import com.hxty.schoolnet.utils.CommonUtil;
import com.hxty.schoolnet.utils.DensityUtil;
import com.lzy.okgo.model.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * 推荐
 */
public class ProgramFragment extends Fragment {

    private View view;
    private int index = -1;
    private FragmentActivity context;
    private List<Program> modelList;
    private RecyclerView programRecycleView;
    private ProgramRightAdapter programRightAdapter;
    private List<Program> programRightList = new ArrayList<>();
    private RelativeLayout firstRl;
    private ImageView bigImagView;
    private TextView bigTextView;
    private ImageView newTv;

    public static ProgramFragment newInstance(int index, ArrayList<Program> modelList) {
        ProgramFragment programFragment = new ProgramFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        bundle.putParcelableArrayList("model", modelList);
        programFragment.setArguments(bundle);
        return programFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (view == null) {
            context = getActivity();

            Bundle bundle = getArguments();
            index = bundle.getInt("index");
            modelList = bundle.getParcelableArrayList("model");

            List<Program> newModels;
            int last = Constants.INIT_PAGE_NUM * index + Constants.INIT_PAGE_NUM;
            if (last >= modelList.size()) {
                newModels = modelList.subList((Constants.INIT_PAGE_NUM * index), modelList.size());
            } else {
                newModels = modelList.subList((Constants.INIT_PAGE_NUM * index), last);
            }

//            Log.i("tag", "当前页数是" + index + "view是null");
            view = LayoutInflater.from(context).inflate(R.layout.fragment_program, container, false);

            firstRl = view.findViewById(R.id.firstRl);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) firstRl.getLayoutParams();
            layoutParams.width = DensityUtil.dip2px(context, 832);
            layoutParams.height = DensityUtil.dip2px(context, 438);
            firstRl.setLayoutParams(layoutParams);
            final Program program = newModels.get(0);

            firstRl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toDetailActivity(program);
                }
            });
            //设置第一个节目获取焦点
            if (index == 0) {
                firstRl.requestFocus();
            }

            bigImagView = view.findViewById(R.id.item_iv);
            bigTextView = view.findViewById(R.id.item_tv);
            newTv = view.findViewById(R.id.newTv);

            if (newModels.get(0).isNew()) {
                newTv.setVisibility(View.VISIBLE);
            } else {
                newTv.setVisibility(View.GONE);
            }

            Glide.with(context).load(newModels.get(0).getImgUrl()).placeholder(R.drawable.default_img).into(bigImagView);
            bigTextView.setText(newModels.get(0).getProgramTitle());
            programRightList.clear();
            for (int i = 1; i < newModels.size(); i++) {
                programRightList.add(newModels.get(i));
                // //设置是否是一周内新发布的
                newModels.get(i).setNew(CommonUtil.isInSevenDays(newModels.get(i).getReleaseTime()));
            }

            programRecycleView = view.findViewById(R.id.program_recycle_view);
            programRightAdapter = new ProgramRightAdapter(context, programRightList);
            programRecycleView.setAdapter(programRightAdapter);
            programRightAdapter.setOnItemClickListener(new ProgramRightAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    toDetailActivity(programRightList.get(position));
                }
            });
            final GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 3) {
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            };
            programRecycleView.setLayoutManager(gridLayoutManager);
            SpacesItemDecoration decoration = new SpacesItemDecoration(DensityUtil.dip2px(context, 14));
            programRecycleView.addItemDecoration(decoration);

        } else {
//            Log.i("tag", "当前页数是" + index + "view不是null");
            ViewGroup root = (ViewGroup) view.getParent();
            if (root != null) {
                root.removeView(view);
            }
        }

//        Log.i("tag", "当前页数是" + index);
        return view;
    }

    private void toDetailActivity(final Program program) {
        if (program.getProgramType() == 1) {
            RequestManager.getInstance().GetVideoPicture(new JsonCallback<BaseResponse<VideoPic>>() {
                @Override
                public void onSuccess(Response<BaseResponse<VideoPic>> response) {
                    VideoPic videoPic = response.body().handleResult;

                    Intent intent = new Intent(context, PlayVideoActivity.class);
                    intent.putExtra("KeyId", program.getKeyId());

                    intent.putExtra("videoUrl", program.getLinkUrl());
                    intent.putExtra("videoTitle", program.getProgramTitle());
                    intent.putExtra("videoThumb", program.getImgUrl());
                    if (videoPic != null) {
                        intent.putExtra("videoImgUrl", videoPic.getImgUrl());
                    }
                    startActivity(intent);
                }

                @Override
                public void onError(Response<BaseResponse<VideoPic>> response) {
                    super.onError(response);
                    Intent intent = new Intent(context, PlayVideoActivity.class);
                    intent.putExtra("KeyId", program.getKeyId());

                    intent.putExtra("videoUrl", program.getLinkUrl());
                    intent.putExtra("videoTitle", program.getProgramTitle());
                    intent.putExtra("videoThumb", program.getImgUrl());
                }
            });
        } else {
            if (TextUtils.isEmpty(program.getLinkUrl())) {
                Toast.makeText(context, "暂无详情页面数据", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(context, WebViewActivity.class)
                        .putExtra("linkUrl", program.getLinkUrl())
                        .putExtra("KeyId", program.getKeyId())
                );
            }
        }
    }

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {

        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;
        }
    }

}
