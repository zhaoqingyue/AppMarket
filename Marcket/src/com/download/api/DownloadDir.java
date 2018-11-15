package com.download.api;

import java.io.File;

import android.os.Environment;

public class DownloadDir {
	
	public static String getDownloadDir()
	{
		String strDirPath = Environment.getExternalStorageDirectory().
				getPath()+"/kmarket/download";
		File path = new File(strDirPath);
		if(!path.exists())
			path.mkdirs();
		
		return strDirPath;
	}
	
	public static String getDiskFilePath(final String urlPath)
	{
		String name = urlPath.substring(urlPath.lastIndexOf("/") + 1);
		return DownloadDir.getDownloadDir()+"/"+name;
	}
}
