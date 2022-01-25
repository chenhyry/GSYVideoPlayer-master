package com.hxty.schoolnet.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.hxty.schoolnet.App;
import com.hxty.schoolnet.R;
import com.hxty.schoolnet.entity.BaseResponse;
import com.hxty.schoolnet.entity.FirstLevelMenu;
import com.hxty.schoolnet.entity.Page;
import com.hxty.schoolnet.entity.Program;
import com.hxty.schoolnet.entity.SchoolMember;
import com.hxty.schoolnet.entity.Version;
import com.hxty.schoolnet.entity.VideoPic;
import com.hxty.schoolnet.net.Constants;
import com.hxty.schoolnet.net.JsonCallback;
import com.hxty.schoolnet.net.RequestManager;
import com.hxty.schoolnet.receiver.NetworkStateReceiver;
import com.hxty.schoolnet.service.CheckDataService;
import com.hxty.schoolnet.utils.CommonUtil;
import com.hxty.schoolnet.utils.DensityUtil;
import com.hxty.schoolnet.utils.RegUtils;
import com.hxty.schoolnet.utils.SimpleDetector;
import com.hxty.schoolnet.utils.sphelper.CommonValue;
import com.hxty.schoolnet.utils.sphelper.ConfigHelper;
import com.hxty.schoolnet.widget.AutoScrollTextView;
import com.lzy.okgo.model.Response;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chen on 2017/3/27.
 * 首页
 */

public class MainActivity extends CheckPermissionsActivity implements Handler.Callback, AutoScrollTextView.onShowNextMsg, AutoScrollTextView.OnClickTextListener, View.OnClickListener {

    private Handler handler;

    private FrameLayout columnFrameLayout;
    private AutoScrollTextView autoScrollTextView;
    private LayoutInflater inflater;

    //每隔一段时间切换一次图片
    private static final int TURN_FIRST_IMAGE = 105;
    private static final int TURN_SECOND_IMAGE = 106;
    private static final int TURN_THIRD_IMAGE = 107;
    private static final int TURN_IMAGE_DELAY_TIME = 10 * 1000;
    private ArrayList<FirstLevelMenu> firstLevelMenuList = new ArrayList<>();

    //3种图的宽高 间距
    private int hengTuWidth = 644;//比例
    private int hengTuheight = 314;
    private int shuTuWidth = 428;//比例 2:3
    private int shuTuheight = 644;
    private int fangTuWidth = 314;
    private int fangTuheight = 314;
    private int marginCount = 16;

    //滚动新闻
    private List<Program> marqueeDatas = new ArrayList<>();
    private int lastMarqueeMsg = 0;

    private TextView toH5Tv;
    private TextView loginoutTv;

    //右上角版本更新 网络状态 时间 设置
    private LinearLayout versionLl;
    private ImageView checkVersionIv;
    private TextView versionTv;
    private ImageView networkStateIv;
    private NetworkStateReceiver receiver;

    private Animation animationIn;
    private Animation animationOut;
    private ImageView san_jiao_iv;
    private ImageView xia_san_jiao_iv;
    private RelativeLayout set_relative;

    //进入设置 安装apk需要输入密码 安装apk过程中如果把安装界面点到后台,安装完成后会提示选择启动项
    private String pswKey = "gly123";//密码
    private Button cancel_btn;
    private Button submit_btn;
    private RelativeLayout psw_set;
    private EditText content_edit;
    private LinearLayout settingLl;
    private int isIntoSettingInstallApkLogout;//1安装apk  2进入设置 3退出登录

    //登录
    private String validcode;
    private int countDown = 60;
    private Handler sendCodeHandler;
    private Runnable runnable;
    private TextView etLoginUsername;
    private TextView etLoginPwd;
    private Button btnSendCode;
    private Button login_cancel_btn;
    private Button login_submit_btn;
    private RelativeLayout login_rl;
    private TextView school_name_tv;
    private RadioGroup rgType;
    private RadioButton tab1;

    //数据请求失败页面
    private LinearLayout errorLl;
    private TextView loadAgain;//点击重新加载

    //版本更新
    private String downloadUrl;
    private String downloadPath;
    private String apkName;

    private ConfigHelper helper;

    private int shutDownTime;
    private int onLineTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inflater = LayoutInflater.from(this);

        helper = ConfigHelper.getDefaultConfigHelper(this);

        initView();
        initData();
        registerReceiver();
        //开启检查版本 更新一级栏目数据 屏保 滚动新闻数据服务
        bindService(new Intent(this, CheckDataService.class), conn, Context.BIND_AUTO_CREATE);


