package cld.kmarcket.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class NetUtil 
{
	/**
	 * 网络是否连接
	 * @param context
	 */
	public static boolean isNetAvailable(Context context) 
	{
		if (context != null)
		{
			ConnectivityManager conManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (conManager != null)
			{
				NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
				if (networkInfo != null) 
				{
					return networkInfo.isAvailable();
				}
			}
		}
		return false;
	}
	
	/**
	 * @Title: getNetType
	 * @Description: 0:移动网络，1:wifi
	 * @param context
	 */
	public static int getNetType(Context context) 
	{
		int type = -1;
		if (context != null)
		{
			ConnectivityManager manager = (ConnectivityManager) 
					context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (manager != null)
			{
				NetworkInfo networkInfo = manager.getNetworkInfo(
						ConnectivityManager.TYPE_MOBILE);
				if (networkInfo != null)
				{
					State mobile = networkInfo.getState();
					if (mobile == State.CONNECTED) 
					{
						return ConstantUtil.NET_TYPE_MOBILE;
					}
				}
				
				networkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				if (networkInfo != null)
				{
					State wifi = networkInfo.getState();
					if (wifi == State.CONNECTED) 
					{
						return ConstantUtil.NET_TYPE_WIFI;
					}
				}
			}
		}
		return type;
	}

	/**
	 * 获取WiFi等级
	 * @param context
	 * @return
	 */
	public static int getWifiLevel(Context context) 
	{
		int strength = 0;
		if (context != null)
		{
			//Wifi的连接速度及信号强度
			WifiManager wifiManager = (WifiManager) context.getSystemService(
					Context.WIFI_SERVICE);
			if (wifiManager != null)
			{
				WifiInfo info = wifiManager.getConnectionInfo();
				if (info != null && info.getBSSID() != null) 
				{
					//链接信号强度
					strength = WifiManager.calculateSignalLevel(info.getRssi(), 5);
				}
			}
		}
		return strength;
	}
}
