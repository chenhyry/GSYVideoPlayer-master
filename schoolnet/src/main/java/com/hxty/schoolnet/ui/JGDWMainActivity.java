package com.hxty.schoolnet.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hxty.schoolnet.App;
import com.hxty.schoolnet.R;
import com.hxty.schoolnet.entity.BaseResponse;
import com.hxty.schoolnet.entity.FirstLevelMenu;
import com.hxty.schoolnet.entity.Page;
import com.hxty.schoolnet.entity.Program;
import com.hxty.schoolnet.entity.VideoPic;
import com.hxty.schoolnet.net.Constants;
import com.hxty.schoolnet.net.JsonCallback;
import com.hxty.schoolnet.net.RequestManager;
import com.hxty.schoolnet.ui.adapter.MainSrcAdapter;
import com.hxty.schoolnet.ui.adapter.TuiGuangAdapter;
import com.hxty.schoolnet.utils.DensityUtil;
import com.hxty.schoolnet.utils.sphelper.CommonValue;
import com.hxty.schoolnet.utils.sphelper.ConfigHelper;
import com.hxty.schoolnet.widget.AutoScrollTextView;
import com.hxty.schoolnet.widget.CustomControlVideo;
import com.lzy.okgo.model.Response;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by chen on 2018/9/17.
 *
 * 如果机构有私密视频则不播放屏保视频
 */
public class JGDWMainActivity extends BaseActivity implements AutoScrollTextView.onShowNextMsg, AutoScrollTextView.OnClickTextListener/*, SuperPlayer.OnCloseScreensaver*/, Handler.Callback {

    private ArrayList<FirstLevelMenu> firstLevelMenuList = new ArrayList<>();

    //推广
    private RecyclerView tuiGuangRecycleView;
    private TuiGuangAdapter tuiGuangAdapter;
    private List<Program> tuiGuangProgramList = new ArrayList<>();

    private FrameLayout columnFrameLayout;
    private AutoScrollTextView autoScrollTextView;
    LayoutInflater inflater;

    private ImageView back_iv;
    private ImageView schooleLogoIv;
    private TextView school_name_tv;

    private String loginName;
    private String loginSchoolName;
    private String columnName;
    private ConfigHelper helper;

    //滚动新闻
    private List<Program> marqueeDatas = new ArrayList<>();
    private int lastMarqueeMsg = 0;
    private Handler handler;
    private static final int GET_NEW_DATA = 101;
    private static final int GET_NEW_DATA_TIME = 60 * 1000;

    private ArrayList<FirstLevelMenu> allLevelMenu = new ArrayList<>();
    private ArrayList<FirstLevelMenu> allLevelMenuTemp = new ArrayList<>();

    //3种图的宽高 间距
    private int hengTuWidth = 570;//比例  1 横图
    private int hengTuheight = 278;
    private int shuTuWidth = 379;//比例 2:3  2 竖图
    private int shuTuheight = 570;
    private int fangTuWidth = 278;//      3 方图
    private int fangTuheight = 278;
    private int marginCount = 14;

//    private int hengTuWidth = 644;//比例
//    private int hengTuheight = 314;
//    private int shuTuWidth = 428;//比例 2:3
//    private int shuTuheight = 644;
//    private int fangTuWidth = 314;
//    private int fangTuheight = 314;
//    private int marginCount = 4;

    private String parentid;

