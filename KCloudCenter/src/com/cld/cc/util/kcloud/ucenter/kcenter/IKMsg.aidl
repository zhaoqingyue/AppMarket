package com.cld.cc.util.kcloud.ucenter.kcenter;

import android.os.Parcelable;
import com.cld.cc.util.kcloud.ucenter.kcenter.CldSysMessageParce;

interface IKMsg{
	List<CldSysMessageParce> getDeviceMsgHistory(in int size);
	List<CldSysMessageParce> getDeviceMsgHistoryDownOrUp(in long lastid,  in long lasttime, in int size, in boolean isDownOrUp);
	List<CldSysMessageParce> getUserMsgHitory(in long kuid, in int size);
	List<CldSysMessageParce> getUserMsgHistoryDownOrUp(in long kuid, in long lastid, in long lasttime, in int size, in boolean isDownOrUp);
	void saveMsgHitory(in List<CldSysMessageParce> lstMsg, in long kuid);
	void updateMsgReadStatus(in List<CldSysMessageParce> clickLst, in boolean isUp);
	List<CldSysMessageParce> getReadMsg();
	int getUnReadMsgCount(in long kuid);
}