        hideNavigationBar();
        toH5Tv.post(new Runnable() {
            @Override
            public void run() {
                //上传活动数据
                postLiveData(true);
            }
        });
    }

    private void initView() {
        toH5Tv = findViewById(R.id.to_h5_tv);
        loginoutTv = findViewById(R.id.loginout_tv);
        toH5Tv.setOnClickListener(this);
        loginoutTv.setOnClickListener(this);

        etLoginUsername = findViewById(R.id.etLoginUsername);
        etLoginPwd = findViewById(R.id.etLoginPwd);
        btnSendCode = findViewById(R.id.btnSendCode);
        login_cancel_btn = findViewById(R.id.login_cancel_btn);
        login_submit_btn = findViewById(R.id.login_submit_btn);
        login_rl = findViewById(R.id.login_rl);
        school_name_tv = findViewById(R.id.school_name_tv);
        btnSendCode.setOnClickListener(this);
        login_cancel_btn.setOnClickListener(this);
        login_submit_btn.setOnClickListener(this);
        school_name_tv.setOnClickListener(this);

        runnable = new Runnable() {
            @Override
            public void run() {
                sendCodeHandler.removeCallbacks(runnable);

                if (countDown <= 0) {
                    btnSendCode.setEnabled(true);
                    btnSendCode.setText("重新发送");
                    countDown = 60;
                } else {
                    btnSendCode.setEnabled(false);
                    btnSendCode.setText(String.format("%02d", countDown) + "s后重发");
                    countDown--;
                    sendCodeHandler.postDelayed(this, 1000);
                }

            }
        };

        rgType = findViewById(R.id.rgType);
        tab1 = findViewById(R.id.tab1);

        rgType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.tab1) {
                    etLoginUsername.setText("");
                    etLoginUsername.setInputType(EditorInfo.TYPE_CLASS_TEXT);
                    etLoginPwd.setText("");
                    etLoginPwd.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
                    etLoginUsername.setHint("请输入账号");
                    etLoginPwd.setHint("请输入密码");
                    btnSendCode.setVisibility(View.GONE);
                } else {
                    etLoginUsername.setText("");
                    etLoginUsername.setInputType(EditorInfo.TYPE_CLASS_PHONE);
                    etLoginPwd.setText("");
                    etLoginPwd.setInputType(EditorInfo.TYPE_CLASS_PHONE);
                    etLoginUsername.setHint("请输入手机号");
                    etLoginPwd.setHint("请输入验证码");
                    btnSendCode.setVisibility(View.VISIBLE);
                }
            }
        });
