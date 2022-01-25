package com.hxty.schoolnet.utils.sphelper;

import android.content.Context;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * sdk工具类
 */
public class SdkUtil {
    private static String TAG = "SdkUtil";

    private static Context appContext;

    public static Context getApplicationContext() {
        return appContext;
    }

    public static void setApplicationContext(Context context) {
        appContext = context;
    }

    /**
     * 获取终端系统型号
     */
    public static String getModel() {
        return android.os.Build.MODEL + " ";
    }

    /**
     * 获取操作系统系统开机运行时间
     */
    public static long getRealTime() {
        return android.os.SystemClock.elapsedRealtime();
    }

    /**
     * 压缩
     *
     * @param src  源文件路径
     * @param dest 目的文件路径
     */
    public static void zip(String src, String dest) {
        // 提供了一个数据项压缩成一个ZIP归档输出流
        ZipOutputStream out = null;
        try {

            File outFile = new File(dest);// 源文件或者目录
            File fileOrDirectory = new File(src);// 压缩文件路径
            out = new ZipOutputStream(new FileOutputStream(outFile));
            // 如果此文件是一个文件，否则为false。
            if (fileOrDirectory.isFile()) {
                zipFileOrDirectory(out, fileOrDirectory, "");
            } else {
                // 返回一个文件或空阵列。
                File[] entries = fileOrDirectory.listFiles();
                for (int i = 0; i < entries.length; i++) {
                    // 递归压缩，更新curPaths
                    zipFileOrDirectory(out, entries[i], "");
                }
            }
        } catch (Exception ex) {
            Log.exception(TAG, ex);
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (Exception ex) {
                Log.exception(TAG, ex);
            }
        }
    }

    /**
     * 压缩一个文件或者目录下所有文件
     */
    private static void zipFileOrDirectory(ZipOutputStream out, File fileOrDirectory, String curPath) {
        // 从文件中读取字节的输入流
        FileInputStream in = null;
        try {
            // 如果此文件是一个目录，否则返回false。
            if (!fileOrDirectory.isDirectory()) {
                // 压缩文件
                byte[] buffer = new byte[4096];
                int bytes_read;
                in = new FileInputStream(fileOrDirectory);
                // 实例代表一个条目内的ZIP归档
                ZipEntry entry = new ZipEntry(curPath + fileOrDirectory.getName());
                // 条目的信息写入底层流
                out.putNextEntry(entry);
                while ((bytes_read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytes_read);
                }
                out.closeEntry();
            } else {
                // 压缩目录
                File[] entries = fileOrDirectory.listFiles();
                for (int i = 0; i < entries.length; i++) {
                    // 递归压缩，更新curPaths
                    zipFileOrDirectory(out, entries[i], curPath + fileOrDirectory.getName() + "/");
                }
            }
        } catch (Exception ex) {
            Log.exception(TAG, ex);
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (Exception ex) {
                Log.exception(TAG, ex);
            }
        }
    }

    /**
     * 解压
     *
     * @param zipFileName     压缩文件
     * @param outputDirectory 输出路径
     */
    public static void unzip(String zipFileName, String outputDirectory) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(zipFileName);
            Enumeration e = zipFile.entries();
            ZipEntry zipEntry = null;
            File dest = new File(outputDirectory);
            dest.mkdirs();
            while (e.hasMoreElements()) {
                zipEntry = (ZipEntry) e.nextElement();
                String entryName = zipEntry.getName();
                InputStream in = null;
                FileOutputStream out = null;
                try {
                    if (zipEntry.isDirectory()) {
                        String name = zipEntry.getName();
                        name = name.substring(0, name.length() - 1);

                        File f = new File(outputDirectory + File.separator + name);
                        f.mkdirs();
                    } else {
                        int index = entryName.lastIndexOf("\\");
                        if (index != -1) {
                            File df = new File(outputDirectory + File.separator + entryName.substring(0, index));
                            df.mkdirs();
                        }
                        index = entryName.lastIndexOf("/");
                        if (index != -1) {
                            File df = new File(outputDirectory + File.separator + entryName.substring(0, index));
                            df.mkdirs();
                        }
                        File f = new File(outputDirectory + File.separator + zipEntry.getName());
                        in = zipFile.getInputStream(zipEntry);
                        out = new FileOutputStream(f);

                        int c;
                        byte[] by = new byte[1024];

                        while ((c = in.read(by)) != -1) {
                            out.write(by, 0, c);
                        }
                        out.flush();
                    }
                } catch (Exception ex) {
                    Log.exception(TAG, ex);
                } finally {
                    try {
                        if (in != null)
                            in.close();
                    } catch (Exception ex) {
                        Log.exception(TAG, ex);
                    }
                    try {
                        if (out != null)
                            out.close();
                    } catch (Exception ex) {
                        Log.exception(TAG, ex);
                    }
                }
            }
        } catch (Exception ex) {
            Log.exception(TAG, ex);
        } finally {
            try {
                if (zipFile != null)
                    zipFile.close();
            } catch (Exception ex) {
                Log.exception(TAG, ex);
            }
        }
    }

    /**
     * 方法说明 : 执行系统命令，需要root权限
     *
     * @param cmd 命令
     * @author fuluo
     * @Date 2016-6-8
     */
    public static int rootCmd(String cmd) {
        Process process = null;
        DataOutputStream os = null;
        int a = -1;

        try {
            process = Runtime.getRuntime().exec("su");
            Log.debug(TAG, cmd);
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            a = process.waitFor();
            Log.debug(TAG, "result:" + a);
        } catch (Exception e) {
            Log.exception(TAG, e);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (Exception e) {
                Log.exception(TAG, e);
            }
        }
        return a;
    }
}
