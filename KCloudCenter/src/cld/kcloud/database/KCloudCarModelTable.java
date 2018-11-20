package cld.kcloud.database;

import java.util.ArrayList;
import cld.kcloud.fragment.CarSelectorFragment.CarModel;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class KCloudCarModelTable 
{
	public static final String AUTHORITY = "cld.kcloud.database.DatabaseManager";
	public static final String MODEL_TABLE = "model_table";                                             
	public static final Uri CONTENT_SORT_URI = Uri.parse("content://" + AUTHORITY + "/" + MODEL_TABLE); 
	public static final String ID = "_id";  
	public static final String BRAND_NAME = "brand_name";  
	public static final String MODEL_NAME = "model_name";       
	
	public static String getCreateSql() 
	{
		StringBuffer sb = new StringBuffer();	
		sb.append("CREATE TABLE  IF NOT EXISTS ");
		sb.append(MODEL_TABLE);
		sb.append("(");
		sb.append(ID);
		sb.append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
		sb.append(BRAND_NAME);
		sb.append(" TEXT,");
		sb.append(MODEL_NAME);
		sb.append(" TEXT");
		sb.append(");");
		
		return sb.toString();
	}  
	
	public static String getUpgradeSql() 
	{
		String string = "DROP TABLE IF EXISTS " + MODEL_TABLE;
		return string;
	}
	
	public static void insertCarModel(CarModel carModel)
	{
		if (DatabaseManager.mDbHelper == null) 
			return;
		
		if (carModel == null)
			return;
	
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		
		ContentValues values = new ContentValues();
		values.put(BRAND_NAME, carModel.brand);
		values.put(MODEL_NAME, carModel.name);
		db.insert(MODEL_TABLE, null, values);
	}
	
	public static void insertCarModel(ArrayList<CarModel> carModelList)
	{
		if (DatabaseManager.mDbHelper == null) 
			return;
		
		if (carModelList == null || carModelList.isEmpty())
			return;
		
		if (queryAllCarModel() != null && queryAllCarModel().size() > 0) 
			deleteCarModel();
	
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		
		db.beginTransaction();
		for (int i=0; i<carModelList.size(); i++) 
		{
			ContentValues values = new ContentValues();
			CarModel carModel = carModelList.get(i);
			values.put(BRAND_NAME, carModel.brand);
			values.put(MODEL_NAME, carModel.name);
			db.insert(MODEL_TABLE, null, values);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	public static ArrayList<CarModel> queryAllCarModel()
	{
		if (DatabaseManager.mDbHelper == null) 
			return null;
		
		ArrayList<CarModel> carModelList = new ArrayList<CarModel>();
		Cursor cursor = null;
		String where = null;
		String orderBy = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		orderBy = ID + " ASC";
		cursor = db.query(MODEL_TABLE, null, where, null, null, null, orderBy);
		if (cursor != null) 
		{
			cursor.setNotificationUri(DatabaseManager.mContext.getContentResolver(), CONTENT_SORT_URI);
			int n = cursor.getCount();
			for (int i=0; i<n; i++) 
			{
				cursor.moveToPosition(i);
				CarModel carModel = new CarModel();
				carModel.brand = cursor.getString(cursor.getColumnIndex(BRAND_NAME));
				carModel.name = cursor.getString(cursor.getColumnIndex(MODEL_NAME));
				carModelList.add(carModel);
			}
		} 
		
		if (cursor != null) 
		{
			cursor.close();
			cursor = null;
		}
		return carModelList;
	}
	
	public static void deleteCarModel()
	{
		if (DatabaseManager.mDbHelper == null) 
			return;
	
		String where = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = DatabaseManager.mDbHelper.getReadableDatabase();
		qb.setTables(CONTENT_SORT_URI.getPathSegments().get(0));
		db.delete(MODEL_TABLE, where, null);
	}	
}
