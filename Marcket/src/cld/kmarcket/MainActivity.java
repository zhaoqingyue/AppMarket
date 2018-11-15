package cld.kmarcket;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import cld.kmarcket.appinfo.AppStatus;
import cld.kmarcket.appinfo.InstalledApp;
import cld.kmarcket.appinfo.NetApp;
import cld.kmarcket.appinfo.UpdateDownloadTime;
import cld.kmarcket.appinfo.UpgradeApp;
import cld.kmarcket.customview.TabWidget;
import cld.kmarcket.fragment.IUpdateIndicate;
import cld.kmarcket.fragment.MyAppFragment;
import cld.kmarcket.fragment.RecdFragment;
import cld.kmarcket.service.RemoteService;

public class MainActivity extends BaseActivity implements OnClickListener,
		IUpdateIndicate 
{
	public static final int MYAPP_FRAGMENT_INDEX = 0;
	public static final int RECD_FRAGMENT_INDEX = 1;
	private TabWidget mTabWidget[] = new TabWidget[2];
	private MyAppFragment mMyappFragment;
	private RecdFragment mRecdFragment;
	private int mIndex = MYAPP_FRAGMENT_INDEX;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kmain);
		initView();
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
		onClick(mTabWidget[mIndex]);
	}

	@SuppressLint("ResourceAsColor")
	private void initView() 
	{
		mTabWidget[0] = (TabWidget) findViewById(R.id.id_tab_myapp);
		mTabWidget[1] = (TabWidget) findViewById(R.id.id_tab_recd);

		mTabWidget[0].setOnClickListener(this);
		mTabWidget[1].setOnClickListener(this);

		mTabWidget[0].setIconSelector(R.drawable.tab_myapp_nor,
				R.drawable.tab_myapp_sel);
		mTabWidget[1].setIconSelector(R.drawable.tab_recd_nor,
				R.drawable.tab_recd_sel);
	}

	@Override
	public void onClick(View v) 
	{
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		hideFragments(transaction);
		switch (v.getId()) 
		{
		case R.id.id_tab_myapp: 
		{
			mIndex = MYAPP_FRAGMENT_INDEX;
			mTabWidget[0].setTabSelected(true);
			mTabWidget[1].setTabSelected(false);

			if (null == mMyappFragment) 
			{
				mMyappFragment = new MyAppFragment();
				if (!mMyappFragment.getHasSetUpdateIndicate()) 
				{
					mMyappFragment.setUpdateIndicate(this);
				}
				transaction.add(R.id.id_fragment_layout, mMyappFragment);
			} 
			else 
			{
				transaction.show(mMyappFragment);
			}
			break;
		}
		case R.id.id_tab_recd: 
		{
			mIndex = RECD_FRAGMENT_INDEX;
			mTabWidget[0].setTabSelected(false);
			mTabWidget[1].setTabSelected(true);

			if (null == mRecdFragment) 
			{
				mRecdFragment = new RecdFragment();
				if (!mRecdFragment.getHasSetUpdateIndicate()) 
				{
					mRecdFragment.setUpdateIndicate(this);
				}
				transaction.add(R.id.id_fragment_layout, mRecdFragment);
			}
			else 
			{
				transaction.show(mRecdFragment);
			}
			break;
		}
		default:
			break;
		}
		transaction.commitAllowingStateLoss();
	}

	private void hideFragments(FragmentTransaction transaction) 
	{
		if (null != mMyappFragment) 
		{
			transaction.hide(mMyappFragment);
		}
		if (null != mRecdFragment) 
		{
			transaction.hide(mRecdFragment);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) 
	{
		outState.putInt("index", mIndex);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) 
	{
		mIndex = savedInstanceState.getInt("index");
	}

	@Override
	public void onUpdateIndicate(int index, boolean display) 
	{
		switch (index) 
		{
		case MYAPP_FRAGMENT_INDEX: 
		{
			mTabWidget[0].setIndicateDisplay(display);
			break;
		}
		case RECD_FRAGMENT_INDEX: 
		{
			mTabWidget[1].setIndicateDisplay(display);
			break;
		}
		default:
			break;
		}
	}
	
	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
		InstalledApp.static_release();
		UpgradeApp.static_release(); 
		NetApp.static_release();
		AppStatus.static_release();
		UpdateDownloadTime.static_release();
		RemoteService.getInstance().unbindService();
		RemoteService.getInstance().static_release();
	}
}
