package cld.kmarket.install;

import com.cld.log.CldLog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class InstallerReceiver extends BroadcastReceiver 
{
	private static final String TAG = "InstallerReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		String action = intent.getAction();
		if(action.equals(InstallerInter.ACTION_INSTALL_START))
		{
		}
		else if(action.equals(InstallerInter.ACTION_INSTALL_PROGRESS))
		{
		}
		else if(action.equals(InstallerInter.ACTION_INSTALL_COMPLETE))
		{
			String appname = intent.getStringExtra("app_name");
			String pkgname = intent.getStringExtra("package_name");
			int retcode = intent.getIntExtra("ret_code", 0);
			CldLog.i(TAG, "INSTALL_COMPLETE appname: " + appname 
					+ ", pkgname: " + pkgname + ", retcode: " + retcode);
		}
		else if(action.equals(InstallerInter.ACTION_DELETE_START))
		{
		}
		else if(action.equals(InstallerInter.ACTION_DELETE_PROGRESS))
		{
		}
		else if(action.equals(InstallerInter.ACTION_DELETE_COMPLETE))
		{
			String appname = intent.getStringExtra("app_name");
			String pkgname = intent.getStringExtra("package_name");
			int retcode = intent.getIntExtra("ret_code", 0);
			CldLog.i(TAG, "DELETE_COMPLETE appname: " + appname 
					+ ", pkgname: " + pkgname + ", retcode: " + retcode);
		}
	}
}
