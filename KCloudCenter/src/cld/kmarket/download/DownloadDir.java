package cld.kmarket.download;

import java.io.File;
import android.os.Environment;

public class DownloadDir 
{
	public static String getDownloadDir()
	{
		String strDirPath = Environment.getExternalStorageDirectory().
				getPath() + "/kcloudcenter/download";
		File path = new File(strDirPath);
		if(!path.exists())
			path.mkdirs();
		
		return strDirPath;
	}
	
	public static String getDiskFilePath(String urlPath)
	{
		String name = urlPath.substring(urlPath.lastIndexOf("/") + 1);
		return DownloadDir.getDownloadDir() + "/" + name;
	}   
}
