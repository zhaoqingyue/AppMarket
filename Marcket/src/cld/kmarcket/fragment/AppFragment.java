package cld.kmarcket.fragment;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import cld.kmarcket.R;
import cld.kmarcket.adapter.AppAdapter;
import cld.kmarcket.appinfo.AppInfo;

public class AppFragment extends Fragment implements OnItemClickListener
{
	private GridView   mGridView;
	private AppAdapter mAdapter;
	private ArrayList<AppInfo> mCurList;
	private static String mKey = "";
	
	@SuppressLint("NewApi") 
	public static AppFragment newInstance(ArrayList<AppInfo> list, String key) 
	{
		mKey = key;
		AppFragment f = new AppFragment();
		Bundle args = new Bundle();
		args.putParcelableArrayList(mKey, list);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.fragment_app, container, false);
		findViews(view);
		return view;
	}

	@SuppressLint("NewApi") 
	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
		
		mCurList = new ArrayList<AppInfo>();
		mCurList.clear();
		mCurList = getArguments().getParcelableArrayList(mKey);
		setAdapter();
	}

	private void findViews(View view) 
	{
		mGridView = (GridView) view.findViewById(R.id.app_fragment_list);
		mGridView.setOnItemClickListener(this);
	}
	
	private void setAdapter()
	{
		mAdapter = new AppAdapter(getActivity(), mCurList);
		mGridView.setAdapter(mAdapter);
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
	{
		Log.d("log", "onItemClick");
	}
	
	@Override
	public void onDestroy() 
	{
		super.onDestroy();
	}
}
