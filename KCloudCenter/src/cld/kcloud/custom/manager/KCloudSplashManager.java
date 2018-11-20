package cld.kcloud.custom.manager;

import java.io.File;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import com.cld.log.CldLog;
import com.cld.net.CldFileDownloader;
import com.cld.net.ICldFileDownloadCallBack;
import com.cld.ols.tools.CldOlsThreadPool;
import cld.kcloud.center.KCloudAppUtils;
import cld.kcloud.center.KCloudCtx;
import cld.kcloud.custom.bean.KCloudCustomInfo.KCloud_Logo;
import cld.kcloud.utils.KCloudNetworkUtils;
import cld.kcloud.utils.KCloudShareUtils;

/**
 * 闪屏管理
 * @author wuyl
 *
 */
@SuppressLint("NewApi") 
public class KCloudSplashManager {
	
	public interface IKCloudSplashListener {
		void onResult(String jsonString);
	}
	
	private static final String TAG = "KCloudSplashManager";
	private static KCloudSplashManager mKCloudSplashManager = null;
	private boolean mIsStopRunning = false;
	
	public static KCloudSplashManager getInstance() {
		if (mKCloudSplashManager == null) {
			synchronized(KCloudSplashManager.class) {
				if (mKCloudSplashManager == null) {
					mKCloudSplashManager = new KCloudSplashManager();
				}
			}
		}
		return mKCloudSplashManager;
	}
	
	public void init() {
		resetKCloudLogo();
		mIsStopRunning = false;
		start_updateKCloudLogo_Running();
	}
	
	public void uninit() {
		mIsStopRunning = true;
	}
	
	/**
	 * 开始定时获取闪屏信息
	 */
	private void start_updateKCloudLogo_Running() {
		CldOlsThreadPool.submit(new Runnable() {
			@Override
			public void run() {
				while (!mIsStopRunning) {
					KCloudNetworkUtils.getSplashInfomation(new IKCloudSplashListener() {
	
						@Override
						public void onResult(String jsonString) {
							CldLog.i(TAG, "jsonString = " + jsonString);
							if (jsonString != null && !jsonString.isEmpty()) {
								try {
									JSONObject jsonObject = new JSONObject(jsonString);
									if (jsonObject.getInt("errcode") == 0) {
										updateKCloudLogo(jsonObject.getString("data"));
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						}
					});
					
					try {
						Thread.sleep(10*60*1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	private void resetKCloudLogo() {
		try {
			JSONArray jsonArray = null;
			ArrayList<JSONObject> list = new ArrayList<JSONObject>();
			String jsonString = KCloudShareUtils.getString(KCloudAppUtils.TARGET_FIELD_SPLASH_LOGO);
			
			if (!jsonString.isEmpty()) {
				jsonArray = new JSONArray(jsonString);
				
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject object = jsonArray.getJSONObject(i);
					if ((object.getLong("start_time") != 0) && KCloudAppUtils.isTimeout(object.getLong("end_time"))) {
						continue;
					}
					list.add(object);
				}
			}
		
			if (!list.isEmpty()) {
				jsonArray = new JSONArray(list);
				KCloudShareUtils.put(KCloudAppUtils.TARGET_FIELD_SPLASH_LOGO, jsonArray.toString());
				CldLog.i(TAG, jsonArray.toString());
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void updateKCloudLogo(String jsonString) {
		if (jsonString.isEmpty()) {
			return ;
		}
		
		try {
			JSONObject jsonObject = new JSONObject(jsonString);
			String root = jsonObject.getString("root_url");
			
			JSONArray jsonArray = null;
			if (jsonObject.has("logolist")) {
				jsonArray = jsonObject.getJSONArray("logolist");
			}
			
			if (jsonArray != null && jsonArray.length() > 0) {
				long logo_prover = jsonArray.getJSONObject(0).getLong("prover");
				long logo_timeout = jsonArray.getJSONObject(0).getLong("timeout");
				long logo_starttime = jsonArray.getJSONObject(0).getLong("starttime");
				long logo_livetime = jsonArray.getJSONObject(0).getLong("stay_time");
				String logo_url = root + jsonArray.getJSONObject(0).getString("download_url");
				int logo_status = jsonArray.getJSONObject(0).getInt("status");
		
				String result = "[]";
				if (!KCloudShareUtils.getString(KCloudAppUtils.TARGET_FIELD_SPLASH_LOGO).isEmpty()) {
					result = KCloudShareUtils.getString(KCloudAppUtils.TARGET_FIELD_SPLASH_LOGO);
				}
				JSONArray jsonArrayResult = new JSONArray(result);
				ArrayList<JSONObject> list = new ArrayList<JSONObject>();
				
				for (int i = 0; i < jsonArrayResult.length(); i++) {
					JSONObject object = jsonArrayResult.getJSONObject(i);
					if (object.getLong("id") != logo_prover) {
						list.add(object);
					}
				} 
				
				if (logo_status != 0) {
					KCloud_Logo item = new KCloud_Logo(); 
					item.id = logo_prover;
					item.start_time = logo_starttime;
					item.end_time = logo_timeout;
					item.live_time = (int)logo_livetime;
					item.url = logo_url;
					item.target = KCloudCtx.getAppPath() + logo_url.substring(logo_url.lastIndexOf("/"));
					list.add(item.toJSON());
				}
				
				// 更新shared_prefs
				jsonArrayResult = new JSONArray(list);
				KCloudShareUtils.put(KCloudAppUtils.TARGET_FIELD_SPLASH_LOGO, jsonArrayResult.toString());
				CldLog.i(TAG, jsonArrayResult.toString());
	
				// 下载logo
				downloadKCloudLogo(logo_prover, logo_url);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void downloadKCloudLogo(final long id, String url) {
		int pos = url.lastIndexOf("/");
        String fileName = url.substring(pos);
        final String target = KCloudCtx.getAppPath() + fileName;
		CldFileDownloader fdl = new CldFileDownloader();
		
		CldLog.i(TAG, "taget = " + target);
		// 文件如果存在则不需要下载
		try {
			File file = new File(target);
			if(file.exists()) {
				return ;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}  
		
		fdl.downloadFile(url, target, false, new ICldFileDownloadCallBack() {

			@Override
			public void onFailure(String arg0) {
				CldLog.i(TAG, "download KCloudLogo failed");
			}
			
			@Override
			public void onSuccess(long arg0, long arg1) {
				CldLog.i(TAG, "download KCloudLogo success");
			}

			@Override
			public void onCancel() {
				
			}

			@Override
			public void onConnecting(boolean arg0, String arg1) {
				
			}

			@Override
			public void updateProgress(long arg0, long arg1, long arg2) {
				
			}
		});
	}
}
