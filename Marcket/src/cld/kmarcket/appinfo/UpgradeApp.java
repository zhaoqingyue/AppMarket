package cld.kmarcket.appinfo;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.os.Handler;
import cld.kcloud.utils.KCloudNetUtils;
import cld.kmarcket.parse.ParseJsonStr;
import cld.kmarcket.util.ConstantUtil;
import cld.kmarcket.util.LogUtil;
import cld.weather.api.CldSapNetUtil;
import cld.weather.api.CldSapReturn;

public class UpgradeApp {
	private static UpgradeApp mInstance = null;
	
	public static UpgradeApp getInstance(){
		if(mInstance == null){
			synchronized(UpgradeApp.class){
				if(mInstance == null)
					mInstance = new UpgradeApp();
			}
		}
		
		return mInstance;
	}
	
	public static void static_release(){
		if(mInstance != null){
			mInstance = null;
		}
	}
	
	ArrayList<AppInfo> mAppInfoList;
	ArrayList<AppInfo> mQuiesceAppInfoList;
	ArrayList<AppInfo> mNormalUpgradeList;
	
	private UpgradeApp(){
		mAppInfoList = new ArrayList<AppInfo>();
		mQuiesceAppInfoList = new ArrayList<AppInfo>();
		mNormalUpgradeList = new ArrayList<AppInfo>();
		mQuiesceAppInfoList.clear();
		mNormalUpgradeList.clear();
	}
	
	public void loadUpdateApps(final Handler handler, final ArrayList<AppInfo> appInfoList, 
			final int page, final int size, final long duid, final long kuid, final int regionId)
	{
		LogUtil.i(LogUtil.TAG, " loadUpdateApps ");
		Thread thread = new Thread() 
		{
			@Override
			public void run() 
			{
				String result;
				CldSapReturn request = null;
				KCloudNetUtils.checkKgoSign();
				request = KCloudNetUtils.getUpdateApp(page, size, appInfoList, duid, kuid, regionId);
				LogUtil.i(LogUtil.TAG, "UpdateApps url: " + request.url + " post: " + request.jsonPost);
				result = CldSapNetUtil.sapPostMethod(request.url, request.jsonPost);
				LogUtil.i(LogUtil.TAG, "UpdateApps result: " + result);
				
				ArrayList<AppInfo> parseResult = ParseJsonStr.parseAppUpgradeResult(result);
				if (parseResult != null && !parseResult.isEmpty())
				{
					mAppInfoList.clear();
					mAppInfoList.addAll(parseResult);
					
					//先判断parseResult是否为null及isEmpty，否则出现NullPointerException
					for (AppInfo appinfo : parseResult)
					{
						if (appinfo.getQuiesce() == ConstantUtil.APP_QUIESCE_YES)
						{
							mQuiesceAppInfoList.clear();
							mQuiesceAppInfoList.add(appinfo);
						}
					}
				}
				handler.obtainMessage(ConstantUtil.MSG_GET_APP_UPGRADE_SUC).sendToTarget();
			}
		};
		thread.start();
	}
	
	public ArrayList<AppInfo> getUpgradeAppList()
	{
		return mAppInfoList;
	}
	
	public ArrayList<AppInfo> getNormalUpgradeList()
	{
		return mNormalUpgradeList;
	}
	
	public ArrayList<AppInfo> getQuiesceUpgradeAppList()
	{
		return mQuiesceAppInfoList;
	}
	
	public ArrayList<AppInfo> getNormalUpgradeAppList(
			ArrayList<AppInfo> installedAppList)
	{
		if (installedAppList == null || installedAppList.isEmpty())
		{
			return null;
		}
		
		ArrayList<AppInfo> appList = new ArrayList<AppInfo>();
		appList.clear();
		for(AppInfo item : installedAppList)
		{
			//LogUtil.i(LogUtil.TAG, "appName:" + item.getAppName());
			//LogUtil.i(LogUtil.TAG, "widget:" + item.getIsWidget());
			//LogUtil.i(LogUtil.TAG, "quiesce:" + item.getQuiesce());
			
			//应用来源 0：我的应用； 1：应用推荐
			item.setSource(ConstantUtil.APP_SOURCE_MYAPP);
			AppInfo temp = getUpgradeAppInfo(item);
			if (temp == null)
			{
				//不可升级
				if (item.getIsWidget() == ConstantUtil.APP_WIDGET_NO)
				{
					appList.add(item); 
				}
			}
			else
			{
				//可升级
				switch (temp.getQuiesce()) 
				{
				case ConstantUtil.APP_QUIESCE_NO:
				{
					//常规升级方式(非widget)
					if (item.getIsWidget() == ConstantUtil.APP_WIDGET_NO)
					{
						temp.setAppDesc(item.getAppDesc());
						temp.setAppIcon(item.getAppIcon());
						appList.add(temp); 
						mNormalUpgradeList.add(temp);
					}
					break;
				}
				case ConstantUtil.APP_QUIESCE_YES:
				{
					//静默升级方式(非widget)
					if (item.getIsWidget() == ConstantUtil.APP_WIDGET_NO)
					{
						//不显示更新
						item.setStatus(ConstantUtil.APP_STATUS_OPEN);
						appList.add(item); 
					}
					//mQuiesceAppInfoList.add(temp);
					break;
				}
				default:
					break;
				}
			}
		}
		return appList;
	}
	
	private AppInfo getUpgradeAppInfo(AppInfo appinfo)
	{
		if(appinfo == null || appinfo.getPkgName().equals(""))
			return null;
		
		for(AppInfo item : mAppInfoList)
		{
			if(item.getPkgName().equals(appinfo.getPkgName()))
			{
				//应用来源 0：我的应用； 1：应用推荐
				item.setSource(ConstantUtil.APP_SOURCE_MYAPP);
				return item;
			}
		}
		return null;
	}
	/**
	 * @param pkgname
	 * 通过 pkgname 判断是否是静默升级
	 */
	@SuppressLint("NewApi") 
	public boolean isQuiesceUpgrade(String pkgname)
	{
		if (pkgname == null || pkgname.isEmpty())
			return false;
		
		for (AppInfo item : mQuiesceAppInfoList)
		{
			if (pkgname.equals(item.getPkgName()))
			{
				return true;
			}
		}
		return false;
	}
}
