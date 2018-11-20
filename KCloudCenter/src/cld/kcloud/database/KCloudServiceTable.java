/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: KCloudServiceTable.java
 * @Prject: KCloudCenter_M550
 * @Package: cld.kcloud.database
 * @Description: 套餐下的服务信息表
 * @author: zhaoqy
 * @date: 2016年8月3日 上午9:54:49
 * @version: V1.0
 */

package cld.kcloud.database;

import java.util.ArrayList;

import com.cld.log.CldLog;

import cld.kcloud.custom.bean.KCloudServiceInfo;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class KCloudServiceTable 
{
	private static final String TAG = "KCloudServiceTable";
	public static final String AUTHORITY = "cld.kcloud.database.DatabaseManager";
	public static final String KCLOUDSERVICE_TABLE = "kcloudservice_table";                                             
	public static final Uri CONTENT_SORT_URI = Uri.parse("content://" + AUTHORITY + "/" + KCLOUDSERVICE_TABLE); 
	public static final String ID = "_id";  
	public static final String PACKAGE_CODE = "package_code";    
	public static final String PACKAGE_STATUS = "package_status";    
	public static final String SERVICE_CODE = "service_code";     
	public static final String SERVICE_NAME = "service_name"; 
	public static final String SERVICE_MONTH = "service_month"; 
	public static final String SERVICE_CHARGE = "service_charge"; 
	public static final String SERVICE_DESC = "service_desc"; 
	public static final String SERVICE_STATUS = "service_status"; 
	public static final String SERVICE_ICON = "service_icon"; 
	
	public static String getCreateSql() 
	{
		StringBuffer sb = new StringBuffer();	
		sb.append("CREATE TABLE  IF NOT EXISTS ");
		sb.append(KCLOUDSERVICE_TABLE);
		sb.append("(");
		sb.append(ID);
		sb.append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
		sb.append(PACKAGE_CODE);
		sb.append(" Integer,");
		sb.append(PACKAGE_STATUS);
		sb.append(" Integer,");
		sb.append(SERVICE_CODE);
		sb.append(" Integer,");
		sb.append(SERVICE_NAME);
		sb.append(" TEXT,");
		sb.append(SERVICE_MONTH);
		sb.append(" Integer,");
		sb.append(SERVICE_CHARGE);
		sb.append(" Integer,");
		sb.append(SERVICE_DESC);
		sb.append(" TEXT,");
		sb.append(SERVICE_STATUS);
		sb.append(" Integer,");
		sb.append(SERVICE_ICON);
		sb.append(" TEXT");
		sb.append(");");
		
		return sb.toString();
	}  
	
	public static String getUpgradeSql() 
	{
		String string = "DROP TABLE IF EXISTS " + KCLOUDSERVICE_TABLE;
		return string;
	}
	
	public static void insertServiceInfo(KCloudServiceInfo serviceInfo)
	{
		if (DatabaseManager.mDbHelper == null) 
			return;
		
		if (serviceInfo == null)
			return;
	
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		
		ContentValues values = new ContentValues();
		values.put(PACKAGE_CODE, serviceInfo.getComboCode());
		values.put(PACKAGE_STATUS, serviceInfo.getComboStatus());
		values.put(SERVICE_CODE, serviceInfo.getServiceCode());
		values.put(SERVICE_NAME, serviceInfo.getServiceName());
		values.put(SERVICE_MONTH, serviceInfo.getServiceMonth());
		values.put(SERVICE_CHARGE, serviceInfo.getServiceCharge());
		values.put(SERVICE_DESC, serviceInfo.getServiceDesc());
		values.put(SERVICE_STATUS, serviceInfo.getServiceStatus());
		values.put(SERVICE_ICON, serviceInfo.getServiceIcon());
		db.insert(KCLOUDSERVICE_TABLE, null, values);
		CldLog.d(TAG, " insertServiceInfo ");
	}
	
	public static void insertServiceInfos(ArrayList<KCloudServiceInfo> serviceInfoList)
	{
		if (DatabaseManager.mDbHelper == null) 
			return;
		
		if (serviceInfoList == null || serviceInfoList.isEmpty())
			return;
	
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		
		db.beginTransaction();
		for (int i=0; i<serviceInfoList.size(); i++) 
		{
			KCloudServiceInfo serviceInfo = serviceInfoList.get(i);
			ContentValues values = new ContentValues();
			values.put(PACKAGE_CODE, serviceInfo.getComboCode());
			values.put(PACKAGE_STATUS, serviceInfo.getComboStatus());
			values.put(SERVICE_CODE, serviceInfo.getServiceCode());
			values.put(SERVICE_NAME, serviceInfo.getServiceName());
			values.put(SERVICE_MONTH, serviceInfo.getServiceMonth());
			values.put(SERVICE_CHARGE, serviceInfo.getServiceCharge());
			values.put(SERVICE_DESC, serviceInfo.getServiceDesc());
			values.put(SERVICE_STATUS, serviceInfo.getServiceStatus());
			values.put(SERVICE_ICON, serviceInfo.getServiceIcon());
			db.insert(KCLOUDSERVICE_TABLE, null, values);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		CldLog.d(TAG, " insertServiceInfos length: " + serviceInfoList.size());
	}
	
	/**
	 * 查询serviceCode对应的KCloudAppInfo
	 */
	public static ArrayList<KCloudServiceInfo> queryServiceInfos(int packageCode)
	{
		if (DatabaseManager.mDbHelper == null) 
			return null;
		
		ArrayList<KCloudServiceInfo> serviceInfoList = new ArrayList<KCloudServiceInfo>();
		Cursor cursor = null;
		String where = null;
		String orderBy = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		where = SERVICE_CODE + "=\"" + packageCode + "\" ";
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		orderBy = ID + " ASC";
		cursor = db.query(KCLOUDSERVICE_TABLE, null, where, null, null, null, orderBy);
		if (cursor != null) 
		{
			cursor.setNotificationUri(DatabaseManager.mContext.getContentResolver(), CONTENT_SORT_URI);
			int n = cursor.getCount();
			for (int i=0; i<n; i++) 
			{
				cursor.moveToPosition(i);
				KCloudServiceInfo serviceInfo = new KCloudServiceInfo();
				serviceInfo.setComboCode(cursor.getInt(cursor.getColumnIndex(PACKAGE_CODE)));
				serviceInfo.setComboStatus(cursor.getInt(cursor.getColumnIndex(PACKAGE_STATUS)));
				serviceInfo.setServiceCode(cursor.getInt(cursor.getColumnIndex(SERVICE_CODE)));
				serviceInfo.setServiceName(cursor.getString(cursor.getColumnIndex(SERVICE_NAME)));
				serviceInfo.setServiceMonth(cursor.getInt(cursor.getColumnIndex(SERVICE_MONTH)));
				serviceInfo.setServiceCharge(cursor.getInt(cursor.getColumnIndex(SERVICE_CHARGE)));
				serviceInfo.setServiceDesc(cursor.getString(cursor.getColumnIndex(SERVICE_DESC)));
				serviceInfo.setServiceStatus(cursor.getInt(cursor.getColumnIndex(SERVICE_STATUS)));
				serviceInfo.setServiceIcon(cursor.getString(cursor.getColumnIndex(SERVICE_ICON)));
				serviceInfoList.add(serviceInfo);
			}
		} 
		
		if (cursor != null) 
		{
			cursor.close();
			cursor = null;
		}
		CldLog.d(TAG, " queryServiceInfos length: " + serviceInfoList.size());
		return serviceInfoList;
	}
	
	/**
	 * 查询所有的KCloudAppInfo
	 */
	public static ArrayList<KCloudServiceInfo> queryServiceInfos()
	{
		if (DatabaseManager.mDbHelper == null) 
			return null;
		
		ArrayList<KCloudServiceInfo> serviceInfoList = new ArrayList<KCloudServiceInfo>();
		Cursor cursor = null;
		String where = null;
		String orderBy = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		orderBy = ID + " ASC";
		cursor = db.query(KCLOUDSERVICE_TABLE, null, where, null, null, null, orderBy);
		if (cursor != null) 
		{
			cursor.setNotificationUri(DatabaseManager.mContext.getContentResolver(), CONTENT_SORT_URI);
			int n = cursor.getCount();
			for (int i=0; i<n; i++) 
			{
				cursor.moveToPosition(i);
				KCloudServiceInfo serviceInfo = new KCloudServiceInfo();
				serviceInfo.setComboCode(cursor.getInt(cursor.getColumnIndex(PACKAGE_CODE)));
				serviceInfo.setComboStatus(cursor.getInt(cursor.getColumnIndex(PACKAGE_STATUS)));
				serviceInfo.setServiceCode(cursor.getInt(cursor.getColumnIndex(SERVICE_CODE)));
				serviceInfo.setServiceName(cursor.getString(cursor.getColumnIndex(SERVICE_NAME)));
				serviceInfo.setServiceMonth(cursor.getInt(cursor.getColumnIndex(SERVICE_MONTH)));
				serviceInfo.setServiceCharge(cursor.getInt(cursor.getColumnIndex(SERVICE_CHARGE)));
				serviceInfo.setServiceDesc(cursor.getString(cursor.getColumnIndex(SERVICE_DESC)));
				serviceInfo.setServiceStatus(cursor.getInt(cursor.getColumnIndex(SERVICE_STATUS)));
				serviceInfo.setServiceIcon(cursor.getString(cursor.getColumnIndex(SERVICE_ICON)));
				serviceInfoList.add(serviceInfo);
			}
		} 
		
		if (cursor != null) 
		{
			cursor.close();
			cursor = null;
		}
		CldLog.d(TAG, " queryServiceInfos length: " + serviceInfoList.size());
		return serviceInfoList;
	}
	
	
	/**
	 * 删除packageCode对应的KCloudServiceInfo
	 */
	public static void deleteServiceInfos(int packageCode)
	{
		if (DatabaseManager.mDbHelper == null) 
			return ;
	
		String where = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		where = PACKAGE_CODE + "=\"" + packageCode + "\" ";
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		db.delete(KCLOUDSERVICE_TABLE, where, null);
		CldLog.d(TAG, " deleteServiceInfos ");
	}	
	
	/**
	 * 删除所有KCloudServiceInfo
	 */
	public static void deleteServiceInfos()
	{
		if (DatabaseManager.mDbHelper == null) 
			return;
	
		String where = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		db.delete(KCLOUDSERVICE_TABLE, where, null);
		CldLog.d(TAG, " deleteServiceInfos ");
	}	
}
