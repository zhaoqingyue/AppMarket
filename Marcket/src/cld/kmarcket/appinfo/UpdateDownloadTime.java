package cld.kmarcket.appinfo;

import cld.kcloud.utils.KCloudNetUtils;
import cld.kmarcket.util.LogUtil;
import cld.weather.api.CldSapNetUtil;
import cld.weather.api.CldSapReturn;

public class UpdateDownloadTime 
{
	private static UpdateDownloadTime mInstance = null;
	
	public static UpdateDownloadTime getInstance()
	{
		if(mInstance == null)
		{
			synchronized(UpdateDownloadTime.class)
			{
				if(mInstance == null)
				{
					mInstance = new UpdateDownloadTime();
				}
			}
		}
		return mInstance;
	}
	
	public static void static_release()
	{
		if(mInstance != null)
		{
			mInstance = null;
		}
	}
	
	public void updateAppDownloadTime(final AppInfo appInfo)
	{
		Thread thread = new Thread() 
		{
			@Override
			public void run() 
			{
				String result;
				CldSapReturn request = null;
				KCloudNetUtils.checkKgoSign();
				request = KCloudNetUtils.getUpdateAppDownloadTime(appInfo);
				LogUtil.i(LogUtil.TAG, "UpdateDownloadTime url: " + request.url);
				result = CldSapNetUtil.sapGetMethod(request.url);
				LogUtil.i(LogUtil.TAG, "UpdateDownloadTime result: " + result);
			}
		};
		thread.start();
	}
}
