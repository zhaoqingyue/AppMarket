package cld.kmarcket.fragment;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;
import cld.kmarcket.KMarcketApplication;
import cld.kmarcket.MainActivity;
import cld.kmarcket.R;
import cld.kmarcket.adapter.AppFragmentPagerAdapter;
import cld.kmarcket.appinfo.AppInfo;
import cld.kmarcket.appinfo.AppStatus;
import cld.kmarcket.appinfo.InstalledApp;
import cld.kmarcket.appinfo.UpgradeApp;
import cld.kmarcket.packages.PackageTable;
import cld.kmarcket.util.CommonUtil;
import cld.kmarcket.util.ConstantUtil;
import cld.kmarcket.util.FileUtil;
import cld.kmarcket.util.LocationUtils;
import cld.kmarcket.util.LocationUtils.IKCloudLocationListener;
import cld.kmarcket.util.LogUtil;
import cld.kmarcket.util.NetUtil;
import cld.kmarcket.util.RegionUtils;
import cld.kmarcket.util.ShareUtil;
import com.download.api.DownloadManager;
import com.download.api.DownloadWatchManager;
import com.download.api.TaskStatus;

public class MyAppFragment extends BaseFragment
{
	private ViewPager mPager;
	private ProgressBar mProgressBar;
	private LinearLayout mPoints;
	private TextView mNoRecd;
	private AppFragmentPagerAdapter mPagerAdapter;
	private ArrayList<AppInfo> mAppInfoList  = new ArrayList<AppInfo>();
	private ArrayList<AppInfo> installedList;
	private ArrayList<AppInfo> mUpdateList  = new ArrayList<AppInfo>();
	private ArrayList<String> mAppStatus = new ArrayList<String>();
	private int mCurPosition = 0;
	
