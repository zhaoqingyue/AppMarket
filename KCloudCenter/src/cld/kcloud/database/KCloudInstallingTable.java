/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: KCloudInstallingTable.java
 * @Prject: KCloudCenter_M550
 * @Package: cld.kcloud.database
 * @Description: 正在安装Table
 * @author: zhaoqy
 * @date: 2016年8月15日 下午4:58:24
 * @version: V1.0
 */

package cld.kcloud.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import cld.kcloud.custom.bean.KCloudInstalledInfo;
import com.cld.log.CldLog;

public class KCloudInstallingTable 
{
	private static final String TAG = "KCloudInstallingTable";
	public static final String AUTHORITY = "cld.kcloud.database.DatabaseManager";
	public static final String KCLOUD_INSTALLING_TABLE = "kcloud_installing_table";                                             
	public static final Uri CONTENT_SORT_URI = Uri.parse("content://" + AUTHORITY + "/" + KCLOUD_INSTALLING_TABLE); 
	public static final String ID = "_id";    	
	public static final String INSTALLING_PKGNAME = "installing_pkgname";
	public static final String INSTALLING_VERSION = "installing_version";
	public static final String INSTALLING_URLPATH = "installing_urlpath";
	public static final String INSTALLING_CURTIME = "installing_curtime";

	private static KCloudInstallingTable mInstance = null;
	public static KCloudInstallingTable getInstance() 
	{
		if (mInstance == null) 
		{
			synchronized (KCloudInstallingTable.class) 
			{
				if (mInstance == null) 
				{
					mInstance = new KCloudInstallingTable();
				}
			}
		}
		return mInstance;
	}
	
	public String getCreateSql() 
	{
		StringBuffer sb = new StringBuffer();	
		sb.append("CREATE TABLE  IF NOT EXISTS ");
		sb.append(KCLOUD_INSTALLING_TABLE);
		sb.append("(");
		sb.append(ID);
		sb.append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
		sb.append(INSTALLING_PKGNAME);
		sb.append(" TEXT,");
		sb.append(INSTALLING_VERSION);
		sb.append(" Integer,");
		sb.append(INSTALLING_URLPATH);
		sb.append(" TEXT,");
		sb.append(INSTALLING_CURTIME);
		sb.append(" Integer");
		sb.append(");");
		
		return sb.toString();
	}  
	
	public String getUpgradeSql() 
	{
		String string = "DROP TABLE IF EXISTS " + KCLOUD_INSTALLING_TABLE;
		return string;
	}
	
	/**
	 * 
	 * @Title: insertInstallingInfo
	 * @Description: 插入installedInfo
	 * @param installedInfo
	 * @return: void
	 */
	public void insertInstallingInfo(KCloudInstalledInfo installedInfo)
	{
		if (DatabaseManager.mDbHelper == null) 
			return;
		
		if (installedInfo == null)
			return;
	
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		
		ContentValues values = new ContentValues();
		values.put(INSTALLING_PKGNAME, installedInfo.getPkgName());
		values.put(INSTALLING_VERSION, installedInfo.getVerCode());
		values.put(INSTALLING_URLPATH, installedInfo.getAppUrl());
		values.put(INSTALLING_CURTIME, System.currentTimeMillis());
		db.insert(KCLOUD_INSTALLING_TABLE, null, values);
		CldLog.d(TAG, " insertInstallingInfo ");
	}
	
	/**
	 * 
	 * @Title: updateInstallingInfo
	 * @Description: 更新installedInfo
	 * @param installedInfo
	 * @return: void
	 */
	public void updateInstallingInfo(KCloudInstalledInfo installedInfo)
	{
		if (DatabaseManager.mDbHelper == null) 
			return;
		
		if (installedInfo == null)
			return;
		
		String where = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		
		where = INSTALLING_PKGNAME + "=\"" + installedInfo.getPkgName() + "\" ";
		ContentValues values = new ContentValues();
		values.put(INSTALLING_VERSION, installedInfo.getVerCode());
		values.put(INSTALLING_URLPATH, installedInfo.getAppUrl());
		values.put(INSTALLING_CURTIME, System.currentTimeMillis());
		db.update(KCLOUD_INSTALLING_TABLE, values, where, null);
		CldLog.d(TAG, " updateInstallingInfo " + installedInfo.getPkgName());
	}
	
	/**
	 * 
	 * @Title: queryInstallingInfo
	 * @Description: 查询pkgname对应的应用信息
	 * @param pkgname
	 * @return
	 * @return: KCloudInstalledInfo
	 */
	public KCloudInstalledInfo queryInstallingInfo(String pkgname)
	{
		if (DatabaseManager.mDbHelper == null) 
			return null;
		
		KCloudInstalledInfo installedInfo = null;
		Cursor cursor = null;
		String where = null;
		String orderBy = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		where = INSTALLING_PKGNAME + "=\"" + pkgname + "\" ";
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		orderBy = ID + " ASC";
		cursor = db.query(KCLOUD_INSTALLING_TABLE, null, where, null, null, null, orderBy);
		if (cursor != null) 
		{
			cursor.setNotificationUri(DatabaseManager.mContext.getContentResolver(), CONTENT_SORT_URI);
			int n = cursor.getCount();
			if (n > 0)
			{
				cursor.moveToPosition(0);
				installedInfo = new KCloudInstalledInfo();
				installedInfo.setPkgName(cursor.getString(cursor.getColumnIndex(INSTALLING_PKGNAME)));
				installedInfo.setVerCode(cursor.getInt(cursor.getColumnIndex(INSTALLING_VERSION)));
				installedInfo.setAppUrl(cursor.getString(cursor.getColumnIndex(INSTALLING_URLPATH)));
				installedInfo.setInstallTime(cursor.getLong(cursor.getColumnIndex(INSTALLING_CURTIME)));
			}
		} 
		
		if (cursor != null) 
		{
			cursor.close();
			cursor = null;
		}
		
		CldLog.d(TAG, " queryInstallingInfo " + pkgname);
		return installedInfo;
	}
	
	/**
	 * 
	 * @Title: deleteInstallingInfo
	 * @Description: 删除pkgname对应的应用
	 * @param pkgname
	 * @return: void
	 */
	public void deleteInstallingInfo(String pkgname)
	{
		if (DatabaseManager.mDbHelper == null) 
			return ;
	
		String where = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		where = INSTALLING_PKGNAME + "=\"" + pkgname + "\" ";
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		db.delete(KCLOUD_INSTALLING_TABLE, where, null);
		CldLog.d(TAG, " deleteInstallingInfo " + pkgname);
	}	
	
	/**
	 * 
	 * @Title: deleteInstallingInfos
	 * @Description: 删除所有已安装应用信息
	 * @return: void
	 */
	public void deleteInstallingInfos()
	{
		if (DatabaseManager.mDbHelper == null) 
			return ;
	
		String where = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		db.delete(KCLOUD_INSTALLING_TABLE, where, null);
		CldLog.d(TAG, " deleteInstallingInfos ");
	}
}
