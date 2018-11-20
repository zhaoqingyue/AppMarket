package cld.kmarket.download;

import java.util.ArrayList;
import java.util.List;
import cld.kcloud.center.KCloudCtx;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

public class DownloadWatchManager 
{
	private List<MyWatchManager> mList = new ArrayList<MyWatchManager>(8);
	private static DownloadWatchManager mInstance = null;
	public static DownloadWatchManager getInstance() 
	{
		if (mInstance == null) 
		{
			synchronized (DownloadWatchManager.class) 
			{
				if (mInstance == null) 
				{
					mInstance = new DownloadWatchManager();
				}
			}
		}
		return mInstance;
	}

	private DownloadWatchManager() 
	{
	}

	public void registerWachter(String url, TaskStatus.ITaskCallBack callback)
	{
		if (callback == null || url == null || url.equalsIgnoreCase(""))
			return;

		MyWatchManager manager = null;
		for (MyWatchManager item : mList) 
		{
			if (item.getUrl().equalsIgnoreCase(url)) 
			{
				manager = item;
				break;
			}
		}

		if (manager == null) 
		{
			mList.add(new MyWatchManager(url, callback));
		} 
		else 
		{
			manager.addWatcher(url, callback);
		}
	}
	
	public void unregisterWachter(String url, TaskStatus.ITaskCallBack callback)
	{
		if (url == null || url.equalsIgnoreCase(""))
			return;
		
		for (MyWatchManager item : mList) 
		{
			if (item.getUrl().equalsIgnoreCase(url)) 
			{
				item.clear(callback);
				return;
			}
		}
	}

	public void updateDownloadProcess(String url, long downLength, 
			long fileLength) 
	{
		if (url == null || url.equalsIgnoreCase(""))
			return;

		MyWatchManager manager = null;
		for (MyWatchManager item : mList) 
		{
			if (item.getUrl().equalsIgnoreCase(url)) 
			{
				manager = item;
				break;
			}
		}

		if (manager == null) 
		{
			manager = new MyWatchManager(url);
			mList.add(manager);
		}
		manager.updateDownloadProcess(url, downLength, fileLength);
	}

	public void updateTaskStatus(String url, int status) 
	{
		if (url == null || url.equalsIgnoreCase(""))
			return;

		MyWatchManager manager = null;
		for (MyWatchManager item : mList) 
		{
			if (item.getUrl().equalsIgnoreCase(url)) 
			{
				manager = item;
				break;
			}
		}

		if (manager == null) 
		{
			manager = new MyWatchManager(url);
			mList.add(manager);
		}
		manager.updateTaskStatus(url, status);
	}

	private class MyWatchManager 
	{
		String mUrl = "";
		long mTempLength = 0;
		long mDownLength = 0;
		long mFileLength = 0;
		int mStatus = TaskStatus.DOWNLOAD_STATUS_IDLE;
		List<TaskStatus.ITaskCallBack> callbacks = new ArrayList<TaskStatus.ITaskCallBack>();
		
		@SuppressLint("HandlerLeak") 
		Handler mHandler = new Handler(KCloudCtx.getAppContext().getMainLooper())
		{
			@Override
			public void handleMessage(Message msg) 
			{
				super.handleMessage(msg);
				if(callbacks.size() <= 0)
					return;
				
				switch (msg.what) 
				{
				case 1:
				{
					removeMessages(1);
					for(TaskStatus.ITaskCallBack item : callbacks)
					{
						item.updateTaskStatus(mStatus);
					}
					break;
				}
				case 2:
				{
					removeMessages(2);
					for(TaskStatus.ITaskCallBack item : callbacks)
					{
						//防止进度条回滚
						if (mTempLength > 0 && mDownLength < mTempLength)
							return;
						//防止界面已经显示"安装"， 再次刷进度
						if (mTempLength == 100 && mDownLength == 100)
							return;
						
						item.updateDownloadProcess(mDownLength, mFileLength);
						mTempLength = mDownLength;
					}
					break;
				}	
				default:
					break;
				}
			}
		};

		MyWatchManager(String url, TaskStatus.ITaskCallBack callback) 
		{
			if (callback == null || url == null || url.equalsIgnoreCase(""))
				return;

			mUrl = url;
			if (callbacks.contains(callback))
			{
				//避免callbacks中有多个相同的callback
				callbacks.remove(callback);
			}
			callbacks.add(callback);
		}

		MyWatchManager(String url) 
		{
			if (url == null || url.equalsIgnoreCase(""))
				return;

			mUrl = url;
		}

		public void addWatcher(String url, TaskStatus.ITaskCallBack callback) 
		{
			if (callback == null || url == null || url.equalsIgnoreCase(""))
				return;

			if (url.equalsIgnoreCase(mUrl)) 
			{
				if (callbacks.contains(callback))
				{
					//避免callbacks中有多个相同的callback
					callbacks.remove(callback);
				}
				callbacks.add(callback);
				sendMsgUpdateStatus();
				sendMsgUpdateProcess();
			}
		}

		public String getUrl() 
		{
			return mUrl;
		}
		
		/**
		 * 清除缓存值
		 */
		public void clear(TaskStatus.ITaskCallBack callback)
		{
			mDownLength = 0;
			mTempLength = 0;
			mFileLength = 0;
			mStatus = TaskStatus.DOWNLOAD_STATUS_IDLE;
			callbacks.remove(callback);
		}

		public void updateDownloadProcess(String url, long downLength, 
				long fileLength) 
		{
			if (url == null || !url.equalsIgnoreCase(mUrl))
				return;

			if (mDownLength != downLength || mFileLength != fileLength) 
			{
				mDownLength = downLength;
				mFileLength = fileLength;
				sendMsgUpdateProcess();
			}
		}

		public void updateTaskStatus(String url, int status) 
		{
			if (url == null || !url.equalsIgnoreCase(mUrl))
				return;

			if (mStatus != status) 
			{
				mStatus = status;
				sendMsgUpdateStatus();
			}
		}
		
		private void sendMsgUpdateStatus() 
		{
			if(callbacks.size() <= 0)
				return;
			mHandler.obtainMessage(1).sendToTarget();
		}

		private void sendMsgUpdateProcess() 
		{
			if(callbacks.size() <= 0)
				return;
			mHandler.obtainMessage(2).sendToTarget();
		}
	}
}
