package cld.weather.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;

import com.google.gson.Gson;

import android.text.TextUtils;
import android.util.Log;

public class KSign {
	/**
	 * 获取Get方法拼接的URL
	 * 
	 * @param map
	 * @param urlHead
	 * @param key
	 * @return
	 * @return ProtReturn
	 * @author Zhouls
	 * @date 2015-4-3 上午11:30:06
	 */
	public static CldSapReturn getGetParms(Map<String, Object> map,
			String urlHead, String key) {
		String urlSource = "";
		String md5Source = "";
		if (null != map) {
			md5Source = formatSource(map);
			urlSource = formatUrlSource(map);
		}
		if (!TextUtils.isEmpty(key)) {
			md5Source += key;
		}
		String sign = MD5(md5Source);
		Log.i("ols", md5Source);
		String strUrl = urlHead + "?" + urlSource + "&sign=" + sign;
		//Log.i("ols", strUrl);
		CldSapReturn errRes = new CldSapReturn();
		errRes.url = strUrl;
		return errRes;
	}

	/**
	 * url 不统一转码
	 * 
	 * @param map
	 * @param urlHead
	 * @param key
	 * @return
	 * @return CldSapReturn
	 * @author Zhouls
	 * @date 2015-7-9 下午4:36:36
	 */
	public static CldSapReturn getGetParmsNoEncode(Map<String, Object> map,
			String urlHead, String key) {
		String urlSource = "";
		String md5Source = "";
		if (null != map) {
			md5Source = formatSource(map);
			urlSource = md5Source;
		}
		if (!TextUtils.isEmpty(key)) {
			md5Source += key;
		}
		String sign = MD5(md5Source);
		String strUrl = urlHead + "?" + urlSource + "&sign=" + sign;
		CldSapReturn errRes = new CldSapReturn();
		errRes.url = strUrl;
		return errRes;
	}

	/**
	 * 获取Post方法拼接的URL
	 * 
	 * @param map
	 * @param urlHead
	 * @param key
	 * @return
	 * @return ProtReturn
	 * @author Zhouls
	 * @date 2015-4-3 上午11:42:43
	 */
	public static CldSapReturn getPostParms(Map<String, Object> map,
			String urlHead, String key) {
		CldSapReturn errRes = new CldSapReturn();
		if (null != map) {
			/**
			 * map不为空
			 */
			String urlSource = formatSource(map);
			//Log.d("fbh","urlsource"+urlSource);
			String md5Source = urlSource;
			if (!TextUtils.isEmpty(key)) {
				md5Source += key;
			}
			Log.d("md5Source", "md5Source: " + md5Source);
			String sign = MD5(md5Source);
			map.put("sign", sign);
			String strPost = mapToJson(map);
			errRes.url = urlHead;
			errRes.jsonPost = strPost;
		}
		return errRes;
	}
	
	public static CldSapReturn getPostParms(Map<String, Object> map,
			String urlHead, String key, String myMd5Source) {
		CldSapReturn errRes = new CldSapReturn();
		if (null != map) {
			/**
			 * map不为空
			 */
//			String urlSource = formatSource(map);
			//Log.d("fbh","urlsource"+urlSource);
			String md5Source = (myMd5Source != null) ? myMd5Source: formatSource(map);
			if (!TextUtils.isEmpty(key)) {
				md5Source += key;
			}
			Log.d("md5Source", "md5Source: " + md5Source);
			String sign = MD5(md5Source);
			map.put("sign", sign);
			String strPost = mapToJson(map);
			errRes.url = urlHead;
			errRes.jsonPost = strPost;
		}
		return errRes;
	}
	
