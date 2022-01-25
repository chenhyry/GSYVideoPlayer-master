package com.hxty.schoolnet.entity;

import androidx.annotation.NonNull;

public class FirstLevelMenu implements Comparable<FirstLevelMenu> {
    private int KeyId;
    private int CommunityTreeId;
    private int SchoolId;
    private String ColumnName;
    private int ParentId;
    private int Sortnum;
    private int State;
    private int IsDelete;
    private String ImgUrl1;
    private String ImgUrl2;
    private String ImgUrl3;
    private String CreateTime;
    private int CreateUserId;
    private String Remark;
    private int DisplayStyle;//横图竖图方图
    private int DisplayProgram;//下级是栏目还是节目 已废弃
    private String CommunityTreeName;
    private String SchoolsName;
    private String ReleaseTime;
    private int x;
    private int y;
    private boolean isjiaoWeiLanMu;//是否是教委栏目

    public int getKeyId() {
        return KeyId;
    }

    public void setKeyId(int keyId) {
        KeyId = keyId;
    }

    public int getCommunityTreeId() {
        return CommunityTreeId;
    }

    public void setCommunityTreeId(int communityTreeId) {
        CommunityTreeId = communityTreeId;
    }

    public int getSchoolId() {
        return SchoolId;
    }

    public void setSchoolId(int schoolId) {
        SchoolId = schoolId;
    }

    public String getColumnName() {
        return ColumnName;
    }

    public void setColumnName(String columnName) {
        ColumnName = columnName;
    }

    public int getParentId() {
        return ParentId;
    }

    public void setParentId(int parentId) {
        ParentId = parentId;
    }

    public int getSortnum() {
        return Sortnum;
    }

    public void setSortnum(int sortnum) {
        Sortnum = sortnum;
    }

    public int getState() {
        return State;
    }

    public void setState(int state) {
        State = state;
    }

    public int getIsDelete() {
        return IsDelete;
    }

    public void setIsDelete(int isDelete) {
        IsDelete = isDelete;
    }

    public String getImgUrl1() {
        return ImgUrl1;
    }

    public void setImgUrl1(String imgUrl1) {
        ImgUrl1 = imgUrl1;
    }

    public String getImgUrl2() {
        return ImgUrl2;
    }

    public void setImgUrl2(String imgUrl2) {
        ImgUrl2 = imgUrl2;
    }

    public String getImgUrl3() {
        return ImgUrl3;
    }

    public void setImgUrl3(String imgUrl3) {
        ImgUrl3 = imgUrl3;
    }

    public String getCreateTime() {
        return CreateTime;
    }

    public void setCreateTime(String createTime) {
        CreateTime = createTime;
    }

    public int getCreateUserId() {
        return CreateUserId;
    }

    public void setCreateUserId(int createUserId) {
        CreateUserId = createUserId;
    }

    public String getRemark() {
        return Remark;
    }

    public void setRemark(String remark) {
        Remark = remark;
    }

    public int getDisplayStyle() {
        return DisplayStyle;
    }

    public void setDisplayStyle(int displayStyle) {
        DisplayStyle = displayStyle;
    }

    public int getDisplayProgram() {
        return DisplayProgram;
    }

    public void setDisplayProgram(int displayProgram) {
        DisplayProgram = displayProgram;
    }

    public String getCommunityTreeName() {
        return CommunityTreeName;
    }

    public void setCommunityTreeName(String communityTreeName) {
        CommunityTreeName = communityTreeName;
    }

    public String getSchoolsName() {
        return SchoolsName;
    }

    public void setSchoolsName(String schoolsName) {
        SchoolsName = schoolsName;
    }

    public String getReleaseTime() {
        return ReleaseTime;
    }

    public void setReleaseTime(String releaseTime) {
        ReleaseTime = releaseTime;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isIsjiaoWeiLanMu() {
        return isjiaoWeiLanMu;
    }

    public void setIsjiaoWeiLanMu(boolean isjiaoWeiLanMu) {
        this.isjiaoWeiLanMu = isjiaoWeiLanMu;
    }

    @Override
    public int compareTo(@NonNull FirstLevelMenu o) {
        return this.getSortnum() - o.getSortnum();
    }
}
