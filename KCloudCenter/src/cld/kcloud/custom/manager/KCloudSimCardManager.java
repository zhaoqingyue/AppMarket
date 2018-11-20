package cld.kcloud.custom.manager;

import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import cld.kcloud.center.KCloudAppUtils;
import cld.kcloud.center.KCloudAppUtils.CLDMessageId;
import cld.kcloud.center.KCloudCtx;
import cld.kcloud.center.KCloudDevice;
import cld.kcloud.center.R;
import cld.kcloud.utils.KCloudCommonUtil;
import cld.kcloud.utils.KCloudNetworkUtils;
import cld.kcloud.utils.KCloudShareUtils;
import cld.kcloud.utils.control.CldMessageDialog;
import cld.kcloud.utils.control.CldMessageDialog.CldMessageIcon;
import cld.kcloud.utils.control.CldMessageDialog.CldMessageType;
import cld.kcloud.utils.control.CldSimActivateDialog;
import com.cld.device.CldPhoneManager;
import com.cld.device.CldPhoneNet;
import com.cld.log.CldLog;
import com.cld.ols.api.CldKAccountAPI;
import com.cld.ols.tools.CldOlsThreadPool;

@SuppressLint("DefaultLocale") 
public class KCloudSimCardManager {
	
	public interface IKCloudSimCardListener {
		void onResult(String jsonString);
	}
	
	private static final int eSIM_NoCard = -1000; // 未插卡
	private static final int eSIM_None = -999;	  // 正在检测卡状态
	private static final int eSIM_Error_8 = -8;   // 网络异常
	private static final int eSIM_Error_7 = -7;   // 卡异常
	private static final int eSIM_Error_6 = -6;   // 已停用
	private static final int eSIM_Error_5 = -5;	  // 已失效     卡状态异常（未出库、已停用、已退货、卡流量异常将被停卡等异常）
	private static final int eSIM_Error_4 = -4;	  // ICCID与SN绑定关系与服务端不一致
	private static final int eSIM_Error_3 = -3;	  // 卡被锁定（卡被插在非法设备上）
	private static final int eSIM_Error_2 = -2;	  // 未激活的服务卡（ICCID与SN未绑定）
	private static final int eSIM_Error_1 = -1;	  // ICCID卡不存在（外来卡）
	private static final int eSIM_Error_0 = 0;	  // 其他错误
	private static final int eSIM_Normal = 1;	  // 注册成功

	private static final String CHECK_TIP = KCloudCommonUtil.getString(R.string.simcard_tip_check);
	private static final String CARD_NORMAL = KCloudCommonUtil.getString(R.string.simcard_tip_normal);
	private static final String NOCARD_TIP = KCloudCommonUtil.getString(R.string.simcard_tip_no_car);
	private static final String CARD_NOT_ACTIVATE_TIP = KCloudCommonUtil.getString(R.string.simcard_tip_car_unactivate);
	private static final String CARD_NOT_BELONG_TIP = KCloudCommonUtil.getString(R.string.simcard_tip_car_unbelong);
	private static final String DEVICES_NOT_BELONG_TIP = KCloudCommonUtil.getString(R.string.simcard_tip_device_unbelong);
	private static final String CARD_BIND_ERROR_TIP = KCloudCommonUtil.getString(R.string.simcard_tip_car_bind_error);
	private static final String NETWORK_ERROR_TIP = KCloudCommonUtil.getString(R.string.simcard_tip_network_error);
	
	private static final String TAG = "KCloudSimCardManager";
	private static KCloudSimCardManager mKCloudSimCardMgr = null;
	private int mStatus = eSIM_NoCard;
	
	public static KCloudSimCardManager getInstance() {
		if (mKCloudSimCardMgr == null) {
			synchronized(KCloudSimCardManager.class) {
				if (mKCloudSimCardMgr == null) {
					mKCloudSimCardMgr = new KCloudSimCardManager();
				}
			}
		}
		return mKCloudSimCardMgr;
	}

