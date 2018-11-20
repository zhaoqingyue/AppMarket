package cld.kcloud.user;

import cld.kcloud.center.KCloudAppUtils;
import cld.kcloud.center.R;
import cld.kcloud.center.KCloudAppUtils.CLDMessageId;
import cld.kcloud.center.KCloudCtx;
import cld.kcloud.custom.bean.KCloudUserInfo;
import cld.kcloud.custom.bean.KCloudUserInfo.ChangeTaskEnum;
import cld.kcloud.custom.manager.KCloudFlowManager;
import cld.kcloud.custom.manager.KCloudHeartbeatManager;
import cld.kcloud.custom.manager.KCloudMsgBoxManager;
import cld.kcloud.custom.manager.KCloudPackageManager;
import cld.kcloud.custom.manager.KCloudPositionManager;
import cld.kcloud.custom.manager.KCloudSimCardManager;
import cld.kcloud.custom.manager.KCloudSplashManager;
import cld.kcloud.custom.manager.KCloudUpgradeManager;
import cld.kcloud.utils.KCloudAccountUtils;
import cld.kcloud.utils.KCloudLocationUtils;
import cld.kcloud.utils.KCloudNetworkUtils;
import cld.kcloud.utils.KCloudShareUtils;
import cld.kcloud.utils.KCloudCommonUtil;
import cld.kcloud.utils.sap.KCloudNetworkSap;

import com.cld.log.CldLog;
import com.cld.ols.api.CldKAccountAPI;
import com.cld.ols.sap.CldSapKAccount;
import com.cld.ols.sap.CldSapUtil;
import com.cld.ols.sap.bean.CldSapKAParm.CldUserInfo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

public class KCloudUser {
	private static final String TAG = "KCloudUser";
	private static KCloudUser mKCloudUser = null;
	private CldOnMessageInterface mOnMessageListener = null;
	// mKCloudUserInfo 正式，mKCloudUserInfoTmp 临时
	private static KCloudUserInfo mKCloudUserInfo = null, mKCloudUserInfoTmp = null;	

	public static KCloudUser getInstance() {
		if (mKCloudUser == null) {
			synchronized(KCloudUser.class) {
				if (mKCloudUser == null) {
					mKCloudUser = new KCloudUser();
				}
			}
		}
		return mKCloudUser;
	}
	
	public void init() {
		CldLog.i(TAG, "init KCloudUser");
        // 初始化相关状态
		KCloudShareUtils.init();
		KCloudAccountUtils.init();
		KCloudLocationUtils.init();

		// 个人信息存储
		mKCloudUserInfo = new KCloudUserInfo();
		mKCloudUserInfoTmp = new KCloudUserInfo();
	
		// 设置登录状态
		setLoginStatus(0);
		
		// 闪屏处理
		KCloudSplashManager.getInstance().init();
		//消息
		KCloudMsgBoxManager.getInstance().init(KCloudCtx.getAppContext());
		//心跳(300s发一次)
		KCloudHeartbeatManager.getInstance().init();
		//应用升级(60分钟获取一次)
		KCloudUpgradeManager.getInstance(KCloudCtx.getAppContext()).init();
		//位置上报
		KCloudPositionManager.getInstance().init();
		
		KCloudShareUtils.put("flowcard_status", 1);
		KCloudShareUtils.put("simcard_status", 
				KCloudSimCardManager.getInstance().getSimStatus());
		
		//KCloudFlowManager.getInstance().init();
		//KCloudPackageManager.getInstance().init();
		/**
		 * 不能在此初始化SimCard检测，防止Launcher调用bindService后，
		 * 还没有调用start_KLD_kcenter(), 就已经执行SimCard检测初始化，
		 * 导致SIM卡检测结果在服务协议界面上
		 */
		//KCloudSimCardManager.getInstance().init();
		
		mHandler.sendEmptyMessageDelayed(CLDMessageId.MSG_ID_CHECK_SESSION_INVAILD, 60*1000);
	}
	
	public void uninit() {
		CldLog.i(TAG, "uninit");
		setLoginStatus(0);
		KCloudFlowManager.getInstance().uninit();
		KCloudSplashManager.getInstance().uninit();
		if (CldKAccountAPI.getInstance().isLogined()) {
			CldKAccountAPI.getInstance().loginOut();
		}
		CldKAccountAPI.getInstance().uninit();
	}
	