	private long mDuid = 0;  
	private long mKuid = 0;
	private int mRegionId = 0;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.fragment_myapp,
				container, false);
		findViews(view);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
		
		setAdapters();
		initCurAddr();
		//loadUpgradeAppsList();
		registerReceivers();
	}

	private void findViews(View view) 
	{
		mProgressBar = (ProgressBar)view.findViewById(R.id.load_progress);
		mPager = (ViewPager) view.findViewById(R.id.id_viewpager);
		mPoints = (LinearLayout) view.findViewById(R.id.id_points);
		mNoRecd = (TextView) view.findViewById(R.id.id_app_no_recd);
	}
	
	private void getDuidKuid()
	{
		Context c = null;
		try 
		{
			c = getActivity().createPackageContext(ConstantUtil.PREFERENCE_PACKAGE,
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
	
	private void loadUpgradeAppsList()
	{
		if (mProgressBar != null)
		{
			mProgressBar.setVisibility(View.VISIBLE);
		}
		
		int type = NetUtil.getNetType(getActivity());
		if (type == -1)
		{
			mNoRecd.setText(getResources().getString(R.string.net_error));
			mNoRecd.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.GONE);
			return;
		}
		
		Thread loadThread = new Thread()
		{
			@Override
			public void run() 
			{
				super.run();
				mAppInfoList.clear();
				mAppStatus.clear();
				//CommonUtil.configureTest();
				getDuidKuid();
				//应用升级接口
				installedList = PackageTable.getInstance().queryPackages();
				if (ShareUtil.getBoolean("first", true))
				{
					ArrayList<AppInfo> installed = InstalledApp.getInstance().
							getInstalledApps();
					ShareUtil.put("first", false);
					ArrayList<AppInfo> temp = new ArrayList<AppInfo>();
					temp.clear();
					temp.addAll(installedList);
					
					for (int i=0; i<temp.size(); i++)
					{
						if (!CommonUtil.isInstalledApp(temp.get(i).getPkgName(), installed))
						{
							PackageTable.getInstance().deletePackage(temp.get(i).getPkgName());
							installedList.remove(temp.get(i));
						}
					}
				}
				UpgradeApp.getInstance().loadUpdateApps(mHandler, installedList, 0, 0, mDuid, mKuid, mRegionId);
			}
		};
		loadThread.start();
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
								mHandler.sendEmptyMessage(0);
							}
						});
			}
		});
	}
	
	@SuppressLint("HandlerLeak") 
	Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg) 
		{
			super.handleMessage(msg);
			
			switch (msg.what) 
			{
			case 0:
			{
				loadUpgradeAppsList();
				break;
			}
			case ConstantUtil.MSG_GET_APP_UPGRADE_SUC:
			{
				//应用状态接口
				AppStatus.getInstance().loadAppStatus(mHandler, installedList);
				break;
			}
			case ConstantUtil.MSG_GET_APP_STATUS_SUC:
			{
				if(mProgressBar != null)
				{
					mProgressBar.setVisibility(View.GONE);
				}
				
				//常规升级列表
				ArrayList<AppInfo> normalUpgrade = UpgradeApp.
						getInstance().getNormalUpgradeAppList(installedList);
				
				//更新下载应用状态
				mAppInfoList.addAll(AppStatus.getInstance().
						getAppList(normalUpgrade));
				
				if (mAppInfoList != null && !mAppInfoList.isEmpty())
				{
					mPagerAdapter.notifyDataSetChanged();
					initPoints();
					setListener();
				}
				else
				{
					mNoRecd.setText(CommonUtil.getString(R.string.no_my_apps));
					mNoRecd.setVisibility(View.VISIBLE);
				}
				
				/*if (UpgradeApp.getInstance().getNormalUpgradeList().size() > 0)
				{
					mUpdateList.clear();
					mUpdateList.addAll(UpgradeApp.getInstance().getNormalUpgradeList());
					mIUpdateIndicate.onUpdateIndicate(
							MainActivity.MYAPP_FRAGMENT_INDEX, true);
				}*/
				
				//检测静默升级列表
				checkQuiesceUpgradeApp();
				break;
			}
			case ConstantUtil.MSG_REPLACED_SUC:
			{
				String pkgName = (String)msg.obj;
				//静默升级成功后不刷新
				boolean quiesce = UpgradeApp.getInstance().isQuiesceUpgrade(pkgName);
				if (quiesce)
					return;
				
				updatePagerAdapter(pkgName);
				break;
			}
			case ConstantUtil.MSG_REMOVED_SUC:
			{
				String pkgName = (String)msg.obj;
				int index = CommonUtil.getAppIndexByPkgName(pkgName, mAppInfoList);
				if (index >= 0)
				{
					mAppInfoList.remove(index);
					resetCheck();
					mPagerAdapter.notifyDataSetChanged();
					initPoints();
					setListener();
				}
				break;
			}
			case ConstantUtil.MSG_ADDED_FAILED:
			{
				String pkgName = (String)msg.obj;
				updatePagerAdapter(pkgName);
				break;
			}
			case ConstantUtil.MSG_REMOVED_FAILED:
			{
				String pkgName = (String)msg.obj;
				updatePagerAdapter(pkgName);
				break;
			}
			default:
				break;
			}
		}
	};
	
	private void updatePagerAdapter(String pkgName)
	{
		int index = CommonUtil.getAppIndexByPkgName(pkgName, mAppInfoList);
		LogUtil.i(LogUtil.TAG, "index: " + index);
		if (index >= 0)
		{
			resetCheck();
			mPagerAdapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * 取消屏保后， 刷新下载进度
	 */
	@Override
	public void onResume() 
	{
		//LogUtil.i(LogUtil.TAG, "+++ onResume MyAppFragment +++");
		super.onResume();
		resetCheck();
		mPagerAdapter.notifyDataSetChanged();
	}
	
	/**
	 * 避免gridview多次调用getview方法导致多次调用onUndone(path);
	 */
	private void resetCheck()
	{
		for (AppInfo appInfo : mAppInfoList)
		{
			appInfo.setChecked(ConstantUtil.APP_CHECKED_NO);
		}
	}
	
	private void updateIndicate(String pkgName) 
	{
		int index = CommonUtil.getAppIndexByPkgName(pkgName, mUpdateList);
		if (index >= 0)
		{
			mUpdateList.remove(index);
			if (mUpdateList.size() <= 0)
			{
				mIUpdateIndicate.onUpdateIndicate(
						MainActivity.MYAPP_FRAGMENT_INDEX, false);
			}
		}
	}
	
	private void checkQuiesceUpgradeApp() 
	{
		ArrayList<AppInfo> quiesceUpgradeApp = UpgradeApp.getInstance().
				getQuiesceUpgradeAppList();
		LogUtil.i(LogUtil.TAG, "size: " + quiesceUpgradeApp.size());
		if (quiesceUpgradeApp != null && quiesceUpgradeApp.size() > 0)
		{
			for (int i=0; i<quiesceUpgradeApp.size(); i++)
			{
				AppInfo appinfo = quiesceUpgradeApp.get(i);
				new QuiesceDownload(appinfo);
			}
		}
	}

	private void setAdapters()
	{
		mPagerAdapter = new AppFragmentPagerAdapter(/*getActivity().
				getSupportFragmentManager()*/getChildFragmentManager(),
				mAppInfoList,
				AppFragmentPagerAdapter.MYAPP_FRAGMENT);
		mPager.setAdapter(mPagerAdapter);
	}

	@SuppressWarnings("deprecation")
	private void initPoints() 
	{
		if (mPoints == null || getActivity() == null)
			return;
		
		mPoints.removeAllViews();
		if (mPagerAdapter.getCount() <= 1)
		{
			return;
		}
    	View view;
    	for (int i=0; i<mPagerAdapter.getCount(); i++) 
    	{
			//添加点view对象
			view = new View(getActivity());
			view.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.viewpager_point_bg));
			//设置Point的大小
			LayoutParams lp = new LayoutParams(20, 20);
			//设置Point间距
			lp.leftMargin = 15;
			view.setLayoutParams(lp);
			view.setEnabled(false);
			view.setVisibility(View.VISIBLE);
			mPoints.addView(view);
		}
    	mPoints.getChildAt(mCurPosition).setEnabled(true);
    }
	
	private void setListener()
	{
		final int pageCount = mPagerAdapter.getCount();
		mPager.setOnPageChangeListener(new OnPageChangeListener() 
		{
			@Override
			public void onPageSelected(int position) 
			{
				mPoints.getChildAt(mCurPosition).setEnabled(false);	    
				mPoints.getChildAt(position % pageCount).setEnabled(true); 
				mPager.setCurrentItem(position % pageCount);
				mCurPosition = position  % pageCount;
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2)
			{
			}
			
			@Override
			public void onPageScrollStateChanged(int position) 
			{
			}
		});
	}
	
	private Receiver mReceiver = new Receiver();
	private class Receiver extends BroadcastReceiver 
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			if (intent.getAction().equals(ConstantUtil.ACTION_REPLACED_SUC))
			{
				String pkgName = intent.getStringExtra("package_name"); 
				Message msg = new Message();
				msg.what= ConstantUtil.MSG_REPLACED_SUC;
				msg.obj = (Object)pkgName;
				mHandler.sendMessage(msg);
			}
			else if (intent.getAction().equals(ConstantUtil.ACTION_REMOVED_SUC))
			{
				String pkgName = intent.getStringExtra("package_name"); 
				Message msg = new Message();	
				msg.what= ConstantUtil.MSG_REMOVED_SUC;
				msg.obj = (Object)pkgName;	
				mHandler.sendMessage(msg);
			}
			else if (intent.getAction().equals(ConstantUtil.ACTION_ADDED_FAILED))
			{
				String pkgName = intent.getStringExtra("package_name"); 
				Message msg = new Message();	
				msg.what= ConstantUtil.MSG_ADDED_FAILED;
				msg.obj = (Object)pkgName;	
				mHandler.sendMessage(msg);
			}
			else if (intent.getAction().equals(ConstantUtil.ACTION_REMOVED_FAILED))
			{
				String pkgName = intent.getStringExtra("package_name"); 
				Message msg = new Message();	
				msg.what= ConstantUtil.MSG_REMOVED_FAILED;
				msg.obj = (Object)pkgName;	
				mHandler.sendMessage(msg);
			}
		}
	}
	
	private void registerReceivers() 
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConstantUtil.ACTION_REPLACED_SUC);
		filter.addAction(ConstantUtil.ACTION_REMOVED_SUC); 
		filter.addAction(ConstantUtil.ACTION_ADDED_FAILED);
		filter.addAction(ConstantUtil.ACTION_REMOVED_FAILED); 
		getActivity().registerReceiver(mReceiver, filter);
	}
	
	private void unregisterReceivers() 
	{
		if (mReceiver != null) 
		{
			getActivity().unregisterReceiver(mReceiver);
			mReceiver = null;
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
			
			if (FileUtil.isEnough(getActivity(), appinfo.getPackSize()))
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
						LogUtil.i(LogUtil.TAG, " DOWNLOAD_STATUS_END ");
						//下载完成
						if (!appinfo.getPkgName().equals("cld.kmarcket") && 
							!appinfo.getPkgName().equals("com.cld.launcher"))
						{
							CommonUtil.startSlienceInstall(urlPath);
						}
						else
						{
							CommonUtil.updateAppType(appinfo.getPkgName(), 
									ConstantUtil.DOWNLOAD_STATUS_FINISH);
						}
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
	
	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		unregisterReceivers();
	}
}
