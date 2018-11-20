package cld.kcloud.utils.sap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import android.text.TextUtils;
import cld.kcloud.center.KCloudAppConfig;
import cld.kcloud.center.KCloudAppUtils;
import cld.kcloud.custom.bean.KCloudInstalledInfo;
import cld.kcloud.utils.KCloudBaseParse;
import cld.kcloud.utils.KCloudCommonUtil;
import com.cld.log.CldLog;
import com.cld.ols.api.CldKAccountAPI;
import com.cld.ols.sap.CldSapUtil;
import com.cld.ols.sap.parse.CldKBaseParse;
import com.cld.ols.tools.CldSapParser;
import com.cld.ols.tools.CldSapReturn;

public class KCloudNetworkSap {

	private final static String TAG = "KCloudNetUtils";

	/** 首次密文. */
	public static String account_key = "";
	public static String kgo_key = "";

	/** The APIVER. */
	private final static int APIVER = 1;
	/** The UMSAVER. */
	private final static int UMSAVER = 1;
	/** The RSCHARSET. */
	private final static int RSCHARSET = 1;
	/** The RSFORMAT. */
	private final static int RSFORMAT = 1;

	//------------------- 账号系统 --------------------
	/**
	 * 获取车辆信息
	 * 
	 * @param appid
	 * @param cid
	 * @param prover
	 * @param duid
	 * @return
	 */
	public static CldSapReturn getUserCarInfo(int appid, int cid,
			String prover, long duid) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("apiver", APIVER);
		map.put("umsaver", UMSAVER);
		map.put("rscharset", RSCHARSET);
		map.put("rsformat", RSFORMAT);
		map.put("appid", appid);
		map.put("encrypt", 1);
		map.put("flag", 1);
		map.put("duid", duid);
		CldSapParser.putIntToMap(map, "cid", cid);
		CldSapParser.putStringToMap(map, "prover", prover);

		CldSapReturn errRes = CldKBaseParse.getGetParms(map,
				CldSapUtil.getNaviSvrKA() + "kaccount_get_device_info.php",
				account_key);

