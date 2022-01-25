package com.hxty.schoolnet.entity;


public class BaseResponseMember {

    public String status;

    //服务器返回的result是String类型（除获取封面不是String ）
//   public T result;
    public SchoolMember result; //如果泛型是String，直接使用这个

//    public VideoPic handleResult;
}
