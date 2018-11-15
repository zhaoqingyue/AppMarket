/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: RegionUtils.java
 * @Prject: KMarcket_M550
 * @Package: cld.kmarcket.util
 * @Description: 区域定位
 * @author: zhaoqy
 * @date: 2016年8月25日 上午9:48:37
 * @version: V1.0
 */

package cld.kmarcket.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.text.TextUtils;
import android.util.Log;

public class RegionUtils {

	static public interface IGetRigonCallback {
		public void onResult(int regionId, String provinceName,
				String cityName, String distsName);
	}

	static public void getRegionDistsName(final double longitude,
			final double latitude, final IGetRigonCallback callback) {
		if (callback == null) {
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				long newLong = (int) (longitude * 3600000);
				long newLat = (int) (latitude * 3600000);
				String params = "&p=" + newLong + "+" + newLat;
				String url = "http://navitest1.careland.com.cn/cgi/pub_getpositioninfo_j.ums?d=1&ct=1"
						+ params;
				Log.d("fbh", "url location:" + url);
				String json = RegionUtils.sapGetMethod(url);
				Log.d("fbh", "json location:" + json);
				int regionCityId = getCityIdJson(json);
				String provinceName = getNameByJson(json, 1);
				String cityName = getNameByJson(json, 2);
				String distsName = getNameByJson(json, 3);
				if (callback != null)
					callback.onResult(regionCityId, provinceName, cityName,
							distsName);
			}
		}).start();
	}

	static public void getRegionId(final double longitude,
			final double latitude, final IGetRigonCallback callback) {
		if (callback == null)
			return;

		new Thread(new Runnable() {

			@Override
			public void run() {
				long newLong = (int) (longitude * 3600000);
				long newLat = (int) (latitude * 3600000);
				String params = "&p=" + newLong + "+" + newLat;
				String url = "http://navitest1.careland.com.cn/cgi/pub_getpositioninfo_j.ums?d=1&ct=1"
						+ params;
				Log.d("fbh", "url location:" + url);
				String json = RegionUtils.sapGetMethod(url);
				Log.d("fbh", "json location:" + json);
				int regionCityId = getCityIdJson(json);
				if (callback != null)
					callback.onResult(regionCityId, "", "", "");
			}
		}).start();
	}

	static String getNameByJson(String jsonStr, int level) {
		if (jsonStr == null) {
			return "";
		}

		String distsName = "";
		try {
			JSONObject jsonObject = new JSONObject(jsonStr);
			JSONArray jsonArray = null;
			if (jsonObject.has("dists")) {
				jsonArray = jsonObject.getJSONArray("dists");
			}

			if (jsonArray != null) {
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonchild = jsonArray.getJSONObject(i);

					int result = jsonchild.getInt("l");
					if (result != level)
						continue;

					if (!jsonchild.has("n"))
						continue;

					distsName = jsonchild.getString("n");
					break;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return distsName;
	}

	static int getCityIdJson(String jsonStr) {
		if (jsonStr == null)
			return 0;

		int regionCity = 0;
		try {
			JSONObject jsonObject = new JSONObject(jsonStr);
			JSONArray jsonArray = null;
			if (jsonObject.has("dists")) {
				jsonArray = jsonObject.getJSONArray("dists");
			}

			if (jsonArray == null)
				return regionCity;

			for (int i = 0; i < jsonArray.length(); ++i) {
				JSONObject jsonchild = jsonArray.getJSONObject(i);
				if (!jsonchild.has("l"))
					continue;

				int result = jsonchild.getInt("l");
				if (result != 2)
					continue;

				if (jsonchild.has("id")) {
					regionCity = jsonchild.getInt("id");
					return regionCity;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return regionCity;
	}

	/**
	 * Http Get鏂规硶
	 * 
	 * @param strUrl
	 *            锛坓et鏂规硶鍥犱负鏄痷rl甯︽湁鐗规畩绗﹀彿锛屼笉鑳界粺涓?浆鐮侊紝鍙兘鍗曚釜杞爜锛?
	 * @return
	 * @return String
	 * @author Zhouls
	 * @date 2015-4-8 涓嬪崍2:40:48
	 */
	public static String sapGetMethod(String strUrl) {
		HttpURLConnection conn = null;
		InputStream inputStream = null;
		try {
			if (!TextUtils.isEmpty(strUrl)) {
				URL url = new URL(strUrl);
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(15000);
				conn.setReadTimeout(15000);
				conn.setRequestMethod("GET");
				conn.addRequestProperty("Accept-Encoding", "gzip");
				int recode = conn.getResponseCode();
				if (recode == 200) {
					inputStream = conn.getInputStream();
					return getJsonFromGZIP(inputStream);
				}
			} else {
				Log.e("[ols]", "url is empty!");
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
					inputStream = null;
				}
			} catch (IOException e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * Http Post 鏂规硶
	 * 
	 * @param strUrl
	 *            the str url
	 * @param strPost
	 *            Post鏄痡son鍙互鍋氱粺涓?浆鐮佸鐞嗭紙鐗规畩瀛楁涓嶇敤涓枃杞爜锛?
	 * @return String
	 * @author Zhouls
	 * @date 2014-9-2 涓婂崍11:06:45
	 */
	public static String sapPostMethod(String strUrl, String strPost) {
		try {
			if (!TextUtils.isEmpty(strUrl) && !TextUtils.isEmpty(strPost)) {
				HttpPost request = new HttpPost(strUrl);
				request.addHeader("Accept-Encoding", "gzip");
				StringEntity strEntity = new StringEntity(strPost, HTTP.UTF_8);
				request.setEntity(strEntity);
				HttpResponse httpResponse = new DefaultHttpClient()
						.execute(request);
				InputStream inputStream = httpResponse.getEntity().getContent();
				return getJsonFromGZIP(inputStream);
			} else {
				Log.e("[ols]", "url is empty!");
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 浠嶨ZIP涓幏鍙朖son鏁版嵁
	 * 
	 * @param is
	 * @return
	 * @return String
	 * @author Zhouls
	 * @date 2015-4-8 涓嬪崍2:43:13
	 */
	private static String getJsonFromGZIP(InputStream is) {
		String jsonString = null;
		try {
			BufferedInputStream bis = new BufferedInputStream(is);
			bis.mark(2);
			// 鍙栧墠涓や釜瀛楄妭
			byte[] header = new byte[2];
			int result = bis.read(header);
			// reset杈撳叆娴佸埌寮?浣嶇疆
			bis.reset();
			// 鍒ゆ柇鏄惁鏄疓ZIP鏍煎紡
			int headerData = getShort(header);
			// Gzip 娴?鐨勫墠涓や釜瀛楄妭鏄?0x1f8b
			if (result != -1 && headerData == 0x1f8b) {
				is = new GZIPInputStream(bis);
			} else {
				is = bis;
			}
			InputStreamReader reader = new InputStreamReader(is, "utf-8");
			char[] data = new char[100];
			int readSize;
			StringBuffer sb = new StringBuffer();
			while ((readSize = reader.read(data)) > 0) {
				sb.append(data, 0, readSize);
			}
			jsonString = sb.toString();
			bis.close();
			reader.close();
			Log.i("ols", jsonString);
			return jsonString;
		} catch (Exception e) {
			/**
			 * 缃戠粶寮傚父锛屽嚭鐜癹sonSting 涓篘ull鎯呭喌
			 */
			Log.e("[ols]", "net_null");
			return null;
		}
	}

	/**
	 * Gets the short.
	 * 
	 * @param data
	 *            the data
	 * @return the short
	 */
	private static int getShort(byte[] data) {
		return (data[0] << 8) | data[1] & 0xFF;
	}
}
