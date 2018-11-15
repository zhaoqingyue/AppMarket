package cld.kmarcket.packages;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import cld.kmarcket.appinfo.AppInfo;

public class PackagesDBOpenHelper extends SQLiteOpenHelper 
{
	private SQLiteDatabase mDefaultWritableDatabase = null;

	public PackagesDBOpenHelper(Context context) 
	{
		super(context, "packages.db", null, 1);
	}

	@Override
	public SQLiteDatabase getWritableDatabase() 
	{
		final SQLiteDatabase db;
		if (mDefaultWritableDatabase != null) 
		{
			db = mDefaultWritableDatabase;
		} 
		else 
		{
			db = super.getWritableDatabase();
		}
		return db;
	}

	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		this.mDefaultWritableDatabase = db;
		db.execSQL(PackageTable.getCreateSql());
		addDefaultList();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		this.mDefaultWritableDatabase = db;
		addDefaultList();
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		this.mDefaultWritableDatabase = db;
	}
	
	@SuppressLint("NewApi") 
	@Override
	public void onConfigure(SQLiteDatabase db) 
	{
		setWriteAheadLoggingEnabled(true);
		super.onConfigure(db);
	}

	public void addDefaultList() 
	{
		SQLiteDatabase database = PackagesDBOpenHelper.this
				.getWritableDatabase();
		ArrayList<AppInfo> packages = DefaultPackages.getDefaultPackages();
		database.beginTransaction();
		ContentValues values = new ContentValues();
		//LogUtil.i(LogUtil.TAG, "query size:" + packages.size());
		
		for (int i=0; i<packages.size(); i++) 
		{
			values.put(PackageTable.PACKAGE_NAME, packages.get(i).getPkgName());
			values.put(PackageTable.APP_WIDGET, packages.get(i).getIsWidget());
			values.put(PackageTable.VERSION_CODE, packages.get(i).getVerCode());
			values.put(PackageTable.APP_DESC, packages.get(i).getAppDesc());
			database.insert(PackageTable.TABLE_NAME,
					/*PackageProvider.TABLE_COLUMN_NAME*/null, values);
		}
		database.setTransactionSuccessful();
		database.endTransaction();
	}
}
