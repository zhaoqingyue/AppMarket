/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: MainService.java
 * @Prject: KCloudCenter_M550
 * @Package: cld.navi.position.frame
 * @Description: 主服务，处理主要的逻辑
 * @author: zhaoqy
 * @date: 2016年8月15日 上午9:41:15
 * @version: V1.0
 */

package cld.navi.position.frame;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.cld.log.CldLog;
import com.cld.ols.api.CldKAccountAPI;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.bugly.crashreport.CrashReport.UserStrategy;
import cld.kcloud.center.R;
import cld.kcloud.custom.manager.KCloudPositionManager;
import cld.navi.position.model.CollectGPSThread;
import cld.navi.position.model.GpsCallBackListen;
import cld.navi.position.model.ReadGpsLogThread;
import cld.navi.position.model.ReportDataQueue;
import cld.navi.position.model.ReportPositionThread;
import cld.navi.util.DeviceUtils;
import cld.navi.util.FileUtils;
import cld.navi.util.Md5Utils;
import cld.navi.util.NetWorkRequest;
import cld.navi.util.NetWorkUtil;
import cld.navi.util.SharePrefUtils;
import cld.navi.util.VersionUtils;
import cld.navi.weixin.WeixinUtil;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class MainService extends Service implements
		ReportPositionThread.GetKuidSessionCallback,
		CollectGPSThread.GetRuidCallback {

	// 初始化相关
	private final static String TAG = "MainService";
	private final String NAVI_SERVICE_ACTION = "cld.navi.IGetParamFromNavi";

	/**
	 * 主线程相关消息
	 */
	private final static int mRegisterGPSMsg = 1001;// 注册GPS回调
	private final static int mStartCollectMsg = mRegisterGPSMsg + 1;
	private final static int mStartUpPostionMsg = mStartCollectMsg + 1;
	private final static int mWemeMainInitMsg = mStartUpPostionMsg + 1;

	/**
	 * 同步导航定义apptype、appid等相关
	 */
	public static final int KCLOUD_APIVER = 1;
	public static final int KCLOUD_APPID = 24;
	public static final int APPTYPE_TO_BUSSINESSID = 12;
	public static final int VALUE_OF_APPTYPE = 53; // M330:51; M530:52; M550:53

	private Timer mTimer = null;// 主定时器
	private DetecMoniSevTask mDetecMoniSevTask = null;// 监测监控服务的定时任务
	private ApnTask mApnTask = null;// 检测apn任务

	private boolean isLogToFile = false;
	private boolean isTestServer = false;

	// 获取网络配置以及其他的一些初始化等
	private MainHandler mMainHandler;
	private MainThread mMainThread;

	/********************** 上报位置凯立德服务端相关接口变量 ****************************/
	private IGetParamFromNavi mNaviParamGet = null;
	// 位置上传
	int isMirrTalkOpen = 1;// 
	int isCldNaviOpen = 1;
	int mCardBelongTo = 0; // 0为非语境卡，100为语境卡
	int mDeviceId = 0;     // duid
	int mRecordRate = 10;  // 默认10s
	int mUpRate = 30;      // 默认30s

	// sim卡激活参数
	final int mDcode = 1;
	final int mPcode = 7;
	final int mCustid = 1270;

	private final long QUERY_APN_INTERVAL = 1000 * 60 * 10;// 5*1000*60;
	private final long QUERY_APN_DELAY = 1000 * 60;

	String mUpPosionHead = "http://tmctest.careland.com.cn/kposition/";// 默认
	/**************************************************************************************/
	String getKodekey = "1F42AF9B4AE3DDB194BBF00A14CC2DC7";
	String testGetKodekey = "D0E484FCA2BE6038D170DFACC6141DA7";
	String mKaccountGetCodeUrl = "http://st.careland.com.cn/ka/api/"; // 默认取秘钥的地址

	String mQureyTestKey = "1a86fb49b070f26d7948d7931ed69233";// 查询流量卡归属接口的测试加密串
	String mQureyKey = "1a86fb49b070f26d7948d7931ed69233";// 查询流量卡归属接口的正式加密串:还没提供
	String mQureyTestUrl = "http://test.careland.com.cn/kldjy/www/?mod=iov&ac=getcardsupplier";
	String mQureyUrl = "http://navione.careland.com.cn/?mod=iov&ac=getcardsupplier";

	String mQureyApnTestUrl = "http://test.careland.com.cn/kldjy/www/?mod=iov&ac=getcardapn";
	String mQureyApnUrl = "http://navione.careland.com.cn/?mod=iov&ac=getcardapn";

	String getDeviceIdKey = null;// 获取设备ID的秘钥，从服务器上取
	/**************************************************************************************/

	final String CFG_URL = "http://st.careland.com.cn/tc/control_download.php";
	final String TEST_CFG_URL = "http://sttest.careland.com.cn/tc/control_download.php";

	// 用于拉取记录频率、上报频率、2个位置开关、凯立德上报地址的key.
	String testRateHeadKey = "D20B600B4C2060EBC21A97AB5557912A";
	String rateHeadKey = "B9720F0D8E5CBCAFC5B6CF409E01C1ED";
	long classtypesGetRate = 2002001000;
	long classtypesGetHead = 1001001000;

	//
	private ReportDataQueue mReportDataQueue = null;
	private CollectGPSThread mCollectGPSThread = null;
	private ReportPositionThread mUpPositionThread = null;
	private GpsCallBackListen mGpsReport = null;
	private BroadcastReceiver mBroadcastReceiver = null;
	/****************************************************************************/

	/*********** 微信小凯互联 ****/
	private final int MSG_NET_DISCONNECTED = 0;
	private final int MSG_GET_WEIXIN_SUCCESS = 1;
	private final int MSG_GET_WEIXIN_FAILED = 2;
	private final int MSG_GET_WEIXIN_ALREADY = 3;
	private final int MSG_GET_DEVICECODE_FAILED = 4;

	private boolean mBPostWeixinQr = true;

	public static int STATUS_FAILED = -1;  // 失败
	public static int STATUS_GETTING = 0;  // 初始状态, 正在获取中
	public static int STATUS_NETERROR = 1; // 网络异常
	public static int STATUS_SUCCESS = 2;  // 成功
	public static int mStatus = STATUS_GETTING;
	public static String mWeixinString = null;

	public static String getWeixinString() {
		return mWeixinString;
	}

	public static void setWeixinString(String s) {
		if (null != s) {
			mWeixinString = s;
			CldLog.i(TAG, "mWeixinString:" + mWeixinString);
		}
	}

	public static int getWeixinStatus() {
		return mStatus;
	}

	public static void setWeixinStatus(int status) {
		mStatus = status;
	}

	WeixinUtil mWeixinUtil = new WeixinUtil();
	MyWeixinCallBack mWeixinCallBack = null;
	@SuppressLint("HandlerLeak") 
	Handler handlerWeixin = new Handler() {

		@SuppressLint("ShowToast")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == MSG_NET_DISCONNECTED) {
				SharePrefUtils.putShareInt(SharePrefUtils.WEIXIN_DUID, -1);
				SharePrefUtils.putShareString(SharePrefUtils.WEIXIN_QR, "");

				PostWeixinQr("", MainService.STATUS_NETERROR);
				setWeixinStatus(MainService.STATUS_NETERROR);
			} else if (msg.what == MSG_GET_DEVICECODE_FAILED) {
				SharePrefUtils.putShareInt(SharePrefUtils.WEIXIN_DUID, -1);
				SharePrefUtils.putShareString(SharePrefUtils.WEIXIN_QR, "");

				PostWeixinQr("", MainService.STATUS_FAILED);
				setWeixinStatus(MainService.STATUS_FAILED);
			} else if (msg.what == MSG_GET_WEIXIN_SUCCESS) {
				String weixinStr = null;
				if (mWeixinCallBack != null) {
					weixinStr = GetQrReturn(mWeixinCallBack.weixinReturnMsg);
				}
				if (weixinStr != null) {
					CldLog.i(TAG, "get weixin :" + weixinStr);
					CldLog.i(TAG, "weixinStr:" + weixinStr);
					MainService.setWeixinString(weixinStr);

					SharePrefUtils.putShareInt(SharePrefUtils.WEIXIN_DUID,
							mDeviceId);
					SharePrefUtils.putShareString(SharePrefUtils.WEIXIN_QR,
							weixinStr);

					PostWeixinQr(weixinStr, MainService.STATUS_SUCCESS);
					setWeixinStatus(MainService.STATUS_SUCCESS);
				} else {
					CldLog.i(TAG, "get weixin yes but create failed");
					SharePrefUtils.putShareInt(SharePrefUtils.WEIXIN_DUID, -1);
					SharePrefUtils.putShareString(SharePrefUtils.WEIXIN_QR, "");
					PostWeixinQr("", MainService.STATUS_FAILED);
					setWeixinStatus(MainService.STATUS_FAILED);
				}
			} else if (msg.what == MSG_GET_WEIXIN_FAILED) {
				CldLog.i(TAG, "get weixin failed");
				SharePrefUtils.putShareInt(SharePrefUtils.WEIXIN_DUID, -1);
				SharePrefUtils.putShareString(SharePrefUtils.WEIXIN_QR, "");

				PostWeixinQr("", MainService.STATUS_FAILED);
				setWeixinStatus(MainService.STATUS_FAILED);
			} else if (msg.what == MSG_GET_WEIXIN_ALREADY) {
				CldLog.i(TAG, "get weixin already");
				String mWeixinQr = SharePrefUtils.getShareString(
						SharePrefUtils.WEIXIN_QR, "");
				MainService.setWeixinString(mWeixinQr);
				PostWeixinQr(mWeixinQr, MainService.STATUS_SUCCESS);
				setWeixinStatus(MainService.STATUS_SUCCESS);
			}
		}
	};

	/* end*********小凯互联******** */

	// M530 SIM卡类型相关
	// 1 正常（已激活的服务卡）
	// 0 其他错误
	// -1 ICCID卡不存在（外来卡）
	// -2 未激活的服务卡（ICCID与SN未绑定）
	// -3卡被锁定（卡被插在非法设备上）
	// -4 ICCID与SN绑定关系与服务端不一致
	// -5卡状态异常（未出库、已停用、已退货、卡流量异常将被停卡等异常）
	public class SimCardTypeInfo {
		public int pkErrCode; // 卡状态
		public String pkErrMsg; // 错误描述
		public int pkId; // 套餐ID
		public String pkName; // 套餐名称
		public String pkDesc; // 套餐描述
	}

	public static final String MSG_TO_REGISTER = "cld.navi.position.frame.registersimcard";
	public static final String MSG_TO_POPREGISTERTIP = "cld.navi.position.frame.popregistersimcardtip";

	public static final String KEY_ERRCOED = "errcode";
	public static final String KEY_ERRMSG = "errmsg";
	public static final String KEY_DATA = "data";
	public static final String KEY_PKID = "pkid";
	public static final String KEY_PKNAME = "pkname";
	public static final String KEY_PKDESC = "pkdesc";

	public static final String NAME_SHAREFILE = "SimCardType";

	public class SimCardTypeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			CldLog.i(TAG, "onReceive:" + action);
			if (action.equals(MSG_TO_REGISTER)) {
				startRegisterSimCard();
			}
		}
	}

	private SimCardTypeReceiver simCardTypeReceiver = new SimCardTypeReceiver();
	public static SimCardTypeInfo mSimCardTypeInfo;
	public static int mLastSimType;

	public void checkSimCardExist() {
		TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		int state = manager.getSimState();
		// 增加提示
		if (state == TelephonyManager.SIM_STATE_ABSENT)
			Toast.makeText(this, R.string.nosimcardtip, Toast.LENGTH_LONG)
					.show();
		CldLog.i(TAG, "state:" + state);
	}

	public void registerSimCardTypeReceiver() {
		registerReceiver(simCardTypeReceiver, new IntentFilter(MSG_TO_REGISTER));
	}

	public void unRegisterSimCardTypeReceiver() {
		unregisterReceiver(simCardTypeReceiver);
	}

	/**
	 * 获取卡类型
	 */
	public int doCheckSimCardType() throws JSONException {
		NetWorkRequest netRequest = KCloudPositionManager.getInstance()
				.getNetRequest();
		JSONObject jsonResult = null;
		String sign = null;
		String url = null;

		String testKey = "1a86fb49b070f26d7948d7931ed69233";
		String key = "1a86fb49b070f26d7948d7931ed69233";

		String pTestUrl = "http://test.careland.com.cn/kldjy/www/?mod=iov&ac=checkcard";
		String pRealUrl = "http://navione.careland.com.cn/?mod=iov&ac=checkcard";

		String paramUnSort = "apiver=1";
		String paramMd5UnSort = "apiver=1";

		if (null == mIccidNum) {
			mIccidNum = NetWorkUtil.getICCIDNum(MainService.this);
		}
		if (null == mImsiNum) {
			mImsiNum = NetWorkUtil.getImsi(MainService.this);
		}
		if (null == mPhoneNum) {
			mPhoneNum = NetWorkUtil.getPhoneNum(MainService.this);
		}

		if ((null == mIccidNum || mIccidNum.equals(""))
				&& (null == mImsiNum || mImsiNum.equals(""))
				&& (null == mPhoneNum || mPhoneNum.equals(""))) {
			CldLog.i(TAG, "doCheckCardState mIccidNum,imsi,sim are all null");
			return -1;
		}

		if (!isNetworkAvailable()) {
			CldLog.i(TAG, "doFirstActivateRegister network error");
			return 0;
		}

		if (mIccidNum != null && !"".equals(mIccidNum)) {
			paramMd5UnSort = paramMd5UnSort + "&iccid=" + mIccidNum;
			paramUnSort = paramUnSort + "&iccid=" + mIccidNum;
		} else {
			paramUnSort = paramUnSort + "&iccid=" + "";
		}

		if (mImsiNum != null && !"".equals(mImsiNum)) {
			paramMd5UnSort = paramMd5UnSort + "&imsi=" + mImsiNum;
			paramUnSort = paramUnSort + "&imsi=" + mImsiNum;
		} else {
			paramUnSort = paramUnSort + "&imsi=" + "";
		}

		if (mPhoneNum != null && !"".equals(mPhoneNum)) {
			paramMd5UnSort = paramMd5UnSort + "&sim=" + mPhoneNum;
			paramUnSort = paramUnSort + "&sim=" + mPhoneNum;
		} else {
			paramUnSort = paramUnSort + "&sim=" + "";
		}

		String typeStr = FileUtils.readAssetsFile(getApplicationContext(), 0);
		if (typeStr != null) {
			typeStr = typeStr.replace("\r", "").replace("\n", "");
		}
		int type = Integer.valueOf(typeStr).intValue();
		String argValue = DeviceUtils.getSerialNum(this, type);
		if (null == argValue && "".equals(argValue)) {
			CldLog.i(TAG, "doCheckCardState argValue is null");
			return -1;
		}

		paramMd5UnSort = paramMd5UnSort + "&sn=" + argValue;
		paramUnSort = paramUnSort + "&sn=" + argValue;

		typeStr = FileUtils.readAssetsFile(getApplicationContext(), 1);
		if (typeStr != null) {
			typeStr = typeStr.replace("\r", "").replace("\n", "");
		}

		String temp[] = typeStr.split("-");

		paramMd5UnSort = paramMd5UnSort + "&ver=" + temp[0];
		paramUnSort = paramUnSort + "&ver=" + temp[0];

		paramMd5UnSort = paramMd5UnSort + "&duid=" + mDeviceId;
		paramUnSort = paramUnSort + "&duid=" + mDeviceId;

		String paramSortted = Md5Utils.sortParam(paramUnSort);
		String paramMd5Sortted = Md5Utils.sortParam(paramMd5UnSort);

		if (isTestServer) {
			sign = Md5Utils.MD5(paramMd5Sortted + testKey);
			url = pTestUrl + "&" + paramSortted + "&sign=" + sign;
		} else {
			sign = Md5Utils.MD5(paramMd5Sortted + key);
			url = pRealUrl + "&" + paramSortted + "&sign=" + sign;
		}

		CldLog.i(TAG, "doCheckCardState url:" + url);

		jsonResult = netRequest.SendGetJson(url);
		if (jsonResult != null) {
			mSimCardTypeInfo = new SimCardTypeInfo();
			mSimCardTypeInfo.pkErrCode = jsonResult.getInt(KEY_ERRCOED);
			mSimCardTypeInfo.pkErrMsg = jsonResult.getString(KEY_ERRMSG);

			CldLog.i(TAG, "doCheckCardState errorCode:"
					+ mSimCardTypeInfo.pkErrCode);

			if (mSimCardTypeInfo.pkErrCode == 1
					|| mSimCardTypeInfo.pkErrCode == -2) {
				JSONObject data = jsonResult.getJSONObject(KEY_DATA);
				if (data != null) {
					mSimCardTypeInfo.pkId = data.getInt(KEY_PKID);
					mSimCardTypeInfo.pkName = data.getString(KEY_PKNAME);
					mSimCardTypeInfo.pkDesc = data.getString(KEY_PKDESC);
				}
				CldLog.i(TAG, "doCheckCardState success:" + "pkId:"
						+ mSimCardTypeInfo.pkId + " pkName:"
						+ mSimCardTypeInfo.pkName + " pkDesc:"
						+ mSimCardTypeInfo.pkDesc);
				mMainHandler.sendEmptyMessage(MainHandler.mCheckSimCardType);
			}
			return 1;
		} else {
			CldLog.i(TAG, "doCheckCardState jsonResult is null");
		}
		return 0;
	}

	// 获取卡状态（非服务器，已激活服务卡，未激活的服务卡）、卡套餐类型
	public void startCheckSimCardType() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				int ret = 0;
				while (ret == 0) {
					try {
						ret = doCheckSimCardType();
					} catch (JSONException e) {
						e.printStackTrace();
					}
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					CldLog.i(TAG, "ret:" + ret);
				}
			}
		}).start();
	}

	// 获取上次卡保存状态
	public int checkLastSaveSimCardType() {
		int ret = 0;
		SharedPreferences simCardType = getSharedPreferences(NAME_SHAREFILE,
				MODE_PRIVATE);
		String iccid = simCardType.getString("iccid", null);
		String imsi = simCardType.getString("imsi", null);
		String sim = simCardType.getString("sim", null);
		if (iccid != null && mIccidNum != null && iccid.equals(mIccidNum)
				|| imsi != null && mImsiNum != null && imsi.equals(mImsiNum)
				|| sim != null && mPhoneNum != null && sim.equals(mPhoneNum)) {
			ret = simCardType.getInt(KEY_ERRCOED, 0);
		}
		CldLog.i(TAG, "checkLastSaveSimCardType-ret" + ret);
		return ret;
	}

	public void startRegisterSimCard() {
		// 开始登记激活
		doFirstActivateRegister_httppost();
	}

	public void startRegisterActivity() {
		final Context context = MainService.this;
		final Intent intent = new Intent(MSG_TO_POPREGISTERTIP);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		Timer delayTimer = new Timer();
		TimerTask delayTimerTask = new TimerTask() {

			@Override
			public void run() {
				context.startActivity(intent);
				CldLog.i(TAG, "startRegisterActivity");
			}
		};
		delayTimer.schedule(delayTimerTask, 30 * 1000);

	}

	@SuppressLint("NewApi")
	public void saveSimCardRegisterState() {
		SharedPreferences simCardType = getSharedPreferences(NAME_SHAREFILE,
				MODE_PRIVATE);
		Editor editor = simCardType.edit();
		if (mIccidNum != null && !mIccidNum.equals("")) {
			editor.putString("iccid", mIccidNum);
		}
		if (mImsiNum != null && !mImsiNum.equals("")) {
			editor.putString("imsi", mImsiNum);
		}
		if (mPhoneNum != null && !mPhoneNum.equals("")) {
			editor.putString("sim", mPhoneNum);
		}
		editor.putInt(KEY_ERRCOED, 1);
		editor.apply();
	}

	// 卡套餐
	private static SimCardPackages cardPackages;
	private static int lastNumPackages = 0;

	/**
	 * 获取卡的所有套餐
	 */
	public int doCheckSimCardPackages() throws JSONException {
		NetWorkRequest netRequest = KCloudPositionManager.getInstance()
				.getNetRequest();
		JSONObject jsonResult = null;
		String sign = null;
		String url = null;

		String testKey = "1a86fb49b070f26d7948d7931ed69233";
		String key = "1a86fb49b070f26d7948d7931ed69233";

		String pTestUrl = "http://test.careland.com.cn/kldjy/www/?mod=iov&ac=getcardpackage";
		String pRealUrl = "http://navione.careland.com.cn/?mod=iov&ac=getcardpackage";

		String paramUnSort = "apiver=1";
		String paramMd5UnSort = "apiver=1";

		if (null == mIccidNum) {
			mIccidNum = NetWorkUtil.getICCIDNum(MainService.this);
		}
		if (null == mImsiNum) {
			mImsiNum = NetWorkUtil.getImsi(MainService.this);
		}
		if (null == mPhoneNum) {
			mPhoneNum = NetWorkUtil.getPhoneNum(MainService.this);
		}

		if ((null == mIccidNum || mIccidNum.equals(""))
				&& (null == mImsiNum || mImsiNum.equals(""))
				&& (null == mPhoneNum || mPhoneNum.equals(""))) {
			CldLog.i(TAG, "doCheckCardPackage mIccidNum,imsi,sim are all null");
			return -1;
		}

		if (!isNetworkAvailable()) {
			CldLog.i(TAG, "doCheckCardPackage network error");
			return 0;
		}

		if (mIccidNum != null && !"".equals(mIccidNum)) {
			paramMd5UnSort = paramMd5UnSort + "&iccid=" + mIccidNum;
			paramUnSort = paramUnSort + "&iccid=" + mIccidNum;
		} else {
			paramUnSort = paramUnSort + "&iccid=" + "";
		}

		if (mImsiNum != null && !"".equals(mImsiNum)) {
			paramMd5UnSort = paramMd5UnSort + "&imsi=" + mImsiNum;
			paramUnSort = paramUnSort + "&imsi=" + mImsiNum;
		} else {
			paramUnSort = paramUnSort + "&imsi=" + "";
		}

		if (mPhoneNum != null && !"".equals(mPhoneNum)) {
			paramMd5UnSort = paramMd5UnSort + "&sim=" + mPhoneNum;
			paramUnSort = paramUnSort + "&sim=" + mPhoneNum;
		} else {
			paramUnSort = paramUnSort + "&sim=" + "";
		}

		String typeStr = FileUtils.readAssetsFile(getApplicationContext(), 0);
		if (typeStr != null) {
			typeStr = typeStr.replace("\r", "").replace("\n", "");
		}
		int type = Integer.valueOf(typeStr).intValue();
		String argValue = DeviceUtils.getSerialNum(this, type);
		if (null == argValue && "".equals(argValue)) {
			CldLog.i(TAG, "doCheckCardPackage argValue is null");
			return -1;
		}

		paramMd5UnSort = paramMd5UnSort + "&sn=" + argValue;
		paramUnSort = paramUnSort + "&sn=" + argValue;

		typeStr = FileUtils.readAssetsFile(getApplicationContext(), 1);
		if (typeStr != null) {
			typeStr = typeStr.replace("\r", "").replace("\n", "");
		}

		String temp[] = typeStr.split("-");

		paramMd5UnSort = paramMd5UnSort + "&ver=" + temp[0];
		paramUnSort = paramUnSort + "&ver=" + temp[0];

		paramMd5UnSort = paramMd5UnSort + "&duid=" + mDeviceId;
		paramUnSort = paramUnSort + "&duid=" + mDeviceId;

		String paramSortted = Md5Utils.sortParam(paramUnSort);
		String paramMd5Sortted = Md5Utils.sortParam(paramMd5UnSort);

		if (isTestServer) {
			sign = Md5Utils.MD5(paramMd5Sortted + testKey);
			url = pTestUrl + "&" + paramSortted + "&sign=" + sign;
		} else {
			sign = Md5Utils.MD5(paramMd5Sortted + key);
			url = pRealUrl + "&" + paramSortted + "&sign=" + sign;
		}
		CldLog.i(TAG, "doCheckCardPackage url:" + url);
		jsonResult = netRequest.SendGetJson(url);
		if (jsonResult != null) {
			String errorCode = jsonResult.getString("errcode");
			CldLog.i(TAG, "doCheckCardPackage-errorCode:" + errorCode
					+ " jsonResult:" + jsonResult);
			if (errorCode != null && errorCode.equals("0")) {
				SimCardPackages pkgs = new SimCardPackages();
				JSONArray dataArray = jsonResult.getJSONArray("data");
				for (int i = 0; i < dataArray.length(); i++) {

					JSONObject data = dataArray.getJSONObject(i);
					SimCardPackageInfo cardPackageInfo = new SimCardPackageInfo();
					cardPackageInfo.pkid = data.getInt("pkid");
					cardPackageInfo.pkalias = data.getString("pkalias");
					cardPackageInfo.pkmonths = data.getInt("pkmonths");
					cardPackageInfo.pktraffic = data.getString("pktraffic");
					cardPackageInfo.pkabletime = data.getString("pkabletime");
					pkgs.cardpkList.add(cardPackageInfo);
				}
				if ((null != mIccidNum && !mIccidNum.equals(""))) {
					pkgs.strId = mIccidNum;
				} else if (null != mImsiNum && !mImsiNum.equals("")) {
					pkgs.strId = mImsiNum;
				} else {
					pkgs.strId = mPhoneNum;
				}
				if (pkgs.cardpkList.size() > 0) {

					/**
					 * 暂时注释掉
					 */
					/*
					 * setSimPackages(pkgs,MainService.this); Intent
					 * updateIntent = new
					 * Intent(SimCardPackageActivity.MSG_TO_UPDATAPKG);
					 * MainService.this.sendBroadcast(updateIntent);
					 */

					CldLog.i(TAG, "sendBroadcast");
				}
				return 1;
			}
		} else {
			CldLog.i(TAG, "doCheckCardPackage jsonResult is null");
		}
		return 0;
	}

	static public int getLastNumPackages() {
		return lastNumPackages;
	}

	static public void sortSimPackages(SimCardPackages pkgs) {
		int len = pkgs.cardpkList.size();
		SimCardPackages simPkgs = new SimCardPackages();
		simPkgs.strId = pkgs.strId;
		if (len > 1) {

			final class ComparatorValues implements
					Comparator<SimCardPackageInfo> {

				@Override
				public int compare(SimCardPackageInfo object1,
						SimCardPackageInfo object2) {
					return object1.pkabletime.compareTo(object2.pkabletime);
				}
			}
			Collections.sort(pkgs.cardpkList, new ComparatorValues());
			for (int i = 0; i < len; i++) {
				CldLog.i(TAG, "sortSimPackages:"
						+ pkgs.cardpkList.get(i).pkabletime);
			}
		}
	}

	// 设置套餐信息，并保存到参数
	static public void setSimPackages(SimCardPackages pkgs, Context context) {
		if (cardPackages != null && cardPackages.cardpkList != null)
			lastNumPackages = cardPackages.cardpkList.size();
		else {
			lastNumPackages = 0;
		}
		sortSimPackages(pkgs);
		cardPackages = pkgs;
		cardPackages.saveToFile(context);
	}

	// 获取套餐信息
	static public SimCardPackages getSimPackages() {
		if (cardPackages == null)
			cardPackages = new SimCardPackages();
		return cardPackages;
	}

	// 根据参数读取上次保存的状态
	void checkLastSaveSimCardPackages() {
		cardPackages = SimCardPackages.readFromFile(this);
		if (cardPackages == null)
			cardPackages = new SimCardPackages();
		else {
			CldLog.i(TAG, "checkLastSaveSimCardPackages-size:"
					+ cardPackages.cardpkList.size());
			for (int i = 0; i < cardPackages.cardpkList.size(); i++) {
				CldLog.i(TAG, "checkLastSaveSimCardPackages-pkalias:"
						+ cardPackages.cardpkList.get(i).pkalias);
			}
		}
	}

	// end M530相关

	/*
	 * 主线程处理器，主要做了个语境初始化已经注册GPS、开启GPS收集线程、开启上报线程??????定时器??
	 */
	@SuppressLint("HandlerLeak") 
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case mRegisterGPSMsg:// 注册GPS
			{
				mGpsReport = new GpsCallBackListen(MainService.this,
						mRecordRate, mCollectGPSThread);
				mGpsReport.registerGPS();// 主线程注册GPS，这里参考了导航的设计，主要是为了保证GPS回调线程和GPS数据进队列的线程不要为同一线程导致阻塞。
				mHandler.sendEmptyMessage(mStartUpPostionMsg);
				break;
			}

			case mStartCollectMsg:// GPS数据收集线程
			{
				int queueSize = 7200 / mRecordRate;// 根据频率配置两小时的缓冲
				mReportDataQueue = new ReportDataQueue(queueSize);
				mCollectGPSThread = new CollectGPSThread(mReportDataQueue,
						MainService.this);
				mCollectGPSThread.start();// 收集GPS 相关数据线程
				if (FileUtils.readLogFile())// 跑日志
				{
					new ReadGpsLogThread(mCollectGPSThread, mRecordRate)
							.start();// 读日志线程
					mHandler.sendEmptyMessage(mStartUpPostionMsg);
				} else // 注册GPS回调
				{
					mHandler.sendEmptyMessage(mRegisterGPSMsg);// 主线程注册GPS回调
				}
				break;
			}
			case mStartUpPostionMsg: {
				mUpPositionThread = new ReportPositionThread(mReportDataQueue,
						mUpPosionHead, mDeviceId, MainService.this);
				mUpPositionThread.start();
				// 定时发消息给上报线程
				if (mTimer != null) {
					mTimer.schedule(new TimerTask() {

						@Override
						public void run() {
							mUpPositionThread
									.removeMessge(ReportPositionThread.MSG_UPPOSITON_UPDATE);// 先把以前的消息删除，防止消息堆积。
							mUpPositionThread
									.sendEmptyMessage(ReportPositionThread.MSG_UPPOSITON_UPDATE);
						}
					}, 5000, mUpRate * 1000);// 根据服务器配置频率设置时间。
				}
				break;
			}
			default:
				break;
			}
		}
	};

	/*
	 * 判断当前应用是否前台
	 */
	public boolean isAppOnForeground(String packageName, String className) {
		if ((null == packageName || "".equals(packageName))
				&& (null == className || "".equals(className))) {
			CldLog.i(TAG, "isAppOnForeground return false--begin");
			return false;
		}

		ComponentName cn = ((ActivityManager) getSystemService(ACTIVITY_SERVICE))
				.getRunningTasks(1).get(0).topActivity;

		CldLog.i(TAG, "packagename:" + cn.getPackageName() + " classname:"
				+ cn.getClassName());

		if ((packageName != null && packageName.equals(cn.getPackageName()))
				|| (className != null && className.equals(cn.getClassName()))) {
			CldLog.i(TAG, "isAppOnForeground return true");
			return true;
		}

		CldLog.i(TAG, "isAppOnForeground return false");
		return false;
	}

	/*
	 * 判断是否需要主动弹出流量续费页面
	 */
	public boolean isNeedAutoPopupSerRenewal() {
		// String launcherPackageName = "com.chenli.launcher";
		// if (!isAppOnForeground(launcherPackageName))
		// {
		// return false;
		// }

		long lCurTime = System.currentTimeMillis();
		long lLastTipTime = 0;

		Bundle bundle = getLastTipTimeFromSharepref();
		lLastTipTime = bundle.getLong("time");
		mLastRemFlowPercent = bundle.getInt("RemFlowPercent");
		mLastReminddays = bundle.getInt("LastReminddays");

		CldLog.i(TAG, "isNeedAutoPopupSerRenewal mRemind:" + mRemind
				+ " mRemaindays:" + mRemaindays + " mFlowType:" + mFlowType
				+ " mLastRemFlowPercent" + mLastRemFlowPercent
				+ " mCurRemFlowPercent" + mCurRemFlowPercent);

		if (1 == mFlowType) {
			if (mRemaindays > 0 && mCurRemFlowPercent <= 20
					&& mCurRemFlowPercent > 0) {
				if ((0 == mLastRemFlowPercent || (mLastRemFlowPercent > 5 && mCurRemFlowPercent <= 5))
						&& false == IsSameDay(lCurTime, lLastTipTime)) {
					mLastRemFlowPercent = mCurRemFlowPercent;
					mLastReminddays = mRemaindays;
					return true;
				}
			}
		}

		if (mSimStatus >= 1 && mSimStatus < 5) {
			if (mSimStatus >= 1 && mSimStatus <= 3) {
				if (2 == mFlowType
						|| (1 == mFlowType && 0 != mCurRemFlowPercent)) {
					if (((0 == mLastReminddays || mLastReminddays > 7)
							&& mRemaindays <= 7 && mRemaindays > 1)
							|| ((0 == mLastReminddays || mLastReminddays > 1) && 1 == mRemaindays)) {
						if (false == IsSameDay(lCurTime, lLastTipTime)) {
							mLastRemFlowPercent = mCurRemFlowPercent;
							mLastReminddays = mRemaindays;
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	/*
	 * 主动弹出流量续费页面
	 */
	public void startShowServiceRenewal() {

		// M530 如果当前有未生效套餐，不自动进入
		if (cardPackages != null && cardPackages.cardpkList.size() > 1)
			return;

		Context context = this.getApplicationContext();
		Intent intent = new Intent(
				"cld.navi.position.frame.StartServiceRenewalActivity");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	public static int mSimStatus = -1; // 状态
	public static String mActivedate = null;// 激活日期
	public static String mDuedate = null;// 到期日期
	public static int mRemaindays = 0;// 剩余有效期
	public static String mTotalFlow = null;// 总流量
	public static String mUsedFlow = null;// 已使用流量
	public static int mRemind = 0;// 剩余流量达到提醒条件(如已使用80%，95%)
	public static int mFlowType = 0;// 流量套餐类型 1：流量计费 2：包年计费
	public static String mFlowDate = null;// 流量包年套餐到期日期
	public static int mFlowDays = 0;// 流量包年套餐剩余天数
	public static int mCurRemFlowPercent = 0;// 当前剩余流量百分比
	public static int mLastRemFlowPercent = 0;// 上次查询到的剩余流量百分比
	public static int mLastReminddays = 0;// 上次查询到的剩余有效期

	public static Bundle getSimStatusInfo() {
		Bundle bundle = new Bundle();
		bundle.putInt("status", mSimStatus);
		bundle.putString("activedate", mActivedate);
		bundle.putString("duedate", mDuedate);
		bundle.putInt("remaindays", mRemaindays);
		bundle.putString("total", mTotalFlow);
		bundle.putString("used", mUsedFlow);
		bundle.putInt("remind", mRemind);
		bundle.putInt("flowtype", mFlowType);
		bundle.putString("flowdate", mFlowDate);
		bundle.putInt("flowdays", mFlowDays);
		return bundle;
	}

	@SuppressLint("NewApi")
	public static void setSimStatusInfo(Bundle bundle) {
		mSimStatus = bundle.getInt("status", -1);
		mActivedate = bundle.getString("activedate", "");
		mDuedate = bundle.getString("duedate", "");
		mRemaindays = bundle.getInt("remaindays", 0);
		mTotalFlow = bundle.getString("total", "");
		mUsedFlow = bundle.getString("used", "");
		mRemind = bundle.getInt("remind", 0);
		mFlowType = bundle.getInt("flowtype", 0);
		mFlowDate = bundle.getString("flowdate", "");
		mFlowDays = bundle.getInt("flowdays", 0);
	}

	// "http://test.careland.com.cn/kldjy/www/?mod=iov&ac=ksimstatus&cf=s"
	// "http://navione.careland.com.cn/?mod=iov&ac=ksimstatus&cf=s"
	public static String mM330PaySimUrlHead = null;// M330充值续费域名URL

	public static int eStatus_init = -2; // 初始状态
	public static int eStatus_failed = -1; // 获取失败
	public static int eStatus_success = 0; // 获取成功
	public static int eStatus_getting = 1; // 正在获取

	public static int sStatusGetSim = eStatus_init;

	public static int eErrorCOde_failed = -1;
	public static int eErrorCOde_none = 0;
	public static int eErrorCOde_invalidParams = 1;
	public static int eErrorCOde_neterror = 2;
	public static int eErrorCOde_invalidDownData = 3;

	public static int sErrorCode_GetSimStatus = eErrorCOde_none;

	public int doGetSimCardStatus() throws JSONException {
		sStatusGetSim = eStatus_getting;// 正在获取
		NetWorkRequest netRequest = KCloudPositionManager.getInstance()
				.getNetRequest();
		JSONObject jsonResult = null;
		String sign = null;
		String url = null;

		String testKey = "1a86fb49b070f26d7948d7931ed69233";
		String key = "1a86fb49b070f26d7948d7931ed69233";

		String pTestUrl = "http://test.careland.com.cn/kldjy/www/?mod=iov&ac=getcardstatus";
		String pRealUrl = "http://navione.careland.com.cn/?mod=iov&ac=getcardstatus";

		String paramUnSort = "apiver=1";
		String paramMd5UnSort = "apiver=1";

		String StrSerial = null;

		if (null == mIccidNum) {
			mIccidNum = NetWorkUtil.getICCIDNum(MainService.this);
		}

		String imsi = NetWorkUtil.getImsi(MainService.this);
		String sim = NetWorkUtil.getPhoneNum(MainService.this);

		if ((null == mIccidNum || mIccidNum.equals(""))
				&& (null == imsi || imsi.equals(""))
				&& (null == sim || sim.equals(""))) {
			CldLog.i(TAG, "mIccidNum,imsi,sim are all null");
			return eErrorCOde_invalidParams;
		}

		if (!isNetworkAvailable()) {
			CldLog.i(TAG, "network error");
			return eErrorCOde_neterror;
		}

		if (mIccidNum != null && !"".equals(mIccidNum)) {
			paramMd5UnSort = paramMd5UnSort + "&iccid=" + mIccidNum;
			paramUnSort = paramUnSort + "&iccid=" + mIccidNum;
		} else {
			paramUnSort = paramUnSort + "&iccid=" + "";
		}

		if (imsi != null && !"".equals(imsi)) {
			paramMd5UnSort = paramMd5UnSort + "&imsi=" + imsi;
			paramUnSort = paramUnSort + "&imsi=" + imsi;
		} else {
			paramUnSort = paramUnSort + "&imsi=" + "";
		}

		if (sim != null && !"".equals(sim)) {
			paramMd5UnSort = paramMd5UnSort + "&sim=" + sim;
			paramUnSort = paramUnSort + "&sim=" + sim;
		} else {
			paramUnSort = paramUnSort + "&sim=" + "";
		}

		String typeStr = FileUtils.readAssetsFile(getApplicationContext(), 0);
		if (typeStr != null) {
			typeStr = typeStr.replace("\r", "").replace("\n", "");
		}
		int type = Integer.valueOf(typeStr).intValue();
		String argValue = DeviceUtils.getSerialNum(this, type);
		if (null == argValue && "".equals(argValue)) {
			CldLog.i(TAG, "argValue is null");
			return eErrorCOde_invalidParams;
		}

		paramMd5UnSort = paramMd5UnSort + "&sn=" + argValue;
		paramUnSort = paramUnSort + "&sn=" + argValue;

		typeStr = FileUtils.readAssetsFile(getApplicationContext(), 1);
		if (typeStr != null) {
			typeStr = typeStr.replace("\r", "").replace("\n", "");
		}

		String temp[] = typeStr.split("-");

		paramMd5UnSort = paramMd5UnSort + "&ver=" + temp[0];
		paramUnSort = paramUnSort + "&ver=" + temp[0];

		String paramSortted = Md5Utils.sortParam(paramUnSort);
		String paramMd5Sortted = Md5Utils.sortParam(paramMd5UnSort);

		if (isTestServer) {
			sign = Md5Utils.MD5(paramMd5Sortted + testKey);
			url = pTestUrl + "&" + paramSortted + "&sign=" + sign;
		} else {
			sign = Md5Utils.MD5(paramMd5Sortted + key);
			url = pRealUrl + "&" + paramSortted + "&sign=" + sign;
		}

		CldLog.i(TAG, "doGetSimCardStatus url:" + url);

		jsonResult = netRequest.SendGetJson(url);

		if (jsonResult == null)
			return eErrorCOde_invalidDownData;

		CldLog.i(TAG, "doGetSimCardStatus jsonResult" + jsonResult);

		String errorCode = jsonResult.getString("errcode");
		if (errorCode != null && errorCode.equals("0")) {
			int status = jsonResult.getInt("status");
			String statusMsg = jsonResult.getString("statusmsg");
			String activeDate = jsonResult.getString("activedate");
			String duedate = jsonResult.getString("duedate");
			int remaindays = jsonResult.getInt("remaindays");
			String total = jsonResult.getString("total");
			String used = jsonResult.getString("used");
			int remind = jsonResult.getInt("remind");
			int flowtype = jsonResult.getInt("flowtype");
			String flowdate = jsonResult.getString("flowdate");
			int flowdays = jsonResult.getInt("flowdays");

			Bundle bundle = new Bundle();
			bundle.putInt("status", status);
			bundle.putString("activedate", activeDate);
			bundle.putString("duedate", duedate);
			bundle.putInt("remaindays", remaindays);
			bundle.putString("total", total);
			bundle.putString("used", used);
			bundle.putInt("remind", remind);
			bundle.putInt("flowtype", flowtype);
			bundle.putString("flowdate", flowdate);
			bundle.putInt("flowdays", flowdays);

			setSimStatusInfo(bundle);

			if (1 == flowtype) {
				float fUsedFlow = Float.valueOf(mUsedFlow);
				float fTotalFlow = Float.valueOf(mTotalFlow);
				float fSurplusFlow = fTotalFlow - fUsedFlow;
				// 记录当前剩余流量百分比
				mCurRemFlowPercent = (int) ((fSurplusFlow / fTotalFlow) * 100 + 0.5);
			}

			// M530获取套餐包数量
			doCheckSimCardPackages();

			CldLog.i(TAG, "doGetSimCardStatus return success");
			return eErrorCOde_none;
		}

		CldLog.i(TAG, "doGetSimCardStatus return failed");
		return eErrorCOde_failed;
	}

	// 是否收到点击特别提示界面“接受”按钮的广播
	public static boolean bIsReceiveShowDisclaimerBrcast = true;

	/*
	 * 开机是否显示特别提示界面
	 */
	public boolean getIsShowDisclaimer() {
		boolean bIsShowDisclaimer = true;
		SharedPreferences share = getApplicationContext().getSharedPreferences(
				"ShowDisclaimer", Context.MODE_WORLD_READABLE);
		if (null != share) {
			bIsShowDisclaimer = share.getBoolean("show_disclaimer", true);
		}

		CldLog.i(TAG, "end getIsShowDisclaimer bIsShowDisclaimer:"
				+ bIsShowDisclaimer);

		return bIsShowDisclaimer;
	}

	public void saveLastTipTimeToSharepref(long lLastTipTime,
			int iLastRemFlowPercent, int iLastReminddays) {
		Context context = getApplicationContext();
		SharedPreferences share = context.getSharedPreferences("LastTipTime",
				Context.MODE_WORLD_WRITEABLE);
		if (null != share) {
			CldLog.i(TAG, "saveLastTipTimeToSharepref lLastTipTime:"
					+ lLastTipTime);

			SharedPreferences.Editor editor = share.edit();
			editor.clear();
			editor.putLong("time", lLastTipTime);
			editor.putInt("RemFlowPercent", iLastRemFlowPercent);
			editor.putInt("LastReminddays", iLastReminddays);
			editor.commit();
		}
	}

	public Bundle getLastTipTimeFromSharepref() {
		Bundle bundle = new Bundle();
		Context context = getApplicationContext();
		SharedPreferences share = getApplicationContext().getSharedPreferences(
				"LastTipTime", Context.MODE_WORLD_READABLE);
		if (null != share) {
			long lLastTipTime = share.getLong("time", 0);
			int iLastRemFlowPercent = share.getInt("RemFlowPercent", 0);
			int iLastReminddays = share.getInt("LastReminddays", 0);

			bundle.putLong("time", lLastTipTime);
			bundle.putInt("RemFlowPercent", iLastRemFlowPercent);
			bundle.putInt("LastReminddays", iLastReminddays);
		}
		return bundle;
	}

	boolean IsSameDay(long lCurTime, long lLastTipTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
		String dateStringCurTime = sdf.format(lCurTime);
		String dateStringLastTipTime = sdf.format(lLastTipTime);

		CldLog.i(TAG, "dateStringCurTime:" + dateStringCurTime
				+ " dateStringLastTipTime" + dateStringLastTipTime);

		return dateStringCurTime.equals(dateStringLastTipTime);
	}

	public static String mIccidNum;
	public static String mImsiNum;
	public static String mPhoneNum;

	public Timer getSimCardStatusTimer = new Timer();
	public static boolean bIsStartTimer = false;

	public static int MAX_GET_TIMES = 10;
	public static int iCnt = 0;

	public void startGetSimCardStatusTimer() {
		if (null == getSimCardStatusTimer) {
			getSimCardStatusTimer = new Timer();
		}

		TimerTask newtimerTask = new TimerTask() {
			@Override
			public void run() {
				try {

					if (iCnt == MAX_GET_TIMES) {
						CldLog.i(TAG,
								"startGetSimCardStatusTimer,doGetSimCardStatus failed,iCnt = MAX_GET_TIMES");
						sErrorCode_GetSimStatus = eErrorCOde_failed;
						sStatusGetSim = eStatus_failed;
						stopGetSimCardStatusTimer();

						/**
						 * 暂时注释掉
						 */
						/*
						 * //发广播更新UI Intent intent = new
						 * Intent(ServiceRenewalActivity.ACTION_UPDATEUI);
						 * sendBroadcast(intent);
						 */
						return;
					}

					CldLog.i(TAG,
							"startGetSimCardStatusTimer--Start !!!  iCnt:"
									+ iCnt + " sStatusGetSim:" + sStatusGetSim);

					String serRenewalActivityName = "cld.navi.position.frame.ServiceRenewalActivity";

					if (eStatus_getting != sStatusGetSim) {
						iCnt++;

						if (null == mIccidNum || mIccidNum.equals("")) {
							mIccidNum = NetWorkUtil
									.getICCIDNum(MainService.this);
							String imsi = NetWorkUtil.getImsi(MainService.this);
							String sim = NetWorkUtil
									.getPhoneNum(MainService.this);
							if ((null == mIccidNum || mIccidNum.equals(""))
									&& (null == imsi || imsi.equals(""))
									&& (null == sim || sim.equals(""))) {

								sStatusGetSim = eStatus_failed;
								CldLog.i(TAG,
										"startGetSimCardStatusTimer--mIccidNum,imsi,sim are all null !!!");
								return;
							}
						}

						sErrorCode_GetSimStatus = doGetSimCardStatus();
						CldLog.i(TAG,
								"startGetSimCardStatusTimer,sErrorCode_GetSimStatus:"
										+ sErrorCode_GetSimStatus);

						if (eErrorCOde_none == sErrorCode_GetSimStatus) {
							sStatusGetSim = eStatus_success;
							stopGetSimCardStatusTimer();// 只要查询成功就先停掉timer
							CldLog.i(TAG,
									"startGetSimCardStatusTimer stopGetSimCardStatusTimer");

							if (bIsStartCheckTimer) {
								stopCheckIsLauncherInterfaceTimer();
							}

							if (false == isAppOnForeground("",
									serRenewalActivityName)) {
								if (isNeedAutoPopupSerRenewal()
										&& null != mMainHandler) {
									CldLog.i(TAG,
											"startGetSimCardStatusTimer sendEmptyMessage mStartSerRenewalActivity");

									// 如果套餐到了需要提醒的条件，需要主动弹出流量续费显示给用户
									mMainHandler
											.sendEmptyMessage(MainHandler.mStartSerRenewalActivity);
								}
							}

							/**
							 * 暂时注释掉
							 */
							/*
							 * //发广播更新UI Intent intent = new
							 * Intent(ServiceRenewalActivity.ACTION_UPDATEUI);
							 * sendBroadcast(intent);
							 */

							CldLog.i(TAG,
									"startGetSimCardStatusTimer success");
							return;
						} else if (eErrorCOde_neterror != sErrorCode_GetSimStatus
								&& eErrorCOde_invalidDownData != sErrorCode_GetSimStatus) {
							sStatusGetSim = eStatus_failed;
							stopGetSimCardStatusTimer();// 非网络异常以及服务器返回数据为空的情况下就不再查询了,防止频繁访问服务器造成压力
							CldLog.i(TAG,
									"startGetSimCardStatusTimer, sErrorCode_GetSimStatus:"
											+ sErrorCode_GetSimStatus);
							return;
						}
						sStatusGetSim = eStatus_failed;
						CldLog.i(TAG,
								"startGetSimCardStatusTimer,doGetSimCardStatus failed,iCnt:"
										+ iCnt);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
		getSimCardStatusTimer.schedule(newtimerTask, 10000, 10000);
		bIsStartTimer = true;
	}

	public void stopGetSimCardStatusTimer() {
		if (null != getSimCardStatusTimer) {
			getSimCardStatusTimer.cancel();
		}
		getSimCardStatusTimer = null;
		bIsStartTimer = false;
		iCnt = 0;
		sErrorCode_GetSimStatus = eErrorCOde_none;
		CldLog.i(TAG, "stopGetSimCardStatusTimer !!!");
	}

	public static String ACTION_GETSIMSTATUS = "cld.navi.position.frame.getSimStatus";
	BroadcastReceiver mUpdateUI = new BroadcastReceiver() {

		@SuppressLint("NewApi")
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if (arg1.getAction().equals(ACTION_GETSIMSTATUS)) {
				boolean IsToStartTimer = arg1.getBooleanExtra("IsToStartTimer",
						true);
				CldLog.i(TAG, "mUpdateUI ACTION:" + ACTION_GETSIMSTATUS
						+ " bIsStartTimer:" + bIsStartTimer
						+ " IsToStartTimer:" + IsToStartTimer);
				if (false == IsToStartTimer) {
					stopGetSimCardStatusTimer();
					return;
				}

				if (false == bIsStartTimer) {
					// 收到重新的广播后，如果当前没有在获取中，就重新获取
					startGetSimCardStatusTimer();
					return;
				}
			}
		}

	};

	private void RegisterUpdateUIReceiver() {
		if (null != mUpdateUI) {
			IntentFilter tmp = new IntentFilter(ACTION_GETSIMSTATUS);
			registerReceiver(mUpdateUI, tmp);
		}
	}

	private void UnregisterUpdateUIReceiver() {
		if (null != mUpdateUI) {
			unregisterReceiver(mUpdateUI);
		}
	}

	/*
	 * 如果需要主动弹出流量续费界面时，启动定时器检测是否是launcher界面(只在launcher界面主动弹出)
	 */
	public Timer checkIsLauncherInterfaceTimer = new Timer();
	public static boolean bIsStartCheckTimer = false;
	public static boolean bIsAutoShowSerRenewalActivity = false;

	public void startCheckIsLauncherInterfaceTimer() {
		if (null == checkIsLauncherInterfaceTimer) {
			checkIsLauncherInterfaceTimer = new Timer();
		}

		TimerTask newtimerTask = new TimerTask() {
			@Override
			public void run() {
				CldLog.i(TAG, "startCheckIsLauncherInterfaceTimer running");

				String launcherPackageName = "com.chenli.launcher";

				if (isAppOnForeground(launcherPackageName, "")) {
					CldLog.i(TAG,
							"startCheckIsLauncherInterfaceTimer startShowServiceRenewal bIsReceiveShowDisclaimerBrcast:"
									+ MainService.bIsReceiveShowDisclaimerBrcast);

					if (true == MainService.bIsReceiveShowDisclaimerBrcast
							|| (false == MainService.bIsReceiveShowDisclaimerBrcast && false == getIsShowDisclaimer())) {
						long lLastTipTime = System.currentTimeMillis();
						// 更新上次弹出提示框的时间
						saveLastTipTimeToSharepref(lLastTipTime,
								mLastRemFlowPercent, mLastReminddays);
						bIsAutoShowSerRenewalActivity = true;
						// 如果套餐到了需要提醒的条件，需要主动弹出流量续费显示给用户
						startShowServiceRenewal();
						stopCheckIsLauncherInterfaceTimer();
					}
				}
			}
		};
		checkIsLauncherInterfaceTimer.schedule(newtimerTask, 1000, 3000);
		bIsStartCheckTimer = true;
	}

	public void stopCheckIsLauncherInterfaceTimer() {
		checkIsLauncherInterfaceTimer.cancel();
		checkIsLauncherInterfaceTimer = null;
		bIsStartCheckTimer = false;
	}

	public static boolean bIsRunning = true;

	public void doFirstActivateRegister_httppost() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (bIsRunning) {
					try {
						if (true == doFirstActivateRegister()) {
							CldLog.i(TAG,
									"doFirstActivateRegister_httppost stop running");
							bIsRunning = false;
						} else {
							CldLog.i(TAG,
									"doFirstActivateRegister_httppost sleep(30000)");
							Thread.sleep(30000);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		thread.start();
	}

	public void onCreate() {
		super.onCreate();
		CldLog.i(TAG, " MainService onCreate");
		
		// 设置前台进程
		@SuppressWarnings("deprecation")
		Notification notification = new Notification(
				R.drawable.ic_xiaokaihulian, "系统服务", System.currentTimeMillis());
		startForeground(0, notification);
		if (DeviceUtils.isUseBugly) {
			buglyInit();
		}
		isLogToFile = KCloudPositionManager.getInstance().getIsWriteLog();
		isTestServer = KCloudPositionManager.getInstance().getIsTestServer();
		bindNaviService();  // 绑定导航服务
		weMeOnInit();       // 功能初始化，整个服务的初始化。
		startMonitorServ(); // 启动监控服务

		/********** 小凯互联 **********/
		RegisterPostWeixinReceiver();
		/* end*********小凯互联********* */

		mIccidNum = NetWorkUtil.getICCIDNum(MainService.this);
		mImsiNum = NetWorkUtil.getImsi(MainService.this);
		mPhoneNum = NetWorkUtil.getPhoneNum(MainService.this);

		// ********* bindNaviServiceM530 相关 *********
		// startGetSimCardStatusTimer();
		// RegisterUpdateUIReceiver();
		//
		// checkSimCardExist();
		// registerSimCardTypeReceiver();
		//
		// //获取上次的套餐
		// checkLastSaveSimCardPackages();
	};

	public void weMeOnInit() {
		mTimer = new Timer();// 先搞个定时器再说
		mDetecMoniSevTask = new DetecMoniSevTask();
		if (mTimer != null && mDetecMoniSevTask != null)
			mTimer.schedule(mDetecMoniSevTask, 3000, 3000);

		mMainThread = new MainThread();
		mMainThread.start();

		// 注册接受导航发过来的广播
		NaviServiceReceiver mNaviServiceReceiver = new NaviServiceReceiver();
		IntentFilter mFilter = new IntentFilter(NaviServiceReceiver.NAVI_ACTION);

		mFilter.addAction(NaviServiceReceiver.ACTION_STOPSELF);// M330添加结束服务广播

		registerReceiver(mNaviServiceReceiver, mFilter);
		// registerReceiver(receiver, filter, broadcastPermission, scheduler)
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		RegisterGetDuidReceiver(this);
		// return super.onStartCommand(intent, START_STICKY, startId);//挂掉了需重启
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		UnRegisterGetDuidReceiver(this);
		unRegisterSimCardTypeReceiver();

		/********** 小凯互联 **********/
		UnregisterPostWeixinReceiver();
		/* end*********小凯互联********* */

		if (MainService.bIsRunning) {
			MainService.bIsRunning = false;// 卡登记线程停掉
		}

		// 把耗时任务个线程干掉
		if (mMainThread != null && mMainThread.isAlive()) {
			if (mMainHandler != null)
				mMainHandler.sendEmptyMessage(mMainHandler.mQuitMsg);

			try {
				mMainThread.join(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}

		// 把那GPS收集线程干掉
		if (mCollectGPSThread != null && mCollectGPSThread.isAlive()) {

			mCollectGPSThread.sendEmptyMessage(CollectGPSThread.MSG_GPS_QUIT);

			try {
				mCollectGPSThread.join(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}

		// 把上报线程干掉
		if (mUpPositionThread != null && mUpPositionThread.isAlive()) {
			mReportDataQueue.notifyQueue();
			mUpPositionThread
					.sendEmptyMessage(ReportPositionThread.MSG_UPPOSITON_QUIT);

			try {
				mUpPositionThread.join(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}

		// 注销GPS
		if (mGpsReport != null)
			mGpsReport.unRegisterGPS();
		mTimer.cancel();
		if (mBroadcastReceiver != null)
			unregisterReceiver(mBroadcastReceiver);
		mDetecMoniSevTask.cancel();

		mTimer = null;
		mDetecMoniSevTask = null;
		mCollectGPSThread = null;
		mUpPositionThread = null;
		mGpsReport = null;
		mReportDataQueue = null;
		mMainThread = null;
		mMainHandler = null;
		mHandler = null;

		System.exit(0);
	}

	/*************************** 新代码 ************************************************/

	/*
	 * 耗时任务线程的Handler
	 */
	public class MainHandler extends Handler {

		final static int mQuitMsg = 100;
		final static int mWemeInitMsg = mQuitMsg + 1; // 语境初始化
		final static int mGetConfigMsg = mWemeInitMsg + 1; // 获取服务端参数的配置参数
		final static int mGetUrlHead = mGetConfigMsg + 1; // 上报位置的URL头和设备注册URL头
		final static int mGetDuid = mGetUrlHead + 1; // 获取Duid
		final static int mCardBelong = mGetDuid + 1; // 判断流量卡归属
		final static int mApnSetMsg = mCardBelong + 1; // apn定时查询设置
		final static int mGetWeixinQr = mCardBelong + 1; // 获取微信关注二维码
		final static int mStartSerRenewalActivity = mGetWeixinQr + 1;// 启动流量续费Activity

		final static int mCheckSimCardType = mStartSerRenewalActivity + 1; // 获取到卡的状态类型

		@SuppressLint("NewApi")
		@Override
		public void handleMessage(Message msg) {
			int message = msg.what;

			/*
			 * 线程停止
			 */
			if (message == mQuitMsg)
				Looper.myLooper().quit();
			if (message == mGetConfigMsg) { // 获取配置
				// apnInit();//apn初始化
				// mApnTask = new ApnTask();
				// if(mTimer!=null && mApnTask!=null)
				// mTimer.schedule(mApnTask, QUERY_APN_DELAY,
				// QUERY_APN_INTERVAL);

				sendNetworkAnomalyMsg();// 小凯互联判断网络异常就发送通知消息

				boolean isSuccess = getServiceCfg();
				if (isSuccess && mMainHandler != null)
					mMainHandler.sendEmptyMessage(mGetUrlHead);// 发送获取上报位置URL头的消息

			} else if (message == mGetUrlHead) {// 获取上报位置URL头以及获取秘钥（deviceID）的url

				sendNetworkAnomalyMsg();// 小凯互联判断网络异常就发送通知消息
				getM330PaySimUrlHead();// M330获取充值续费URL

				boolean isSuccess = getUpAndDevUrlHead();
				if (isSuccess && mMainHandler != null)
					mMainHandler.sendEmptyMessage(mGetDuid);// 发送获取设备ID消息

			} else if (message == mGetDuid) {// 获取设备ID的消息

				sendNetworkAnomalyMsg();// 小凯互联判断网络异常就发送通知消息

				boolean isSuccess = getKcountKey();// 获取账户系统秘钥
				if (isSuccess) {

					sendNetworkAnomalyMsg();// 小凯互联判断网络异常就发送通知消息

					isSuccess = getServiceDuid();

					// // ************ M330 自动激活 ************
					// if (isSuccess)
					// {
					// //开机后卡登记
					// doFirstActivateRegister_httppost();
					// }

					// ************ M530需要手动登记卡才能激活，故去掉自动激活 ************
					// 获取上次保存的SIM类型
					// mLastSimType = checkLastSaveSimCardType();
					// if(mLastSimType == 0){
					// //无上次状态或者上次状态是未知
					// startCheckSimCardType();
					// }
					// else if(mLastSimType == -2){
					// //上次状态是已知为未激活的服务卡
					// startRegisterActivity();
					// }

					if (isSuccess && mMainHandler != null)
						mMainHandler.sendEmptyMessage(mGetWeixinQr);// 获取微信关注二维码

					if (isSuccess && isCldNaviOpen == 1 && mMainHandler != null) {
						mHandler.sendEmptyMessage(mStartCollectMsg);
					}
				}
			} else if (message == mGetWeixinQr) { // 获取微信关注二维码

				sendNetworkAnomalyMsg();// 小凯互联判断网络异常就发送通知消息

				getWeixinQr();
			} else if (message == mStartSerRenewalActivity) {
				CldLog.i(TAG,
						"MainHandler received msg: mStartSerRenewalActivity");

				// 如果套餐到了需要提醒的条件，需要主动弹出流量续费显示给用户
				startCheckIsLauncherInterfaceTimer();
			} else if (message == mCheckSimCardType) {
				MainService.SimCardTypeInfo simCardTypeInfo = MainService.mSimCardTypeInfo;

				CldLog.i(TAG, "simCardTypeInfo.pkErrCode:"
						+ simCardTypeInfo.pkErrCode);

				if (simCardTypeInfo.pkErrCode == 1
						|| simCardTypeInfo.pkErrCode == -2) {
					// 未激活的服务卡
					SharedPreferences simCardType = getSharedPreferences(
							NAME_SHAREFILE, MODE_PRIVATE);
					Editor editor = simCardType.edit();

					if (mIccidNum != null && !mIccidNum.equals("")) {
						editor.putString("iccid", mIccidNum);
					}
					if (mImsiNum != null && !mImsiNum.equals("")) {
						editor.putString("imsi", mImsiNum);
					}
					if (mPhoneNum != null && !mPhoneNum.equals("")) {
						editor.putString("sim", mPhoneNum);
					}

					editor.putInt(KEY_ERRCOED, simCardTypeInfo.pkErrCode);
					editor.putInt(KEY_PKID, simCardTypeInfo.pkId);
					editor.putString(KEY_PKNAME, simCardTypeInfo.pkName);
					editor.putString(KEY_PKDESC, simCardTypeInfo.pkDesc);
					editor.apply();

					if (simCardTypeInfo.pkErrCode == -2) {
						// 启动跳转到激活界面
						startRegisterActivity();
					}
				} else {
					// 其他结果暂不做处理
				}
			}
		}
	}

	/*
	 * 耗时任务线程，处理语境初始化、服务器获取配置参数、启动日志线程、获取设备ID
	 */
	public class MainThread extends Thread {

		@Override
		public void run() {
			Looper.prepare();
			mMainHandler = new MainHandler();
			mMainHandler.sendEmptyMessage(MainHandler.mGetConfigMsg);
			Looper.loop();
		}

	}

	/*
	 * 收集日志
	 */
	public void buglyInit() {
		String channel = (String) VersionUtils.getMetaData(
				getApplicationContext(), "Channel");
		String revision = (String) VersionUtils.getMetaData(
				getApplicationContext(), "Revision");
		String appVersion = VersionUtils
				.getAppPackageName(getApplicationContext()) + "+" + revision;
		// App的策略Bean
		UserStrategy strategy = new UserStrategy(getApplicationContext());
		// 设置渠道
		strategy.setAppChannel(channel);
		// App的版??
		strategy.setAppVersion(appVersion);
		// 自定义策略生??
		CrashReport.initCrashReport(getApplicationContext(), "900030969", true,
				strategy); // 900002986 使用M550自己的bugly, AppId
		// 设置用户标示用来区分唯一用户(机器)，必须在initCrashReport之后调用 //
		CrashReport.setUserId(VersionUtils.getUID(getApplicationContext()));
	}

	/*
	 * 绑定导航服务，用于获取kuid\session\ruid\duid
	 */

	public void bindNaviService() {
		Intent mIntent = new Intent(NAVI_SERVICE_ACTION);
		mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		bindService(mIntent, naviService, BIND_AUTO_CREATE);

	}

	private ServiceConnection naviService = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {

			if (service != null) {
				mNaviParamGet = IGetParamFromNavi.Stub.asInterface(service);
				FileUtils.logOut("绑定导航服务成功", isLogToFile);
			} else {
				FileUtils.logOut("绑定导航服务失败", isLogToFile);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

			mNaviParamGet = null;
			FileUtils.logOut("解绑导航服务成功", isLogToFile);
		}

	};

	/*
	 * 用来判断是否还是用存储在本地的duid
	 */
	public boolean isUseLocalDuid(int argtype, String argvalue, int duid,
			boolean isTestSever) {
		int mArgType = SharePrefUtils.getShareInt(SharePrefUtils.DUID_ARG_TYPE,
				-1);
		String mArgValue = SharePrefUtils.getShareString(
				SharePrefUtils.DUID_ARG, "");
		boolean mIsTestSever = SharePrefUtils.getShareBoolean(
				SharePrefUtils.IS_TEST_SEVER, false);
		boolean mIsDuidFromNavi = SharePrefUtils.getShareBoolean(
				SharePrefUtils.IS_DUID_FROM_NAVI, false);

		if ((argtype == mArgType && argvalue.equals(mArgValue) && isTestSever == mIsTestSever)
				|| mIsDuidFromNavi == true && duid != 0)
			return true;
		return false;
	}

	/*
	 * 服务器取失败时休眠
	 */
	public void httpSleep(long t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
	}

	/*
	 * 导航与服务交互广播
	 */
	private class NaviServiceReceiver extends BroadcastReceiver {
		static final String NAVI_ACTION = "CLD.NAVI.TO.SERVICE";// 导航到服务的广播
		static final String SERVICE_ACTION = "CLD.SERVICE.TO.NAVI";// 服务到导航的广播
		static final String ACTION_STOPSELF = "CLD.NAVI.ACTION_STOPSELF";// M330

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action != null && action.equals(NAVI_ACTION)) {
				Intent mIntent = new Intent(SERVICE_ACTION);
				sendBroadcast(mIntent);
			}

			if (action != null && action.equals(ACTION_STOPSELF)) {
				CldLog.i(TAG, "NaviServiceReceiver action:"
						+ ACTION_STOPSELF);
				MainService.this.stopSelf();
			}
		}

	}

	public int getDuid() {

		int duid = -1;
		if (mNaviParamGet != null) {
			try {
				duid = mNaviParamGet.getDuidFromNavi();
			} catch (RemoteException e) {
				e.printStackTrace();
				// bindNaviService();
				return duid;
			}
		}
		return duid;
	}

	@Override
	public int getKuid() {

		int kuid = -1;
		if (mNaviParamGet != null) {
			try {
				kuid = mNaviParamGet.getKuidFromNavi();
			} catch (RemoteException e) {
				e.printStackTrace();
				// bindNaviService();
				return kuid;
			}
		}
		return kuid;
	}

	@Override
	public String getSession() {

		String session = "";
		if (mNaviParamGet != null) {
			try {
				session = mNaviParamGet.getSessionFromNavi();
			} catch (RemoteException e) {
				e.printStackTrace();
				// bindNaviService();
				return session;
			}
		}
		return session;
	}

	@Override
	public GpsDataParam getRuidXY() {
		int ruid = -1;

		GpsDataParam value = null;
		if (mNaviParamGet != null) {
			try {
				value = mNaviParamGet.getRuidXYFromNavi();
			} catch (RemoteException e) {
				e.printStackTrace();
				return value;
			}
		}
		return value;
	}

	/*
	 * 服务自启动，系统每秒在发送tick广播
	 */
	public void registerRestart() {
		IntentFilter mIntentFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
		mBroadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
					boolean isExist = getServiceExist();
					FileUtils.logOut(
							"mainService-ACTION_TIME_TICK-isMonitorExist="
									+ isExist, isLogToFile);
					if (!isExist) {
						startMonitorServ();
					}
				}
			}
		};
		registerReceiver(mBroadcastReceiver, mIntentFilter);
	}

	/*
	 * 判断服务是否在运行
	 */
	public static boolean isServiceRunning(Context context, String className) {

		boolean isRunning = false;

		ActivityManager activityManager =

		(ActivityManager) context.getSystemService(ACTIVITY_SERVICE);

		List<ActivityManager.RunningServiceInfo> serviceList

		= activityManager.getRunningServices(Integer.MAX_VALUE);

		if (!(serviceList.size() > 0)) {

			return false;

		}

		for (int i = 0; i < serviceList.size(); i++) {

			if (serviceList.get(i).service.getClassName().equals(className) == true) {
				isRunning = true;
				break;
			}
		}
		return isRunning;
	}

	public boolean getServiceExist() {
		boolean isExist = false;
		isExist = isServiceRunning(this, MonitorService.class.getName());
		return isExist;
	}

	public void startMonitorServ() {
		Intent mIntent = new Intent(MainService.this, MonitorService.class);
		// mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.startService(mIntent);
	}

	/*
	 * 服务端取配置包括 记录频率、上报频率，语境开关、凯立德开关
	 */
	public boolean getServiceCfg() {
		String sign = null;
		String url = null;
		boolean isSuccess = false;
		NetWorkRequest netRequest = KCloudPositionManager.getInstance()
				.getNetRequest();
		// 先取配置参数（record_rate,up_rate,open)
		if (isTestServer)// 测试服务
		{
			sign = Md5Utils
					.MD5("classtypes=2002001000,0&rscharset=1&rsformat=1"
							+ testRateHeadKey);
			url = TEST_CFG_URL
					+ "?classtypes=2002001000,0&rscharset=1&rsformat=1&sign="
					+ sign;
		} else {
			sign = Md5Utils
					.MD5("classtypes=2002001000,0&rscharset=1&rsformat=1"
							+ rateHeadKey);
			url = CFG_URL
					+ "?classtypes=2002001000,0&rscharset=1&rsformat=1&sign="
					+ sign;
		}
		while (!isSuccess) {
			
			JSONObject jobject = null;
			if (netRequest != null) {
				jobject = netRequest.SendGetJson(url);
				if (jobject != null) {
					FileUtils.logOut("mGetRateMsg->" + jobject, isLogToFile);
				}
			}

			try {
				if (jobject != null) {
					String errorCode = jobject.getString("errcode");
					if (errorCode != null && errorCode.equals("0"))// 成功返回数据
					{
						JSONObject jsonItem = jobject.getJSONArray("item")
								.getJSONObject(0);
						JSONObject jsonCfg = jsonItem
								.getJSONObject("configitem");
						String recordRate = jsonCfg.getString("record_rate");
						String upRate = jsonCfg.getString("up_rate");
						String mirrTalkOpen = jsonCfg.getJSONObject(
								"context_up").getString("open");
						String cldNaviOpen = jsonCfg.getJSONObject("svc_up")
								.getString("open");

						if (recordRate != null && !"".equals(recordRate)
								&& upRate != null && !"".equals(upRate)
								&& mirrTalkOpen != null
								&& !"".equals(mirrTalkOpen)
								&& cldNaviOpen != null
								&& !"".equals(cldNaviOpen)) {
							mRecordRate = Integer.valueOf(recordRate)
									.intValue();
							mUpRate = Integer.valueOf(upRate).intValue();
							isMirrTalkOpen = Integer.valueOf(mirrTalkOpen)
									.intValue();
							isCldNaviOpen = Integer.valueOf(cldNaviOpen)
									.intValue();
							// 成功了就下一步获取URL头
							isSuccess = true;
						}

					}
				} else {
					httpSleep(3000);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				CldLog.w(TAG, "error==" + e);
				return false;
			}
		}
		return true;
	}

	/*
	 * 获取上报位置的域名头及注册duid的域名头
	 */
	public boolean getUpAndDevUrlHead() {
		String sign = null;
		String url = null;
		boolean isSuccess = false;
		NetWorkRequest netRequest = KCloudPositionManager.getInstance()
				.getNetRequest();
		if (isTestServer)// 测试服务器
		{
			sign = Md5Utils
					.MD5("classtypes=1001001000,0&rscharset=1&rsformat=1"
							+ testRateHeadKey);
			url = TEST_CFG_URL
					+ "?classtypes=1001001000,0&rscharset=1&rsformat=1&sign="
					+ sign;
		} else {
			sign = Md5Utils
					.MD5("classtypes=1001001000,0&rscharset=1&rsformat=1"
							+ rateHeadKey);
			url = CFG_URL
					+ "?classtypes=1001001000,0&rscharset=1&rsformat=1&sign="
					+ sign;
		}

		while (!isSuccess) {
			JSONObject jobject = netRequest.SendGetJson(url);
			if (jobject != null) {
				FileUtils.logOut("mGetUPUrlHeadMsg->" + jobject, isLogToFile);
			}

			try {
				if (jobject != null) {
					String errorCode = jobject.getString("errcode");
					if (errorCode != null && errorCode.equals("0"))// 成功返回数据
					{
						JSONObject jsonItem = jobject.getJSONArray("item")
								.getJSONObject(0);

						String svrpos = jsonItem.getJSONObject("configitem")
								.getString("svr_pos");
						String svrKaccount = jsonItem.getJSONObject(
								"configitem").getString("svr_kaccount");

						if (svrpos != null && !"".equals(svrpos)
								&& svrKaccount != null
								&& !"".equals(svrKaccount)) {
							CldLog.w("js", "mUpPosionHead-pre->" + mUpPosionHead);
							mUpPosionHead = svrpos;
							mKaccountGetCodeUrl = svrKaccount;
							CldLog.w("js", "last-mUpPosionHead->" + mUpPosionHead
									+ ",mKaccountGetCodeUrl->"
									+ mKaccountGetCodeUrl);
							isSuccess = true;

						}
					}
				} else {
					httpSleep(3000);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				CldLog.w(TAG, "error==" + e);
				return false;
			}
		}
		return true;
	}

	/*
	 * M330获取充值续费URL
	 */
	public boolean getM330PaySimUrlHead() {
		int iReqTryTimes = 0;
		int MAX_REQ_COUNT = 10;
		String sign = null;
		String url = null;
		boolean isSuccess = false;
		NetWorkRequest netRequest = KCloudPositionManager.getInstance()
				.getNetRequest();

		if (isTestServer) // 测试服务器
		{
			sign = Md5Utils
					.MD5("classtypes=1001003000,0&rscharset=1&rsformat=1"
							+ testRateHeadKey);
			url = TEST_CFG_URL
					+ "?classtypes=1001003000,0&rscharset=1&rsformat=1&sign="
					+ sign;
		} else {
			sign = Md5Utils
					.MD5("classtypes=1001003000,0&rscharset=1&rsformat=1"
							+ rateHeadKey);
			url = CFG_URL
					+ "?classtypes=1001003000,0&rscharset=1&rsformat=1&sign="
					+ sign;
		}

		while (!isSuccess) {
			JSONObject jobject = netRequest.SendGetJson(url);
			try {
				if (jobject != null) {
					CldLog.i(TAG, "jobject:" + jobject);

					String errorCode = jobject.getString("errcode");
					if (errorCode != null && errorCode.equals("0"))// 成功返回数据
					{
						JSONObject jsonItem = jobject.getJSONArray("item")
								.getJSONObject(0);

						String svrPaySimUrl = jsonItem.getJSONObject(
								"configitem").getString("url_m330_pay");
						if (null != svrPaySimUrl && !"".equals(svrPaySimUrl)) {
							mM330PaySimUrlHead = svrPaySimUrl;
							isSuccess = true;
							CldLog.i(TAG, "svrPaySimUrl:" + svrPaySimUrl);
							return true;
						}
					}

					CldLog.i(TAG, "getM330PaySimUrlHead errorCode:"
							+ errorCode);
				} else {
					httpSleep(3000);
				}

				if (iReqTryTimes < MAX_REQ_COUNT) {
					iReqTryTimes++;
					continue;
				}

				return false;

			} catch (JSONException e) {
				e.printStackTrace();
				CldLog.w(TAG, "error==" + e);
				return false;
			}
		}
		return true;
	}

	/*
	 * 充值卡首次登记-服务激活
	 */
	public boolean doFirstActivateRegister() throws JSONException {
		NetWorkRequest netRequest = KCloudPositionManager.getInstance()
				.getNetRequest();
		JSONObject jsonResult = null;
		String sign = null;
		String url = null;

		String testKey = "1a86fb49b070f26d7948d7931ed69233";
		String key = "1a86fb49b070f26d7948d7931ed69233";

		String pTestUrl = "http://test.careland.com.cn/kldjy/www/?mod=iov&ac=registercard";
		String pRealUrl = "http://navione.careland.com.cn/?mod=iov&ac=registercard";

		String paramUnSort = "apiver=1";
		String paramMd5UnSort = "apiver=1";

		String StrSerial = null;

		if (null == mIccidNum) {
			mIccidNum = NetWorkUtil.getICCIDNum(MainService.this);
		}

		String imsi = NetWorkUtil.getImsi(MainService.this);
		String sim = NetWorkUtil.getPhoneNum(MainService.this);

		if ((null == mIccidNum || mIccidNum.equals(""))
				&& (null == imsi || imsi.equals(""))
				&& (null == sim || sim.equals(""))) {
			CldLog.i(TAG,
					"doFirstActivateRegister mIccidNum,imsi,sim are all null");
			return true;
		}

		if (!isNetworkAvailable()) {
			CldLog.i(TAG, "doFirstActivateRegister network error");
			return false;
		}

		if (mIccidNum != null && !"".equals(mIccidNum)) {
			paramMd5UnSort = paramMd5UnSort + "&iccid=" + mIccidNum;
			paramUnSort = paramUnSort + "&iccid=" + mIccidNum;
		} else {
			paramUnSort = paramUnSort + "&iccid=" + "";
		}

		if (imsi != null && !"".equals(imsi)) {
			paramMd5UnSort = paramMd5UnSort + "&imsi=" + imsi;
			paramUnSort = paramUnSort + "&imsi=" + imsi;
		} else {
			paramUnSort = paramUnSort + "&imsi=" + "";
		}

		if (sim != null && !"".equals(sim)) {
			paramMd5UnSort = paramMd5UnSort + "&sim=" + sim;
			paramUnSort = paramUnSort + "&sim=" + sim;
		} else {
			paramUnSort = paramUnSort + "&sim=" + "";
		}

		String typeStr = FileUtils.readAssetsFile(getApplicationContext(), 0);
		if (typeStr != null) {
			typeStr = typeStr.replace("\r", "").replace("\n", "");
		}
		int type = Integer.valueOf(typeStr).intValue();
		String argValue = DeviceUtils.getSerialNum(this, type);
		if (null == argValue && "".equals(argValue)) {
			CldLog.i(TAG, "doFirstActivateRegister argValue is null");
			return true;
		}

		paramMd5UnSort = paramMd5UnSort + "&sn=" + argValue;
		paramUnSort = paramUnSort + "&sn=" + argValue;

		typeStr = FileUtils.readAssetsFile(getApplicationContext(), 1);
		if (typeStr != null) {
			typeStr = typeStr.replace("\r", "").replace("\n", "");
		}

		String temp[] = typeStr.split("-");

		paramMd5UnSort = paramMd5UnSort + "&ver=" + temp[0];
		paramUnSort = paramUnSort + "&ver=" + temp[0];

		paramMd5UnSort = paramMd5UnSort + "&duid=" + mDeviceId;
		paramUnSort = paramUnSort + "&duid=" + mDeviceId;

		paramMd5UnSort += "&dcode=" + mDcode + "&pcode=" + mPcode + "&custid="
				+ mCustid;
		paramUnSort += "&dcode=" + mDcode + "&pcode=" + mPcode + "&custid="
				+ mCustid;

		String paramSortted = Md5Utils.sortParam(paramUnSort);
		String paramMd5Sortted = Md5Utils.sortParam(paramMd5UnSort);

		if (isTestServer) {
			sign = Md5Utils.MD5(paramMd5Sortted + testKey);
			url = pTestUrl + "&" + paramSortted + "&sign=" + sign;
		} else {
			sign = Md5Utils.MD5(paramMd5Sortted + key);
			url = pRealUrl + "&" + paramSortted + "&sign=" + sign;
		}

		CldLog.i(TAG, "doFirstActivateRegister url:" + url + " sign:" + sign);

		jsonResult = netRequest.SendGetJson(url);
		if (jsonResult != null) {
			CldLog.i(TAG, "doFirstActivateRegister jsonResult:" + jsonResult);
			int errorCode = jsonResult.getInt("errcode");
			CldLog.i(TAG, "doFirstActivateRegister errorCode:" + errorCode);
			/*
			 * 1 正常 （包括绑定成功或已经绑定） 0 未知错误、网络原因等其他异常 -1 ICCID不存在 -2 SN不存在
			 * -3卡被锁定（卡被插在非法设备上） -4 ICCID与SN绑定关系与服务端不一致 -5卡流量异常（超标）
			 */
			CldLog.i(TAG, "doFirstActivateRegister success");

			// M530，绑定成功，保存状态
			if (errorCode == 1) {
				saveSimCardRegisterState();
			}
			//

			return true;
		}

		CldLog.i(TAG, "doFirstActivateRegister jsonResult is null");
		return false;
	}

	/*
	 * 获取账户系统秘钥
	 */
	public boolean getKcountKey() {
		NetWorkRequest netRequest = KCloudPositionManager.getInstance()
				.getNetRequest();
		boolean isSuccess = false;
		String sign = null;
		String url = null;
		long cid = 1010;
		String strCid = FileUtils.readAssetsFile(this, 4);
		if (strCid != null)
			cid = Long.valueOf(strCid).longValue();
		if (isTestServer)// 测试服务器
		{
			sign = Md5Utils.MD5("apiver=1&appid=24&cid=" + cid
					+ "&prover=1.0&rscharset=1&rsformat=1&umsaver=2"
					+ testGetKodekey);
			url = mKaccountGetCodeUrl
					+ "kaccount_get_code.php?umsaver=2&rscharset=1&rsformat=1&apiver=1&appid=24&cid="
					+ cid + "&prover=1.0&sign=" + sign;
		} else {
			sign = Md5Utils.MD5("apiver=1&appid=24&cid=" + cid
					+ "&prover=1.0&rscharset=1&rsformat=1&umsaver=2"
					+ getKodekey);
			url = mKaccountGetCodeUrl
					+ "kaccount_get_code.php?umsaver=2&rscharset=1&rsformat=1&apiver=1&appid=24&cid="
					+ cid + "&prover=1.0&sign=" + sign;
		}
		// 获取"获取device ID"的秘钥
		while (!isSuccess) {
			JSONObject jobject = netRequest.SendGetJson(url);
			try {
				if (jobject != null) {
					String code = jobject.getString("code");
					getDeviceIdKey = Md5Utils.decodeKey(code);
					isSuccess = true;
					return true;
				} else {
					httpSleep(3000);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

	/*
	 * 服务获取duid
	 */
	public boolean getServiceDuid() {
		NetWorkRequest netRequest = KCloudPositionManager.getInstance()
				.getNetRequest();
		int deviceId = SharePrefUtils.getShareInt(SharePrefUtils.DUID, 0);
		String typeStr = FileUtils.readAssetsFile(getApplicationContext(), 0);
		if (typeStr != null) {
			typeStr = typeStr.replace("\r", "").replace("\n", "");
		}
		int type = Integer.valueOf(typeStr).intValue();
		String argValue = DeviceUtils.getSerialNum(this, type);
		CldLog.w("js", "isUseLocalDuid-pre=" + type + "," + argValue + ","
				+ deviceId + ",isTestSever=" + isTestServer);
		//mDeviceId = getDuid();// 从导航远程获取duid
		mDeviceId = (int) CldKAccountAPI.getInstance().getDuid();
		CldLog.d("mDeviceId", "mDeviceId====" + mDeviceId);
		
		if (mDeviceId != -1)// 从导航获取了就直接返回了
		{
			FileUtils.logOut("mDeviceId====" + mDeviceId, isLogToFile);
			SharePrefUtils.putShareBoolean(SharePrefUtils.IS_DUID_FROM_NAVI,
					true);
			SharePrefUtils.putShareInt(SharePrefUtils.DUID, mDeviceId);
			return true;
		}

		if (!isUseLocalDuid(type, argValue, deviceId, isTestServer))// 本地没有符合条件的存储设备ID
		{
			boolean isSuccess = false;
			String sign = null;
			String url = null;

			String mNaviVer = FileUtils.readAssetsFile(getApplicationContext(),
					1);// 程序版本号
			if (mNaviVer != null) {
				mNaviVer = mNaviVer.replace("\r", "").replace("\n", "");
			}

			String mMapVer = FileUtils.readAssetsFile(getApplicationContext(),
					2);// 地图版本号
			if (mMapVer != null) {
				mMapVer = mMapVer.replace("\r", "").replace("\n", "");
			}
			String mICCID = NetWorkUtil.getICCIDNum(getApplicationContext());
			String mImsi = NetWorkUtil.getImsi(getApplicationContext());
			String mPhoneNum = NetWorkUtil.getPhoneNum(getApplicationContext());
			String deviceName = "后视镜";

			String serialNum = argValue.toUpperCase();// 传入的参数，如IMEI相当与导航里的设备特征码
			String naviDeviceNum = FileUtils.readAssetsFile(this, 3);// 读客户设备编号，需要加入设备特征码
//			String temp = serialNum;//+naviDeviceNum;
//			if (temp != null)
//				serialNum = temp.replace("\r", "").replace("\n", "");
			if (serialNum == null)
				return false;

			long cid = 1010;
			String strCid = FileUtils.readAssetsFile(this, 4);
			if (strCid != null)
				cid = Long.valueOf(strCid).longValue();

			// 谭博后来更新apptype = 19 //注意签名参数需要按字母排序
			// String paramUnSort =
			// "prover=1.0&apiver=1&appid=24&apptype=19&cid="+cid+"&devicecode="+serialNum+"&devicename="+deviceName+"&devicesn="+serialNum+"&ostype=1&rscharset=1&rsformat=1&umsaver=2"+"&naviver="+mNaviVer+"&mapver="+mMapVer;
			String paramUnSort = "prover=1.0&apiver=" + KCLOUD_APIVER
					+ "&appid=" + KCLOUD_APPID + "&apptype=" + VALUE_OF_APPTYPE
					+ "&cid=" + cid + "&devicecode=" + serialNum
					+ "&devicename=" + deviceName + "&devicesn=" + serialNum
					+ "&ostype=1&rscharset=1&rsformat=1&umsaver=2"
					+ "&naviver=" + mNaviVer + "&mapver=" + mMapVer;
			if (mICCID != null && !"".equals(mICCID))
				paramUnSort += "&iccid=" + mICCID;
			if (mImsi != null && !"".equals(mImsi))
				paramUnSort += "&imsi=" + mImsi;
			if (mPhoneNum != null && !"".equals(mPhoneNum))
				paramUnSort += "&mobile=" + mPhoneNum;

			String paramSortted = Md5Utils.sortParam(paramUnSort);// 排序
			sign = Md5Utils.MD5(paramSortted + getDeviceIdKey);
			url = mKaccountGetCodeUrl + "kaccount_reg_device.php";
			CldLog.i(TAG, "sign = " + sign);

			JSONObject paramObj = new JSONObject();
			try {
				paramObj.put("apptype", VALUE_OF_APPTYPE);
				paramObj.put("devicecode", serialNum);
				paramObj.put("devicename", deviceName);
				paramObj.put("devicesn", serialNum);
				paramObj.put("ostype", 1);

				if (mICCID != null && !"".equals(mICCID))
					paramObj.put("iccid", mICCID);

				if (mImsi != null && !"".equals(mImsi))
					paramObj.put("imsi", mImsi);

				if (mPhoneNum != null && !"".equals(mPhoneNum))
					paramObj.put("mobile", mPhoneNum);

				paramObj.put("naviver", mNaviVer);
				paramObj.put("mapver", mMapVer);

				paramObj.put("prover", "1.0");
				paramObj.put("apiver", KCLOUD_APIVER);
				paramObj.put("appid", KCLOUD_APPID);
				paramObj.put("rscharset", 1);
				paramObj.put("rsformat", 1);
				paramObj.put("umsaver", 2);
				paramObj.put("cid", cid);// 加入渠道号，听陆神说以后可能会变
				paramObj.put("sign", sign);

				if (null != url && null != paramObj) {
					CldLog.i(TAG, "getServiceDuid--url:" + url
							+ "\n paramObj:" + paramObj.toString());
				}

				// 真正的获取deviceID
				while (!isSuccess) {
					JSONObject jobject = netRequest.sendPostJson(url, paramObj);
					if (jobject != null) {
						FileUtils.logOut("GetDuid-Result = " + jobject,
								isLogToFile);
						int errCode = jobject.getInt("errcode");
						switch (errCode) {
						case 0:
						case 301: {
							int duid = jobject.getInt("duid");
							mDeviceId = duid;
							SharePrefUtils.putShareInt(SharePrefUtils.DUID,
									duid);
							SharePrefUtils.putShareInt(
									SharePrefUtils.DUID_ARG_TYPE, type);
							SharePrefUtils.putShareString(
									SharePrefUtils.DUID_ARG, argValue);
							SharePrefUtils.putShareBoolean(
									SharePrefUtils.IS_TEST_SEVER, isTestServer);
							isSuccess = true;
							FileUtils.logOut("DUID=" + duid + ",DUID_ARG_TYPE="
									+ type + ",DUID_ARG=" + argValue
									+ ",is_test_sever=" + isTestServer,
									isLogToFile);
							break;
						}

						default:
							httpSleep(3000);
							break;
						}
					} else {
						httpSleep(3000);
					}

				}
			} catch (JSONException e) {
				e.printStackTrace();
				return false;
			}
		} else// 已经存储了设备ID
		{
			CldLog.w("js", "isUseLocalDuid-after-1");
			mDeviceId = deviceId;
		}
		FileUtils.logOut("mDeviceId====" + mDeviceId, isLogToFile);
		return true;
	}

	/**
	 * 查询流量卡归属 1语境，2,3,4 ，-1失败
	 */
	public int queryCardBelong() throws JSONException {
		NetWorkRequest netRequest = KCloudPositionManager.getInstance()
				.getNetRequest();
		JSONObject jsonResult = null;
		String sign = null;
		String url = null;

		String iccid = NetWorkUtil.getICCIDNum(MainService.this);
		String imsi = NetWorkUtil.getImsi(MainService.this);
		String sim = NetWorkUtil.getPhoneNum(MainService.this);

		String paramUnSort = "apiver=1";
		String paramMd5UnSort = "apiver=1";

		if (iccid != null && !"".equals(iccid)) {
			paramMd5UnSort = paramMd5UnSort + "&iccid=" + iccid;
			paramUnSort = paramUnSort + "&iccid=" + iccid;
		} else {
			paramUnSort = paramUnSort + "&iccid=" + "";
		}

		if (imsi != null && !"".equals(imsi)) {
			paramMd5UnSort = paramMd5UnSort + "&imsi=" + imsi;
			paramUnSort = paramUnSort + "&imsi=" + imsi;
		} else {
			paramUnSort = paramUnSort + "&imsi=" + "";
		}

		if (sim != null && !"".equals(sim)) {
			paramMd5UnSort = paramMd5UnSort + "&sim=" + sim;
			paramUnSort = paramUnSort + "&sim=" + sim;
		} else {
			paramUnSort = paramUnSort + "&sim=" + "";
		}

		String paramSortted = Md5Utils.sortParam(paramUnSort);
		String paramMd5Sortted = Md5Utils.sortParam(paramMd5UnSort);

		if (isTestServer) {
			sign = Md5Utils.MD5(paramMd5Sortted + mQureyTestKey);
			url = mQureyTestUrl + "&" + paramSortted + "&sign=" + sign;
		} else {
			sign = Md5Utils.MD5(paramMd5Sortted + mQureyKey);
			url = mQureyUrl + "&" + paramSortted + "&sign=" + sign;
		}

		jsonResult = netRequest.SendGetJson(url);
		if (jsonResult == null)
			return -1;
		String suplerName = jsonResult.getString("cardsupplier");
		if (suplerName != null && !"".equals(suplerName)) {
			String temp = suplerName.replace(" ", "").trim();
			if (temp != null && temp.equals("语境"))
				return 1;
		}
		return -1;
		// long cid = 1010;
		// String strCid = FileUtils.readAssetsFile(this, 4);
		// if(strCid!=null)
		// cid = Long.valueOf(strCid).longValue();
		// String paramUnSort =
		// "apiver=1&appid=24&cid="+cid+"&duid="+mDeviceId+"&prover=1.0&rscharset=1&rsformat=1&umsaver=2";
		// String paramSorted = Md5Utils.sortParam(paramUnSort);
		// String sign = Md5Utils.MD5(paramSorted+getDeviceIdKey);
		// String url =
		// mKaccountGetCodeUrl+"kaccount_get_device_info.php?duid="+mDeviceId+"&cid="+cid+"&prover=1.0&umsaver=2&rscharset=1&rsformat=1&apiver=1&appid=24&sign="+sign;
		// if(netRequest!=null)
		// jobject = netRequest.SendGetJson(url);
		//
		// if(jobject!=null)
		// {
		// mCardBelongTo = jobject.getJSONObject("data").getInt("supplier");
		// return true;
		// }

	}

	/**
	 * 设置apn区隔
	 * 
	 * @return apn
	 */
	private String queryApnChannel() throws JSONException {
		NetWorkRequest netRequest = KCloudPositionManager.getInstance()
				.getNetRequest();
		JSONObject jResult = null;
		String sign = null;
		String url = null;
		String cardApn = null;

		String iccid = NetWorkUtil.getICCIDNum(this);
		String imsi = NetWorkUtil.getImsi(this);
		String sim = NetWorkUtil.getPhoneNum(this);

		String paramUnSort = "apiver=1";
		String paramMd5UnSort = "apiver=1";

		if (iccid != null && !"".equals(iccid)) {
			paramMd5UnSort = paramMd5UnSort + "&iccid=" + iccid;
			paramUnSort = paramUnSort + "&iccid=" + iccid;
		} else {
			paramUnSort = paramUnSort + "&iccid=" + "";
		}

		if (imsi != null && !"".equals(imsi)) {
			paramMd5UnSort = paramMd5UnSort + "&imsi=" + imsi;
			paramUnSort = paramUnSort + "&imsi=" + imsi;
		} else {
			paramUnSort = paramUnSort + "&imsi=" + "";
		}

		if (sim != null && !"".equals(sim)) {
			paramMd5UnSort = paramMd5UnSort + "&sim=" + sim;
			paramUnSort = paramUnSort + "&sim=" + sim;
		} else {
			paramUnSort = paramUnSort + "&sim=" + "";
		}

		String paramSortted = Md5Utils.sortParam(paramUnSort);
		String paramMd5Sortted = Md5Utils.sortParam(paramMd5UnSort);

		if (isTestServer) {
			sign = Md5Utils.MD5(paramMd5Sortted + mQureyTestKey);
			url = mQureyApnTestUrl + "&" + paramSortted + "&sign=" + sign;
		} else {
			sign = Md5Utils.MD5(paramMd5Sortted + mQureyKey);
			url = mQureyApnUrl + "&" + paramSortted + "&sign=" + sign;
		}

		jResult = netRequest.SendGetJson(url);
		if (jResult == null)
			return null;
		int errorCode = jResult.getInt("errcode");

		if (errorCode != 0)
			return null;

		cardApn = jResult.getString("cardapn");
		if (cardApn != null && !"".equals(cardApn) && !"0".equals(cardApn)
				&& !"NO".equals(cardApn)) {
			cardApn = cardApn.trim();
		} else {
			cardApn = null;
		}

		return cardApn;
	}

	/**
	 * 监测监控服务
	 */
	public class DetecMoniSevTask extends TimerTask {

		@Override
		public void run() {
			boolean isExist = getServiceExist();
			FileUtils.logOut("mainService-ACTION_TIME_TICK-isMonitorExist="
					+ isExist, isLogToFile);
			if (!isExist) {
				startMonitorServ();
			}
		}

	}

	static int curApnRanmType = 0;

	private class ApnTask extends TimerTask {
		private String lastQuerayApn = "";

		@Override
		public void run() {
			try {
				if (NetWorkUtil.isNetAvailable()) {
					String apnResult = queryApnChannel();
					if (apnResult != null && !apnResult.equals(lastQuerayApn)) {
						apnSet(apnResult);
						lastQuerayApn = apnResult;
					}
				} else {
					apnInit(curApnRanmType);
					curApnRanmType = ++curApnRanmType % 3;
					// if(ApnTools.isCurApnBelongKldPublicNet(getApplicationContext()))
					// {
					// apnInit(1);//切换为凯立德定向网络
					// }
					// else
					// {
					// apnInit(0);//切换为凯立德公共网络
					// }

				}
			} catch (JSONException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	/**
	 * 设置指定apn，重载函数
	 * 
	 * @param apn
	 */
	public void apnSet(String apn) {
		String tartgetApn = null;
		String strICCID = NetWorkUtil.getICCIDNum(getApplicationContext());
		if (apn == null || "".equals(apn))
			return;

		if (strICCID == null) {
			strICCID = NetWorkUtil.getImsi(getApplicationContext());
		}
		if (strICCID == null || "".equals(strICCID))
			return;

		tartgetApn = apn.trim();
		if (tartgetApn != null) {
			if (!ApnTools.isExisApn(getApplicationContext(), tartgetApn)) {
				ApnTools.insertApn(MainService.this, tartgetApn);
			}
			int prefId = ApnTools.getPreferredApnId(getApplicationContext());
			int targetId = ApnTools.getApnId(getApplicationContext(),
					tartgetApn);
			if (prefId != targetId)
				ApnTools.setDefaultApn(getApplicationContext(), targetId);
		}
	}

	/**
	 * @param channel
	 *            0:标准02定向apn，1：公网, 2:标准01定向apn
	 */
	public void apnInit(int channel) {
		String tartgetApn = null;
		String strICCID = NetWorkUtil.getICCIDNum(getApplicationContext());
		if (channel < 0 || channel > 2)
			return;

		if (strICCID == null) {
			strICCID = NetWorkUtil.getImsi(getApplicationContext());
		}
		if (strICCID == null || "".equals(strICCID))
			return;

		int len = strICCID.trim().length();
		if (len >= 20)// 表明是ICCID，需要检测是否为物联卡
		{
			String key = strICCID.substring(0, 6);
			if (key != null && key.equals("898606")) {
				if (channel == 0) {
					tartgetApn = ApnTools.MY_APN_STADARD;

				} else if (channel == 1) {
					tartgetApn = ApnTools.MY_APN_PUBLIC;

				} else if (channel == 2) {
					tartgetApn = ApnTools.MY_APN;
				}
				if (tartgetApn != null) {
					tartgetApn = tartgetApn.trim();
					if (!ApnTools
							.isExisApn(getApplicationContext(), tartgetApn)) {
						ApnTools.insertApn(MainService.this, tartgetApn);
					}
					int prefId = ApnTools
							.getPreferredApnId(getApplicationContext());
					int targetId = ApnTools.getApnId(getApplicationContext(),
							tartgetApn);
					if (prefId != targetId)
						ApnTools.setDefaultApn(getApplicationContext(),
								targetId);
				}

			}
		} else if (len >= 15)// 不是ICCID难以判断是否为物联卡
		{
			if (channel == 0) {
				tartgetApn = ApnTools.MY_APN_STADARD;

			} else if (channel == 1) {
				tartgetApn = ApnTools.MY_APN_PUBLIC;
			} else if (channel == 2) {
				tartgetApn = ApnTools.MY_APN;
			}
			if (tartgetApn != null) {
				if (!ApnTools.isExisApn(getApplicationContext(), tartgetApn)) {
					ApnTools.insertApn(MainService.this, tartgetApn);
				}
				int prefId = ApnTools
						.getPreferredApnId(getApplicationContext());
				int targetId = ApnTools.getApnId(getApplicationContext(),
						tartgetApn);
				if (prefId != targetId)
					ApnTools.setDefaultApn(getApplicationContext(), targetId);
			}
		}
	}

	/*
	 * 设置apn相关
	 */
	// public void apnInit()
	// {
	// String strICCID = NetWorkUtil.getICCIDNum(getApplicationContext());
	// if(strICCID == null)
	// {
	// strICCID = NetWorkUtil.getImsi(getApplicationContext());
	// }
	// if(strICCID==null||"".equals(strICCID))
	// return;
	//
	// int len = strICCID.trim().length();
	// if(len>=20)//表明是ICCID，需要检测是否为物联卡
	// {
	// String key = strICCID.substring(4, 6);
	// if(key!=null&&key.equals("06"))
	// {
	// if(!ApnTools.isExisApn(getApplicationContext(), ApnTools.MY_APN))
	// {
	// ApnTools.insertApn(MainService.this);
	// int curid = ApnTools.getApnId(getApplicationContext(),ApnTools.MY_APN);
	// if(curid!=-1)
	// {
	// ApnTools.setDefaultApn(getApplicationContext(), curid);
	// }
	// }
	// }
	// }
	// else if(len>=15)//不是ICCID难以判断是否为物联卡
	// {
	// if(!ApnTools.isExisApn(getApplicationContext(), ApnTools.MY_APN))
	// {
	// ApnTools.insertApn(MainService.this);
	// int curid = ApnTools.getApnId(getApplicationContext(),ApnTools.MY_APN);
	// if(curid!=-1)
	// {
	// ApnTools.setDefaultApn(getApplicationContext(), curid);
	// }
	// }
	// }
	// }
	/*************************** 新代码 ************************************************/

	/*************************** 新代码 ************************************************/

	/** DUID测试 **/
	GetDuidReceiver mGetDuidReceiver = null;

	private void RegisterGetDuidReceiver(Context context) {
		if (mGetDuidReceiver == null) {
			mGetDuidReceiver = new GetDuidReceiver();
			IntentFilter filter = new IntentFilter(
					GetDuidReceiver.GET_DUID_REGISTER);
			context.registerReceiver(mGetDuidReceiver, filter);
		}
	}

	private void UnRegisterGetDuidReceiver(Context context) {
		if (mGetDuidReceiver != null) {
			context.unregisterReceiver(mGetDuidReceiver);
			mGetDuidReceiver = null;
		}
	}

	private class GetDuidReceiver extends BroadcastReceiver {
		public static final String GET_DUID_REGISTER = "cld_get_duid_register";
		public static final String SEND_DUID_REGISTER = "cld_send_duid_register";

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(GET_DUID_REGISTER)) {
				Intent intent1 = new Intent(SEND_DUID_REGISTER);
				intent1.putExtra("duid", mDeviceId);
				intent1.putExtra("kuid", getKuid());
				sendBroadcast(intent1);
			}
		}

	}

	/** DUID测试end **/

	/********** 小凯互联 *********/
	class MyWeixinCallBack extends WeixinUtil.WeixinCallback {

		@Override
		public void onGetSuccess() {
			SendEmptyMsgToHandler(MSG_GET_WEIXIN_SUCCESS);
			mWeixinUtil.SetCallback(null);// 获得微信成功
		}

		@Override
		public void onGetError() {
			SendEmptyMsgToHandler(MSG_GET_WEIXIN_FAILED);
			mWeixinUtil.SetCallback(null);// 获得微信失败
		}
	}

	private void SendEmptyMsgToHandler(int msg) {
		if (handlerWeixin != null)
			handlerWeixin.sendEmptyMessage(msg);
	}

	private String GetQrReturn(String QrString) {
		String errmsg = null;
		String weixinStr = null;
		JSONObject jsonObjectReturn = null;

		try {
			do {
				jsonObjectReturn = new JSONObject(QrString);
				int errcode = -1;
				if (jsonObjectReturn.has("errcode")) {
					errcode = jsonObjectReturn.getInt("errcode");
				}
				if (errcode != 0) {
					break;
				}
				if (jsonObjectReturn.has("errmsg")) {
					errmsg = jsonObjectReturn.getString("errmsg");
				}
				if (jsonObjectReturn.has("data")) {
					weixinStr = jsonObjectReturn.getString("data");
				}
			} while (false);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String out = "";
		if (errmsg != null) {
			out = out + "errormsg:" + errmsg;
		}
		if (weixinStr != null) {
			out = out + "weixinStr:" + weixinStr;
		}
		CldLog.d(TAG, "get:" + out);

		if (weixinStr == null || "".equals(weixinStr) || weixinStr.length() < 1) {
			return null;
		}

		return weixinStr;
	}

	public boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}

	/**
	 * 判断网络连接是否可用
	 * 
	 * @return true:可以访问网络
	 **/
	public boolean isNetworkAvailable() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (null != cm) {
			NetworkInfo info = cm.getActiveNetworkInfo();
			if (null != info && info.isConnected()) {
				if (NetworkInfo.State.CONNECTED == info.getState()) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * 发送网络异常的消息
	 **/
	public void sendNetworkAnomalyMsg() {
		if (!isNetworkAvailable()) {
			SendEmptyMsgToHandler(MSG_NET_DISCONNECTED);
		}
	}

	@SuppressLint("NewApi")
	public void getWeixinQr() {
		
		CldLog.i(TAG, "getWeixinQr");
		int mWeixinDuid = SharePrefUtils.getShareInt(
				SharePrefUtils.WEIXIN_DUID, -1);
		String mWeixinQr = SharePrefUtils.getShareString(
				SharePrefUtils.WEIXIN_QR, "");
		
		CldLog.i(TAG, "mDeviceId: " + mDeviceId);
		CldLog.i(TAG, "mWeixinDuid: " + mWeixinDuid);
		CldLog.i(TAG, "mWeixinQr: " + mWeixinQr);
		if (mWeixinDuid == mDeviceId && mWeixinDuid != -1
				&& !mWeixinQr.isEmpty()) {
			// 已获取过微信二维码数据
			SendEmptyMsgToHandler(MSG_GET_WEIXIN_ALREADY);
			return;
		}
		
		if (!isNetworkConnected() || mDeviceId == -1) {
			SendEmptyMsgToHandler(MSG_NET_DISCONNECTED);
			return;
		}

		if (mWeixinCallBack == null)
			mWeixinCallBack = new MyWeixinCallBack();
		mWeixinUtil.SetCallback(mWeixinCallBack);

		// 获取设备特征码
		String typeStr = FileUtils.readAssetsFile(getApplicationContext(), 0);
		if (typeStr != null) {
			typeStr = typeStr.replace("\r", "").replace("\n", "");
		}
		
		int type = Integer.valueOf(typeStr).intValue();
		CldLog.i(TAG, "type: " + type);
		String argValue = DeviceUtils.getSerialNum(this, type);

		if (null == argValue) {
			SendEmptyMsgToHandler(MSG_NET_DISCONNECTED);
			return;
		}

		PostWeixinQr("", MainService.STATUS_GETTING);
		mWeixinUtil.onGetQrText("" + mDeviceId, argValue);
	}

	BroadcastReceiver mWeixinReceiver = new BroadcastReceiver() {
		@SuppressLint("NewApi")
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if (arg1.getAction().equals("CLD.NAVI.XIAOKAIHULIAN.SERVICE")) {
				CldLog.i(TAG, "get service receiver");
				mBPostWeixinQr = true;
				getWeixinQr();
			}
		}
	};

	private void RegisterPostWeixinReceiver() {
		IntentFilter tmp = new IntentFilter("CLD.NAVI.XIAOKAIHULIAN.SERVICE");
		registerReceiver(mWeixinReceiver, tmp);
	}

	private void UnregisterPostWeixinReceiver() {
		unregisterReceiver(mWeixinReceiver);
	}

	// nGetQrStatus: 0-获取失败; 1-获取中; 2-获取完成
	private void PostWeixinQr(String strWeixinQr, int nGetQrStatus) {
		if (!mBPostWeixinQr)
			return;

		// 已获取过微信二维码数据
		Intent i = new Intent("CLD.NAVI.XIAOKAIHULIAN.CLIENT");
		Bundle b = new Bundle();
		b.putInt("status", nGetQrStatus); // 0:获取失败; 1:获取中; 2:获取完成
		b.putString("QrText", strWeixinQr);// 只有在"status"为2时"QrText"项才有效,"QrText"的内容用于生成二维码
		i.putExtras(b);
		sendBroadcast(i);
	}
	/* end*********小凯互联******** */
}
