package com.download.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseManager 
{
	private static DownloadDBOpenHelper mHelper;
	private static SQLiteDatabase mDatabase;
	
	@SuppressLint("NewApi") 
	public static SQLiteDatabase getWritableDatabase(Context context) 
	{
		if (mHelper == null)
		{
			mHelper = new DownloadDBOpenHelper(context);
		}
		
		if (mDatabase == null) 
		{
			mDatabase = mHelper.getWritableDatabase();
			//enable it only once
			mDatabase.enableWriteAheadLogging();
		}
		
		return mDatabase;
	}
	
	public static void closeDatabase() 
	{
		if (mDatabase != null) 
		{
			mDatabase.close();
			mDatabase = null;
		}
	}
}
