package cld.kmarket.download;

import java.util.ArrayList;
import java.util.List;
import com.cld.log.CldLog;
import android.content.Context;

public class DownloadManager 
{
	private static final String TAG = "DownloadManager";
	private Context mContext = null;
	private List<String> mDownLoadList = new ArrayList<String>(8);
	private List<String> mPauseList = new ArrayList<String>(8);
	private List<DownloadTask> mDownloaderList = new ArrayList<DownloadTask>(4);
	static private DownloadManager mInstance = null;
	static public DownloadManager getInstance(Context context) 
	{
		if (mInstance == null) 
		{
			synchronized (DownloadManager.class) 
			{
				if (mInstance == null) 
				{
					mInstance = new DownloadManager(context);
				}
			}
		}
		return mInstance;
	}

	private DownloadManager(Context context) 
	{
		mContext = context;
	}
	
	public boolean isDownloadTaskTaskExist(String url)
	{
		for (DownloadTask item : mDownloaderList) 
		{
			if (item.getDownloadUrl().equals(url))
			{
				return true;
			}
		}
		return false;
	}

	public void addDownloadTask(String url) 
	{
		if (isDownloadTaskTaskExist(url))
		{
			if (mPauseList.contains(url))
			{
				/**
				 * ���ع����У��Ȱ���ͣ���˳�KӦ�ã����ؽ���KӦ�ú󣬼���Ƿ���δ��ɵ�����
				 * ����У��򴴽�һ���µ�����
				 */
				CldLog.d(TAG, "++++ resume downloadtask ++++");
				resumeDownloadTask(url);
			}
			else
			{
				/**
				 * ���ع����У�ֱ���˳�KӦ�ã�����������û�н���ǰ�ٴν���KӦ�ã�
				 * ��ʱ����Ƿ���δ��ɵ���������У�����������񣬶������ٴ���һ���µ�����
				 */
				CldLog.d(TAG, "++++ go on downloadtask ++++");
				return;
			}
		}
		else
		{
			//�����������
			CldLog.d(TAG, "++++ start new task ++++");
			DownloadTask downloader = new DownloadTask(url, mContext);
			mDownLoadList.add(url);
			mDownloaderList.add(downloader);
			downloader.startTask();
		}
	}

	public void pauseDownloadTask(String url) 
	{
		for (DownloadTask item : mDownloaderList) 
		{
			if (item.getDownloadUrl().equalsIgnoreCase(url)) 
			{
				CldLog.d(TAG," ++++ pauseDownloadTask ++++");
				mPauseList.add(url);
				item.pauseTask();
				break;
			}
		}
	}

	public void resumeDownloadTask(String url) 
	{
		for (DownloadTask item : mDownloaderList) 
		{
			if (item.getDownloadUrl().equalsIgnoreCase(url)) 
			{
				CldLog.d(TAG," ++++ resumeDownloadTask ++++");
				mPauseList.remove(url);
				item.resumeTask();
				break;
			}
		}
	}

	public void deleteDownloadTask(String url) 
	{
		for (DownloadTask item : mDownloaderList) 
		{
			if (item.getDownloadUrl().equalsIgnoreCase(url)) 
			{
				CldLog.d(TAG," ++++ deleteDownloadTask ++++");
				item.deleteTask();
				if (mPauseList != null && !mPauseList.isEmpty())
				{
					if (mPauseList.contains(url))
					{
						mPauseList.remove(url);
					}
				}
				mDownLoadList.remove(url);
				mDownloaderList.remove(item);
				break;
			}
		}
	}

	public String getDownloadDiskFilePath(String url) 
	{
		String diskFilePath = "";
		for (DownloadTask item : mDownloaderList) 
		{
			if (item.getDownloadUrl().equalsIgnoreCase(url)) 
			{
				diskFilePath = item.getDownloadDiskFilePath();
				break;
			}
		}
		return diskFilePath;
	}
}
