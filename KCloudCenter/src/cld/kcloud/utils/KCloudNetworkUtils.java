package cld.kcloud.utils;

import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import cld.kcloud.center.KCloudAppConfig;
import cld.kcloud.center.KCloudAppUtils;
import cld.kcloud.center.KCloudAppUtils.CLDMessageId;
import cld.kcloud.center.KCloudCtx;
import cld.kcloud.center.KCloudDevice;
import cld.kcloud.custom.bean.KCloudCarInfo;
import cld.kcloud.custom.bean.KCloudInstalledInfo;
import cld.kcloud.custom.manager.KCloudAlarmManager.IKCloudAlarmListener;
import cld.kcloud.custom.manager.KCloudFlowManager.IKCloudFlowListener;
import cld.kcloud.custom.manager.KCloudHeartbeatManager.IKCloudHeartbeatListener;
import cld.kcloud.custom.manager.KCloudPackageManager.IKCloudPackageListener;
import cld.kcloud.custom.manager.KCloudSimCardManager.IKCloudSimCardListener;
import cld.kcloud.custom.manager.KCloudSplashManager.IKCloudSplashListener;
import cld.kcloud.custom.manager.KCloudUpgradeManager.IKCloudUpgradeListener;
import cld.kcloud.datastore.KCloudCarStore;
import cld.kcloud.fragment.ServiceRenewalFragment.IKCloudRenewalListener;
import cld.kcloud.user.KCloudUser;
import cld.kcloud.utils.control.CldQRCode;
import cld.kcloud.utils.sap.KCloudNetworkSap;
import com.cld.device.CldPhoneManager;
import com.cld.device.CldPhoneNet;
import com.cld.log.CldLog;
import com.cld.net.CldHttpClient;
import com.cld.ols.api.CldKAccountAPI;
import com.cld.ols.sap.CldSapKAccount;
import com.cld.ols.sap.CldSapUtil;
import com.cld.ols.tools.CldOlsThreadPool;
import com.cld.ols.tools.CldSapReturn;

public class KCloudNetworkUtils {

	private final static String TAG = "KCloudNetworkUtils";
	private static String platform_key_code = "";
	
	public static String getAccountKeyCode() {
		return CldSapKAccount.keyCode;
	}
	
	public static String getPlatformKeyCode() {
		return platform_key_code;
	}
	
