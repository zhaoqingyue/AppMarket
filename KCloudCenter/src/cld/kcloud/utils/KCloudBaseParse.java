package cld.kcloud.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import com.cld.ols.tools.CldSapReturn;
import com.google.gson.Gson;
import android.text.TextUtils;
import android.util.Log;

public class KCloudBaseParse 
{
	public static CldSapReturn getPostParms(Map<String, Object> map,
			String urlHead, String key, String myMd5Source) {
		CldSapReturn errRes = new CldSapReturn();
		if (null != map) {
			/**
			 * map��Ϊ��
			 */
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
	 * ��map�����л�Ϊjson�ַ���
	 * @param map ������
	 * @return String
	 */
	public static <T> String mapToJson(Map<String, T> map) {
		Gson gson = new Gson();
		String jsonStr = gson.toJson(map);
		return jsonStr;
	}
	
	
	/**
	 * ��ȡЭ���md5Source��
	 * @param map
	 * @return String
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
	 * ��ȡURL�������
	 * @param map �����������
	 * @return String
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
	 * ת��
	 * @param urlDecodeStr the url decode str
	 * @return String
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
	 * MD5����
	 * @param sourceStr the source str
	 * @return String
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
	 * ð������
	 */
	public static class BubbleSort {

		/**
		 * ð������Ascii���С��������ѭ�������С�Σ�ÿ�ν����ķŵ����
		 * @param array ��������
		 * @return void
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
		 * 2���ַ����Ƚ�Ascii ���С
		 * @param strA
		 * @param strB
		 * @return ��A<B ����true ���򷵻�false
		 * @return boolean
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
				 * һ������һ����ǰ׺
				 */
				return false;
			} else {
				/**
				 * 2���ַ�����ȫ���
				 */
				return true;
			}
		}
	}
}
