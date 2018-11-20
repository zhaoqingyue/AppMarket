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
	 * ��ȡSD�����ܿռ�
	 * @param context
	 * @return
	 */
    @SuppressWarnings("deprecation")
    public static long getSDCardTotalSize(Context context) 
    {  
    	//ȡ��SD���ļ�·��
    	File path = Environment.getExternalStorageDirectory();
    	StatFs statFs = new StatFs(path.getPath());
    	//��ȡ�������ݿ�Ĵ�С(Byte)
    	long blockSize = statFs.getBlockSize();
    	//��ȡ�������ݿ���
    	long totalBlocks = statFs.getBlockCount();
    	return totalBlocks * blockSize;                   //��λ: Byte
    	//return (totalBlocks * blockSize) / 1024;        //��λ: KB
    	//return (totalBlocks * blockSize) / 1024 / 1024; //��λ: MB
    } 
    
    /**
     * ��ȡSD���Ŀ��ÿռ�
     * @param context
     * @return
     */
    @SuppressWarnings("deprecation")
    public static long getSDCardAvailSize() 
    {  
    	//ȡ��SD���ļ�·��
    	File path = Environment.getExternalStorageDirectory();
    	StatFs statFs = new StatFs(path.getPath());
    	//��ȡ�������ݿ�Ĵ�С(Byte)
    	long blockSize = statFs.getBlockSize();
    	//���е����ݿ������
    	long availableBlocks = statFs.getAvailableBlocks();
    	return availableBlocks * blockSize;                   //��λ: Byte
    	//return (availableBlocks * blockSize) / 1024 / 1024; //��λ: MB
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
	 * @Description: ɾ���ļ�
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
