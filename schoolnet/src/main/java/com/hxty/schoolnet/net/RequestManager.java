package com.hxty.schoolnet.net;

import com.hxty.schoolnet.App;
import com.hxty.schoolnet.entity.BaseResponse;
import com.hxty.schoolnet.entity.FirstLevelMenu;
import com.hxty.schoolnet.entity.Page;
import com.hxty.schoolnet.entity.Program;
import com.hxty.schoolnet.entity.SchoolMember;
import com.hxty.schoolnet.entity.ScreenVideo;
import com.hxty.schoolnet.entity.Version;
import com.hxty.schoolnet.entity.VideoPic;
import com.lzy.okgo.OkGo;

import org.json.JSONObject;

import java.util.HashMap;

public class RequestManager {


    /**
     * get请求获取数据
     *
     * @param url
     */
//    private void getByOkGo(String url){
//        OkGo.get(url)                            // 请求方式和请求url
//                .tag(this)                       // 请求的 tag, 主要用于取消对应的请求
//                .cacheKey("cacheKey")            // 设置当前请求的缓存key,建议每个不同功能的请求设置一个，如果不指定cacheKey，默认是用url带参数的全路径名为cacheKey。
//                .cacheMode(CacheMode.DEFAULT)    // 缓存模式
//                .execute(new StringCallback() {
//                    @Override
//                    public void onSuccess(String s, Call call, Response response) {
//                        mTextView2.setText(s);
//                    }
//                });
//    }
    /*
    这里简单说一下缓存模式cacheMode
一共有五种CacheMode缓存模式

NO_CACHE：不使用缓存，该模式下cacheKey、cacheTime 参数均无效
DEFAULT：按照HTTP协议的默认缓存规则，例如有304响应头时缓存。
REQUEST_FAILED_READ_CACHE：先请求网络，如果请求网络失败，则读取缓存，如果读取缓存失败，本次请求失败。
IF_NONE_CACHE_REQUEST：如果缓存不存在才请求网络，否则使用缓存。
FIRST_CACHE_THEN_REQUEST：先使用缓存，不管是否存在，仍然请求网络。
缓存的对象bean必须实现Serializable接口，否者会报NotSerializableException。因为缓存的原理是将对象序列化后直接写入数据库中，如果不实现Serializable接口，会导致对象无法序列化，进而无法写入到数据库中，也就达不到缓存的效果。
     */


    private static RequestManager requestManager;

    public static RequestManager getInstance() {
        if (null == requestManager) synchronized (RequestManager.class) {
            if (null == requestManager) requestManager = new RequestManager();
        }
        return requestManager;
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return Constants.BASE_URL + relativeUrl;
    }


    /**
     * 获取第一级栏目信息  第一次进入显示加载框，自动刷新不显示加载框
     */
    public void GetFirstLevelColumns(JsonCallback<BaseResponse<Page<FirstLevelMenu>>> callback) {
        HashMap opMap = new HashMap<String, String>();
        opMap.put("pageSize", "100");
        opMap.put("pageIndex", "1");
        opMap.put("sort", "");
        opMap.put("order", "");
        JSONObject jsonObject = new JSONObject(opMap);
        OkGo.<BaseResponse<Page<FirstLevelMenu>>>post(getAbsoluteUrl(Constants.GetFirstLevelColumns))
                .upJson(jsonObject.toString())
                .execute(callback);
    }

    /**
     * 获取、检查更新
     */
    public void getNewVersion(JsonCallback<BaseResponse<Page<Version>>> callback) {
        HashMap opMap = new HashMap<String, String>();
        opMap.put("AppNameEN", "schoolnet");
        JSONObject jsonObject = new JSONObject(opMap);
        OkGo.<BaseResponse<Page<Version>>>post(getAbsoluteUrl(Constants.GET_NEW_VERSION_URL))
                .upJson(jsonObject.toString())
                .execute(callback);
    }

    /**
     * 获取视频封面接口
     */
    public void GetVideoPicture(JsonCallback<BaseResponse<VideoPic>> callback) {
        OkGo.<BaseResponse<VideoPic>>post(getAbsoluteUrl(Constants.GetVideoPicture))
                .execute(callback);
    }

    /**
     * 增加栏目点击次数
     */
    public void AddComumnClickNum(String columnId, JsonCallback<BaseResponse<String>> callback) {
        OkGo.<BaseResponse<String>>get(getAbsoluteUrl(Constants.AddComumnClickNum))
                .params("columnId", columnId)
                .execute(callback);
    }

