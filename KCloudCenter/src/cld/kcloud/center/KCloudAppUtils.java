package cld.kcloud.center;

import java.util.concurrent.TimeUnit;

public class KCloudAppUtils {
	public static final String LAUNCHER_PACKAGE_NAME = "com.cld.launcher";
	public static final String TARGET_PACKAGE_NAME = "cld.kcloud.center";
	public static final String TARGET_CLASS_NAME_MAIN = "cld.kcloud.user.KCloudMainActivity";
	public static final String TARGET_CLASS_NAME_USER = "cld.kcloud.user.KCloudUserActivity";
	public static final String TARGET_CLASS_NAME_USERINFO = "cld.kcloud.user.KCloudUserInfoActivity"; 
	
	public static final String ACTION_FLOW_FRESH = "cld.kcloud.action.FLOW_FRESH";
	public static final String ACTION_FLOW_GET_SUCCESS = "cld.kcloud.action.FLOW_GET_SUCCESS";
	public static final String ACTION_FLOW_GET_FAILED = "cld.kcloud.action.FLOW_GET_FAILED";
	
	/**
	 * 通知Launcher关闭服务界面
	 */
	public static final String ACTION_KCLOUD_LOGIN_FINISH = "kclound_login_finish";
	
	public static final String TARGET_DEFAULT_NAME = "logo.png";
	public static final String TAGGET_FIELD_USERNAME = "userName";
	public static final String TAGGET_FIELD_NICKNAME = "nickName";
	public static final String TAGGET_FIELD_PASSWORD = "password";
	public static final String TARGET_FIELD_LOGIN_STATUS = "login_status";
	public static final String TARGET_FIELD_SPLASH_LOGO = "splash_logo";
	public static final String TARGET_FIELD_DUID = "duid";
	public static final String TARGET_FIELD_KUID = "kuid";
	public static final String TARGET_FIELD_ICCID = "iccid";
	public static final String TARGET_FIELD_IMEI = "imei";
	public static final String TARGET_FIELD_CARD_STATUS = "card_status";
	public static final String TARGET_FIELD_REGISTER_ICCID = "register_iccid";
	public static final String TARGET_FIELD_LAST_TIP_PKID = "last_tip_pkid";
	public static final String TARGET_FIELD_LAST_TIP_TIME = "last_tip_time";
	public static final String TARGET_FIELD_LAST_REMAIN_DAY = "last_remain_day";
	public static final String TARGET_FIELD_LAST_REMAIN_FLOW = "last_remain_flow";
	public static final String TARGET_FIELD_LAST_CARD_STATUS = "last_card_status";
	
	public static final String WIDGET_KCLOUD_UPDATE_TIME = "widget_kcloud_update_time";
	public static final String WIDGET_KCLOUD_TOTAL_FLOW = "widget_kcloud_total_flow";
	public static final String WIDGET_KCLOUD_USED_FLOW = "widget_kcloud_used_flow";
	public static final String WIDGET_KCLOUD_REMAIN_FLOW = "widget_kcloud_remain_flow";
	public static final String WIDGET_KCLOUD_PROGRESS = "widget_kcloud_progress";
	public static final String WIDGET_KCLOUD_FLOW_UNIT = "widget_kcloud_flow_unit";
	public static final String WIDGET_KCLOUD_NAME = "widget_kcloud_name";
	public static final String WIDGET_KCLOUD_STATUS = "widget_kcloud_status";
	
	public static final String START_ACTIVITY_EXTRA = "start_activity_extra";
	
	public static final String CAR_SERIE_RESULT = "car_serie_result";
	public static final String CAR_SERIE_GET_TIME = "car_serie_get_time";
	
	public static final int FRAGMENT_PERSON = 0;
	public static final int FRAGMENT_CARINFO = 1;
	public static final int FRAGMENT_SERVICE = 2;
	public static final int FRAGMENT_FLOW = 3;
	public static final int FRAGMENT_RENEWAL = 4;
	
