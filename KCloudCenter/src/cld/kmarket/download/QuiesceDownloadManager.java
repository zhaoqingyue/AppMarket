package cld.kmarket.download;

import com.cld.log.CldLog;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import cld.kcloud.center.KCloudCtx;
import cld.kcloud.custom.bean.KCloudInstalledInfo;
import cld.kcloud.database.KCloudInstallingTable;
import cld.kcloud.utils.KCloudCommonUtil;
import cld.kmarket.install.InstallManager;

public class QuiesceDownloadManager 
{
	private static final String TAG = "QuiesceDownloadManager";
	private Context mContext;
	private KCloudInstalledInfo mAppinfo;
	private String mUrlPath;
	private long mFileLen;
	
	static private QuiesceDownloadManager mInstance = null;
	static public QuiesceDownloadManager getInstance(Context context) 
	{
		if (mInstance == null) 
		{
			synchronized (QuiesceDownloadManager.class) 
			{
				if (mInstance == null) 
				{
					mInstance = new QuiesceDownloadManager(context);
				}
			}
		}
		return mInstance;
	}
	
	public QuiesceDownloadManager(Context context)
	{
		mContext = context;
	}
	
	public void startQuiesceDownload(KCloudInstalledInfo appinfo)
	{
		//判断剩余空间是否足够
		if (DownloadUtils.isEnough(appinfo.getPackSize()))
		{
			mAppinfo = appinfo;
			mUrlPath = mAppinfo.getAppUrl();
			DownloadManager.getInstance(mContext).addDownloadTask(mUrlPath);
			DownloadWatchManager.getInstance().registerWachter(mUrlPath, mDownloadCallback);
		}
		else 
		{
			CldLog.i(TAG, " memory is not enough ");
		}
	}
	
	@SuppressLint("NewApi") 
	private void onDownloadFinish(String urlPath)
	{
		if (urlPath == null || urlPath.isEmpty())
			return;
		
		//是否静默安装  0：否; 1：是 
		switch (mAppinfo.getQuiesce()) 
		{
		case 0:
		{
			KCloudInstalledInfo temp = KCloudInstallingTable.getInstance().
					queryInstallingInfo(mAppinfo.getPkgName());
			if (temp != null)
			{
				long curtime = System.currentTimeMillis();
				if (mAppinfo.getAppUrl().endsWith(temp.getAppUrl()) && 
					mAppinfo.getVerCode() == temp.getVerCode())
				{
					if (KCloudCommonUtil.isSameDay(curtime, temp.getInstallTime()))
					{
						CldLog.i(TAG, " The same days ");
						return;
					}
					else
					{
						CldLog.i(TAG, " Different days ");
						KCloudInstallingTable.getInstance().updateInstallingInfo(temp);
						InstallManager.getInstance().startNormalInstall(mContext, urlPath);
					}
				}
				else
				{
					CldLog.i(TAG, " Different upgrade packages ");
					KCloudInstallingTable.getInstance().updateInstallingInfo(mAppinfo);
					InstallManager.getInstance().startNormalInstall(mContext, urlPath);
				}
			}
			else
			{
				CldLog.i(TAG, " Does not exist ");
				KCloudInstallingTable.getInstance().insertInstallingInfo(mAppinfo);
				InstallManager.getInstance().startNormalInstall(mContext, urlPath);
			}
			break;
		}
		case 1:
		{
			KCloudInstallingTable.getInstance().insertInstallingInfo(mAppinfo);
			InstallManager.getInstance().startSlienceInstall(mContext, urlPath);
			break;
		}
		default:
			break;
		}
	}
	
	private TaskStatus.ITaskCallBack mDownloadCallback = new TaskStatus.ITaskCallBack()
	{
		@Override
		public void updateTaskStatus(int status) 
		{
			Message msg = mHandler.obtainMessage(2);
			msg.getData().putInt("status", status);
			mHandler.sendMessage(msg);
		}
		
		@Override
		public void updateDownloadProcess(long downLength, long fileLength) 
		{
			if (fileLength != mFileLen) 
			{
				Message msg = mHandler.obtainMessage(0);
				msg.getData().putLong("fileLen", fileLength);
				mHandler.sendMessage(msg);
			}

			Message msg = mHandler.obtainMessage(1);
			msg.getData().putLong("done", downLength);
			mHandler.sendMessage(msg);
		}
	};
	
	@SuppressLint("HandlerLeak") 
	private Handler mHandler = new Handler(KCloudCtx.getAppContext().getMainLooper()) 
	{
		@Override
		public void handleMessage(Message msg) 
		{
			switch (msg.what) 
			{
			case 0: //获取文件大小
			{
				mFileLen = msg.getData().getLong("fileLen");
				break;
			}
			case 1: //获取下载大小
			{
				//获取当前下载的总量
				long done = msg.getData().getLong("done");
				//CldLog.i(TAG, " done: " + done);
				if (mFileLen > 0) 
				{
					long progress = done * 100 / mFileLen;
					//CldLog.i(TAG, " progress: " + progress);
				}
				break;
			}
			case 2: //获取下载状态
			{
				switch (msg.getData().getInt("status")) 
				{
				case TaskStatus.DOWNLOAD_STATUS_ING:
				{
					break;
				}
				case TaskStatus.DOWNLOAD_STATUS_END:
				{
					CldLog.i(TAG, " DOWNLOAD_STATUS_END ");
					onDownloadFinish(mUrlPath);
					break;
				}	
				default:
					break;
				}
			}
			default:
				break;
			}
		}
	};
}
