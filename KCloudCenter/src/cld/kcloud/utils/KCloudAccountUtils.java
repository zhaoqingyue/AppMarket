package cld.kcloud.utils;


import cld.kcloud.center.KCloudAppUtils.CLDMessageId;
import cld.kcloud.service.KCloudService;
import cld.kcloud.user.KCloudUser;

import com.cld.log.CldLog;
import com.cld.ols.api.CldKAccountAPI;
import com.cld.ols.api.CldKAccountAPI.CldBussinessCode;
import com.cld.ols.api.CldKAccountAPI.CldLoginType;
import com.cld.ols.api.CldKAccountAPI.ICldKAccountListener;

public class KCloudAccountUtils {
	
	private static final String TAG = "KCloudAccountUtils";
	private static KCloudAccountUtils mKCloudAccountUtils = null;

	/** 
	 * 账号相关的所有回调都在这里了
	 */
	public static ICldKAccountListener mListener = new ICldKAccountListener() {

		@Override
		public void onUpdateUserInfo(int paramInt) {
			if (paramInt == 0) {
				KCloudUser.getInstance().sendMessage(
						CLDMessageId.MSG_ID_USERINFO_UPDATE_SUCCESS, 
						paramInt);
				
				KCloudService.onClientListener(2, 0, 
						"MSG_ID_USERINFO_UPDATE_SUCCESS");
			} else {
				KCloudUser.getInstance().sendMessage(
						CLDMessageId.MSG_ID_USERINFO_UPDATE_FAILED, 
						paramInt);
			}
		}

		@Override
		public void onGetMobileVeriCode(int paramInt,
				CldBussinessCode bussinessCode) {
			CldLog.i(TAG, "onGetMobileVeriCode = " + String.valueOf(bussinessCode));
			switch (bussinessCode) {
			case FAST_LOGIN:
				if (paramInt == 0) {
					KCloudUser.getInstance().sendMessage(
							CLDMessageId.MSG_ID_LOGIN_GET_VERICODE_SUCCESS,
							paramInt);
				} else {
					KCloudUser.getInstance().sendMessage(
							CLDMessageId.MSG_ID_LOGIN_GET_VERICODE_FAILED,
							paramInt);
				}
				break;
				
			case RESET_PWD:
				if (paramInt == 0) {
					KCloudUser.getInstance().sendMessage(
							CLDMessageId.MSG_ID_PASSWORD_GET_VERICODE_SUCCESS,
							paramInt);
				} else {
					KCloudUser.getInstance().sendMessage(
							CLDMessageId.MSG_ID_PASSWORD_GET_VERICODE_FAILED,
							paramInt);
				}
				break;
				
			case MODIFY_PWD:
				if (paramInt == 0) {
					KCloudUser.getInstance().sendMessage(
							CLDMessageId.MSG_ID_USERINFO_PWD_VERICODE_SUCCESS,
							paramInt);
				} else {
					KCloudUser.getInstance().sendMessage(
							CLDMessageId.MSG_ID_USERINFO_PWD_VERICODE_FAILED,
							paramInt);
				}
				
			case MODIFY_MOBILE:
				if (paramInt == 0) {
					KCloudUser.getInstance().sendMessage(
							CLDMessageId.MSG_ID_USERINFO_REVISE_MOBILE_VERICODE_SUCCESS,
							paramInt);
				} else {
					KCloudUser.getInstance().sendMessage(
							CLDMessageId.MSG_ID_USERINFO_REVISE_MOBILE_VERICODE_FAILED,
							paramInt);
				}
				break;
				
			case BIND_MOBILE:
				if (paramInt == 0) {
					KCloudUser.getInstance().sendMessage(
							CLDMessageId.MSG_ID_USERINFO_BIND_MOBILE_VERICODE_SUCCESS,
							paramInt);
				} else {
					KCloudUser.getInstance().sendMessage(
							CLDMessageId.MSG_ID_USERINFO_BIND_MOBILE_VERICODE_FAILED,
							paramInt);
				}
				break;

			default:
				break;
			}
		}

		@Override
		public void onCheckMobileVeriCode(int paramInt,
				CldBussinessCode bussinessCode) {
			CldLog.i(TAG, "onCheckMobileVeriCode = " + String.valueOf(bussinessCode));
			switch (bussinessCode) {
			case RESET_PWD:
				if (paramInt == 0) {
					KCloudUser.getInstance().sendMessage(
							CLDMessageId.MSG_ID_PASSWORD_CHECK_VERICODE_SUCCESS,
							paramInt);
				} else {
					KCloudUser.getInstance().sendMessage(
							CLDMessageId.MSG_ID_PASSWORD_CHECK_VERICODE_FAILED,
							paramInt);
				}
				break;
				
			case MODIFY_PWD:
				if (paramInt == 0) {
					KCloudUser.getInstance().sendMessage(
							CLDMessageId.MSG_ID_USERINFO_PWD_CHECK_SUCCESS,
							paramInt);
				} else {
					KCloudUser.getInstance().sendMessage(
							CLDMessageId.MSG_ID_USERINFO_PWD_CHECK_FAILED,
							paramInt);
				}
				
			default:
				break;
			}
		}

		@Override
		public void onCheckEmailVeriCode(int i,
				CldBussinessCode cldbussinesscode) {

		}

		@Override
		public void onGetEmailVeriCode(int i, CldBussinessCode cldbussinesscode) {

		}

		@Override
		public void onRegister(int i, long l, String s) {

		}

		@Override
		public void onRegBySms(int i, long l, String s) {

		}

		@Override
		public void onRetrievePwdByMobile(int paramInt) {
			if (paramInt == 0) {
				KCloudUser.getInstance().sendMessage(
						CLDMessageId.MSG_ID_PASSWORD_SET_PWD_SUCCESS, paramInt);
			} else {
				KCloudUser.getInstance().sendMessage(
						CLDMessageId.MSG_ID_PASSWORD_SET_PWD_FAILED, paramInt);
			}
		}

		@Override
		public void onRetrievePwdByEmail(int paramInt) {

		}

		@Override
		public void onRevisePwd(int paramInt) {

		}

		@Override
		public void onLoginResult(int paramInt, CldLoginType type) {
			CldLog.i(TAG, "onLoginResult paramInt = " + String.valueOf(paramInt));
			if (paramInt == 0) {
				switch (type) {
				case LOGIN:
					KCloudUser.getInstance().sendMessage(
							CLDMessageId.MSG_ID_LOGIN_ACCOUNT_LOGIN_SUCCESS,
							paramInt);
					
					KCloudService.onClientListener(0, 0, 
							"MSG_ID_LOGIN_ACCOUNT_LOGIN_SUCCESS");
					break;
				case FASTLOGIN:
					KCloudUser.getInstance().sendMessage(
							CLDMessageId.MSG_ID_LOGIN_MOBILE_LOGIN_SUCCESS,
							paramInt);
					KCloudService.onClientListener(0, 0, 
							"MSG_ID_LOGIN_MOBILE_LOGIN_SUCCESS");
					break;
				case THIRDLOGIN:
					KCloudUser.getInstance().sendMessage(
							CLDMessageId.MSG_ID_LOGIN_THIRD_LOGIN_SUCCESS,
							paramInt);
					KCloudService.onClientListener(0, 0, 
							"MSG_ID_LOGIN_THIRD_LOGIN_SUCCESS");
					break;
				}
			} else {
				switch (type) {
				case LOGIN:
					KCloudUser.getInstance().sendMessage(
							CLDMessageId.MSG_ID_LOGIN_ACCOUNT_LOGIN_FAILED,
							paramInt);
					KCloudService.onClientListener(0, 1, 
							"MSG_ID_LOGIN_ACCOUNT_LOGIN_FAILED");
					break;
				case FASTLOGIN:
					KCloudUser.getInstance().sendMessage(
							CLDMessageId.MSG_ID_LOGIN_MOBILE_LOGIN_FAILED,
							paramInt);
					KCloudService.onClientListener(0, 1, 
							"MSG_ID_LOGIN_MOBILE_LOGIN_FAILED");
					break;
				case THIRDLOGIN:
					KCloudUser.getInstance().sendMessage(
							CLDMessageId.MSG_ID_LOGIN_THIRD_LOGIN_FAILED,
							paramInt);
					KCloudService.onClientListener(0, 1, 
							"MSG_ID_LOGIN_THIRD_LOGIN_FAILED");
					break;
				}
			}
		}

		@Override
		public void onGetUserInfoResult(int paramInt) {
			if (paramInt == 0) {
				KCloudUser.getInstance().sendMessage(
						CLDMessageId.MSG_ID_USERINFO_GETDETAIL_SUCCESS,
						paramInt);
			} else {
				KCloudUser.getInstance().sendMessage(
						CLDMessageId.MSG_ID_USERINFO_GETDETAIL_FAILED,
						paramInt);
			}
		}

		@Override
		public void onLoginOutResult(int paramInt) {
			if (paramInt == 0) {
				KCloudUser.getInstance().sendMessage(
						CLDMessageId.MSG_ID_USERINFO_LOGOUT_SUCCESS, 
						paramInt);
			} else {
				KCloudUser.getInstance().sendMessage(
						CLDMessageId.MSG_ID_USERINFO_LOGOUT_FAILED, 
						paramInt);
			}
		}

		@Override
		public void onAutoLoginResult(int loginState, int errCode) {
			CldLog.i(TAG, "onAutoLoginResult   " + String.valueOf(loginState) + " --- "
					+ String.valueOf(errCode));
			switch (loginState) {
			case 0: // 账号或密码为空
				break;

			case 1: // 登录中
				break;

			case 2: // 登录成功
				KCloudUser.getInstance().sendMessage(
						CLDMessageId.MSG_ID_LOGIN_AUTO_LOGIN_SUCCESS, errCode);
				KCloudService.onClientListener(0, 0, 
						"MSG_ID_LOGIN_AUTO_LOGIN_SUCCESS");
				break;

			case 3: // 密码被其他终端修改 登录失败
				KCloudUser.getInstance().sendMessage(
						CLDMessageId.MSG_ID_LOGIN_AUTO_LOGIN_FAILED, errCode);
				KCloudService.onClientListener(0, 1, 
						"MSG_ID_LOGIN_AUTO_LOGIN_FAILED");
				break;

			default:
				break;
			}
		}

		@Override
		public void onInValidSession(int paramInt) {
			// 用户被挤下线 
			KCloudUser.getInstance().sendMessage(
					CLDMessageId.MSG_ID_LOGIN_SESSION_INVAILD,
					paramInt);
		}

		@Override
		public void onBindMobile(int paramInt) {
			if (paramInt == 0) {
				KCloudUser.getInstance().sendMessage(
						CLDMessageId.MSG_ID_USERINFO_BIND_MOBILE_SUCCESS,
						paramInt);
			} else {
				KCloudUser.getInstance().sendMessage(
						CLDMessageId.MSG_ID_USERINFO_BIND_MOBILE_FAILED,
						paramInt);
			}
		}

		@Override
		public void onUnbindMobile(int paramInt) {
		}

		@Override
		public void onUpdateMobile(int paramInt) {
			if (paramInt == 0) {
				KCloudUser.getInstance().sendMessage(
						CLDMessageId.MSG_ID_USERINFO_REVISE_MOBILE_SUCCESS,
						paramInt);
				KCloudService.onClientListener(2, 0, 
						"MSG_ID_USERINFO_REVISE_MOBILE_SUCCESS");
			} else {
				KCloudUser.getInstance().sendMessage(
						CLDMessageId.MSG_ID_USERINFO_REVISE_MOBILE_FAILED,
						paramInt);
			}
		}

		@Override
		public void onBindEmail(int paramInt) {

		}

		@Override
		public void onUnbindEmail(int paramInt) {

		}

		@Override
		public void onUpdateEmail(int paramInt) {

		}

		@Override
		public void onRevisePwdByFastLogin(int paramInt) {

		}

		@Override
		public void onGetQRcodeResult(int paramInt) {
			if (paramInt == 0) {
				KCloudUser.getInstance().sendMessage(
						CLDMessageId.MSG_ID_LOGIN_GET_QRCODE_SUCCESS, 
						paramInt);
			} else {
				KCloudUser.getInstance().sendMessage(
						CLDMessageId.MSG_ID_LOGIN_GET_QRCODE_FAILED, 
						paramInt);
			}
		}

		@Override
		public void onGetQRLoginStatusResult(int arg0) {
			if (arg0 == 0) {
				// 登陆成功
				KCloudUser.getInstance().sendMessage(
						CLDMessageId.MSG_ID_LOGIN_QRCODE_LOGIN_SUCCESS, 0);
				KCloudService.onClientListener(0, 0, "MSG_ID_LOGIN_AUTO_LOGIN_SUCCESS");
			} else if (arg0 == 801) {
				// 二维码过期
				CldKAccountAPI.getInstance().getQRcode(0);
			}
		}
	};
	
	public static void init() {
		if (mKCloudAccountUtils == null) {
			mKCloudAccountUtils = new KCloudAccountUtils();
		}
		CldKAccountAPI.getInstance().setCldKAccountListener(mListener);
	}

	public static KCloudAccountUtils getInstance() {
		if (mKCloudAccountUtils == null) {
			synchronized (KCloudAccountUtils.class) {
				if (mKCloudAccountUtils == null) {
					mKCloudAccountUtils = new KCloudAccountUtils();
				}
			}
		}
		return mKCloudAccountUtils;
	}
}
