package cld.kcloud.custom.view;

import java.util.ArrayList;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import cld.kcloud.fragment.manager.BaseFragment;

public class ServiceFragmentPagerAdapter extends FragmentStatePagerAdapter {
	
	private ArrayList<? extends BaseFragment> list;

	public ServiceFragmentPagerAdapter(FragmentManager fm, 
			ArrayList<? extends BaseFragment> list) {
		super(fm);
		this.list = list;
	}

	@Override
	public Fragment getItem(int index) {
		return list.get(index);
	}

	@Override
	public int getCount() {
		return list.size();
	}
	
}
