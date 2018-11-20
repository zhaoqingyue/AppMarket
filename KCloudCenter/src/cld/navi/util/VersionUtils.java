package cld.navi.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;

public class VersionUtils {

	/**
	 * @Title: getAppVersionName
	 * @Description: 获取版本name
	 * @param context
	 * @return: String
	 */
	public static String getAppVersionName(Context context) {
		String versionName = null;
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			versionName = packageInfo.versionName;
			if (TextUtils.isEmpty(versionName)) {
				return "";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return versionName;
	}
	
	/**
	 * @Title: getAppVersionCode
	 * @Description: 获取当前版本code
	 * @param context
	 * @return: int
	 */
	public static int getAppVersionCode(Context context)
	{
		int versionCode = -1;
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			versionCode = packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		return versionCode;
	}
	
	/**
	 * @Title: getAppPackageName
	 * @Description: 获取当前包名
	 * @param context
	 * @return: String
	 */
	public static  String getAppPackageName(Context context){
		String packageName = null;
		packageName = context.getPackageName();
		return packageName;
	}
	
	/**
	 * @Title: isCurAppPreInstalled
	 * @Description: 当前是否为预装
	 * @param context
	 * @return: boolean
	 */
	public static boolean isCurAppPreInstalled(Context context) {
	    PackageManager packageManager = context.getPackageManager();
	    PackageInfo packInfo = null;
	    
		try {
			packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			if ((packInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
				return true;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * 
	 * @Title: getMetaData
	 * @Description: 获取AndroidManifest中定义的MetaData
	 * @param context
	 * @param name meta-data->name
	 * @return: Object
	 */
	public static Object getMetaData(Context context, String name) {
		Object val = null;
		try {
			ApplicationInfo appInfo = context.getPackageManager()
					.getApplicationInfo(context.getPackageName(),
							PackageManager.GET_META_DATA);
			val = appInfo.metaData.get(name);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return val;
	}
	
	/**
	 * 
	 * @Title: getDeviceInfo
	 * @Description: 获取设备信息，for友盟添加测试设备
	 * @param context
	 * @return: String
	 */
	public static String getDeviceInfo(Context context) {
		try {
			org.json.JSONObject json = new org.json.JSONObject();
			android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);

			String device_id = tm.getDeviceId();

			android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);

			String mac = wifi.getConnectionInfo().getMacAddress();
			json.put("mac", mac);

			if (TextUtils.isEmpty(device_id)) {
				device_id = mac;
			}

			if (TextUtils.isEmpty(device_id)) {
				device_id = android.provider.Settings.Secure.getString(
						context.getContentResolver(),
						android.provider.Settings.Secure.ANDROID_ID);
			}

			json.put("device_id", device_id);

			return json.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "null";
	}
	
	/**
	 * @Title: getUID
	 * @Description: 获取设备的UID
	 * @param context
	 * @return
	 * @return: String
	 */
	public static String getUID(Context context) {
		return Md5Utils.MD5(getDeviceInfo(context));
	}
}
