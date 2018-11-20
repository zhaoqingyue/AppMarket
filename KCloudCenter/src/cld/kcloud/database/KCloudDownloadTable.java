/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: KCloudDownloadTable.java
 * @Prject: KCloudCenter_M550
 * @Package: cld.kcloud.database
 * @Description: 下载进度表
 * @author: zhaoqy
 * @date: 2016年8月3日 下午3:13:42
 * @version: V1.0
 */

package cld.kcloud.database;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import cld.kcloud.custom.bean.KCloudDownloadInfo;
import com.cld.log.CldLog;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class KCloudDownloadTable 
{
	private static final String TAG = "KCloudDownloadTable";
	public static final String AUTHORITY = "cld.kcloud.database.DatabaseManager";
	public static final String KCLOUD_DOWNLOAD_TABLE = "kcloud_download_table";                                             
	public static final Uri CONTENT_SORT_URI = Uri.parse("content://" + AUTHORITY + "/" + KCLOUD_DOWNLOAD_TABLE); 
	public static final String ID = "_id";    	
	public static final String DOWNLOAD_PATH = "download_path";     
	public static final String DOWNLOAD_THREADID = "download_threadid"; 
	public static final String DOWNLOAD_START = "download_start";   
	public static final String DOWNLOAD_END = "download_end";   
	public static final String DOWNLOAD_DONE = "download_done";     
	
	private static KCloudDownloadTable mInstance = null;
	public static KCloudDownloadTable getInstance() 
	{
		if (mInstance == null) 
		{
			synchronized (KCloudDownloadTable.class) 
			{
				if (mInstance == null) 
				{
					mInstance = new KCloudDownloadTable();
				}
			}
		}
		return mInstance;
	}
	
	public String getCreateSql() 
	{
		StringBuffer sb = new StringBuffer();	
		sb.append("CREATE TABLE  IF NOT EXISTS ");
		sb.append(KCLOUD_DOWNLOAD_TABLE);
		sb.append("(");
		sb.append(ID);
		sb.append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
		sb.append(DOWNLOAD_PATH);
		sb.append(" TEXT,");
		sb.append(DOWNLOAD_THREADID);
		sb.append(" Integer,");
		sb.append(DOWNLOAD_START);
		sb.append(" Integer,");
		sb.append(DOWNLOAD_END);
		sb.append(" Integer,");
		sb.append(DOWNLOAD_DONE);
		sb.append(" Integer");
		sb.append(");");
		
		return sb.toString();
	}  
	
	public String getUpgradeSql() 
	{
		String string = "DROP TABLE IF EXISTS " + KCLOUD_DOWNLOAD_TABLE;
		return string;
	}
	
	public void insertDownloadInfo(KCloudDownloadInfo downloadInfo)
	{
		if (DatabaseManager.mDbHelper == null) 
			return;
		
		if (downloadInfo == null)
			return;
	
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		
		ContentValues values = new ContentValues();
		values.put(DOWNLOAD_PATH, downloadInfo.getPath());
		values.put(DOWNLOAD_THREADID, downloadInfo.getThreadId());
		values.put(DOWNLOAD_START, downloadInfo.getStart());
		values.put(DOWNLOAD_END, downloadInfo.getEnd());
		values.put(DOWNLOAD_DONE, downloadInfo.getDone());
		db.insert(KCLOUD_DOWNLOAD_TABLE, null, values);
		CldLog.d(TAG, " insertDownloadInfo "  + downloadInfo.getPath());
	}
	
	public void updateDownloadInfo(KCloudDownloadInfo downloadInfo)
	{
		if (DatabaseManager.mDbHelper == null) 
			return;
		
		if (downloadInfo == null)
			return;
		
		String where = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		
		where = DOWNLOAD_PATH + "=\"" + downloadInfo.getPath() + "\" " + "AND" + " " + 
				DOWNLOAD_THREADID + "=" + downloadInfo.getThreadId();
		ContentValues values = new ContentValues();
		values.put(DOWNLOAD_DONE, downloadInfo.getDone());
		db.update(KCLOUD_DOWNLOAD_TABLE, values, where, null);
		//CldLog.d(TAG, " updateDownloadInfo " + downloadInfo.getPath() + ", " + downloadInfo.getDone());
	}
	
	public KCloudDownloadInfo queryDownloadInfos(String path, int thid)
	{
		if (DatabaseManager.mDbHelper == null) 
			return null;
		
		KCloudDownloadInfo downloadInfo = null;
		Cursor cursor = null;
		String where = null;
		String orderBy = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		
		where = DOWNLOAD_PATH + "=\"" + path + "\" " + "AND" + " " + DOWNLOAD_THREADID + "=" + thid;
		orderBy = ID + " ASC";
		cursor = db.query(KCLOUD_DOWNLOAD_TABLE, null, where, null, null, null, orderBy);
		if (cursor != null) 
		{
			cursor.setNotificationUri(DatabaseManager.mContext.getContentResolver(), CONTENT_SORT_URI);
			int n = cursor.getCount();
			if (n > 0)
			{
				cursor.moveToPosition(0);
				downloadInfo = new KCloudDownloadInfo();
				downloadInfo.setPath(cursor.getString(cursor.getColumnIndex(DOWNLOAD_PATH)));
				downloadInfo.setThreadId(cursor.getInt(cursor.getColumnIndex(DOWNLOAD_THREADID)));
				downloadInfo.setStart(cursor.getInt(cursor.getColumnIndex(DOWNLOAD_START)));
				downloadInfo.setEnd(cursor.getInt(cursor.getColumnIndex(DOWNLOAD_END)));
				downloadInfo.setDone(cursor.getInt(cursor.getColumnIndex(DOWNLOAD_DONE)));
			}
		} 
		
		if (cursor != null) 
		{
			cursor.close();
			cursor = null;
		}
		CldLog.d(TAG, " queryDownloadInfos " + path + ", " + thid);
		return downloadInfo;
	}
	
	public ArrayList<KCloudDownloadInfo> queryUndoDownloadInfos(String path)
	{
		if (DatabaseManager.mDbHelper == null) 
			return null;
		
		ArrayList<KCloudDownloadInfo> downloadInfoList = new ArrayList<KCloudDownloadInfo>();
		Cursor cursor = null;
		String where = null;
		String orderBy = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		
		where = DOWNLOAD_PATH + "=\"" + path + "\" ";
		orderBy = ID + " ASC";
		cursor = db.query(KCLOUD_DOWNLOAD_TABLE, null, where, null, null, null, orderBy);
		if (cursor != null) 
		{
			cursor.setNotificationUri(DatabaseManager.mContext.getContentResolver(), CONTENT_SORT_URI);
			int n = cursor.getCount();
			for (int i=0; i<n; i++) 
			{
				cursor.moveToPosition(i);
				KCloudDownloadInfo downloadInfo = new KCloudDownloadInfo();
				downloadInfo.setPath(cursor.getString(cursor.getColumnIndex(DOWNLOAD_PATH)));
				downloadInfo.setThreadId(cursor.getInt(cursor.getColumnIndex(DOWNLOAD_THREADID)));
				downloadInfo.setStart(cursor.getInt(cursor.getColumnIndex(DOWNLOAD_START)));
				downloadInfo.setEnd(cursor.getInt(cursor.getColumnIndex(DOWNLOAD_END)));
				downloadInfo.setDone(cursor.getInt(cursor.getColumnIndex(DOWNLOAD_DONE)));
				downloadInfoList.add(downloadInfo);
			}
		} 
		
		if (cursor != null) 
		{
			cursor.close();
			cursor = null;
		}
		CldLog.d(TAG, " queryDownloadInfos length: " + downloadInfoList.size());
		return downloadInfoList;
	}
	
	
	public List<String> queryUndoDownloadInfos()
	{
		if (DatabaseManager.mDbHelper == null) 
			return null;
		
		List<String> undoList = new ArrayList<String>();
		Cursor cursor = null;
		String where = null;
		String orderBy = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		
		orderBy = ID + " ASC";
		cursor = db.query(KCLOUD_DOWNLOAD_TABLE, null, where, null, null, null, orderBy);
		if (cursor != null) 
		{
			cursor.setNotificationUri(DatabaseManager.mContext.getContentResolver(), CONTENT_SORT_URI);
			int n = cursor.getCount();
			for (int i=0; i<n; i++) 
			{
				cursor.moveToPosition(i);
				String  downloadInfo = cursor.getString(cursor.getColumnIndex(DOWNLOAD_PATH));
				undoList.add(downloadInfo);
			}
			undoList = removeDuplicateWithOrder(undoList);
		} 
		
		if (cursor != null) 
		{
			cursor.close();
			cursor = null;
		}
		CldLog.d(TAG, " queryDownloadInfos length: " + undoList.size());
		return undoList;
	}
	
	/**
	 * @Title: removeDuplicateWithOrder
	 * @Description: 删除ArrayList中重复元素，保持顺序 
	 * @param undoList
	 * @return: void
	 */
	public List<String> removeDuplicateWithOrder(List<String> undoList) 
	{
		Set<String> set = new HashSet<String>();
		List<String> newList = new ArrayList<String>();
		for (Iterator<String> iter = undoList.iterator(); iter.hasNext();) 
		{
			String element = iter.next();
			if (set.add(element))
				newList.add(element);
		}
		undoList.clear();
		undoList.addAll(newList);
		CldLog.d(TAG, " remove duplicate " + undoList);
		return undoList;
	}
	
	/**
	 * 删除path和thid对应的KCloudDownloadInfo
	 */
	@SuppressLint("NewApi") 
	public void deleteDownloadInfo(String path, int thid)
	{
		if (DatabaseManager.mDbHelper == null) 
			return ;
		
		if (path == null || path.isEmpty())
			return ;
	
		String where = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		
		where = DOWNLOAD_PATH + "=\"" + path + "\" " + "AND" + " " + DOWNLOAD_THREADID + "=" + thid;
		db.delete(KCLOUD_DOWNLOAD_TABLE, where, null);
		CldLog.d(TAG, " deleteDownloadInfo " + path + ", " + thid);
	}	
	
	@SuppressLint("NewApi") 
	public void deleteDownloadInfo(String path)
	{
		if (DatabaseManager.mDbHelper == null) 
			return ;
		
		if (path == null || path.isEmpty())
			return ;
	
		String where = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		
		where = DOWNLOAD_PATH + "=\"" + path + "\" ";
		db.delete(KCLOUD_DOWNLOAD_TABLE, where, null);
		CldLog.d(TAG, " deleteDownloadInfo " + path);
	}	
	
	@SuppressLint("NewApi") 
	public void deleteDownloadInfo()
	{
		if (DatabaseManager.mDbHelper == null) 
			return ;
	
		String where = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		
		db.delete(KCLOUD_DOWNLOAD_TABLE, where, null);
		CldLog.d(TAG, " deleteDownloadInfo ");
	}	
}
