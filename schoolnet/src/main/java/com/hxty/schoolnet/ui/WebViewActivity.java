package com.hxty.schoolnet.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.hxty.schoolnet.R;
import com.hxty.schoolnet.entity.BaseResponse;
import com.hxty.schoolnet.net.JsonCallback;
import com.hxty.schoolnet.net.RequestManager;
import com.hxty.schoolnet.utils.DensityUtil;
import com.lzy.okgo.model.Response;

/**
 * 图文节目
 */
public class WebViewActivity extends BaseActivity implements View.OnClickListener {

    private WebView mWebView;
    private ProgressBar progressBar;
    private LinearLayout backLl;
    private RelativeLayout id_toolbar;
    private int KeyId;
    private String url;
    private String title;
    private Handler handler;
    private static final int GOTOMAINVIEW = 1;
    private static final int STAY_TIME = 5 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        KeyId = getIntent().getIntExtra("KeyId", -1);
        url = getIntent().getStringExtra("linkUrl");
        title = getIntent().getStringExtra("title");
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == GOTOMAINVIEW) {
                    finish();
                }
            }
        };
        initView();
        hideNavigationBar();
    }

    private void initView() {
        progressBar = findViewById(R.id.progressBar);
        backLl = findViewById(R.id.backLl);
        id_toolbar = findViewById(R.id.id_toolbar);

        if (backLl.getVisibility() == View.VISIBLE) {
            int leftMargin = (DensityUtil.getScreenWidth(this) - 1000) / 2 - DensityUtil.dip2px(this, 15) - DensityUtil.dip2px(this, 38);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) backLl.getLayoutParams();
            layoutParams.leftMargin = leftMargin;
            backLl.setLayoutParams(layoutParams);
        }

        if (!DensityUtil.isPhone(this)) {
            mWebView = findViewById(R.id.webView);
        } else {
            mWebView = findViewById(R.id.webView2);
        }
        mWebView.setVisibility(View.VISIBLE);


        mWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                handler.removeMessages(GOTOMAINVIEW);
                handler.sendEmptyMessageDelayed(GOTOMAINVIEW, STAY_TIME);
                return false;
            }
        });
        backLl.setOnClickListener(this);
        if (id_toolbar != null) {
            id_toolbar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WebViewActivity.this.finish();
                }
            });
        }
        initWebView(mWebView);
        handler.sendEmptyMessageDelayed(GOTOMAINVIEW, STAY_TIME);
    }

    private void initWebView(final WebView webView) {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);//支持javascript脚本
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setAllowFileAccess(true); //允许访问文件
        webSettings.setDefaultTextEncodingName("utf-8");
//        webSettings.setBlockNetworkImage(false);//把图片加载放在最后来加载渲染
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setSupportZoom(true); // 不支持缩放
        webSettings.setBuiltInZoomControls(false); // 设置不显示缩放按钮
        /**
         * 用WebView显示图片，可使用这个参数 设置网页布局类型：
         * 1、LayoutAlgorithm.NARROW_COLUMNS ：适应内容大小
         * 2、LayoutAlgorithm.SINGLE_COLUMN : 适应屏幕，内容将自动缩放
         */
//        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setUseWideViewPort(true);//Tell the WebView to use the wide viewport
        webSettings.setLoadWithOverviewMode(true);//和setUseWideViewPort(true)一起解决网页自适应问题

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setHorizontalScrollBarEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                progressBar.setVisibility(View.VISIBLE);// 显示进度条
                return false;
            }
        });
        webView.setWebChromeClient(mChromeClient);
    }

    private WebChromeClient mChromeClient = new WebChromeClient() {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            progressBar.setProgress(newProgress);
            if (newProgress >= 90) {
                if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.GONE);
                    updateClickData();
                }
            }
        }
    };


    /**
     * 上传增加点击次数
     */
    public void updateClickData() {
        //增加节目点击次数
        RequestManager.getInstance().AddProgramClickNum(KeyId + "", new JsonCallback<BaseResponse<String>>() {
            @Override
            public void onSuccess(Response<BaseResponse<String>> response) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(url)) {
            mWebView.loadUrl(url);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.loadUrl("about:blank");
    }

    @Override
    protected void onDestroy() {
        ((RelativeLayout) mWebView.getParent()).removeView(mWebView);
        mWebView.removeAllViews();
        mWebView.destroy();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.backLl) {
            this.finish();
        }
    }

}
