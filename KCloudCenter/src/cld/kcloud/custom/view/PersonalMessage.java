package cld.kcloud.custom.view;

public class PersonalMessage {
	
	private long msgId;
	private int msgType;
	private long msgTime;
	private String msgTitle;
	private String msgContent;
	private int msgMark;	//  «∑Ò“—∂¡ 3“—∂¡ 2 Œ¥∂¡
	
	public PersonalMessage() {
		this.msgId = 0;
		this.msgTime = 0;
		this.msgType = 0;
		this.msgTitle = "";
		this.msgContent = "";
		this.msgMark = 0; 
	}
	
	public PersonalMessage(long msgId, int msgType, int msgMark, long msgTime, String msgTitle, String msgContent) {
		this.msgId = msgId;
		this.msgTime = msgTime;	
		this.msgType = msgType;
		this.msgMark = msgMark;
		this.msgTitle = msgTitle;
		this.msgContent = msgContent;
	}
	
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
	
	public long getMsgId() {
		return this.msgId;
	}
	
	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}
	
	public int getMsgType() {
		return this.msgType;
	}
	
	public void setMsgMark(int msgMark) {
		this.msgMark = msgMark;
	}
	
	public int getMsgMark() {
		return this.msgMark;
	}
	
	public void setTime(long msgTime) {
		this.msgTime = msgTime;
	}
	
	public long getTime() {
		return this.msgTime;
	}
	
	public void setMsgTitle(String msgTitle) {
		this.msgTitle = msgTitle;
	}
	
	public String getMsgTitle() {
		return this.msgTitle;
	}
	
	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}
	
	public String getMsgContent() {
		return this.msgContent;
	}
}
