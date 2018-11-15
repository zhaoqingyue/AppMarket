package com.download.api;

import cld.kmarcket.util.LogUtil;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DownloadDBOpenHelper extends SQLiteOpenHelper 
{
	public DownloadDBOpenHelper(Context context) 
	{
		//先前版本是1
		super(context, "download.db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		db.execSQL(getInfoSql());
		db.execSQL(getStatusSql());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
	}
	
	@SuppressLint("NewApi") 
	@Override
	public void onConfigure(SQLiteDatabase db) 
	{
		setWriteAheadLoggingEnabled(true);
		super.onConfigure(db);
	}
	
	private String getInfoSql() 
	{
		StringBuffer sb = new StringBuffer();	
		sb.append("CREATE TABLE ");
		sb.append("info");
		sb.append("(");
		sb.append("path ");
		sb.append("VARCHAR(1024), ");
		sb.append("thid ");
		sb.append("INTEGER, ");
		sb.append("start ");
		sb.append("LONG, ");
		sb.append("end ");
		sb.append("LONG, ");
		sb.append("done ");
		sb.append("LONG, ");
		sb.append("PRIMARY KEY(");
		sb.append("path, thid");
		sb.append(")");
		sb.append(")");
		
		LogUtil.i(LogUtil.TAG, "info sql: " + sb);
		return sb.toString();
	}
	
	private String getStatusSql() 
	{
		StringBuffer sb = new StringBuffer();	
		sb.append("CREATE TABLE ");
		sb.append("status");
		sb.append("(");
		sb.append("packagename ");
		sb.append("VARCHAR(1024), ");
		sb.append("type ");
		sb.append("INTEGER, ");
		sb.append("PRIMARY KEY(");
		sb.append("packagename");
		sb.append(")");
		sb.append(")");
		
		LogUtil.i(LogUtil.TAG, "status sql: " + sb);
		return sb.toString();
	}
}