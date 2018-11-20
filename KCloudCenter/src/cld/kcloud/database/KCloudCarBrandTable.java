package cld.kcloud.database;

import java.util.ArrayList;
import cld.kcloud.fragment.CarSelectorFragment.CarBrand;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class KCloudCarBrandTable 
{
	public static final String AUTHORITY = "cld.kcloud.database.DatabaseManager";
	public static final String BRAND_TABLE = "brand_table";                                             
	public static final Uri CONTENT_SORT_URI = Uri.parse("content://" + AUTHORITY + "/" + BRAND_TABLE); 
	public static final String ID = "_id";    															
	public static final String BRAND_LETTER = "brand_letter";                                              
	public static final String BRAND_NAME = "brand_name";  
	
	public static String getCreateSql() 
	{
		StringBuffer sb = new StringBuffer();	
		sb.append("CREATE TABLE  IF NOT EXISTS ");
		sb.append(BRAND_TABLE);
		sb.append("(");
		sb.append(ID);
		sb.append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
		sb.append(BRAND_LETTER);
		sb.append(" TEXT,");
		sb.append(BRAND_NAME);
		sb.append(" TEXT");
		sb.append(");");
		
		return sb.toString();
	}  
	
	public static String getUpgradeSql() 
	{
		String string = "DROP TABLE IF EXISTS " + BRAND_TABLE;
		return string;
	}
	
	public static void insertCarBrand(CarBrand carBrand)
	{
		if (DatabaseManager.mDbHelper == null) 
			return;
		
		if (carBrand == null) 
			return;
	
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		
		ContentValues values = new ContentValues();
		values.put(BRAND_LETTER, carBrand.firstStr);
		values.put(BRAND_NAME, carBrand.name);
		db.insert(BRAND_TABLE, null, values);
	}
	
	public static void insertCarBrand(ArrayList<CarBrand> carBrandList)
	{
		if (DatabaseManager.mDbHelper == null) 
			return;
		
		if (carBrandList == null || carBrandList.isEmpty())
			return;
		
		if (queryAllCarBrand() != null && queryAllCarBrand().size() > 0) 
			deleteCarBrand();
	
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		
		db.beginTransaction();
		for (int i=0; i<carBrandList.size(); i++) 
		{
			ContentValues values = new ContentValues();
			CarBrand carBrand = carBrandList.get(i);
			values.put(BRAND_LETTER, carBrand.firstStr);
			values.put(BRAND_NAME, carBrand.name);
			db.insert(BRAND_TABLE, null, values);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	public static ArrayList<CarBrand> queryAllCarBrand()
	{
		if (DatabaseManager.mDbHelper == null) 
			return null;
		
		ArrayList<CarBrand> carBrandList = new ArrayList<CarBrand>();
		Cursor cursor = null;
		String where = null;
		String orderBy = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		orderBy = ID + " ASC";
		cursor = db.query(BRAND_TABLE, null, where, null, null, null, orderBy);
		if (cursor != null) 
		{
			cursor.setNotificationUri(DatabaseManager.mContext.getContentResolver(), CONTENT_SORT_URI);
			int n = cursor.getCount();
			for (int i=0; i<n; i++) 
			{
				cursor.moveToPosition(i);
				CarBrand carBrand = new CarBrand();
				carBrand.firstStr = cursor.getString(cursor.getColumnIndex(BRAND_LETTER));
				carBrand.name = cursor.getString(cursor.getColumnIndex(BRAND_NAME));
				carBrandList.add(carBrand);
			}
		} 
		
		if (cursor != null) 
		{
			cursor.close();
			cursor = null;
		}
		return carBrandList;
	}
	
	public static void deleteCarBrand()
	{
		if (DatabaseManager.mDbHelper == null) 
			return;
	
		String where = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		db.delete(BRAND_TABLE, where, null);
	}	
}
