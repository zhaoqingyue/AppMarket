package cld.kmarket.install;

import java.io.File;
import com.cld.log.CldLog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.IBinder;

public class InstallerService extends Service
{
	private static final String TAG = "InstallerService";
	
	@Override
	public void onCreate() 
	{
		super.onCreate();
	}

	@SuppressWarnings("deprecation")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		if(intent != null)
		{
			Uri uri = intent.getData();
			if(uri != null)
			{
				if(uri.getScheme().equals("file"))
				{
					startInstall(intent);
				}
				else if(uri.getScheme().equals("package"))
				{
					startDelete(intent);
				}
			}	
		}
		
		super.onStart(intent, startId);
		return Service.START_NOT_STICKY;
	}
	
	private void startInstall(Intent intent)
	{
		try 
		{
			CldLog.i(TAG, " startInstall ");
			Uri uri;
			PackageInfo pkgInfo;
			uri = intent.getData();
			if(uri != null)
			{
				if(uri.getScheme().equals("file"))
				{
					final File sourceFile = new File(uri.getPath());
					//安装包路径  
					String archiveFilePath=sourceFile.getAbsolutePath();
			        PackageManager pm = getPackageManager();    
			        pkgInfo = pm.getPackageArchiveInfo(archiveFilePath, 
			        		PackageManager.GET_ACTIVITIES); 
			        if(pkgInfo != null)
			        {    
			            ApplicationInfo appInfo = pkgInfo.applicationInfo;  
			            appInfo.sourceDir = archiveFilePath;
			            appInfo.publicSourceDir = archiveFilePath;
			            String appName = pm.getApplicationLabel(appInfo).toString();    
			          
			            InstallInfo info = new InstallInfo();
		            	info.setPath(sourceFile.getAbsolutePath());
		            	info.setPkgName(pkgInfo.packageName);
		            	info.setInstallFlag(true);
		            	info.setAppName(appName);
		            	
		            	InstallerManager.getInstance(
		            			getApplicationContext()).addInstallList(info);
			        }
			        else 
			        {
			        	CldLog.e(TAG, " error apk file: " + sourceFile.getAbsolutePath());
					}
			    }	
			}
		} 
		catch (Exception e) 
		{
			CldLog.e(TAG, " error apk file: " + e.toString());
		}
	}
	
	private void startDelete(Intent intent) 
	{
		CldLog.i(TAG, "startDelete");
		PackageManager pm;
		Uri uri;
		ApplicationInfo appInfo = null;
		uri = intent.getData();

		if (uri != null) 
		{
			String packageName = uri.getEncodedSchemeSpecificPart();
			if (packageName == null) 
			{
				CldLog.e(TAG, " input null package: " + packageName);
				return;
			}

			pm = getPackageManager();
			boolean errFlag = false;
			try 
			{
				appInfo = pm.getApplicationInfo(packageName,
						PackageManager.GET_UNINSTALLED_PACKAGES);
			} 
			catch (NameNotFoundException e) 
			{
				errFlag = true;
				CldLog.e(TAG, " Invalid package name: " + packageName);
			}

			String className = uri.getFragment();
			@SuppressWarnings("unused")
			ActivityInfo activityInfo = null;
			if (className != null) 
			{
				try 
				{
					activityInfo = pm.getActivityInfo(new ComponentName(
							packageName, className), 0);
				} 
				catch (NameNotFoundException e) 
				{
					CldLog.e(TAG, " Invalid className name: " + className);
					errFlag = true;
				}
			}
			
			if (appInfo != null && !errFlag) 
			{
				InstallInfo info = new InstallInfo();
				info.setPkgName(appInfo.packageName);
				info.setInstallFlag(false);
				info.setAppName(appInfo.loadLabel(
						getPackageManager()).toString());
				InstallerManager.getInstance(getApplicationContext())
						.addInstallList(info);
			} 
			else 
			{
				CldLog.e(TAG, " Invalid packageName or componentName in " + uri.toString());
			}
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) 
	{
		return null;
	}
}