	public static enum InputError {
		eERROR_NONE,							
		eERROR_BASE,			          // 异常错误
		eERROR_ACCOUNT_EMPTY,	          // 账号为空
		eERROR_PASSWORD_EMPTY,	          // 密码为空
		eERROR_EMAIL_INPUT,		          // 错误的邮箱地址
		eERROR_ACCOUNT_INPUT,	          // 无效的账号
		eERROR_PASSWORD_INPUT,	          // 无效的密码
		eERROR_PASSWORD_CONTAINS_SPECAIL, // 密码包含特殊字符
		eERROR_PASSWORD_LESSONENUM,	      // 新密码至少包含1个数字和1个字母
		eERROR_OLD_PASSWORD_EMPTY,	      // 旧密码为空
		eERROR_NEW_PASSWORD_EMPTY,        // 新密码为空
		eERROR_AFFIRM_PASSWORD_EMPTY,     // 确认密码为空
		eERROR_NEW_OLD_SAME,		      // 新旧密码一致
		eERROR_NEW_AFFIRM_UNSAME,	      // 输入的新密码不一致
	};
	
	public static final class CLDMessageId {
		public static final int MSG_ID_WM_USER = 1024;
	
		// 登录界面消息
		public static final int MSG_ID_LOGIN_GET_QRCODE_SUCCESS = MSG_ID_WM_USER + 1;
		public static final int MSG_ID_LOGIN_GET_QRCODE_FAILED = MSG_ID_WM_USER + 2;
		public static final int MSG_ID_LOGIN_QRCODE_LOGIN_SUCCESS = MSG_ID_WM_USER + 3;  // 二维码登录成功能
		public static final int MSG_ID_LOGIN_GET_VERICODE_SUCCESS = MSG_ID_WM_USER + 4;  // 获取验证码失败
		public static final int MSG_ID_LOGIN_GET_VERICODE_FAILED = MSG_ID_WM_USER + 5;   // 获取验证码成功
		public static final int MSG_ID_LOGIN_MOBILE_LOGIN_SUCCESS = MSG_ID_WM_USER + 6;  // 手机登录成功
		public static final int MSG_ID_LOGIN_MOBILE_LOGIN_FAILED = MSG_ID_WM_USER + 7;   // 手机登录失败
		public static final int MSG_ID_LOGIN_ACCOUNT_LOGIN_SUCCESS = MSG_ID_WM_USER + 8; // 帐户登录成功
		public static final int MSG_ID_LOGIN_ACCOUNT_LOGIN_FAILED = MSG_ID_WM_USER + 9;  // 帐户登录失败
		public static final int MSG_ID_LOGIN_THIRD_LOGIN_SUCCESS = MSG_ID_WM_USER + 10;  // 第三方登录成功
		public static final int MSG_ID_LOGIN_THIRD_LOGIN_FAILED = MSG_ID_WM_USER + 11;   // 第三方登录成功
		public static final int MSG_ID_LOGIN_AUTO_LOGIN_SUCCESS = MSG_ID_WM_USER + 12;   // 自动登录成功
		public static final int MSG_ID_LOGIN_AUTO_LOGIN_FAILED = MSG_ID_WM_USER + 13;    // 自动登录失败
		public static final int MSG_ID_LOGIN_SESSION_INVAILD = MSG_ID_WM_USER + 14;      //  sesstion失效

		// 密码界面消息
		public static final int MSG_ID_PASSWORD_GET_VERICODE_SUCCESS = MSG_ID_LOGIN_SESSION_INVAILD + 1;// 获取验证码成功
		public static final int MSG_ID_PASSWORD_GET_VERICODE_FAILED = MSG_ID_LOGIN_SESSION_INVAILD + 2;// 获取验证码成功
		public static final int MSG_ID_PASSWORD_CHECK_VERICODE_SUCCESS = MSG_ID_LOGIN_SESSION_INVAILD + 3;
		public static final int MSG_ID_PASSWORD_CHECK_VERICODE_FAILED = MSG_ID_LOGIN_SESSION_INVAILD + 4;
		public static final int MSG_ID_PASSWORD_SET_PWD_SUCCESS = MSG_ID_LOGIN_SESSION_INVAILD + 5;
		public static final int MSG_ID_PASSWORD_SET_PWD_FAILED = MSG_ID_LOGIN_SESSION_INVAILD + 6;
		
