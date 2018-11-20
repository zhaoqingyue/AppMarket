package com.cld.cc.util.kcloud.ucenter.kcenter;

import android.os.Parcel;
import android.os.Parcelable;

public class CldSysMessageParce implements Parcelable {
	
	private String title;        // 名称
	private String content;      // 内容
	private long DownloadTime;   // 接收时间
	private long messageId;      // 消息Id
	private int ReadMark;        // 是否已读 3:已读; 2: 未读
	private String Wherecomfrom; // 消息来自
	private String createuser;   // 构造者名称
	private long createtime;     // 构造时间
	private String hyperlink;    // 超链接
	private long createuserid;   // 构造者ID
	private int apptype;         // 应用类型
	private int poptype;         // 弹出表现形式
	// 运营消息特有
	private String imageurl;     // 图片链接 指定区域属性才有
	private String roadname;     // 道路名称 指定区域属性才有
	private int msgType;         // 消息类型 1:活动消息; 2:升级消息; 3:区域消息; 11:POI; 12:路径; 13:路书; 14:路况; 15:一键通
	private int module;          // 消息所属模块(1:K云; 2:WEB地图; 3:一键通)
	private int createType;      // 构造类型（1：运营；2：终端）
	private int receiveObject;
	private String strJson;      // 携带原始Json数据

	public CldSysMessageParce() {
		
	}
	public CldSysMessageParce(Parcel dest) {
		title = dest.readString();
		content = dest.readString();
		DownloadTime = dest.readLong();
		messageId = dest.readLong();
		ReadMark = dest.readInt();
		Wherecomfrom = dest.readString();
		createuser = dest.readString();
		createtime = dest.readLong();
		hyperlink = dest.readString();
		createuserid = dest.readLong();
		apptype = dest.readInt();
		poptype = dest.readInt();
		// 运营消息特有
		imageurl = dest.readString();
		roadname = dest.readString();
		msgType = dest.readInt();
		module = dest.readInt();
		createType = dest.readInt();
		receiveObject = dest.readInt();
		strJson = dest.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		CldSysMessageParce info = (CldSysMessageParce)o;
		if (this.messageId == info.getMessageId()) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeString(content);
		dest.writeLong(DownloadTime);
		dest.writeLong(messageId);
		dest.writeInt(ReadMark);
		dest.writeString(Wherecomfrom);
		dest.writeString(createuser);
		dest.writeLong(createtime);
		dest.writeString(hyperlink);
		dest.writeLong(createuserid);
		dest.writeInt(apptype);
		dest.writeInt(poptype);
		// 运营消息特有
		dest.writeString(imageurl);
		dest.writeString(roadname);
		dest.writeInt(msgType);
		dest.writeInt(module);
		dest.writeInt(createType);
		dest.writeInt(receiveObject);
		dest.writeString(strJson);
	}
	
	public int getReceiveObject() {
		return receiveObject;
	}

	public void setReceiveObject(int receiveObject) {
		this.receiveObject = receiveObject;
	}

	public String getCreateuser() {
		return createuser;
	}

	public void setCreateuser(String createuser) {
		this.createuser = createuser;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public int getModule() {
		return module;
	}

	public void setModule(int module) {
		this.module = module;
	}

	public int getCreateType() {
		return createType;
	}

	public void setCreateType(int createType) {
		this.createType = createType;
	}

	public String getTitle() {
		return title;
	}

	public long getDownloadTime() {
		return DownloadTime;
	}

	public void setDownloadTime(long downloadTime) {
		DownloadTime = downloadTime;
	}

	public long getCreatetime() {
		return createtime;
	}

	public String getImageurl() {
		return imageurl;
	}

	public void setImageurl(String imageurl) {
		this.imageurl = imageurl;
	}

	public String getRoadname() {
		return roadname;
	}

	public void setRoadname(String roadname) {
		this.roadname = roadname;
	}

	public void setCreatetime(long createtime) {
		this.createtime = createtime;
	}

	public String getHyperlink() {
		return hyperlink;
	}

	public void setHyperlink(String hyperlink) {
		this.hyperlink = hyperlink;
	}

	public long getCreateuserid() {
		return createuserid;
	}

	public void setCreateuserid(long createuserid) {
		this.createuserid = createuserid;
	}

	public int getApptype() {
		return apptype;
	}

	public void setApptype(int apptype) {
		this.apptype = apptype;
	}

	public int getPoptype() {
		return poptype;
	}

	public void setPoptype(int poptype) {
		this.poptype = poptype;
	}

	public long getMessageId() {
		return messageId;
	}

	public void setMessageId(long messageId) {
		this.messageId = messageId;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getReadMark() {
		return ReadMark;
	}

	public void setReadMark(int readMark) {
		ReadMark = readMark;
	}

	public String getWherecomfrom() {
		return Wherecomfrom;
	}

	public void setWherecomfrom(String wherecomfrom) {
		Wherecomfrom = wherecomfrom;
	}

	/** @return the strJson */
	public String getStrJson() {
		return strJson;
	}

	/**
	 * @param strJson the strJson to set
	 */
	public void setStrJson(String strJson) {
		this.strJson = strJson;
	}

	public static final Parcelable.Creator<CldSysMessageParce> CREATOR = new Parcelable.Creator<CldSysMessageParce>() {

		@Override
		public CldSysMessageParce createFromParcel(Parcel source) {
			return new CldSysMessageParce(source);
		}

		@Override
		public CldSysMessageParce[] newArray(int size) {
			return new CldSysMessageParce[size];
		}
	};
}
