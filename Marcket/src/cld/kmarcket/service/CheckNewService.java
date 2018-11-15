package cld.kmarcket.service;

import java.io.File;
import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import cld.kmarcket.KMarcketApplication;
import cld.kmarcket.appinfo.AppInfo;
import cld.kmarcket.appinfo.InstalledApp;
import cld.kmarcket.appinfo.UpgradeApp;
import cld.kmarcket.packages.PackageTable;
import cld.kmarcket.util.CommonUtil;
import cld.kmarcket.util.ConfigUtils;
import cld.kmarcket.util.ConstantUtil;
import cld.kmarcket.util.FileUtil;
import cld.kmarcket.util.LocationUtils;
import cld.kmarcket.util.LocationUtils.IKCloudLocationListener;
import cld.kmarcket.util.LogUtil;
import cld.kmarcket.util.RegionUtils;
import cld.kmarcket.util.ShareUtil;
import cld.weather.api.NetUtil;
import com.download.api.DownloadDir;
import com.download.api.DownloadManager;
import com.download.api.DownloadWatchManager;
import com.download.api.Status;
import com.download.api.StatusDao;
import com.download.api.TaskStatus;

public class CheckNewService extends Service 
{
	public final static int MSG_START_LOCATION = 3;
	public final static int MSG_START_REQUEST = 4;
	public final static int MSG_START_DOWNLOAD = 5;
	
	private Context mContext; 
	private ArrayList<AppInfo> installedList;
	private long mDuid = 0;  
	private long mKuid = 0;
	private int mRegionId = 0;
	
	@Override
	public void onCreate() 
	{
		super.onCreate();
		mContext = this;
		LogUtil.d(LogUtil.TAG, "CheckNewService");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		checkUpgradeApps();
		return super.onStartCommand(intent, flags, startId);
	}
	
	private void checkUpgradeApps()
	{
		//先检测是否已经获取到静默升级列表
		ArrayList<AppInfo> quiesceUpgradeApp = UpgradeApp.
				getInstance().getQuiesceUpgradeAppList();
		LogUtil.i(LogUtil.TAG, "Quiesce size: " + quiesceUpgradeApp.size());
		if (quiesceUpgradeApp != null && quiesceUpgradeApp.size() > 0)
		{
			for (int i=0; i<quiesceUpgradeApp.size(); i++)
			{
				AppInfo appinfo = quiesceUpgradeApp.get(i);
				LogUtil.i(LogUtil.TAG, "PkgName: " + appinfo.getPkgName());
				onQuiesceUpgradeApp(appinfo);
			}
		}
		else
		{
			mHandler.sendEmptyMessage(MSG_START_LOCATION);
		}
	}
	
	private void startloadUpdateApps()
	{
		installedList = PackageTable.getInstance().queryPackages();
		if (ShareUtil.getBoolean("first", true))
		{
			ArrayList<AppInfo> installed = InstalledApp.getInstance().getInstalledApps();
			ShareUtil.put("first", false);
			ArrayList<AppInfo> temp = new ArrayList<AppInfo>();
			temp.clear();
			temp.addAll(installedList);
			
			for (int i=0; i<temp.size(); i++)
			{
				String pkgname = temp.get(i).getPkgName();
				if (!CommonUtil.isInstalledApp(pkgname, installed))
				{
					PackageTable.getInstance().deletePackage(pkgname);
					installedList.remove(temp.get(i));
				}
			}
		}
		//CommonUtil.configureTest();
		getDuidKuid();
		UpgradeApp.getInstance().loadUpdateApps(mHandler, installedList, 0, 0, mDuid, mKuid, mRegionId);
	}
	
