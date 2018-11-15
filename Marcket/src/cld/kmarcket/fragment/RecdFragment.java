package cld.kmarcket.fragment;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;
import cld.kcloud.service.aidl.IKCloudService;
import cld.kmarcket.MainActivity;
import cld.kmarcket.R;
import cld.kmarcket.adapter.AppFragmentPagerAdapter;
import cld.kmarcket.appinfo.AppInfo;
import cld.kmarcket.appinfo.NetApp;
import cld.kmarcket.packages.PackageTable;
import cld.kmarcket.util.CommonUtil;
import cld.kmarcket.util.ConstantUtil;
import cld.kmarcket.util.LogUtil;
import cld.kmarcket.util.NetUtil;

public class RecdFragment extends BaseFragment
{
	private ViewPager mPager;
	private ProgressBar mProgressBar;
	private LinearLayout mPoints;
	private TextView mNoRecd;
	private AppFragmentPagerAdapter mPagerAdapter;
	private ArrayList<AppInfo> mAppInfoList  = new ArrayList<AppInfo>();
	private ArrayList<AppInfo> mTempList  = new ArrayList<AppInfo>();
	private int mCurPosition = 0;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.fragment_app_recd, 
				container, false);
		findViews(view);
		registerReceivers();
		//bindService();
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
		setAdapters();
		loadRecdAppsList();
	}

	private void findViews(View view) 
	{
		mProgressBar = (ProgressBar)view.findViewById(R.id.load_progress);
		mPager = (ViewPager) view.findViewById(R.id.id_viewpager);
		mPoints = (LinearLayout) view.findViewById(R.id.id_points);
		mNoRecd = (TextView) view.findViewById(R.id.id_app_no_recd);
	}
	
	private void loadRecdAppsList()
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
				
				/*ArrayList<AppInfo> installedList = InstalledApp.getInstance().
						getInstalledApp(KMarcketApplication.getContext());*/
				
				ArrayList<AppInfo> installedList = PackageTable.
						getInstance().queryPackages();
				NetApp.getInstance().loadNetApps(mHandler, installedList, 0, 0);
			}
		};
		loadThread.start();
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
			case ConstantUtil.MSG_GET_APP_RECD_SUC:
			{
				if(mProgressBar != null)
				{
					mProgressBar.setVisibility(View.GONE);
				}
				
				ArrayList<AppInfo> netApps = NetApp.getInstance().getNetApps();
				if (netApps != null && !netApps.isEmpty())
				{
					mAppInfoList.addAll(netApps);
					mTempList.clear();
					mTempList.addAll(mAppInfoList);
					
					mPagerAdapter.notifyDataSetChanged();
					initPoints();
					setListener();
				}
				else
				{
					mNoRecd.setText(CommonUtil.getString(R.string.no_recd_apps));
					mNoRecd.setVisibility(View.VISIBLE);
				}
				break;
			}
			case ConstantUtil.MSG_ADDED_SUC:
			{
				String pkgName = (String)msg.obj;
				updatePagerAdapter(pkgName);
				break;
			}
			case ConstantUtil.MSG_REPLACED_SUC:
			{
				String pkgName = (String)msg.obj;
				updatePagerAdapter(pkgName);
				break;
			}
			case ConstantUtil.MSG_ADDED_FAILED:
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
		LogUtil.i(LogUtil.TAG, "index:" + index);
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
		//LogUtil.i(LogUtil.TAG, "+++ onResume RecdFragment +++");
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
		int index = CommonUtil.getAppIndexByPkgName(pkgName, mTempList);
		if (index >= 0)
		{
			mTempList.remove(index);
			if (mTempList.size() <= 0)
			{
				mIUpdateIndicate.onUpdateIndicate(
						MainActivity.RECD_FRAGMENT_INDEX, false);
			}
		}
	}
	
	private void setAdapters()
	{
		mPagerAdapter = new AppFragmentPagerAdapter(
				getChildFragmentManager(), mAppInfoList,
				AppFragmentPagerAdapter.MARKET_FRAGMENT);
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
			if(intent.getAction().equals(ConstantUtil.ACTION_ADDED_SUC))
			{
				String pkgName = intent.getStringExtra("package_name");  
				Message msg = new Message();
				msg.what= ConstantUtil.MSG_ADDED_SUC;
				msg.obj = (Object)pkgName;
				mHandler.sendMessage(msg);
			}
			if (intent.getAction().equals(ConstantUtil.ACTION_REPLACED_SUC))
			{
				String pkgName = intent.getStringExtra("package_name"); 
				Message msg = new Message();
				msg.what= ConstantUtil.MSG_REPLACED_SUC;
				msg.obj = (Object)pkgName;
				mHandler.sendMessage(msg);
			}
			if(intent.getAction().equals(ConstantUtil.ACTION_ADDED_FAILED))
			{
				String pkgName = intent.getStringExtra("package_name");  
				Message msg = new Message();
				msg.what= ConstantUtil.MSG_ADDED_FAILED;
				msg.obj = (Object)pkgName;
				mHandler.sendMessage(msg);
			}
		}
	}
	
	private void registerReceivers() 
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConstantUtil.ACTION_ADDED_SUC);  
		filter.addAction(ConstantUtil.ACTION_REPLACED_SUC);
		filter.addAction(ConstantUtil.ACTION_ADDED_FAILED);
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
	
	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		unregisterReceivers();
		//getActivity().unbindService(mConn);   
	}
	
	private void getKldLoginResult()
	{
		try 
		{
			if (mIKCloudService != null)
			{
				String result = mIKCloudService.get_KLD_login_result();
				LogUtil.i(LogUtil.TAG, "result: " + result);
			}
		} 
	    catch (RemoteException e) 
	    {
			e.printStackTrace();
		}
	}
	
	IKCloudService mIKCloudService;
	
	private ServiceConnection mConn = new ServiceConnection() 
	{   
        @Override   
        public void onServiceDisconnected(ComponentName name) 
        {   
        	mIKCloudService = null;   
        }   
   
        @Override   
        public void onServiceConnected(ComponentName name, IBinder service) 
        {   
            //获取远程Service的onBinder方法返回的对象代理   
        	mIKCloudService = IKCloudService.Stub.asInterface(service);   
        }   
    }; 
    
    private void bindService()
    {
    	//创建所需要绑定的Service的Intent   
        Intent intent = new Intent();   
        intent.setAction("cld.kcloud.center.aidl.service");   
        //绑定远程的服务   
        getActivity().bindService(intent, mConn, Service.BIND_AUTO_CREATE);
    }
}
