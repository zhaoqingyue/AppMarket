package cld.kcloud.fragment.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import android.util.Log;

public class ManagerFragment implements ManagerFragmentInterface {

	private static final String TAG = "FragmentOrderManager";
	
	private class FragmentItem {
		String Tag;
		BaseFragment fragment;
		
		public FragmentItem(BaseFragment fragment, String Tag) {
			this.Tag = Tag;
			this.fragment = fragment;
		}
	}
	
	private Stack<FragmentItem> mFragmentList;
	
	public ManagerFragment() {
		mFragmentList = new Stack<FragmentItem>();
	}
	
	public void release(){
		mFragmentList.clear();
	}
	
	public int getSize() {
		if (mFragmentList != null) {
			return mFragmentList.size(); 
		}
		return 0;
	}

	@Override
	public void pushFragment(BaseFragment fragment, String Tag) {
		if (mFragmentList != null) {
			FragmentItem item = new FragmentItem(fragment, Tag);
			mFragmentList.push(item);
			
			Log.d(TAG, "push fragment" + String.valueOf(mFragmentList.size()));
		}
	}

	@Override
	public boolean popFragment() {
		if (mFragmentList != null && !mFragmentList.isEmpty()) {
			mFragmentList.pop();
			
			BaseFragment fragment = getLastFragment();
			if (fragment != null) {
				fragment.onUpdate();
			}
			return true;
		}
		return false;
	}

	@Override
	public BaseFragment getFirstFragment() {
		if (mFragmentList != null) {
			if (mFragmentList.isEmpty()) {
				return null;
			}
			FragmentItem item = mFragmentList.firstElement();
			if (item != null) {
				return item.fragment;
			}
		}
		return null;
	}
	
	@Override
	public BaseFragment getLastFragment() {
		if (mFragmentList != null) {
			if (mFragmentList.isEmpty()) {
				return null;
			}
			FragmentItem item = mFragmentList.lastElement();
			if (item != null) {
				return item.fragment;
			}
		}
		return null;
	}

	@Override
	public BaseFragment findFragmentByTag(String Tag) {
		if (mFragmentList != null) {
			if (mFragmentList.isEmpty()) {
				return null;
			}
			
			for (FragmentItem item : mFragmentList) {
				if (item.Tag.equals(Tag)) {
					return item.fragment;
				}
			}
		}
		return null;
	}

	@Override
	public List<BaseFragment> getFragments() {
		if (mFragmentList != null) {
			if (mFragmentList.isEmpty()) {
				return null;
			}
			
			List<BaseFragment> list = new ArrayList<BaseFragment>();
			
			Iterator<?> iter = mFragmentList.iterator();
			while(iter.hasNext()){
				FragmentItem item = (FragmentItem) iter.next();
				list.add(item.fragment);
			}
			return list;
		}		
		return null;
	}

	@Override
	public boolean backPressed() {
		if (mFragmentList != null) {
			if (mFragmentList.isEmpty()) {
				return false;
			}
			FragmentItem item = mFragmentList.peek();
			if (item != null) {
				return item.fragment.onBackPressed();
			}
		}
		return false;
	}
}
