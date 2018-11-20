package cld.kcloud.service;

import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import cld.kcloud.center.KCloudAppUtils;
import cld.kcloud.center.KCloudAppUtils.CLDMessageId;
import cld.kcloud.custom.bean.KCloudCarInfo;
import cld.kcloud.custom.manager.KCloudAlarmManager;
import cld.kcloud.custom.manager.KCloudPackageManager;
import cld.kcloud.custom.manager.KCloudSimCardManager;
import cld.kcloud.datastore.KCloudCarStore;
import cld.kcloud.service.aidl.IKCloudClient;
import cld.kcloud.service.aidl.IKCloudService;
import cld.kcloud.user.KCloudUser;
import cld.kcloud.utils.KCloudCommonUtil;
import com.cld.device.CldPhoneManager;
import com.cld.device.CldPhoneNet;
import com.cld.log.CldLog;
import com.cld.ols.api.CldKAccountAPI;
import com.cld.ols.api.CldKAccountAPI.CldLoginStatus;
import com.cld.ols.base.CldOlsEnv;
import com.cld.ols.dal.CldDalKAccount;
import com.cld.ols.sap.bean.CldSapKAParm.CldUserInfo;

public class KCloudService extends Service {
	private static final String TAG = "KCloudService";
	static private IKCloudClient mClientListener = null;
	
	IKCloudService.Stub mBinder = new IKCloudService.Stub() {
		
		@Override
		public void start_KLD_app() throws RemoteException {
			CldLog.i(TAG, "startKCenter");
			KCloudCommonUtil.startActivity(
					KCloudAppUtils.TARGET_CLASS_NAME_USER);
		}

		@SuppressLint("NewApi") 
		@Override
		public String get_KLD_account() throws RemoteException {
			if (!CldKAccountAPI.getInstance().getLoginName().isEmpty()
					&& !CldKAccountAPI.getInstance().getLoginPwd().isEmpty()) {
				if (CldKAccountAPI.getInstance().isLogined()) {
					return CldKAccountAPI.getInstance().getBindMobile();
				}
				return CldKAccountAPI.getInstance().getLoginName();
			}

			return ""; // ��ʱ���������ʹ�ýӿ�start_KLD_app�����¼ҳ��
		}

		@Override
		public String get_KLD_login_result() throws RemoteException {
			String result = "none";
			CldLoginStatus status = CldKAccountAPI.getInstance().getLoginStatus();
			
			switch (status) {
			case LOGIN_DOING:
				result = "doing";
				break;
				
			case LOGIN_DONE:
				result = "done";
				break;
				
			case LOGIN_NONE:
			default:
				break;
			}
			
			return result;
		}

		@Override
		public void set_DDH_account(String accountJson) throws RemoteException {
			
		}

		@Override
		public void set_DDH_login_result(String resultJson)
				throws RemoteException {
			
		}
		
		/**
		 * ���sim��(�Է�Launcher����������󣬻�û��bindservice�ɹ�)
		 */
		@Override
		public void start_check_simcard() throws RemoteException {
			CldLog.i(TAG, "start_check_simcard");
			KCloudSimCardManager.getInstance().init();
		}
		
		/**
		 * ��Launcher��ʼbindService�����
		 */
		@SuppressLint("NewApi") 
		@Override
		public void start_KLD_kcenter() throws RemoteException {
			CldLog.i(TAG, "start_KLD_kcenter");
			// sim�����
			KCloudSimCardManager.getInstance().init();
			
			int netType = CldPhoneNet.getNetworkType();			
			CldLog.i(TAG, "netType = " + String.valueOf(netType));
			if (CldPhoneNet.isNetConnected()) {
				String username = CldKAccountAPI.getInstance().getLoginName();
				String password = CldKAccountAPI.getInstance().getLoginPwd();
				
				if (!username.isEmpty() && !password.isEmpty()) {
					//�û��������벻Ϊ��ʱ���Զ���¼
					CldKAccountAPI.getInstance().startAutoLogin();
					//֪ͨlauncher, �ر�Э�����
					KCloudCommonUtil.sendCloseServiceInterface();
				} else {
					KCloudCommonUtil.startActivity(
							KCloudAppUtils.TARGET_CLASS_NAME_USER);
				}
			} else {
				//�����������£�����¼K�ƣ�֪ͨLauncher
				KCloudCommonUtil.sendCloseServiceInterface();
			}
		}

		@Override
		public void setLoginListener(final IKCloudClient client)
				throws RemoteException {
			if(client != null){
				mClientListener = client;
				CldLog.i(TAG, "mClientListener = " + mClientListener.toString());
				
				CldLoginStatus status = CldKAccountAPI.getInstance().getLoginStatus();
				if(status == CldLoginStatus.LOGIN_DONE){
					onClientListener(0, 0, "ALREADY_LOGIN");
				}
			}
		}

		@Override
		public int isKOS_RunningApp(String app_name) throws RemoteException {
			if (!CldKAccountAPI.getInstance().isLogined()) {
				return -1;	// δ��½�����ѵ�½����K��
			}
			
			if (!CldPhoneManager.isSimReady()) {
				return -2;
			}
			
			if (KCloudPackageManager.getInstance().getTaskStatus() != KCloudPackageManager.TASK_GETED) {
				return -3;	// �ײ�δ��ȡ��������ϵ�����¿ͻ�
			}
			
			if (KCloudPackageManager.getInstance().isRunningApp(app_name)) {
				return -4;
			}
			
			return 0;
		}

		@Override
		public void notify_KLD_InvalidSession() throws RemoteException {
			KCloudUser.getInstance().sendMessage(CLDMessageId.MSG_ID_LOGIN_SESSION_INVAILD, 0);
		}

		@Override
		public void notify_KLD_Message(String app_name, String message,
				String btnText, int type) throws RemoteException {
			
		}

		@Override
		public void jump_to_renew() throws RemoteException {
			
			//���K��û��¼�� ��Ҫ�ȵ�¼
			//��ת�����ѽ���
			KCloudAlarmManager.getInstance().jump2Renew();
		}	
	};
	
