package com.cld.cc.util.kcloud.ucenter.kcenter;

import android.os.Parcel;
import android.os.Parcelable;

public class CldSysMessageParce implements Parcelable {
	
	private String title;        // ����
	private String content;      // ����
	private long DownloadTime;   // ����ʱ��
	private long messageId;      // ��ϢId
	private int ReadMark;        // �Ƿ��Ѷ� 3:�Ѷ�; 2: δ��
	private String Wherecomfrom; // ��Ϣ����
	private String createuser;   // ����������
	private long createtime;     // ����ʱ��
	private String hyperlink;    // ������
	private long createuserid;   // ������ID
	private int apptype;         // Ӧ������
	private int poptype;         // ����������ʽ
	// ��Ӫ��Ϣ����
	private String imageurl;     // ͼƬ���� ָ���������Բ���
	private String roadname;     // ��·���� ָ���������Բ���
	private int msgType;         // ��Ϣ���� 1:���Ϣ; 2:������Ϣ; 3:������Ϣ; 11:POI; 12:·��; 13:·��; 14:·��; 15:һ��ͨ
	private int module;          // ��Ϣ����ģ��(1:K��; 2:WEB��ͼ; 3:һ��ͨ)
	private int createType;      // �������ͣ�1����Ӫ��2���նˣ�
	private int receiveObject;
	private String strJson;      // Я��ԭʼJson����

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
		// ��Ӫ��Ϣ����
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
		// ��Ӫ��Ϣ����
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
