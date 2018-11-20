/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: InstallManager.java
 * @Prject: KCloudCenter_M550
 * @Package: cld.kmarket.install
 * @Description: 安装管理器
 * @author: zhaoqy
 * @date: 2016年8月3日 下午3:47:09
 * @version: V1.0
 */

package cld.kmarket.install;

import java.io.File;
import com.cld.log.CldLog;
import cld.kmarket.download.DownloadDir;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class InstallManager 
{
	private static final String TAG = "InstallManager";   
	private static InstallManager mInstance = null;
	
	static public InstallManager getInstance() 
	{
		if (mInstance == null) 
		{
			synchronized (InstallManager.class) 
			{
				if (mInstance == null) 
				{
					mInstance = new InstallManager();
				}
			}
		}
		return mInstance;
	}
	
	/**
	 * 开始静默安装
	 * @param urlPath
	 */
	@SuppressLint("NewApi") 
	public void startSlienceInstall(Context context, String urlPath)
	{
		if (context == null || urlPath == null || urlPath.isEmpty())
			return;
		
		try 
		{
			String path = DownloadDir.getDownloadDir();
			String name = urlPath.substring(urlPath.lastIndexOf("/") + 1);
			CldLog.i(TAG, "SlienceInstall path: " + path + ", name: " + name);
			File apkfile = new File(path, name);
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(apkfile), 
					"cld_application/vnd.android.package-archive");
			context.startService(intent);
		} 
		catch (Exception e) 
		{
			CldLog.e(TAG, "error: " + e.toString());
		}
	}
	
	/**
	 * 开始静默卸载
	 * @param pkgName
	 */
	@SuppressLint("NewApi") 
	public void startSlienceUninstall(Context context, String pkgName)
	{
		if (context == null || pkgName == null || pkgName.isEmpty())
			return;
		
		try 
		{
			CldLog.i(TAG, "startSlienceUninstall pkgName: " + pkgName);
			Intent intent = new Intent();
		    intent.setAction(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse("package:" + pkgName), 
					"cld_application/vnd.android.package-archive");
			context.startService(intent);
		} 
		catch (Exception e) 
		{
			CldLog.e(TAG, "error: " + e.toString());
		}
	}
	
	/**
	 * 常规方式安装
	 * @param context
	 * @param packageName
	 */
	@SuppressLint("NewApi") 
	public void startNormalInstall(Context context, String urlPath)
	{
		if (context == null || urlPath == null || urlPath.isEmpty())
			return;
		
		String path = DownloadDir.getDownloadDir();
		String name = urlPath.substring(urlPath.lastIndexOf("/") + 1);
		CldLog.i(TAG, "startNormalInstall path: " + path + ", name: " + name);
		File apkfile = new File(path, name);
		if (!apkfile.exists())
			return;
		
		try 
		{
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			intent.setDataAndType(Uri.fromFile(apkfile), 
					"application/vnd.android.package-archive");
			context.startActivity(intent);
		} 
		catch (Exception e) 
		{
			CldLog.e(TAG, "error: " + e.toString());
		}
	}
	
	/**
	 * 常规方式卸载
	 * @param context
	 * @param packageName
	 */
	@SuppressLint("NewApi") 
	public void startNormalUninstall(Context context, String pkgName) 
	{
		if (context == null || pkgName == null || pkgName.isEmpty())
			return;
		
		try 
		{
			CldLog.i(TAG, "startNormalInstall pkgName: " + pkgName);
			Uri packageURI = Uri.parse("package:" + pkgName);   
			Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);   
			context.startActivity(uninstallIntent);
		} 
		catch (Exception e) 
		{
			CldLog.e(TAG, "error: " + e.toString());
		}
	}
}
