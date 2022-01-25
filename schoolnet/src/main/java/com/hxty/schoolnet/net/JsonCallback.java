package com.hxty.schoolnet.net;


import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.hxty.schoolnet.entity.BaseResponse;
import com.hxty.schoolnet.entity.BaseResponseMember;
import com.hxty.schoolnet.entity.BaseResponseS;
import com.hxty.schoolnet.entity.Page;
import com.hxty.schoolnet.entity.SchoolMember;
import com.hxty.schoolnet.entity.VideoPic;
import com.hxty.schoolnet.ui.BaseActivity;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.exception.HttpException;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;

import java.lang.ref.WeakReference;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import okhttp3.ResponseBody;

/*
{"status":"hx-001","result":"{\"total\":1,\"rows\":[{\"KeyId\":1119,\"AppNameEN\":\"schoolnet\",\"AppNameCN\":\"校园屏\",\"CurVersion\":40,\"Url\":\"uploadfiles\/apk\/199fcee1-10bf-4d92-9210-655243461c04.apk\",\"AddTime\":\"2020-11-10 10:59:10\",\"Remark\":\"修改新域名\"}]}"}
{"status":"hx-001","result":"{\"total\":0,\"rows\":[]}"}
{"status":"hx-001","result":"40097"}
{"status":"hx-001","result":"{ CloseAudioTimes: 11:05,11:10,11:15,11:30, SchoolName:学校测试账号，ImgUrl:uploadimages\/School\/065d2727-7f0e-412c-88c2-71bd756eb955.png#，SchoolType:1，ParentId:0}"}

{"LastUpdateTime":"","DeviceIMEI":"020000000000..null","isOnline":"0","Remark":"关闭App"}

{"sort":"","order":"","pageIndex":"1","pageSize":"100"}
{"Message":"请求的资源不支持 http 方法“POST”。"}
{"status":"hx-001","result":{"KeyId":1,"State":1,"ImgUrl":"uploadimages/Columns/1245abae-54c6-46a6-b211-c0af8bf6084d.jpg"}}
 */
public class JsonCallback<T> extends AbsCallback<T> {

    private Type type;
    private Class<T> clazz;

    //显示加载框
    private WeakReference<BaseActivity> activityWeakReference;
    private boolean showDialog = false;

    public JsonCallback() {
    }

    public JsonCallback(BaseActivity activity, boolean showDialog) {
        if (showDialog) {
            activityWeakReference = new WeakReference<>(activity);
        }
        this.showDialog = showDialog;
    }

    public JsonCallback(Type type) {
        this.type = type;
    }

