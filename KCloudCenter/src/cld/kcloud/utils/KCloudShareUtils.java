package cld.kcloud.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import cld.kcloud.center.KCloudCtx;
import java.util.Map;

public class KCloudShareUtils {
	private static final String TARGET_SHARE_NAME = "cld.kcloud.user";
	private static SharedPreferences.Editor mEditor = null;
	private static SharedPreferences mSharedPreferences = null;

	@SuppressLint("WorldReadableFiles") 
	@TargetApi(Build.VERSION_CODES.HONEYCOMB) 
	@SuppressWarnings("deprecation")
	public static void init() {
		Context ctx = KCloudCtx.getAppContext();
		mSharedPreferences = ctx.getSharedPreferences(TARGET_SHARE_NAME,
				Context.MODE_WORLD_READABLE | Context.MODE_MULTI_PROCESS);
		mEditor = mSharedPreferences.edit();
	}

	public static void put(String key, boolean value) {
		mEditor.putBoolean(key, value);
		mEditor.commit();
	}

	public static void put(String key, int value) {
		mEditor.putInt(key, value);
		mEditor.commit();
	}

	public static void put(String key, float value) {
		mEditor.putFloat(key, value);
		mEditor.commit();
	}

	public static void put(String key, long value) {
		mEditor.putLong(key, value);
		mEditor.commit();
	}

	public static void put(String key, String value) {
		mEditor.putString(key, value);
		mEditor.commit();
	}

	public static String getString(String key) {
		return mSharedPreferences.getString(key, "");
	}

	public static String getString(String key, String defaultValue) {
		return mSharedPreferences.getString(key, defaultValue);
	}

	public static boolean getBoolean(String key) {
		return mSharedPreferences.getBoolean(key, false);
	}

	public static boolean getBoolean(String key, boolean defaultValue) {
		return mSharedPreferences.getBoolean(key, defaultValue);
	}

	public static float getFloat(String key) {
		return mSharedPreferences.getFloat(key, 0.0F);
	}

	public static long getLong(String key) {
		return mSharedPreferences.getLong(key, 0L);
	}

	public static long getLong(String key, long defaultValue) {
		return mSharedPreferences.getLong(key, defaultValue);
	}

	public static int getInt(String key) {
		return mSharedPreferences.getInt(key, 0);
	}

	public static int getInt(String key, int defaultValue) {
		return mSharedPreferences.getInt(key, defaultValue);
	}

	public static Map<String, ?> getAll() {
		return mSharedPreferences.getAll();
	}

	public static boolean isContains(String key) {
		return mSharedPreferences.contains(key);
	}

	public static void remove(String key) {
		mEditor.remove(key);
		mEditor.commit();
	}

	public static void clear() {
		mEditor.clear().commit();
	}
}
