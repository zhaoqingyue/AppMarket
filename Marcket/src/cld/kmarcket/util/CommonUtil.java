package cld.kmarcket.util;

import java.io.File;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;
import android.widget.Toast;
import cld.kmarcket.KMarcketApplication;
import cld.kmarcket.R;
import cld.kmarcket.appinfo.AppInfo;
import cld.weather.api.NetUtil;
import com.cld.customview.Toast.CLDToast;
import com.download.api.DownloadDir;
import com.download.api.Status;
import com.download.api.StatusDao;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

public class CommonUtil 
{
	private static Context mContext;
	private static StatusDao mStatusDao;
	private static Toast mToast = null; 
	
	public static void init()
	{
		mContext = KMarcketApplication.getContext();
		mStatusDao = StatusDao.getInstance(mContext);
	}
	
	public static DisplayImageOptions iconOption = new DisplayImageOptions
			.Builder()
    		.showStubImage(R.drawable.icon_default)
    		.showImageForEmptyUri(R.drawable.icon_default)
    		.showImageOnFail(R.drawable.icon_default)
    		.cacheInMemory()
    		.cacheOnDisc()
    		.displayer(new SimpleBitmapDisplayer())
    		.build();
	
	/**
	 * 格式化文件大小
	 * @param size 文件大小（byte） 
	 */
	public static String FormatFileSize(int size) 
	{
		double fSize;
		String str = "";

		if (size >= 1024 * 1024) 
		{
			fSize = size / (1024 * 1024.0);
			str = new DecimalFormat("0.0").format(fSize) + "MB";
		} 
		else if (size >= 1024 && size < 1024 * 1024) 
		{
			fSize = size / (1024.0);
			str = new DecimalFormat("0.0").format(fSize) + "KB";
		} 
		else if (size >= 0 && size < 1024) 
		{
			fSize = size / (1024.0);
			str = new DecimalFormat("0.0").format(fSize) + "KB";
		}
		else 
		{
			str = new DecimalFormat("0.0").format(0) + "KB";
		}
		return str;
	}
	
	/**
	 * 格式化下载次数
	 * @param context
	 * @param time
	 */
	public static String FormatTimes(int time) 
	{
		double ftime;
		String str = "";
		
		if (time >= 0 && time < Math.pow(10,4))
		{
			str = time + getString(R.string.util_default);
		}
		else if (time >= Math.pow(10,4) && time < Math.pow(10,8))
		{
			ftime = time / 10000.0;
			str = new DecimalFormat("0.0").format(ftime) + 
					getString(R.string.util_wan);
		}
		else if (time >= Math.pow(10,8))
		{
			ftime = time / (10000 * 10000.0);
			str = new DecimalFormat("0.0").format(ftime) + 
					getString(R.string.util_yi);
		}
		else
		{
			str = "0" + getString(R.string.util_default);
		}
		return str;
	}
	
	public static String getString(int stringId)
	{
		return mContext.getResources().getString(stringId);
	}
	
	/**
	 * 得到百分比
	 * @param cur 当前大小
	 * @param total 总大小
	 * @return 百分比
	 */
	public static String getPrecent(int cur, int total) 
	{
		double perFloat = cur / (total * 1.0);
		StringBuffer sb = new StringBuffer();
		int perInt;
		if (perFloat >= 0.0 && perFloat <= 1.0) 
		{
			perInt = (int) (perFloat * 100);
		} 
		else 
		{
			//total为负数
			perInt = 0;
		}
		sb.append(perInt);
		sb.append("%");
		return sb.toString();
	}
	
	/**
	 * 获取屏幕宽度
	 * @param activity
	 */
	public static int getMetricWidth(Activity activity)
	{
		DisplayMetrics metric = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
		return metric.widthPixels; //宽度（PX）
	}
	
	/**
	 * 获取屏幕高度
	 * @param activity
	 */
	public static int getMetricHeight(Activity activity)
	{
		DisplayMetrics metric = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
		return metric.heightPixels; //高度（PX）
	}
	
	/*public static boolean getIsFirstOpen()
	{
		@SuppressWarnings("static-access")
		SharedPreferences sp = mContext.getSharedPreferences("sp", 
				mContext.MODE_PRIVATE);
		return sp.getBoolean("first", true);
	}
	
	public static void setIsFirstOpen(boolean isFirstOpen)
	{
		@SuppressWarnings("static-access")
		SharedPreferences sp = mContext.getSharedPreferences("sp", 
				mContext.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean("first", isFirstOpen);
		editor.commit();
	}*/
	
