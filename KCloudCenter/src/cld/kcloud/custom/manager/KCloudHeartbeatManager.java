package cld.kcloud.custom.manager;

import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import cld.kcloud.center.KCloudCtx;
import cld.kcloud.utils.KCloudCommonUtil;
import cld.kcloud.utils.KCloudNetworkUtils;
import com.cld.log.CldLog;
import com.cld.ols.tools.CldOlsThreadPool;

public class KCloudHeartbeatManager 
{
	private final String TAG = "KCloudHeartbeatManager";
	public static final String PKGNAME_NAVI = "com.cld.navi.cc";
	
	private static KCloudHeartbeatManager mKCloudUpgrade = null;
	public static KCloudHeartbeatManager getInstance() 
	{
		if (mKCloudUpgrade == null) 
		{
			synchronized(KCloudHeartbeatManager.class) 
			{
				if (mKCloudUpgrade == null) 
				{
					mKCloudUpgrade = new KCloudHeartbeatManager();
				}
			}
		}
		return mKCloudUpgrade;
	}
	
	public interface IKCloudHeartbeatListener 
	{
		void onResult(String jsonString);
	}
	
	public void init() 
	{
		CldLog.i(TAG, "init ");
		mHandler.sendEmptyMessage(0);
	}
	
	private void startHeartbeatRunning() 
	{
		CldOlsThreadPool.submit(new Runnable() 
		{
			@Override
			public void run() 
			{
				long update = System.currentTimeMillis();
				KCloudNetworkUtils.getHeartbeatStatus(update, new IKCloudHeartbeatListener()
				{
					@SuppressLint("NewApi") 
					@Override
					public void onResult(String jsonString) 
					{
						CldLog.i(TAG, "jsonString = " + jsonString);
						if (jsonString != null && !jsonString.isEmpty())
						{
							try 
							{
								JSONObject jsonObject = new JSONObject(jsonString);
								if (jsonObject.getInt("errcode") == 1) 
								{
								}
							} 
							catch (JSONException e) 
							{
								e.printStackTrace();
							}
						}
					}
				});
			}
		});
	}
	
	private Handler mHandler = new Handler(KCloudCtx.getAppContext().getMainLooper())
	{
		public void handleMessage(Message msg) 
		{
			switch (msg.what) 
			{
			case 0:
			{
				//导航已安装，则发送心跳; 否则不发
				boolean exist = KCloudCommonUtil.isPackageExist(PKGNAME_NAVI);
				if (exist)
				{
					startHeartbeatRunning();
					//300s发送一次
					mHandler.sendEmptyMessageDelayed(0, 300*1000);
				}
				break;
			}
			default:
				break;
			}
		};
	};
}
