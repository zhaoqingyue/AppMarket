package cld.kmarcket.receiver;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import cld.kmarcket.appinfo.AppInfo;
import cld.kmarcket.appinfo.NetApp;
import cld.kmarcket.appinfo.UpdateDownloadTime;
import cld.kmarcket.packages.PackageTable;
import cld.kmarcket.util.CommonUtil;
import cld.kmarcket.util.ConstantUtil;
import cld.kmarcket.util.LauncherUtil;
import cld.kmarcket.util.LogUtil;

public class PackageReceiver extends BroadcastReceiver 
{
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED))
		{
			Uri data = intent.getData();
			String pkgName = data.getEncodedSchemeSpecificPart();
			boolean replace = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
			LogUtil.i(LogUtil.TAG, "ACTION_PACKAGE_ADDED replace: "  + replace);
			
			if (!replace)
			{
				Message msg = new Message();
				msg.what= ConstantUtil.MSG_ADDED_SUC;
				msg.obj = (Object)pkgName;
				mHandler.sendMessage(msg);
			}
		}
		else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED))
		{
			Uri data = intent.getData();
			String pkgName = data.getEncodedSchemeSpecificPart();
			LogUtil.i(LogUtil.TAG, "ACTION_PACKAGE_REPLACED");
			
			Message msg = new Message();
			msg.what= ConstantUtil.MSG_REPLACED_SUC;
			msg.obj = (Object)pkgName;
			mHandler.sendMessage(msg);
		}
		else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED))
		{
			Uri data = intent.getData();
			String pkgName = data.getEncodedSchemeSpecificPart();
			boolean replace = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
			LogUtil.i(LogUtil.TAG, "ACTION_PACKAGE_REMOVED replace: " + replace);
		
			if (!replace)
			{
				Message msg = new Message();	
				msg.what= ConstantUtil.MSG_REMOVED_SUC;
				msg.obj = (Object)pkgName;	
				mHandler.sendMessage(msg);
			}
		}
	}
	
	@SuppressLint("HandlerLeak") 
	Handler mHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			switch (msg.what) 
			{
			case ConstantUtil.MSG_ADDED_SUC:
			{
				String pkgName = (String)msg.obj;
				LogUtil.i(LogUtil.TAG, "added pkgName: " + pkgName);
				
				AppInfo appinfo1 = CommonUtil.getAppInfoByPkgName(pkgName, 
						NetApp.getInstance().getNetApps());
				if (appinfo1 != null)
				{
					//通过推荐应用安装
					CommonUtil.updateAppType(pkgName, ConstantUtil.INSTALL_STATUS_FINISH);
					//发送安装成功广播，更新应用状态
					CommonUtil.sendPackageAddedSuc(pkgName);
					insertPackage(appinfo1);
					updateDownloadTimes(pkgName);
				}
				else
				{
					//其它途径安装
					AppInfo appinfo2 = CommonUtil.getAppInfoByPkgname(pkgName);
					if (appinfo2 != null)
					{
						/**
						 * 不需要更新状态
						 * 避免手动安装后，后台配置升级包，因为这个状态导致不显示"更新",而是显示"打开"
						 */
						/*CommonUtil.updateAppType(pkgName, 
								ConstantUtil.INSTALL_STATUS_FINISH);*/
						insertPackage(appinfo2);
					}
				}
				break;
			}
			case ConstantUtil.MSG_REPLACED_SUC:
			{
				String pkgName = (String)msg.obj;
				LogUtil.i(LogUtil.TAG, "replaced pkgName: " + pkgName);
				CommonUtil.updateAppType(pkgName, ConstantUtil.INSTALL_STATUS_FINISH);
				//发送替换成功广播，更新应用状态
				CommonUtil.sendPackageReplacedSuc(pkgName);
				updatePackage(pkgName);
				updateDownloadTimes(pkgName);
				if (pkgName.equals("com.cld.launcher"))
				{
					CommonUtil.updateAppType(pkgName, ConstantUtil.DOWNLOAD_STATUS_DEFAULT);
					LogUtil.i(LogUtil.TAG, "++++++Launcher升级成功 ++++++");
					LauncherUtil.onLauncherUpgradeFinish();
				}
				else if (pkgName.equals("cld.kmarcket"))
				{
					LogUtil.i(LogUtil.TAG, "++++++K应用升级成功 ++++++");
					CommonUtil.updateAppType(pkgName, ConstantUtil.DOWNLOAD_STATUS_DEFAULT);
				}
				break;
			}
			case ConstantUtil.MSG_REMOVED_SUC:
			{
				String pkgName = (String)msg.obj;
				LogUtil.i(LogUtil.TAG, "removeed pkgName: " + pkgName);
				CommonUtil.updateAppType(pkgName, ConstantUtil.DOWNLOAD_STATUS_DEFAULT);
				//发送卸载成功广播，更新应用状态
				CommonUtil.sendPackageRemovedSuc(pkgName);
				deletePackage(pkgName);
				break;
			}
			default:
				break;
			}
		}
	};
	
	/**
	 * 插入到valid_pakcages表
	 * @param appinfo
	 */
	private void insertPackage(AppInfo appinfo)
	{
		//是否是Widget 0：否； 1：是
		appinfo.setIsWidget(ConstantUtil.APP_WIDGET_NO);
		PackageTable.getInstance().insertPackage(appinfo);
	}
	
	/**
	 * 从valid_pakcages表中删除
	 * @param pkgName
	 */
	private void deletePackage(String pkgName)
	{
		ArrayList<String> packagenames = PackageTable.getInstance()
				.queryPackageName();
		for (String pkgname : packagenames)
		{
			if (pkgName.equals(pkgname))
			{
				PackageTable.getInstance().deletePackage(pkgName);
			}
		}
	}
	
	/**
	 * 更新vercode到Package表
	 * @param pkgName
	 */
	private void updatePackage(String pkgName)
	{
		AppInfo appinfo = new AppInfo();
		appinfo.setPkgName(pkgName);
		appinfo.setVerCode(CommonUtil.getVercodeByPkgname(pkgName));
		PackageTable.getInstance().updatePackage(appinfo);
	}
	
	/**
	 * 更新下载次数
	 * @param pkgName
	 */
	private void updateDownloadTimes(String pkgName)
	{
		AppInfo appinfo = new AppInfo();
		appinfo.setPkgName(pkgName);
		appinfo.setVerCode(CommonUtil.getVercodeByPkgname(pkgName));
		UpdateDownloadTime.getInstance().updateAppDownloadTime(appinfo);
	}
}
