/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: NetWorkUtil.java
 * @Prject: KCloudCenter_M550
 * @Package: cld.navi.util
 * @Description: 网络状况工具类
 * @author: zhaoqy
 * @date: 2016年8月11日 下午4:32:14
 * @version: V1.0
 */

package cld.navi.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import cld.kcloud.custom.manager.KCloudPositionManager;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.Log;

@SuppressLint("DefaultLocale")
public class NetWorkUtil {

	// 创建NetWorkUtil对象
	public static final int NT_UNKNOWN = 0;    // 未知
	public static final int NT_ETHERNET = 1;   // 通过以太网卡
	public static final int NT_WIFI = 2;       // wifi网络
	public static final int NT_CT_WAP = 3;     // 电信WAP
	public static final int NT_CT_NET = 4;     // 电信NET
	public static final int NT_UNI_WAP = 5;    // 联通2G WAP
	public static final int NT_UNI_NET = 6;    // 联通2G NET
	public static final int NT_UNI_3G_WAP = 7; // 联通3G WAP
	public static final int NT_UNI_3G_NET = 8; // 联通3G NET
	public static final int NT_TD_3G_WAP = 9;  // 移动3G WAP
	public static final int NT_TD_3G_NET = 10; // 移动3G NET
	public static final int NT_GPRS_NET = 11;  // 移动GPRS网络 net方式
	public static final int NT_GPRS_WAP = 12;  // 移动GPRS网络 wap方式
	public static final int NT_OTHER = 15;     // 其他网络

	public NetWorkUtil() {

	}

