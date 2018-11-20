package cld.kcloud.custom.manager;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import cld.kcloud.center.KCloudCtx;
import cld.kcloud.center.KCloudAppUtils.CLDMessageId;
import cld.kcloud.user.KCloudUser;

import com.cld.cc.util.kcloud.ucenter.kcenter.CldSysMessageParce;
import com.cld.cc.util.kcloud.ucenter.kcenter.IKMsg;
import com.cld.log.CldLog;
import com.cld.ols.api.CldKAccountAPI;

public class KCloudMsgBoxManager {
	
	private static final String TAG = "KCloudMsgBoxManager";
	private static final int MAX_MESSAGE_NUM = 20;
	private static final String CLD_MSG_UNREAD_COUNT = "CLD_MSG_UNREAD_COUNT";
	private static final String CLD_MSG_BROADCAST_UNREADMESSAGE	= "CLD.LAUNCHER.BROADCAST.UNREADMESSAGE";
	
	private IKMsg mBinder = null;
	private Context mContext = null;
	private ServiceConnection mConn = null;
	private List<CldSysMessageParce> mList = new ArrayList<CldSysMessageParce>();
	private static KCloudMsgBoxManager mKCloudMsgBoxMgr = null;
	
	public static KCloudMsgBoxManager getInstance() {
		if (mKCloudMsgBoxMgr == null) {
			synchronized(KCloudMsgBoxManager.class) {
				if (mKCloudMsgBoxMgr == null) {
					mKCloudMsgBoxMgr = new KCloudMsgBoxManager();
				}
			}
		}
		return mKCloudMsgBoxMgr;
	}
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				mHandler.removeMessages(0);
				
				if (mBinder == null && mConn != null) {
					Intent intent = new Intent("cld.navi.kcenter.service");
					mContext.bindService(intent, mConn, Context.BIND_AUTO_CREATE);
				}
				
				if (mBinder == null || !CldKAccountAPI.getInstance().isLogined()) {
					mHandler.sendEmptyMessageDelayed(0, 2*1000);
					return ;
				}

				mHandler.sendEmptyMessageDelayed(0, 30*1000);
				List<CldSysMessageParce> list = null;
				try {
					list = mBinder.getUserMsgHitory(CldKAccountAPI.getInstance().getKuid(), MAX_MESSAGE_NUM);
					if (list != null && !list.isEmpty()) {

						if (!list.equals(mList)) {
							mList.clear();
							mList.addAll(list);
							// 有更新时通知UI更新
							KCloudUser.getInstance().sendMessage(CLDMessageId.MSG_ID_MSGBOX_UPDATE, 0);
						}
						
						int unReadNum = getUnReadMsgNum();
						Intent broadcast = new Intent(CLD_MSG_BROADCAST_UNREADMESSAGE);
						broadcast.putExtra(CLD_MSG_UNREAD_COUNT, unReadNum);
						KCloudCtx.getAppContext().sendBroadcast(broadcast, null);
						CldLog.i(TAG, "unReadNum = " + unReadNum);
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				catch (NullPointerException e) {
					e.printStackTrace();
				}

			}
		}
	};
	
	public void init(Context context) {
		Intent intent = new Intent("cld.navi.kcenter.service");
		
		mContext = context;
		mConn = new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName name) {
				mBinder = null;
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mBinder = IKMsg.Stub.asInterface(service);
				
				CldLog.d(TAG, "cld.navi.kcenter.service connected:");
			}
		};
		
		context.bindService(intent, mConn, Context.BIND_AUTO_CREATE);
		mHandler.sendEmptyMessage(0);
	}
	
	public List<CldSysMessageParce> getMsgList() {
		return mList;
	}
	
	public int getMsgNum() {
		return mList.size();
	}
	
	public int getUnReadMsgNum() {
		if (mList.size() <= 0) {
			return 0;
		}
		
		int num = 0;
		for (CldSysMessageParce item : mList) {
			if (item.getReadMark() == 2) {
				// 存在未读消息
				num++;
			}
		}
		
		return num;
	}
	
	public void setMsgMark(long msgId) {
		if (mBinder == null) {
			return ;
		}
		
		if (mList.isEmpty()) {
			return ;
		}
		
		CldSysMessageParce info = null;
		for (int i = 0; i < mList.size(); i++) {
			if (msgId == mList.get(i).getMessageId()) {
				mList.get(i).setReadMark(3);
				info = mList.get(i);
				break ;
			}
		}
		
		try {
			List<CldSysMessageParce> list = new ArrayList<CldSysMessageParce>();
			list.add(info);
			mBinder.updateMsgReadStatus(list, true);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		int unReadNum = getUnReadMsgNum();
		if (unReadNum <= 0) {
			Intent broadcast = new Intent(CLD_MSG_BROADCAST_UNREADMESSAGE);
			broadcast.putExtra(CLD_MSG_UNREAD_COUNT, unReadNum);
			KCloudCtx.getAppContext().sendBroadcast(broadcast, null);
		}
	}
}
