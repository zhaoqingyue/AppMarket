/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: KCloudAppTable.java
 * @Prject: KCloudCenter_M550
 * @Package: cld.kcloud.database
 * @Description: 服务下的应用信息表
 * @author: zhaoqy
 * @date: 2016年8月3日 上午9:56:30
 * @version: V1.0
 */

package cld.kcloud.database;

import java.util.ArrayList;

import com.cld.log.CldLog;

import cld.kcloud.custom.bean.KCloudAppInfo;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class KCloudAppTable 
{
	private static final String TAG = "KCloudAppTable";
	public static final String AUTHORITY = "cld.kcloud.database.DatabaseManager";
	public static final String KCLOUDAPP_TABLE = "kcloudapp_table";                                             
	public static final Uri CONTENT_SORT_URI = Uri.parse("content://" + AUTHORITY + "/" + KCLOUDAPP_TABLE); 
	public static final String ID = "_id";    	
	public static final String SERVICE_CODE = "service_code";     
	public static final String APP_PACKNAME = "app_packname"; 
	
	public static String getCreateSql() 
	{
		StringBuffer sb = new StringBuffer();	
		sb.append("CREATE TABLE  IF NOT EXISTS ");
		sb.append(KCLOUDAPP_TABLE);
		sb.append("(");
		sb.append(ID);
		sb.append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
		sb.append(SERVICE_CODE);
		sb.append(" Integer,");
		sb.append(APP_PACKNAME);
		sb.append(" TEXT");
		sb.append(");");
		
		return sb.toString();
	}  

	public static String getUpgradeSql() 
	{
		String string = "DROP TABLE IF EXISTS " + KCLOUDAPP_TABLE;
		return string;
	}
	
	public static void insertAppInfo(KCloudAppInfo appInfo)
	{
		if (DatabaseManager.mDbHelper == null) 
			return;
		
		if (appInfo == null)
			return;
	
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		
		ContentValues values = new ContentValues();
		values.put(SERVICE_CODE, appInfo.getServiceCode());
		values.put(APP_PACKNAME, appInfo.getAppPackName());
		db.insert(KCLOUDAPP_TABLE, null, values);
		CldLog.d(TAG, " insertAppInfo ");
	}
	
	public static void insertAppInfos(ArrayList<KCloudAppInfo> appInfoList)
	{
		if (DatabaseManager.mDbHelper == null) 
			return;
		
		if (appInfoList == null || appInfoList.isEmpty())
			return;
	
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		
		db.beginTransaction();
		for (int i=0; i<appInfoList.size(); i++) 
		{
			ContentValues values = new ContentValues();
			KCloudAppInfo appInfo = appInfoList.get(i);
			values.put(SERVICE_CODE, appInfo.getServiceCode());
			values.put(APP_PACKNAME, appInfo.getAppPackName());
			db.insert(KCLOUDAPP_TABLE, null, values);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		CldLog.d(TAG, " insertAppInfos length: " + appInfoList.size());
	}
	
	public static KCloudAppInfo queryAppInfo(int serviceCode)
	{
		if (DatabaseManager.mDbHelper == null) 
			return null;
		
		KCloudAppInfo appInfo = null;
		Cursor cursor = null;
		String where = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		where = SERVICE_CODE + "=\"" + serviceCode + "\" ";
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		cursor = db.query(KCLOUDAPP_TABLE, null, where, null, null, null, null);
		if (cursor != null) 
		{
			cursor.setNotificationUri(DatabaseManager.mContext.getContentResolver(),CONTENT_SORT_URI);
			int n = cursor.getCount();
			if (n > 0)
			{
				cursor.moveToPosition(0);
				appInfo = new KCloudAppInfo();
				appInfo.setServiceCode(cursor.getInt(cursor.getColumnIndex(SERVICE_CODE)));
				appInfo.setAppPackName(cursor.getString(cursor.getColumnIndex(APP_PACKNAME)));
			}
		} 
		
		if (cursor != null) 
		{
			cursor.close();
			cursor = null;
		}
		CldLog.d(TAG, " queryAppInfo ");
		return appInfo;
	}
	
	/**
	 * 查询serviceCode对应的KCloudAppInfo
	 */
	public static ArrayList<KCloudAppInfo> queryAppInfos(int serviceCode)
	{
		if (DatabaseManager.mDbHelper == null) 
			return null;
		
		ArrayList<KCloudAppInfo> appInfoList = new ArrayList<KCloudAppInfo>();
		Cursor cursor = null;
		String where = null;
		String orderBy = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		where = SERVICE_CODE + "=\"" + serviceCode + "\" ";
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		orderBy = ID + " ASC";
		cursor = db.query(KCLOUDAPP_TABLE, null, where, null, null, null, orderBy);
		if (cursor != null) 
		{
			cursor.setNotificationUri(DatabaseManager.mContext.getContentResolver(), CONTENT_SORT_URI);
			int n = cursor.getCount();
			for (int i=0; i<n; i++) 
			{
				cursor.moveToPosition(i);
				KCloudAppInfo appInfo = new KCloudAppInfo();
				appInfo.setServiceCode(cursor.getInt(cursor.getColumnIndex(SERVICE_CODE)));
				appInfo.setAppPackName(cursor.getString(cursor.getColumnIndex(APP_PACKNAME)));
				appInfoList.add(appInfo);
			}
		} 
		
		if (cursor != null) 
		{
			cursor.close();
			cursor = null;
		}
		CldLog.d(TAG, " queryAppInfos length: " + appInfoList.size());
		return appInfoList;
	}
	
	/**
	 * 查询所有的KCloudAppInfo
	 */
	public static ArrayList<KCloudAppInfo> queryAppInfos()
	{
		if (DatabaseManager.mDbHelper == null) 
			return null;
		
		ArrayList<KCloudAppInfo> appInfoList = new ArrayList<KCloudAppInfo>();
		Cursor cursor = null;
		String where = null;
		String orderBy = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		orderBy = ID + " ASC";
		cursor = db.query(KCLOUDAPP_TABLE, null, where, null, null, null, orderBy);
		if (cursor != null) 
		{
			cursor.setNotificationUri(DatabaseManager.mContext.getContentResolver(), CONTENT_SORT_URI);
			int n = cursor.getCount();
			for (int i=0; i<n; i++) 
			{
				cursor.moveToPosition(i);
				KCloudAppInfo appInfo = new KCloudAppInfo();
				appInfo.setServiceCode(cursor.getInt(cursor.getColumnIndex(SERVICE_CODE)));
				appInfo.setAppPackName(cursor.getString(cursor.getColumnIndex(APP_PACKNAME)));
				appInfoList.add(appInfo);
			}
		} 
		
		if (cursor != null) 
		{
			cursor.close();
			cursor = null;
		}
		CldLog.d(TAG, " queryAppInfos length: " + appInfoList.size());
		return appInfoList;
	}
	
	/**
	 * 删除serviceCode对应的KCloudAppInfo
	 */
	public static void deleteAppInfos(int serviceCode)
	{
		if (DatabaseManager.mDbHelper == null) 
			return ;
	
		String where = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		where = SERVICE_CODE + "=\"" + serviceCode + "\" ";
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		db.delete(KCLOUDAPP_TABLE, where, null);
		CldLog.d(TAG, " deleteAppInfos ");
	}	
	
	/**
	 * 删除所有KCloudAppInfo
	 */
	public static void deleteAppInfos()
	{
		if (DatabaseManager.mDbHelper == null) 
			return;
	
		String where = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		db.delete(KCLOUDAPP_TABLE, where, null);
		CldLog.d(TAG, " deleteAppInfos ");
	}	
}
