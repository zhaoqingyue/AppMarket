package cld.kmarcket.appinfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import cld.kmarcket.KMarcketApplication;
import cld.kmarcket.util.ConstantUtil;
import cld.kmarcket.util.LogUtil;

public class InstalledApp {
	
	private static InstalledApp mInstance = null;
	
	public static InstalledApp getInstance(){
		if(mInstance == null){
			synchronized(InstalledApp.class){
				if(mInstance == null)
					mInstance = new InstalledApp();
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
	
	private InstalledApp(){
		mAppInfoList = new ArrayList<AppInfo>();
	}
	
	public ArrayList<AppInfo> getInstalledApps(){
		if (mAppInfoList == null || mAppInfoList.size() <= 0)
		{
			mAppInfoList.addAll(LoadInstalledApps(KMarcketApplication.getContext()));
		}
		return mAppInfoList;
	}
	
	public boolean appHaveInstalled(AppInfo appinfo){
		if(appinfo == null || appinfo.getPkgName().equalsIgnoreCase(""))
			return false;
		
		for(AppInfo item : mAppInfoList){
			if(item.getPkgName().equalsIgnoreCase(appinfo.getPkgName()))
				return true;
		}
		
		return false;
	}

	public ArrayList<AppInfo> LoadInstalledApps(Context context) {
		if(context == null)
			return new ArrayList<AppInfo>();
		
		PackageManager packageManager = context.getPackageManager();
		ArrayList<AppInfo> appList = new ArrayList<AppInfo>(); // 用来存储获取的应用信息数据
		List<PackageInfo> packages = packageManager.getInstalledPackages(0);

		for (int i = 0; i < packages.size(); i++) 
		{
			PackageInfo packageInfo = packages.get(i);
			AppInfo tmpInfo = new AppInfo();
			tmpInfo.setAppName(packageInfo.applicationInfo.loadLabel(packageManager).toString()); 
			tmpInfo.setPkgName(packageInfo.packageName);
			tmpInfo.setVerName(packageInfo.versionName);
			tmpInfo.setVerCode(packageInfo.versionCode);
			tmpInfo.setAppIcon(packageInfo.applicationInfo.loadIcon(packageManager));
			tmpInfo.setStatus(ConstantUtil.APP_STATUS_OPEN); //表示打开
			//LogUtil.i(LogUtil.TAG, " install_app packageName: " + packageInfo.packageName);
			
			// Only display the non-system app info
			if (((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0))
			{
				appList.add(tmpInfo);
			}
			else 
			{
				if (tmpInfo.getPkgName().equals("com.cld.launcher"))
				{
					appList.add(tmpInfo);
				}
			}
		}
		return appList;
	}
	
	/**
	 * 
	 * @Title: getInstalledApp
	 * @Description: 获取已安装的app列表
	 * @param context
	 * @param type
	 * @return
	 * @return: List<AppItem>
	 */
	public ArrayList<AppInfo> getInstalledApp(Context context) 
	{
		if (context == null) 
		{
			return null;
		}
		
		ArrayList<AppInfo> appinfos = new ArrayList<AppInfo>();
		appinfos.clear();
		PackageManager pm = context.getPackageManager();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, 0);
		Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(context.getPackageManager()));
		
		for(ResolveInfo resolveInfo : resolveInfos)
		{
			String pkgName = resolveInfo.activityInfo.packageName; //获取包名
			try 
			{
				PackageInfo packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
				if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) 
				{  
		            //第三方应用  
					LogUtil.i(LogUtil.TAG, " normal app -- Name:" + resolveInfo.
							loadLabel(pm).toString() + ", pkgname: " + pkgName);
					AppInfo appInfo = new AppInfo();
					appInfo.setAppName(resolveInfo.loadLabel(pm).toString());
					appInfo.setPkgName(resolveInfo.activityInfo.packageName);
					appInfo.setAppIcon(resolveInfo.loadIcon(pm));
					appInfo.setVerName(packageInfo.versionName);
					appInfo.setVerCode(packageInfo.versionCode);
					appInfo.setStatus(ConstantUtil.APP_STATUS_OPEN); //表示打开
					appinfos.add(appInfo);
				} 
				else 
				{  
		            //系统应用  
					LogUtil.i(LogUtil.TAG, " system app -- Name:" + resolveInfo.
							loadLabel(pm).toString() + ", pkgname: " + pkgName);
		        }  
			} 
			catch (NameNotFoundException e) 
			{
				e.printStackTrace();
			}  
		}
		return appinfos;
	}
}
