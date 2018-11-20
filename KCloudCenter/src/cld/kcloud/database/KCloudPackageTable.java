/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: KCloudPackageTable.java
 * @Prject: KCloudCenter_M550
 * @Package: cld.kcloud.database
 * @Description: 套餐信息表
 * @author: zhaoqy
 * @date: 2016年8月3日 上午9:55:58
 * @version: V1.0
 */

package cld.kcloud.database;

import java.util.ArrayList;
import com.cld.log.CldLog;
import cld.kcloud.custom.bean.KCloudPackageInfo;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class KCloudPackageTable 
{
	private static final String TAG = "KCloudPackageTable";
	public static final String AUTHORITY = "cld.kcloud.database.DatabaseManager";
	public static final String KCLOUDPACKAGE_TABLE = "kcloudpackage_table";                                             
	public static final Uri CONTENT_SORT_URI = Uri.parse("content://" + AUTHORITY + "/" + KCLOUDPACKAGE_TABLE); 
	public static final String ID = "_id";    	
	public static final String PACKAGE_CODE = "package_code";     
	public static final String PACKAGE_NAME = "package_name";  
	public static final String PACKAGE_ICON = "package_icon";  
	public static final String PACKAGE_DESC = "package_desc";  
	public static final String PACKAGE_CHARGES = "package_charges";
	public static final String PACKAGE_PAY_TIMES = "package_pay_times";  
	public static final String PACKAGE_FLOW = "package_flow";  
	public static final String PACKAGE_ENDTIME = "package_endtime";  
	public static final String PACKAGE_STATUS = "package_status";  
	public static final String PACKAGE_NUMBER = "package_number";  
	
	public static String getCreateSql() 
	{
		StringBuffer sb = new StringBuffer();	
		sb.append("CREATE TABLE  IF NOT EXISTS ");
		sb.append(KCLOUDPACKAGE_TABLE);
		sb.append("(");
		sb.append(ID);
		sb.append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
		sb.append(PACKAGE_CODE);
		sb.append(" Integer,");
		sb.append(PACKAGE_NAME);
		sb.append(" TEXT,");
		sb.append(PACKAGE_ICON);
		sb.append(" TEXT,");
		sb.append(PACKAGE_DESC);
		sb.append(" TEXT,");
		sb.append(PACKAGE_CHARGES);
		sb.append(" Integer,");
		sb.append(PACKAGE_PAY_TIMES);
		sb.append(" Integer,");
		sb.append(PACKAGE_FLOW);
		sb.append(" Integer,");
		sb.append(PACKAGE_ENDTIME);
		sb.append(" Integer,");
		sb.append(PACKAGE_STATUS);
		sb.append(" Integer,");
		sb.append(PACKAGE_NUMBER);
		sb.append(" Integer");
		sb.append(");");
		
		return sb.toString();
	}  
	
	public static String getUpgradeSql() 
	{
		String string = "DROP TABLE IF EXISTS " + KCLOUDPACKAGE_TABLE;
		return string;
	}
	
	public static void insertPackageInfo(KCloudPackageInfo packageInfo)
	{
		if (DatabaseManager.mDbHelper == null) 
			return;
		
		if (packageInfo == null)
			return;
	
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		
		ContentValues values = new ContentValues();
		values.put(PACKAGE_CODE, packageInfo.getComboCode());
		values.put(PACKAGE_NAME, packageInfo.getComboName());
		values.put(PACKAGE_ICON, packageInfo.getComboIcon());
		values.put(PACKAGE_DESC, packageInfo.getComboDesc());
		values.put(PACKAGE_CHARGES, packageInfo.getCharges());
		values.put(PACKAGE_PAY_TIMES, packageInfo.getPayTimes());
		values.put(PACKAGE_FLOW, packageInfo.getFlow());
		values.put(PACKAGE_ENDTIME, packageInfo.getEndtime());
		values.put(PACKAGE_STATUS, packageInfo.getStatus());
		values.put(PACKAGE_NUMBER, packageInfo.getNumber());
		db.insert(KCLOUDPACKAGE_TABLE, null, values);
		CldLog.d(TAG, " insertPackageInfo ");
	}
	
	public static void insertPackageInfos(ArrayList<KCloudPackageInfo> packageInfoList)
	{
		if (DatabaseManager.mDbHelper == null) 
			return;
		
		if (packageInfoList == null || packageInfoList.isEmpty())
			return;
	
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		
		db.beginTransaction();
		for (int i=0; i<packageInfoList.size(); i++) 
		{
			ContentValues values = new ContentValues();
			KCloudPackageInfo packageInfo = packageInfoList.get(i);
			values.put(PACKAGE_CODE, packageInfo.getComboCode());
			values.put(PACKAGE_NAME, packageInfo.getComboName());
			values.put(PACKAGE_ICON, packageInfo.getComboIcon());
			values.put(PACKAGE_DESC, packageInfo.getComboDesc());
			values.put(PACKAGE_CHARGES, packageInfo.getCharges());
			values.put(PACKAGE_PAY_TIMES, packageInfo.getPayTimes());
			values.put(PACKAGE_FLOW, packageInfo.getFlow());
			values.put(PACKAGE_ENDTIME, packageInfo.getEndtime());
			values.put(PACKAGE_STATUS, packageInfo.getStatus());
			values.put(PACKAGE_NUMBER, packageInfo.getNumber());
			db.insert(KCLOUDPACKAGE_TABLE, null, values);
			CldLog.d(TAG, PACKAGE_NAME + ": " + packageInfo.getComboName());
			CldLog.d(TAG, PACKAGE_STATUS + ": " + packageInfo.getStatus());
			//CldLog.d(TAG, PACKAGE_NUMBER + ": " + packageInfo.getNumber());
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		CldLog.d(TAG, " insertPackageInfos length: " + packageInfoList.size());
	}
	
	/**
	 * 查询packageCode对应的KCloudPackageInfo
	 */
	public static KCloudPackageInfo queryPackageInfo(int packageCode)
	{
		if (DatabaseManager.mDbHelper == null) 
			return null;
		
		KCloudPackageInfo packageInfo = null;
		Cursor cursor = null;
		String where = null;
		String orderBy = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		where = PACKAGE_CODE + "=" + packageCode;
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		orderBy = ID + " ASC";
		cursor = db.query(KCLOUDPACKAGE_TABLE, null, where, null, null, null, orderBy);
		if (cursor != null) 
		{
			cursor.setNotificationUri(DatabaseManager.mContext.getContentResolver(), CONTENT_SORT_URI);
			int n = cursor.getCount();
			if (n > 0)
			{
				cursor.moveToPosition(0);
				packageInfo = new KCloudPackageInfo();
				packageInfo.setComboCode(cursor.getInt(cursor.getColumnIndex(PACKAGE_CODE)));
				packageInfo.setComboName(cursor.getString(cursor.getColumnIndex(PACKAGE_NAME)));
				packageInfo.setComboIcon(cursor.getString(cursor.getColumnIndex(PACKAGE_ICON)));
				packageInfo.setComboDesc(cursor.getString(cursor.getColumnIndex(PACKAGE_DESC)));
				packageInfo.setCharges(cursor.getInt(cursor.getColumnIndex(PACKAGE_CHARGES)));
				packageInfo.setPayTimes(cursor.getInt(cursor.getColumnIndex(PACKAGE_PAY_TIMES)));
				packageInfo.setFlow(cursor.getInt(cursor.getColumnIndex(PACKAGE_FLOW)));
				packageInfo.setEndtime(cursor.getInt(cursor.getColumnIndex(PACKAGE_ENDTIME)));
				packageInfo.setStatus(cursor.getInt(cursor.getColumnIndex(PACKAGE_STATUS)));
				packageInfo.setNumber(cursor.getInt(cursor.getColumnIndex(PACKAGE_NUMBER)));
			}
		} 
		
		if (cursor != null) 
		{
			cursor.close();
			cursor = null;
		}
		CldLog.d(TAG, " queryPackageInfo ");
		return packageInfo;
	}
	
	/**
	 * 
	 * 查询所有的KCloudPackageInfo
	 */
	public static ArrayList<KCloudPackageInfo> queryPackageInfos()
	{
		if (DatabaseManager.mDbHelper == null) 
			return null;
		
		ArrayList<KCloudPackageInfo> packageInfoList = new ArrayList<KCloudPackageInfo>();
		Cursor cursor = null;
		String where = null;
		String orderBy = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		orderBy = ID + " ASC";
		cursor = db.query(KCLOUDPACKAGE_TABLE, null, where, null, null, null, orderBy);
		if (cursor != null) 
		{
			cursor.setNotificationUri(DatabaseManager.mContext.getContentResolver(), CONTENT_SORT_URI);
			int n = cursor.getCount();
			for (int i=0; i<n; i++) 
			{
				cursor.moveToPosition(i);
				KCloudPackageInfo packageInfo = new KCloudPackageInfo();
				packageInfo.setComboCode(cursor.getInt(cursor.getColumnIndex(PACKAGE_CODE)));
				packageInfo.setComboName(cursor.getString(cursor.getColumnIndex(PACKAGE_NAME)));
				packageInfo.setComboIcon(cursor.getString(cursor.getColumnIndex(PACKAGE_ICON)));
				packageInfo.setComboDesc(cursor.getString(cursor.getColumnIndex(PACKAGE_DESC)));
				packageInfo.setCharges(cursor.getInt(cursor.getColumnIndex(PACKAGE_CHARGES)));
				packageInfo.setPayTimes(cursor.getInt(cursor.getColumnIndex(PACKAGE_PAY_TIMES)));
				packageInfo.setFlow(cursor.getInt(cursor.getColumnIndex(PACKAGE_FLOW)));
				packageInfo.setEndtime(cursor.getInt(cursor.getColumnIndex(PACKAGE_ENDTIME)));
				packageInfo.setStatus(cursor.getInt(cursor.getColumnIndex(PACKAGE_STATUS)));
				packageInfo.setNumber(cursor.getInt(cursor.getColumnIndex(PACKAGE_NUMBER)));
				packageInfoList.add(packageInfo);
				CldLog.d(TAG, PACKAGE_NAME + ": " + packageInfo.getComboName());
				CldLog.d(TAG, PACKAGE_NUMBER + ": " + packageInfo.getNumber());
			}
		} 
		
		if (cursor != null) 
		{
			cursor.close();
			cursor = null;
		}
		CldLog.d(TAG, " queryPackageInfos length: " + packageInfoList.size());
		return packageInfoList;
	}
	
	/**
	 * 删除packageCode对应的KCloudPackageInfo
	 */
	public static void deletePackageInfos(int packageCode)
	{
		if (DatabaseManager.mDbHelper == null) 
			return ;
	
		String where = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		where = PACKAGE_CODE + "=" + packageCode;
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		db.delete(KCLOUDPACKAGE_TABLE, where, null);
		CldLog.d(TAG, " deletePackageInfos ");
	}	
	
	/**
	 * 删除所有KCloudPackageInfo
	 */
	public static void deletePackageInfos()
	{
		if (DatabaseManager.mDbHelper == null) 
			return;
	
		String where = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		db.delete(KCLOUDPACKAGE_TABLE, where, null);
		CldLog.d(TAG, " deletePackageInfos ");
	}	
}