	@SuppressLint("NewApi") 
	private static void onErrorResult(String jsonString) {
		if (jsonString != null && !jsonString.isEmpty()) {
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(jsonString);
				
				switch (jsonObject.getInt("errcode")) {
				case 501:
					// 用户被挤下线 
					KCloudUser.getInstance().sendMessage(CLDMessageId.MSG_ID_LOGIN_SESSION_INVAILD, 0);
					break;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 初始化运营平台的key_code
	 */
	@SuppressLint("NewApi") 
	public static void initPlatformKeyCode() {
		String result = null;
		CldSapReturn request = null;
		
		request = KCloudNetworkSap.initPlatformKeyCode(
				KCloudAppConfig.cid,
				KCloudAppConfig.appver);
		CldLog.i(TAG, "initPlatformKeyCode = " + request.url);
		
		result = CldHttpClient.get(request.url);
		if (result != null && !result.isEmpty()) {
			onErrorResult(result);
			try {
				JSONObject object = new JSONObject(result);
				if (object.getInt("errcode") == 0) {
					platform_key_code = CldSapUtil.decodeKey(object.getString("code"));
					KCloudNetworkSap.setKgoKey(platform_key_code);
					CldLog.i(TAG, "platform_key_code = " + platform_key_code);
				} 
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 获取闪屏信息
	 * @param listener
	 */
	public static void getSplashInfomation(IKCloudSplashListener listener) {
		if (!CldPhoneNet.isNetConnected()) {
			return ;
		}
		
		String result = null;
		CldSapReturn request = null;
		
		String version = KCloudUIUtils.getApkVersion(KCloudCtx.getAppContext(), "com.cld.launcher");
		request = KCloudNetworkSap.getSplashInfomation(
				KCloudAppConfig.apptype,
				version, 
				KCloudAppConfig.device_width, 
				KCloudAppConfig.device_height);
		CldLog.i(TAG, "getSplashInfomation = " + request.url);
		
		result = CldHttpClient.get(request.url);
		if (result != null) {
			onErrorResult(result);
			if (listener != null) {
				listener.onResult(result);
			}
		}
	}
	
	/**
	 * 检查卡合法性
	 * @param listener
	 */
	public static void checkSimCard(IKCloudSimCardListener listener) {
		String result;
		String iccid = KCloudDevice.getSimSerialNumberEx();
		String imsi = CldPhoneManager.getImsi();
		String sim = CldPhoneManager.getPhoneNumber();
				
		if ("unknown".equals(iccid)) {
			iccid = "";
		}

		if ("unknown".equals(imsi)) {
			imsi = "";
		}

		if ("unknown".equals(sim)) {
			sim = "";
		}

		CldSapReturn request = KCloudNetworkSap.checkSimCard(iccid, imsi, sim, 
				KCloudDevice.getDeviceID(), 
				KCloudAppConfig.appver.substring(0,5), 
				CldKAccountAPI.getInstance().getDuid(), 
				CldKAccountAPI.getInstance().getKuid());
		CldLog.i(TAG, "checkSimCard = " + request.url);
		
		result = CldHttpClient.post(request.url, request.jsonPost);
		if (result != null) {
			onErrorResult(result);
			if (listener != null) {
				listener.onResult(result);
			}
		}
	}
	
	public static void registerSimCard(IKCloudSimCardListener listener) {
		String result;
		String iccid = KCloudDevice.getSimSerialNumberEx(); 
		String imsi = CldPhoneManager.getImsi();
		String sim = CldPhoneManager.getPhoneNumber();
				
		if ("unknown".equals(iccid)) {
			iccid = "";
		}

		if ("unknown".equals(imsi)) {
			imsi = "";
		}

		if ("unknown".equals(sim)) {
			sim = "";
		}

		CldSapReturn request = KCloudNetworkSap.registerSimCard(iccid, imsi, sim, 
				KCloudDevice.getDeviceID(), 
				KCloudAppConfig.appver.substring(0,5), 
				CldKAccountAPI.getInstance().getDuid(), 
				CldKAccountAPI.getInstance().getKuid(),
				KCloudAppConfig.device_code, 
				KCloudAppConfig.product_code, 
				KCloudAppConfig.custom_id);
		CldLog.i(TAG, "registerSimCard = " + request.url);
		
		result = CldHttpClient.post(request.url, request.jsonPost);
		if (result != null) {
			onErrorResult(result);
			if (listener != null) {
				listener.onResult(result);
			}
		}
	}
	
	/**
	 * 获取用户已订购套餐列表
	 * @param listener
	 */
	@SuppressLint("NewApi") 
	public static void getKGoUserPackageList(IKCloudPackageListener listener) {
		String result;
		CldSapReturn request = null;

		if (getPlatformKeyCode().isEmpty()) {
			initPlatformKeyCode();
		}
		
		if (getPlatformKeyCode().isEmpty()) {
			if (listener != null) {
				try {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("errcode", 1);
					listener.onResult(jsonObject.toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return ;
			}
		}
		
		String version = KCloudUIUtils.getApkVersion(KCloudCtx.getAppContext(), "com.cld.launcher");
		String iccid = KCloudDevice.getSimSerialNumberEx();
		if ("unknown".equals(iccid)) {
			iccid = CldPhoneManager.getImsi();
		}
		
		if ("unknown".equals(iccid)) {
			iccid = CldPhoneManager.getPhoneNumber();
		}

		request = KCloudNetworkSap.getUserPackageList(
				KCloudAppConfig.system_code, 
				KCloudAppConfig.device_code, 
				KCloudAppConfig.product_code, 
				version,
				CldKAccountAPI.getInstance().getKuid(),
				CldKAccountAPI.getInstance().getSession(),
				KCloudAppConfig.appid, 
				KCloudAppConfig.bussinessid, 
				KCloudAppConfig.cid, 
				KCloudAppConfig.appver, 
				KCloudAppConfig.device_width, 
				KCloudAppConfig.device_height, 
				iccid);
		CldLog.i(TAG, "getKGoUserPackageList = " + request.url);
		
		result = CldHttpClient.get(request.url);
		if (result != null) {
			onErrorResult(result);
			if (listener != null) {
				listener.onResult(result);
			}
		}
	}
	
	/**
	 * 获取应用列表
	 * @param listener
	 */
	@SuppressLint("NewApi") 
	public static void getKGoServicesAppList(IKCloudPackageListener listener) {
		String result;
		CldSapReturn request = null;
		
		if (getPlatformKeyCode().isEmpty()) {
			initPlatformKeyCode();
		}
		
		if (getPlatformKeyCode().isEmpty()) {
			if (listener != null) {
				try {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("errcode", 1);
					listener.onResult(jsonObject.toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return ;
			}
		}
		
		request = KCloudNetworkSap.getUserServicesAppList(
				KCloudAppConfig.appid,
				KCloudAppConfig.bussinessid, 
				KCloudAppConfig.cid, 
				CldKAccountAPI.getInstance().getKuid(), 
				CldKAccountAPI.getInstance().getSession(), 
				KCloudDevice.getSimSerialNumberEx(), 
				KCloudAppConfig.appver);
		CldLog.i(TAG, "getKGoServicesAppList = " + request.url);
		
		result = CldHttpClient.get(request.url);	
		if (result != null) {
			onErrorResult(result);
			if (listener != null) {
				listener.onResult(result);
			}
		}
	}
	
	/**
	 * 获取套餐提醒设置
	 * @param comboId
	 * @param listener
	 */
	@SuppressLint("NewApi") 
	public static void getKGoAlarmSetting(int comboId, IKCloudAlarmListener listener) {
		String result;
		CldSapReturn request = null;
			
		if (getPlatformKeyCode().isEmpty()) {
			initPlatformKeyCode();
		}
		
		if (getPlatformKeyCode().isEmpty()) {
			if (listener != null) {
				try {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("errcode", 1);
					listener.onResult(jsonObject.toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return ;
			}
		}
		
		request = KCloudNetworkSap.getAlarmSetting(comboId, 
				KCloudAppConfig.cid, 
				KCloudAppConfig.appver);
		CldLog.i(TAG, "getKGoAlarmSetting = " + request.url);
		
		result = CldHttpClient.get(request.url);
		if (result != null) {
			onErrorResult(result);
			if (listener != null) {
				listener.onResult(result);
			}
		}
	}
	
	/**
	 * 获取流量卡状态
	 * @param listener
	 */
	@SuppressLint("NewApi") 
	public static void getSimCarStatus(IKCloudFlowListener listener) {
		String result;
		CldSapReturn request = null;

		String iccid = KCloudDevice.getSimSerialNumberEx();
		String imsi = CldPhoneManager.getImsi();
		String sim = CldPhoneManager.getPhoneNumber();

		if ("unknown".equals(iccid)) {
			iccid = "";
		}

		if ("unknown".equals(imsi)) {
			imsi = "";
		}

		if ("unknown".equals(sim)) {
			sim = "";
		}

		request = KCloudNetworkSap.getSimCardStatus(iccid, imsi, sim, 
				KCloudDevice.getDeviceID(), 
				KCloudAppConfig.appver.substring(0,5));
		CldLog.i(TAG, "getSimCarStatus = " + request.url);
		
		result = CldHttpClient.get(request.url);
		if (result != null) {
			onErrorResult(result);
			if (listener != null) {
				listener.onResult(result);
			}
		}
	}
	
	/**
	 * 获取续费链接
	 * @param comboId
	 * @return
	 */
	@SuppressLint("DefaultLocale") 
	public static String getRenewalQRCode(int comboId, long curTimeId) {
		String Head = "";
		String QRText = "";
		if (KCloudAppUtils.isTestVersion()) {
			Head = "http://test.careland.com.cn/genuine/iccid.php?s=";
		} else {
			Head = "http://genuine.careland.com.cn/iccid.php?s=";
		}
		
		if (!CldKAccountAPI.getInstance().isLogined()) {
			return "";
		}
		
		String iccid = KCloudDevice.getSimSerialNumberEx(); 
		String imsi = CldPhoneManager.getImsi();
		String sim = CldPhoneManager.getPhoneNumber();
		if (imsi == null || "unknown".equals(imsi)) {
			imsi = "";
		}
		if (sim == null || "unknown".equals(sim)) {
			sim = "";
		}		
		
		QRText = String.format("%d;%d;%s;%s;%s;%d;%d;%s;%d;%s;%d;%d;%s;%d;%d", 
				KCloudAppConfig.device_code, 
				KCloudAppConfig.product_code, 
				iccid, imsi, sim, 
				KCloudAppConfig.custom_id,
				CldKAccountAPI.getInstance().getDuid(), 
				KCloudDevice.getDeviceID(), 
				comboId, 
				CldKAccountAPI.getInstance().getSession(), 
				KCloudAppConfig.appid, 
				KCloudAppConfig.bussinessid, 
				KCloudAppConfig.appver.substring(0,5), 
				CldKAccountAPI.getInstance().getKuid(),
				curTimeId).toUpperCase();
		CldLog.i(TAG, "QRText before = " + QRText);
		
		long Key = 0;
		long[] KTemp = new long[3];
		Key = (long)(Math.random() * 900000) + 100000;
		KTemp[0] = (Key / 10000) % 24;
		KTemp[1] = ((Key % 10000) / 100) % 24;
		KTemp[2] = (Key % 100) % 24;
		
		long iIndex = 0;
		int lTextLenght = QRText.length();
		byte[] tempCode = new byte[lTextLenght];
		tempCode = QRText.getBytes();
		
		for(int i = 0;i < lTextLenght;i++)
		{
			iIndex = CldQRCode.GetIndexEncrypt((char)tempCode[i]);
			tempCode[i] = (byte)CldQRCode.GetChEncrpyt((int)(iIndex + KTemp[i % 3]));
		}

		QRText = new String(tempCode);
		QRText = Head + Key + QRText;
		CldLog.i(TAG, "QRText after = " + QRText);
		return QRText;
	}
	
	/**
	 * 检查续费状态
	 */
	@SuppressLint("NewApi") 
	public static void getRenewalStatus(long curTimeId, IKCloudRenewalListener listener) {
		String result;
		String iccid = KCloudDevice.getSimSerialNumberEx();
		String imsi = CldPhoneManager.getImsi();
		String sim = CldPhoneManager.getPhoneNumber();
					
		if ("unknown".equals(iccid)) {
			iccid = "";
		}
	
		if ("unknown".equals(imsi)) {
			imsi = "";
		}
	
		if ("unknown".equals(sim)) {
			sim = "";
		}
	
		CldSapReturn request = KCloudNetworkSap.checkPayStatus(iccid, imsi, sim, 
				KCloudDevice.getDeviceID(), 
				KCloudAppConfig.appver.substring(0,5), 
				CldKAccountAPI.getInstance().getDuid(), 
				CldKAccountAPI.getInstance().getKuid(), curTimeId);
		CldLog.i(TAG, "getRenewalStatusByQRCode = " + request.url);
		
		result = CldHttpClient.get(request.url);
		if (result != null) {
			onErrorResult(result);
			if (listener != null) {
				listener.onResult(result);
			}
		}
	}
	
	/**
	 * 定时检查session是否失效
	 */
	@SuppressLint("NewApi") 
	public static void checkSessionInvaild() {
		CldLog.d(TAG, "checkSessionInvaild");
		CldOlsThreadPool.submit(new Runnable() {
			public void run() {
				String result;
				CldSapReturn request = KCloudNetworkSap.checkSessionInvaild(
						KCloudAppConfig.appid,
						KCloudAppConfig.cid, 
						KCloudAppConfig.appver, 
						KCloudAppConfig.bussinessid,
						CldKAccountAPI.getInstance().getKuid(), 
						CldKAccountAPI.getInstance().getSession());
				result = CldHttpClient.get(request.url);
				CldLog.i(TAG, "checkSessionInvaild = " + request.url);
				
				if (result != null && !result.isEmpty()) {
					onErrorResult(result);
				}
			}
		});
	}
	
	@SuppressLint("NewApi") 
	public static void getUserCarInfo(final Handler handler) {
		CldLog.d(TAG, "getUserCarInfo");
		CldOlsThreadPool.submit(new Runnable() {
			public void run() {
				String result;
				CldSapReturn request = KCloudNetworkSap.getUserCarInfo(
						KCloudAppConfig.appid, 
						KCloudAppConfig.cid,
						KCloudAppConfig.appver,
						CldKAccountAPI.getInstance().getDuid());
				result = CldHttpClient.get(request.url);
				CldLog.i(TAG, "getUserCarInfo result: " + result);
				
				if (result != null && !result.isEmpty()) {
					try {
						JSONObject object = new JSONObject(result);
						if (object.getInt("errcode") == 0) {
							Bundle bundle = new Bundle();
							Message message = handler.obtainMessage();
							bundle.putString("result", result);
							message.what = CLDMessageId.MSG_ID_CAR_GET_SUCCESS;
							message.setData(bundle);
							handler.sendMessage(message);
						} else {
							Message message = handler.obtainMessage();
							message.what = CLDMessageId.MSG_ID_CAR_GET_FAILED;
							handler.sendMessage(message);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					Message message = handler.obtainMessage();
					message.what = CLDMessageId.MSG_ID_CAR_GET_FAILED;
					handler.sendMessage(message);
				}
			}
		});
	}

	/**
	 * 更新车辆信息
	 * 
	 * @param handler
	 */
	@SuppressLint("NewApi") 
	public static void updateCarInfo(final Handler handler) {
		CldOlsThreadPool.submit(new Runnable() {
			public void run() {
				String result;
				CldSapReturn request = null;
				KCloudCarInfo carInfoTmp = KCloudCarStore.getInstance().getTemp();
				
				if (!KCloudCarStore.getInstance().hasCar()) {
					request = KCloudNetworkSap.bindCarInfo(
							KCloudAppConfig.appid,
							KCloudAppConfig.bussinessid, 
							CldKAccountAPI.getInstance().getKuid(), 
							CldKAccountAPI.getInstance().getDuid(), 
							CldKAccountAPI.getInstance().getSession(),
							carInfoTmp.brand, 
							carInfoTmp.car_model,
							carInfoTmp.series, 
							carInfoTmp.plate_num,
							carInfoTmp.frame_num, 
							carInfoTmp.engine_num);
					result = CldHttpClient.post(request.url, request.jsonPost);
					CldLog.i(TAG, "bindCarInfo result: " + result);
					
					if (result != null && !result.isEmpty()) {
						onErrorResult(result);
						try {
							JSONObject object = new JSONObject(result);
							int paramInt = object.getInt("errcode");
							if (paramInt != 0) {
								Message message = handler.obtainMessage();
								message.what = CLDMessageId.MSG_ID_CAR_UPDATE_FAILED;
								handler.sendMessage(message);
								return;
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}else {
						Message message = handler.obtainMessage();
						message.what = CLDMessageId.MSG_ID_CAR_UPDATE_FAILED;
						handler.sendMessage(message);
					}
				}

//				request = KCloudNetUtils.unbindCarInfo(KCloudAppConfig.appid,
//						CldKAccountAPI.getInstance().getDuid());
//				result = CldHttpClient.post(request.url, request.jsonPost);
//				CldLog.d(TAG, result);
				
				request = KCloudNetworkSap.updateCarInfo(
						KCloudAppConfig.appid,
						CldKAccountAPI.getInstance().getDuid(),
						carInfoTmp.brand, 
						carInfoTmp.car_model,
						carInfoTmp.series, 
						carInfoTmp.plate_num,
						carInfoTmp.frame_num, 
						carInfoTmp.engine_num);
				result = CldHttpClient.post(request.url, request.jsonPost);
				CldLog.d(TAG, "updateCarInfo result: " + result);
				
				if (result != null && !result.isEmpty()) {
					onErrorResult(result);
					try {
						JSONObject object = new JSONObject(result);
						int paramInt = object.getInt("errcode");
						if (paramInt == 0) {
							Message message = handler.obtainMessage();
							message.what = CLDMessageId.MSG_ID_CAR_UPDATE_SUCCESS;
							handler.sendMessage(message);
						} else {
							Message message = handler.obtainMessage();
							message.what = CLDMessageId.MSG_ID_CAR_UPDATE_FAILED;
							handler.sendMessage(message);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					Message message = handler.obtainMessage();
					message.what = CLDMessageId.MSG_ID_CAR_UPDATE_FAILED;
					handler.sendMessage(message);
				}
			}
		});
	}

	/**
	 * 获取用户车辆信息
	 * @param handler
	 */
	@SuppressLint("NewApi") 
	public static void getUserCarList(final Handler handler) {
		CldOlsThreadPool.submit(new Runnable() {
			public void run() {
				String prover = KCloudAppConfig.appver;
				String result;
				CldSapReturn request = null;

				if (getPlatformKeyCode().isEmpty()) {
					initPlatformKeyCode();
				}
				
				if (getPlatformKeyCode().isEmpty()) {
					Message message = handler.obtainMessage();
					message.what = CLDMessageId.MSG_ID_KGO_GETCODE_FAILED;
					handler.sendMessage(message);
					return ;
				}
				
				request = KCloudNetworkSap.getCarList(KCloudAppConfig.cid, prover);
				result = CldHttpClient.get(request.url);
				//CldLog.d(TAG, " getUserCarList result: " + result);
				
				if (result != null && !result.isEmpty()) {
					try {
						onErrorResult(result);
						JSONObject object = new JSONObject(result);
						if (object.getInt("errcode") == 0) {
							Bundle bundle = new Bundle();
							Message message = handler.obtainMessage();
							bundle.putString("result", result);
							message.setData(bundle);
							message.what = CLDMessageId.MSG_ID_KGO_GET_CARLIST_SUCCESS;
							handler.sendMessage(message);
						} else {
							Message message = handler.obtainMessage();
							message.what = CLDMessageId.MSG_ID_KGO_GET_CARLIST_FAILED;
							handler.sendMessage(message);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else{
					Message message = handler.obtainMessage();
					message.what = CLDMessageId.MSG_ID_KGO_GET_CARLIST_FAILED;
					handler.sendMessage(message);
				}
			}
		});
	}
	
	/**
	 * @Title: getKGoUpgradeList
	 * @Description: 获取升级应用列表
	 * @param listener
	 * @return: void
	 */
	@SuppressLint("NewApi") 
	public static void getKGoUpgradeList(String launcher_ver, int regionId, 
			ArrayList<KCloudInstalledInfo> installedInfoList, 
			IKCloudUpgradeListener listener) {
		String result;
		CldSapReturn request = null;
			
		if (getPlatformKeyCode().isEmpty()) {
			initPlatformKeyCode();
		}
		
		if (getPlatformKeyCode().isEmpty()) {
			if (listener != null) {
				try {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("errcode", 1);
					listener.onResult(jsonObject.toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return ;
			}
		}
		
		
		request = KCloudNetworkSap.getUpgradeList(launcher_ver, regionId, installedInfoList);
		CldLog.i(TAG, "getKGoUpgradeList = : " + request.url + " post:" + request.jsonPost);
		result = CldHttpClient.post(request.url, request.jsonPost);
		
		if (result != null) {
			onErrorResult(result);
			if (listener != null) {
				listener.onResult(result);
			}
		}
	}
	
	/**
	 * 
	 * @Title: getHeartbeatStatus
	 * @Description: 获取心跳状态
	 * @param curTimeId
	 * @param listener
	 * @return: void
	 */
	public static void getHeartbeatStatus(long update, IKCloudHeartbeatListener listener) {
		String result;
		String iccid = KCloudDevice.getSimSerialNumberEx();
		String sim = CldPhoneManager.getPhoneNumber();
					
		if ("unknown".equals(iccid)) {
			iccid = "";
		}
	
		if ("unknown".equals(sim)) {
			sim = "";
		}
	
		CldSapReturn request = KCloudNetworkSap.checkHeartbeat(iccid, sim, 
				KCloudDevice.getDeviceID(), 
				KCloudAppConfig.appver.substring(0,5),
				update);
		CldLog.i(TAG, "getHeartbeatStatus = " + request.url);
		
		result = CldHttpClient.get(request.url);
		if (result != null) {
			onErrorResult(result);
			if (listener != null) {
				listener.onResult(result);
			}
		}
	}
}
