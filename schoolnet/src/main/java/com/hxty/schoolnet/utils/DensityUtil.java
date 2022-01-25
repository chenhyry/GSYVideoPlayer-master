package com.hxty.schoolnet.utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * dip 转px   反向转换
 */
public class DensityUtil {
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        if (scale == 1.0f) {
            if (getScreenWidth(context) > 1280) {//24寸1080p平板--校园屏平板
                return (int) (dpValue * scale + 0.5f);
            } else {//720电视
                return (int) (dpValue * 0.69f);
            }
        } else if (scale == 2.0f) {
            if (getScreenWidth(context) > 1366) {//40寸以上平板电视 >1280
                return (int) (dpValue);
            } else {//720p手机
                return (int) (dpValue / 1.5f);
            }
        }
        //-------手机 首页做了更改，将二级菜单放大了点
        else if (scale <= 3.0) {//1080p 正常对比720p比例应该返回 return (int) dpValue,此处处理放大
            return (int) (dpValue / 0.96f);
        } else {//2k
            return (int) (dpValue / 0.8f);
        }
    }

//    public static int dip2px(Context context, float dpValue) {
//        float scale = context.getResources().getDisplayMetrics().density;
//        return (int) (dpValue * scale + 0.5F);
//    }

    public static boolean isPhone(Context context) {
        float scale = context.getResources().getDisplayMetrics().density;
        if (scale == 1.0f) {
            return false;
        } else if (scale == 2.0f) {
            if (getScreenWidth(context) > 1366) {//40寸以上平板电视 >1280
                return false;
            } else {//720p手机
                return true;
            }
        }
        //-------手机 首页做了更改，将二级菜单放大了点
        else if (scale <= 3.0) {//1080p 正常对比720p比例应该返回 return (int) dpValue,此处处理放大
            return true;
        } else {//2k
            return true;
        }
    }


    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 获取屏幕宽度
     *
     * @return the int
     */
    public static int getScreenWidth(Context context) {
        // 获取屏幕密度（方法2） 
        DisplayMetrics dm = dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;      // 屏幕宽（像素，如：480px）
    }

    /**
     * 获取屏幕高度
     *
     * @return the int
     */
    public static int getScreenHeight(Context context) {
        // 获取屏幕密度（方法2）
        DisplayMetrics dm = new DisplayMetrics();
        dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;      // 屏幕宽（像素，如：480px）
    }
}
