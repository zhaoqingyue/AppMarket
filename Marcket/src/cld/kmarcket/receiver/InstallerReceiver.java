package cld.kmarcket.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import cld.kmarcket.R;
import cld.kmarcket.appinfo.UpgradeApp;
import cld.kmarcket.util.CommonUtil;
import cld.kmarcket.util.ConstantUtil;
import cld.kmarcket.util.LogUtil;

public class InstallerReceiver extends BroadcastReceiver 
{
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		if(intent.getAction().equals(ConstantUtil.ACTION_INSTALL_COMPLETE))
		{
			String appname = intent.getStringExtra("app_name");
			String pkgname = intent.getStringExtra("package_name");
			int retcode = intent.getIntExtra("ret_code", 0);
			LogUtil.i(LogUtil.TAG, "INSTALL_COMPLETE appname: " + appname 
					+ ", pkgname: " + pkgname + ", retcode: " + retcode);
			
			//静默升级成功后不提示
			boolean quiesce = UpgradeApp.getInstance().isQuiesceUpgrade(pkgname);
			if (quiesce)
			{
				//test
				CommonUtil.makeText("静默安装完成");
				return;
			}
			
			String tip = "";
			if (retcode == 1)
			{
				tip = CommonUtil.getString(R.string.toast_install_success);
				CommonUtil.makeText(appname + tip);
				//CommonUtil.showToast(appname + "安装成功");
			}
			else
			{
				tip = CommonUtil.getString(R.string.toast_install_failed);
				CommonUtil.makeText(appname + tip);
				//CommonUtil.showToast(appname + "安装失败");
				CommonUtil.updateAppType(pkgname, 
						ConstantUtil.INSTALL_STATUS_FAILED);
				CommonUtil.sendPackageAddedFailed(pkgname);
			}
		}
		else if(intent.getAction().equals(ConstantUtil.ACTION_DELETE_COMPLETE))
		{
			String appname = intent.getStringExtra("app_name");
			String pkgname = intent.getStringExtra("package_name");
			int retcode = intent.getIntExtra("ret_code", 0);
			LogUtil.i(LogUtil.TAG, "DELETE_COMPLETE appname: " + appname 
					+ ", pkgname: " + pkgname + ", retcode: " + retcode);
			
			String tip = "";
			if (retcode == 1)
			{
				tip = CommonUtil.getString(R.string.toast_uninstall_success);
				CommonUtil.makeText(appname + tip);
				//CommonUtil.showToast(appname + "卸载成功");
			}
			else
			{
				tip = CommonUtil.getString(R.string.toast_uninstall_failed);
				CommonUtil.makeText(appname + tip);
				//CommonUtil.showToast(appname + "卸载失败");
				CommonUtil.updateAppType(pkgname, 
						ConstantUtil.UNINSTALL_STATUS_FAILED);
				CommonUtil.sendPackageRemovedFailed(pkgname);
			}
		}
	}
}
