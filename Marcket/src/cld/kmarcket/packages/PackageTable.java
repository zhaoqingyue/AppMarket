package cld.kmarcket.packages;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import cld.kmarcket.KMarcketApplication;
import cld.kmarcket.appinfo.AppInfo;
import cld.kmarcket.util.ConstantUtil;
import cld.kmarcket.util.LogUtil;

public class PackageTable 
{
	public static final String TABLE_NAME = "valid_pakcages";
	public static final String PACKAGE_NAME = "pakcage_name";
	public static final String APP_WIDGET = "app_widget"; 
	public static final String VERSION_CODE = "version_code";
	public static final String APP_VALIDATE = "app_validate";
	public static final String APP_DESC = "app_desc";   
	private static PackageTable mInstance = null;
	private Context mContext;
	
	public static PackageTable getInstance()
	{
		if(mInstance == null)
		{
			synchronized(PackageTable.class)
			{
				if(mInstance == null)
				{
					mInstance = new PackageTable();
				}
			}
		}
		return mInstance;
	}
	
	public static void static_release()
	{
		if(mInstance != null)
		{
			mInstance = null;
		}
	}
	
	private PackagesDBOpenHelper mHelper = null;
	
	private PackageTable()
	{
		mContext = KMarcketApplication.getContext();
		mHelper = new PackagesDBOpenHelper(mContext);
	}
	
	public static String getCreateSql() 
	{
		StringBuffer sb = new StringBuffer();	
		sb.append("CREATE TABLE ");
		sb.append(TABLE_NAME);
		sb.append("(");
		sb.append(PACKAGE_NAME);
		sb.append(" VARCHAR(1024), ");
		sb.append(APP_WIDGET);
		sb.append(" INTEGER, ");
		sb.append(VERSION_CODE);
		sb.append(" INTEGER, ");
		sb.append(APP_VALIDATE);
		sb.append(" INTEGER, ");
		sb.append(APP_DESC);
		sb.append(" VARCHAR(1024), ");
		sb.append("PRIMARY KEY(");
		sb.append(PACKAGE_NAME);
		sb.append(")");
		sb.append(")");
		
		LogUtil.i(LogUtil.TAG, "pakcage sql: " + sb);
		return sb.toString();
	}
	
	public void insertPackage(AppInfo appInfo)
	{
		if (appInfo == null)
		{
			LogUtil.i(LogUtil.TAG, "insertPackage pkgName is null");
			return;
		}
		LogUtil.i(LogUtil.TAG, "insertPackage pkgName:" + appInfo.getPkgName());
		SQLiteDatabase database = mHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(PACKAGE_NAME, appInfo.getPkgName());
		values.put(APP_WIDGET, appInfo.getIsWidget());
		values.put(VERSION_CODE, appInfo.getVerCode());
		values.put(APP_VALIDATE, appInfo.getValidate());
		values.put(APP_DESC, appInfo.getAppDesc());
		database.insert(TABLE_NAME, null, values);
	}
	
	public ArrayList<AppInfo> queryPackages()
	{
		LogUtil.i(LogUtil.TAG, " +++++ queryPackages +++++ ");
		ArrayList<AppInfo> packages = new ArrayList<AppInfo>();
		Cursor cursor = null;
		SQLiteDatabase database = mHelper.getWritableDatabase();
		cursor = database.query(TABLE_NAME, null, null, null, null, null, null);
		if (cursor != null) 
		{
			PackageManager pm = mContext.getPackageManager();
			int n = cursor.getCount();
			for (int i=0; i<n; i++) 
			{
				cursor.moveToPosition(i);
				AppInfo appinfo = new AppInfo();
				String pkgname = cursor.getString(cursor.getColumnIndex(PACKAGE_NAME));
				appinfo.setPkgName(pkgname);
				appinfo.setIsWidget(cursor.getInt(cursor.getColumnIndex(APP_WIDGET)));
				appinfo.setVerCode(cursor.getInt(cursor.getColumnIndex(VERSION_CODE)));
				appinfo.setValidate(cursor.getInt(cursor.getColumnIndex(APP_VALIDATE)));
				appinfo.setAppDesc(cursor.getString(cursor.getColumnIndex(APP_DESC)));
				try 
				{
					PackageInfo packageInfo = pm.getPackageInfo(pkgname, 
							PackageManager.GET_PERMISSIONS);
					appinfo.setAppName(packageInfo.applicationInfo.loadLabel(pm).toString());
					appinfo.setVerName(packageInfo.versionName);
					appinfo.setAppIcon(packageInfo.applicationInfo.loadIcon(pm));
					appinfo.setStatus(ConstantUtil.APP_STATUS_OPEN); //表示打开
				} 
				catch (NameNotFoundException e) 
				{
					e.printStackTrace();
				}
				packages.add(appinfo);
			}
		}
		if (cursor != null) 
		{
			cursor.close();
			cursor = null;
		}
		return packages;
	}
	
