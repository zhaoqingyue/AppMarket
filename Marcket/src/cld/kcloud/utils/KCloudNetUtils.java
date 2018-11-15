package cld.kcloud.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import android.text.TextUtils;
import android.util.Log;
import cld.kmarcket.appinfo.AppInfo;
import cld.kmarcket.util.CommonUtil;
import cld.kmarcket.util.ConfigUtils;
import cld.kmarcket.util.LogUtil;
import cld.weather.api.CldSapNetUtil;
import cld.weather.api.CldSapReturn;
import cld.weather.api.KSign;
import cld.weather.api.NetUtil;

public class KCloudNetUtils {

	private final static String TAG = "KCloudNetUtils";

	/** 首次密文. */
	public static String account_key = "";
	public static String kgo_key = "";
	
	public static final int umsaver = 1;
	public static final int rscharset = 1;
	public static final int rsformat = 1;
	public static final int apiver = 1;
	public static final int cid = 1010;	
	public static final String prover = "C3486-C7M04-3721J0Q";
	
	public static final int system_code = 1;
	public static final int device_code = 1;
	public static final int product_code = 2;
	public static final int width = 1600;
	public static final int height = 480;
	public static String launcher_ver = "";
	
	public static void setAccountKey(String key) {
		account_key = decodeKey(key);
		Log.d(TAG, "account_key = " + account_key);
	}

	private static String getHeadUrl() {
		if (NetUtil.isTestVersion()) {
			return "http://tmctest.careland.com.cn/";
		} else {
			return "http://stat.careland.com.cn/";
		}
	}

	public static CldSapReturn getKgoSign() {
		String key = "";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("umsaver", umsaver);
		map.put("rscharset", rscharset);
		map.put("rsformat", rsformat);
		map.put("apiver", apiver);
		map.put("cid", cid);
		map.put("prover", prover);

		if (NetUtil.isTestVersion()) {
			key = "373275EB226022907CCA40BD2AE481D8";
		} else {
			key = "373275EB226022907CCA40BD2AE481D8";
		}
		
		CldSapReturn errRes = KSign.getGetParms(map, getHeadUrl()
				+ "kgo/api/kgo_get_code.php", key);
		return errRes;
	}

	public static void setKgoKey(String key) {
		kgo_key = decodeKey(key);
		Log.d(TAG, "kgo_key = " + kgo_key);
	}

	public static boolean isGetSign() {
		if (!TextUtils.isEmpty(kgo_key))
			return true;

		return false;
	}
	
	static String decodeKey(String keyCode){
		if (TextUtils.isEmpty(keyCode)) {
			return "";
		} else {
			String key = keyCode.substring(keyCode.length() - 6);
			String str = keyCode.substring(0, keyCode.length() - 6);
			CldEDecrpy cldEDecrpy = new CldEDecrpy(str, key);
			keyCode = cldEDecrpy.decrypt();
			return keyCode;
		}
	}
	