		return errRes;
	}

	public static void setAccountKey(String key) {
		account_key = CldSapUtil.decodeKey(key);
		CldLog.i(TAG, "account_key = " + account_key);
	}

	/**
	 * 获取车型列表
	 * 
	 * @param cid
	 * @param prover
	 * @return
	 */
	public static CldSapReturn getCarList(int cid, String prover) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("umsaver", UMSAVER);
		map.put("rscharset", RSCHARSET);
		map.put("rsformat", RSFORMAT);
		map.put("apiver", APIVER);
		map.put("cid", cid);
		map.put("prover", prover);
		map.put("encrypt", 0);
		map.put("useid", 2);

		CldSapReturn errRes = CldKBaseParse.getGetParms(map, /*getHeadUrl()*/getOperationPlatformHeadUrl()
				+ "kgo/api/kgo_get_car_list.php", kgo_key);

		return errRes;
	}

	/**
	 * 
	 * @param appid
	 * @param bussinessid
	 * @param kuid
	 * @param session
	 * @param brand 品牌
	 * @param car_model 车型
	 * @param series 车系
	 * @param plate_num 车牌号
	 * @param frame_num 车架号后6位
	 * @param engine_num 发动机号后 6 位
	 * @return
	 */
	public static CldSapReturn bindCarInfo(int appid, int bussinessid,
			long kuid, long duid, String session, String brand,
			String car_model, String series, String plate_num,
			String frame_num, String engine_num) {

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("apiver", APIVER);
		map.put("umsaver", UMSAVER);
		map.put("rscharset", RSCHARSET);
		map.put("rsformat", RSFORMAT);
		map.put("appid", appid);
		map.put("kuid", kuid);
		map.put("duid", duid);
		map.put("session", session);
		map.put("bussinessid", bussinessid);
		map.put("brand", brand);
		map.put("car_model", car_model);
		map.put("series", series);
		map.put("plate_num", plate_num);
		map.put("frame_num", frame_num);
		map.put("engine_num", engine_num);

		CldSapReturn errRes = CldKBaseParse.getPostParms(map,
				CldSapUtil.getNaviSvrKA() + "kaccount_user_bind_carinfo.php",
				account_key);

		return errRes;
	}

	public static CldSapReturn unbindCarInfo(int appid, long duid) {

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("apiver", APIVER);
		map.put("umsaver", UMSAVER);
		map.put("rscharset", RSCHARSET);
		map.put("rsformat", RSFORMAT);
		map.put("appid", appid);
		map.put("duid", duid);

		CldSapReturn errRes = CldKBaseParse.getPostParms(map,
				CldSapUtil.getNaviSvrKA() + "kaccount_user_unbind_carinfo.php",
				account_key);

		return errRes;
	}
	
	/**
	 * 
	 * @param appid
	 * @param bussinessid
	 * @param kuid
	 * @param session
	 * @param carid 唯一id
	 * @param brand 品牌
	 * @param car_model 车型
	 * @param series 车系
	 * @param plate_num 车牌号
	 * @param frame_num 车架号后6位
	 * @param engine_num 发动机号后 6 位
	 * @return
	 */
	public static CldSapReturn updateCarInfo(int appid, long duid,
			String brand, String car_model, String series, String plate_num,
			String frame_num, String engine_num) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("apiver", APIVER);
		map.put("umsaver", UMSAVER);
		map.put("rscharset", RSCHARSET);
		map.put("rsformat", RSFORMAT);
		map.put("appid", appid);
		map.put("duid", duid);
		CldSapParser.putStringToMap(map, "brand", brand);
		CldSapParser.putStringToMap(map, "car_model", car_model);
		CldSapParser.putStringToMap(map, "series", series);
		CldSapParser.putStringToMap(map, "plate_num", plate_num);
		CldSapParser.putStringToMap(map, "frame_num", frame_num);
		CldSapParser.putStringToMap(map, "engine_num", engine_num);

		CldSapReturn errRes = CldKBaseParse.getPostParms(map,
				CldSapUtil.getNaviSvrKA() + "kaccount_user_update_carinfo.php",
				account_key);

		return errRes;
	}
	
	/**
	 * 检查登录session
	 * @param appid
	 * @param cid
	 * @param prover
	 * @param bussinessid
	 * @param kuid
	 * @param session
	 * @return
	 */
	public static CldSapReturn checkSessionInvaild(int appid, int cid, String prover,
			int bussinessid, long kuid, String session) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("umsaver", UMSAVER);
		map.put("rscharset", RSCHARSET);
		map.put("rsformat", RSFORMAT);
		map.put("apiver", APIVER);
		map.put("cid", cid);
		map.put("prover", prover);
		map.put("encrypt", 0);
		map.put("appid", appid);
		map.put("kuid", kuid);
		map.put("session", session);
		map.put("bussinessid", bussinessid);
		
		CldSapReturn errRes = CldKBaseParse.getGetParms(map,
				CldSapUtil.getNaviSvrKA() + "kaccount_check_user_login.php",
				account_key);

		return errRes;
	}

	// ------------------- 流量接口 --------------------
	@SuppressWarnings("rawtypes")
	private static String getPostParms(Map<String, Object> map, String urlHead, String key) {
		String url = "";
		String urlSource = "";
		String md5Source = "";
		
		if (null != map) {
			int size = map.size();
			String[] parms = new String[size];
			Iterator<?> iter = map.entrySet().iterator();
			int i = 0;
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				parms[i] = (String) entry.getKey();
				i++;
			}
			CldSapParser.BubbleSort.sort(parms);

			for (i = 0; i < parms.length; i++) {
				if (i != 0) {
					if (!TextUtils.isEmpty(parms[i])) {
						if (map.get(parms[i]) != null) {
							md5Source = md5Source + "&" + parms[i] + "="
									+ map.get(parms[i]);
						}
					}
				} else if (!TextUtils.isEmpty(parms[i])) {
					if (map.get(parms[i]) != null) {
						md5Source = md5Source + parms[i] + "="
								+ map.get(parms[i]);
					}
				}
			}

			for (i = 0; i < parms.length; i++) {
				if (i != 0) {
					if (!TextUtils.isEmpty(parms[i])) {
						if (map.get(parms[i]) != null) {
							urlSource = urlSource
									+ "&"
									+ parms[i]
									+ "="
									+ CldSapUtil.getUrlEncodeString(map.get(
											parms[i]).toString());
						} else {
							urlSource = urlSource + "&" + parms[i] + "=" + "";
						}
					}
				} else {
					if (!TextUtils.isEmpty(parms[i])) {
						if (map.get(parms[i]) != null) {
							urlSource = urlSource
									+ parms[i]
									+ "="
									+ CldSapUtil.getUrlEncodeString(map.get(
											parms[i]).toString());
						} else {
							urlSource = urlSource + "&" + parms[i] + "=" + "";
						}
					}
				}
			}
		}

		if (!TextUtils.isEmpty(key)) {
			md5Source = md5Source + key;
			
			CldLog.i(TAG, "md5Source = " + md5Source);
			String sign = CldSapParser.MD5(md5Source);
			url = urlHead + urlSource + "&sign=" + sign;
		} else {
			url = urlHead + urlSource;
		}
		
		return url;
	}
	
	
	/**
	 * 获取当前流量
	 * 
	 */
	public static CldSapReturn getSimCardStatus(String iccid, String imsi, String sim, String sn, String ver) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("apiver", APIVER);
		map.put("iccid", iccid);
		map.put("imsi", imsi);
		map.put("sim", sim);
		map.put("sn", sn);
		map.put("ver", ver);

		CldSapReturn errRes = new CldSapReturn();
		errRes.url = getPostParms(map, getFlowHeadUrl() + "?mod=iov&ac=getcardstatus&", getFlowKey());
		return errRes;
	}
	
	/**
	 * 获取卡状态
	 * @param iccid
	 * @param imsi
	 * @param sim
	 * @param sn
	 * @param ver
	 * @param duid
	 * @param kuid
	 * @return
	 */
	public static CldSapReturn checkSimCard(String iccid, String imsi, String sim, String sn, String ver, long duid, long kuid) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("apiver", APIVER);
		map.put("iccid", iccid);
		map.put("imsi", imsi);
		map.put("sim", sim);
		map.put("sn", sn);
		map.put("ver", ver);
		map.put("duid", duid);
		map.put("kuid", kuid);
		
		CldSapReturn errRes = new CldSapReturn();;
		errRes.url = getPostParms(map, getFlowHeadUrl() +  "?mod=iov&ac=checkcard&", getFlowKey());		
		return errRes;
	}

	/**
	 * 服务卡登记
	 * @param iccid
	 * @param imsi
	 * @param sim
	 * @param sn
	 * @param ver
	 * @param duid
	 * @param kuid
	 * @return
	 */
	public static CldSapReturn registerSimCard(String iccid, String imsi, String sim, String sn, String ver, long duid, long kuid,
			long dcode, long pcode, long custid) {
		Map<String, Object> map = new HashMap<String, Object>();
				
		map.put("apiver", APIVER);
		map.put("iccid", iccid);
		map.put("imsi", imsi);
		map.put("sim", sim);
		map.put("sn", sn);
		map.put("ver", ver);
		map.put("duid", duid);
		map.put("kuid", kuid);
		map.put("dcode", dcode);
		map.put("pcode", pcode);
		map.put("custid", custid);
		
		CldSapReturn errRes = new CldSapReturn();
		errRes.url = getPostParms(map, getFlowHeadUrl() +  "?mod=iov&ac=registercard&", getFlowKey());	
		return errRes;
	}
	
	/**
	 * 检查续费情况
	 * @param iccid
	 * @param imsi
	 * @param sim
	 * @param sn
	 * @param ver
	 * @param duid
	 * @param kuid
	 * @return
	 */
	public static CldSapReturn checkPayStatus(String iccid, String imsi, String sim, String sn, String ver, 
			long duid, long kuid, long getordertime) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("apiver", APIVER);
		map.put("iccid", iccid);
		map.put("imsi", imsi);
		map.put("sim", sim);
		map.put("sn", sn);
		map.put("ver", ver);
		map.put("duid", duid);
		map.put("kuid", kuid);
		map.put("getordertime", getordertime);
		
		CldSapReturn errRes = new CldSapReturn();;
		errRes.url = getPostParms(map, getFlowHeadUrl() +  "?mod=iov&ac=checkcardpay&", getFlowKey());	
		return errRes;
	}
	
	private static String getFlowKey() {
		//checkcard, getcardstatus, getpaystatus, heatrbeat涉及到流量卡的接口用该key
		String key = "";
		key = "1a86fb49b070f26d7948d7931ed69233";
		
		/*if (KCloudAppUtils.isTestVersion()) {
			key = "1a86fb49b070f26d7948d7931ed69233";
		} else {
			key = "3578ff7e621432719238730de417fa76";
		}*/
		return key;
	}

	private static String getFlowHeadUrl() {
		if (KCloudAppUtils.isTestVersion()) {
			return "http://test.careland.com.cn/kldjy/www/";
		} else {
			return "http://navione.careland.com.cn/";
		}
	}

	/**
	 * 获取闪屏、tips下载地址
	 * @param apptype
	 * @param prover
	 * @param length
	 * @param width
	 * @return
	 */
	public static CldSapReturn getSplashInfomation(int apptype, String prover,
			int width, int height) {
		String key = "";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("apptype", apptype);
		map.put("prover", prover);
		map.put("length", width);
		map.put("width", height);

		if (KCloudAppUtils.isTestVersion()) {
			key = "F4A41A19D58AE8F7B7306FD30CB3F3FA";
		} else {
			key = "BB2CC2F71F01DF39A156E4F4FE56FEE5";
		}
		
		CldSapReturn errRes = CldKBaseParse.getGetParms(map, /*getHeadUrl()*/getOperationPlatformHeadUrl()
				+ "kgo/api/get_logo_tips_url.php", key);

		return errRes;
	}

	/*private static String getHeadUrl() {
		if (KCloudAppUtils.isTestVersion()) {
			return "http://tmctest.careland.com.cn/";
		} else {
			return "http://st.careland.com.cn/";
		}
	}*/
	
	/**
	 * 
	 * @Title: getOperationPlatformHeadUrl
	 * @Description: 获取运营平台域名(套餐相关)
	 * @return stat.careland.com.cn 
	 * @return: String
	 */
	private static String getOperationPlatformHeadUrl() {
		if (KCloudAppUtils.isTestVersion()) {
			return "http://tmctest.careland.com.cn/";
		} else {
			return "http://stat.careland.com.cn/";
		}
	}
	
	/**
	 * 
	 * @Title: getAccountPlatformHeadUrl
	 * @Description: 获取账号平台域名(账号、消息相关)
	 * @return
	 * @return: String
	 */
	private static String getAccountPlatformHeadUrl() {
		if (KCloudAppUtils.isTestVersion()) {
			return "http://tmctest.careland.com.cn/";
		} else {
			return "http://st.careland.com.cn/";
		}
	}

	public static CldSapReturn initPlatformKeyCode(int cid, String prover) {
		String key = "";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("umsaver", UMSAVER);
		map.put("rscharset", RSCHARSET);
		map.put("rsformat", RSFORMAT);
		map.put("apiver", APIVER);
		map.put("cid", cid);
		map.put("prover", prover);

		if (KCloudAppUtils.isTestVersion()) {
			key = "373275EB226022907CCA40BD2AE481D8";
		} else {
			key = "373275EB226022907CCA40BD2AE481D8";
		}

		CldSapReturn errRes = CldKBaseParse.getGetParms(map, /*getHeadUrl()*/getOperationPlatformHeadUrl()
				+ "kgo/api/kgo_get_code.php", key);

		return errRes;
	}
	
	public static CldSapReturn getAlarmSetting(int combo_code, int cid, String prover) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("umsaver", UMSAVER);
		map.put("rscharset", RSCHARSET);
		map.put("rsformat", RSFORMAT);
		map.put("apiver", APIVER);
		map.put("combo_code", combo_code);
		map.put("cid", cid);
		map.put("prover", prover);
		
		CldSapReturn errRes = CldKBaseParse.getGetParms(map, /*getHeadUrl()*/getOperationPlatformHeadUrl()
				+ "kgo/api/kgo_get_combo_alarm_setting.php", kgo_key);
		
		return errRes;
	}

	/**
	 * 
	 * @param system 操作系统编码
	 * @param device 设备型号编码
	 * @param launcher Launcher版本号
	 * @param kuid 用户kuid
	 * @param session 登录Session
	 * @param appid 账号系统分配
	 * @param bussinessid 业务编码
	 * @param cid
	 * @param prover
	 * @return
	 */
	public static CldSapReturn getUserPackageList(int system, int device, int product,
			String launcher, long kuid, String session, int appid,
			int bussinessid, int cid, String prover, int width, int height, String iccid) {
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("system_code", system);
		map.put("device_code", device);
		map.put("product_code", product);
		map.put("launcher_ver", launcher);
		map.put("kuid", kuid);
		map.put("appid", appid);
		map.put("bussinessid", bussinessid);
		map.put("session", session);
		map.put("umsaver", UMSAVER);
		map.put("rscharset", RSCHARSET);
		map.put("rsformat", RSFORMAT);
		map.put("apiver", APIVER);
		map.put("cid", cid);
		map.put("prover", prover);
		map.put("encrypt", 0);
		map.put("width", width);
		map.put("height", height);
		map.put("iccid", iccid);

		CldSapReturn errRes = CldKBaseParse.getGetParms(map, /*getHeadUrl()*/getOperationPlatformHeadUrl()
				+ "kgo/api/kgo_get_user_combo_list.php", kgo_key);

		return errRes;
	}

	public static CldSapReturn getUserServicesAppList(int appid, int bussinessid,
			int cid, long kuid, String session, String iccid, String prover) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("iccid", iccid);
		map.put("kuid", kuid);
		map.put("appid", appid);
		map.put("bussinessid", bussinessid);
		map.put("session", session);
		map.put("umsaver", UMSAVER);
		map.put("rscharset", RSCHARSET);
		map.put("rsformat", RSFORMAT);
		map.put("apiver", APIVER);
		map.put("cid", cid);
		map.put("prover", prover);
		map.put("encrypt", 0);

		CldSapReturn errRes = CldKBaseParse.getGetParms(map, /*getHeadUrl()*/getOperationPlatformHeadUrl()
				+ "kgo/api/kgo_get_service_app.php", kgo_key);

		return errRes;
	}
	
	/**
	 * 
	 * @Title: getUpgradeList
	 * @Description: 获取升级应用列表
	 * @param cid
	 * @param prover
	 * @return
	 * @return: CldSapReturn
	 */
	public static CldSapReturn getUpgradeList(String launcher, int regionId, 
			ArrayList<KCloudInstalledInfo> installedInfoList) 
	{
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("umsaver", UMSAVER);
		map.put("rscharset", RSCHARSET);
		map.put("rsformat", RSFORMAT);
		map.put("apiver", APIVER);
		map.put("encrypt", 0);
		map.put("page", 0);
		map.put("size", 0);
		map.put("area_code", regionId); //先定位，获取区域id
		map.put("launcher_ver", launcher);
		
		map.put("cid", KCloudAppConfig.cid);
		map.put("prover", KCloudAppConfig.appver);
		map.put("system_code", KCloudAppConfig.system_code);
		map.put("device_code", KCloudAppConfig.device_code);
		map.put("product_code", KCloudAppConfig.product_code);
		map.put("width", KCloudAppConfig.device_width);
		map.put("height", KCloudAppConfig.device_height);
		map.put("custom_code", KCloudAppConfig.custom_code);
		map.put("duid", CldKAccountAPI.getInstance().getDuid());
		map.put("kuid", CldKAccountAPI.getInstance().getKuid());
		
		/**
		 * 可配置测试
		 */
		map.put("system_ver", KCloudCommonUtil.getSystemVer()); //android系统版本
		map.put("plan_code", KCloudAppConfig.plan_code);        //方案商编号
		map.put("dpi", KCloudAppConfig.device_dpi);             //设备分辨率
		//appInfoList不加入计算sign
		String mymd5 = KCloudBaseParse.formatSource(map);
		
		List<Map<String, Object>> apllist = new ArrayList<Map<String,Object>>();
		Map<String, Object> mapApp = null;
		for (KCloudInstalledInfo item : installedInfoList) 
		{
			mapApp = new HashMap<String, Object>();
			mapApp.put("packname", item.getPkgName());
			mapApp.put("vercode", "" + item.getVerCode());
			apllist.add(mapApp);
		}
		map.put("install_app", apllist);
		
		CldSapReturn errRes = KCloudBaseParse.getPostParms(map, /*getHeadUrl()*/getOperationPlatformHeadUrl()
				+ "kgo/api/kgo_get_app_upgrade.php", kgo_key, mymd5);
		
		return errRes;
	}
	
	/**
	 * 
	 * @Title: checkHeartbeat
	 * @Description: 检测心跳
	 * @param iccid
	 * @param sim
	 * @param sn
	 * @param ver
	 * @param update
	 * @return
	 * @return: CldSapReturn
	 */
	public static CldSapReturn checkHeartbeat(String iccid, String sim, String sn, 
			String ver, long update) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("apiver", APIVER);
		map.put("iccid", iccid);
		map.put("sim", sim);
		map.put("sn", sn);
		map.put("ver", ver);
		map.put("update", update + "");
		
		CldSapReturn errRes = new CldSapReturn();;
		errRes.url = getPostParms(map, getFlowHeadUrl() + "?mod=iov&ac=checkheart&", getFlowKey());	
		return errRes;
	}

	public static void setKgoKey(String key) {
		kgo_key = key;
		CldLog.i(TAG, "kgo_key = " + kgo_key);
	}

	public static boolean isGetSign() {
		if (!kgo_key.isEmpty())
			return true;

		return false;
	}	
}
