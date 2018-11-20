/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: KCloudInstalledTable.java
 * @Prject: KCloudCenter_M550
 * @Package: cld.kcloud.database
 * @Description: 已安装应用表
 * @author: zhaoqy
 * @date: 2016年8月3日 下午3:14:56
 * @version: V1.0
 */

package cld.kcloud.database;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import cld.kcloud.center.KCloudCtx;
import cld.kcloud.center.R;
import cld.kcloud.custom.bean.KCloudInstalledInfo;
import cld.kcloud.utils.KCloudCommonUtil;

import com.cld.log.CldLog;

public class KCloudInstalledTable 
{
	private static final String TAG = "KCloudInstalledTable";
	public static final String AUTHORITY = "cld.kcloud.database.DatabaseManager";
	public static final String KCLOUD_INSTALLED_TABLE = "kcloud_installed_table";                                             
	public static final Uri CONTENT_SORT_URI = Uri.parse("content://" + AUTHORITY + "/" + KCLOUD_INSTALLED_TABLE); 
	public static final String ID = "_id";    	
	public static final String INSTALLED_PKGNAME = "installed_pkgname";
	public static final String INSTALLED_VERSIONCODE = "installed_versioncode";
	public static final String INSTALLED_VALIDATE = "installed_validate";
	
	private static KCloudInstalledTable mInstance = null;
	public static KCloudInstalledTable getInstance() 
	{
		if (mInstance == null) 
		{
			synchronized (KCloudInstalledTable.class) 
			{
				if (mInstance == null) 
				{
					mInstance = new KCloudInstalledTable();
				}
			}
		}
		return mInstance;
	}
	
	public String getCreateSql() 
	{
		StringBuffer sb = new StringBuffer();	
		sb.append("CREATE TABLE  IF NOT EXISTS ");
		sb.append(KCLOUD_INSTALLED_TABLE);
		sb.append("(");
		sb.append(ID);
		sb.append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
		sb.append(INSTALLED_PKGNAME);
		sb.append(" TEXT,");
		sb.append(INSTALLED_VERSIONCODE);
		sb.append(" Integer,");
		sb.append(INSTALLED_VALIDATE);
		sb.append(" Integer");
		sb.append(");");
		
		return sb.toString();
	}  
	
	public String getUpgradeSql() 
	{
		String string = "DROP TABLE IF EXISTS " + KCLOUD_INSTALLED_TABLE;
		return string;
	}
	
	/**
	 * 
	 * @Title: insertInstalledInfo
	 * @Description: 插入installedInfo
	 * @param installedInfo
	 * @return: void
	 */
	public void insertInstalledInfo(KCloudInstalledInfo installedInfo)
	{
		if (DatabaseManager.mDbHelper == null) 
			return;
		
		if (installedInfo == null)
			return;
	
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		
		ContentValues values = new ContentValues();
		values.put(INSTALLED_PKGNAME, installedInfo.getPkgName());
		values.put(INSTALLED_VERSIONCODE, installedInfo.getVerCode());
		values.put(INSTALLED_VALIDATE, installedInfo.getValidate());
		db.insert(KCLOUD_INSTALLED_TABLE, null, values);
		CldLog.d(TAG, " insertInstalledInfo ");
	}
	