	/**
	 * 检测kgo_key，如果不存在，则获取kgo_key
	 */
	public static void checkKgoSign()
	{
		launcher_ver = CommonUtil.getVernameByPkgname("com.cld.launcher");
		LogUtil.i(LogUtil.TAG, " launcher_ver: " + launcher_ver);
		
		String result;
		CldSapReturn request = null;
		if (!KCloudNetUtils.isGetSign()) 
		{
			request = KCloudNetUtils.getKgoSign();
			LogUtil.i(LogUtil.TAG, "kgosign url: " + request.url);
			result = CldSapNetUtil.sapGetMethod(request.url);
			LogUtil.i(LogUtil.TAG, "kgosign result: " + result);
			if (result != null) 
			{
				try 
				{
					JSONObject object = new JSONObject(result);
					if (object.getInt("errcode") == 0) 
					{
						KCloudNetUtils.setKgoKey(object.getString("code"));
					} 
					else 
					{
						//Message message = handler.obtainMessage();
						//message.what = CLDMessageId.MSG_ID_KGO_GETCODE_FAILED;
						//handler.sendMessage(message);
						return;
					}
				} 
				catch (JSONException e) 
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 获取推荐应用
	 * @param page
	 * @param size
	 * @param appInfoList
	 * @return
	 */
	public static CldSapReturn getNetApp(int page, int size,
			ArrayList<AppInfo> appInfoList) 
	{
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("umsaver", umsaver);
		map.put("rscharset", rscharset);
		map.put("rsformat", rsformat);
		map.put("apiver", apiver);
		map.put("cid", cid);      //可以不要
		map.put("prover", prover);//可以不要
		map.put("encrypt", 0);
		
		map.put("system_code", 1);
		map.put("device_code", 1);
		map.put("product_code", 2); 
		map.put("launcher_ver", launcher_ver); 
		map.put("width", width);
		map.put("height", height); 
		map.put("page", page);
		map.put("size", size);
		//appInfoList不加入计算sign
		String myMd5 = KSign.formatSource(map);
		
		List<Map<String, Object>> apllist = new ArrayList<Map<String,Object>>();
		Map<String, Object> mapApp = null;
		for (AppInfo item : appInfoList) 
		{
			mapApp = new HashMap<String, Object>();
			mapApp.put("packname", item.getPkgName());
			apllist.add(mapApp);
		}
		map.put("install_app", apllist);
		CldSapReturn errRes = KSign.getPostParms(map, getHeadUrl()
				+ "kgo/api/kgo_get_recommend_app.php", kgo_key, myMd5);
		return errRes;
	}
	
	/**
	 * 获取升级应用
	 * @param page
	 * @param size
	 * @param appInfoList
	 * @return
	 */
	public static CldSapReturn getUpdateApp(int page, int size,
			ArrayList<AppInfo> appInfoList, long duid, long kuid, int regionId) 
	{
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("umsaver", umsaver);
		map.put("rscharset", rscharset);
		map.put("rsformat", rsformat);
		map.put("apiver", apiver);
		map.put("encrypt", 0);
		map.put("page", page);
		map.put("size", size);
		map.put("area_code", regionId); //先定位，获取区域id
		map.put("launcher_ver", launcher_ver);
		
		map.put("cid", ConfigUtils.cid);
		map.put("prover", ConfigUtils.appver);
		map.put("system_code", ConfigUtils.system_code);
		map.put("device_code", ConfigUtils.device_code);
		map.put("product_code", ConfigUtils.product_code);
		map.put("width", ConfigUtils.device_width);
		map.put("height", ConfigUtils.device_height);
		map.put("custom_code", ConfigUtils.custom_code);
		map.put("duid", duid);
		map.put("kuid", kuid);
		
		/**
		 * 可配置测试
		 */
		
		if (!NetUtil.isTestVersion()){
			map.put("system_ver", CommonUtil.getSystemVer()); //android系统版本
		} else {
			map.put("system_ver", ConfigUtils.system_ver); //android系统版本
		}
		map.put("plan_code", ConfigUtils.plan_code);      //方案商编号
		map.put("dpi", ConfigUtils.device_dpi);           //设备分辨率
		
		//appInfoList不加入计算sign
		String myMd5 = KSign.formatSource(map);
		
		List<Map<String, Object>> apllist = new ArrayList<Map<String,Object>>();
		Map<String, Object> mapApp = null;
		for (AppInfo item : appInfoList) 
		{
			mapApp = new HashMap<String, Object>();
			mapApp.put("packname", item.getPkgName());
			mapApp.put("vercode", "" + item.getVerCode());
			apllist.add(mapApp);
		}
		map.put("install_app", apllist);
		CldSapReturn errRes = KSign.getPostParms(map, getHeadUrl()
				+ "kgo/api/kgo_get_app_upgrade.php", kgo_key, myMd5);
		return errRes;
	}
	
	/**
	 * 获取app状态
	 * @param appInfoList
	 * @return
	 */
	public static CldSapReturn getAppStatus(ArrayList<AppInfo> appInfoList) 
	{
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("umsaver", umsaver);
		map.put("rscharset", rscharset);
		map.put("rsformat", rsformat);
		map.put("apiver", apiver);
		map.put("cid", cid);      //可以不要
		map.put("prover", prover);//可以不要
		map.put("encrypt", 0);
		//appInfoList不加入计算sign
		String myMd5 = KSign.formatSource(map);
		
		List<Map<String, Object>> apllist = new ArrayList<Map<String,Object>>();
		Map<String, Object> mapApp = null;
		for (AppInfo item : appInfoList) 
		{
			mapApp = new HashMap<String, Object>();
			mapApp.put("packname", item.getPkgName());
			apllist.add(mapApp);
		}
		map.put("install_app", apllist);
		CldSapReturn errRes = KSign.getPostParms(map, getHeadUrl()
				+ "kgo/api/kgo_get_app_status.php", kgo_key, myMd5);
		return errRes;
	}
	
	/**
	 * 获取app下载次数
	 * @param appInfo
	 * @return
	 */
	public static CldSapReturn getUpdateAppDownloadTime(AppInfo appInfo)
	{
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("umsaver", umsaver);
		map.put("rscharset", rscharset);
		map.put("rsformat", rsformat);
		map.put("apiver", apiver);
		map.put("cid", cid);      //可以不要
		map.put("prover", prover);//可以不要
		map.put("encrypt", 0);
		map.put("packname", appInfo.getPkgName());
		map.put("vercode", appInfo.getVerCode());
		CldSapReturn errRes = KSign.getGetParms(map, getHeadUrl()
				+ "kgo/api/kgo_get_update_app_down_times.php", kgo_key);
		return errRes;
	}
	
	/**
	 * 密钥加密 解密
	 * 
	 * @author Zhouls
	 * @date 2014-8-13 下午4:05:06
	 */
	public static class CldEDecrpy {
		/** The str. */
		private String str;
		/** The key box. */
		private int[] keyBox;

		/**
		 * Instantiates a new cld e decrpy.
		 * 
		 * @param str
		 *            the str
		 * @param key
		 *            the key
		 */
		public CldEDecrpy(String str, String key) {
			this.str = str;
			keyBox = new int[3];
			keyBox[0] = Integer.parseInt(key.substring(0, 2));
			keyBox[1] = Integer.parseInt(key.substring(2, 4));
			keyBox[2] = Integer.parseInt(key.substring(4, 6));
		}

		/**
		 * 加密
		 * 
		 * @return char
		 * @author Zhouls
		 * @date 2014-8-13 下午5:13:26
		 */
		public String encrypt() {
			String enStr = "";
			int tempKey;
			for (int i = 0; i < str.length(); i++) {
				tempKey = keyBox[i % 3] % 24;
				enStr = enStr
						+ getChEncrpyt(getIndexEncrpyt(str.charAt(i)) + tempKey);
			}
			return enStr;
		}

		/**
		 * 解密
		 * 
		 * @return char
		 * @author Zhouls
		 * @date 2014-8-13 下午5:00:18
		 */
		public String decrypt() {
			String deStr = "";
			int tempKey;
			for (int i = 0; i < str.length(); i++) {
				tempKey = keyBox[i % 3] % 24;
				deStr += getChDecrpyt(getIndexDecrpyt(str.charAt(i)) - tempKey);
			}
			return deStr;
		}

		/**
		 * 获取加密字符
		 * 
		 * @param i
		 *            the i
		 * @return String
		 * @author Zhouls
		 * @date 2014-8-13 下午4:22:18
		 */
		private char getChEncrpyt(int i) {
			if (i >= 0 && i <= 25) {
				return chr(ord('a') + i);
			} else if (i >= 26 && i <= 35) {
				return chr(ord('0') + i - 26);
			} else if (i >= 36 && i <= 61) {
				return chr(ord('A') + i - 36);
			}
			return '0';
		}

		/**
		 * 获取解密字符
		 * 
		 * @param i
		 *            the i
		 * @return String
		 * @author Zhouls
		 * @date 2014-8-13 下午4:21:09
		 */
		private char getChDecrpyt(int i) {
			if (i == 0) {
				return '-';
			} else if (i == 1) {
				return ';';
			} else if (i >= 2 && i <= 11) {
				return chr(i + ord('0') - 2);
			} else if (i >= 12 && i <= 37) {
				return chr(i + ord('A') - 12);
			}
			return '0';
		}

		/**
		 * 获取加密索引
		 * 
		 * @param c
		 *            the c
		 * @return int
		 * @author Zhouls
		 * @date 2014-8-13 下午4:20:45
		 */
		private int getIndexDecrpyt(char c) {
			if (ord(c) >= ord('0') && ord(c) <= ord('9')) {
				return ord(c) + 26 - ord('0');
			} else if (ord(c) >= ord('A') && ord(c) <= ord('Z')) {
				return ord(c) + 36 - ord('A');
			} else {
				return ord(c) - ord('a');
			}
		}

		/**
		 * 获取解密索引
		 * 
		 * @param c
		 *            the c
		 * @return int
		 * @author Zhouls
		 * @date 2014-8-13 下午4:22:13
		 */
		private int getIndexEncrpyt(char c) {
			if (c == '-') {
				return 0;
			}
			if (c == ';') {
				return 1;
			} else if (c >= '0' && c <= '9') {
				return c - '0' + 2;
			} else if (c >= 'A' && c <= 'Z') {
				return c - 'A' + 12;
			}
			return 0;
		}

		/**
		 * int转char
		 * 
		 * @param i
		 *            the i
		 * @return char
		 * @author Zhouls
		 * @date 2014-8-13 下午4:31:41
		 */
		private char chr(int i) {
			char c = (char) i;
			return c;
		}

		/**
		 * char转int
		 * 
		 * @param c
		 *            the c
		 * @return int
		 * @author Zhouls
		 * @date 2014-8-13 下午4:29:52
		 */
		private int ord(char c) {
			int a = c;
			return a;
		}
	}
}