	public ArrayList<String> queryPackageName()
	{
		LogUtil.i(LogUtil.TAG, " +++++ queryPackageName +++++ ");
		ArrayList<String> packagenames = new ArrayList<String>();
		Cursor cursor = null;
		SQLiteDatabase database = mHelper.getWritableDatabase();
		cursor = database.query(TABLE_NAME, null, null, null, null, null, null);
		if (cursor != null) 
		{
			int n = cursor.getCount();
			for (int i=0; i<n; i++) 
			{
				cursor.moveToPosition(i);
				String pkgname = cursor.getString(cursor.getColumnIndex(PACKAGE_NAME));
				packagenames.add(pkgname);
			}
		}
		if (cursor != null) 
		{
			cursor.close();
			cursor = null;
		}
		return packagenames;
	}
	
	@SuppressLint("NewApi") 
	public AppInfo queryPackage(String pkgName)
	{
		LogUtil.i(LogUtil.TAG, " +++++ query pkgName:" + pkgName + " +++++ ");
		if (pkgName == null || pkgName.isEmpty())
		{
			return null;
		}
		AppInfo appinfo = null;
		Cursor cursor = null;
		String where;
		SQLiteDatabase database = mHelper.getWritableDatabase();
		where = PACKAGE_NAME + "=\"" + pkgName + "\"";
		cursor = database.query(TABLE_NAME, null,
				where, null, null, null, null);
		if (cursor != null) 
		{
			PackageManager pm = mContext.getPackageManager();
			int n = cursor.getCount();
			for (int i=0; i<n; i++) 
			{
				cursor.moveToPosition(i);
				appinfo = new AppInfo();
				String pkgname = cursor.getString(cursor.getColumnIndex(PACKAGE_NAME));
				appinfo.setPkgName(pkgname);
				appinfo.setIsWidget(cursor.getInt(cursor.getColumnIndex(APP_WIDGET)));
				appinfo.setVerCode(cursor.getInt(cursor.getColumnIndex(VERSION_CODE)));
				appinfo.setValidate(cursor.getInt(cursor.getColumnIndex(APP_VALIDATE)));
				appinfo.setAppDesc(cursor.getString(cursor.getColumnIndex(APP_DESC)));
				try 
				{
					PackageInfo packageInfo = pm.getPackageInfo(pkgname, 
							PackageManager.GET_PERMISSIONS);
					appinfo.setAppName(packageInfo.applicationInfo.loadLabel(pm).toString());
					appinfo.setVerName(packageInfo.versionName);
					appinfo.setAppIcon(packageInfo.applicationInfo.loadIcon(pm));
				} 
				catch (NameNotFoundException e) 
				{
					e.printStackTrace();
				}
			}
		}
		if (cursor != null) 
		{
			cursor.close();
			cursor = null;
		}
		return appinfo;
	}
	
	public void updatePackage(AppInfo appinfo)
	{
		if (appinfo == null)
		{
			LogUtil.i(LogUtil.TAG, "updatePackage pkgName is null");
			return;
		}
		
		//add by zhaoqy 2016-6-21(卸载K应用后，已安装的非内置应用出现在推荐应用，下载安装后当做升级了)
		AppInfo temp = queryPackage(appinfo.getPkgName());
		if (temp == null)
		{
			LogUtil.i(LogUtil.TAG, appinfo.getPkgName() + "is not in valid_pakcages");
			insertPackage(appinfo);
			return;
		}
		
		LogUtil.i(LogUtil.TAG, "updatePackage pkgName:" + appinfo.getPkgName());
		String where;
		SQLiteDatabase database = mHelper.getWritableDatabase();
		where = PACKAGE_NAME + "=\"" + appinfo.getPkgName() + "\"";
		ContentValues values = new ContentValues();
		values.put(VERSION_CODE, appinfo.getVerCode());
		database.update(TABLE_NAME, values, where, null);
	}
	
	@SuppressLint("NewApi") 
	public void deletePackage(String pkgname)
	{
		LogUtil.i(LogUtil.TAG, "deletePackage pkgName:" + pkgname);
		if (pkgname == null || pkgname.isEmpty())
		{
			return;
		}
		SQLiteDatabase database = mHelper.getWritableDatabase();
		String where = null;
		where = PACKAGE_NAME + "=\"" + pkgname + "\" ";
		database.delete(TABLE_NAME, where, null);
	}
}