		// 个人信息界面
		public static final int MSG_ID_USERINFO_GETDETAIL_SUCCESS = MSG_ID_PASSWORD_SET_PWD_FAILED + 1;
		public static final int MSG_ID_USERINFO_GETDETAIL_FAILED = MSG_ID_PASSWORD_SET_PWD_FAILED + 2;
		public static final int MSG_ID_USERINFO_BIND_MOBILE_VERICODE_SUCCESS = MSG_ID_PASSWORD_SET_PWD_FAILED + 3;
		public static final int MSG_ID_USERINFO_BIND_MOBILE_VERICODE_FAILED = MSG_ID_PASSWORD_SET_PWD_FAILED + 4;
		public static final int MSG_ID_USERINFO_BIND_MOBILE_SUCCESS = MSG_ID_PASSWORD_SET_PWD_FAILED + 5;
		public static final int MSG_ID_USERINFO_BIND_MOBILE_FAILED = MSG_ID_PASSWORD_SET_PWD_FAILED + 6;
		public static final int MSG_ID_USERINFO_REVISE_MOBILE_VERICODE_SUCCESS = MSG_ID_PASSWORD_SET_PWD_FAILED + 7;
		public static final int MSG_ID_USERINFO_REVISE_MOBILE_VERICODE_FAILED = MSG_ID_PASSWORD_SET_PWD_FAILED + 8;
		public static final int MSG_ID_USERINFO_REVISE_MOBILE_SUCCESS = MSG_ID_PASSWORD_SET_PWD_FAILED + 9;
		public static final int MSG_ID_USERINFO_REVISE_MOBILE_FAILED = MSG_ID_PASSWORD_SET_PWD_FAILED + 10;
		public static final int MSG_ID_USERINFO_PWD_VERICODE_SUCCESS = MSG_ID_PASSWORD_SET_PWD_FAILED + 11;
		public static final int MSG_ID_USERINFO_PWD_VERICODE_FAILED = MSG_ID_PASSWORD_SET_PWD_FAILED + 12;
		public static final int MSG_ID_USERINFO_PWD_CHECK_SUCCESS = MSG_ID_PASSWORD_SET_PWD_FAILED + 13;
		public static final int MSG_ID_USERINFO_PWD_CHECK_FAILED = MSG_ID_PASSWORD_SET_PWD_FAILED + 14;
		public static final int MSG_ID_USERINFO_UPDATE_SUCCESS = MSG_ID_PASSWORD_SET_PWD_FAILED + 15;
		public static final int MSG_ID_USERINFO_UPDATE_FAILED = MSG_ID_PASSWORD_SET_PWD_FAILED + 16;
		public static final int MSG_ID_USERINFO_LOGOUT_SUCCESS = MSG_ID_PASSWORD_SET_PWD_FAILED + 17;// 自动登录失败
		public static final int MSG_ID_USERINFO_LOGOUT_FAILED = MSG_ID_PASSWORD_SET_PWD_FAILED + 18;// 自动登录失败
		
		// 区域选择界面
		public static final int MSG_ID_LOCATION_CHANGE = MSG_ID_USERINFO_LOGOUT_FAILED + 1; 
		
		// 个人消息
		public static final int MSG_ID_MSGBOX_UPDATE = MSG_ID_LOCATION_CHANGE + 1;
		
		// 流量获取
		public static final int MSG_ID_KLDJY_FLOW_GET = MSG_ID_MSGBOX_UPDATE + 1;
		public static final int MSG_ID_KLDJY_FLOW_GET_SUCCESS = MSG_ID_MSGBOX_UPDATE + 2;
		public static final int MSG_ID_KLDJY_FLOW_GET_FAILED = MSG_ID_MSGBOX_UPDATE + 3;
		public static final int MSG_ID_KLDJY_FLOW_GET_FRESH = MSG_ID_MSGBOX_UPDATE + 4;
		public static final int MSG_ID_KLDJY_CHECK_CARD	= MSG_ID_MSGBOX_UPDATE + 5;
		
