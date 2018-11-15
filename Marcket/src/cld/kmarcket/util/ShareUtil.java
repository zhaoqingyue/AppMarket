package cld.kmarcket.util;

import java.util.Map;
import cld.kmarcket.KMarcketApplication;
import android.content.Context;
import android.content.SharedPreferences;

public class ShareUtil 
{
	private static final String TARGET_SHARE_NAME = "cld.kmarcket";
	private static SharedPreferences.Editor mEditor = null;
	private static SharedPreferences mSharedPreferences = null;
	
	public static void init()
	{
		Context ctx = KMarcketApplication.getContext();
		mSharedPreferences = ctx.getSharedPreferences(TARGET_SHARE_NAME, 
				Context.MODE_PRIVATE);
		mEditor = mSharedPreferences.edit();
	}
	
	/**
	 * 防止调用put或get时，没有初始化
	 */
	private static void checkShared()
	{
		if (mSharedPreferences == null || mEditor == null)
		{
			init();
		}
	}
	
	public static void put(String key, boolean value) 
	{
		checkShared();
		mEditor.putBoolean(key, value);
		mEditor.commit();
	}

	public static void put(String key, int value) 
	{
		checkShared();
		mEditor.putInt(key, value);
		mEditor.commit();
	}

	public static void put(String key, float value) 
	{
		checkShared();
		mEditor.putFloat(key, value);
		mEditor.commit();
	}

	public static void put(String key, long value) 
	{
		checkShared();
		mEditor.putLong(key, value);
		mEditor.commit();
	}

	public static void put(String key, String value) 
	{
		checkShared();
		mEditor.putString(key, value);
		mEditor.commit();
	}

	public static String getString(String key) 
	{
		checkShared();
		return mSharedPreferences.getString(key, "");
	}

	public static String getString(String key, String defaultValue) 
	{
		checkShared();
		return mSharedPreferences.getString(key, defaultValue);
	}

	public static boolean getBoolean(String key) 
	{
		checkShared();
		return mSharedPreferences.getBoolean(key, false);
	}

	public static boolean getBoolean(String key, boolean defaultValue) 
	{
		checkShared();
		return mSharedPreferences.getBoolean(key, defaultValue);
	}

	public static float getFloat(String key) 
	{
		checkShared();
		return mSharedPreferences.getFloat(key, 0.0F);
	}

	public static long getLong(String key) 
	{
		checkShared();
		return mSharedPreferences.getLong(key, 0L);
	}

	public static long getLong(String key, long defaultValue) 
	{
		checkShared();
		return mSharedPreferences.getLong(key, defaultValue);
	}

	public static int getInt(String key) 
	{
		checkShared();
		return mSharedPreferences.getInt(key, 0);
	}

	public static int getInt(String key, int defaultValue) 
	{
		checkShared();
		return mSharedPreferences.getInt(key, defaultValue);
	}

	public static Map<String, ?> getAll() 
	{
		checkShared();
		return mSharedPreferences.getAll();
	}

	public static boolean isContains(String key) 
	{
		checkShared();
		return mSharedPreferences.contains(key);
	}

	public static void remove(String key) 
	{
		checkShared();
		mEditor.remove(key);
		mEditor.commit();
	}

	public static void clear() 
	{
		checkShared();
		mEditor.clear().commit();
	}
}
