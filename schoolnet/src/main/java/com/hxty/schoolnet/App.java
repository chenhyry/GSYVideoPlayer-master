package com.hxty.schoolnet;

import android.app.Application;
import android.content.Context;

import androidx.annotation.Nullable;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.hxty.schoolnet.net.Constants;
import com.hxty.schoolnet.utils.CommonUtil;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.https.HttpsUtils;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;

import org.xutils.x;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import tv.danmaku.ijk.media.exo2.ExoMediaSourceInterceptListener;
import tv.danmaku.ijk.media.exo2.ExoSourceManager;

public class App extends MultiDexApplication {

    private static App instance;

    public static int currentPlayIndex = 0;
    public static List<String> screenVideoUrls = new ArrayList<>();

    public static String loginName;
    public static String loginSchoolName;
    public static String SCHOOLTYPE;//1学校 2机构机关 3普通用户
    public static boolean islockScreen;//屏幕是否锁定

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        x.Ext.init(this);

        // 获取当前包名
        String packageName = getApplicationContext().getPackageName();
        // 获取当前进程名
        String processName = CommonUtil.getProcessName(android.os.Process.myPid());
        if (processName == null || processName.equals(packageName)) {
            instance = this;
            initBugly();
            initOkGo();
            initPlayer();
            UMConfigure.preInit(this, "58ef1ba9a3251105ed0006fe", "umeng");
            UMConfigure.init(this, "58ef1ba9a3251105ed0006fe", "umeng", UMConfigure.DEVICE_TYPE_PHONE, "");
        }
    }

    private void initPlayer() {
        //因为忽略证书会导致一些 Google Play 的审核问题所以改为自定义支持
        //如果需要使用 SkipSSLChain ，可以参考 demo 里面的 exosource
        //另外通过 getHttpDataSourceFactory 也可以自定义需要的 HttpDataSource 逻辑
        ExoSourceManager.setExoMediaSourceInterceptListener(new ExoMediaSourceInterceptListener() {
            @Override
            public MediaSource getMediaSource(String dataSource, boolean preview, boolean cacheEnable,
                                              boolean isLooping, File cacheDir) {
                //如果返回 null，就使用默认的
                return null;
            }

            @Override
            public DataSource.Factory getHttpDataSourceFactory(String userAgent, @Nullable TransferListener listener,
                                                               int connectTimeoutMillis, int readTimeoutMillis,

                                                               Map<String, String> mapHeadData, boolean allowCrossProtocolRedirects) {
                //如果返回 null，就使用默认的
                return null;
            }

        });
    }

    /**
     * Okgo网络请求
     */
    private void initOkGo() {
        //日志
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);//log打印级别，决定了log显示的详细程度
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if (CommonUtil.isApkInDebug(instance)) {
            builder.addInterceptor(loggingInterceptor);
        }

        //自动管理cookie（或者叫session的保持），以下几种任选其一就行
        //builder.cookieJar(new CookieJarImpl(new SPCookieStore(this)));            //使用sp保持cookie，如果cookie不过期，则一直有效
//        builder.cookieJar(new CookieJarImpl(new DBCookieStore(this)));              //使用数据库保持cookie，如果cookie不过期，则一直有效
        //builder.cookieJar(new CookieJarImpl(new MemoryCookieStore()));            //使用内存保持cookie，app退出后，cookie消失

        //超时时间设置，默认60秒
        builder.readTimeout(10000, TimeUnit.MILLISECONDS);      //全局的读取超时时间
        builder.writeTimeout(10000, TimeUnit.MILLISECONDS);     //全局的写入超时时间
        builder.connectTimeout(10000, TimeUnit.MILLISECONDS);   //全局的连接超时时间

        //验证 -- 升级接口不能验证
        HttpsUtils.SSLParams sslParams4 = null;
        try {
            //方法一：信任所有证书,不安全有风险
            sslParams4 = HttpsUtils.getSslSocketFactory();
            //方法二：自定义信任规则，校验服务端证书
//        HttpsUtils.SSLParams sslParams2 = HttpsUtils.getSslSocketFactory(new SafeTrustManager());
            //方法三：使用预埋证书，校验服务端证书（自签名证书）
//            sslParams4 = HttpsUtils.getSslSocketFactory(getAssets().open("service.cer"));
            //方法四：使用bks证书和密码管理客户端证书（双向认证），使用预埋证书，校验服务端证书（自签名证书）
//            sslParams4 = HttpsUtils.getSslSocketFactory(getAssets().open("client.bks"), "bjxypserver", getAssets()
//            .open("serverxyp.cer"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        builder.sslSocketFactory(sslParams4.sSLSocketFactory, sslParams4.trustManager);

        //配置https的域名匹配规则，详细看demo的初始化介绍，不需要就不要加入，使用不当会导致https握手失败
        builder.hostnameVerifier(new SafeHostnameVerifier());
        OkGo.getInstance().init(this)                       //必须调用初始化
                .setOkHttpClient(builder.build())               //建议设置OkHttpClient，不设置将使用默认的
                .setCacheMode(CacheMode.NO_CACHE)               //全局统一缓存模式，默认不使用缓存，可以不传
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)   //全局统一缓存时间，默认永不过期，可以不传
                .setRetryCount(0);                          //全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0
//                .addCommonHeaders(headers)                      //全局公共头
//                .addCommonParams(params);                       //全局公共参数
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private class SafeHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            //验证主机名是否匹配
//            return hostname.equals(Constants.BASE_HOST);
            return true;
        }
    }

    /**
     * Bugly上报
     */
    private void initBugly() {
        CrashReport.initCrashReport(getApplicationContext(), "4af17ea85e", true);
    }

    public static App getInstance() {
        return instance;
    }

    {
        PlatformConfig.setWeixin("wx3ef833d0b36da1d2", "2eeffe3dd1fe022da2012a029afd96ab");
    }


}