		// 闪屏信息
		public static final int MSG_ID_LOGO_TIP_GET = MSG_ID_KLDJY_CHECK_CARD + 1;
		public static final int MSG_ID_LOGO_TIP_FAILED = MSG_ID_KLDJY_CHECK_CARD + 2;
		public static final int MSG_ID_LOGO_TIP_SUCCESS = MSG_ID_KLDJY_CHECK_CARD + 3;
		
		// KGO信息
		public static final int MSG_ID_KGO_GETCODE_FAILED = MSG_ID_LOGO_TIP_SUCCESS + 1;
		public static final int MSG_ID_KGO_GET_CARLIST_SUCCESS = MSG_ID_LOGO_TIP_SUCCESS + 2;
		public static final int MSG_ID_KGO_GET_CARLIST_FAILED = MSG_ID_LOGO_TIP_SUCCESS + 3;
		public static final int MSG_ID_KGO_GET_USER_PACKAGE_LIST_FAILED = MSG_ID_LOGO_TIP_SUCCESS + 4;		// 获取套餐列表失败
		public static final int MSG_ID_KGO_GET_USER_PACKAGE_LIST_SUCCESS = MSG_ID_LOGO_TIP_SUCCESS + 5;		// 获取套餐列表成功
		public static final int MSG_ID_KGO_GET_SERVICES_APP_SUCCESS = MSG_ID_LOGO_TIP_SUCCESS + 6;			// 获取关联的应用
		public static final int MSG_ID_KGO_GET_SERVICES_APP_FAILED = MSG_ID_LOGO_TIP_SUCCESS + 7;
		public static final int MSG_ID_KGO_GET_PAY_STATUS = MSG_ID_LOGO_TIP_SUCCESS + 8;					// 获取支付状态
		
		// 车辆信息
		public static final int MSG_ID_CAR_GET_FAILED = MSG_ID_KGO_GET_USER_PACKAGE_LIST_FAILED + 1;
		public static final int MSG_ID_CAR_GET_SUCCESS = MSG_ID_KGO_GET_USER_PACKAGE_LIST_FAILED + 2;
		public static final int MSG_ID_CAR_UPDATE_FAILED = MSG_ID_KGO_GET_USER_PACKAGE_LIST_FAILED + 3;
		public static final int MSG_ID_CAR_UPDATE_SUCCESS = MSG_ID_KGO_GET_USER_PACKAGE_LIST_FAILED + 4;
		
		
		public static final int MSG_ID_WM_USER_COMMON = MSG_ID_WM_USER + 2048; 
		public static final int MSG_ID_UPDATE_CODE_RAMAIN_TIME = MSG_ID_WM_USER_COMMON + 1;//验证码等待消息ID
		public static final int MSG_ID_LOGIN_LOST_PWD = MSG_ID_WM_USER_COMMON + 2;	// 找回密码
		public static final int MSG_ID_CHECK_CARD = MSG_ID_WM_USER_COMMON + 3;	// 检查卡
		public static final int MSG_ID_SHOW_SERVICE_LIST = MSG_ID_WM_USER_COMMON + 4;	// 显示套餐列表
		public static final int MSG_ID_SHOW_RENEWAL_QRCODE = MSG_ID_WM_USER_COMMON + 5;	// 显示续费界面
		public static final int MSG_ID_CHECK_SESSION_INVAILD = MSG_ID_WM_USER_COMMON + 6;	// 检查session是否有效
	}	
	
	public static boolean isTestVersion() {
		return false;
	}
	
	/**
	 * 判断是否超时
	 * @param timestamp 时间戳
	 */
	public static boolean isTimeout(long timestamp) {
		long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
		return timestamp - timeSeconds >= 0 ? false : true;
	}
}
