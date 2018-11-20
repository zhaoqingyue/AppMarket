package cld.navi.weixin;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import com.cld.log.CldLog;
import cld.kcloud.center.KCloudCtx;
import cld.kcloud.custom.manager.KCloudPositionManager;
import cld.navi.util.FileUtils;
import cld.navi.util.NetWorkUtil;
import android.annotation.SuppressLint;
import android.support.v4.util.ArrayMap;

public class WeixinUtil {

	private final String TAG = "WeixinUtil";
	final String weixinUrl = "http://weixin.careland.com.cn/api/make_qrcode.php?";
	final String weixinTestUrl = "http://test.careland.com.cn/kz/web/kldwx/api/make_qrcode.php?";
	final String totkenKey = "Qw388swWzS8s75Lw98Sr9W87LF91Ww5i";
	final String totkenTestKey = "594d063cc2fc1305d36549837a068a7c";

	String getUrl() {
		if (KCloudPositionManager.getInstance().getIsTestServer())
			return new String(weixinTestUrl);
		else
			return new String(weixinUrl);
	}

	String getTokenKey() {
		if (KCloudPositionManager.getInstance().getIsTestServer())
			return new String(totkenTestKey);
		else
			return new String(totkenKey);
	}

	WeixinCallback mCallBack = null;

	public void SetCallback(WeixinCallback callBack) {
		mCallBack = callBack;
	};

	@SuppressLint("NewApi")
	public void onGetQrText(String duid, String deviceSn) {
		
		CldLog.i(TAG, " onGetQrText ");
		CldLog.i(TAG, "duid: " + duid + ", deviceSn: " + deviceSn);
		String logintquto = "11111111111111111111111111111111";
		String tmplogin = "11111111111111111111111111111111";
		String tmpduid = "0000";
		String tmpsign = getTokenKey();
		try {
			tmplogin = java.net.URLEncoder.encode(logintquto, "utf-8");
			tmpduid = java.net.URLEncoder.encode(duid, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		String qrString = getUrl();
		Map<String, String> sArray = new ArrayMap<String, String>();
		String imsi = NetWorkUtil.getImsi(KCloudCtx.getAppContext());
		String sim = NetWorkUtil.getPhoneNum(KCloudCtx.getAppContext());
		String iccid = NetWorkUtil.getICCIDNum(KCloudCtx.getAppContext());
		String ver = FileUtils.readAssetsFile(KCloudCtx.getAppContext(), 1).substring(0, 5);//?????
		
		sArray.put("wxtype", "kldlk");
		sArray.put("pname", "xksim");
		qrString += "wxtype=kldlk";
		qrString += "&pname=xksim";
		if ((null == iccid || iccid.equals("")) && 
				(null == imsi || imsi.equals("")) && 
				(null == sim || sim.equals(""))) {
			sArray.put("duid", tmpduid);
			sArray.put("devicesn", deviceSn);
			qrString += "&duid=" + tmpduid;
			qrString += "&devicesn=" + deviceSn;
		} else {
			sArray.put("duid", tmpduid);
			sArray.put("devicesn", deviceSn);
			sArray.put("ver", ver);	
			qrString += "&duid=" + tmpduid;
			qrString += "&devicesn=" + deviceSn;
			qrString += "&ver=" + ver;
			if (iccid != null && !iccid.isEmpty()) {
				sArray.put("iccid", iccid);
				qrString += "&iccid=" + iccid;
			}
			
			if (imsi != null && !imsi.isEmpty()) {
				sArray.put("imsi", imsi);
				qrString += "&imsi=" + imsi;
			}
			
			if (sim != null && !sim.isEmpty()) {
				sArray.put("sim", sim);
				qrString += "&sim=" + sim;
			}
		}
		
		String md5String = KSign.make_sign(sArray, getTokenKey());
		try {
			tmpsign = java.net.URLEncoder.encode(md5String, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		qrString += "&sign=" + tmpsign;
		CldLog.i(TAG, "send: " + qrString);
		httpThreadPost(qrString);
	}

	private void httpThreadPost(String url) {
		Thread pppp = new Thread(new PostRunnable(url));
		pppp.start();
	}

	class PostRunnable implements Runnable {
		String urlconnect = null;

		public PostRunnable(String url) {
			urlconnect = url;
		}

		@Override
		public void run() {
			if (urlconnect == null)
				return;

			try {
				HttpParams httpParams = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
				HttpClient httpClient = new DefaultHttpClient(httpParams);
				HttpPost post = new HttpPost(urlconnect);

				HttpResponse httpResponse = httpClient.execute(post);
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					if (mCallBack != null) {
						mCallBack.SetReturnMsg(EntityUtils
								.toString(httpResponse.getEntity()));
						mCallBack.onGetSuccess();
					}
				} else {
					if (mCallBack != null) {
						mCallBack.SetReturnMsg(null);
						mCallBack.onGetError();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (mCallBack != null) {
					mCallBack.SetReturnMsg(null);
					mCallBack.onGetError();
				}
			}
		}
	}

	public static class WeixinCallback {
		public String weixinReturnMsg;

		public void SetReturnMsg(String returnMsg) {
			weixinReturnMsg = returnMsg;
		}

		public void onGetSuccess() {
		};

		public void onGetError() {
		};
	}
}
