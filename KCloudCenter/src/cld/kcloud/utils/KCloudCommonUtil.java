package cld.kcloud.utils;

import java.text.SimpleDateFormat;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Log;
import cld.kcloud.center.KCloudAppUtils;
import cld.kcloud.center.KCloudAppUtils.CLDMessageId;
import cld.kcloud.center.KCloudCtx;
import cld.kcloud.user.KCloudUser;
import com.cld.customview.Toast.CLDToast;

public class KCloudCommonUtil 
{
	private static final String TAG = "KCloudCommonUtil";
	
	public static void makeText(int strid)
	{
		CLDToast.show(KCloudCtx.getAppContext(), 
				KCloudCtx.getAppContext().getResources().getString(strid),
				false);
	}
	
	public static void makeText(String str)
	{
		CLDToast.show(KCloudCtx.getAppContext(), str, false);
	}
	
	public static void makeTextLong(int strid) 
	{
		CLDToast.show(KCloudCtx.getAppContext(), 
				KCloudCtx.getAppContext().getResources().getString(strid),
				true);
	}
	
	public static void makeTextLong(String str) 
	{
		CLDToast.show(KCloudCtx.getAppContext(), str, true);
	}
	
	public static String getString(int strid)
	{
		return KCloudCtx.getAppContext().getResources().getString(strid);
	}
	
	public static String[] getStringArray(int id)
	{
		return KCloudCtx.getAppContext().getResources().getStringArray(id);
	}
	
	public static int getColor(int colorid)
	{
		return KCloudCtx.getAppContext().getResources().getColor(colorid);
	}
	
	@SuppressLint("SimpleDateFormat") 
	public static boolean isSameDay(long ltime_first, long ltime_second) 
	{
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
		String dateStringFirstTime = sdf.format(ltime_first);
		String dateStringSecondTime = sdf.format(ltime_second);
		
		return dateStringFirstTime.equals(dateStringSecondTime);
	}
	
	/**
	 * @Title: isActivityShow
	 * @Description: 判断activity是否可见
	 * @param activityName
	 * @return: boolean
	 */
	@SuppressLint("NewApi") 
	public static boolean isActivityShow(String activityName) 
	{
	    boolean result = false;  
	    if (activityName != null && !activityName.isEmpty()) 
	    {
	    	ActivityManager am = (ActivityManager) KCloudCtx.getAppContext().
		    		getSystemService(Context.ACTIVITY_SERVICE);  
		    ComponentName cn = am.getRunningTasks(2).get(0).topActivity;  
		    if (cn != null) 
		    {  
		        if (activityName.equals(cn.getClassName())) 
		        {  
		            result = true;  
		        }  
		    }  
	    }
	    return result;  
	}
	
	/**
	 * @Title: sendCloseServiceInterface
	 * @Description: 通知Launcher关闭服务协议界面
	 * @return: void
	 */
	public static void sendCloseServiceInterface()
	{
		Intent broadcast = new Intent(KCloudAppUtils.ACTION_KCLOUD_LOGIN_FINISH);
		KCloudCtx.getAppContext().sendBroadcast(broadcast, null);
	}
	
	/**
	 * @Title: sendFreshFlowBroadcast
	 * @Description: 发送刷新流量的广播
	 * @param flag：true 通知流量统计界面刷新流量
	 * @return: void
	 */
	public static void sendFreshFlowBroadcast(boolean flag)
	{
		Intent intent = new Intent(KCloudAppUtils.ACTION_FLOW_FRESH);
		KCloudCtx.getAppContext().sendBroadcast(intent);
		
		if (flag)
			KCloudUser.getInstance().sendMessage(CLDMessageId.MSG_ID_KLDJY_FLOW_GET_FRESH, 0);
	}
	
	public static void sendGetFlowSuccessBroadcast()
	{
		Intent intent = new Intent(KCloudAppUtils.ACTION_FLOW_GET_SUCCESS);
		KCloudCtx.getAppContext().sendBroadcast(intent);
	}
	
	public static void sendGetFlowFailBroadcast()
	{
		Intent intent = new Intent(KCloudAppUtils.ACTION_FLOW_GET_FAILED);
		KCloudCtx.getAppContext().sendBroadcast(intent);
	}
	
	/**
	 * 
	 * @Title: startActivity
	 * @Description: 启动activity
	 * @param activityName
	 * @return: void
	 */
	public static void startActivity(String activityName)
	{
		Intent intent = new Intent();
		intent.setClassName(KCloudAppUtils.TARGET_PACKAGE_NAME, activityName);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		KCloudCtx.getAppContext().startActivity(intent);
	}
	
	/**
	 * 
	 * @Title: isRunBackground
	 * @Description: 判断程序的运行在前台还是后台 
	 * @param activityName
	 * @return 0: 后台运行; 1: 前台运行; 2: 正在运行
	 * @return: int
	 */
   public static int isRunBackground(String activityName) 
   {  
	   Context context = KCloudCtx.getAppContext();
       ActivityManager activityManager = (ActivityManager) context  
               .getSystemService(Context.ACTIVITY_SERVICE);  
       List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);  
       if (tasksInfo.size() > 0) 
       {  
           ComponentName topConponent = tasksInfo.get(0).topActivity;  
           if (KCloudAppUtils.TARGET_PACKAGE_NAME.equals(topConponent.getPackageName())) 
           {  
               if (topConponent.getClassName().equals(activityName)) 
               {  
                   //正在运行
                   return 2;  
               }  
               //前台运行
               return 1;  
           } 
           else 
           {  
               //后台运行
               return 0;  
           }  
       }  
       return 0;  
   }
   
   /**
	 * 根据Pkgname获取Vercode
	 * @param pkgname
	 */
	public static int getVercodeByPkgname(String pkgname)
	{
		int versionCode = 0;
		Context context = KCloudCtx.getAppContext();
		PackageManager pm = context.getPackageManager();
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
	
	/**
	 * @Title: isPackageExist
	 * @Description: 判断pkgname对应的安装包是否已安装
	 * @param pkgname
	 * @return: boolean
	 */
	@SuppressLint("NewApi") 
	public static boolean isPackageExist(String pkgname) 
	{
		if (pkgname == null || pkgname.isEmpty())
			return false;
		
		try 
		{
			Context context = KCloudCtx.getAppContext();
			ApplicationInfo info = context.getPackageManager()
					.getApplicationInfo(pkgname,
							PackageManager.GET_UNINSTALLED_PACKAGES);

			if (info == null)
			{
				return false;
			}
			else
			{
				return true;
			}
		} 
		catch (NameNotFoundException e) 
		{
			return false;
		}
	}
	
	public static String getSystemVer()
	{
	    //String device_model = Build.MODEL;            // 设备型号  
	    //String version_sdk = Build.VERSION.SDK;       // 设备SDK版本  
	    String version_release = Build.VERSION.RELEASE; // 设备的系统版本  
	    
	    //Log.i(TAG, "   device_model: " + device_model);
	    //Log.i(TAG, "    version_sdk: " + version_sdk);
	    Log.i(TAG, "version_release: " + version_release);
	    return version_release;
	}
}
