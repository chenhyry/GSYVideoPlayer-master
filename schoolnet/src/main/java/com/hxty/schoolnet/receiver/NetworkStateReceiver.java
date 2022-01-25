package com.hxty.schoolnet.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

/**
 * 监听网络状态广播
 */
public class NetworkStateReceiver extends BroadcastReceiver {

    public static final IntentFilter FILTER = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

    public static final int NO_NET = 0x00;
    public static final int NET_WIFI = 0x01;
    public static final int NET_CMWAP = 0x02;
    public static final int NET_CMNET = 0x03;

    private NetStateChangeCallBack netStateChangeCallBack;

    public void setNetStateChangeCallBack(NetStateChangeCallBack netStateChangeCallBack) {
        this.netStateChangeCallBack = netStateChangeCallBack;
    }

    public interface NetStateChangeCallBack {

        void netStateChange(int state);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(netStateChangeCallBack != null){
            netStateChangeCallBack.netStateChange(getNetworkType(context));
        }
    }

    /**
     * 获取当前网络类型
     *
     * @return 0：没有网络   1：WIFI网络   2：WAP网络    3：NET网络
     */
    public int getNetworkType(Context context) {
        int netType = NO_NET;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE) {
            String extraInfo = networkInfo.getExtraInfo();
            if (!TextUtils.isEmpty(extraInfo)) {
                if (extraInfo.toLowerCase().equals("cmnet")) {
                    netType = NET_CMNET;
                } else {
                    netType = NET_CMWAP;
                }
            }
        } else if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = NET_WIFI;
        }
        return netType;
    }
}
