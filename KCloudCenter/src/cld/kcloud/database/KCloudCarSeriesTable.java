package cld.kcloud.database;

import java.util.ArrayList;
import cld.kcloud.fragment.CarSelectorFragment.CarSeries;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class KCloudCarSeriesTable 
{
	public static final String AUTHORITY = "cld.kcloud.database.DatabaseManager";
	public static final String SERIES_TABLE = "series_table";                                             
	public static final Uri CONTENT_SORT_URI = Uri.parse("content://" + AUTHORITY + "/" + SERIES_TABLE); 
	public static final String ID = "_id";  
	public static final String MODEL_NAME = "model_name";
	public static final String SERIES_NAME = "series_name";  
	
	public static String getCreateSql() 
	{
		StringBuffer sb = new StringBuffer();	
		sb.append("CREATE TABLE  IF NOT EXISTS ");
		sb.append(SERIES_TABLE);
		sb.append("(");
		sb.append(ID);
		sb.append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
		sb.append(MODEL_NAME);
		sb.append(" TEXT,");
		sb.append(SERIES_NAME);
		sb.append(" TEXT");
		sb.append(");");
		
		return sb.toString();
	}  
	
	public static String getUpgradeSql() 
	{
		String string = "DROP TABLE IF EXISTS " + SERIES_TABLE;
		return string;
	}
	
	public static void insertCarSeries(CarSeries carSeries)
	{
		if (DatabaseManager.mDbHelper == null) 
			return;
		
		if (carSeries == null)
			return;
	
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		
		ContentValues values = new ContentValues();
		values.put(MODEL_NAME, carSeries.model);
		values.put(SERIES_NAME, carSeries.name);
		db.insert(SERIES_TABLE, null, values);
	}
	
	public static void insertCarSeries(ArrayList<CarSeries> carSeriesList)
	{
		if (DatabaseManager.mDbHelper == null) 
			return;
		
		if (carSeriesList == null || carSeriesList.isEmpty())
			return;
		
		if (queryAllCarSeries() != null && queryAllCarSeries().size() > 0) 
			deleteCarSeries();
	
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		
		db.beginTransaction();
		for (int i=0; i<carSeriesList.size(); i++) 
		{
			CarSeries carSeries = carSeriesList.get(i);
			ContentValues values = new ContentValues();
			values.put(MODEL_NAME, carSeries.model);
			values.put(SERIES_NAME, carSeries.name);
			db.insert(SERIES_TABLE, null, values);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	public static ArrayList<CarSeries> queryAllCarSeries()
	{
		if (DatabaseManager.mDbHelper == null) 
			return null;
		
		ArrayList<CarSeries> carSeriesList = new ArrayList<CarSeries>();
		Cursor cursor = null;
		String where = null;
		String orderBy = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		orderBy = ID + " ASC";
		cursor = db.query(SERIES_TABLE, null, where, null, null, null, orderBy);
		if (cursor != null) 
		{
			cursor.setNotificationUri(DatabaseManager.mContext.getContentResolver(), CONTENT_SORT_URI);
			int n = cursor.getCount();
			for (int i=0; i<n; i++) 
			{
				cursor.moveToPosition(i);
				CarSeries carSeries = new CarSeries();
				carSeries.model = cursor.getString(cursor.getColumnIndex(MODEL_NAME));
				carSeries.name = cursor.getString(cursor.getColumnIndex(SERIES_NAME));
				carSeriesList.add(carSeries);
			}
		} 
		
		if (cursor != null) 
		{
			cursor.close();
			cursor = null;
		}
		return carSeriesList;
	}
	
	public static void deleteCarSeries()
	{
		if (DatabaseManager.mDbHelper == null) 
			return;
	
		String where = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		db.delete(SERIES_TABLE, where, null);
	}	
}