	/**
	 * 获取协议层md5Source串
	 * 
	 * @param map
	 * @return
	 * @return String
	 * @author Zhouls
	 * @date 2015-3-18 上午9:54:09
	 */
	@SuppressWarnings("rawtypes")
	public static String formatSource(Map<String, Object> map) {
		if (null != map) {
			int size = map.size();
			String[] parms = new String[size];
			Iterator<?> iter = map.entrySet().iterator();
			int i = 0;
			String md5Source = "";
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				parms[i] = (String) entry.getKey();
				i++;
			}
			BubbleSort.sort(parms);
			for (i = 0; i < parms.length; i++) {
				if (i != 0) {
					if (!TextUtils.isEmpty(parms[i])) {
						md5Source = md5Source + "&" + parms[i] + "="
								+ map.get(parms[i]);
					}
				} else {
					if (!TextUtils.isEmpty(parms[i])) {
						md5Source = md5Source + parms[i] + "="
								+ map.get(parms[i]);
					}
				}
			}
			return md5Source;
		} else {
			return "";
		}
	}
	
	/**
	 * 获取URL请求参数
	 * 
	 * @param map
	 *            请求参数集合
	 * @return
	 * @return String
	 * @author Zhouls
	 * @date 2015-6-23 上午9:20:46
	 */
	@SuppressWarnings("rawtypes")
	public static String formatUrlSource(Map<String, Object> map) {
		if (null != map) {
			int size = map.size();
			String[] parms = new String[size];
			Iterator<?> iter = map.entrySet().iterator();
			int i = 0;
			String md5Source = "";
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				parms[i] = (String) entry.getKey();
				i++;
			}
			BubbleSort.sort(parms);
			for (i = 0; i < parms.length; i++) {
				if (i != 0) {
					if (!TextUtils.isEmpty(parms[i])) {
						md5Source = md5Source
								+ "&"
								+ parms[i]
								+ "="
								+ getUrlEncodeString(map.get(
										parms[i]).toString());
					}
				} else {
					if (!TextUtils.isEmpty(parms[i])) {
						md5Source = md5Source
								+ parms[i]
								+ "="
								+ getUrlEncodeString(map.get(
										parms[i]).toString());
					}
				}
			}
			return md5Source;
		} else {
			return "";
		}
	}
	
	/**
	 * 转码
	 * 
	 * @param urlDecodeStr
	 *            the url decode str
	 * @return String
	 * @author Zhouls
	 * @date 2014-8-28 上午10:26:54
	 */
	public static String getUrlEncodeString(String urlDecodeStr) {
		String encodeStr = "";
		try {
			if (!TextUtils.isEmpty(urlDecodeStr)) {
				encodeStr = URLEncoder.encode(urlDecodeStr, "utf-8");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return encodeStr;
	}
	
	/**
	 * MD5加密
	 * 
	 * @param sourceStr
	 *            the source str
	 * @return String
	 * @author Zhouls
	 * @date 2014-8-13 下午2:39:21
	 */
	public static String MD5(String sourceStr) {
		String result = "";
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(sourceStr.getBytes());
			byte b[] = md.digest();
			int i;
			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}
			result = buf.toString();
		} catch (NoSuchAlgorithmException e) {
			System.out.println(e);
		}
		return result;
	}
	
	/**
	 * 将map反序列化为json字符串
	 * 
	 * @param map
	 *            参数表
	 * @return
	 * @return String
	 * @author Zhouls
	 * @date 2015-3-18 下午3:56:16
	 */
	public static <T> String mapToJson(Map<String, T> map) {
		Gson gson = new Gson();
		String jsonStr = gson.toJson(map);
		return jsonStr;
	}
	
	/**
	 * 
	 * 冒泡排序
	 * 
	 * @author Zhouls
	 * @date 2015-3-18 上午8:55:13
	 */
	public static class BubbleSort {

		/**
		 * 冒泡排序按Ascii码大小增序排序（循环数组大小次，每次将最大的放到最后）
		 * 
		 * @param array
		 *            排序数组
		 * @return void
		 * @author Zhouls
		 * @date 2015-3-18 上午8:55:24
		 */
		public static void sort(String[] array) {
			if (null != array) {
				for (int i = 1; i < array.length; i++) {
					for (int j = 0; j < array.length - i; j++) {
						if (compare(array[j + 1], array[j])) {
							String temp = array[j];
							array[j] = array[j + 1];
							array[j + 1] = temp;
						}
					}
				}
			}
		}

		/**
		 * 2个字符串比较Ascii 码大小
		 * 
		 * @param strA
		 * @param strB
		 * @return 若A<B 返回true 否则返回false
		 * @return boolean
		 * @author Zhouls
		 * @date 2015-3-18 上午8:55:45
		 */
		public static boolean compare(String strA, String strB) {
			char[] a = strA.toCharArray();
			char[] b = strB.toCharArray();
			int cycNum = a.length < b.length ? a.length : b.length;
			for (int i = 0; i < cycNum; i++) {
				if (a[i] == b[i]) {
					continue;
				} else {
					if (a[i] < b[i]) {
						return true;
					} else {
						return false;
					}
				}
			}
			if (a.length != b.length) {
				/**
				 * 一个是另一个的前缀
				 */
				return false;
			} else {
				/**
				 * 2个字符串完全相等
				 */
				return true;
			}
		}
	}
	
}