    private int currentPage = 1;
    private int pageSize = 8;
    private int maxPage = 0;
    private ArrayList<Program> mDatas = new ArrayList<>();
    private ArrayList<Program> mVideoDatas = new ArrayList<>();
    private int radomIndex;
    private MainSrcAdapter mainSrcAdapter;
    private TextView titleTv;
    private RelativeLayout privateRl;
    private CustomControlVideo player;
    private ImageView pre_page;
    private ImageView next_page;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jgdw_main);
        inflater = LayoutInflater.from(this);

        initView();
        initData();
        hideNavigationBar();
    }

    private void initView() {
        back_iv = findViewById(R.id.back_iv);
        back_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JGDWMainActivity.this.finish();
            }
        });

        schooleLogoIv = findViewById(R.id.schooleLogoIv);
        school_name_tv = findViewById(R.id.school_name_tv);

        columnFrameLayout = findViewById(R.id.column_frameLayout);

        autoScrollTextView = findViewById(R.id.marquee_notice_tv);
        autoScrollTextView.init(getWindowManager());
        autoScrollTextView.setOnShowNextMsg(this);
        autoScrollTextView.setOnClickTextListener(this);

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

        titleTv = findViewById(R.id.titleTv);
        titleTv.setText("“机关管理年” 沙龙展播");
        privateRl = findViewById(R.id.privateRl);
        pre_page = findViewById(R.id.pre_page);
        next_page = findViewById(R.id.next_page);
        RecyclerView column_name_recycleview = findViewById(R.id.column_name_recycleview);

        player = findViewById(R.id.player);
        player.getClTop().setVisibility(View.GONE);
        GSYVideoOptionBuilder gsyVideoOption = new GSYVideoOptionBuilder();
//        ImageView imageView = new ImageView(this);
//        if (!TextUtils.isEmpty(videoImgUrl)) {
//            Glide.with(this).load(videoImgUrl).into(imageView);
//        } else if (!TextUtils.isEmpty(videoThumb)) {
//            Glide.with(this).load(videoThumb).into(imageView);
//        }

        gsyVideoOption
//                .setThumbImageView(imageView)
                .setIsTouchWiget(true)//是否可以滑动调整
                .setRotateViewAuto(false)
//                .setLockLand(true)
                .setAutoFullWithSize(true)
                .setShowFullAnimation(false)
//                .setNeedLockFull(true)
//                .setUrl(videoUrl)
                .setCacheWithPlay(true)
