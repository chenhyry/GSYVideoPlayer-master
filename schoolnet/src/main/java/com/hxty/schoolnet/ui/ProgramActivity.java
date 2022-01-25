package com.hxty.schoolnet.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.hxty.schoolnet.App;
import com.hxty.schoolnet.R;
import com.hxty.schoolnet.entity.BaseResponse;
import com.hxty.schoolnet.entity.Page;
import com.hxty.schoolnet.entity.Program;
import com.hxty.schoolnet.entity.VideoPic;
import com.hxty.schoolnet.net.Constants;
import com.hxty.schoolnet.net.JsonCallback;
import com.hxty.schoolnet.net.RequestManager;
import com.hxty.schoolnet.ui.adapter.SimplePageAdapter;
import com.hxty.schoolnet.ui.adapter.TuiGuangAdapter;
import com.hxty.schoolnet.ui.fragment.ProgramFragment;
import com.hxty.schoolnet.utils.CommonUtil;
import com.hxty.schoolnet.utils.DensityUtil;
import com.hxty.schoolnet.utils.sphelper.CommonValue;
import com.hxty.schoolnet.utils.sphelper.ConfigHelper;
import com.hxty.schoolnet.widget.AutoScrollTextView;
import com.lzy.okgo.model.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chen on 2017/3/28.
 */

public class ProgramActivity extends BaseActivity implements View.OnClickListener, AutoScrollTextView.onShowNextMsg, AutoScrollTextView.OnClickTextListener, /*SuperPlayer.OnNetChangeListener, SuperPlayer.OnCloseScreensaver,*/ Handler.Callback {

    private int pageIndex = 1;
    private int id;
    private String loginName;
    private String loginSchoolName;
    private String columnName;
    //推广
    private RecyclerView tuiGuangRecycleView;
    private TuiGuangAdapter tuiGuangAdapter;
    private List<Program> tuiGuangProgramList = new ArrayList<>();
    private TextView backTv;
    private LinearLayout backLl;

    private ViewPager viewPager;
    private List<Fragment> fragments;
    private SimplePageAdapter pageAdapter;
    private ArrayList<Program> modelList = new ArrayList<>();
    private int pageCount;

    private AutoScrollTextView autoScrollTextView;
    private TextView pre_page_tv;
    private TextView next_page_tv;

    private ImageView xiaoyuanlogoIv;
    private ImageView schooleLogoIv;
    private TextView school_name_tv;
    private ConfigHelper helper;

    //滚动新闻
    private List<Program> marqueeDatas = new ArrayList<>();
    private int lastMarqueeMsg = 0;

