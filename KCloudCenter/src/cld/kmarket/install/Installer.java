package cld.kmarket.install;

import com.cld.log.CldLog;
import android.content.Context;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

public class Installer implements Runnable
{
	private static final String TAG = "Installer";
	private Context mContext;
	private InstallInfo mInfo;
	private Handler mHandler;
	private PackageInfo mPkgInfo;
	private static final int FRESH_TIME = 500;
	
	public Installer(Context context, InstallInfo info, Handler handle) 
	{
		mContext = context;
		mInfo = info;
		mHandler = handle;
		if (mHandler != null) 
		{
			mHandler.postDelayed(this, FRESH_TIME);
		}
		
		Message msg = new Message();
		if(mInfo.isInstallFlag())
		{
			msg.what = InstallerManager.INSTALL_START;
		}
		else 
		{
			msg.what = InstallerManager.DELETE_START;
		}
		msg.obj = mInfo;
		mHandler.sendMessage(msg);
		
		if(mInfo.isInstallFlag())
		{
			mInfo.setTotalProgress(InstallerInter.INSTALL_TOTAL_PROGRESS);
			PackageInstall();
		}
		else 
		{
			mInfo.setTotalProgress(InstallerInter.DELETE_TOTAL_PROGRESS);
			PackageDelete();
		}
	}

	public void run() 
	{
		int mCurProgress = mInfo.getCurProgress() + 1;
		mInfo.setCurProgress(mCurProgress);
		if(mInfo.getCurProgress() < mInfo.getTotalProgress())
		{
			if (mHandler != null) 
			{
				mHandler.postDelayed(this, FRESH_TIME);
			}
			Message msg = new Message();
			if(mInfo.isInstallFlag())
			{
				msg.what = InstallerManager.INSTALL_PROGRESS;
			}
			else 
			{
				msg.what = InstallerManager.DELETE_PROGRESS;
			}
			
			msg.obj = mInfo;
			mHandler.sendMessage(msg);
		}
	}
	
	private void stopInstallProgress()
	{
		if(mHandler != null)
		{
			mHandler.removeCallbacks(this);
		}
	}

	public void PackageInstall() 
	{
		int installFlags = 0;
		PackageManager pm = mContext.getPackageManager();
		try 
		{
		mPkgInfo = pm.getPackageArchiveInfo(mInfo.getPath(),
				PackageManager.GET_PERMISSIONS
						| PackageManager.GET_UNINSTALLED_PACKAGES);

			PackageInfo pi = pm.getPackageInfo(mPkgInfo.packageName,
					PackageManager.GET_UNINSTALLED_PACKAGES);
			if (pi != null) 
			{
				installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
				installFlags |= PackageManager.INSTALL_ALLOW_DOWNGRADE;
			}
		} 
		catch (Exception e) 
		{
			CldLog.i(TAG, "PackageInstall" + e.toString());
		}

		if ((installFlags & PackageManager.INSTALL_REPLACE_EXISTING) != 0) 
		{
			CldLog.i(TAG, "Replacing package:" + mPkgInfo.packageName);
		}

		PackageInstallObserver observer = new PackageInstallObserver();
		try 
		{
			pm.installPackage(Uri.parse("file://" + mInfo.getPath()), 
					observer, installFlags, mPkgInfo.packageName);
		} 
		catch (Exception e) 
		{
			CldLog.i(TAG, "PackageInstall" + e.toString());
		}
	}

	class PackageInstallObserver extends IPackageInstallObserver.Stub 
	{
		public void packageInstalled(String packageName, int returnCode) 
		{
			stopInstallProgress();
			Message msg = new Message();
			msg.what = InstallerManager.INSTALL_COMPLETE;
			msg.arg1 = returnCode;
			msg.obj = mInfo;
			mHandler.sendMessage(msg);
			CldLog.i(TAG, "PackageInstall" + "install_complete" +
					", returnCode = " + returnCode);
		}
	}
	
	@SuppressWarnings("unused")
	public void PackageDelete()
	{
		try 
		{		 
			PackageManager pm = mContext.getPackageManager();
			CldLog.i(TAG, "PackageDelete info.getPkgName() = " 
					+ mInfo.getPkgName());
			mPkgInfo = pm.getPackageInfo(mInfo.getPkgName(), 
					PackageManager.GET_PERMISSIONS);

			PackageDeleteObserver observer = new PackageDeleteObserver();
			mContext.getPackageManager().deletePackage(mPkgInfo.packageName, 
					observer, true ? PackageManager.DELETE_ALL_USERS : 0);
		} 
		catch (Exception e) 
		{
			CldLog.i(TAG, " PackageDelete: " + e.toString());
		}
	}
	
	class PackageDeleteObserver extends IPackageDeleteObserver.Stub 
	{
		public void packageDeleted(String packageName, int returnCode) 
		{
			stopInstallProgress();
			
			Message msg = new Message();
			msg.what = InstallerManager.DELETE_COMPLETE;
			msg.arg1 = returnCode;
			msg.obj = mInfo;
			mHandler.sendMessage(msg);
			CldLog.i(TAG, " PackageDelete " + " delete_complete: " +
					", returnCode: " + returnCode);
		}
	}
}