	@Override
	public IBinder onBind(Intent intent) 
	{
		return null;
	}
	
	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		if(mHandler != null)
		{
			mHandler.removeCallbacksAndMessages(null);
		}
	}
	
	private void initCurAddr()
	{
		LogUtil.i(LogUtil.TAG, " initCurAddr ");
		LocationUtils.startLocation(new IKCloudLocationListener() 
		{
			@Override
			public void onLocation(double latitude, double longtitude) 
			{
				LogUtil.i(LogUtil.TAG, "latitude: " + latitude + ", longtitude: " + longtitude);
				RegionUtils.getRegionDistsName(longtitude, latitude,
						new RegionUtils.IGetRigonCallback() 
						{
							@SuppressLint("NewApi")
							@Override
							public void onResult(int regionId, String provinceName,
									String cityName, String distsName) {
								LogUtil.i(LogUtil.TAG, "regionId: " + regionId);
								//深圳区域regionId: 440300
								mRegionId = regionId;
								mHandler.sendEmptyMessage(MSG_START_REQUEST);
							}
						});
			}
		});
	}
	
	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() 
	{
		public void handleMessage(Message msg) 
		{
			switch (msg.what) 
			{
			case MSG_START_LOCATION:
			{
				initCurAddr();
				break;
			}
			case MSG_START_REQUEST:
			{
				startloadUpdateApps();
				break;
			}
			case MSG_START_DOWNLOAD:
			{
				Bundle bundle = msg.getData();
				if (bundle != null)
				{
					AppInfo appinfo = bundle.getParcelable("appinfo");
					if (appinfo != null)
					{
						new QuiesceDownload(appinfo);
					}
				}
				break;
			}
			case ConstantUtil.MSG_GET_APP_UPGRADE_SUC:
			{
				LogUtil.i(LogUtil.TAG, " MSG_GET_APP_UPGRADE_SUC ");
				ArrayList<AppInfo> quiesceUpgradeApp = UpgradeApp.
						getInstance().getQuiesceUpgradeAppList();
				LogUtil.i(LogUtil.TAG, "Quiesce size: " + quiesceUpgradeApp.size());
				if (quiesceUpgradeApp != null && quiesceUpgradeApp.size() > 0)
				{
					for (int i=0; i<quiesceUpgradeApp.size(); i++)
					{
						AppInfo appinfo = quiesceUpgradeApp.get(i);
						LogUtil.i(LogUtil.TAG, "PkgName: " + appinfo.getPkgName());
						onQuiesceUpgradeApp(appinfo);
					}
				}
				break;
			}
			default:
				break;
			}
		}
	};
	
	private void getDuidKuid()
	{
		Context c = null;
		try 
		{
			c = mContext.createPackageContext(ConstantUtil.PREFERENCE_PACKAGE,
					Context.CONTEXT_IGNORE_SECURITY);
		} 
		catch (NameNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		SharedPreferences sharedPreferences = c.getSharedPreferences(
				ConstantUtil.PREFERENCE_NAME, ConstantUtil.MODE);  
		mDuid = sharedPreferences.getLong(ConstantUtil.TARGET_FIELD_DUID, 0);  
		mKuid = sharedPreferences.getLong(ConstantUtil.TARGET_FIELD_KUID, 0);  
		LogUtil.i(LogUtil.TAG, " duid: " + mDuid + ", kuid: " + mKuid);
	}
	
	private void onQuiesceUpgradeApp(AppInfo appinfo)
	{
		String pkgName = appinfo.getPkgName();
		if (pkgName.equals("cld.kmarcket") || pkgName.equals("com.cld.launcher")) 
		{
			String urlPath = appinfo.getAppUrl();
			String path = DownloadDir.getDownloadDir();
			String name = urlPath.substring(urlPath.lastIndexOf("/") + 1);
			LogUtil.i(LogUtil.TAG, "path: " + path + ", name: " + name);
			File apkfile = new File(path, name);
			if (apkfile.exists()) 
			{
				LogUtil.i(LogUtil.TAG, name + " exist ");
				Status status = StatusDao.getInstance(mContext).query(pkgName);
				if (status != null) 
				{
					LogUtil.i(LogUtil.TAG, "Type: " + status.getType());
					//下载完成或安装失败，则直接安装
					if (status.getType() == ConstantUtil.DOWNLOAD_STATUS_FINISH 
					 || status.getType() == ConstantUtil.INSTALL_STATUS_FAILED)
					{
						LogUtil.i(LogUtil.TAG, "install  ");
						CommonUtil.startSlienceInstall(urlPath);
					}
				}
			}
			else 
			{
				//new QuiesceDownload(appinfo);
				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putParcelable("appinfo", appinfo);
				message.setData(bundle);
				message.what = MSG_START_DOWNLOAD;
				mHandler.sendMessageDelayed(message, 5*60*1000);
			}
		}
	}
	
	private final class QuiesceDownload
	{
		private AppInfo appinfo;
		private String urlPath;
		private long fileLen;
		
		public QuiesceDownload(AppInfo appinfo)
		{
			this.appinfo = appinfo;
			this.urlPath = this.appinfo.getAppUrl();
			
			if (FileUtil.isEnough(mContext, appinfo.getPackSize()))
			{
				DownloadManager.getInstance(KMarcketApplication.getContext()).
					addDownloadTask(urlPath);
				DownloadWatchManager.getInstance().registerWachter(urlPath, 
				mDownloadCallback);
			}
		}
		
		private TaskStatus.ITaskCallBack mDownloadCallback = 
				new TaskStatus.ITaskCallBack()
		{
			@Override
			public void updateTaskStatus(int status) 
			{
				Message msg = handler.obtainMessage(2);
				msg.getData().putInt("status", status);
				handler.sendMessage(msg);
			}
			
			@Override
			public void updateDownloadProcess(long downLength,
					long fileLength) 
			{
				if (fileLength != fileLen) 
				{
					Message msg = handler.obtainMessage(0);
					msg.getData().putLong("fileLen", fileLength);
					handler.sendMessage(msg);
				}
	
				Message msg = handler.obtainMessage(1);
				msg.getData().putLong("done", downLength);
				handler.sendMessage(msg);
			}
		};
		
		@SuppressLint("HandlerLeak") 
		private Handler handler = new Handler() 
		{
			@Override
			public void handleMessage(Message msg) 
			{
				switch (msg.what) 
				{
				case 0: //获取文件大小
				{
					fileLen = msg.getData().getLong("fileLen");
					break;
				}
				case 1: //获取下载大小
				{
					//获取当前下载的总量
					long done = msg.getData().getLong("done");
					break;
				}
				case 2: //获取下载状态
				{
					switch (msg.getData().getInt("status")) 
					{
					case TaskStatus.DOWNLOAD_STATUS_ING:
					{
						break;
					}
					case TaskStatus.DOWNLOAD_STATUS_END:
					{
						//下载完成
						CommonUtil.updateAppType(appinfo.getPkgName(), 
								ConstantUtil.DOWNLOAD_STATUS_FINISH);
						break;
					}	
					default:
						break;
					}
				}
				default:
					break;
				}
			}
		};
	}
}