	@SuppressLint("HandlerLeak") 
	private Handler mHandler = new Handler() {
		@SuppressLint("NewApi") 
		public void handleMessage(Message msg) {
			CldLog.i(TAG, String.valueOf(msg.what));
			switch (msg.what) {
			case CLDMessageId.MSG_ID_CHECK_SESSION_INVAILD: 
				mHandler.removeMessages(CLDMessageId.MSG_ID_CHECK_SESSION_INVAILD);
				mHandler.sendEmptyMessageDelayed(
						CLDMessageId.MSG_ID_CHECK_SESSION_INVAILD, 60*1000);
				if (CldKAccountAPI.getInstance().isLogined()) {
					KCloudNetworkUtils.checkSessionInvaild();
				}
				break;
				
			case CLDMessageId.MSG_ID_LOGIN_MOBILE_LOGIN_SUCCESS:
			case CLDMessageId.MSG_ID_LOGIN_ACCOUNT_LOGIN_SUCCESS: {
				KCloudNetworkSap.setAccountKey(CldSapKAccount.keyCode);
				// 设置登录状态
				setLoginStatus(2);
				KCloudShareUtils.put(KCloudAppUtils.TARGET_FIELD_DUID, 
						CldKAccountAPI.getInstance().getDuid());
				KCloudShareUtils.put(KCloudAppUtils.TARGET_FIELD_KUID, 
						CldKAccountAPI.getInstance().getKuid());
				
				/*CldUserInfo info = CldKAccountAPI.getInstance().getUserInfoDetail();
				CldLog.e(TAG, " ************************* ");
				CldLog.e(TAG, " UserName: " + info.getLoginName());
				CldLog.e(TAG, "UserAlias: " + info.getUserAlias());
				CldLog.e(TAG, "      Sex: " + info.getSex());
				CldLog.e(TAG, " DistName: " + info.getAddress());
				CldLog.e(TAG, "   Mobile: " + info.getMobile());
				CldLog.e(TAG, " ************************* ");*/
				
				// 获取详细信息
				CldKAccountAPI.getInstance().getUserInfo();
				
				//登录成功后，取套餐信息
				if (KCloudSimCardManager.getInstance().getSimStatus() == 1 &&
					KCloudPackageManager.getInstance().getTaskStatus() == 
					KCloudPackageManager.TASK_NONE)
				{
					KCloudPackageManager.getInstance().init();
				}
				break;
			}
			
			case CLDMessageId.MSG_ID_LOGIN_AUTO_LOGIN_SUCCESS:
			case CLDMessageId.MSG_ID_LOGIN_QRCODE_LOGIN_SUCCESS: {
				KCloudNetworkSap.setAccountKey(CldSapKAccount.keyCode);
				
				/*String key = CldSapKAccount.key;
				String sex = "mQPVKTpp3VO7jyJZojnTNA==";
				String dd = CldSapUtil.CldAescrpy.decrypt(key, sex);
				CldLog.e(TAG, "   dd: " + dd);

				CldUserInfo info = CldKAccountAPI.getInstance().getUserInfoDetail();
				CldLog.e(TAG, " ************************* ");
				CldLog.e(TAG, " UserName: " + info.getLoginName());
				CldLog.e(TAG, "UserAlias: " + info.getUserAlias());
				CldLog.e(TAG, "      Sex: " + info.getSex());
				CldLog.e(TAG, " DistName: " + info.getAddress());
				CldLog.e(TAG, "   Mobile: " + info.getMobile());
				CldLog.e(TAG, " ************************* ");*/
				
				// 获取详细信息
				CldKAccountAPI.getInstance().getUserInfo();
				// 保存登录状态
				setLoginStatus(2);
				break;
			}

			// 获取用户详细信息
			case CLDMessageId.MSG_ID_USERINFO_GETDETAIL_SUCCESS:
			case CLDMessageId.MSG_ID_USERINFO_GETDETAIL_FAILED: {
				// 初始化个人信息
				CldUserInfo info = CldKAccountAPI.getInstance().getUserInfoDetail();
				/*CldLog.e(TAG, " ************************* ");
				CldLog.e(TAG, " UserName: " + info.getLoginName());
				CldLog.e(TAG, "UserAlias: " + info.getUserAlias());
				CldLog.e(TAG, "      Sex: " + info.getSex());
				CldLog.e(TAG, " DistName: " + info.getAddress());
				CldLog.e(TAG, "   Mobile: " + info.getMobile());
				CldLog.e(TAG, " ************************* ");*/
				
				KCloudShareUtils.put(KCloudAppUtils.TAGGET_FIELD_USERNAME, 
						CldKAccountAPI.getInstance().getLoginName());
				KCloudShareUtils.put(KCloudAppUtils.TAGGET_FIELD_PASSWORD,
						CldKAccountAPI.getInstance().getLoginPwd());
				//昵称为空，则为"未设置"
				String nickname = info.getUserAlias();
				if (nickname == null || nickname.isEmpty()) {
					nickname = KCloudCommonUtil.getString(R.string.setting_unset);
				}
				//保存昵称，用于快捷设置上显示
				KCloudShareUtils.put(KCloudAppUtils.TAGGET_FIELD_NICKNAME, nickname);
				mKCloudUserInfo.setSuccess(msg.arg1);	
				mKCloudUserInfo.setSex(info.getSex());
				mKCloudUserInfo.setUserName(info.getLoginName());
				mKCloudUserInfo.setUserAlias(info.getUserAlias());
				mKCloudUserInfo.setDistName(info.getAddress());
				mKCloudUserInfo.setMobile(info.getMobile());
				
				mKCloudUserInfoTmp.assignVaule(mKCloudUserInfo);	// 备份
				KCloudShareUtils.put("bindMobile", info.getMobile());

				break;
			}

			// 更新用户信息
			case CLDMessageId.MSG_ID_USERINFO_UPDATE_SUCCESS: {
				int[] status = mKCloudUserInfoTmp.getChangeStatus(); 
				
				if (status[0] == 1) {
					mKCloudUserInfoTmp.resetChangeStatus(ChangeTaskEnum.eSEX);
					mKCloudUserInfo.setSex(KCloudCommonUtil.getString(R.string.setting_male)
							.equals(mKCloudUserInfoTmp.getSex()) ? 2 : 1);
				} else if (status[1] == 1) {
					mKCloudUserInfoTmp.resetChangeStatus(ChangeTaskEnum.eUSERALIAS);
					mKCloudUserInfo.setUserAlias(mKCloudUserInfoTmp.getUserAlias());
				} else if (status[2] == 1) {
					mKCloudUserInfoTmp.resetChangeStatus(ChangeTaskEnum.eDISTNAME);
					mKCloudUserInfo.setDistName(mKCloudUserInfoTmp.getDistName());
				}
				break;
			}
			
			// 更新用户信息失败
			case CLDMessageId.MSG_ID_USERINFO_UPDATE_FAILED: {
				mKCloudUserInfoTmp.resetChangeStatus(ChangeTaskEnum.eALL);
				break;
			}
			case CLDMessageId.MSG_ID_LOGIN_MOBILE_LOGIN_FAILED:
			case CLDMessageId.MSG_ID_LOGIN_ACCOUNT_LOGIN_FAILED:
			case CLDMessageId.MSG_ID_LOGIN_AUTO_LOGIN_FAILED:
				setLoginStatus(0);
				break;

			default:
				break;
			}
			
			if (mOnMessageListener != null) {
				mOnMessageListener.OnHandleMessage(msg);
			}
		}
	};
	