	private Handler mHandler = new Handler(KCloudCtx.getAppContext().getMainLooper()) {

		@SuppressLint("NewApi") 
		@Override
		public void handleMessage(Message msg) {
			//检测卡结果返回null, 要再次检测(最多3次) add by zhaoqy 2016-7-19
			if (msg.what == -1)
			{
				Log.d(TAG, "mCheckTimer: " + mCheckTimer);
				if (mCheckTimer > 0 && mCheckTimer < 3)
				{
					mHandler.sendEmptyMessageDelayed(-2, 1000);	
				}
				else if (mCheckTimer >= 3)
				{
					//检测卡结果
					KCloudCommonUtil.sendFreshFlowBroadcast(true);
					
					//checkSimCard返回null或空的时候要处理, 提示网络异常
					mStatus = eSIM_Error_8;
					mHandler.sendEmptyMessage(CLDMessageId.MSG_ID_KLDJY_CHECK_CARD);
				}
				return;
			}
			else if (msg.what == -2)
			{
				mInited = true;
				mCheckTimer++;
				mStatus = eSIM_None;
				start_checkSimCard_Running();
				return;
			}
			
			CldMessageDialog.cancelMessageDialog();
			if (msg.what == 0) {
				CldMessageDialog.showMessageDialog(
						KCloudCtx.getAppContext(),
						CHECK_TIP, 
						CldMessageType.eMessageType_None, 
						CldMessageIcon.eMessageIcon_System, 
						"", "", null);
			} else if (msg.what == 1) {
				CldLog.i(TAG, " check sim failed ");
				//10s过后，没有检测卡
				if (!CldPhoneManager.isSimReady()) {
					//没有检测到卡
					CldMessageDialog.showMessageDialog(
							KCloudCtx.getAppContext(), 
							NOCARD_TIP, 
							CldMessageType.eMessageType_Close, 
							CldMessageIcon.eMessageIcon_System, 
							"", "", null);
				} else if (!CldPhoneNet.isNetConnected()) {
					// 无网络状态时，判断本地是否存有上次的检卡结果，有且一致可使用上次的状态; 无则需重新检卡
					String share_iccid = KCloudShareUtils.getString(KCloudAppUtils.TARGET_FIELD_ICCID);
					String share_serialId = KCloudShareUtils.getString(KCloudAppUtils.TARGET_FIELD_IMEI);
					    
					CldLog.i(TAG, "iccid = " + share_iccid + ", serialId = " + share_serialId);
					if (share_iccid.equals(KCloudDevice.getSimSerialNumberEx())
							&& share_serialId.equals(KCloudDevice.getDeviceID())) {
						setSimStatus(KCloudShareUtils.getInt(KCloudAppUtils.TARGET_FIELD_CARD_STATUS));
						mHandler.removeMessages(CLDMessageId.MSG_ID_KLDJY_CHECK_CARD);
						mHandler.sendEmptyMessage(CLDMessageId.MSG_ID_KLDJY_CHECK_CARD);
					} else {
						CldMessageDialog.showMessageDialog(
								KCloudCtx.getAppContext(), 
								NETWORK_ERROR_TIP, 
								CldMessageType.eMessageType_Close,
								CldMessageIcon.eMessageIcon_System, 
								"", "", null);
					}
				}
			} else if (msg.what == CLDMessageId.MSG_ID_KLDJY_CHECK_CARD) {
				CldLog.i(TAG, "mStatus = " + String.valueOf(mStatus));
				KCloudAlarmManager.getInstance().notifyNavi();
				switch (mStatus) {
				case eSIM_Normal:  //注册成功
				case eSIM_Error_6: //已停用， 可以获取流量和套餐信息
					//获取流量和套餐信息
					KCloudFlowManager.getInstance().init();
					KCloudPackageManager.getInstance().init();
					break;
					
				case eSIM_Error_1: //ICCID卡不存在（外来卡）
					CldMessageDialog.showMessageDialog(
							KCloudCtx.getAppContext(), 
							CARD_NOT_BELONG_TIP, 
							CldMessageType.eMessageType_Close, 
							"", null);
					break;
					
				case eSIM_Error_2: //未激活的服务卡（ICCID与SN未绑定）
					String register_iccid = KCloudShareUtils.getString(KCloudAppUtils.TARGET_FIELD_REGISTER_ICCID);
					if (register_iccid.isEmpty()
							|| !register_iccid.equals(KCloudDevice.getSimSerialNumberEx())) {
						Bundle bundle = msg.getData();
						
						//如果导航已安装，则提示激活，否则不激活
						boolean exist = KCloudCommonUtil.isPackageExist(KCloudHeartbeatManager.PKGNAME_NAVI);
						Log.i(TAG, "exist = " + exist);
						if (exist)
						{
							CldSimActivateDialog.showDialog(
									KCloudCtx.getAppContext(), 
									bundle != null ? bundle.getString("pkalias") : "", 
									new CldSimActivateDialog.CldSimActivateDialogListener() {

								@Override
								public void onActivate() {
									KCloudShareUtils.put(KCloudAppUtils.TARGET_FIELD_REGISTER_ICCID, KCloudDevice.getSimSerialNumberEx());
									start_registerSimCard_Running();
								}
							});
						}
					} else  {
						start_registerSimCard_Running();
					}
					break;
					
				case eSIM_Error_3: //卡被锁定（卡被插在非法设备上）
					CldMessageDialog.showMessageDialog(
							KCloudCtx.getAppContext(), 
							DEVICES_NOT_BELONG_TIP, 
							CldMessageType.eMessageType_Close, 
							"", null);
					break;

				case eSIM_Error_0:
				case eSIM_Error_4:
				case eSIM_Error_5:	
				case eSIM_Error_7:
				case eSIM_Error_8:
					String tip = String.format(CARD_BIND_ERROR_TIP, mStatus);
					CldMessageDialog.showMessageDialog(
							KCloudCtx.getAppContext(), 
							tip, 
							CldMessageType.eMessageType_Close,
							"", null);
				default: 
					break;
				}
			}
		}
	};
	
