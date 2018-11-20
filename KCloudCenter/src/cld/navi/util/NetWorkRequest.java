/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: NetWorkRequest.java
 * @Prject: KCloudCenter_M550
 * @Package: cld.navi.util
 * @Description: 网络请求
 * @author: zhaoqy
 * @date: 2016年8月11日 下午4:31:14
 * @version: V1.0
 */

package cld.navi.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import cld.kcloud.custom.manager.KCloudPositionManager;

public class NetWorkRequest {

	private final String TAG = "NetWorkRequest";
	private DefaultHttpClient httpClient;
	private HttpHost m_proxy = null;
	private boolean isLogToFile = false;

	public NetWorkRequest() {
		if (null == httpClient) {
			httpClient = new DefaultHttpClient();
		}
		isLogToFile = KCloudPositionManager.getInstance().getIsWriteLog();
	}

	public void Set_ProxyHost(HttpHost in_proxy) {
		m_proxy = in_proxy;
		if (httpClient != null & in_proxy != null) {
			httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, in_proxy);
		}
	}

	public JSONArray SendGetJsonJtzx(String url) {
		httpClient = new DefaultHttpClient();
		String strResult = "";
		HttpGet httpGet;

		try {
			Log.i("careland.url", url);
			httpGet = new HttpGet(url);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				Log.w(TAG, "OK-->" + url);

				// 取得返回的字符串
				strResult = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
				strResult.replaceAll("\r\n", "");
				strResult.replaceAll("\r", "");
				strResult.replaceAll("\t", "");
			}
			return new JSONArray(strResult);
		} catch (Exception e) {
			Log.w(TAG, "bad-->" + url);
			e.printStackTrace();
			return null;
		}
	}

	/*
	 [
	 {"Model":
		 "[
	 		{"type":1,"data":""},
	 		{"type":2,"data":""},
	 		{"type":3,"data":[
	                           {"type":1,"data":""},
	                  		   {"type":2,"data":""},
	                           {"type":1,"data":""}
	                         ]},
	        {"type":2,"data":""},
	        {"type":2,"data":""},
	        {"type":1,"data":""}
	     ]"
	  }
	]
	*/
	public JSONObject SendGetJsonObjJtzx(String url) {
		httpClient = new DefaultHttpClient();
		String strResult = "";
		HttpGet httpGet;

		try {
			httpGet = new HttpGet(url);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				Log.w(TAG, "OK-->" + url);

				// 取得返回的字符串
				strResult = EntityUtils.toString(httpResponse.getEntity(),
						"UTF-8");
				strResult.replaceAll("\r", "");
			}
			return new JSONObject(strResult);
		} catch (Exception e) {
			Log.w(TAG, "bad-->" + url);
			e.printStackTrace();
			return null;
		}
	}

	public String SendGetStringJtzx(String url) {

		String strResult = "";
		HttpGet httpGet;

		try {
			httpGet = new HttpGet(url);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				Log.w(TAG, "OK-->" + url);
				// 取得返回的字符串
				strResult = EntityUtils.toString(httpResponse.getEntity(),
						"UTF-8");
				strResult.replaceAll("\r", "");
				strResult.replaceAll("\t", "");
				strResult.replaceAll("\r\n", "");
				strResult.replaceAll("\r\t", "");
				Log.d("careland.test", strResult);
			}
			return strResult;
		} catch (Exception e) {
			Log.w(TAG, "bad-->" + url);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 判断是否有外网
	 */
	public Boolean IsNetWork(String url) {
		try {
			httpClient = new DefaultHttpClient();
			Set_ProxyHost(m_proxy);
			HttpGet httpGet;
			httpGet = new HttpGet(url);
			httpClient.execute(httpGet);
			return true;
		} catch (Exception e) {
			Log.w(TAG, "bad-->" + url);
			e.printStackTrace();
			return false;
		}
	}

	public InputStream SendGetInputStream(String url) {
		httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		InputStream in_Stream = null;
		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				Log.w("zbm", "OK-->" + url);

				in_Stream = httpResponse.getEntity().getContent();

			}
		} catch (Exception e) {
			Log.w("zbm", "bad-->" + url);
			e.printStackTrace();
		}
		return in_Stream;
	}

	public InputStream SendGet(String url) {
		httpClient = new DefaultHttpClient();
		InputStream in_Stream = null;
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				Log.w("zbm", "OK-->" + url);
				in_Stream = httpResponse.getEntity().getContent();
			}
		} catch (Exception e) {
			Log.w("zbm", "bad-->" + url);
			e.printStackTrace();
		}
		return in_Stream;
	}

	public InputStream SendPost(String url, HttpEntity Entity) {
		httpClient = new DefaultHttpClient();
		InputStream Result = null;
		try {
			HttpPost httpPost = new HttpPost(url);

			httpPost.setEntity(Entity);
			HttpResponse httpResponse = httpClient.execute(httpPost);

			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				Log.w("zbm", "OK-->" + url);
				Result = httpResponse.getEntity().getContent();
			}
		} catch (Exception e) {
			Log.w("zbm", "bad-->" + url);
			e.printStackTrace();
		}

		return Result;
	}

	public JSONObject SendGetJson(String url, int timeOut) {
		httpClient = new DefaultHttpClient();
		// 设置超时时间
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeOut);
		String strResult = "";
		HttpGet httpGet;

		try {

			httpGet = new HttpGet(url);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				Log.w(TAG, "OK-->" + url);

				// 取得返回的字符串
				strResult = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
				strResult = strResult.replaceAll("\r", "");
				strResult = strResult.replaceAll("\n", "");
			}
			// return new JSONArray(strResult.length() > 85 ?
			// strResult.substring(
			// 76, strResult.length() - 9) : strResult);
			return new JSONObject(strResult);
		} catch (Exception e) {
			Log.w(TAG, "bad-->" + e.toString() + url);
			e.printStackTrace();
			return null;

		}
	}

	public String SendGetString(String url) throws SocketTimeoutException,
			UnknownHostException {
		httpClient = new DefaultHttpClient();
		String strResult = "";
		HttpGet httpGet;

		try {
			Log.i("careland.url", url);
			httpGet = new HttpGet(url);

			HttpParams params = httpGet.getParams();
			// 设置2秒没反应就超时
			params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 2000);
			params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 2000);
			// httpGet.setParams(params);
			httpClient.setParams(params);

			HttpResponse httpResponse = httpClient.execute(httpGet);

			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				Log.w(TAG, "OK-->" + url);

				// 取得返回的字符串
				strResult = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
				strResult.replaceAll("\r\n", "");
				strResult.replaceAll("\r", "");
				strResult.replaceAll("\t", "");
			}
			return strResult;
		} catch (UnknownHostException e) {
			Log.w(TAG, "UnknownHost-->" + url);
			throw e;
		} catch (SocketTimeoutException e) {
			Log.w(TAG, "Timeout-->" + url);
			throw e;
		} catch (Exception e) {
			Log.w(TAG, "bad-->" + url);
			e.printStackTrace();
			return null;
		}
	}

	public JSONObject sendPostJson(String url, byte[] bytes) {
		try {
			if (url == null || "".equals(url) || bytes == null)
				return null;

			// ZipEntry
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ByteArrayOutputStream byteOutZip = new ByteArrayOutputStream();
			byte[] mByte = bytes;
			byte[] mByteZip = null;

			URL mUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
			conn.setConnectTimeout(10000);
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			// conn.setRequestProperty("Content-Type", "binary/octet-stream");
			conn.connect();
			OutputStream out = conn.getOutputStream();
			// ZipOutputStream zout = new ZipOutputStream(out);
			ZipOutputStream zoutbak = new ZipOutputStream(byteOutZip);
			zoutbak.putNextEntry(new ZipEntry("0"));
			zoutbak.write(mByte);
			zoutbak.closeEntry();
			mByteZip = byteOutZip.toByteArray();// 压缩后的数据

			InputStream in = conn.getInputStream();

			int readByte = -1;
			while ((readByte = in.read()) != -1) {
				byteOut.write(readByte);
			}
			in.close();
			String strResult = byteOut.toString();
			Log.w(TAG, "********-strResult->" + strResult);
			out.close();

			byteOut.close();

			JSONObject json = new JSONObject(strResult);
			return json;

		} catch (MalformedURLException e) {
			e.printStackTrace();
			Log.w(TAG, "sendPostJson-bad->" + e);
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			Log.w(TAG, "sendPostJson-bad->" + e);
			return null;
		} catch (JSONException e) {
			e.printStackTrace();
			Log.w(TAG, "sendPostJsonbad->" + e);
			return null;
		}

	}

	/**
	 * 通过发送二进制
	 */
	public JSONObject getJsonSendPost(String url, String param) {
		try {
			if (url == null || "".equals(url) || param == null
					|| "".equals(param))
				return null;

			byte[] mByte = param.getBytes();
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			URL mUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
			conn.setConnectTimeout(5000);
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			// conn.setRequestProperty("Content-Type", "binary/octet-stream");
			// conn.connect();
			OutputStream out = conn.getOutputStream();
			ZipOutputStream zout = new ZipOutputStream(out);
			zout.write(mByte);
			zout.flush();
			out.close();
			zout.close();

			InputStream in = conn.getInputStream();

			int readByte = -1;
			while ((readByte = in.read()) != -1) {
				byteOut.write(readByte);
			}
			in.close();
			String strResult = byteOut.toString();
			JSONObject json = new JSONObject(strResult);
			return json;

		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public JSONObject sendPostJson(String url, JSONObject obj) {
		HttpParams myParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(myParams, 6000);
		HttpConnectionParams.setSoTimeout(myParams, 6000);
		HttpClient httpclient = new DefaultHttpClient(myParams);
		String json = obj.toString();

		try {

			HttpPost httppost = new HttpPost(url);
			httppost.setHeader("Content-type", "application/json");

			StringEntity se = new StringEntity(obj.toString(), "UTF-8");
			se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
					"application/json"));
			httppost.setEntity(se);

			HttpResponse response = httpclient.execute(httppost);
			String temp = EntityUtils.toString(response.getEntity());
			Log.i("tag", temp);
			JSONObject mJson = new JSONObject(temp);
			return mJson;

		} catch (JSONException e) {
			FileUtils.logOut("SendGetJson-bad-->" + e.toString(), isLogToFile);
			return null;
		} catch (ClientProtocolException e) {
			FileUtils.logOut("SendGetJson-bad-->" + e.toString(), isLogToFile);
			return null;
		} catch (IOException e) {
			FileUtils.logOut("SendGetJson-bad-->" + e.toString(), isLogToFile);
			return null;
		}
	}

	/**
	 * 向指定URL发起get请求方法,位置上报使用该函数获取URL头等
	 */
	public JSONObject SendGetJson(String url) {
		httpClient = new DefaultHttpClient();
		// 设置超时10s
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
		String strResult = "";
		HttpGet httpGet;

		try {

			httpGet = new HttpGet(url);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int status = httpResponse.getStatusLine().getStatusCode();
			FileUtils.logOut("SendGetJson-status-->" + status, isLogToFile);
			if (status == HttpStatus.SC_OK) {
				FileUtils.logOut("SendGetJson-OK-->" + url, isLogToFile);
				// 取得返回的字符串
				strResult = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
				strResult = strResult.replaceAll("\r", "");
				strResult = strResult.replaceAll("\n", "");
			}
			return new JSONObject(strResult);
		} catch (Exception e) {
			FileUtils.logOut("SendGetJson-bad-->" + e.toString() + " url:"
					+ url, isLogToFile);
			e.printStackTrace();
			return null;

		}
	}

	/**
	 * 位置上报zlib格式的压缩二进制数据
	 */
	public JSONObject sendPostBytes(String url, byte[] bytes) {
		try {
			if (url == null || "".equals(url) || bytes == null)
				return null;

			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

			byte[] mByte = bytes;
			byte[] mByteZlib = null;

			URL mUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestProperty("Content-Type", "binary/octet-stream");
			conn.connect();
			OutputStream out = conn.getOutputStream();
			mByteZlib = ZLibUtils.compress(mByte);
			out.write(mByteZlib);
			out.flush();
			out.close();

			InputStream in = conn.getInputStream();

			int readByte = -1;
			while ((readByte = in.read()) != -1) {
				byteOut.write(readByte);
			}
			in.close();
			String strResult = byteOut.toString();
			FileUtils.logOut("上报接口获取到的结果-->" + strResult, isLogToFile);
			JSONObject json = new JSONObject(strResult);
			return json;

		} catch (MalformedURLException e) {
			e.printStackTrace();
			FileUtils.logOut("sendPostJson-bad->" + e, isLogToFile);
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			FileUtils.logOut("sendPostJson-bad->" + e, isLogToFile);
			return null;
		} catch (JSONException e) {
			e.printStackTrace();
			FileUtils.logOut("sendPostJsonbad->" + e, isLogToFile);
			return null;
		}

	}

	/**
	 * 是否可访问
	 * @param url
	 * @return
	 */
	public Boolean SendGetIsAccess(String url) {
		httpClient = new DefaultHttpClient();
		Boolean blResult = false;
		HttpGet httpGet;

		try {
			Log.i("careland.url", url);
			httpGet = new HttpGet(url);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				Log.w(TAG, "OK-->" + url);
				blResult = true;

			}
			return blResult;
		} catch (Exception e) {
			Log.w(TAG, "bad-->" + url);
			e.printStackTrace();
			return false;
		}
	}
}
