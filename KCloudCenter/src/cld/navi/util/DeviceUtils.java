package cld.navi.util;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.os.RemoteException;
import android.provider.Settings;

@SuppressLint("DefaultLocale") 
public class DeviceUtils {

	public static boolean isNeedChangeParameterDirectory = false;
	public static boolean isUseBugly = false;
	
	@SuppressLint("SdCardPath") 
	static public String[] paths = { 
		"/mnt/extsd1", 
		"/mnt/extsd2",
		"/storage/emulated/0", 
		"/storage/emulated/0/Sdcard",
		"/storage/emulated/0/sdcard", 
		"/Sdcard", 
		"/storage/sdcard1",
		"/mnt/sdcard/extStorages/SdCard",
		// "/mnt/mapSD",
		"/mnt/extSdCard",
		"/sdcard",             // 0
		"/mnt/sdcard",         // 1
		"/nand",               // 2
		"/flash",              // 3
		"/mnt/flash",          // 4
		"/mnt/D",              // 5
		"/mnt/sdcard/tflash",  // 6
		"/sdcard/extsd",       // 7
		"/tflash",             // 8
		"/sdcard/tflash",      // 9
		"/extsd",              // 10
		"/mnt/extsd",          // 11
		"/sdcard/external_sd", // 12
		"/sdcard2",            // 13
		"/sdcard/sd",          // 14
		"/mnt/sdcard-ext",     // 15
		"/mnt/sdcard/sdcard1", // 16
		"/mnt/ext_sdcard2", 
		"/mnt/sdcard/extern_sd", 
		"/storage/sdcard1",
		"/storage/emulated/0", 
		"/storage/sdcard0", 
		"storage/udisk" 
	};
	
	@SuppressLint({ "NewApi", "DefaultLocale" })
	public static String getSerialNum(Context context,int type)
	{

		int oneDigit = type%10;
		if(oneDigit == 1)
		{
			int tenDigit = type/10;
			String path = "/sys/class/mmc_host/mmc" + tenDigit+File.separator;
			File pathFile = new File(path);
			
			if(pathFile==null || !pathFile.exists()) 
				return "";
			
			if(android.os.Build.MODEL.trim().equals("Galaxy Series 7") ||
			   android.os.Build.MODEL.trim().equals("Galaxy Series 8") ||
			   android.os.Build.MODEL.trim().equals("Galaxy Series 9") ||
			   android.os.Build.MODEL.trim().equals("Galaxy Series 6S"))
			{
				String str = NetWorkUtil.getImeiNum(context);
				if(str.length()>14)
					str = str.substring(0, 14);
				return str;
			}
			if(pathFile.isDirectory())
			{
				String fileNames[] = pathFile.list();
				if(fileNames == null || fileNames.length == 0)
					return "";
				
				for(String fileName:fileNames)
				{
					if(fileName!=null&&fileName.length()>5 && 
						fileName.substring(0, 5).replace(" ", "").equals("mmc"+tenDigit+":"))
					{
						String fullPath = path+fileName+File.separator+"cid";
						String result = FileUtils.readCommonFile(fullPath, 32);
						if(result!=null&&result.length() == 32)
							return result.substring(18, 26);
					}
				}
			}
			return null;
		}
		else if(oneDigit ==2)
		{
			String str = Settings.System.getString(context.getContentResolver(), 
					Settings.Secure.ANDROID_ID);
		    if(str!=null)
		    {
		    	str = str.toUpperCase();
		    if(str.length()>23)
		    	str = str.substring(0, 23);
		    }
		    return str;
		}
		else if(oneDigit == 3)
		{
			String imei = NetWorkUtil.getImeiNum(context);
//			String mImeiNum = Build.SERIAL;
			String delUselessChar = imei.replace(" ", "");
			String str = delUselessChar.replace(":", "");

			if(str.length()>14)
				str = str.substring(0, 14);
			return str;
		}
		else if(oneDigit == 4)
		{
			String fileName = "/data/misc/wifi/gps_uuid";
			String result = FileUtils.readCommonFile(fileName, 23);
			if(result!=null)
			{
				return result.toUpperCase();
			}
		    return null;
		}
		else if(oneDigit == 5)
		{
			String fileName = "/data/misc/wifi/gps_uuid_two";
			String result = FileUtils.readCommonFile(fileName, 23);
			if(result!=null)
			{
				return result.toUpperCase();
			}
		    return null;
		}
		else if(oneDigit == 7) 
		{
			if(android.os.Build.SERIAL != null)
			{
				String str = android.os.Build.SERIAL.replace(" ", "");
				if(str!=null)
				{
					if(str.length()>23)
						str = str.substring(0, 23);
					return str;
				}
			}
			return null;
		}
		else if (oneDigit == 8)	
		{
			String serialId = null;
			try {
				serialId = ActivityManagerNative.getDefault().getSomething(1, "", 0, 0);
			} catch (RemoteException e1) { 
				e1.printStackTrace();
			} 
	    	
	    	if (serialId == null) {
	    		return "UNKNOW"; 
	    	} 
	    	
	    	serialId = serialId.toUpperCase();
	    	serialId = serialId.replace('O', '0');
	    	
	    	return serialId;
		}
		else
		{
			String str = NetWorkUtil.getImeiNum(context);
			if(str.length()>14)
				str = str.substring(0, 14);
			return str;
		}
	}
	
	public static boolean isNaviCardExists(String naviPath)
	{
		//if(new File(naviPath+"/NaviOne/manifest.cld").exists())
		if (new File(naviPath + "/NaviOne/data1.ndz").exists()
				|| new File(naviPath + "/NaviOne/data2.ndz").exists()) {
			return true;
		} else {
			return false;
		}
	}
}