//                .setVideoTitle(videoTitle)
                .setVideoAllCallBack(new GSYSampleCallBack() {
                    @Override
                    public void onPrepared(String url, Object... objects) {
                        super.onPrepared(url, objects);
                    }

                    @Override
                    public void onPlayError(String url, Object... objects) {
                        Toast.makeText(JGDWMainActivity.this, "播放失败", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAutoComplete(String url, Object... objects) {
                        getRadomIndex();
                        player.setUp(mVideoDatas.get(radomIndex).getLinkUrl(), true, "");
                    }
                }).build(player);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        column_name_recycleview.setLayoutManager(gridLayoutManager);
        mainSrcAdapter = new MainSrcAdapter(JGDWMainActivity.this, mDatas);
        column_name_recycleview.setAdapter(mainSrcAdapter);
        mainSrcAdapter.setOnItemClickListener(new MainSrcAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                toDetailActivity(mDatas.get(position));
            }
        });
        pre_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage == 1) {
                    Toast.makeText(JGDWMainActivity.this, "当前已经是第一页", Toast.LENGTH_SHORT).show();
                } else {
                    currentPage--;
                    getPrivateData();
                }
            }
        });
        next_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage == maxPage || maxPage == 0) {
                    Toast.makeText(JGDWMainActivity.this, "没有更多数据", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    currentPage++;
                    getPrivateData();
                }
            }
        });
    }

    private void initData() {
        helper = ConfigHelper.getDefaultConfigHelper(this);

        loginName = getIntent().getStringExtra("loginName");
        loginSchoolName = getIntent().getStringExtra("loginSchoolName");
        columnName = getIntent().getStringExtra("columnName");

        String imgUrl = helper.getString(CommonValue.LOGO_URL, CommonValue.USER_NULL_DEFAULT);
        parentid = helper.getString(CommonValue.PARENTID, CommonValue.USER_NULL_DEFAULT);
        if (!TextUtils.isEmpty(imgUrl)) {
            schooleLogoIv.setVisibility(View.VISIBLE);
            Glide.with(this).load(Constants.IMAGE_URL + imgUrl).into(schooleLogoIv);
        } else {
            schooleLogoIv.setVisibility(View.GONE);
        }

        school_name_tv.setText(loginSchoolName);

        if (TextUtils.isEmpty(loginName)) finish();
        getFirstDatahigherData();

        handler = new Handler(this);
        handler.sendEmptyMessageDelayed(GET_NEW_DATA, GET_NEW_DATA_TIME);

        //登录后首页私密视频进行轮播
        getPrivateVideo();
        //登录后首页私密节目
//        getPrivateData();
    }


    private void getPrivateData() {
        if (App.loginName != null) {
            RequestManager.getInstance().GetProgramsIsPrivate(App.loginName,
                    pageSize + "",
                    currentPage + "",
                    new JsonCallback<BaseResponse<Page<Program>>>() {

                        @Override
                        public void onSuccess(Response<BaseResponse<Page<Program>>> response) {
                            if (response.body().handleResult != null) {
                                int total = response.body().handleResult.total;
                                maxPage = (total + pageSize - 1) / pageSize;
                                if (total > 0) {
                                    privateRl.setVisibility(View.VISIBLE);
                                }
                            }

                            ArrayList<Program> datas = response.body().handleResult.rows;
                            if (datas != null) {
                                for (int i = 0; i < datas.size(); i++) {
                                    datas.get(i).setProgramTitle(((currentPage - 1) * pageSize + i + 1) + "." + datas.get(i).getProgramTitle());
                                }
                                mDatas.clear();
                                mDatas.addAll(datas);
                                mainSrcAdapter.notifyDataSetChanged();
                            }

                        }
                    });

        }
    }

    private void getPrivateVideo() {
        if (App.loginName != null) {
            RequestManager.getInstance().GetProgramsIsPrivate(App.loginName, "1000", currentPage + "",
                    new JsonCallback<BaseResponse<Page<Program>>>() {

                        @Override
                        public void onSuccess(Response<BaseResponse<Page<Program>>> response) {
                            if (response.body().handleResult != null) {
                                int total = response.body().handleResult.total;
                                maxPage = (total + pageSize - 1) / pageSize;
                                if (total > 0) {
                                    privateRl.setVisibility(View.VISIBLE);
                                }
                            }

                            ArrayList<Program> datas = response.body().handleResult.rows;

                            if (datas != null) {
                                //处理第一页节目数据
                                mDatas.clear();
                                for (int i = 0; i < pageSize; i++) {
                                    //处理标题
                                    datas.get(i).setProgramTitle(((currentPage - 1) * pageSize + i + 1) + "." + datas.get(i).getProgramTitle());
                                    mDatas.add(datas.get(i));
                                }
                                mainSrcAdapter.notifyDataSetChanged();

                                //处理轮播视频
                                for (int i = 0; i < datas.size(); i++) {
                                    if (datas.get(i).getProgramType() == 1) {
                                        mVideoDatas.add(datas.get(i));
                                    }
                                }

                                if (mVideoDatas.size() > 0) {
                                    player.setUp(mVideoDatas.get(radomIndex).getLinkUrl(), true, "");
                                    //播放
                                    player.startPlayLogic();
                                }
                            }
                        }
                    });

        }
    }

    /**
     * 计算随机播放视频下标
     */
    public void getRadomIndex() {
        if (mVideoDatas.size() == 1) {
            radomIndex = 0;
        } else if (mVideoDatas.size() > 1) {
            int newRadomIndex = new Random().nextInt(mVideoDatas.size());
            if (radomIndex == newRadomIndex) {
                getRadomIndex();
            } else {
                radomIndex = newRadomIndex;
            }
        }
    }

    /**
     * 根据登录名称获取该机构的推荐节目列表
     */
    public void getRecomment() {
        RequestManager.getInstance().GetRecommendedProgramsByColumnId(loginName, -1, new JsonCallback<BaseResponse<Page<Program>>>() {

            @Override
            public void onSuccess(Response<BaseResponse<Page<Program>>> response) {

                handler.sendEmptyMessageDelayed(GET_NEW_DATA, GET_NEW_DATA_TIME);
                ArrayList<Program> datas = response.body().handleResult.rows;
                if (datas != null) {
                    if (tuiGuangProgramList.size() != datas.size()) {
                        tuiGuangProgramList.clear();
                        tuiGuangProgramList.addAll(datas);
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

                //如果有私密节目视频不播放屏保视频
                if (mVideoDatas.size() == 0) {
                    if (App.screenVideoUrls != null && App.screenVideoUrls.size() > 0) {
                        Intent intent = new Intent(this, ScreenVideoActivity.class);
                        startActivity(intent);
                    } else {
                        handler.sendEmptyMessageDelayed(SHOW_SCREENSAVER, Constants.SHOW_SCREENSAVE_DELAY_TIME);
                    }
                }
                break;
            case GET_NEW_DATA:
                handler.removeMessages(GET_NEW_DATA);
                getRollNews();
                getFirstLevelData2();
                getRecomment();
                break;
        }
        return true;
    }


    @Override
    protected void onPause() {
        super.onPause();
//        if (view_super_player2 != null) {
//            view_super_player2.pause();
//        }
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
            //如果机构有私密视频则不播放屏保视频 此处可以不判断
            if (mVideoDatas.size() == 0) {
                handler.sendEmptyMessageDelayed(SHOW_SCREENSAVER, Constants.SHOW_SCREENSAVE_DELAY_TIME);
            }

            handler.removeMessages(GET_NEW_DATA);
            handler.sendEmptyMessage(GET_NEW_DATA);
        }

//        if (view_super_player2 != null) {
//            view_super_player2.onResume();
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (view_super_player2 != null) {
//            view_super_player2.onDestroy();
//        }

        if (handler != null) {
            handler.removeMessages(SHOW_SCREENSAVER);
            handler.removeCallbacksAndMessages(null);
        }
        handler = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        if (view_super_player2 != null) {
//            view_super_player2.onConfigurationChanged(newConfig);
//        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            handler.removeMessages(SHOW_SCREENSAVER);
            //如果机构有私密视频则不播放屏保视频 此处可以不判断
            if (mVideoDatas.size() == 0) {
                handler.sendEmptyMessageDelayed(SHOW_SCREENSAVER, Constants.SHOW_SCREENSAVE_DELAY_TIME);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 获取一级栏目数据   返回的是无排序顺序
     */
    private void getFirstDatahigherData() {
        int parentIdInt = 0;
        if (!TextUtils.isEmpty(parentid)) {
            parentIdInt = Integer.parseInt(parentid);
        }
        if (parentIdInt > 0) {
            RequestManager.getInstance().GetColumnsByParentId(parentid, new JsonCallback<BaseResponse<Page<FirstLevelMenu>>>(JGDWMainActivity.this, true) {

                @Override
                public void onSuccess(Response<BaseResponse<Page<FirstLevelMenu>>> response) {
                    ArrayList<FirstLevelMenu> dataParents = response.body().handleResult.rows;

                    Collections.sort(dataParents);

                    allLevelMenu.clear();
                    for (int i = 0; i < dataParents.size(); i++) {
                        dataParents.get(i).setIsjiaoWeiLanMu(true);
                    }
                    allLevelMenu.addAll(dataParents);
                    setFirstLevelMenuData(0, allLevelMenu);

                    RequestManager.getInstance().GetColumnBySchoolName(loginName, new JsonCallback<BaseResponse<Page<FirstLevelMenu>>>(JGDWMainActivity.this, true) {

                        @Override
                        public void onSuccess(Response<BaseResponse<Page<FirstLevelMenu>>> response) {
                            ArrayList<FirstLevelMenu> datas = response.body().handleResult.rows;

                            //设置本单位栏目坐标
                            if (allLevelMenu.size() > 0) {
                                int lastMaxX = 0;
                                FirstLevelMenu firstLevelMenu = allLevelMenu.get(allLevelMenu.size() - 1);

                                lastMaxX = firstLevelMenu.getX();

                                if (firstLevelMenu.getDisplayStyle() == 1) {//横图
                                    lastMaxX += DensityUtil.dip2px(JGDWMainActivity.this, marginCount + hengTuWidth);
                                } else if (firstLevelMenu.getDisplayStyle() == 2) {//竖图
                                    lastMaxX += DensityUtil.dip2px(JGDWMainActivity.this, marginCount + shuTuWidth);
                                } else {//方图
                                    if (allLevelMenu.size() > 1 && allLevelMenu.get(allLevelMenu.size() - 2).getDisplayStyle() == 1) {
                                        lastMaxX += DensityUtil.dip2px(JGDWMainActivity.this, marginCount + hengTuWidth);
                                    } else {
                                        lastMaxX += DensityUtil.dip2px(JGDWMainActivity.this, marginCount + fangTuWidth);
                                    }
                                }

                                setFirstLevelMenuData(lastMaxX, datas);
                            }

                            allLevelMenu.addAll(datas);
                            addColumnView(firstLevelMenuList, true);
                        }
                    });
                }
            });
        } else {
            RequestManager.getInstance().GetColumnBySchoolName(loginName, new JsonCallback<BaseResponse<Page<FirstLevelMenu>>>(JGDWMainActivity.this, true) {

                @Override
                public void onSuccess(Response<BaseResponse<Page<FirstLevelMenu>>> response) {
                    allLevelMenu.clear();
                    ArrayList<FirstLevelMenu> datas = response.body().handleResult.rows;
                    allLevelMenu.addAll(datas);
                    setFirstLevelMenuData(0, allLevelMenu);
                    addColumnView(firstLevelMenuList, true);
                }
            });
        }
    }


    /**
     * 获取栏目数据   判断是否需要更新界面
     */
    private void getFirstLevelData2() {
        int parentIdInt = 0;
        if (!TextUtils.isEmpty(parentid)) {
            parentIdInt = Integer.valueOf(parentid);
        }
        if (parentIdInt > 0) {
            RequestManager.getInstance().GetColumnsByParentId(parentid, new JsonCallback<BaseResponse<Page<FirstLevelMenu>>>(JGDWMainActivity.this, true) {

                @Override
                public void onSuccess(Response<BaseResponse<Page<FirstLevelMenu>>> response) {
                    ArrayList<FirstLevelMenu> dataParents = response.body().handleResult.rows;

                    Collections.sort(dataParents);

                    allLevelMenuTemp.clear();
                    for (int i = 0; i < dataParents.size(); i++) {
                        dataParents.get(i).setIsjiaoWeiLanMu(true);
                    }
                    allLevelMenuTemp.addAll(dataParents);
                    setFirstLevelMenuData(0, allLevelMenuTemp);

                    RequestManager.getInstance().GetColumnBySchoolName(loginName, new JsonCallback<BaseResponse<Page<FirstLevelMenu>>>(JGDWMainActivity.this, true) {

                        @Override
                        public void onSuccess(Response<BaseResponse<Page<FirstLevelMenu>>> response) {
                            ArrayList<FirstLevelMenu> datas = response.body().handleResult.rows;

                            //设置本单位栏目坐标
                            if (allLevelMenuTemp.size() > 0) {
                                int lastMaxX = 0;
                                FirstLevelMenu firstLevelMenu = allLevelMenuTemp.get(allLevelMenuTemp.size() - 1);

                                lastMaxX = firstLevelMenu.getX();

                                if (firstLevelMenu.getDisplayStyle() == 1) {//横图
                                    lastMaxX += DensityUtil.dip2px(JGDWMainActivity.this, marginCount + hengTuWidth);
                                } else if (firstLevelMenu.getDisplayStyle() == 2) {//竖图
                                    lastMaxX += DensityUtil.dip2px(JGDWMainActivity.this, marginCount + shuTuWidth);
                                } else {//方图
                                    if (allLevelMenuTemp.size() > 1 && allLevelMenuTemp.get(allLevelMenuTemp.size() - 2).getDisplayStyle() == 1) {
                                        lastMaxX += DensityUtil.dip2px(JGDWMainActivity.this, marginCount + hengTuWidth);
                                    } else {
                                        lastMaxX += DensityUtil.dip2px(JGDWMainActivity.this, marginCount + fangTuWidth);
                                    }
                                }

                                setFirstLevelMenuData(lastMaxX, datas);
                            }
                            allLevelMenuTemp.addAll(datas);
                            isNewData(allLevelMenuTemp);
                        }
                    });
                }
            });
        } else {
            RequestManager.getInstance().GetColumnBySchoolName(loginName, new JsonCallback<BaseResponse<Page<FirstLevelMenu>>>(JGDWMainActivity.this, true) {

                @Override
                public void onSuccess(Response<BaseResponse<Page<FirstLevelMenu>>> response) {
                    isNewData(response.body().handleResult.rows);
                }
            });
        }

    }


    public void isNewData(ArrayList<FirstLevelMenu> datas) {
        if (allLevelMenu != null) {
//            if (errorLl.getVisibility() == View.VISIBLE) {
//                errorLl.setVisibility(View.GONE);
//            }
            //只有栏目改变才需要重新绘制,只有图片变换不用重新绘制--可优化
            for (int i = 0; i < allLevelMenu.size(); i++) {
                if (datas != null && datas.size() == allLevelMenu.size()) {
                    if (datas.get(i).getKeyId() != allLevelMenu.get(i).getKeyId()) {
                        setFirstLevelMenuData(0, datas);
                        addColumnView(allLevelMenu, false);
                        break;
                    } else {
                        if (!comPareImg(datas.get(i).getImgUrl1(), allLevelMenu.get(i).getImgUrl1())
                                && !comPareImg(datas.get(i).getImgUrl2(), allLevelMenu.get(i).getImgUrl2())
                                && !comPareImg(datas.get(i).getImgUrl3(), allLevelMenu.get(i).getImgUrl3())) {
                            allLevelMenu.get(i).setImgUrl1(datas.get(i).getImgUrl1());
                            allLevelMenu.get(i).setImgUrl2(datas.get(i).getImgUrl2());
                            allLevelMenu.get(i).setImgUrl3(datas.get(i).getImgUrl3());
                        }
                    }
                } else {
                    allLevelMenu.clear();
                    allLevelMenu.addAll(datas);
                    setFirstLevelMenuData(0, allLevelMenu);
                    addColumnView(allLevelMenu, false);
                    break;
                }
            }
        }
    }

    /**
     * 排序　计算坐标  保证教委的栏目在前边
     *
     * @param datas
     */
    public void setFirstLevelMenuData(int lastMaxX, ArrayList<FirstLevelMenu> datas) {
        setxytemp(lastMaxX, datas);
    }

    public void setxytemp(int lastMaxX, List<FirstLevelMenu> firstLevelMenuList) {
        //教委和机关数据分开处理
        for (int i = 0; i < firstLevelMenuList.size(); i++) {
            //DisplayStyle 1:横图  2：竖图 3：方图
            if (i == 0) {//第一张图片
                firstLevelMenuList.get(i).setX(lastMaxX);
                firstLevelMenuList.get(i).setY(0);
            } else if (i == 1) {
                if (firstLevelMenuList.get(0).getDisplayStyle() == 1) {
                    firstLevelMenuList.get(i).setX(lastMaxX);
                    firstLevelMenuList.get(i).setY(1);
                } else if (firstLevelMenuList.get(0).getDisplayStyle() == 2) {
                    firstLevelMenuList.get(i).setX(lastMaxX + DensityUtil.dip2px(this, marginCount + shuTuWidth));
                    firstLevelMenuList.get(i).setY(0);
                } else if (firstLevelMenuList.get(0).getDisplayStyle() == 3) {
                    if (firstLevelMenuList.get(i).getDisplayStyle() == 1) {
                        firstLevelMenuList.get(i).setX(lastMaxX);
                        firstLevelMenuList.get(i).setY(1);
                    } else {
                        firstLevelMenuList.get(i).setX(lastMaxX);
                        firstLevelMenuList.get(i).setY(1);
                    }
                }
            } else {
                if (firstLevelMenuList.get(i - 1).getDisplayStyle() == 1) {//前面一张是横图 1.第一排　2.第二排
                    if (firstLevelMenuList.get(i - 1).getY() == 0) {//第一排 -- 下排起始　横图方图方图,第二张方图  横图横图，第二张横图　
                        firstLevelMenuList.get(i).setX(firstLevelMenuList.get(i - 1).getX());
                        firstLevelMenuList.get(i).setY(1);
                    } else {//第二排 新行起始
                        firstLevelMenuList.get(i).setX(DensityUtil.dip2px(this, marginCount + hengTuWidth) + firstLevelMenuList.get(i - 1).getX());
                        firstLevelMenuList.get(i).setY(0);
                    }
                } else if (firstLevelMenuList.get(i - 1).getDisplayStyle() == 2) {//前面一张是竖图
                    firstLevelMenuList.get(i).setX(DensityUtil.dip2px(this, marginCount + shuTuWidth) + firstLevelMenuList.get(i - 1).getX());
                    firstLevelMenuList.get(i).setY(0);
                } else {//前面一张是方图
                    //1.方图方图第二张  2.横图方图方图第3张  3.新行起始第一张
                    if (firstLevelMenuList.get(i - 1).getY() == 0) {
                        firstLevelMenuList.get(i).setX(firstLevelMenuList.get(i - 1).getX());
                        firstLevelMenuList.get(i).setY(1);
                    } else if (firstLevelMenuList.get(i - 1).getY() == 1 && firstLevelMenuList.get(i - 2).getDisplayStyle() == 1 && firstLevelMenuList.get(i - 2).getY() == 0) {//横图方图方图第3张
                        firstLevelMenuList.get(i).setX(DensityUtil.dip2px(this, marginCount + fangTuWidth) + firstLevelMenuList.get(i - 1).getX());
                        firstLevelMenuList.get(i).setY(1);
                    } else {//新行起始第一张
                        //判断前面是　竖图　横图 方图
                        if (firstLevelMenuList.get(i - 1).getDisplayStyle() == 2) {
                            firstLevelMenuList.get(i).setX(DensityUtil.dip2px(this, marginCount + shuTuWidth) + firstLevelMenuList.get(i - 1).getX());
                        } else if (firstLevelMenuList.get(i - 1).getDisplayStyle() == 1) {
                            firstLevelMenuList.get(i).setX(DensityUtil.dip2px(this, marginCount + hengTuWidth) + firstLevelMenuList.get(i - 1).getX());
                        } else {
                            firstLevelMenuList.get(i).setX(DensityUtil.dip2px(this, marginCount + fangTuWidth) + firstLevelMenuList.get(i - 1).getX());
                        }
                        firstLevelMenuList.get(i).setY(0);
                    }
                }
            }
        }
        this.firstLevelMenuList.addAll(firstLevelMenuList);
    }

    public boolean comPareImg(String url1, String url2) {
        if (url1 == null && url2 == null) {
            return true;
        } else if (url1 != null && url2 != null) {
            if (url1.equals(url2)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 添加视图
     *
     * @param firstLevelMenuList
     * @param isFirstTimeAdd     第一次添加视图第一个栏目获取焦点
     */
    private void addColumnView(final List<FirstLevelMenu> firstLevelMenuList, boolean isFirstTimeAdd) {
        columnFrameLayout.removeAllViews();
        FirstLevelMenu item = null;
        for (int i = 0; i < firstLevelMenuList.size(); i++) {
            item = firstLevelMenuList.get(i);
            ImageView first_item_iv = (ImageView) inflater.inflate(R.layout.first_level_item, null);
            first_item_iv.setTag(R.id.imageid, i);
            first_item_iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Integer index = (Integer) v.getTag(R.id.imageid);
                    startActivity(new Intent(JGDWMainActivity.this, ProgramActivity.class).putExtra("id", firstLevelMenuList.get(index).getKeyId()).putExtra("columnName", firstLevelMenuList.get(index).getColumnName()));
                }
            });

            FrameLayout.LayoutParams params = null;
            if (item.getDisplayStyle() == 1) {
                params = new FrameLayout.LayoutParams(DensityUtil.dip2px(this, hengTuWidth), DensityUtil.dip2px(this, hengTuheight));
            } else if (item.getDisplayStyle() == 2) {
                params = new FrameLayout.LayoutParams(DensityUtil.dip2px(this, shuTuWidth), DensityUtil.dip2px(this, shuTuheight));
            } else {
                params = new FrameLayout.LayoutParams(DensityUtil.dip2px(this, fangTuWidth), DensityUtil.dip2px(this, fangTuheight));
            }
            params.gravity = Gravity.LEFT | Gravity.TOP;
            params.leftMargin = item.getX();
            params.topMargin = item.getY() * DensityUtil.dip2px(this, fangTuWidth + marginCount);
            first_item_iv.setLayoutParams(params);

            if (!TextUtils.isEmpty(item.getImgUrl1())) {
                Glide.with(this).load(Constants.IMAGE_URL + item.getImgUrl1()).placeholder(R.drawable.default_img).into(first_item_iv);
            }
            columnFrameLayout.addView(first_item_iv);
            if (isFirstTimeAdd && i == 0) {
                first_item_iv.requestFocus();
            }
        }
    }

    public int getImgWidth(int DisplayStyle) {
        if (DisplayStyle == 1) {
            return hengTuWidth;
        } else if (DisplayStyle == 2) {
            return shuTuWidth;
        } else {
            return fangTuWidth;
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
            if (TextUtils.isEmpty(marqueeDatas.get(lastMarqueeMsg).getLinkUrl()))
                return;//视频无连接不跳转
        }

        toDetailActivity(marqueeDatas.get(lastMarqueeMsg));
    }

    private void toDetailActivity(final Program program) {
        if (program.getProgramType() == 1) {
            RequestManager.getInstance().GetVideoPicture(new JsonCallback<BaseResponse<VideoPic>>(this, true) {
                @Override
                public void onSuccess(Response<BaseResponse<VideoPic>> response) {
                    VideoPic videoPic = response.body().handleResult;
                    Intent intent = new Intent(JGDWMainActivity.this, PlayVideoActivity.class);
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
                public void onError(Response response) {
                    super.onError(response);
                    Intent intent = new Intent(JGDWMainActivity.this, PlayVideoActivity.class);
                    intent.putExtra("KeyId", program.getKeyId());
                    intent.putExtra("videoUrl", program.getLinkUrl());
                    intent.putExtra("videoTitle", program.getProgramTitle());
                    intent.putExtra("videoThumb", program.getImgUrl());
                }
            });
        } else {
            if (TextUtils.isEmpty(program.getLinkUrl())) {
                Toast.makeText(JGDWMainActivity.this, "暂无详情页面数据", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(JGDWMainActivity.this, WebViewActivity.class)
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
            outRect.right = space;
        }
    }
}
