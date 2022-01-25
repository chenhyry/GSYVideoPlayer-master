package com.hxty.schoolnet.entity;

public class Version {
    private int KeyId;
    private int CurVersion;
    private String AppNameEN;
    private String AppNameCN;
    private String Url;
    private String Remark;

    public int getKeyId() {
        return KeyId;
    }

    public void setKeyId(int keyId) {
        KeyId = keyId;
    }

    public int getCurVersion() {
        return CurVersion;
    }

    public void setCurVersion(int curVersion) {
        CurVersion = curVersion;
    }

    public String getAppNameEN() {
        return AppNameEN;
    }

    public void setAppNameEN(String appNameEN) {
        AppNameEN = appNameEN;
    }

    public String getAppNameCN() {
        return AppNameCN;
    }

    public void setAppNameCN(String appNameCN) {
        AppNameCN = appNameCN;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

    public String getRemark() {
        return Remark;
    }

    public void setRemark(String remark) {
        Remark = remark;
    }
}
