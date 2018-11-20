package cld.kcloud.fragment.manager;

import java.util.List;

public interface ManagerFragmentInterface {
	public void pushFragment(BaseFragment fragment, String Tag);
	public boolean popFragment();
	public BaseFragment getFirstFragment();
	public BaseFragment getLastFragment();
	public BaseFragment findFragmentByTag(String Tag);
	public List<BaseFragment> getFragments();
	public boolean backPressed();
}