    private Handler handler;
    private static final int GET_NEW_DATA = 101;
    private static final int GET_NEW_DATA_TIME = 60 * 1000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program);

        initView();
        initData();
        hideNavigationBar();
    }

    private void initView() {
        xiaoyuanlogoIv = findViewById(R.id.xiaoyuanlogoIv);
        schooleLogoIv = findViewById(R.id.schooleLogoIv);
        school_name_tv = findViewById(R.id.school_name_tv);

        loginName = getIntent().getStringExtra("loginName");
        loginSchoolName = getIntent().getStringExtra("loginSchoolName");


        backTv = findViewById(R.id.back_tv);
        backLl = findViewById(R.id.back_ll);
        backLl.setOnClickListener(this);

        tuiGuangRecycleView = findViewById(R.id.tuiguang_recycleview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        tuiGuangRecycleView.setLayoutManager(layoutManager);
        SpacesItemDecoration decoration = new SpacesItemDecoration(DensityUtil.dip2px(this, 12));
        tuiGuangRecycleView.addItemDecoration(decoration);
        tuiGuangAdapter = new TuiGuangAdapter(this, tuiGuangProgramList);
        tuiGuangRecycleView.setAdapter(tuiGuangAdapter);
        tuiGuangAdapter.setOnItemClickListener(new TuiGuangAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                toDetailActivity(tuiGuangProgramList.get(position));
            }
        });

        pre_page_tv = findViewById(R.id.pre_page_tv);
        next_page_tv = findViewById(R.id.next_page_tv);
        pre_page_tv.setOnClickListener(this);
        next_page_tv.setOnClickListener(this);

        autoScrollTextView = findViewById(R.id.marquee_notice_tv);
        autoScrollTextView.init(getWindowManager());
        autoScrollTextView.setOnShowNextMsg(this);
        autoScrollTextView.setOnClickTextListener(this);

        viewPager = findViewById(R.id.viewpager);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) viewPager.getLayoutParams();
        layoutParams.height = DensityUtil.dip2px(this, 438);
        viewPager.setLayoutParams(layoutParams);
        fragments = new ArrayList<>();
        pageAdapter = new SimplePageAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(pageAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                if (position == pageCount - 1) {
                    // 在最后一页，开始加载后一页的数据
                    getProgram(false);
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }

    private void initData() {
        helper = ConfigHelper.getDefaultConfigHelper(this);

        loginName = getIntent().getStringExtra("loginName");

        id = getIntent().getIntExtra("id", -1);
        columnName = getIntent().getStringExtra("columnName");
        backTv.setText(columnName);

        if ("1".equals(App.SCHOOLTYPE) || "3".equals(App.SCHOOLTYPE)) {//学校  普通用户
            school_name_tv.setVisibility(View.GONE);
            schooleLogoIv.setVisibility(View.GONE);
        } else {
            xiaoyuanlogoIv.setVisibility(View.GONE);
            school_name_tv.setText(App.loginSchoolName);
            String imgUrl = helper.getString(CommonValue.LOGO_URL, CommonValue.USER_NULL_DEFAULT);
            if (!TextUtils.isEmpty(imgUrl)) {
                schooleLogoIv.setVisibility(View.VISIBLE);
                Glide.with(this).load(Constants.IMAGE_URL + imgUrl).into(schooleLogoIv);
            } else {
                schooleLogoIv.setVisibility(View.GONE);
            }
        }

        handler = new Handler(this);
        //获取节目
        getProgram(true);
    }

    public void getRecomment() {
        RequestManager.getInstance().GetRecommendedProgramsByColumnId(loginName, id, new JsonCallback<BaseResponse<Page<Program>>>() {
            @Override
            public void onSuccess(Response<BaseResponse<Page<Program>>> response) {
                handler.sendEmptyMessageDelayed(GET_NEW_DATA, GET_NEW_DATA_TIME);

                ArrayList<Program> datas = response.body().handleResult.rows;
                if (datas != null) {
                    if (tuiGuangProgramList.size() != datas.size()) {
                        tuiGuangProgramList.clear();
                        tuiGuangProgramList.addAll(datas);

                        //设置是否是一周内新发布的
                        for (Program e : datas) {
                            e.setNew(CommonUtil.isInSevenDays(e.getReleaseTime()));
                        }

                        tuiGuangAdapter.notifyDataSetChanged();
                    } else {
                        for (int i = 0; i < datas.size(); i++) {
                            if (datas.get(i).getKeyId() != tuiGuangProgramList.get(i).getKeyId()) {
                                tuiGuangProgramList.clear();
                                tuiGuangProgramList.addAll(datas);
                                tuiGuangAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onError(Response<BaseResponse<Page<Program>>> response) {
                super.onError(response);
                handler.sendEmptyMessageDelayed(GET_NEW_DATA, GET_NEW_DATA_TIME);
            }

        });
    }

    /**
     * 获取滚动新闻
     */
    public void getRollNews() {
        RequestManager.getInstance().GetRollingNews(App.loginName, new JsonCallback<BaseResponse<Page<Program>>>() {

            @Override
            public void onSuccess(Response<BaseResponse<Page<Program>>> response) {
                ArrayList<Program> datas = response.body().handleResult.rows;
                if (datas != null) {//比对数据keyId，数据改变才替换
                    if (marqueeDatas.size() != datas.size()) {
                        marqueeDatas = datas;
                        autoScrollTextView.setText(marqueeDatas.get(0).getProgramTitle());
                        autoScrollTextView.init(getWindowManager());
                        autoScrollTextView.startScroll();
                        lastMarqueeMsg = 0;
                    } else if (datas.size() == 0) {
                    } else {
                        for (int i = 0; i < datas.size(); i++) {
                            if (datas.get(i).getKeyId() != marqueeDatas.get(i).getKeyId()) {
                                marqueeDatas = datas;
                                autoScrollTextView.setText(marqueeDatas.get(0).getProgramTitle());
                                autoScrollTextView.init(getWindowManager());
                                autoScrollTextView.startScroll();
                                lastMarqueeMsg = 0;
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    public void getProgram(final boolean refresh) {
        if (refresh) {
            pageIndex = 0;
        }
        pageIndex++;

        RequestManager.getInstance().GetProgramsByColumnId(loginName, id + "", pageIndex, refresh ? Constants.INIT_PAGE_NUM * 2 : Constants.INIT_PAGE_NUM,
                new JsonCallback<BaseResponse<Page<Program>>>() {

                    @Override
                    public void onSuccess(Response<BaseResponse<Page<Program>>> response) {
                        ArrayList<Program> datas = response.body().handleResult.rows;
                        if (datas == null || datas.size() == 0) return;

                        //设置是否是一周内新发布的
                        for (Program e : datas) {
                            e.setNew(CommonUtil.isInSevenDays(e.getReleaseTime()));
                        }

                        modelList.addAll(datas);
                        if (refresh) {
                            pageIndex++;//第一次加载的2页

                            int size = modelList.size();
                            if (size % Constants.INIT_PAGE_NUM == 0) {
                                pageCount = size / Constants.INIT_PAGE_NUM;
                            } else {
                                pageCount = size / Constants.INIT_PAGE_NUM + 1;
                            }
                            for (int i = 0; i < pageCount; i++) {
                                //初始化每一个fragment
                                ProgramFragment programFragment = ProgramFragment.newInstance(i, modelList);
                                fragments.add(programFragment);
                            }

                            updateClickData();
                        } else {
                            //加载更多不管是否相等都需要加一页
//                        if (datas.size() == Constants.INIT_PAGE_NUM) {
//                            pageAdapter.addFragment(pageCount++, modelList);
//                        }
//                        if (datas.size() > 0) {
                            pageAdapter.addFragment(pageCount++, modelList);
//                        }
                        }
                        pageAdapter.notifyDataSetChanged();
                    }
                });
    }


    public void updateClickData() {
        //增加栏目点击次数
        RequestManager.getInstance().AddComumnClickNum(id + "", new JsonCallback<BaseResponse<String>>() {
            @Override
            public void onSuccess(Response<BaseResponse<String>> response) {

            }
        });
    }

    /**
     * 播放视频屏保 滚动新闻
     *
     * @param msg
     * @return
     */
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case SHOW_SCREENSAVER:
                handler.removeMessages(SHOW_SCREENSAVER);
                if (App.screenVideoUrls != null && App.screenVideoUrls.size() > 0) {
                    Intent intent = new Intent(this, ScreenVideoActivity.class);
                    startActivity(intent);
                } else {
                    handler.sendEmptyMessageDelayed(SHOW_SCREENSAVER, Constants.SHOW_SCREENSAVE_DELAY_TIME);
                }
                break;
            case GET_NEW_DATA:
                handler.removeMessages(GET_NEW_DATA);
                getRollNews();
                getRecomment();
                break;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.removeMessages(SHOW_SCREENSAVER);
            handler.removeMessages(GET_NEW_DATA);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (handler != null) {
            handler.removeMessages(SHOW_SCREENSAVER);
            handler.sendEmptyMessageDelayed(SHOW_SCREENSAVER, Constants.SHOW_SCREENSAVE_DELAY_TIME);

            handler.removeMessages(GET_NEW_DATA);
            handler.sendEmptyMessage(GET_NEW_DATA);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeMessages(SHOW_SCREENSAVER);
            handler.removeCallbacksAndMessages(null);
        }
        handler = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            handler.removeMessages(SHOW_SCREENSAVER);
            handler.sendEmptyMessageDelayed(SHOW_SCREENSAVER, Constants.SHOW_SCREENSAVE_DELAY_TIME);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (backLl.isFocused() || pre_page_tv.isFocused()) {
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (next_page_tv.isFocused()) {
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                break;
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_ll:
                this.finish();
                break;
            case R.id.pre_page_tv:
                if (viewPager.getCurrentItem() - 1 < 0) {
                    Toast.makeText(this, "已经是第一页", Toast.LENGTH_SHORT).show();
                    return;
                }
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                break;
            case R.id.next_page_tv:
                if (viewPager.getCurrentItem() + 1 >= pageCount) {
                    Toast.makeText(this, "已经是最后一页", Toast.LENGTH_SHORT).show();
                    return;
                }
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                break;
        }
    }

    private void toDetailActivity(final Program program) {
        if (program.getProgramType() == 1) {
            RequestManager.getInstance().GetVideoPicture(new JsonCallback<BaseResponse<VideoPic>>(this, true) {
                @Override
                public void onSuccess(Response<BaseResponse<VideoPic>> response) {
                    VideoPic videoPic = response.body().handleResult;

                    Intent intent = new Intent(ProgramActivity.this, PlayVideoActivity.class);
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
                    Intent intent = new Intent(ProgramActivity.this, PlayVideoActivity.class);
                    intent.putExtra("KeyId", program.getKeyId());
                    intent.putExtra("videoUrl", program.getLinkUrl());
                    intent.putExtra("videoTitle", program.getProgramTitle());
                    intent.putExtra("videoThumb", program.getImgUrl());
                }
            });
        } else {
            if (TextUtils.isEmpty(program.getLinkUrl())) {
                Toast.makeText(ProgramActivity.this, "暂无详情页面数据", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(ProgramActivity.this, WebViewActivity.class)
                        .putExtra("linkUrl", program.getLinkUrl())
                        .putExtra("KeyId", program.getKeyId())
                );
            }
        }
    }

    @Override
    public void turn() {
        if (marqueeDatas.size() == 0) return;
        autoScrollTextView.setText(marqueeDatas.get((lastMarqueeMsg + 1) % marqueeDatas.size()).getProgramTitle());
        autoScrollTextView.init(getWindowManager());
        autoScrollTextView.startScroll();
        lastMarqueeMsg = (lastMarqueeMsg + 1) % marqueeDatas.size();
    }

    @Override
    public void onClickText() {
        if (marqueeDatas.get(lastMarqueeMsg).getProgramType() == 1) {
            if (TextUtils.isEmpty(marqueeDatas.get(lastMarqueeMsg).getLinkUrl())) return;//视频无连接不跳转
        }

        toDetailActivity(marqueeDatas.get(lastMarqueeMsg));
    }

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {

        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.right = space;
        }
    }

}
