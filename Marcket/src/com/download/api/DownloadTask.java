package com.download.api;

import cld.kmarcket.util.LogUtil;
import android.content.Context;

public class DownloadTask 
{
	private String  mUrl;
	private Context mContext;
	private Downloader downloader;
	private DownloadWatchManager mWatcher;
	
	private TaskStatus.ITaskCallBack mDownloadCallback = 
			new TaskStatus.ITaskCallBack() 
	{
		@Override
		public void updateTaskStatus(int status) 
		{
			mWatcher.updateTaskStatus(mUrl, status);
		}
		
		@Override
		public void updateDownloadProcess(long downLength, long fileLength) 
		{
			mWatcher.updateDownloadProcess(mUrl, downLength, fileLength);
		}
	};

	public DownloadTask(String url, Context context) 
	{
		mUrl = url;
		mContext = context;
		mWatcher = DownloadWatchManager.getInstance();
	}
	
	public String getDownloadUrl()
	{
		return mUrl;
	}

	public void startTask() 
	{
		if(mUrl == null || mUrl.equalsIgnoreCase(""))
			return;
		
		LogUtil.i(LogUtil.TAG," ++++ startTask ++++");
		downloader = new Downloader(mContext, mDownloadCallback);
		downloader.download(mUrl, 3);
	}

	public void pauseTask() 
	{
		LogUtil.i(LogUtil.TAG," ++++ pauseTask ++++");
		downloader.pause();
	}
	
	public void resumeTask()
	{
		LogUtil.i(LogUtil.TAG," ++++ resumeTask ++++");
		downloader.resume();
	}

	public void deleteTask() 
	{
		LogUtil.i(LogUtil.TAG," ++++ deleteTask ++++");
		downloader.delete();
		mDownloadCallback.updateDownloadProcess(0, 0);
	}
	
	public String getDownloadDiskFilePath() 
	{
		return downloader.getDownloadDiskFilePath();
	}
}