	public IBinder onBind(Intent t) {
		CldLog.i(TAG, "service on bind");
		return mBinder;
	}
	
	/***
	 * @param type 0: ��¼; 1: �ǳ�; 2: �����û���Ϣ
	 * @param errorCode 0: �ɹ�; ����: ʧ��
	 * @param errorMsg ������Ϣ
	 */
	static public void onClientListener(int type, int errorCode, String errorMsg){
		try {
			if (mClientListener != null){
				switch (type) {
				case 0: { //��¼
					JSONObject json = new JSONObject();
					try {
						json.put("errorCode", errorCode);
						json.put("errorMsg", errorMsg);
						
						json.put("LoginName", CldDalKAccount.getInstance().getLoginName());
						json.put("LoginPwd", CldDalKAccount.getInstance().getLoginPwd());
						json.put("Pwdtype", CldDalKAccount.getInstance().getPwdtype());
						json.put("Kuid", CldDalKAccount.getInstance().getKuid());
						json.put("Session", CldDalKAccount.getInstance().getSession());
						json.put("bussinessid", CldOlsEnv.getInstance().getBussinessid());
						
						CldUserInfo userInfo = CldDalKAccount.getInstance().getCldUserInfo();
						json.put("userinfo_LoginName", userInfo.getLoginName());
						json.put("userinfo_UserName", userInfo.getUserName());
						json.put("userinfo_UserAlias", userInfo.getUserAlias());
						json.put("userinfo_Email", userInfo.getEmail());
						json.put("userinfo_EmailBind", userInfo.getEmailBind());
						json.put("userinfo_Mobile", userInfo.getMobile());
						json.put("userinfo_MobileBind", userInfo.getMobileBind());
						json.put("userinfo_Sex", userInfo.getSex());
						json.put("userinfo_Vip", userInfo.getVip());
						json.put("userinfo_UserLevel", userInfo.getUserLevel());
						json.put("userinfo_RegTime", userInfo.getRegTime());
						json.put("userinfo_LastLoginTime", userInfo.getLastLoginTime());
						json.put("userinfo_Status", userInfo.getStatus());
						json.put("userinfo_PhotoPath", userInfo.getPhotoPath());
					} catch (JSONException e) {
						e.printStackTrace();
					}
					CldLog.i(TAG, "json.toString() = " + json.toString());
					mClientListener.onLoginListener(json.toString());
					break;
				}

				case 1: //�˳�
					mClientListener.onLogoutListener("");
					break;
				
				case 2: { // �����û���Ϣ
					JSONObject json = new JSONObject();
					CldUserInfo userInfo = CldDalKAccount.getInstance().getCldUserInfo();
					KCloudCarInfo carInfo = KCloudCarStore.getInstance().get();
					
					try {
						json.put("userinfo_Sex", userInfo.getSex());
						json.put("userinfo_Mobile", userInfo.getMobile());
						json.put("userinfo_UserName", userInfo.getUserName());
						json.put("userinfo_UserAlias", userInfo.getUserAlias());
						json.put("carinfo_brand", carInfo.brand);
						json.put("carinfo_model", carInfo.car_model);
						json.put("carinfo_series", carInfo.series);
						json.put("carinfo_plate_num", carInfo.plate_num);
						json.put("carinfo_frame_num", carInfo.frame_num);
						json.put("carinfo_engine_num", carInfo.engine_num);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					mClientListener.onUpdateInfoListener(json.toString());
					break;
				}
			}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
