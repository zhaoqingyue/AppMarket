/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: SharePrefUtils.java
 * @Prject: KCloudCenter_M550
 * @Package: cld.navi.util
 * @Description: 本地配置工具
 * @author: zhaoqy
 * @date: 2016年8月11日 下午4:33:45
 * @version: V1.0
 */

package cld.navi.util;

import android.content.SharedPreferences;
import cld.kcloud.custom.manager.KCloudPositionManager;

public class SharePrefUtils {

	public static final String RECORD_RATE = "recordrate";
	public static final String UP_RATE = "uprate";
	public static final String UP_POSITION_URL_HEAD = "uppositionhead";// 位置上报的url头

	public static final String DUID = "duid";// 存储的设备ID
	public static final String DUID_ARG_TYPE = "duid_arg_type";// 存储的设备ID请求的参数类型
	public static final String DUID_ARG = "duid_arg";// 存储的设备ID的参数
	public static final String KUID = "Kuid";
	public static final String IS_TEST_SEVER = "is_test_sever";
	public static final String IS_DUID_FROM_NAVI = "is_duid_from_navi";

	/* start*********小凯互联******** */
	public static final String WEIXIN_DUID = "weixin_duid";
	public static final String WEIXIN_QR = "weixin_qr";
	/* end*********小凯互联******** */

	public static int getShareInt(String key, int def) {
		SharedPreferences mshare = KCloudPositionManager.getInstance()
				.getSharedPreferences();
		int value = mshare.getInt(key, def);
		return value;
	}

	public static void putShareInt(String key, int value) {
		SharedPreferences mshare = KCloudPositionManager.getInstance()
				.getSharedPreferences();
		SharedPreferences.Editor mEditor = mshare.edit();
		mEditor.putInt(key, value);
		mEditor.commit();
	}

	public static boolean getShareBoolean(String key, boolean def) {
		SharedPreferences mshare = KCloudPositionManager.getInstance()
				.getSharedPreferences();
		boolean value = mshare.getBoolean(key, def);
		return value;
	}

	public static void putShareBoolean(String key, boolean value) {
		SharedPreferences mshare = KCloudPositionManager.getInstance()
				.getSharedPreferences();
		SharedPreferences.Editor mEditor = mshare.edit();
		mEditor.putBoolean(key, value);
		mEditor.commit();
	}

	public static String getShareString(String key, String def) {
		SharedPreferences mshare = KCloudPositionManager.getInstance()
				.getSharedPreferences();
		String value = mshare.getString(key, def);
		return value;
	}

	public static void putShareString(String key, String value) {
		SharedPreferences mshare = KCloudPositionManager.getInstance()
				.getSharedPreferences();
		SharedPreferences.Editor mEditor = mshare.edit();
		mEditor.putString(key, value);
		mEditor.commit();
	}
}