	/**
	 * 根据Pkgname获取AppInfo
	 * @param context
	 * @param pkgname
	 */
	public static AppInfo getAppInfoByPkgname(String pkgname)
	{
		AppInfo appinfo = null;
		PackageManager pm = mContext.getPackageManager();
		PackageInfo packageInfo;
		try 
		{
			boolean flag = false;  
			packageInfo = pm.getPackageInfo(pkgname, 
					PackageManager.GET_PERMISSIONS);
			//非系统应用
			if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
			{
				flag = true;
			}
			else 
			{
				//系统应用中Launcher例外
				if (pkgname.equals("com.cld.launcher"))
				{
					flag = true;
				}
			}
			if (flag)
			{
				appinfo = new AppInfo();
				appinfo.setPkgName(pkgname);
				appinfo.setAppName(packageInfo.applicationInfo.loadLabel(pm).toString());
				appinfo.setVerName(packageInfo.versionName);
				appinfo.setVerCode(packageInfo.versionCode);
				appinfo.setAppIcon(packageInfo.applicationInfo.loadIcon(pm));
			}
		} 
		catch (NameNotFoundException e) 
		{
			e.printStackTrace();
		}
		return appinfo;
	}
	
	/**
	 * 根据Pkgname获取Vercode
	 * @param pkgname
	 */
	public static int getVercodeByPkgname(String pkgname)
	{
		int versionCode = 0;
		PackageManager pm = mContext.getPackageManager();
		PackageInfo packageInfo;
		try 
		{
			packageInfo = pm.getPackageInfo(pkgname, 
					PackageManager.GET_PERMISSIONS);
			if (packageInfo != null)
			{
				versionCode = packageInfo.versionCode;
			}
		} 
		catch (NameNotFoundException e) 
		{
			e.printStackTrace();
		}
		return versionCode;
	}
	
	public static String getVernameByPkgname(String pkgname)
	{
		String versionName = "";
		PackageManager pm = mContext.getPackageManager();
		PackageInfo packageInfo;
		try 
		{
			packageInfo = pm.getPackageInfo(pkgname, 
					PackageManager.GET_PERMISSIONS);
			if (packageInfo != null)
			{
				versionName = packageInfo.versionName;
			}
		} 
		catch (NameNotFoundException e) 
		{
			e.printStackTrace();
		}
		return versionName;
	}
	
	/**
	 * 如果vername以数字开头，则在vername前加V
	 * @param vername
	 */
	public static String getVerName(String vername)
	{
		if (vername != null)
		{
			if (vername.matches("[0-9]+.*"))
			{
				vername = "V" + vername;
			}
			return vername;
		}
		return "";
	}
	
