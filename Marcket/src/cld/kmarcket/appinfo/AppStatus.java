package cld.kmarcket.appinfo;

import java.util.ArrayList;
import android.os.Handler;
import cld.kcloud.utils.KCloudNetUtils;
import cld.kmarcket.parse.ParseJsonStr;
import cld.kmarcket.util.ConstantUtil;
import cld.kmarcket.util.LogUtil;
import cld.weather.api.CldSapNetUtil;
import cld.weather.api.CldSapReturn;

public class AppStatus 
{
	private static AppStatus mInstance = null;
	
	public static AppStatus getInstance()
	{
		if(mInstance == null)
		{
			synchronized(AppStatus.class)
			{
				if(mInstance == null)
				{
					mInstance = new AppStatus();
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
	
	private ArrayList<String> mAppStatus;
	
	private AppStatus()
	{
		mAppStatus = new ArrayList<String>();
	}
	
	public void loadAppStatus(final Handler handler, 
			final ArrayList<AppInfo> appInfoList)
	{
		Thread loadThread = new Thread() 
		{
			@Override
			public void run() 
			{
				String result;
				CldSapReturn request = null;
				KCloudNetUtils.checkKgoSign();
				request = KCloudNetUtils.getAppStatus(appInfoList);
				
				LogUtil.i(LogUtil.TAG, "AppStatus url: " + request.url + 
						" post: " + request.jsonPost);
				result = CldSapNetUtil.sapPostMethod(request.url, 
						request.jsonPost);
				LogUtil.i(LogUtil.TAG, "AppStatus result: " + result);
				
				ArrayList<String> parseResult = ParseJsonStr.
						parseAppStatusResult(result);
				if (parseResult != null && !parseResult.isEmpty())
				{
					mAppStatus.clear();
					mAppStatus.addAll(parseResult);
				}
				handler.obtainMessage(ConstantUtil.MSG_GET_APP_STATUS_SUC).sendToTarget();
			}
		};
		
		loadThread.start();
	}
	
	public ArrayList<String> getAppStatusList()
	{
		return mAppStatus;
	}
	
	public ArrayList<AppInfo> getAppList(ArrayList<AppInfo> installedAppList)
	{
		if (installedAppList == null || installedAppList.isEmpty())
		{
			return null;
		}
		
		ArrayList<AppInfo> appList = new ArrayList<AppInfo>();
		appList.clear();
		for(AppInfo item : installedAppList)
		{
			AppInfo temp = getUninstallAppInfo(item);
			if (temp == null)
			{
				String pkgName = item.getPkgName();
				if (pkgName.equals("cld.kmarcket")
						|| pkgName.equals("com.cld.launcher"))
				{
					//Launcher和Kmarket在无版本更新时不需要展示
					if (isKmarcketOrLauncherNormalUpgrade(pkgName))
					{
						appList.add(item); //不可卸载
					}
				}
				else
				{
					appList.add(item); //不可卸载
				}
			}
			else
			{
				appList.add(temp); //可卸载
			}
		}
		return appList;
	}
	
	private boolean isKmarcketOrLauncherNormalUpgrade(String pkgname)
	{
		ArrayList<AppInfo> normalUpgradeList = UpgradeApp.getInstance().
				getNormalUpgradeList();
		for(AppInfo normalUpgrade : normalUpgradeList)
		{
			if (pkgname.equals(normalUpgrade.getPkgName()))
			{
				return true;
			}
		}
		return false;
	}
	
	public AppInfo getUninstallAppInfo(AppInfo appinfo)
	{
		if(appinfo == null || appinfo.getPkgName().equalsIgnoreCase(""))
			return null;
		
		for(String pkgName : mAppStatus)
		{
			if(pkgName.equals(appinfo.getPkgName()))
			{
				appinfo.setStatus(ConstantUtil.APP_STATUS_UNINSTALL); //表示卸载
				return appinfo;
			}
		}
		return null;
	}
}
