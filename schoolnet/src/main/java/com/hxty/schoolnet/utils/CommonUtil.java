package com.hxty.schoolnet.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.content.FileProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by chen on 2016/10/14.
 */

public class CommonUtil {

    /**
     * 判断当前应用是否是debug状态
     */
    public static boolean isApkInDebug(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static int getVersionCode(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            return info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getVersionName(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    public static String getMyUUID(Context context) {
        return getLocalMacAddressFromIp(context);
    }

    //根据IP获取本地Mac
    public static String getLocalMacAddressFromIp(Context context) {
        String mac_s = "";
        try {
            byte[] mac;
            NetworkInterface ne = NetworkInterface.getByInetAddress(InetAddress.getByName(getLocalIpAddress()));
            mac = ne.getHardwareAddress();
            mac_s = byte2hex(mac);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mac_s;
    }

    public static String byte2hex(byte[] b) {
        StringBuffer hs = new StringBuffer(b.length);
        String stmp = "";
        int len = b.length;
        for (int n = 0; n < len; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            if (stmp.length() == 1)
                hs = hs.append("0").append(stmp);
            else {
                hs = hs.append(stmp);
            }
        }
        return String.valueOf(hs);
    }

    //获取本地IP
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
//            Log.e("WifiPreference IpAddress", ex.toString());
        }
        return null;
    }

    /**
     * 7天内新发布的作品
     *
     * @param finishTime
     * @return
     */
    public static boolean isInSevenDays(String finishTime) {
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            Date date = fmt.parse(finishTime);
            long finishT = date.getTime();
            if (System.currentTimeMillis() - finishT < 7 * 24 * 60 * 60 * 1000) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 安装apk
     */
    public static void installApk(Activity activity, String downloadPath) {
        if (Build.VERSION.SDK_INT >= 24) { //判断版本是否在7.0以上
            File file = new File(downloadPath);
            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
            Uri apkUri = FileProvider.getUriForFile(activity, "com.hxty.schoolnet.fileprovider", file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            // 由于没有在Activity环境下启动Activity,设置下面的标签
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            activity.startActivity(intent);
        } else {
            DataCleanManager.cleanSharedPreference(activity.getApplicationContext());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(new File(downloadPath)), "application/vnd.android.package-archive");
            activity.startActivity(intent);
        }
    }

    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    public static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }


    /**
     * 查询网卡（网线）Mac
     * @return
     */
    public static String getWifiMacAddress() {
        String macAddress = null;
        String str = "";
        try {
            //linux下查询网卡mac地址的命令
//            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address");
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/eth0/address");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macAddress = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return macAddress;
    }

    public static String getWifiMacAddress(Context context) {
        String macAddress = null;
        WifiManager wifiManager =
                (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = (null == wifiManager ? null : wifiManager.getConnectionInfo());

        if (!wifiManager.isWifiEnabled()) {
            //必须先打开，才能获取到MAC地址
            wifiManager.setWifiEnabled(true);
            wifiManager.setWifiEnabled(false);
        }

        if (null != info) {
            macAddress = info.getMacAddress() + ".." + getWifiMacAddress();
        }
        return macAddress;
    }

    /**
     * 生成验证码
     *
     * @return
     */
    public static String getRanCode() {
        /*Random random = new Random();
        return random.nextInt(max)%(max-min+1) + min;*/
        /*String[] beforeShuffle = new String[]
                {"0", "1", "2", "3", "4", "5", "6", "7",
				  "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
				  "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
				  "W", "X", "Y", "Z" };  */
        String[] beforeShuffle = new String[]{"0", "1", "2", "3", "4", "5", "6", "7",
                "8", "9"};
        List list = Arrays.asList(beforeShuffle);
        Collections.shuffle(list);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
        }
        String afterShuffle = sb.toString();
        return afterShuffle.substring(5, 9);
    }

    public static boolean compareImg(String url1, String url2) {
        if (url1 == null && url2 == null) {
            return true;
        } else if (url1 != null && url2 != null) {
            return url1.equals(url2);
        } else {
            return false;
        }
    }
}
