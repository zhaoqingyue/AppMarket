package cld.kmarcket.packages;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class PackageProvider extends ContentProvider 
{
	public final static String TABLE_COLUMN_ID = "_id";
	
	private final static String AUTHORITY = "cld.kmarcket.providers.packages_provider";
	private final static String PACKAGES_PATH = "packages";
	private final static String PACKAGE_PATH = "packages/#";

	private final static int PACKAGES = 1;
	private final static int PACKAGE = 2;

	private final static UriMatcher sMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		// UriMatcher类的一个方法
		sMatcher.addURI(AUTHORITY, PACKAGES_PATH, PACKAGES);
		sMatcher.addURI(AUTHORITY, PACKAGE_PATH, PACKAGE);
	}

	private PackagesDBOpenHelper mHelper = null;

	@Override
	public boolean onCreate() {
		mHelper = new PackagesDBOpenHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase database = mHelper.getWritableDatabase();
		switch (sMatcher.match(uri)) {
		case PACKAGES:
			return database.query(PackageTable.TABLE_NAME, projection, selection,
					selectionArgs, null, null, sortOrder);
		default:
			// 均不匹配的时候，抛出异常
			throw new IllegalArgumentException("Unknown Uri : " + uri);
		}
	}

	@Override
	public String getType(Uri uri) {
		switch (sMatcher.match(uri)) {
		case PACKAGES:
			// 集合类型，返回值前面一般是固定的，后面的值是自己添加的，也可以加上包路径
			return "vnd.android.cursor.dir/" + PackageTable.TABLE_NAME;
		case PACKAGE:
			// 非集合类型数据，返回值前面一般是固定的，后面的值是自己添加的，也可以加上包路径
			return "vnd.android.cursor.item/" + PackageTable.TABLE_NAME;
		default:
			throw new IllegalArgumentException("Unknown Uri : " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase database = mHelper.getWritableDatabase();
		// 由于此时的values中具体是否含有数据是不确定的，所以此时需要在第二个参数中添加person表中的非主键的一列
		long id = database.insert(PackageTable.TABLE_NAME, /*PACKAGE_NAME*/null, values);
		return ContentUris.withAppendedId(uri, id);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase database = mHelper.getWritableDatabase();
		switch (sMatcher.match(uri)) {
		case PACKAGES:
			return database.delete(PackageTable.TABLE_NAME, selection, selectionArgs);
		default:
			throw new IllegalArgumentException("Unknown Uri : " + uri);
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}
}
