package cld.kmarcket;

import cld.kmarcket.util.ConstantUtil;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class BaseActivity extends FragmentActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		IntentFilter intent = new IntentFilter();
	    intent.addAction(ConstantUtil.ACTION_EXIT_APP);      
	    registerReceiver(mBroadcastReceiver, intent);
	}
	
	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
		unregisterReceiver(mBroadcastReceiver);
	}
	
	protected BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() 
	{
		@Override
	    public void onReceive(Context context, Intent intent) 
	    {
			BaseActivity.this.onReceive(intent);
		}
	};
	
	protected void onReceive(Intent intent) 
	{
		this.finish();
	}
}
