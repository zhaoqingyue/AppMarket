package cld.kmarcket.adapter;

import java.util.ArrayList;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import cld.kmarcket.appinfo.AppInfo;
import cld.kmarcket.fragment.AppFragment;

public class AppFragmentPagerAdapter extends FragmentStatePagerAdapter 
{
	public static final String MYAPP_FRAGMENT = "myapp_fragment";
	public static final String MARKET_FRAGMENT = "market_fragment";
	public static final int PAGE_SIZE = 4; //每页的最大数量
	private ArrayList<AppInfo> mAppInfoList;
	private String mKey = "";
	
	public AppFragmentPagerAdapter(FragmentManager fm) 
	{
		super(fm);
	}
	
	public AppFragmentPagerAdapter(FragmentManager fm, 
		ArrayList<AppInfo> appInfoList, String key) 
	{
		super(fm);
		mAppInfoList = appInfoList;
		mKey = key;
	}
	
	@Override
	public int getItemPosition(Object object) 
	{
		return PagerAdapter.POSITION_NONE;
	}

	@Override
	public Fragment getItem(int position) 
	{
		int start = 0, end = 0;
		start = position * PAGE_SIZE;
		end = Math.min(start + PAGE_SIZE, mAppInfoList.size());
		ArrayList<AppInfo> curList = new ArrayList<AppInfo>();
		curList.clear();
		for (int i=start; i<end; i++)
		{
			curList.add(mAppInfoList.get(i));
		}
		return AppFragment.newInstance(curList, mKey);
	}

	@Override
	public int getCount() 
	{
		if (mAppInfoList == null || mAppInfoList.size() <= 0)
		{
			return 0;
		}
		else
		{
			return (mAppInfoList.size()-1) / PAGE_SIZE + 1;
		}
	}

    @Override
    public boolean isViewFromObject(View view, Object object) 
    {
    	return super.isViewFromObject(view, object);
    }
    
    public void destroyItem(ViewGroup container, int position, Object object) 
	{
    	super.destroyItem(container, position, object);
    }

    @Override
    public Parcelable saveState() 
    {
    	return super.saveState();
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) 
    {
    	super.restoreState(state, loader);
    }
}