	// 获取网络状况
	public static boolean getNetworkStatus() {
		ConnectivityManager mConnectivityManager = KCloudPositionManager
				.getInstance().getConnectivityManager();
		try {
			if (null == mConnectivityManager) {
				return false;
			}
			NetworkInfo networkInfo = mConnectivityManager
					.getActiveNetworkInfo();

			if (null == networkInfo) {
				return false;
			}
			if (networkInfo.isAvailable() == true) {
				return true;
			} else {
				Log.i("network", "network is bad");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	// 获取当前网络状态 1：已连接 0 ：未连接
	public static boolean isConnByHttp() {
		boolean isConn = false;
		URL url;
		HttpURLConnection conn = null;
		try {
			url = new URL("http://www.baidu.com");
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(1000 * 3);
			if (conn.getResponseCode() == 200) {
				isConn = true;
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			conn.disconnect();
		}
		return isConn;
	}

	public static boolean isNetAvailable() {
		ConnectivityManager mConnectivityManager = KCloudPositionManager
				.getInstance().getConnectivityManager();
		if (mConnectivityManager == null)
			return false;

		NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
		if (netInfo == null)
			return false;

		boolean isEnable = netInfo.isAvailable();
		return isEnable;
	}

	// 获取当前网络类型
	public static int getNetworkType() {
		ConnectivityManager mConnectivityManager = KCloudPositionManager
				.getInstance().getConnectivityManager();
		try {
			// 先检查是否为WiFi网络
			if (mConnectivityManager != null) {
				NetworkInfo networkInfo = mConnectivityManager
						.getNetworkInfo(ConnectivityManager.TYPE_WIFI); // 检查wifi是否连接
				if (networkInfo != null
						&& networkInfo.getState() == State.CONNECTED) {
					return NT_WIFI;
				}
				// 再检查是否为手机网络
				networkInfo = mConnectivityManager
						.getNetworkInfo(ConnectivityManager.TYPE_MOBILE); // 检查GPRS是否连接
				if (networkInfo != null
						&& networkInfo.getState() == State.CONNECTED) {
					String apn = networkInfo.getExtraInfo();
					if (apn == null || apn.equals("") || apn.equals(" ")) {
						return NT_WIFI;// 如果apn无效还能上网则认为是wifi
					}

					if (apn.trim().equalsIgnoreCase("internet")) {
						return NT_ETHERNET;
					}

					// 以下为移动2G网络
					if (apn.trim().equalsIgnoreCase("cmnet")) {
						return NT_GPRS_NET;
					}
					if (apn.trim().equalsIgnoreCase("cmwap")) {
						return NT_GPRS_WAP;
					}

					// 以下为移动3G网络
					if (apn.trim().equalsIgnoreCase("cmnet")) {
						return NT_TD_3G_NET;
					}
					if (apn.trim().equalsIgnoreCase("cmwap")) {
						return NT_TD_3G_WAP;
					}

					// 以下为联通2G网络
					if (apn.trim().equalsIgnoreCase("uninet")) {
						return NT_UNI_NET;
					}
					if (apn.trim().equalsIgnoreCase("uniwap")) {
						return NT_UNI_WAP;
					}

					// 以下为联通3G网络
					if (apn.trim().equalsIgnoreCase("3gnet")) {
						return NT_UNI_3G_NET;
					}
					if (apn.trim().equalsIgnoreCase("3gwap")) {
						return NT_UNI_3G_WAP;
					}
					// 以下为电信3G网络
					if (apn.trim().equalsIgnoreCase("ctnet")
							|| apn.trim().equalsIgnoreCase("ctc")) {
						return NT_CT_NET;
					}
					if (apn.trim().equalsIgnoreCase("ctwap")) {
						return NT_CT_WAP;
					}
					return NT_OTHER;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return NT_UNKNOWN;

	}

	// 获取网卡地址
	public static String getMacAddress(Context context) {
		try {
			WifiManager mWifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = mWifiManager.getConnectionInfo();
			if (info.getBSSID() != null) {
				info.getMacAddress();
				return info.getMacAddress();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}

	// 获取sim卡号
	public static String getSimSerialNumberEx() {
		// M530根据系统属性读取，方法由众鸿提供
		String iccid = SystemProperties.get("ril.sim.iccid", null);
		Log.d("NaviOne", "iccid = " + iccid);
		return iccid;
	}

	// 获取sim的ICCID号
	public static String getICCIDNum(Context context) {

		String SerialNumber = getSimSerialNumberEx();
		if (SerialNumber != null && !SerialNumber.contains("0000000000"))
			return SerialNumber.trim();

		TelephonyManager mTelephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (mTelephonyManager != null) {
			SerialNumber = mTelephonyManager.getSimSerialNumber();
			return SerialNumber == null ? null : SerialNumber.trim();
		}
		return null;
	}

	/*
	 * 获取本机的蓝牙地址
	 */
	public static String getBlueToothMac(Context context) {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (mBluetoothAdapter != null)
			return mBluetoothAdapter.getAddress();
		return null;
	}

	// 获取IMEI号
	public static String getImeiNum(Context context) {
		String mImeiNum = null;
		TelephonyManager mTelephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		mImeiNum = mTelephonyManager.getDeviceId();

		String temp;

		if (mImeiNum != null) {
			temp = mImeiNum.replace(" ", "");
		} else {
			temp = "".replace(" ", "");
		}

		return temp;
	}

	@SuppressLint("DefaultLocale") 
	public static String getCidNum(int cid)// 卡ID加密,默认8位
	{
		String mCidNum = null;
		char[] buffer = new char[64];
		String pathId = "/sys/class/mmc_host/mmc" + cid + "/";
		File pathFile = new File(pathId);
		if (!pathFile.exists())
			return null;

		try {
			@SuppressWarnings("resource")
			BufferedReader bReader = new BufferedReader(new InputStreamReader(
					new FileInputStream(pathFile)));
			bReader.read(buffer);
			mCidNum = new String(buffer);
			if (mCidNum != null) {

				String temp = mCidNum.substring(18, 26);
				if (temp != null)
					temp.toUpperCase();
				return temp;
			}

		} catch (IOException e) {
			e.printStackTrace();
			
		}
		return null;
	}

	/**
	 * 获取SIM卡的IMSI
	 */
	public static String getImsi(Context context) {
		TelephonyManager mTelephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (mTelephonyManager != null) {
			String IMSI = mTelephonyManager.getSubscriberId();
			return IMSI == null ? null : IMSI.trim();

		}
		return null;
	}

	/**
	 * 获取SIM卡的电话号码
	 */
	public static String getPhoneNum(Context context) {
		String mPhoneNum = null;
		TelephonyManager mTelephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (mTelephonyManager != null) {
			mPhoneNum = mTelephonyManager.getLine1Number();
			return mPhoneNum == null ? null : mPhoneNum.trim();
		}
		return null;
	}

	/**
	 * 获取休眠锁，防止休眠
	 */
	@SuppressLint("Wakelock") 
	public static void getDeviceLock(Context context) {
		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		@SuppressWarnings("deprecation")
		PowerManager.WakeLock wakeLock = pm.newWakeLock(
				PowerManager.SCREEN_DIM_WAKE_LOCK
						| PowerManager.ON_AFTER_RELEASE, "MainService");
		wakeLock.acquire();
	}
}