	/**
	 * 获取用户可修改信息
	 */
	public KCloudUserInfo getUserInfo() {
		return mKCloudUserInfo;
	}
	
	/**
	 * 获取用户临时信息
	 */
	public KCloudUserInfo getTmpUserInfo() {
		return mKCloudUserInfoTmp;
	}
	
	/**
	 * 通知登录状态变更
	 * 0: 未登录； 1：登录中； 2：登录成功
	 */
	@SuppressLint("NewApi") 
	public void setLoginStatus(int status) {
		KCloudShareUtils.put(KCloudAppUtils.TARGET_FIELD_LOGIN_STATUS, status);
		Intent broadcast = new Intent("kclound_login_status");
		broadcast.putExtra("status", status);
		//昵称为空，则为"未设置"
		String nickname = mKCloudUserInfo.getUserAlias();
		if (nickname == null || nickname.isEmpty()) {
			nickname = KCloudCommonUtil.getString(R.string.setting_unset);
		}
		broadcast.putExtra("nickname", nickname);
		KCloudCtx.getAppContext().sendBroadcast(broadcast, null);
	}
	
	/**
	 * 
	 * @param listener
	 */
	public void setOnMessageListener(CldOnMessageInterface listener) {
		mOnMessageListener = listener;
	}
	
	/**
	 * 发送消息
	 * @param msgId
	 * @param obj
	 */
	public void sendMessage(int msgId, int obj) {
		Message msg = mHandler.obtainMessage(); 
		msg.what = msgId;
		msg.obj = obj;

		mHandler.removeMessages(msgId);
	    mHandler.sendMessage(msg);
	}
	
	/**
	 * 发送消息
	 * @param msgId
	 * @param obj
	 * @param delayMillis
	 */
	public void sendMessageDelayed(int msgId, int obj, long delayMillis) {
		Message msg = mHandler.obtainMessage(); 
		msg.what = msgId;
		msg.obj = obj;

		mHandler.removeMessages(msgId);
	    mHandler.sendMessageDelayed(msg, delayMillis);
	}
	
	public static abstract interface CldOnMessageInterface {
		public abstract void OnHandleMessage(Message paramMessage);
	}
}