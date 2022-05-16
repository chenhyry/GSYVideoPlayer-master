package com.hxty.schoolnet.service;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.hxty.schoolnet.App;
import com.hxty.schoolnet.entity.BaseResponse;
import com.hxty.schoolnet.entity.FirstLevelMenu;
import com.hxty.schoolnet.entity.Page;
import com.hxty.schoolnet.entity.Program;
import com.hxty.schoolnet.entity.ScreenVideo;
import com.hxty.schoolnet.entity.Version;
import com.hxty.schoolnet.net.JsonCallback;
import com.hxty.schoolnet.net.RequestManager;
import com.hxty.schoolnet.receiver.ScreenOffAdminReceiver;
import com.hxty.schoolnet.utils.CommonUtil;
import com.hxty.schoolnet.utils.sphelper.CommonValue;
import com.hxty.schoolnet.utils.sphelper.ConfigHelper;
import com.lzy.okgo.model.Response;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * 检查版本 更新一级栏目数据 屏保 滚动新闻
 */
public class CheckDataService extends Service {

    private static final Long UPDATE_LIVE_INFO_DELAY = 5 * 1000L;
    private Handler handler;
    private Runnable runnable;
    private int time;
    private boolean isVolumeOn;
    private int flag = 1;
    private ConfigHelper helper;

    public interface OnVersionInfoChangeListener {
        void onVersionInfoChange(String url);

        void onFirstLevelColumnDataChange(ArrayList<FirstLevelMenu> datas);

        void GetRollingNews(ArrayList<Program> datas);
    }

    public OnVersionInfoChangeListener onVersionInfoChangeListener;

    public void setOnVersionInfoChangeListener(OnVersionInfoChangeListener onVersionInfoChangeListener) {
        this.onVersionInfoChangeListener = onVersionInfoChangeListener;
    }

    public CheckDataService() {
    }

    public class MyBinder extends Binder {
        //提供获取当前服务实例的方法
        public CheckDataService getService() {
            return CheckDataService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        helper = ConfigHelper.getDefaultConfigHelper(this);
        isVolumeOn = (getCurrentVolume() != 0);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(this);
                handler.postDelayed(this, UPDATE_LIVE_INFO_DELAY);

                if (time % 13 == 0) {//一分钟查询一次数据  12次 60秒
                    getVersion();
                    getFirstLevelColumnData();
                    getGetRollingNews();
                    getScreenVideo();
                    getCloseTime();
                    if (time == 13) {
                        time = 0;
                    }
                } else {
                    setVolumnScreen(time);//5秒查询一次时间 检查是否关闭开启屏幕声音
                }
                time++;
            }
        };