	public int getSimStatus() {
		return this.mStatus;
	}
	
	public String getTipString(int status) {
		String result = "";
		
		switch (status) {
		case eSIM_Normal:
			result = CARD_NORMAL;
			break;
			
		case eSIM_Error_1:
			result = CARD_NOT_BELONG_TIP;
			break;
			
		case eSIM_Error_2:
			result = CARD_NOT_ACTIVATE_TIP;
			break;
			
		case eSIM_Error_3:
			result = DEVICES_NOT_BELONG_TIP;
			break;

		case eSIM_Error_0:
		case eSIM_Error_4:
		case eSIM_Error_5:
		//case eSIM_Error_6:
		case eSIM_Error_7:
		case eSIM_Error_8:
		default:
			result = String.format(CARD_BIND_ERROR_TIP, status);
			break;
		}
		
		return result;
	}
	
	boolean mInited = false;
	int mCheckTimer = 0;
	
	@SuppressLint("DefaultLocale") 
	public void init() {
		if (mInited)
			return;
		
		mInited = true;
		mCheckTimer++;
		CldLog.i(TAG, "init");
		mStatus = eSIM_None;
		start_checkSimCard_Running();
	}
	
	private void setSimStatus(int status) {
		this.mStatus = status;
	}
	
	/**
	 * @Title: setCheckResult
	 * @Description: 设置检查卡的结果
	 * @param errcode
	 * @param pkalias
	 * @return: void
	 */
	private void setCheckResult(int errcode, String pkalias) {
		//检测卡结果
		KCloudCommonUtil.sendFreshFlowBroadcast(true);
		
		setSimStatus(errcode);
			
		if (errcode == 1 || errcode == -2) {
			// 正常情况下
			KCloudShareUtils.put(KCloudAppUtils.TARGET_FIELD_ICCID, KCloudDevice.getSimSerialNumberEx());
			KCloudShareUtils.put(KCloudAppUtils.TARGET_FIELD_IMEI, KCloudDevice.getDeviceID());
			KCloudShareUtils.put(KCloudAppUtils.TARGET_FIELD_CARD_STATUS, errcode);
		}
		
		Message message = mHandler.obtainMessage();
		message.what = CLDMessageId.MSG_ID_KLDJY_CHECK_CARD;
		mHandler.removeMessages(CLDMessageId.MSG_ID_KLDJY_CHECK_CARD);
		if (errcode == -2) {
			Bundle bundle = new Bundle();
			bundle.putString("pkalias", pkalias);
			message.setData(bundle);
		}
		mHandler.sendMessage(message);
	}
	
