package com.hxty.schoolnet.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by C.H.O on 2017/8/23.
 */
public class RegUtils {

    /**
     * 是否是手机号
     * @param phonenum
     * @return
     */
    public static boolean isPhonenum(String phonenum) {
        if (phonenum.isEmpty()) {
            return false;
        }
        String regex = "^(1)\\d{10}$";
        return Pattern.compile(regex).matcher(phonenum).matches();
    }

    /**
     * 只允许汉字
     *
     * @param str
     * @return
     * @throws PatternSyntaxException
     */
    public static String chineseCharacterFilter(String str) throws PatternSyntaxException {
        //只允许汉字
        String regEx = "[^\u4E00-\u9FA5_a-zA-Z]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }
}