        handler.post(runnable);
        return new MyBinder();
    }


    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (handler != null) {
            handler.removeCallbacks(runnable);
            handler = null;
        }
        super.onDestroy();
    }

    public void getVersion() {
        RequestManager.getInstance().getNewVersion(new JsonCallback<BaseResponse<Page<Version>>>() {

            @Override
            public void onSuccess(Response<BaseResponse<Page<Version>>> response) {
                ArrayList<Version> versions = response.body().handleResult.rows;
                if (versions != null && versions.size() > 0) {
                    Version netVersion = versions.get(0);
                    if (check(netVersion)) {
                        if (onVersionInfoChangeListener != null) {
                            onVersionInfoChangeListener.onVersionInfoChange(netVersion.getUrl());
                        }
                    }
                } else {
                    if (onVersionInfoChangeListener != null) {
                        onVersionInfoChangeListener.onVersionInfoChange(null);
                    }
                }
            }
        });
    }

    private boolean check(Version netVersion) {
        if (netVersion != null) {
            if (CommonUtil.getVersionCode(this) < netVersion.getCurVersion()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 普通用户登录不传参数 获取平台滚动新闻
     */
    public void getGetRollingNews() {
        RequestManager.getInstance().GetRollingNews(App.loginName, new JsonCallback<BaseResponse<Page<Program>>>() {

            @Override
            public void onSuccess(Response<BaseResponse<Page<Program>>> response) {
                ArrayList<Program> datas = response.body().handleResult.rows;
                if (datas != null) {
                    if (onVersionInfoChangeListener != null) {
                        onVersionInfoChangeListener.GetRollingNews(datas);
                    }
                }
            }
        });
    }

    private void getFirstLevelColumnData() {
        RequestManager.getInstance().GetFirstLevelColumns(new JsonCallback<BaseResponse<Page<FirstLevelMenu>>>() {
            @Override
            public void onSuccess(Response<BaseResponse<Page<FirstLevelMenu>>> response) {
                ArrayList<FirstLevelMenu> datas = response.body().handleResult.rows;
                if (datas != null && datas.size() > 0) {
                    if (onVersionInfoChangeListener != null) {
                        onVersionInfoChangeListener.onFirstLevelColumnDataChange(datas);
                    }
                }
            }
        });
    }

    /***
     * 获取屏保数据
     */
    private void getScreenVideo() {
        //学校的要单独多调用一次平台视频接口  平台和学校2个
        if ("1".equals(App.SCHOOLTYPE)) {
            RequestManager.getInstance().GetScreenVideo(null, new JsonCallback<BaseResponse<Page<ScreenVideo>>>() {
                @Override
                public void onSuccess(Response<BaseResponse<Page<ScreenVideo>>> response) {
                    if (response.body().handleResult == null) {

                        RequestManager.getInstance().GetScreenVideo(App.loginName, new JsonCallback<BaseResponse<Page<ScreenVideo>>>() {
                            @Override
                            public void onSuccess(Response<BaseResponse<Page<ScreenVideo>>> response) {
                                if (App.screenVideoUrls != null) {
                                    App.screenVideoUrls.clear();
                                }
                                if (response.body().handleResult != null) {
                                    Page<ScreenVideo> screenVideo = response.body().handleResult;
                                    if (screenVideo.getTotal() > 0) {
                                        for (int i = 0; i < screenVideo.getRows().size(); i++) {
                                            App.screenVideoUrls.add(screenVideo.getRows().get(i).getLinkUrl());
                                        }
                                    }
                                }
                            }
                        });
                    } else {
                        if (App.screenVideoUrls != null) {
                            App.screenVideoUrls.clear();
                        }
                        Page<ScreenVideo> screenVideo = response.body().handleResult;
                        if (screenVideo.getTotal() > 0) {
                            for (int i = 0; i < screenVideo.getRows().size(); i++) {
                                App.screenVideoUrls.add(screenVideo.getRows().get(i).getLinkUrl());
                            }
                        }

                        RequestManager.getInstance().GetScreenVideo(App.loginName, new JsonCallback<BaseResponse<Page<ScreenVideo>>>() {
                            @Override
                            public void onSuccess(Response<BaseResponse<Page<ScreenVideo>>> response) {
                                if (response.body().handleResult != null) {
                                    Page<ScreenVideo> screenVideo = response.body().handleResult;
                                    if (screenVideo.getTotal() > 0) {
                                        for (int i = 0; i < screenVideo.getRows().size(); i++) {
                                            App.screenVideoUrls.add(screenVideo.getRows().get(i).getLinkUrl());
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            });
        } else {
            RequestManager.getInstance().GetScreenVideo(App.loginName, new JsonCallback<BaseResponse<Page<ScreenVideo>>>() {
                @Override
                public void onSuccess(Response<BaseResponse<Page<ScreenVideo>>> response) {
                    if (App.screenVideoUrls != null) {
                        App.screenVideoUrls.clear();
                    }
                    if (response.body().handleResult != null) {
                        Page<ScreenVideo> screenVideo = response.body().handleResult;
                        if (screenVideo.getTotal() > 0) {
                            for (int i = 0; i < screenVideo.getRows().size(); i++) {
                                App.screenVideoUrls.add(screenVideo.getRows().get(i).getLinkUrl());
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * 未登录 普通用户登录不获取关闭屏幕时间
     */
    public void getCloseTime() {
        if (!"1".equals(App.SCHOOLTYPE) && !"2".equals(App.SCHOOLTYPE)) {
            return;
        }
        RequestManager.getInstance().SchoolLogin(helper.getString(CommonValue.LOGIN_NAME, ""), helper.getString(CommonValue.PASSWORD, ""), new JsonCallback<BaseResponse<String>>() {
            @Override
            public void onSuccess(Response<BaseResponse<String>> response) {
                //{ CloseAudioTimes: 10:00,10:05,10:37,10:42,10:50,11:30, SchoolName:测试学校账号，ImgUrl:uploadimages/School/3c7b7b0a-f4ac-4f03-adbe-cced42d41d3b.jpg#，SchoolType:1，ParentId:0}
                String result = response.body().result;
                if (result.contains("CloseAudioTimes") && result.contains(",")) {
                    String CloseAudioTimes = result.substring(result.indexOf("CloseAudioTimes:") + "CloseAudioTimes:".length(), result.lastIndexOf(","));
                    if (!TextUtils.isEmpty(CloseAudioTimes)) {
                        CloseAudioTimes = CloseAudioTimes.trim();
                    }
                    String oldClose = helper.getString(CommonValue.CloseAudioTimes, "");
                    if (!oldClose.equals(CloseAudioTimes)) {
                        helper.putString(CommonValue.CloseAudioTimes, CloseAudioTimes);
                    }
                }
            }
        });
    }

    /**
     * 发送广播
     *
     * @param
     */
    public void setVolumnScreen(int time) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);

        //----------------7:00-17:00关屏幕声音 周末全天关闭
        if (calendar.get(Calendar.DAY_OF_WEEK) == 7 || calendar.get(Calendar.DAY_OF_WEEK) == 1) {
            if (time == 12) {
                if (!App.islockScreen) {
                    openVolumn(false);
                    App.islockScreen = true;
                    turnOff();
//                    Log.e("islockScreen", "关声音关屏幕");
                }
            }
            return;
        }
        if (hour < 7 || (hour == 7 && min <= 30)) {
            if (time == 12) {
                if (!App.islockScreen) {
                    openVolumn(false);
                    App.islockScreen = true;
                    turnOff();
//                    Log.e("islockScreen", "关声音关屏幕");
                }
            }
            return;
        } else if (hour >= 17) {
            if (time == 12) {
                if (!App.islockScreen) {
                    App.islockScreen = true;
                    openVolumn(false);
                    turnOff();
//                    Log.e("islockScreen", "关声音关屏幕");
                }
            }
            return;
        } else {
            if (time == 12) {
                if (App.islockScreen) {//特殊情况没亮屏
//                    Log.e("islockScreen", "开屏幕");
                    turnOnScreen();
                }
            }
        }

        String timeStr = helper.getString(CommonValue.CloseAudioTimes, null);

        if (TextUtils.isEmpty(timeStr)) {
            if (flag == 1) {
                openVolumn(true);
                flag = 0;
            }
            return;
        }
        flag = 1;

        String splitStr = null;//兼容多种输入的符号进行分割 只能数同一种
        if (timeStr.contains(",")) {
            splitStr = ",";
        } else if (timeStr.contains(";")) {
            splitStr = ";";
        } else if (timeStr.contains("；")) {
            splitStr = "；";
        }
        if (splitStr == null || !timeStr.contains(splitStr) || !timeStr.contains(":")) {
            return;
        }

        String[] timeArr = timeStr.split(splitStr);
        try {
            if (time == 12) {//1分查询一次声音
                isVolumeOn = (getCurrentVolume() != 0);
            }

            for (int i = 0; i < timeArr.length; i++) {
                String[] hourmin = timeArr[i].split(":");
                if (hour < Integer.parseInt(hourmin[0]) ||
                        (hour == Integer.parseInt(hourmin[0]) && min <= Integer.parseInt(hourmin[1]))) {
                    if (i == 0) {//小于第一个时间----需要保持关
                        if (hour == Integer.parseInt(hourmin[0]) && min == Integer.parseInt(hourmin[1])) {//关声音
                            if (isVolumeOn) {
//                                Log.e("关声音<", timeArr[i]);
                                openVolumn(false);
                                Toast.makeText(this, "关声音", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (!isVolumeOn) {
//                                Log.e("开声音<", timeArr[i]);
                                openVolumn(true);
                                Toast.makeText(this, "开声音", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        if (i % 2 != 0) {//需要关声音
                            if (isVolumeOn) {
//                                Log.e("关声音<", timeArr[i]);
                                openVolumn(false);
                                Toast.makeText(this, "关声音", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (hour == Integer.valueOf(hourmin[0]) && min == Integer.valueOf(hourmin[1])) {//关声音
                                if (isVolumeOn) {
//                                    Log.e("关声音<", timeArr[i]);
                                    openVolumn(false);
                                    Toast.makeText(this, "关声音", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                if (!isVolumeOn) {
//                                    Log.e("开声音<", timeArr[i]);
                                    openVolumn(true);
                                    Toast.makeText(this, "开声音", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                    break;
                } else {//判断是否比最后一个时间大 放学后开声音
                    if (i == timeArr.length - 1) {
                        if (hour <= 17) {//黑屏之前
                            if (!isVolumeOn) {
                                openVolumn(true);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {

        }

    }

    /**
     * 判断是否黑屏
     *
     * @param
     * @return
     */
    public void openVolumn(boolean isNeedON) {
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (isNeedON) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, helper.getInt(CommonValue.CURRENT_VOLUME, 2), AudioManager.FLAG_PLAY_SOUND);
        } else {
            int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (currentVolume == 0) {
                currentVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 3;
            }
            helper.putInt(CommonValue.CURRENT_VOLUME, currentVolume);
//            Log.e(CommonValue.CURRENT_VOLUME, currentVolume + "");
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_PLAY_SOUND);
        }

        isVolumeOn = isNeedON;
    }

    public int getCurrentVolume() {
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }


    //锁屏
    private PowerManager.WakeLock mWakeLock;

    public void turnOff() {
        //防止系统自动关屏
        setLockPatternEnabled(false);
        //关闭系统锁屏
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
        lock.disableKeyguard();

        ComponentName adminReceiver = new ComponentName(this, ScreenOffAdminReceiver.class);
        DevicePolicyManager policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        boolean admin = policyManager.isAdminActive(adminReceiver);
        if (admin) {
            //熄灭屏幕
            policyManager.lockNow();
        } else {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, new ComponentName(this, ScreenOffAdminReceiver.class));
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "需要开启");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Toast.makeText(this, "没有设备管理权限，无法根据设定休眠时间关屏幕", Toast.LENGTH_LONG).show();
        }
    }

    public void setLockPatternEnabled(boolean enabled) {
        setBoolean(android.provider.Settings.System.LOCK_PATTERN_ENABLED,
                enabled);
    }

    private void setBoolean(String systemSettingKey, boolean enabled) {
        android.provider.Settings.System.putInt(getContentResolver(),
                systemSettingKey, enabled ? 1 : 0);
    }

    public void turnOnScreen() {
        // turn on screen
        PowerManager mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP //该flag使能屏幕关闭时，也能点亮屏幕（通常的wakelock只能维持屏幕处于一直开启状态，如果灭屏时，是不会自动点亮的）
                        | PowerManager.SCREEN_DIM_WAKE_LOCK
                        | PowerManager.ON_AFTER_RELEASE, "screenOnWakeLock");
        mWakeLock.acquire();
        mWakeLock.release();
//        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE);
//        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
//        lock.reenableKeyguard();//开启系统锁屏
    }
}
