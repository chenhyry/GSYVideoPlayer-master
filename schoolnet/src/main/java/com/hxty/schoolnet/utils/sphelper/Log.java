package com.hxty.schoolnet.utils.sphelper;

import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// import java.util.Locale;
// import java.text.DateFormat;
// import java.text.SimpleDateFormat;

/**
 * Class Log allows the printing of log messages onto standard output or files
 * or any PrintStream.
 * <p/>
 * Every Log has a <i>verboselevel</i> associated with it; any log request with
 * <i>loglevel</i> less or equal to the <i>verbose-level</i> is logged. <br>
 * Verbose level 0 indicates no log. The log levels should be greater than 0.
 * <p/>
 * Parameter <i>logname</i>, if non-null, is used as log header (i.e. written
 * at the begin of each log row).
 */
public class Log {
    /** ***************************** Attributes ****************************** */

    /**
     * (static) Default maximum log file size
     */
    public static int MAX_SIZE = 40; // MB

    /**
     * The log output stream
     */
    private static PrintStream out_stream;

    /**
     * 日期格式
     */
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");

    /**
     * 消息队列
     */
    private static BlockingQueue<String> logQueue = new LinkedBlockingQueue<String>();

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
     * 日志队列最大数量
     */
    private static int maxLogQueueSize = 2000;

    /**
     * 主控单元控制PoC SDK设置日志文件存储路径
     *
     * @param szLogPath 日志路径(路径，不带文件名)
     * @param szLogName 日志文件名
     * @param nMaxSize  最大容量，单位：M
     * @return ServiceConstant.METHOD_SUCCESS：成功调用；
     * ServiceConstant.METHOD_FAILED：方法调用失败；
     * ServiceConstant.PARAM_ERROR：参数错误。
     */
    public static int startLogService(String szLogPath, String szLogName, int nMaxSize) {
        Log.debug("LogSystem", "setLogPath:: szLogPath:" + szLogPath + " szLogName:" + szLogName + " nMaxSize:" + nMaxSize);
        CommonConfigEntry.LOG_FILEPATH = szLogPath;
        CommonConfigEntry.LOG_NAME = szLogName;
        CommonConfigEntry.LOG_MAXSIZE = nMaxSize;
        Log.startLogFile();
        return CommonConstantEntry.METHOD_SUCCESS;
    }

    /**
     * 记录调试日志
     *
     * @param tag     标识
     * @param message 日志信息
     */
    public static void debug(String tag, String message) {
        if (tag == null || message == null) {// 参数验证
            return;
        }
        if (CommonConfigEntry.LOG_DEBUG) {
            log(tag, message, LogLevel.DEBUG);
        }
    }

