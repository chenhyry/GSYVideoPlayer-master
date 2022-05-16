package com.hxty.schoolnet.net;

public class Constants {

    //    https://www.xiaoyuanping.cn/help  文档地址
    public static final String BASE_HOST = "xyp.bjedu.cn";
    public static final String BASE_URL = "https://xyp.bjedu.cn/";
        public static final String HOME_IMAGE_URL = "https://xyp.bjedu.cn:8888/";
    public static final String IMAGE_URL = "https://xyp.bjedu.cn:8888/";//二级栏目图片和更新apk地址

//    public static final String BASE_HOST = "58.129.247.147";
//    public static final String BASE_URL = "https://58.129.247.147/";
//    public static final String HOME_IMAGE_URL = "https://58.129.247.147:8001/";
//    public static final String IMAGE_URL = "https://58.129.247.147:8001/";

    public static final String GET_NEW_VERSION_URL = "api/SchoolNet/GetAndroidNewVersion";
    public static final String AddDviceRunningLog = "api/SchoolNet/AddDviceRunningLog";
//    public static final String AddExceptionInfo = "api/SchoolNet/AddExceptionInfo";
    //    public static final String FIRST_MENU_URL = "api/SchoolNet/GetFirstLevelColumns";
//    public static final String SECOND_MENU_URL = "api/SchoolNet/GetColumnsByParentId?parentId=";
//    public static final String THREE_MENU_URL = "api/SchoolNet/GetProgramsByColumnId?columnId=";
    public static final String GetVideoPicture = "api/SchoolNet/GetVideoPicture";


    //获取屏保视频列表数据
    public static final String GetScreenVideoList = "api/SchoolNet/GetScreenVideoList";
    //获取首页数据
    public static final String GetFirstLevelColumns = "api/SchoolNet/GetFirstLevelColumns";
    //获取滚动新闻
    public static final String GetRollingNews = "api/SchoolNet/GetRollingNews";
    public static final String GetProgramsByColumnId = "api/SchoolNet/GetProgramsByColumnIdII?columnId=";
    public static final String GetRecommendedProgramsByColumnId = "api/SchoolNet/GetRecommendedProgramsByColumnId?columnId=";

    //增加栏目点击次数
    public static final String AddComumnClickNum = "api/SchoolNet/AddComumnClickNum";
    //增加节目点击次数
    public static final String AddProgramClickNum = "api/SchoolNet/AddProgramClickNum";

    //登录
    public static final String SchoolLogin = "api/SchoolNet/SchoolLogin"; //单位终端大屏登录接口
    public static final String GetProgramBySchoolName = "api/SchoolNet/GetProgramBySchoolName?loginName=";
    public static final String GetRollNewsBySchoolName = "api/SchoolNet/GetRollNewsBySchoolNameII";//根据登录名称获取该机构和上级机构的滚动新闻
    public static final String GetScreenVideoBySchoolName = "api/SchoolNet/GetScreenVideoBySchoolNameII";//根据登录名称获取该机构和上级机构的导视视频列表
    public static final String GetRecommendedBySchoolName = "api/SchoolNet/GetRecommendedBySchoolName";
    public static final String GetColumnBySchoolName = "api/SchoolNet/GetColumnsBySchoolName";//根据登录名称获取该机构所有栏目列表
    public static final String GetProgramsIsPrivate = "api/SchoolNet/GetProgramsIsPrivate?loginName=";//获取私密版块的节目带分页
    public static final String GetColumnsByParentId = "api/SchoolNet/GetColumnsByParentId?parentId=";//根据ParentId获取子栏目带分页

    public static final String ScreenMemberLogin = "api/Member/ScreenMemberLogin?loginName=";//大屏幕普通用户登录接口

    public static final String SendValidateCode = "api/Member/SendValidateCode";//发送验证码接口
    public static final String CheckProgramDianzan = "api/Member/CheckProgramDianzan";//检查某个会员是否给某个节目点赞过forapp
    public static final String AddProgramDianzan = "api/Member/AddProgramDianzan?programId=";//检查某个会员是否给某个节目点赞过forapp

    //每页显示条数
    public static final int INIT_PAGE_NUM = 7;

    public static final int SHOW_SCREENSAVE_DELAY_TIME = 5 * 60 * 1000;
}