    /**
     * 增加节点击次数
     */
    public void AddProgramClickNum(String programId, JsonCallback<BaseResponse<String>> callback) {
        OkGo.<BaseResponse<String>>get(getAbsoluteUrl(Constants.AddProgramClickNum))
                .params("programId", programId)
                .execute(callback);
    }


    /**
     * 添加设备运行日志，并修改设备是否上线状态
     */
    public void AddDviceRunningLog(String DeviceIMEI, int isOnline, String LastUpdateTime, String Remark, JsonCallback<BaseResponse<String>> callback) {
        HashMap opMap = new HashMap<String, String>();
        opMap.put("DeviceIMEI", DeviceIMEI);
        opMap.put("isOnline", isOnline + "");
        opMap.put("LastUpdateTime", LastUpdateTime);
        opMap.put("Remark", Remark);
        JSONObject jsonObject = new JSONObject(opMap);
        OkGo.<BaseResponse<String>>post(getAbsoluteUrl(Constants.AddDviceRunningLog))
                .upJson(jsonObject)
                .execute(callback);
    }


    //改版---------------------------------

    /**
     * 获取屏幕保护程序
     */
    public void GetScreenVideo(String loginName, JsonCallback<BaseResponse<ScreenVideo>> callback) {
        if (loginName == null || "3".equals(App.SCHOOLTYPE)) {
            OkGo.<BaseResponse<ScreenVideo>>get(getAbsoluteUrl(Constants.GetScreenVideoList))
                    .execute(callback);
        } else {
            OkGo.<BaseResponse<ScreenVideo>>get(getAbsoluteUrl(Constants.GetScreenVideoBySchoolName))
                    .params("loginName", loginName)
                    .execute(callback);
        }
    }

    /**
     * 获取滚动新闻--不显示进度条
     */
    public void GetRollingNews(String loginName, JsonCallback<BaseResponse<Page<Program>>> callback) {
        if (loginName == null || "3".equals(App.SCHOOLTYPE)) {
            OkGo.<BaseResponse<Page<Program>>>post(getAbsoluteUrl(Constants.GetRollingNews))
                    .execute(callback);
        } else {
            OkGo.<BaseResponse<Page<Program>>>get(getAbsoluteUrl(Constants.GetRollNewsBySchoolName))
                    .params("loginName", loginName)
                    .execute(callback);
        }
    }


    /**
     * 根据登录名称获取该机构所有栏目列表
     */
    public void GetColumnBySchoolName(String loginName, JsonCallback<BaseResponse<Page<FirstLevelMenu>>> callback) {
        OkGo.<BaseResponse<Page<FirstLevelMenu>>>get(getAbsoluteUrl(Constants.GetColumnBySchoolName))
                .params("loginName", loginName)
                .execute(callback);
    }

    /**
     * 根据登录名称获取该机构所有栏目列表
     */
    public void GetProgramsIsPrivate(String loginName, String pageSize, String pageIndex, JsonCallback<BaseResponse<Page<Program>>> callback) {
        HashMap opMap = new HashMap<String, String>();
        opMap.put("pageSize", pageSize);
        opMap.put("pageIndex", pageIndex);
        JSONObject jsonObject = new JSONObject(opMap);
        OkGo.<BaseResponse<Page<Program>>>post(getAbsoluteUrl(Constants.GetProgramsIsPrivate + loginName))
                .upJson(jsonObject)
                .execute(callback);
    }

    public void GetColumnsByParentId(String parentId, JsonCallback<BaseResponse<Page<FirstLevelMenu>>> callback) {
        HashMap opMap = new HashMap<String, String>();
        opMap.put("parentId", parentId);
        opMap.put("pageSize", "100");
        opMap.put("pageIndex", "1");
        JSONObject jsonObject = new JSONObject(opMap);
        OkGo.<BaseResponse<Page<FirstLevelMenu>>>post(getAbsoluteUrl(Constants.GetColumnsByParentId))
                .upJson(jsonObject)
                .execute(callback);
    }


    /**
     * 第三方单位登录  自动登录不显示加载框，手动登录显示
     */
    public void SchoolLogin(String loginName, String password, JsonCallback<BaseResponse<String>> callback) {
        OkGo.<BaseResponse<String>>get(getAbsoluteUrl(Constants.SchoolLogin))
                .params("loginName", loginName)
                .params("password", password)
                .execute(callback);
    }


