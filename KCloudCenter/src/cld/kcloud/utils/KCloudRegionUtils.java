package cld.kcloud.utils;

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
import com.cld.log.CldLog;
import android.text.TextUtils;

public class KCloudRegionUtils {
	private final static String TAG = "KCloudRegionUtils";
	
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
				CldLog.d(TAG, "url location:" + url);
				String json = KCloudRegionUtils.sapGetMethod(url);
				CldLog.d(TAG, "json location:" + json);
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
				CldLog.d(TAG, "url location:" + url);
				String json = KCloudRegionUtils.sapGetMethod(url);
				CldLog.d(TAG, "json location:" + json);
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
	 * Http Get方法
	 * 
	 * @param strUrl
	 *            （get方法因为是url带有特殊符号，不能统�?��码，只能单个转码�?
	 * @return
	 * @return String
	 * @author Zhouls
	 * @date 2015-4-8 下午2:40:48
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
				CldLog.e("[ols]", "url is empty!");
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
	 * Http Post 方法
	 * 
	 * @param strUrl
	 *            the str url
	 * @param strPost
	 *            Post是json可以做统�?��码处理（特殊字段不用中文转码�?
	 * @return String
	 * @author Zhouls
	 * @date 2014-9-2 上午11:06:45
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
				CldLog.e("[ols]", "url is empty!");
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 从GZIP中获取Json数据
	 * 
	 * @param is
	 * @return
	 * @return String
	 * @author Zhouls
	 * @date 2015-4-8 下午2:43:13
	 */
	private static String getJsonFromGZIP(InputStream is) {
		String jsonString = null;
		try {
			BufferedInputStream bis = new BufferedInputStream(is);
			bis.mark(2);
			// 取前两个字节
			byte[] header = new byte[2];
			int result = bis.read(header);
			// reset输入流到�?��位置
			bis.reset();
			// 判断是否是GZIP格式
			int headerData = getShort(header);
			// Gzip �?的前两个字节�?0x1f8b
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
			CldLog.i("ols", jsonString);
			return jsonString;
		} catch (Exception e) {
			/**
			 * 网络异常，出现jsonSting 为Null情况
			 */
			CldLog.e("[ols]", "net_null");
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
