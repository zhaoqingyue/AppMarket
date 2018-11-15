package cld.kmarcket.appinfo;

import java.util.ArrayList;
import android.os.Handler;
import cld.kcloud.utils.KCloudNetUtils;
import cld.kmarcket.parse.ParseJsonStr;
import cld.kmarcket.util.ConstantUtil;
import cld.kmarcket.util.LogUtil;
import cld.weather.api.CldSapNetUtil;
import cld.weather.api.CldSapReturn;

public class NetApp 
{
	private static NetApp mInstance = null;
	
	public static NetApp getInstance()
	{
		if(mInstance == null)
		{
			synchronized(NetApp.class)
			{
				if(mInstance == null)
					mInstance = new NetApp();
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
	
	ArrayList<AppInfo> mAppInfoList;
	
	private NetApp()
	{
		mAppInfoList = new ArrayList<AppInfo>();
		mAppInfoList.clear();
	}
	
	public void loadNetApps(final Handler handler, 
		final ArrayList<AppInfo> appInfoList, final int page, final int size) 
	{
		Thread thread = new Thread() 
		{
			@Override
			public void run() 
			{
				String result;
				CldSapReturn request = null;
				KCloudNetUtils.checkKgoSign();
				request = KCloudNetUtils.getNetApp(page, size, appInfoList);
				LogUtil.i(LogUtil.TAG, "netapp url: " + request.url + " post: " + request.jsonPost);
				result = CldSapNetUtil.sapPostMethod(request.url, request.jsonPost);
				LogUtil.i(LogUtil.TAG, "netapp result: " + result);
				
				ArrayList<AppInfo> parseResult = ParseJsonStr.parseRecdAppResult(result);
				if (parseResult != null && !parseResult.isEmpty())
				{
					mAppInfoList.clear();
					mAppInfoList.addAll(parseResult);
				}
				handler.obtainMessage(ConstantUtil.
						MSG_GET_APP_RECD_SUC).sendToTarget();
			}
		};
		thread.start();
	}
	
	public ArrayList<AppInfo> getNetApps()
	{
		for (AppInfo appinfo : mAppInfoList)
		{
			//推荐应用 全部非静默 add 2016-5-17 by zhaoqy
			appinfo.setQuiesce(ConstantUtil.APP_QUIESCE_NO);
			//应用来源 0：我的应用； 1：应用推荐
			appinfo.setSource(ConstantUtil.APP_SOURCE_RECD);
		}
		return mAppInfoList;
	}
}
