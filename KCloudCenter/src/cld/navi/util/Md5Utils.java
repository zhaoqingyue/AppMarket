/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: Md5Utils.java
 * @Prject: KCloudCenter_M550
 * @Package: cld.navi.util
 * @Description: MD5值
 * @author: zhaoqy
 * @date: 2016年8月11日 下午3:55:02
 * @version: V1.0
 */

package cld.navi.util;

import java.security.MessageDigest;
import java.util.Arrays;
import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

@SuppressLint("DefaultLocale") 
public class Md5Utils {
	/**
	 * 
	 * @Title: MD5
	 * @Description: 获取字符串的MD5值
	 * @param string
	 * @return: String
	 */
	public final static String MD5(String string) {
		try {
			byte[] btInput = string.getBytes();
			// 获得MD5摘要算法的 MessageDigest 对象 
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			// 使用指定的字节更新摘要 
			mdInst.update(btInput);
			// 获得密文 
			byte[] md = mdInst.digest();
			Log.w("js", "md-size===" + md.length);
			return bytes2String(md);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @Title: bytes2String
	 * @Description: 获取Byte数组的16进制表示
	 * @param bytes
	 * @return
	 * @return: String
	 */
	public static String bytes2String(byte[] bytes) {
		if (bytes == null) {
			return "bytes is null";
		}
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };
		// 把密文转换成十六进制的字符串形式 
		char str[] = new char[bytes.length * 2];
		for (int i = 0, k = 0; i < bytes.length; i++) {
			byte byte0 = bytes[i];
			str[k++] = hexDigits[byte0 >>> 4 & 0xf];
			str[k++] = hexDigits[byte0 & 0xf];
		}

		return new String(str).toLowerCase();
	}

	/**
	 * @Title: decodeKey
	 * @Description: 解密密钥 
	 * @param keyCode
	 * @return
	 * @return: String
	 */
	@SuppressLint("DefaultLocale") 
	public static String decodeKey(String keyCode) { 
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
	 * 
	 * @Title: sortParam
	 * @Description: 对MD5加密的参数进行排序
	 * @param param
	 * @return
	 * @return: String
	 */
	public static String sortParam(String param)
	{
		if(param==null || "".equals(param)) 
			return null;

		StringBuilder sb = new StringBuilder();
		String params[] = param.split("&");
		Arrays.sort(params);
		int len = params.length;
		for(int i=0;i<len;i++)
		{
			sb.append(params[i]);
			if(i!=len-1)
				sb.append('&');
		}
		return sb.toString();
	}
}
