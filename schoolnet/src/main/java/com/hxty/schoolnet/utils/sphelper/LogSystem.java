package com.hxty.schoolnet.utils.sphelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * 系统日志记录
 */
public class LogSystem {
    /**
     * (static) Default maximum log file size
     */
    public static int MAX_SIZE = 40; // MB
    /**
     * 日志抓取开关
     */
    private static boolean running;
    /**
     * The log output stream
     */
    private static PrintStream out_stream;

    /**
     * 记录日志线程
     */
    private static Thread logThread = null;

    /**
     * 日志文件
     */
    private static File logFile = null;

    /**
     * The log input stream
     */
    private static FileInputStream input_stream = null;

    /**
     * 超出最大日志大小的日志备份文件
     */
    private static String bakLogName = ".bak";

    /**
     * 主控单元控制PoC SDK设置日志文件存储路径
     *
     * @param szLogPath 日志路径(路径，不带文件名)
     * @param nMaxSize  最大容量，单位：M
     * @return ServiceConstant.METHOD_SUCCESS：成功调用；
     * ServiceConstant.METHOD_FAILED：方法调用失败；
     * ServiceConstant.PARAM_ERROR：参数错误。
     */
    public static int startLogSystemService(String szLogPath, int nMaxSize) {
        Log.debug("LogSystem", "setLogPath:: szLogPath:" + szLogPath + " nMaxSize:" + nMaxSize);
        CommonConfigEntry.LOG_FILEPATH = szLogPath;
        CommonConfigEntry.LOG_MAXSIZE = nMaxSize;
        startLogFile();
        return CommonConstantEntry.METHOD_SUCCESS;
    }

    /**
     * 主控单元控制PoC SDK设置系统日志文件存储路径
     * AndroidManifest.xml需要增加：
     * <uses-permission android:name="android.permission.READ_LOGS"/>
     *
     * @param bOn 系统日志记录开关
     * @return ServiceConstant.METHOD_SUCCESS：成功调用；
     * ServiceConstant.METHOD_FAILED：方法调用失败；
     * ServiceConstant.PARAM_ERROR：参数错误。
     */
    public static int logSystem(boolean bOn) {
        Log.debug("LogSystem", "logSystem:: bOn:" + bOn);
        if (bOn) {// 打开
            startLogFile();
        } else {// 关闭
            stopLogFile();
        }
        return CommonConstantEntry.METHOD_SUCCESS;
    }

    /**
     * 开启记录日志文件
     */
    public static void startLogFile() {
        if (logThread != null) {
            return;
        }

        initLog();// 初始化

        running = true;

        logThread = new Thread() {
            @Override
            public void run() {
                startSystemLog();// 启动记录系统日志
                logThread = null;// 结束日志输出置空
            }
        };
        logThread.start();
    }

    /**
     * 记录日志文件
     */
    private static void writeLogFile(String logStr) {

        int count = 0;// 计数器
        try {
            if (CommonConfigEntry.LOG_OUTFILE) {// 记录日志文件
                checkLogExists();// 检查日志文件是否存在，不存在则重新创建
                if (out_stream != null)
                    out_stream.print(logStr);// 写入
                if (count % 100 == 0) {// 每n次检查一次日志
                    checkLogSize();// 检查日志大小
                }
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查日志文件是否存在，如果不存在则创建
     */
    private static void checkLogExists() {
        if (!logFile.exists()) {// 日志文件不存在（被删除）
            try {
                if (out_stream != null)
                    out_stream.close();// 关闭输出流

                if (input_stream != null)
                    input_stream.close();// 关闭输入流

                initLog();// 重新初始化日志
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检查日志文件大小，防止日志文件过大占用存储空间
     */
    private static void checkLogSize() {
        try {
            int size = input_stream.available() / (1024 * 1024);// 大小

            if (size >= MAX_SIZE) {// 超出大小
                logFile.renameTo(new File(CommonConfigEntry.LOG_FILEPATH + CommonConfigEntry.LOG_NAME + bakLogName));// 改名
                logFile.delete();
                initLog();// 重新初始化
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化日志处理
     */
    private static void initLog() {
        try {
            MAX_SIZE = CommonConfigEntry.LOG_MAXSIZE;// 设置日志大小
            new File(CommonConfigEntry.LOG_FILEPATH).mkdirs();// 创建目录
            logFile = new File(CommonConfigEntry.LOG_FILEPATH + CommonConfigEntry.LOG_SYSTEM_NAME);
            if (!logFile.exists()) {// 日志文件不存在，则创建
                logFile.createNewFile();
            }
            // 追加写入，自动flush
            out_stream = new PrintStream(new FileOutputStream(logFile, true), true);
            // 日志读取流，用于获取日志文件大小
            input_stream = new FileInputStream(logFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止记录日志文件
     */
    public static void stopLogFile() {
        running = false;

        if (out_stream != null) {
            try {
                out_stream.close();// 关闭输出流
            } catch (Exception e) {
                e.printStackTrace();
            }
            out_stream = null;
        }

        if (input_stream != null) {
            try {
                input_stream.close();// 关闭输入流
            } catch (Exception e) {
                e.printStackTrace();
            }
            input_stream = null;
        }
    }

    /**
     * 获取系统日志
     */
    private static void startSystemLog() {
        BufferedReader bufferedReader = null;
        try {
            String[] cmdLine = new String[2];
            // 设置命令 logcat -d 读取日志
            cmdLine[0] = "logcat";
            cmdLine[1] = "-d";
            // 读取系统日志
            bufferedReader = catSystemLog(cmdLine);
            // 日志信息
            String logInfo = null;
            while (running) {
                if (bufferedReader == null) {
                    Thread.sleep(40);
                    continue;
                }

                if ((logInfo = bufferedReader.readLine()) != null) {// 开始读取日志，每次读取一行
                    // 输出到文件
                    writeLogFile(logInfo + "\r\n");
                } else {// 读取完毕
                    bufferedReader.close();
                    bufferedReader = catSystemLog(cmdLine);// 重新读取
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 读取系统日志
     */
    private static BufferedReader catSystemLog(String[] cmdLine) {
        try {
            // 重新捕获日志
            Process process = Runtime.getRuntime().exec(cmdLine);
            return new BufferedReader(new InputStreamReader(process.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