//        etLoginUsername.setOnEditorActionListener();

        //右上角版本等view
        versionTv = findViewById(R.id.versionTv);
        checkVersionIv = findViewById(R.id.checkVersionIv);
        versionLl = findViewById(R.id.version_linear);
        versionTv.setText("版本 : " + CommonUtil.getVersionName(this));
        networkStateIv = findViewById(R.id.networkStateIv);
        san_jiao_iv = findViewById(R.id.shang_san_jiao_iv);
        xia_san_jiao_iv = findViewById(R.id.xia_san_jiao_iv);
        set_relative = findViewById(R.id.set_relative);
        settingLl = findViewById(R.id.setting_linear);
        xia_san_jiao_iv.setOnTouchListener(new SimpleDetector(this) {
            @Override
            public void onScrollDown() {
                if (set_relative.getVisibility() == View.GONE) {
                    set_relative.setVisibility(View.VISIBLE);
                    set_relative.startAnimation(animationIn);
                    xia_san_jiao_iv.setVisibility(View.GONE);
                }
            }

            @Override
            public void onScrollUp() {

            }
        });
        san_jiao_iv.setOnTouchListener(new SimpleDetector(this) {
            @Override
            public void onScrollDown() {

            }

            @Override
            public void onScrollUp() {
                if (set_relative.getVisibility() == View.VISIBLE) {
                    set_relative.startAnimation(animationOut);
                }
            }
        });
        versionLl.setOnClickListener(this);
        settingLl.setOnClickListener(this);
        versionLl.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (set_relative.getVisibility() == View.VISIBLE) {
                        set_relative.startAnimation(animationOut);
                    }
                }
            }
        });
        xia_san_jiao_iv.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (set_relative.getVisibility() == View.GONE) {
                        set_relative.setVisibility(View.VISIBLE);
                        set_relative.startAnimation(animationIn);
                        xia_san_jiao_iv.setVisibility(View.GONE);
                    }
                }
            }
        });

        //进入设置或者安装apk需要输入的密码框
        psw_set = findViewById(R.id.psw_set);
        content_edit = findViewById(R.id.content_edit);
        cancel_btn = findViewById(R.id.cancel_btn);
        submit_btn = findViewById(R.id.submit_btn);
        cancel_btn.setOnClickListener(this);
        submit_btn.setOnClickListener(this);

        //网络等问题导致的请求失败页面
        errorLl = findViewById(R.id.error_ll);
        loadAgain = findViewById(R.id.load_again_tv);
        loadAgain.setOnClickListener(this);

        columnFrameLayout = findViewById(R.id.column_frameLayout);
        autoScrollTextView = findViewById(R.id.marquee_notice_tv);
        autoScrollTextView.init(getWindowManager());
        autoScrollTextView.setOnShowNextMsg(this);
        autoScrollTextView.setOnClickTextListener(this);

        animationIn = AnimationUtils.loadAnimation(this, R.anim.main_set_anim_in);
        animationOut = AnimationUtils.loadAnimation(this, R.anim.main_set_anim_out);
        animationOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                set_relative.setVisibility(View.GONE);
                xia_san_jiao_iv.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private void initData() {
        apkName = getResources().getString(R.string.app_name_en) + ".apk";
        downloadPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getResources().getString(R.string.app_name) + File.separator + apkName;

        String loginName = helper.getString(CommonValue.LOGIN_NAME, CommonValue.USER_NULL_DEFAULT);
        if (!TextUtils.isEmpty(loginName)) {
            App.loginName = loginName;
            App.loginSchoolName = helper.getString(CommonValue.LOGIN_SCHOOL_NAME, CommonValue.USER_NULL_DEFAULT);
            App.SCHOOLTYPE = helper.getString(CommonValue.SCHOOLTYPE, CommonValue.USER_NULL_DEFAULT);
            school_name_tv.setVisibility(View.VISIBLE);
            school_name_tv.setText(App.loginSchoolName);
            loginoutTv.setText("退出");

            //机关单位跳转到自己的主页
            if ("2".equals(App.SCHOOLTYPE)) {
                toProPage();
            } else if ("3".equals(App.SCHOOLTYPE)) {
                schoolMemberLogin();
            }
        }

        handler = new Handler(this);

        getFirstLevelData();
    }


    /**
     * 判断登录状态直接到机构的栏目页面
     */
    public void toProPage() {
        Intent intent = new Intent(MainActivity.this, JGDWMainActivity.class);
        intent.putExtra("loginName", App.loginName);
        intent.putExtra("loginSchoolName", App.loginSchoolName);
        startActivity(intent);
    }


    /**
     * 获取一级栏目数据   返回的排序顺序从上到下，从左到右 一共5种排版
     */
    public void getFirstLevelData() {
        RequestManager.getInstance().GetFirstLevelColumns(new JsonCallback<BaseResponse<Page<FirstLevelMenu>>>(this, true) {
            @Override
            public void onSuccess(Response<BaseResponse<Page<FirstLevelMenu>>> response) {
                firstLevelMenuList = response.body().handleResult.rows;
                setFirstLevelMenuData(firstLevelMenuList);
                addColumnView(firstLevelMenuList, true);
            }

            @Override
            public void onError(Response<BaseResponse<Page<FirstLevelMenu>>> response) {
                super.onError(response);
                errorLl.setVisibility(View.VISIBLE);
            }
        });
    }


    private void registerReceiver() {
        //网络监听
        receiver = new NetworkStateReceiver();
        receiver.setNetStateChangeCallBack(new NetworkStateReceiver.NetStateChangeCallBack() {
            @Override
            public void netStateChange(int state) {
                switch (state) {
                    case NetworkStateReceiver.NO_NET:
                        Toast.makeText(MainActivity.this, "网络断开", Toast.LENGTH_SHORT).show();
                        networkStateIv.setImageResource(R.drawable.no_net);
                        break;
                    case NetworkStateReceiver.NET_WIFI:
                        networkStateIv.setImageResource(R.drawable.wifi_icon);
                        break;
                    case NetworkStateReceiver.NET_CMWAP:
                    case NetworkStateReceiver.NET_CMNET:
                        networkStateIv.setImageResource(R.drawable.wired_conn);
                        break;
                }
            }
        });
        registerReceiver(receiver, NetworkStateReceiver.FILTER);

        //按Home键
//        registerReceiver(mHomeKeyEventReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        // 注册锁屏事件
        registerReceiver(myReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(myReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        registerReceiver(myReceiver, new IntentFilter(Intent.ACTION_USER_PRESENT));
    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {//当按下电源键，屏幕亮起的时候
                App.islockScreen = false;
            }
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {//当按下电源键，屏幕变黑的时候
                App.islockScreen = true;
            }
            if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {//当解除锁屏的时候
                App.islockScreen = false;
            }
        }
    };


    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            CheckDataService.MyBinder binder = (CheckDataService.MyBinder) iBinder;
            CheckDataService checkDataService = binder.getService();
            checkDataService.setOnVersionInfoChangeListener(new CheckDataService.OnVersionInfoChangeListener() {
                @Override
                public void onVersionInfoChange(String url) {
                    if (url != null) {
                        if (!url.equals(downloadUrl)) {//新版本地址
                            downloadUrl = url;
                            //发现新版本  准备下载 下载中 下载成功 下载失败
                            //没有正在下载
                            if (!"准备下载".equals(versionTv.getText().toString()) && !versionTv.getText().toString().contains("下载中")) {
                                versionTv.setText("发现新版本");
                                checkVersionIv.setImageResource(R.drawable.download_icon);
                            }
                        }
                    } else {
                        versionTv.setText("版本：" + CommonUtil.getVersionName(MainActivity.this));
                        checkVersionIv.setImageResource(R.drawable.check_version_icon);
                    }
                }

                @Override
                public void onFirstLevelColumnDataChange(ArrayList<FirstLevelMenu> datas) {
                    if (firstLevelMenuList != null) {
                        if (errorLl.getVisibility() == View.VISIBLE) {
                            errorLl.setVisibility(View.GONE);
                        }

                        //只有栏目改变才需要重新绘制,只有图片变换不用重新绘制--可优化
                        for (int i = 0; i < firstLevelMenuList.size(); i++) {
                            if (datas != null && datas.size() == firstLevelMenuList.size()) {
                                if (datas.get(i).getKeyId() != firstLevelMenuList.get(i).getKeyId()) {
                                    firstLevelMenuList = datas;
                                    setFirstLevelMenuData(firstLevelMenuList);
                                    addColumnView(firstLevelMenuList, false);
                                    break;
                                } else {
                                    if (!CommonUtil.compareImg(datas.get(i).getImgUrl1(), firstLevelMenuList.get(i).getImgUrl1())
                                            && !CommonUtil.compareImg(datas.get(i).getImgUrl2(), firstLevelMenuList.get(i).getImgUrl2())
                                            && !CommonUtil.compareImg(datas.get(i).getImgUrl3(), firstLevelMenuList.get(i).getImgUrl3())) {
                                        firstLevelMenuList.get(i).setImgUrl1(datas.get(i).getImgUrl1());
                                        firstLevelMenuList.get(i).setImgUrl2(datas.get(i).getImgUrl2());
                                        firstLevelMenuList.get(i).setImgUrl3(datas.get(i).getImgUrl3());
                                    }
                                }
                            } else {
                                firstLevelMenuList = datas;
                                setFirstLevelMenuData(firstLevelMenuList);
                                addColumnView(firstLevelMenuList, false);
                                break;
                            }
                        }
                    }
                }

                @Override
                public void GetRollingNews(ArrayList<Program> datas) {
                    if (datas != null) {//比对数据keyId，数据改变才替换
                        if (marqueeDatas.size() != datas.size()) {
                            marqueeDatas = datas;
                            autoScrollTextView.setText(marqueeDatas.get(0).getProgramTitle());
                            autoScrollTextView.init(getWindowManager());
                            autoScrollTextView.startScroll();
                            lastMarqueeMsg = 0;
                        } else if (datas.size() == 0) {
                            return;
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

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };


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

                    Intent intent = new Intent(MainActivity.this, PlayVideoActivity.class);
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
                    Intent intent = new Intent(MainActivity.this, PlayVideoActivity.class);
                    intent.putExtra("KeyId", program.getKeyId());
                    intent.putExtra("videoUrl", program.getLinkUrl());
                    intent.putExtra("videoTitle", program.getProgramTitle());
                    intent.putExtra("videoThumb", program.getImgUrl());
                }

            });
        } else {
            if (TextUtils.isEmpty(program.getLinkUrl())) {
                Toast.makeText(MainActivity.this, "暂无详情页面数据", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(MainActivity.this, WebViewActivity.class)
                        .putExtra("linkUrl", program.getLinkUrl())
                        .putExtra("KeyId", program.getKeyId())
                );
            }
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
                    if (firstLevelMenuList.get(index).getKeyId() == -999) {//学校 进入学校节目页面
                        Intent intent = new Intent(MainActivity.this, SchoolMainActivity.class);
                        intent.putExtra("loginSchoolName", App.loginSchoolName);
                        intent.putExtra("loginName", App.loginName);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(MainActivity.this, ProgramActivity.class);
                        intent.putExtra("id", firstLevelMenuList.get(index).getKeyId());
                        intent.putExtra("columnName", firstLevelMenuList.get(index).getColumnName());
                        startActivity(intent);
                    }
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
                Glide.with(this).load(Constants.HOME_IMAGE_URL + item.getImgUrl1()).placeholder(R.drawable.default_img).into(first_item_iv);
            }
            columnFrameLayout.addView(first_item_iv);
            if (isFirstTimeAdd && i == 0) {
                first_item_iv.requestFocus();
            }
        }
    }

    /**
     * 计算图片坐标
     *
     * @param firstLevelMenuList
     */
    private void setFirstLevelMenuData(List<FirstLevelMenu> firstLevelMenuList) {
        //学校显示logo在第一个栏目数据 竖图
        if ("1".equals(App.SCHOOLTYPE)) {
            FirstLevelMenu firstLevelMenu = new FirstLevelMenu();
            firstLevelMenu.setDisplayStyle(2);
            firstLevelMenu.setKeyId(-999);
            String imgUrl = helper.getString(CommonValue.LOGO_URL, CommonValue.USER_NULL_DEFAULT);
            firstLevelMenu.setImgUrl1(imgUrl);
            firstLevelMenuList.add(0, firstLevelMenu);
        }

        for (int i = 0; i < firstLevelMenuList.size(); i++) {
            //DisplayStyle 1:横图  2：竖图 3：方图
            if (i == 0) {
                firstLevelMenuList.get(i).setX(0);
                firstLevelMenuList.get(i).setY(0);
            } else if (i == 1) {
                if (firstLevelMenuList.get(0).getDisplayStyle() == 1) {
                    firstLevelMenuList.get(i).setX(0);
                    firstLevelMenuList.get(i).setY(1);
                } else if (firstLevelMenuList.get(0).getDisplayStyle() == 2) {
                    firstLevelMenuList.get(i).setX(DensityUtil.dip2px(this, marginCount + shuTuWidth));
                    firstLevelMenuList.get(i).setY(0);
                } else if (firstLevelMenuList.get(0).getDisplayStyle() == 3) {
                    firstLevelMenuList.get(i).setX(0);
                    firstLevelMenuList.get(i).setY(1);
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
    }


    /**
     * 播放视频屏保 切换栏目图片
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
            case TURN_FIRST_IMAGE:
                if (columnFrameLayout.getChildCount() == firstLevelMenuList.size()) {
                    for (int i = 0; i < firstLevelMenuList.size(); i++) {
                        if (!TextUtils.isEmpty(firstLevelMenuList.get(i).getImgUrl2())) {
                            Glide.with(this).load(Constants.HOME_IMAGE_URL + firstLevelMenuList.get(i).getImgUrl2()).crossFade().into((ImageView) columnFrameLayout.getChildAt(i));
                        }
                    }
                }

                handler.sendEmptyMessageDelayed(TURN_SECOND_IMAGE, TURN_IMAGE_DELAY_TIME);
                break;
            case TURN_SECOND_IMAGE:
                if (columnFrameLayout.getChildCount() == firstLevelMenuList.size())
                    for (int i = 0; i < firstLevelMenuList.size(); i++) {
                        if (!TextUtils.isEmpty(firstLevelMenuList.get(i).getImgUrl3())) {
                            Glide.with(this).load(Constants.HOME_IMAGE_URL + firstLevelMenuList.get(i).getImgUrl3()).crossFade().into((ImageView) columnFrameLayout.getChildAt(i));
                        }
                    }

                handler.sendEmptyMessageDelayed(TURN_THIRD_IMAGE, TURN_IMAGE_DELAY_TIME);
                break;
            case TURN_THIRD_IMAGE:
                if (columnFrameLayout.getChildCount() == firstLevelMenuList.size())
                    for (int i = 0; i < firstLevelMenuList.size(); i++) {
                        if (!TextUtils.isEmpty(firstLevelMenuList.get(i).getImgUrl1()) && (!TextUtils.isEmpty(firstLevelMenuList.get(i).getImgUrl2()) || !TextUtils.isEmpty(firstLevelMenuList.get(i).getImgUrl3()))) {
                            Glide.with(this).load(Constants.HOME_IMAGE_URL + firstLevelMenuList.get(i).getImgUrl1()).crossFade().into((ImageView) columnFrameLayout.getChildAt(i));
                        }
                    }

                handler.sendEmptyMessageDelayed(TURN_FIRST_IMAGE, TURN_IMAGE_DELAY_TIME);
                break;
        }
        return true;
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.removeMessages(SHOW_SCREENSAVER);
            handler.removeMessages(TURN_FIRST_IMAGE);
            handler.removeMessages(TURN_SECOND_IMAGE);
            handler.removeMessages(TURN_THIRD_IMAGE);
        }

        Glide.with(this).pauseRequests();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //栏目页面返回
        if (handler != null) {
            handler.removeMessages(SHOW_SCREENSAVER);
            handler.sendEmptyMessageDelayed(SHOW_SCREENSAVER, Constants.SHOW_SCREENSAVE_DELAY_TIME);

            handler.sendEmptyMessageDelayed(TURN_FIRST_IMAGE, TURN_IMAGE_DELAY_TIME);
        }
        Glide.with(this).resumeRequests();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeMessages(SHOW_SCREENSAVER);
            handler.removeMessages(TURN_FIRST_IMAGE);
            handler.removeMessages(TURN_SECOND_IMAGE);
            handler.removeMessages(TURN_THIRD_IMAGE);
        }

        postLiveData(false);
        unregisterReceiver(receiver);
//        unregisterReceiver(mHomeKeyEventReceiver);
        unregisterReceiver(myReceiver);
        unbindService(conn);
        handler = null;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            handler.removeMessages(SHOW_SCREENSAVER);
            handler.sendEmptyMessageDelayed(SHOW_SCREENSAVER, Constants.SHOW_SCREENSAVE_DELAY_TIME);
        }
        return super.dispatchTouchEvent(ev);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 按返回键触发,后面的判断增强程序健壮性，点后退键的时候，为了防止点得过快，触发两次后退事件，故做此设置！
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (psw_set.getVisibility() == View.VISIBLE) {
                psw_set.setVisibility(View.GONE);
            } else {
                exit();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private long mPressedTime = 0;

    public void exit() {
        if (!isMyLauncherDefault()) {
            long mNowTime = System.currentTimeMillis();
            if (mNowTime - mPressedTime > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出", Toast.LENGTH_SHORT).show();
                mPressedTime = mNowTime;
            } else {
                finish();
            }
        } else {
            Toast.makeText(this, "当前已经是主页,如果需要退出,请'进入设置-应用-校园屏-清除默认设置'后再试", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isMyLauncherDefault() {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);

        List<IntentFilter> filters = new ArrayList<IntentFilter>();
        filters.add(filter);

        final String myPackageName = getPackageName();
        List<ComponentName> activities = new ArrayList<ComponentName>();
        final PackageManager packageManager = (PackageManager) getPackageManager();

        // You can use name of your package here as third argument
        packageManager.getPreferredActivities(filters, activities, null);

        for (ComponentName activity : activities) {
            if (myPackageName.equals(activity.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 上传活动数据
     *
     * @param isOnLine
     */
    private void postLiveData(boolean isOnLine) {
        if (isOnLine) {
            if (onLineTime > 1) {
                return;
            } else {
                onLineTime++;
            }
        } else {
            if (shutDownTime > 1) {
                return;
            } else {
                shutDownTime++;
            }
        }

        if (CommonUtil.getWifiMacAddress(this) != null) {
            RequestManager.getInstance().AddDviceRunningLog(
                    CommonUtil.getWifiMacAddress(this).replaceAll(":", "")
                    , isOnLine ? 1 : 0
                    , ""
                    , isOnLine ? "启动App" : "关闭App"
                    , new JsonCallback<BaseResponse<String>>() {
                        @Override
                        public void onSuccess(Response<BaseResponse<String>> response) {

                        }
                    }
            );
        }

        if (CommonUtil.getWifiMacAddress() != null) {
            RequestManager.getInstance().AddDviceRunningLog(
                    CommonUtil.getWifiMacAddress().replaceAll(":", "")
                    , isOnLine ? 1 : 0
                    , ""
                    , isOnLine ? "启动App" : "关闭App"
                    , new JsonCallback<BaseResponse<String>>() {
                        @Override
                        public void onSuccess(Response<BaseResponse<String>> response) {

                        }
                    });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.to_h5_tv:
                this.finish();
                break;
            case R.id.loginout_tv:
                if ("登录".equals(loginoutTv.getText().toString())) {//未登录状态--显示登录
                    etLoginUsername.requestFocus();
                    login_rl.setVisibility(View.VISIBLE);
                } else {//登录状态--退出
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle("提示")
                            .setMessage("确定退出登录?")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    isIntoSettingInstallApkLogout = 3;

                                    //如果是普通用户直接退出
                                    if ("3".equals(App.SCHOOLTYPE)) {//未登录 普通用户登录不需要输入密码
                                        logoutSetData();
                                    } else {
                                        psw_set.setVisibility(View.VISIBLE);
                                        content_edit.requestFocus();
                                    }
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            }).create();
                    dialog.show();
                }
                break;
            case R.id.school_name_tv: //学校跳到学校节目界面 点击机构名称直接跳转进入机构栏目页面
                if ("1".equals(App.SCHOOLTYPE)) {
                    Intent intent = new Intent(MainActivity.this, SchoolMainActivity.class);
                    intent.putExtra("loginSchoolName", App.loginSchoolName);
                    intent.putExtra("loginName", App.loginName);
                    startActivity(intent);
                } else if ("2".equals(App.SCHOOLTYPE)) {
                    Intent intent = new Intent(MainActivity.this, JGDWMainActivity.class);
                    intent.putExtra("loginName", App.loginName);
                    intent.putExtra("loginSchoolName", App.loginSchoolName);
                    startActivity(intent);
                }
                break;
            case R.id.login_cancel_btn:
                etLoginUsername.setText("");
                etLoginPwd.setText("");
                etLoginUsername.clearFocus();
                etLoginPwd.clearFocus();
                login_rl.setVisibility(View.GONE);
                break;
            case R.id.btnSendCode:
                if (TextUtils.isEmpty(etLoginUsername.getText().toString().trim())) {
                    Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!RegUtils.isPhonenum(etLoginUsername.getText().toString().trim())) {
                    Toast.makeText(this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                    return;
                }
                validcode = CommonUtil.getRanCode();
                RequestManager.getInstance().SendValidateCode(etLoginUsername.getText().toString().trim(), validcode, new JsonCallback<BaseResponse<String>>(this, true) {

                    @Override
                    public void onSuccess(Response<BaseResponse<String>> response) {
                        if (sendCodeHandler == null) {
                            sendCodeHandler = new Handler();
                        }
                        sendCodeHandler.post(runnable);
                        Toast.makeText(MainActivity.this, "验证码发送成功，请在手机上查看", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case R.id.login_submit_btn:
                if (TextUtils.isEmpty(etLoginUsername.getText().toString().trim())) {
                    Toast.makeText(this, "请输入登录账号", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (tab1.isChecked()) {
                    if (TextUtils.isEmpty(etLoginPwd.getText().toString().trim())) {
                        Toast.makeText(this, "请输入登录密码", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (etLoginPwd.getText().toString().trim().length() < 6 || etLoginPwd.getText().toString().trim().length() > 10) {
                        Toast.makeText(this, "密码长度为6-10位", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    RequestManager.getInstance().SchoolLogin(etLoginUsername.getText().toString(), etLoginPwd.getText().toString(), new JsonCallback<BaseResponse<String>>(MainActivity.this, true) {
                        @Override
                        public void onSuccess(Response<BaseResponse<String>> response) {
                            //{"status":"hx-001","result":"{ CloseAudioTimes: 11:05,11:10,11:15,11:30, SchoolName:学校测试账号，ImgUrl:uploadimages\/School\/065d2727-7f0e-412c-88c2-71bd756eb955.png#，SchoolType:1}"}
//                       {"status":"hx-001","result":"{ CloseAudioTimes: 11:05,11:10,11:15,11:30, SchoolName:学校测试账号，ImgUrl:uploadimages\/School\/065d2727-7f0e-412c-88c2-71bd756eb955.png#，SchoolType:1，ParentId:0}"}
                            String SchoolName = null;
                            String imgUrl = null;
                            String ParentId = null;
                            String str[] = response.body().result.replace("{ ", "").replace("}", "").split("，");
                            for (int i = 0; i < str.length; i++) {
                                if (str[i].contains("SchoolName")) {
                                    SchoolName = str[i].substring(str[i].indexOf("SchoolName:") + "SchoolName:".length());
                                }

                                if (str[i].contains("ImgUrl:")) {
                                    imgUrl = str[i].substring(str[i].indexOf("ImgUrl:") + "ImgUrl:".length());
                                }
                                //1：学校 2：机关
                                //如果是学校，直接在市级栏目最前面增动态加一个栏目，显示学校的图片和名称，点击直接显示学校的节目，不显示学校的栏目；
                                if (str[i].contains("SchoolType:")) {
                                    App.SCHOOLTYPE = str[i].substring(str[i].indexOf("SchoolType:") + "SchoolType:".length());

                                }

                                if (str[i].contains("ParentId:")) {
                                    ParentId = str[i].substring(str[i].indexOf("ParentId:") + "ParentId:".length());
                                }
                            }


                            school_name_tv.setVisibility(View.VISIBLE);
                            school_name_tv.setText(SchoolName);
                            loginoutTv.setText("退出");
                            App.loginName = etLoginUsername.getText().toString();
                            App.loginSchoolName = SchoolName;

                            helper.putString(CommonValue.LOGIN_NAME, App.loginName);
                            helper.putString(CommonValue.LOGIN_SCHOOL_NAME, App.loginSchoolName);
                            helper.putString(CommonValue.PASSWORD, etLoginPwd.getText().toString());
                            helper.putString(CommonValue.LOGO_URL, imgUrl);
                            helper.putString(CommonValue.SCHOOLTYPE, App.SCHOOLTYPE);
                            helper.putString(CommonValue.PARENTID, ParentId);

                            if ("2".equals(App.SCHOOLTYPE)) {
                                toProPage();
                            } else {
                                getFirstLevelData();
                            }

                            etLoginUsername.setText("");
                            etLoginPwd.setText("");
                            etLoginUsername.clearFocus();
                            etLoginPwd.clearFocus();
                            login_rl.setVisibility(View.GONE);
                        }

                    });
                } else {
                    if (!RegUtils.isPhonenum(etLoginUsername.getText().toString().trim())) {
                        Toast.makeText(this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(etLoginPwd.getText().toString().trim())) {
                        Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (etLoginPwd.getText().toString().trim().equals(validcode)) {
                        schoolMemberLogin();
                    } else {
                        Toast.makeText(this, "验证码错误", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.cancel_btn:
                psw_set.setVisibility(View.GONE);
                content_edit.setText("");
                content_edit.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                break;
            case R.id.submit_btn://1进入系统设置 2安装apk 3退出登录     清除密码
                if (isIntoSettingInstallApkLogout == 3) {
                    if (content_edit.getText().toString().equals(helper.getString(CommonValue.PASSWORD, ""))) {
                        logoutSetData();
                    } else {
                        content_edit.setText("");
                        Toast.makeText(this, "密码不正确", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (pswKey.equals(content_edit.getText().toString())) {//进入设置
                        if (isIntoSettingInstallApkLogout == 1) {
                            startActivity(new Intent(Settings.ACTION_SETTINGS));
                        } else if (isIntoSettingInstallApkLogout == 2) {//安装apk
                            CommonUtil.installApk(MainActivity.this, downloadPath);
                        }
                        content_edit.setText("");
                        content_edit.clearFocus();
                        psw_set.setVisibility(View.GONE);
                    } else {
                        content_edit.setText("");
                        Toast.makeText(this, "密码不正确", Toast.LENGTH_SHORT).show();
                    }
                }

                break;
            case R.id.setting_linear://系统设置
                isIntoSettingInstallApkLogout = 1;
                psw_set.setVisibility(View.VISIBLE);
                content_edit.requestFocus();
                break;
            case R.id.version_linear://发现新版本 准备下载 下载中 下载成功 下载失败
                if ("发现新版本".equals(versionTv.getText().toString())) {//下载新版本
                    versionTv.setText("准备下载...");
                    downloadFile(Constants.HOME_IMAGE_URL + downloadUrl, downloadPath);
                } else if (versionTv.getText().toString().contains("下载中")) {

                } else if ("下载成功".equals(versionTv.getText().toString())) {
                    needInstallApk();
                } else if ("下载失败".equals(versionTv.getText().toString())) {
                    versionTv.setText("准备下载...");
                    downloadFile(Constants.HOME_IMAGE_URL + downloadUrl, downloadPath);
                } else {//检查更新
                    startAnimtion();
                    getVersion();
                }
                break;
            case R.id.load_again_tv:
                errorLl.setVisibility(View.GONE);
                getFirstLevelData();
                break;
        }
    }

    /**
     * 普通用户登录
     */
    private void schoolMemberLogin() {
        RequestManager.getInstance().ScreenMemberLogin(etLoginUsername.getText().toString(), new JsonCallback<BaseResponse<SchoolMember>>(MainActivity.this, true) {
            @Override
            public void onSuccess(Response<BaseResponse<SchoolMember>> response) {
                SchoolMember member = response.body().handleResult;

                school_name_tv.setVisibility(View.VISIBLE);
                school_name_tv.setText(member.getClassName());
                loginoutTv.setText("退出");
                App.loginName = etLoginUsername.getText().toString();
                App.loginSchoolName = member.getClassName();
                App.SCHOOLTYPE = "3";

                helper.putString(CommonValue.LOGIN_NAME, App.loginName);
                helper.putString(CommonValue.LOGIN_SCHOOL_NAME, App.loginSchoolName);
//                                helper.putString(CommonValue.PASSWORD, etLoginPwd.getText().toString());
//                                helper.putString(CommonValue.LOGO_URL, member.getImgUrl());
                helper.putString(CommonValue.SCHOOLTYPE, App.SCHOOLTYPE);
                helper.putString(CommonValue.TOKEN, member.getTokenId());
//                                helper.putString(CommonValue.PARENTID, ParentId);

//                getFirstLevelData();

                etLoginUsername.setText("");
                etLoginPwd.setText("");
                etLoginUsername.clearFocus();
                etLoginPwd.clearFocus();
                login_rl.setVisibility(View.GONE);
            }

        });
    }

    private void logoutSetData() {
        loginoutTv.setText("登录");
        App.loginName = null;
        App.loginSchoolName = null;
        if (App.screenVideoUrls != null) {
            App.screenVideoUrls.clear();
        }
        school_name_tv.setVisibility(View.GONE);
        school_name_tv.setText(null);
        helper.putString(CommonValue.LOGIN_NAME, "");
        helper.putString(CommonValue.PASSWORD, "");
        helper.putString(CommonValue.LOGIN_SCHOOL_NAME, App.loginSchoolName);
        helper.putString(CommonValue.LOGO_URL, "");
        helper.putString(CommonValue.TOKEN, "");
        Toast.makeText(MainActivity.this, "成功退出登录", Toast.LENGTH_SHORT).show();
        content_edit.setText("");
        content_edit.clearFocus();
        psw_set.setVisibility(View.GONE);

        App.SCHOOLTYPE = "";
        helper.putString(CommonValue.SCHOOLTYPE, App.SCHOOLTYPE);
        getFirstLevelData();
    }


    //检查更新动画
    private void startAnimtion() {
        Animation lodingAnimation = AnimationUtils.loadAnimation(this, R.anim.loading);
        checkVersionIv.startAnimation(lodingAnimation);
    }

    private void stopAnimation(int delayTime) {
        checkVersionIv.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkVersionIv.clearAnimation();
            }
        }, delayTime);
    }

    private void downloadFile(final String url, String path) {
        RequestParams requestParams = new RequestParams(url);
        requestParams.setSaveFilePath(path);
        x.http().get(requestParams, new Callback.ProgressCallback<File>() {
            @Override
            public void onWaiting() {
            }

            @Override
            public void onStarted() {
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                versionTv.setText("下载中..." + current * 100 / total + "%");
            }

            @Override
            public void onSuccess(File result) {
                versionTv.setText("下载成功");
                Toast.makeText(MainActivity.this, "下载成功", Toast.LENGTH_SHORT).show();
                needInstallApk();
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
                versionTv.setText("下载失败");
                Toast.makeText(MainActivity.this, "请稍后再试或检查网络", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });
    }

    private void needInstallApk() {
        isIntoSettingInstallApkLogout = 2;
        psw_set.setVisibility(View.VISIBLE);
        content_edit.requestFocus();
    }


    public void getVersion() {
        RequestManager.getInstance().getNewVersion(new JsonCallback<BaseResponse<Page<Version>>>() {

            @Override
            public void onSuccess(Response<BaseResponse<Page<Version>>> response) {
                ArrayList<Version> versions = response.body().handleResult.rows;
                if (versions != null && versions.size() > 0) {
                    Version netVersion = versions.get(0);
                    if (check(netVersion)) {
                        downloadUrl = netVersion.getUrl();
                        versionTv.setText("发现新版本");
                        checkVersionIv.setImageResource(R.drawable.download_icon);
                    } else {
                        stopAnimation(1000);
                        Toast.makeText(MainActivity.this, "已是最新版本", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onError(Response<BaseResponse<Page<Version>>> response) {
                super.onError(response);
                stopAnimation(0);
            }
        });
    }

    private boolean check(Version netVersion) {
        if (netVersion != null) {
            return CommonUtil.getVersionCode(this) < netVersion.getCurVersion();
        }
        return false;
    }

}
