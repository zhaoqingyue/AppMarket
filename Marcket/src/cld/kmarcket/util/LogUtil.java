package cld.kmarcket.util;

import android.util.Log;

public class LogUtil 
{
	public static final String TAG = "kmarcket";   
	private static boolean sVerbose = true; //冗长
	private static boolean sDebug = true;   //调试
	private static boolean sInfo = true;    //信息
	private static boolean sWarn = true;    //警告
	private static boolean sError = true;   //错误
	
	public static void v(String tag, String msg)
	{
		if (sVerbose)
		{
			Log.v(tag, msg);
		}
	}
	
	public static void d(String tag, String msg)
	{
		if (sDebug)
		{
			Log.d(tag, msg);
		}
	}
	
	public static void i(String tag, String msg)
	{
		if (sInfo)
		{
			Log.i(tag, msg);
		}
	}
	
	public static void w(String tag, String msg)
	{
		if (sWarn)
		{
			Log.w(tag, msg);
		}
	}
	
	public static void e(String tag, String msg)
	{
		if (sError)
		{
			Log.e(tag, msg);
		}
	}
}