	/**
	 * 
	 * @Title: insertInstalledInfos
	 * @Description: 插入应用列表
	 * @param installedInfoList
	 * @return: void
	 */
	public void insertInstalledInfos(ArrayList<KCloudInstalledInfo> installedInfoList)
	{
		if (DatabaseManager.mDbHelper == null) 
			return;
		
		if (installedInfoList == null || installedInfoList.isEmpty())
			return;
	
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		
		db.beginTransaction();
		for (int i=0; i<installedInfoList.size(); i++) 
		{
			ContentValues values = new ContentValues();
			KCloudInstalledInfo installedInfo = installedInfoList.get(i);
			values.put(INSTALLED_PKGNAME, installedInfo.getPkgName());
			values.put(INSTALLED_VERSIONCODE, installedInfo.getVerCode());
			values.put(INSTALLED_VALIDATE, installedInfo.getValidate());
			db.insert(KCLOUD_INSTALLED_TABLE, null, values);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	/**
	 * 
	 * @Title: updateInstalledInfo
	 * @Description: 更新installedInfo
	 * @param installedInfo
	 * @return: void
	 */
	public void updateInstalledInfo(KCloudInstalledInfo installedInfo)
	{
		if (DatabaseManager.mDbHelper == null) 
			return;
		
		if (installedInfo == null)
			return;
		
		String where = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		
		where = INSTALLED_PKGNAME + "=\"" + installedInfo.getPkgName() + "\" ";
		ContentValues values = new ContentValues();
		values.put(INSTALLED_VERSIONCODE, installedInfo.getVerCode());
		db.update(KCLOUD_INSTALLED_TABLE, values, where, null);
		CldLog.d(TAG, " updateInstalledInfo " + installedInfo.getPkgName() + ", " + installedInfo.getVerCode());
	}
	
	/**
	 * 
	 * @Title: queryInstalledInfo
	 * @Description: 查询pkgname对应的应用信息
	 * @param pkgname
	 * @return
	 * @return: KCloudInstalledInfo
	 */
	public KCloudInstalledInfo queryInstalledInfo(String pkgname)
	{
		if (DatabaseManager.mDbHelper == null) 
			return null;
		
		KCloudInstalledInfo installedInfo = null;
		Cursor cursor = null;
		String where = null;
		String orderBy = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		where = INSTALLED_PKGNAME + "=\"" + pkgname + "\" ";
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		orderBy = ID + " ASC";
		cursor = db.query(KCLOUD_INSTALLED_TABLE, null, where, null, null, null, orderBy);
		if (cursor != null) 
		{
			cursor.setNotificationUri(DatabaseManager.mContext.getContentResolver(), CONTENT_SORT_URI);
			int n = cursor.getCount();
			if (n > 0)
			{
				cursor.moveToPosition(0);
				installedInfo = new KCloudInstalledInfo();
				installedInfo.setPkgName(cursor.getString(cursor.getColumnIndex(INSTALLED_PKGNAME)));
				installedInfo.setVerCode(cursor.getInt(cursor.getColumnIndex(INSTALLED_VERSIONCODE)));
				installedInfo.setValidate(cursor.getInt(cursor.getColumnIndex(INSTALLED_VALIDATE)));
			}
		} 
		
		if (cursor != null) 
		{
			cursor.close();
			cursor = null;
		}
		CldLog.d(TAG, " queryInstalledInfo " + pkgname);
		return installedInfo;
	}
	
	/**
	 * 
	 * @Title: queryInstalledInfos
	 * @Description: 查询所有已安装应用信息
	 * @return
	 * @return: ArrayList<KCloudInstalledInfo>
	 */
	public ArrayList<KCloudInstalledInfo> queryInstalledInfos()
	{
		if (DatabaseManager.mDbHelper == null) 
			return null;
		
		ArrayList<KCloudInstalledInfo> installedInfoList = new ArrayList<KCloudInstalledInfo>();
		Cursor cursor = null;
		String where = null;
		String orderBy = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		orderBy = ID + " ASC";
		cursor = db.query(KCLOUD_INSTALLED_TABLE, null, where, null, null, null, orderBy);
		if (cursor != null) 
		{
			cursor.setNotificationUri(DatabaseManager.mContext.getContentResolver(), CONTENT_SORT_URI);
			int n = cursor.getCount();
			for (int i=0; i<n; i++) 
			{
				cursor.moveToPosition(i);
				KCloudInstalledInfo installedInfo = new KCloudInstalledInfo();
				installedInfo.setPkgName(cursor.getString(cursor.getColumnIndex(INSTALLED_PKGNAME)));
				installedInfo.setVerCode(cursor.getInt(cursor.getColumnIndex(INSTALLED_VERSIONCODE)));
				installedInfo.setValidate(cursor.getInt(cursor.getColumnIndex(INSTALLED_VALIDATE)));
				installedInfoList.add(installedInfo);
			}
		} 
		
		if (cursor != null) 
		{
			cursor.close();
			cursor = null;
		}
		CldLog.d(TAG, " queryInstalledInfos length: " + installedInfoList.size());
		return installedInfoList;
	}
	
	/**
	 * 
	 * @Title: deleteInstalledInfo
	 * @Description: 删除pkgname对应的应用
	 * @param pkgname
	 * @return: void
	 */
	public void deleteInstalledInfo(String pkgname)
	{
		if (DatabaseManager.mDbHelper == null) 
			return ;
	
		String where = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		where = INSTALLED_PKGNAME + "=\"" + pkgname + "\" ";
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		db.delete(KCLOUD_INSTALLED_TABLE, where, null);
		CldLog.d(TAG, " deleteInstalledInfo " + pkgname);
	}	
	
	/**
	 * 
	 * @Title: deleteInstalledInfos
	 * @Description: 删除所有已安装应用信息
	 * @return: void
	 */
	public void deleteInstalledInfos()
	{
		if (DatabaseManager.mDbHelper == null) 
			return ;
	
		String where = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		db.delete(KCLOUD_INSTALLED_TABLE, where, null);
		CldLog.d(TAG, " deleteInstalledInfos ");
	}
	
	/**
	 * 
	 * @Title: addDefaultInstalledInfos
	 * @Description: 添加内置已安装应用
	 * @return: void
	 */
	public void addDefaultInstalledInfos() 
	{
		CldLog.d(TAG, " addDefaultInstalledInfos ");
		insertInstalledInfos(getDefaultInstalledInfos());
	}
	
	/**
	 * 
	 * @Title: getDefaultInstalledInfos
	 * @Description: 获取内置已安装应用信息
	 * @return
	 * @return: ArrayList<KCloudInstalledInfo>
	 */
	public ArrayList<KCloudInstalledInfo> getDefaultInstalledInfos()
	{
		ArrayList<KCloudInstalledInfo> installedInfos = new ArrayList<KCloudInstalledInfo>();
		Context context = KCloudCtx.getAppContext();
		String[] packages = context.getResources().getStringArray(R.array.installed_pkgname);
		for (int i=0; i<packages.length; i++)
		{
			CldLog.d(TAG, " addDefaultInstalledInfos pkgname: " + packages[i]);
			KCloudInstalledInfo installedInfo = new KCloudInstalledInfo();
			installedInfo.setPkgName(packages[i]);
			installedInfo.setVerCode(KCloudCommonUtil.getVercodeByPkgname(packages[i]));
			installedInfos.add(installedInfo);
		}
		return installedInfos;
	}
	
	/**
	 * 
	 * @Title: isDefaultInstalledInfo
	 * @Description: 是否是内置已安装应用
	 * @param pkgname
	 * @return
	 * @return: boolean
	 */
	@SuppressLint("NewApi")
	public boolean isDefaultInstalledInfo(String pkgname)
	{
		if (pkgname != null && !pkgname.isEmpty())
		{
			Context context = KCloudCtx.getAppContext();
			String[] packages = context.getResources().getStringArray(R.array.installed_pkgname);
			for (int i=0; i<packages.length; i++)
			{
				if (pkgname.equals(packages[i]))
				{
					return true;
				}
			}
		}
		return false;
	}
}