	/**
	 * 开始静默安装
	 * @param urlPath
	 */
	public static void startSlienceInstall(String urlPath)
	{
		String path = DownloadDir.getDownloadDir();
		String name = urlPath.substring(urlPath.lastIndexOf("/") + 1);
		LogUtil.i(LogUtil.TAG, "SlienceInstall path: " + path + ", name: " + name);
		File apkfile = new File(path, name);
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(apkfile), 
				"cld_application/vnd.android.package-archive");
		mContext.startService(intent);
	}
	
	/**
	 * 开始静默卸载
	 * @param pkgName
	 */
	public static void startSlienceUninstall(String pkgName)
	{
		Intent intent = new Intent();
	    intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("package:" + pkgName), 
				"cld_application/vnd.android.package-archive");
		mContext.startService(intent);
	}
	
	/**
	 * 常规方式安装
	 * @param context
	 * @param packageName
	 */
	@SuppressLint("NewApi") 
	public static void startNormalInstall(String packageName)
	{
		if (mContext == null || packageName == null || packageName.isEmpty())
		{
			return;
		}
		
		String apkName = packageName + ".apk";
		File apkfile = new File(FileUtil.getApkDir(), apkName);
		if (!apkfile.exists())
		{
			return;
		}
		
		try 
		{
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			intent.setDataAndType(Uri.fromFile(apkfile), 
					"application/vnd.android.package-archive");
			mContext.startActivity(intent);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 常规方式卸载
	 * @param context
	 * @param packageName
	 */
	@SuppressLint("NewApi") 
	public static void startNormalUninstall(String packageName) 
	{
		if (mContext == null || packageName == null || packageName.isEmpty())
		{
			return;
		}
		
		try 
		{
			Uri packageURI = Uri.parse("package:" + packageName);   
			Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, 
					packageURI);   
			mContext.startActivity(uninstallIntent);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @Description: 打开应用
	 * @param context
	 * @param packageName
	 */
	@SuppressLint("NewApi") 
	public static void openAppByPkgname(String packageName)
	{
		LogUtil.i(LogUtil.TAG, " openApp: " + packageName);
		if (mContext == null || packageName == null || packageName.isEmpty())
		{
			return;
		}
		
		try 
		{
			PackageManager packageManager = mContext.getPackageManager();
			Intent intent = packageManager.getLaunchIntentForPackage(packageName);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 发送广播给Launcher，状态栏中显示下载状态
	 * @param status 1：开始下载；2：暂停下载；3：取消下载；4：完成下载
	 * @param appname 应用名称
	 */
	public static void sendDownloadStatus(String appname, int status)
	{
		Intent intent = new Intent("cld.kmarcket.DOWNLAOD_STATUS");
		intent.putExtra("DOWNLOAD_NAME", appname);
		intent.putExtra("DOWNLOAD_STATUS", status);
		mContext.sendBroadcast(intent);
	}
	
	/**
	 * 发送安装成功广播
	 * @param pkgname
	 */
	public static void sendPackageAddedSuc(String pkgname)
	{
		Intent intent = new Intent(ConstantUtil.ACTION_ADDED_SUC);
		intent.putExtra("package_name", pkgname);
		mContext.sendBroadcast(intent);
	}
	
	/**
	 * 发送替换成功广播
	 * @param pkgname
	 */
	public static void sendPackageReplacedSuc(String pkgname)
	{
		Intent intent = new Intent(ConstantUtil.ACTION_REPLACED_SUC);
		intent.putExtra("package_name", pkgname);
		mContext.sendBroadcast(intent);
	}
	
	/**
	 * 发送卸载成功广播
	 * @param pkgname
	 */
	public static void sendPackageRemovedSuc(String pkgname)
	{
		Intent intent = new Intent(ConstantUtil.ACTION_REMOVED_SUC);
		intent.putExtra("package_name", pkgname);
		mContext.sendBroadcast(intent);
	}
	
	/**
	 * 发送安装失败广播
	 * @param pkgname
	 */
	public static void sendPackageAddedFailed(String pkgname)
	{
		Intent intent = new Intent(ConstantUtil.ACTION_ADDED_FAILED);
		intent.putExtra("package_name", pkgname);
		mContext.sendBroadcast(intent);
	}
	
	/**
	 * 发送卸载失败广播
	 * @param pkgname
	 */
	public static void sendPackageRemovedFailed(String pkgname)
	{
		Intent intent = new Intent(ConstantUtil.ACTION_REMOVED_FAILED);
		intent.putExtra("package_name", pkgname);
		mContext.sendBroadcast(intent);
	}
	
	/**
	 * 发送开始Launcher升级广播
	 * @param pkgname
	 */
	public static void sendStartLauncherUpgrade()
	{
		Intent intent = new Intent(ConstantUtil.ACTION_LAUNCHER_UPGRADE_START);
		mContext.sendBroadcast(intent);
	}
	
	/**
	 * 退出K应用
	 */
	public static void sendExitKmarket()
	{
		Intent intent = new Intent();
		intent.setAction(ConstantUtil.ACTION_EXIT_APP);
		mContext.sendBroadcast(intent);
	}
	
	@SuppressLint("NewApi")
	public static void updateAppType(String pkgname, int type)
	{
		if (pkgname == null || pkgname.isEmpty())
		{
			return;
		}
		
		if (mStatusDao == null)
		{
			mStatusDao = StatusDao.getInstance(mContext);
		}
		
		Status status =  mStatusDao.query(pkgname);
		if (status == null)
		{
			mStatusDao.insert(new Status(pkgname, type));
		}
		else
		{
			if (status.getType() != type)
			{
				mStatusDao.updateType(new Status(pkgname, type));
			}
		}
	}
	
	public static StatusDao getStatusDao()
	{
		if (mStatusDao == null)
		{
			mStatusDao = StatusDao.getInstance(mContext);
		}
		return mStatusDao;
	}
	
	/**
	 * 避免多次创建Toast，导致显示时间叠加
	 */
	public static void showToast(String text)
	{
		TextView textView;
		if(mToast == null)
		{
			mToast = new Toast(mContext);
			textView = new TextView(mContext);
			int size = (int) textView.getTextSize() + 2;
			textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
			textView.setTextColor(Color.parseColor("#ffffff"));
			textView.setBackgroundResource(R.drawable.toast_bg);
			textView.setPadding(30, 10, 30, 10);
			mToast.setDuration(Toast.LENGTH_LONG);
			mToast.setView(textView);
		}
		else
		{
			textView = (TextView) mToast.getView();
		}
		textView.setText(text);
		mToast.show();
		//Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
	}
	
	public static void makeText(int strid)
	{
		if (mContext == null)
			mContext = KMarcketApplication.getContext();
		CLDToast.show(mContext, getString(strid), false);
	}
	
	public static void makeText(String str)
	{
		if (mContext == null)
			mContext = KMarcketApplication.getContext();
		CLDToast.show(mContext, str, false);
	}
	
	public static int getAppIndexByPkgName(String pkgName, 
			ArrayList<AppInfo> appList)
	{
		if(pkgName == null || pkgName.equalsIgnoreCase(""))
			return -1;
		
		if (appList == null || appList.isEmpty())
			return -1;
		
		
		for (int i=0; i<appList.size(); i++)
		{
			if(pkgName.equals(appList.get(i).getPkgName()))
			{
				return i;
			}
		}
		return -1;
	}
	
	public static AppInfo getAppInfoByPkgName(String pkgName, 
			ArrayList<AppInfo> appList)
	{
		if(pkgName == null || pkgName.equalsIgnoreCase(""))
			return null;
		
		if (appList == null || appList.isEmpty())
			return null;
		
		for (int i=0; i<appList.size(); i++)
		{
			if(pkgName.equals(appList.get(i).getPkgName()))
			{
				return appList.get(i);
			}
		}
		return null;
	}
	
	public static boolean isInstalledApp(String pkgname, 
			ArrayList<AppInfo> appList)
	{
		if(pkgname == null || pkgname.equalsIgnoreCase(""))
			return false;
		
		if (appList == null || appList.isEmpty())
			return false;
		
		for (int i=0; i<appList.size(); i++)
		{
			if (pkgname.equals(appList.get(i).getPkgName()))
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean isBackground(Context context) 
	{
		ActivityManager activityManager = (ActivityManager) context.
				getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcesses = activityManager.
				getRunningAppProcesses();
		for (RunningAppProcessInfo appProcess : appProcesses) 
		{
			if (appProcess.processName.equals(context.getPackageName())) 
			{
				if (appProcess.importance == RunningAppProcessInfo.
						IMPORTANCE_BACKGROUND) 
				{
					return true;
				}
				else 
				{
					return false;
				}
			}
		}
		return false;
	}
	
	public static boolean isAppRunning(Context context)
	{
		ActivityManager am = (ActivityManager)context.
				getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> list = am.getRunningTasks(100);
		boolean isAppRunning = false;
		for (RunningTaskInfo info : list) 
		{
			if (info.topActivity.getPackageName().equals(
					context.getPackageName()) || 
				info.baseActivity.getPackageName().equals(
						context.getPackageName())) 
			{
				isAppRunning = true;
				break;
			}
		}
		return isAppRunning;
	}
	
	/**
	 * 获取app运行状态 0：没有运行； 1：正在运行； 2：后台运行
	 * @param context
	 * @return
	 */
	public static int getAppRunningStatus(Context context)
	{
		ActivityManager am = (ActivityManager)context.
				getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> list = am.getRunningTasks(100);
		int appRunningStatus = 0;
		for (RunningTaskInfo info : list) 
		{
			if (info.topActivity.getPackageName().equals(
					context.getPackageName())) 
			{
				appRunningStatus = 1;
				break;
			}
			else if (info.baseActivity.getPackageName().equals(
						context.getPackageName())) 
			{
				appRunningStatus = 2;
				break;
			}
		}
		return appRunningStatus;
	}
	
	static public boolean startAppByPkgName(Context context, String packagename) 
	{
		if (context == null)
			return false;

		//通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
		PackageInfo packageinfo = null;
		try 
		{
			packageinfo = context.getPackageManager().
					getPackageInfo(packagename, 0);
		} 
		catch (NameNotFoundException e) 
		{
			e.printStackTrace();
		}

		if (packageinfo == null) 
		{
			return false;
		}

		//创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
		Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resolveIntent.setPackage(packageinfo.packageName);

		//通过getPackageManager()的queryIntentActivities方法遍历
		List<ResolveInfo> resolveinfoList = context.getPackageManager().
				queryIntentActivities(resolveIntent, 0);

		if(resolveinfoList == null || resolveinfoList.size() <= 0)
			return false;
		
		ResolveInfo resolveinfo = resolveinfoList.iterator().next();
		if (resolveinfo != null) 
		{
			String packageName = resolveinfo.activityInfo.packageName;
			String className = resolveinfo.activityInfo.name;

			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			//设置ComponentName参数1:packagename参数2:MainActivity路径
			ComponentName cn = new ComponentName(packageName, className);
			intent.setComponent(cn);
			context.startActivity(intent);
			return true;
		}
		return false;
	}
	
	/**
	 * 获取状态栏的高度
	 * @param context
	 * @return
	 */
	public static int getBarHeight(Context context)
	{
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, sbar = 38; //默认为38，貌似大部分是这样的

        try 
        {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = context.getResources().getDimensionPixelSize(x);
        } 
        catch (Exception e1) 
        {
            e1.printStackTrace();
        }
        return sbar;
    }
	
	public static String getSystemVer()
	{
	    //String device_model = Build.MODEL;            // 设备型号  
	    //String version_sdk = Build.VERSION.SDK;       // 设备SDK版本  
	    String version_release = Build.VERSION.RELEASE; // 设备的系统版本  
	    
	    //Log.i(TAG, "   device_model: " + device_model);
	    //Log.i(TAG, "    version_sdk: " + version_sdk);
	    LogUtil.i(LogUtil.TAG, "version_release: " + version_release);
	    return version_release;
	}
	
	/**
	 * 
	 * @Title: configureTest
	 * @Description: 配置测试
	 * @return: void
	 */
	@SuppressLint("NewApi")
	public static void configureTest()
	{
		LogUtil.i(LogUtil.TAG, " configureTest ");
		//KCloudAppConfig.plan_code = 100100;  // 方案商编号
		//KCloudAppConfig.device_dpi = 160;    // 设备分辨率
		//KCloudAppConfig.system_ver = "4.4";  //android系统版本	
		
		@SuppressWarnings("static-access")
		SharedPreferences sp = mContext.getSharedPreferences("configure", mContext.MODE_PRIVATE);
		ConfigUtils.plan_code = sp.getInt("plan_code", -1);
		if (ConfigUtils.plan_code == -1)
		{
			SharedPreferences.Editor editor = sp.edit();
			editor.putInt("plan_code", 100100);
			editor.commit();
			ConfigUtils.plan_code = sp.getInt("plan_code", -1);
		}
		
		ConfigUtils.device_dpi = sp.getInt("device_dpi", -1);
		if (ConfigUtils.device_dpi == -1)
		{
			SharedPreferences.Editor editor = sp.edit();
			editor.putInt("device_dpi", 160);
			editor.commit();
			ConfigUtils.device_dpi = sp.getInt("device_dpi", -1);
		}
		
		ConfigUtils.system_ver = sp.getString("system_ver", "");
		if (ConfigUtils.system_ver.isEmpty())
		{
			SharedPreferences.Editor editor = sp.edit();
			editor.putString("system_ver", "4.4");
			editor.commit();
			ConfigUtils.system_ver = sp.getString("system_ver", "");
		}
		
		LogUtil.i(LogUtil.TAG, " plan_code: " + ConfigUtils.plan_code);
		LogUtil.i(LogUtil.TAG, " device_dpi: " + ConfigUtils.device_dpi);
		LogUtil.i(LogUtil.TAG, " system_ver: " + ConfigUtils.system_ver);
	}
}
