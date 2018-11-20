package cld.kcloud.center;

import android.annotation.SuppressLint;
import android.app.ActivityManagerNative;
import android.os.RemoteException;
import android.os.SystemProperties;
import cld.kcloud.utils.KCloudShareUtils;
import com.cld.log.CldLog;

/**
 * 
 * @author wuyl
 *
 */
public class KCloudDevice {
	private static final String TAG = "KCloudDevice";
	
	/**
	 * 
	 * @Title: getDeviceID
	 * @Description: sn号：唯一识别(mac 烧录到设备上)
	 * @return
	 * @return: String
	 */
	@SuppressLint({ "NewApi", "DefaultLocale" }) 
	public static String getDeviceID() {
		String serialId = null;
		try {
			serialId = ActivityManagerNative.getDefault().getSomething(1, "", 0, 0);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}

		if (serialId == null) {
			return "UNKNOW";
		}

		serialId = serialId.toUpperCase();
		serialId = serialId.replace('O', '0');

		CldLog.i(TAG, "serialId = " + serialId);
		return serialId;
	}
	
	/**
	 * 与上次是否同台机器
	 */
	@SuppressLint("NewApi") 
	public static boolean isDifferentLast() {
		if (!KCloudShareUtils.getString(KCloudAppUtils.TARGET_FIELD_IMEI).isEmpty()
				&& KCloudShareUtils.getString(KCloudAppUtils.TARGET_FIELD_IMEI).equals(getDeviceID())) {
			return true;
		}
		
		return false;
	}
	
	// 获取sim卡号
	@SuppressLint("NewApi") 
	public static String getSimSerialNumberEx() {
		String iccid = "";
		//M530根据系统属性读取，方法由众鸿提供
		iccid = SystemProperties.get("ril.sim.iccid", ""); //"8986061509000978456";
		CldLog.i(TAG, "iccid = " + iccid);
		
		//如果为空，则使用上次保存的iccid
		if (iccid.equals("")) {
			iccid = KCloudShareUtils.getString(KCloudAppUtils.TARGET_FIELD_ICCID);
		}
			
		if (iccid != null && !iccid.isEmpty() && !iccid.contains("0000000000")) {
			return iccid.replace(" ", "");
		}
		return iccid;
	}
}
