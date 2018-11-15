package cld.kmarcket.util;

import java.io.File;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

public class FileUtil 
{
	/**
	 * 获取SD卡的总空间
	 * @param context
	 * @return
	 */
    @SuppressWarnings("deprecation")
    public static long getSDCardTotalSize(Context context) 
    {  
    	File path = Environment.getExternalStorageDirectory();
    	StatFs stat = new StatFs(path.getPath());
    	long blockSize = stat.getBlockSize();
    	long totalBlocks = stat.getBlockCount();
    	return totalBlocks * blockSize;
    }  
    
    /**
     * 获取SD卡的可用空间
     * @param context
     * @return
     */
    @SuppressWarnings("deprecation")
    public static long getSDCardAvailSize(Context context) 
    {  
    	File path = Environment.getExternalStorageDirectory();
    	StatFs stat = new StatFs(path.getPath());
    	long blockSize = stat.getBlockSize();
    	long availableBlocks = stat.getAvailableBlocks();
    	return availableBlocks * blockSize;
    }  
    
    public static boolean isEnough(Context context, int packsize)
    {
    	LogUtil.i(LogUtil.TAG, "packsize: " + packsize + ", availsize: " + getSDCardAvailSize(context));
    	if (packsize < getSDCardAvailSize(context))
    	{
    		return true;
    	}
    	return false;
    }
    
    /**
	 * 
	 * @Title: getApkDir
	 * @Description: 获取升级包路径
	 * @return: String
	 */
	public static String getApkDir()
	{
		//获得存储卡的路径
		String sdpath = Environment.getExternalStorageDirectory() + "/";
		return sdpath + "kmarcket";
	}
	
	/** 
	 * @Title: delete
	 * @Description: 删除文件
	 * @param file
	 * @return: void
	 */
	public static void delete(File file) 
	{
		if (file.isFile()) 
		{  
			file.delete();  
			return;  
		}  
		
		if(file.isDirectory())
		{  
			File[] childFiles = file.listFiles();  
			if (childFiles == null || childFiles.length == 0) 
			{  
				file.delete();  
				return;
			}  
			
			for (int i=0; i<childFiles.length; i++) 
			{  
		        delete(childFiles[i]);  
			}  
	        file.delete();  
	    }  
	}
}