    public JsonCallback(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void onStart(Request<T, ? extends Request> request) {
        super.onStart(request);
        if (showDialog && activityWeakReference != null && activityWeakReference.get() != null) {
            activityWeakReference.get().showDialog();
        }
    }

    @Override
    public void onFinish() {
        super.onFinish();
        if (showDialog && activityWeakReference != null && activityWeakReference.get() != null) {
            activityWeakReference.get().dismissDialog();
        }
    }

    @Override
    public void onSuccess(Response<T> response) {

    }

    @Override
    public T convertResponse(okhttp3.Response response) throws Throwable {
        Type genType = getClass().getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();//返回Type[]，即“<>”里的参数，
        //这里得到第二层泛型的所有的类型 BaseResponse<T>
        Type type = params[0];

        if (!(type instanceof ParameterizedType)) throw new IllegalStateException("泛型参数错误");
        //这里得到第二层数据的真实类型 BaseResponse
        Type rawType = ((ParameterizedType) type).getRawType();
        //这里得到第二层数据的泛型的真实类型 T
        Type typeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];

        ResponseBody responseBody = response.body();
        if (responseBody == null) return null;
        Gson gson = new Gson();
        JsonReader jsonReader = new JsonReader(responseBody.charStream());

        if (rawType != BaseResponse.class) {//不是BaseResponse<>直接解析
            T data = gson.fromJson(jsonReader, type);
            response.close();
            return data;
        } else {
            if (typeArgument != Void.class) {//有数据类型 有数据
                BaseResponse baseResponse;
                //处理获取封面不是String是Object
                if (typeArgument == VideoPic.class) {
                    BaseResponseS baseResponses = gson.fromJson(jsonReader, BaseResponseS.class);
                    baseResponse = new BaseResponse<VideoPic>();
                    baseResponse.status = baseResponses.status;
                    baseResponse.result = gson.toJson(baseResponses.result);
                } else if (typeArgument == SchoolMember.class) {
                    BaseResponseMember baseResponses = gson.fromJson(jsonReader, BaseResponseMember.class);
                    baseResponse = new BaseResponse<SchoolMember>();
                    baseResponse.status = baseResponses.status;
                    baseResponse.result = gson.toJson(baseResponses.result);
                } else {
                    //原始写法
                    baseResponse = gson.fromJson(jsonReader, type);
                }

                response.close();
                /*
hx-001：正常
hx-002：参数错误
hx-003：没有数据
hx-004：其他错误
hx-005：登录丢失，请重新登录
hx-006：账号或密码错误
hx-007：用户未验证
hx-008：用户待审核
hx-009：重复收藏
hx-010：重复评分
hx-011：取消失败
hx-012：文件类型错误
hx-013：旧密码错误
hx-999：Token失效
                 */
                String code = baseResponse.status;
                switch (code) {
                    case "hx-001":
                        //原始写法
//                        baseResponse.handleResult = gson.fromJson(baseResponse.result, typeArgument);
//                        return (T) baseResponse;

                        //-----此应用需要单独处理
                        //服务器返回的result是String类型（除获取封面不是String ）
                        try {
                            if (typeArgument != String.class) {//需要转换
                                if (baseResponse.result == null || "0".equals(baseResponse.result)) {
                                    baseResponse.handleResult = new Page<T>();
                                    return (T) baseResponse;
//                                    throw new IllegalStateException("没有数据");
                                } else {
                                    baseResponse.handleResult = gson.fromJson(baseResponse.result, typeArgument);
                                }
                            }
                            return (T) baseResponse;
                        } catch (Exception e) {
                            throw new IllegalStateException("解析出错");
                        }
                    case "hx-002":
//                        throw new IllegalStateException("参数错误");
                        throw new IllegalStateException("");
                    case "hx-003":
//                        throw new IllegalStateException("没有数据");
                        throw new IllegalStateException("");
                    case "hx-004":
                        throw new IllegalStateException("其他错误" + baseResponse.result);
                    case "hx-005":
                        throw new IllegalStateException("请重新登录");
                    case "hx-006":
                        throw new IllegalStateException("账号或密码错误");
                    case "hx-007":
                        throw new IllegalStateException("用户未验证");
                    case "hx-008"://用户待审核
                        throw new IllegalStateException("用户待审核");
                    case "hx-009":
                        throw new IllegalStateException("重复收藏");
                    case "hx-010":
                        throw new IllegalStateException("重复评分");
                    case "hx-013":
                        throw new IllegalStateException("原密码错误");
                    case "hx-999":
                        throw new IllegalStateException("Token失效");
                    default:
                        throw new IllegalStateException(code);
                }
            } else {
                throw new IllegalStateException("参数错误");
            }
        }
    }

    @Override
    public void onError(Response<T> response) {
        Throwable throwable = response.getException();
        if (throwable != null) throwable.printStackTrace();
        if (throwable instanceof UnknownHostException || throwable instanceof ConnectException) {
            Log.e("onError:", "网络连接失败，请检查网络！");
        } else if (throwable instanceof SocketTimeoutException) {
            Log.e("onError:", "网络请求超时！");
        } else if (throwable instanceof HttpException) {
            Log.e("onError:", "网络未响应！");
        } else if (throwable instanceof IllegalStateException) {
            Log.e("onError:", throwable.getMessage());
            if (!throwable.getMessage().isEmpty()) {
                if (activityWeakReference != null && activityWeakReference.get() != null)
                    Toast.makeText(activityWeakReference.get(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
//                ToastUtils.show(throwable.getMessage());
            }
        }
    }
}
