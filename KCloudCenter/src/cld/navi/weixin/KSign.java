/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: KSign.java
 * @Prject: KCloudCenter_M550
 * @Package: cld.navi.weixin
 * @Description: java版计算sign签名
 * @author: zhaoqy
 * @date: 2016年8月15日 上午9:52:40
 * @version: V1.0
 */

package cld.navi.weixin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.UnsupportedEncodingException;
import java.security.SignatureException;
import org.apache.commons.codec.digest.DigestUtils;

public class KSign {

    /**
     * 生成签名结果
     * @param sArray 要签名的数组
	 * @param token token接入密钥
     * @return 签名结果字符丿
     */
	public static String make_sign(Map<String, String> sArray, String token) {
		// 除去数组中的空急Ҍ签名参敿
		Map<String, String> sPara = paraFilter(sArray);

		String prestr = createLinkString(sPara); // 把数组所有元素，按照“参敿参数值〧ڄ模式用‿”字符拼接成字符
		prestr = prestr + token; // 把拼接后的字符串再与安全校验码直接连接起板
		String mysign = md5(prestr);
		return mysign;
	}

    /** 
     * 除去数组中的空急Ҍ签名参敿
     * @param sArray 签名参数绿
     * @return 去掉空怤؎签名参数后的新签名参数绿
     */
	public static Map<String, String> paraFilter(Map<String, String> sArray) {
		Map<String, String> result = new HashMap<String, String>();

		if (sArray == null || sArray.size() <= 0) {
			return result;
		}

		for (String key : sArray.keySet()) {
			String value = sArray.get(key);
			if (value == null || value.equals("")
					|| key.equalsIgnoreCase("sign")
					|| key.equalsIgnoreCase("callback")) {
				continue;
			}
			result.put(key, value);
		}

		return result;
	}

    /** 
     * 把数组所有元素排序，并按照〥ς数=参数值〧ڄ模式用‿”字符拼接成字符丿
     * @param params 霿Ɓ排序并参与字符拼接的参数绿
     * @return 拼接后字符串
     */
	public static String createLinkString(Map<String, String> params) {
		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);

		String prestr = "";

		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = params.get(key);

			if (i == keys.size() - 1) {// 拼接时，不包括最后一丿字符
				prestr = prestr + key + "=" + value;
			} else {
				prestr = prestr + key + "=" + value + "&";
			}
		}

		return prestr;
	}
	
	/**
     * 对字符串进行MD5签名
     * @param text 明文
     * @return 密文
     */
    public static String md5(String text) {
        return DigestUtils.md5Hex(getContentBytes(text, "UTF-8"));
    }

    /**
     * @param content 文本内容
     * @param charset 文本编码
     * @return 返回文本的字节数
     * @throws SignatureException
     * @throws UnsupportedEncodingException 
     */
	private static byte[] getContentBytes(String content, String charset) {
		if (charset == null || "".equals(charset)) {
			return content.getBytes();
		}

		try {
			return content.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("MD5签名过程中出现错诿指定的编码集不对,您目前指定的编码集是:"
					+ charset);
		}
	}
}