    /**
     * 根据栏目id获取栏目下的节目信息带分页
     */
    public void GetProgramsByColumnId(String loginName, String columnId, int pageIndex, int pageSize, JsonCallback<BaseResponse<Page<Program>>> callback) {
        HashMap opMap = new HashMap<String, String>();
        opMap.put("pageSize", pageSize + "");
        opMap.put("pageIndex", pageIndex + "");
        opMap.put("sort", "");
        opMap.put("order", "");
        JSONObject jsonObject = new JSONObject(opMap);
        if (loginName != null) {
            OkGo.<BaseResponse<Page<Program>>>post(getAbsoluteUrl(Constants.GetProgramBySchoolName + loginName))
                    .upJson(jsonObject)
                    .execute(callback);
        } else {
            OkGo.<BaseResponse<Page<Program>>>post(getAbsoluteUrl(Constants.GetProgramsByColumnId + columnId))
                    .upJson(jsonObject)
                    .execute(callback);
        }
    }

    /**
     * 根据栏目id获取栏目下的推荐节目信息带分页
     */
    public void GetRecommendedProgramsByColumnId(String loginName, int columnId, JsonCallback<BaseResponse<Page<Program>>> callback) {
        HashMap opMap = new HashMap<String, String>();
        opMap.put("pageSize", "50");
        opMap.put("pageIndex", "1");
        opMap.put("sort", "");
        opMap.put("order", "");
        JSONObject jsonObject = new JSONObject(opMap);
        if (loginName == null) {
            OkGo.<BaseResponse<Page<Program>>>post(getAbsoluteUrl(Constants.GetRecommendedProgramsByColumnId + columnId))
                    .upJson(jsonObject)
                    .execute(callback);
        } else {
            OkGo.<BaseResponse<Page<Program>>>get(getAbsoluteUrl(Constants.GetRecommendedBySchoolName))
                    .params("loginName", loginName)
                    .execute(callback);
        }
    }

    /**
     * 普通用户登录  自动登录不显示加载框
     */
    public void ScreenMemberLogin(String loginName, JsonCallback<BaseResponse<SchoolMember>> callback) {
        OkGo.<BaseResponse<SchoolMember>>post(getAbsoluteUrl(Constants.ScreenMemberLogin + loginName))
                .execute(callback);
    }

    /**
     * 发送验证码接口
     */
    public void SendValidateCode(String PhoneNumber, String ValidateCode, JsonCallback<BaseResponse<String>> callback) {
        HashMap opMap = new HashMap<String, String>();
        opMap.put("PhoneNumber", PhoneNumber);
        opMap.put("ValidateCode", ValidateCode);
        JSONObject jsonObject = new JSONObject(opMap);
        OkGo.<BaseResponse<String>>post(getAbsoluteUrl(Constants.SendValidateCode))
                .upJson(jsonObject)
                .execute(callback);
    }

    /**
     * 检查某个会员是否给某个节目点赞过forapp
     */
    public void CheckProgramDianzan(String programId, String TokenId, JsonCallback<BaseResponse<String>> callback) {
        OkGo.<BaseResponse<String>>get(getAbsoluteUrl(Constants.CheckProgramDianzan))
                .params("programId", programId)
                .params("TokenId", TokenId)
                .execute(callback);
    }

    /**
     * 增加节目点赞记录for APP
     */
    public void AddProgramDianzan(String programId, String TokenId, JsonCallback<BaseResponse<String>> callback) {
        HashMap<String, String> opMap = new HashMap<>();
        opMap.put("TokenId", TokenId);
        JSONObject jsonObject = new JSONObject(opMap);
        OkGo.<BaseResponse<String>>post(getAbsoluteUrl(Constants.AddProgramDianzan + programId))
                .upJson(jsonObject)
                .execute(callback);
    }


    /**
     * 添加一个异常捕获描述记录
     */
//    public void AddExceptionInfo(String AppVersion, String FunctionName, String MemberName, String Description, JsonCallback callback) {
//        HashMap opMap = new HashMap<String, String>();
//        opMap.put("PhoneType", android.os.Build.MANUFACTURER + android.os.Build.MODEL);
//        opMap.put("OSVersion", android.os.Build.VERSION.RELEASE);
//        opMap.put("AppVersion", AppVersion);
//        opMap.put("FunctionName", FunctionName);
//        opMap.put("MemberName", MemberName);
//        opMap.put("Description", Description);
//        JSONObject jsonObject = new JSONObject(opMap);
//        postReq(Constants.AddExceptionInfo, jsonObject, false, callback);
//    }

}
