package com.hxty.schoolnet.utils.sphelper;

import android.os.Environment;

/**
 * 说明：配置信息常量
 */
public class CommonConfigEntry {

    public static boolean LOG_LOGCAT = true;// 日志输出到控制台
    public static boolean LOG_DEBUG = true;// 记录调试日志
    public static boolean LOG_OUTFILE = true;// 日志输出到文件
    public static int LOG_MAXSIZE = 100;// 日志最大容量，单位：m(1024 * 1024)
    public static int LOG_MAX_FILES = 10;// 日志最大文件数量
    public static String LOG_FILEPATH = Environment.getExternalStorageDirectory() + "/Shizhifengyun/";// 日志输出路径
    public static String LOG_NAME = "tbrf.log";// 日志名称
    public static String LOG_SYSTEM_NAME = "system.log";// 系统日志名称


}
