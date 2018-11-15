package cld.kmarcket.fragment;

import android.support.v4.app.Fragment;

public class BaseFragment extends Fragment
{
	public IUpdateIndicate mIUpdateIndicate;
	private boolean mHasSetUpdateIndicate = false;
	
	public void setUpdateIndicate(IUpdateIndicate iupdateIndicate)
	{
		mIUpdateIndicate = iupdateIndicate;
		mHasSetUpdateIndicate = true;
	}
	
	public boolean getHasSetUpdateIndicate()
	{
		return mHasSetUpdateIndicate;
	}
}
