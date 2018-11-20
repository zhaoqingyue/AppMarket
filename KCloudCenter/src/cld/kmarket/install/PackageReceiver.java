package cld.kmarket.install;

import java.io.File;
import com.cld.log.CldLog;
import cld.kcloud.custom.bean.KCloudInstalledInfo;
import cld.kcloud.database.KCloudInstalledTable;
import cld.kcloud.database.KCloudInstallingTable;
import cld.kcloud.utils.KCloudCommonUtil;
import cld.kmarket.download.DownloadDir;
import cld.kmarket.download.DownloadUtils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class PackageReceiver extends BroadcastReceiver 
{
	private static final String TAG = "PackageReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_PACKAGE_ADDED))
		{
			Uri data = intent.getData();
			String pkgName = data.getEncodedSchemeSpecificPart();
			boolean replace = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
			CldLog.i(TAG, "ACTION_PACKAGE_ADDED replace: "  + replace);
			if (!replace)
			{
				//��װ�ɹ�
				CldLog.i(TAG, "ACTION_PACKAGE_ADDED pkgName: "  + pkgName);
				KCloudInstalledInfo appInfo = new KCloudInstalledInfo();
				appInfo.setPkgName(pkgName);
				appInfo.setVerCode(KCloudCommonUtil.getVercodeByPkgname(pkgName));
				KCloudInstalledTable.getInstance().insertInstalledInfo(appInfo);
			}
		}
		else if (action.equals(Intent.ACTION_PACKAGE_REPLACED))
		{
			Uri data = intent.getData();
			String pkgName = data.getEncodedSchemeSpecificPart();
			//�滻�ɹ�
			CldLog.i(TAG, "ACTION_PACKAGE_REPLACED pkgName: "  + pkgName);
			
			KCloudInstalledInfo appInfo = KCloudInstallingTable.getInstance().
					queryInstallingInfo(pkgName);
			if (appInfo != null)
			{
				//ɾ�����ڰ�װ����Ϣ
				KCloudInstallingTable.getInstance().deleteInstallingInfo(pkgName);
				//�����Ѱ�װ����Ϣ
				KCloudInstalledTable.getInstance().updateInstalledInfo(appInfo);
				
				//��װ���֮�� ɾ����װ��
				String urlPath = appInfo.getAppUrl();
				String path = DownloadDir.getDownloadDir();
				String name = urlPath.substring(urlPath.lastIndexOf("/") + 1);
				CldLog.i(TAG, "delete path: " + path + ", name: " + name);
				File file = new File(path, name);
				DownloadUtils.delete(file);
			}
		}
		else if (action.equals(Intent.ACTION_PACKAGE_REMOVED))
		{
			Uri data = intent.getData();
			String pkgName = data.getEncodedSchemeSpecificPart();
			boolean replace = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
			CldLog.i(TAG, "ACTION_PACKAGE_REMOVED replace: " + replace);
			if (!replace)
			{
				//ɾ���ɹ�
				CldLog.i(TAG, "ACTION_PACKAGE_REMOVED pkgName: " + pkgName);
				KCloudInstalledTable.getInstance().deleteInstalledInfo(pkgName);
			}
		}
	}
}
