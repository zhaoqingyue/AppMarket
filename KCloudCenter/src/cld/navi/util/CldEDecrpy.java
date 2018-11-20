/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: CldEDecrpy.java
 * @Prject: KCloudCenter_M550
 * @Package: cld.navi.util
 * @Description: 密钥加密 解密
 * @author: zhaoqy
 * @date: 2016年8月11日 下午3:49:35
 * @version: V1.0
 */

package cld.navi.util;

public class CldEDecrpy {

	private String str;
	private int[] keyBox;

	/**
	 * Instantiates a new cld e decrpy.
	 * @param str
	 * @param key
	 */
	public CldEDecrpy(String str, String key) {
		this.str = str;
		keyBox = new int[3];
		keyBox[0] = Integer.parseInt(key.substring(0, 2));
		keyBox[1] = Integer.parseInt(key.substring(2, 4));
		keyBox[2] = Integer.parseInt(key.substring(4, 6));
	}

	/**
	 * 
	 * @Title: encrypt
	 * @Description: 加密
	 * @return
	 * @return: String
	 */
	public String encrypt() {
		String enStr = "";
		int tempKey;
		for (int i = 0; i < str.length(); i++) {
			tempKey = keyBox[i % 3] % 24;
			enStr = enStr + getChEncrpyt(getIndexEncrpyt(str.charAt(i)) + tempKey);
		}
		return enStr;
	}

	/**
	 * 
	 * @Title: decrypt
	 * @Description: 解密
	 * @return
	 * @return: String
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
	 * 
	 * @Title: getChEncrpyt
	 * @Description: 获取加密字符
	 * @param i
	 * @return
	 * @return: char
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
	 * 
	 * @Title: getChDecrpyt
	 * @Description: 获取解密字符
	 * @param i
	 * @return
	 * @return: char
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
	 * 
	 * @Title: getIndexDecrpyt
	 * @Description: 获取加密索引
	 * @param c
	 * @return
	 * @return: int
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
	 * 
	 * @Title: getIndexEncrpyt
	 * @Description: 获取解密索引
	 * @param c
	 * @return
	 * @return: int
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
	 * 
	 * @Title: chr
	 * @Description: int转char
	 * @param i
	 * @return
	 * @return: char
	 */
	private char chr(int i) {
		char c = (char) i;
		return c;
	}

	/**
	 * 
	 * @Title: ord
	 * @Description: char转int
	 * @param c
	 * @return
	 * @return: int
	 */
	private int ord(char c) {
		int a = c;
		return a;
	}
}
