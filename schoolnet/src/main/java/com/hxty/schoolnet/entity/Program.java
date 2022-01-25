package com.hxty.schoolnet.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class Program implements Parcelable{

	private int KeyId;
	private int ColumnId;
	private int ProgramType;//节目类型：1：视频 2：图文
	private String ProgramTitle;//节目名称
	private String SubTitle;//副标题
	private String ReleaseTime;
	private String Author;
	private int State;
	private int IsDelete;
	private int Sortnum;
	private String LinkUrl;
	private String ImgUrl;
	private String CreateTime;
	private int CreateUserId;
	private int ClickTimes;
	private String Remark;
	private String ProgramContent;
	private boolean isNew;


	public Program(int keyId, int columnId, int programType, String programTitle, String subTitle, String releaseTime, String author,
				   int state, int isDelete, int sortnum, String linkUrl, String imgUrl, String createTime,
				   int createUserId, int clickTimes, String remark, String programContent) {
		KeyId = keyId;
		ColumnId = columnId;
		ProgramType = programType;
		ProgramTitle = programTitle;
		SubTitle = subTitle;
		ReleaseTime = releaseTime;
		Author = author;
		State = state;
		IsDelete = isDelete;
		Sortnum = sortnum;
		LinkUrl = linkUrl;
		ImgUrl = imgUrl;
		CreateTime = createTime;
		CreateUserId = createUserId;
		ClickTimes = clickTimes;
		Remark = remark;
		ProgramContent = programContent;
	}

	// 重写describeContents方法，内容接口描述，默认返回0就可以，基本不用
	@Override
	public int describeContents() {
		return 0;
	}

	// 重写writeToParcel方法，将你的对象序列化为一个Parcel对象，即：将类的数据写入外部提供的Parcel中，打包需要传递的数据到Parcel容器保存，以便从
	// Parcel容器获取数据
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(KeyId);
		dest.writeInt(ColumnId);
		dest.writeInt(ProgramType);
		dest.writeString(ProgramTitle);
		dest.writeString(SubTitle);
		dest.writeString(ReleaseTime);
		dest.writeString(Author);
		dest.writeInt(State);
		dest.writeInt(IsDelete);
		dest.writeInt(Sortnum);
		dest.writeString(LinkUrl);
		dest.writeString(ImgUrl);
		dest.writeString(CreateTime);
		dest.writeInt(CreateUserId);
		dest.writeInt(ClickTimes);
		dest.writeString(Remark);
		dest.writeString(ProgramContent);
	}

	/**
	 * 其中public static
	 * final一个都不能少，内部对象CREATOR的名称也不能改变，必须全部大写。需重写本接口中的两个方法：createFromParcel
	 * (Parcel in) 实现从Parcel容器中读取传递数据值，封装成Parcelable对象返回逻辑层，newArray(int size)
	 * 创建一个类型为T，长度为size的数组，仅一句话即可（return new T[size]），供外部类反序列化本类数组使用。
	 */
	public static final Creator<Program> CREATOR = new Creator<Program>() {

		@Override
		public Program createFromParcel(Parcel source) {
			return new Program(source.readInt(),source.readInt(),source.readInt(), source.readString(),source.readString(),source.readString(),source.readString(),
					source.readInt(),source.readInt(),source.readInt(),source.readString(),source.readString(),source.readString(),source.readInt(),source.readInt(),source.readString(),source.readString());
		}

		@Override
		public Program[] newArray(int size) {
			return new Program[size];
		}

	};

	public int getKeyId() {
		return KeyId;
	}

	public void setKeyId(int keyId) {
		KeyId = keyId;
	}

	public int getColumnId() {
		return ColumnId;
	}

	public void setColumnId(int columnId) {
		ColumnId = columnId;
	}

	public int getProgramType() {
		return ProgramType;
	}

	public void setProgramType(int programType) {
		ProgramType = programType;
	}

	public String getProgramTitle() {
		return ProgramTitle;
	}

	public void setProgramTitle(String programTitle) {
		ProgramTitle = programTitle;
	}

	public String getSubTitle() {
		return SubTitle;
	}

	public void setSubTitle(String subTitle) {
		SubTitle = subTitle;
	}

	public String getReleaseTime() {
		return ReleaseTime;
	}

	public void setReleaseTime(String releaseTime) {
		ReleaseTime = releaseTime;
	}

	public String getAuthor() {
		return Author;
	}

	public void setAuthor(String author) {
		Author = author;
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

	public int getSortnum() {
		return Sortnum;
	}

	public void setSortnum(int sortnum) {
		Sortnum = sortnum;
	}

	public String getLinkUrl() {
		return LinkUrl;
	}

	public void setLinkUrl(String linkUrl) {
		LinkUrl = linkUrl;
	}

	public String getImgUrl() {
		return ImgUrl;
	}

	public void setImgUrl(String imgUrl) {
		ImgUrl = imgUrl;
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

	public int getClickTimes() {
		return ClickTimes;
	}

	public void setClickTimes(int clickTimes) {
		ClickTimes = clickTimes;
	}

	public String getRemark() {
		return Remark;
	}

	public void setRemark(String remark) {
		Remark = remark;
	}

	public String getProgramContent() {
		return ProgramContent;
	}

	public void setProgramContent(String programContent) {
		ProgramContent = programContent;
	}

	public static Creator<Program> getCREATOR() {
		return CREATOR;
	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean aNew) {
		isNew = aNew;
	}
}