	/**
	 * @Title: start_checkSimCard_Running
	 * @Description: 开始检查卡
	 * @return: void
	 */
	private void start_checkSimCard_Running() {
		final int[] check_count = new int[1]; 
		CldLog.i(TAG, "start_checkSimCard_Running");
		CldOlsThreadPool.submit(new Runnable() {
			@Override
			public void run() {
				
				//不弹"检测Sim卡中"的提示 2016-7-25
				//mHandler.sendEmptyMessage(0);
				
				while (!CldPhoneManager.isSimReady() || !CldPhoneNet.isNetConnected()) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} 
					
					check_count[0]++;
					if (check_count[0] == 5) {	// 检查是否有卡10s
						mHandler.sendEmptyMessage(1);
						return ;
					}
					continue; 
				}
				
				// 检查sim状态
				KCloudNetworkUtils.checkSimCard(new IKCloudSimCardListener() {

					@SuppressLint("NewApi") 
					@Override
					public void onResult(String jsonString) {
						CldLog.i(TAG, "checkSimCard = " + jsonString);
						if (jsonString != null && !jsonString.isEmpty()) {
							try {
								JSONObject jsonObject = new JSONObject(jsonString);
								
								String pkalias = "";
								int errcode = jsonObject.getInt("errcode");
								if (errcode == -2) {
									pkalias = jsonObject.getJSONObject("data").getString("pkalias");
								}
								setCheckResult(errcode, pkalias);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}else{
							//checkSimCard返回null或空的时候要处理, 重新检测卡(最多3次)
							mHandler.sendEmptyMessage(-1);								
						}
					}
				});
			}
		});
	}
	
	public void test(){
		start_registerSimCard_Running();
	}
	
	
	/**
	 * @Title: checkSimStatus
	 * @Description: 如果sim卡没有激活， 则要激活sim卡
	 * @return: void
	 */
	public void checkSimStatus() {
		int simStatus = KCloudShareUtils.getInt(KCloudAppUtils.TARGET_FIELD_CARD_STATUS);
		if (simStatus != eSIM_Normal)
		{
			mStatus = eSIM_None;
			start_checkSimCard_Running();
		}
	}
	
	/**
	 * @Title: start_registerSimCard_Running
	 * @Description: 开始注册卡
	 * @return: void
	 */
	private void start_registerSimCard_Running() {
		CldLog.i(TAG, "start_registerSimCard_Running");
		CldOlsThreadPool.submit(new Runnable() {

			@Override
			public void run() {
				while (!CldPhoneNet.isNetConnected()) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}
				
				KCloudNetworkUtils.registerSimCard(new IKCloudSimCardListener() {

					@SuppressLint("NewApi") 
					@Override
					public void onResult(String jsonString) {
						CldLog.i(TAG, "registerSimCard = " + jsonString);
						if (jsonString != null && !jsonString.isEmpty()) {
							try {
								JSONObject jsonObject = new JSONObject(jsonString);
								if (jsonObject.getInt("errcode") == 1) {
									mStatus = eSIM_Normal;
									KCloudShareUtils.put(KCloudAppUtils.TARGET_FIELD_CARD_STATUS, jsonObject.getInt("errcode"));
									CldSimActivateDialog.cancelDialog();
									
									//获取流量和套餐信息
									KCloudFlowManager.getInstance().init();
									KCloudPackageManager.getInstance().init();
									
									// 出现登录UI
									if (!CldKAccountAPI.getInstance().isLogined()) {
										if (!CldKAccountAPI.getInstance().getLoginName().isEmpty() 
												&& !CldKAccountAPI.getInstance().getLoginPwd().isEmpty()) {
											CldKAccountAPI.getInstance().startAutoLogin();
											return ;
										}
										Intent intent = new Intent();
										intent.setClassName(KCloudAppUtils.TARGET_PACKAGE_NAME, 
												KCloudAppUtils.TARGET_CLASS_NAME_USER);
										intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										KCloudCtx.getAppContext().startActivity(intent);
									}
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				});
			}
			
		});
	}
}