package cld.kmarket.download;

import java.io.File;
import com.cld.log.CldLog;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

public class DownloadUtils 
{
	private static final String TAG = "DownloadUtils";
	
	/**
	 * 获取SD卡的总空间
	 * @param context
	 * @return
	 */
    @SuppressWarnings("deprecation")
    public static long getSDCardTotalSize(Context context) 
    {  
    	//取得SD卡文件路径
    	File path = Environment.getExternalStorageDirectory();
    	StatFs statFs = new StatFs(path.getPath());
    	//获取单个数据块的大小(Byte)
    	long blockSize = statFs.getBlockSize();
    	//获取所有数据块数
    	long totalBlocks = statFs.getBlockCount();
    	return totalBlocks * blockSize;                   //单位: Byte
    	//return (totalBlocks * blockSize) / 1024;        //单位: KB
    	//return (totalBlocks * blockSize) / 1024 / 1024; //单位: MB
    } 
    
    /**
     * 获取SD卡的可用空间
     * @param context
     * @return
     */
    @SuppressWarnings("deprecation")
    public static long getSDCardAvailSize() 
    {  
    	//取得SD卡文件路径
    	File path = Environment.getExternalStorageDirectory();
    	StatFs statFs = new StatFs(path.getPath());
    	//获取单个数据块的大小(Byte)
    	long blockSize = statFs.getBlockSize();
    	//空闲的数据块的数量
    	long availableBlocks = statFs.getAvailableBlocks();
    	return availableBlocks * blockSize;                   //单位: Byte
    	//return (availableBlocks * blockSize) / 1024 / 1024; //单位: MB
    }
    
    public static boolean isEnough(int packsize)
    {
    	CldLog.i(TAG, "packsize: " + packsize + ", availsize: " + getSDCardAvailSize());
    	if (packsize < getSDCardAvailSize())
    	{
    		return true;
    	}
    	return false;
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
