package cld.kcloud.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class DatabaseManager extends ContentProvider
{
	public static final int DATABASE_VERSION = 1;                 //数据库版本 修改数据库要修改此代码
	public static final String DATABASE_NAME = "kcloudcenter.db"; //数据库文件
	public static SQLiteDatabase mSqlDB;     //全局数据库
	public static DatabaseHelper mDbHelper;  //数据库帮助器
	public static Context        mContext;   //应用上下文
	
	@Override
	public boolean onCreate() 
	{
		if (mDbHelper == null) 
		{
			mDbHelper = new DatabaseHelper(getContext());
			mContext = getContext();
		}
		return (mDbHelper == null) ? false : true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) 
	{
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		qb.setTables(uri.getPathSegments().get(0));

		Cursor c = qb.query(db, projection, selection, null, null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public String getType(Uri uri) 
	{
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) 
	{
		return null;
	}

	@Override
	public int delete(Uri uri, String s, String[] as) 
	{
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		return db.delete(uri.getPathSegments().get(0), s, as);
	}

	@Override
	public int update(Uri uri, ContentValues values, String s, String[] as) 
	{
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		return db.update(uri.getPathSegments().get(0), values, s, as);
	}
	
	public static class DatabaseHelper extends SQLiteOpenHelper 
	{
		DatabaseHelper(Context context) 
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) 
		{
			db.execSQL(KCloudCarBrandTable.getCreateSql());
			db.execSQL(KCloudCarModelTable.getCreateSql());
			db.execSQL(KCloudCarSeriesTable.getCreateSql());
			db.execSQL(KCloudPackageTable.getCreateSql());
			db.execSQL(KCloudServiceTable.getCreateSql());
			db.execSQL(KCloudAppTable.getCreateSql());
			
			db.execSQL(KCloudDownloadTable.getInstance().getCreateSql());
			db.execSQL(KCloudInstalledTable.getInstance().getCreateSql());
			db.execSQL(KCloudInstallingTable.getInstance().getCreateSql());
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
		{
			db.execSQL(KCloudCarBrandTable.getUpgradeSql());
			db.execSQL(KCloudCarBrandTable.getUpgradeSql());
			db.execSQL(KCloudCarBrandTable.getUpgradeSql());
			db.execSQL(KCloudPackageTable.getUpgradeSql());
			db.execSQL(KCloudServiceTable.getUpgradeSql());
			db.execSQL(KCloudAppTable.getUpgradeSql());
			
			db.execSQL(KCloudDownloadTable.getInstance().getUpgradeSql());
			db.execSQL(KCloudInstalledTable.getInstance().getUpgradeSql());
			db.execSQL(KCloudInstallingTable.getInstance().getUpgradeSql());
			onCreate(db);
		}
	}
}