    /**
     * 开启记录日志文件
     */
    public static void startLogFile() {
        if (logThread != null) {
            return;
        }

        initLog();//初始化

        logThread = new Thread() {
            @Override
            public void run() {
                int count = 0;//计数器
                while (CommonConfigEntry.LOG_OUTFILE) {
                    try {
                        String logStr = logQueue.take();
                        if (CommonConfigEntry.LOG_OUTFILE) {//记录日志文件
                            checkLogExists();//检查日志文件是否存在，不存在则重新创建
                            out_stream.print(logStr);//写入
                            if (count % 100 == 0) {//每n次检查一次日志
                                checkLogSize();//检查日志大小
                            }
                            count++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }
                logThread = null;//结束日志输出置空
            }
        };
        logThread.start();
    }

    /**
     * 输出日志
     *
     * @param tag     标识
     * @param message 日志信息
     * @param level   日志级别
     */
    private static void log(String tag, String message, int level) {
        try {
            String log = packLog(tag, message, level);

            if (CommonConfigEntry.LOG_OUTFILE) {//输出到文件
                try {
                    if (logQueue.size() == maxLogQueueSize) {// 超出最多包数
                        logQueue.poll();
                    }
                    logQueue.add(log);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (CommonConfigEntry.LOG_LOGCAT) {//输出到控制台
                logcat_Android(tag, message, level);
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
            MAX_SIZE = CommonConfigEntry.LOG_MAXSIZE;//设置日志大小
            new File(CommonConfigEntry.LOG_FILEPATH).mkdirs();//创建目录
            File logPath = new File(CommonConfigEntry.LOG_FILEPATH);
            if (!logPath.exists()) {//日志文件不存在，则创建
                logPath.createNewFile();
            }
            logFile = new File(CommonConfigEntry.LOG_FILEPATH + CommonConfigEntry.LOG_NAME);
            if (!logFile.exists()) {//日志文件不存在，则创建
                logFile.createNewFile();
            }
            //追加写入，自动flush
            out_stream = new PrintStream(new FileOutputStream(logFile, true), true);
            //日志读取流，用于获取日志文件大小
            input_stream = new FileInputStream(logFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查日志文件是否存在，如果不存在则创建
     */
    private static void checkLogExists() {
        if (!logFile.exists()) {//日志文件不存在（被删除）
            try {
                out_stream.close();//关闭输出流
                input_stream.close();//关闭输入流

                initLog();//重新初始化日志
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检查日志文件大小，防止日志文件过大占用存储空间
     */
    private static void checkLogSize() {
        try {
            int size = input_stream.available() / (1024 * 1024);//大小

            if (size >= MAX_SIZE) {//超出大小
                String logPathName = CommonConfigEntry.LOG_FILEPATH + CommonConfigEntry.LOG_NAME;
                // 删除第x个压缩包
                File backLog = new File(logPathName + "." + CommonConfigEntry.LOG_MAX_FILES + ".zip");
                backLog.delete();
                File bakLog = null;
                int tempMaxFiles = CommonConfigEntry.LOG_MAX_FILES - 1;
                for (int i = tempMaxFiles; i > 0; i--) {// 遍历改名2-x个压缩包
                    bakLog = new File(logPathName + "." + i + ".zip");
                    bakLog.renameTo(new File(logPathName + "." + (i + 1) + ".zip"));
                }
                // 压缩成第1个压缩包
                SdkUtil.zip(logPathName, logPathName + ".1.zip");
                logFile.delete();
                initLog();//重新初始化
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 组装日志
     */
    private static String packLog(String tag, String message, int level) {
        StringBuffer logsb = new StringBuffer();
        String zone = null;
        if (!TextUtils.isEmpty(zone)) {
            TimeZone timeZone = TimeZone.getTimeZone(zone);
            sdf.setTimeZone(timeZone);
        }
        logsb.append(sdf.format(System.currentTimeMillis())).append(": ");
//        logsb.append(System.currentTimeMillis()).append(": ");
        logsb.append(Thread.currentThread().getId()).append(": ");
        if (level == LogLevel.DEBUG) {
            logsb.append("DEBUG: ");
        } else if (level == LogLevel.INFO) {
            logsb.append("INFO: ");
        } else if (level == LogLevel.EXCEPTION) {
            logsb.append("EXCEPTION: ");
        } else if (level == LogLevel.ERROR) {
            logsb.append("ERROR: ");
        }
        logsb.append(tag).append(":");
        logsb.append(message).append("\r\n");

        return logsb.toString();
    }

    /**
     * 输出到logcat
     */
    private static void logcat_Android(String tag, String message, int level) {
        if (level == LogLevel.DEBUG) {
            android.util.Log.d(tag, message);
        }
        if (level == LogLevel.INFO) {
            android.util.Log.i(tag, message);
        } else if (level == LogLevel.EXCEPTION) {
            android.util.Log.w(tag, message);
        } else if (level == LogLevel.ERROR) {
            android.util.Log.e(tag, message);
        }
    }

    /**
     * 记录异常日志
     *
     * @param tag 标识
     * @param e   异常对象
     */
    public static void exception(String tag, Exception e) {
        if (tag == null || e == null) {// 参数验证
            return;
        }
        log(tag, ExceptionPrinter.getStackTraceOf(e), LogLevel.EXCEPTION);
    }

    /**
     * 记录异常日志
     *
     * @param tag 标识
     * @param e   异常对象
     */
    public static void throwable(String tag, Throwable e) {
        if (tag == null || e == null) {// 参数验证
            return;
        }
        log(tag, ExceptionPrinter.getStackTraceOf(e), LogLevel.EXCEPTION);
    }

    /**
     * 记录错误日志
     *
     * @param tag     标识
     * @param message 日志信息
     */
    public static void error(String tag, String message) {
        if (tag == null || message == null) {// 参数验证
            return;
        }
        log(tag, message, LogLevel.ERROR);
    }

    /**
     * 记录信息日志
     *
     * @param tag     标识
     * @param message 日志信息
     */
    public static void info(String tag, String message) {
        if (tag == null || message == null) {// 参数验证
            return;
        }
        log(tag, message, LogLevel.INFO);

    }

    /**
     * 输出到console
     */
    private static void log_Console(String log) {
        System.out.print(log);//输出到console
    }

}
