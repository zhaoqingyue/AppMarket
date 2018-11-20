package cld.kmarket.install;

import java.util.ArrayList;
import com.cld.log.CldLog;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class InstallerManager 
{
	public static final int INSTALL_START = 1;
	public static final int INSTALL_COMPLETE = 2;
	public static final int INSTALL_PROGRESS = 3;
	public static final int DELETE_START = 4;
	public static final int DELETE_COMPLETE = 5;
	public static final int DELETE_PROGRESS = 6;
	
	private static final String TAG = "InstallerManage";
	private static InstallerManager sInstance = null;
	private Context mContext = null;
	private Handler mHandler = null;
	private ArrayList<InstallInfo> mInstallList = null; //安装列表
	private InstallInfo mCurInstall = null;			    //当前安装
	private InstallThread mInstallThread = null;		//安装线程

	public static synchronized InstallerManager getInstance(Context context) 
	{
		if (sInstance == null) 
		{
			sInstance = new InstallerManager(context);
		}
		return sInstance;
	}

	@SuppressLint("HandlerLeak") 
	public InstallerManager(Context context) 
	{
		mContext = context;
		mHandler = new Handler() 
		{
			public void handleMessage(Message msg) 
			{
				InstallMessage(msg);
				super.handleMessage(msg);
			}
		};
		mInstallList = new ArrayList<InstallInfo>();
		mInstallThread = new InstallThread();
		mInstallThread.start();
	}

	public synchronized void addInstallList(InstallInfo info) 
	{
		if (info != null) 
		{
			mInstallList.add(info);
		}
	}

	public synchronized void removeInstallList(InstallInfo info) 
	{
		if (info != null) 
		{
			mInstallList.remove(info);
		}
	}

	public synchronized boolean existInstall() 
	{
		if(mCurInstall != null)
		{
			return true;
		}
		else 
		{
			return false;
		}	
	}
	
	public InstallInfo getCurInstall() 
	{
		return mCurInstall;
	}

	public void setCurInstall(InstallInfo mCurInstall) 
	{
		this.mCurInstall = mCurInstall;
	}

	public synchronized void startInstall() 
	{
		setCurInstall(mInstallList.get(0));
		new Installer(mContext, mCurInstall, mHandler);
	}
	
	public static void sendVersionInfo(Context context)
	{
		Intent intent = new Intent();
		intent.setAction(InstallerInter.ACTION_INSTALL_VERSION);
		Bundle bundle = new Bundle();
		bundle.putString(InstallerInter.VERSION_NUMBER, getVerName(context));
		intent.putExtras(bundle);
		context.sendBroadcast(intent);
	}
	
	public static String getVerName(Context context) 
	{
		String verName = "";
		try 
		{
			verName = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;
		} 
		catch (NameNotFoundException e) 
		{
			CldLog.e(TAG, e.getMessage());
		}
		return verName;
	}
	
	/**
	 * @Title: InstallMessage
	 * @Description: 所有安装消息处理
	 * @param msg
	 * @return: void
	 */
	public void InstallMessage(Message msg) 
	{
		switch (msg.what) 
		{
		case INSTALL_START:
		{
			InstallInfo info= (InstallInfo)msg.obj;
			if(info != null)
			{
				Intent intent = new Intent();
				intent.setAction(InstallerInter.ACTION_INSTALL_START);
				Bundle bundle = new Bundle();
				bundle.putString(InstallerInter.APP_NAME, info.getAppName());
				bundle.putString(InstallerInter.PACKAGE_NAME, info.getPkgName());
				intent.putExtras(bundle);
				mContext.sendBroadcast(intent);
			}	
			break;
		}
		case INSTALL_PROGRESS:
		{
			InstallInfo info = (InstallInfo) msg.obj;
			if (info != null) 
			{
				Intent intent = new Intent();
				intent.setAction(InstallerInter.ACTION_INSTALL_PROGRESS);
				Bundle bundle = new Bundle();
				bundle.putString(InstallerInter.APP_NAME, info.getAppName());
				bundle.putString(InstallerInter.PACKAGE_NAME, info.getPkgName());
				bundle.putInt(InstallerInter.CURRENT_PROGRESS, info.getCurProgress());
				bundle.putInt(InstallerInter.TOTAL_PROGRESS, info.getTotalProgress());
				intent.putExtras(bundle);
				mContext.sendBroadcast(intent);
			}
			break;
		}
		case INSTALL_COMPLETE:
		{
			InstallInfo info= (InstallInfo)msg.obj;
			if (info != null) 
			{
				Intent intent = new Intent();
				intent.setAction(InstallerInter.ACTION_INSTALL_COMPLETE);
				Bundle bundle = new Bundle();
				bundle.putString(InstallerInter.APP_NAME, info.getAppName());
				bundle.putString(InstallerInter.PACKAGE_NAME, info.getPkgName());
				bundle.putInt(InstallerInter.RET_CODE, msg.arg1);
				intent.putExtras(bundle);
				mContext.sendBroadcast(intent);
				
				removeInstallList(mCurInstall);
				setCurInstall(null);
			}
			break;
		}
		case DELETE_START:
		{
			InstallInfo  info= (InstallInfo)msg.obj;
			if(info != null)
			{
				Intent intent = new Intent();
				intent.setAction(InstallerInter.ACTION_DELETE_START);
				Bundle bundle = new Bundle();
				bundle.putString(InstallerInter.APP_NAME, info.getAppName());
				bundle.putString(InstallerInter.PACKAGE_NAME, info.getPkgName());
				intent.putExtras(bundle);
				mContext.sendBroadcast(intent);
			}	
			break;
		}
		case DELETE_PROGRESS:
		{
			InstallInfo info = (InstallInfo) msg.obj;
			if (info != null) 
			{
				Intent intent = new Intent();
				intent.setAction(InstallerInter.ACTION_DELETE_PROGRESS);
				Bundle bundle = new Bundle();
				bundle.putString(InstallerInter.APP_NAME, info.getAppName());
				bundle.putString(InstallerInter.PACKAGE_NAME, info.getPkgName());
				bundle.putInt(InstallerInter.CURRENT_PROGRESS, info.getCurProgress());
				bundle.putInt(InstallerInter.TOTAL_PROGRESS, info.getTotalProgress());
				intent.putExtras(bundle);
				mContext.sendBroadcast(intent);
			}
			break;
		}
		case DELETE_COMPLETE:
		{
			InstallInfo info= (InstallInfo)msg.obj;
			if (info != null)
			{
				Intent intent = new Intent();
				intent.setAction(InstallerInter.ACTION_DELETE_COMPLETE);
				Bundle bundle = new Bundle();
				bundle.putString(InstallerInter.APP_NAME, info.getAppName());
				bundle.putString(InstallerInter.PACKAGE_NAME, info.getPkgName());
				bundle.putInt(InstallerInter.RET_CODE, msg.arg1);
				intent.putExtras(bundle);
				mContext.sendBroadcast(intent);
				
				removeInstallList(mCurInstall);
				setCurInstall(null);
			}
			break;
		}
		default:
			break;
		}
	}
	
	/**
	 * 安装线程
	 */
	public class InstallThread extends Thread 
	{
		public InstallThread() 
		{
		}

		@Override
		public void run() 
		{
			try 
			{
				super.run();
				while (true) 
				{
					if(!existInstall())
					{
						if(mInstallList.size() > 0)
						{
							//开始一个处理
							startInstall();
						}
						else 
						{
							//队列没有待处理安装
							sleep(2000);
						}
					}
					else 
					{
						//队列安装正在处理
						sleep(200);
					}	
				}
			} 
			catch (Exception e) 
			{
				CldLog.e(TAG, "InstallThread error: " + e.toString());
			}
		}
	}
}
