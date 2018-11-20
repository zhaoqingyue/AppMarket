package cld.kmarket.download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import com.cld.log.CldLog;
import cld.kcloud.custom.bean.KCloudDownloadInfo;
import cld.kcloud.database.KCloudDownloadTable;
import cld.kmarket.download.TaskStatus.ITaskCallBack;
import android.content.Context;

public class Downloader 
{
	private static final String TAG = "Downloader";
	private KCloudDownloadTable mDownloadTable;
	private String mUrlPath;
	private long mFileLen;
	private long mDone;
	private int mThreadCount = 3;
	private int mDownLoadStatus = TaskStatus.DOWNLOAD_STATUS_IDLE;
	private int mRunningThread = 0;
	private int mErrorCount = 0;
	private boolean isPause = false;
	private boolean isDelete = false;
	private ITaskCallBack callBack = null;
	ArrayList<DownloadThread> mDownloadThreads = new ArrayList<DownloadThread>(4);

	public Downloader(Context context, ITaskCallBack callBack)
	{
		mDownloadTable = KCloudDownloadTable.getInstance();
		this.callBack = callBack;
	}	
	
	public String getDownloadDiskFilePath(){
		 return getDownloadDiskFilePath(mUrlPath);
	}
	
	private String getDownloadDiskFilePath(final String urlPath)
	{
		String name = urlPath.substring(urlPath.lastIndexOf("/") + 1);
		return DownloadDir.getDownloadDir()+"/"+name;
	}

	public boolean download(final String urlPath, final int thCount) 
	{
		if(callBack == null)
			return false;
		
		mUrlPath = urlPath;
		if(thCount > 0)
			mThreadCount = thCount;
		
		startDown(mUrlPath, mThreadCount);
		return true;
	}
	
	private void startDown(final String urlPath, final int thCount)
	{
		if(isPause || isDelete)
			return;

		mDone = 0;
		Runnable runnable = new Runnable() 
		{
			@Override
			public void run() 
			{
				try 
				{
					URL url = new URL(urlPath);
					HttpURLConnection conn;
					conn = (HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(10000);
					if (conn.getResponseCode() == 200) 
					{
						if(isPause || isDelete)
							return;
						
						setDownloadStatus(TaskStatus.DOWNLOAD_STATUS_ING);
						mFileLen = conn.getContentLength();
						File file = new File(getDownloadDiskFilePath(urlPath));
						if(file.exists() && file.length() != mFileLen)
						{
							setDownloadStatus(TaskStatus.DOWNLOAD_STATUS_FILEBREAK);
						}
						RandomAccessFile raf = new RandomAccessFile(file, "rws");
						raf.setLength(mFileLen);
						raf.close();
						
						List<KCloudDownloadInfo> existList = mDownloadTable.queryUndoDownloadInfos(urlPath);
						if (existList.size() <= 0) 
						{
							CldLog.d(TAG," start new filelent: " + mFileLen);
							if (callBack != null) 
							{
								callBack.updateDownloadProcess(0, mFileLen);
							}

							long partLen = (mFileLen + thCount - 1) / thCount;
							DownloadThread threadTmp = null;
							long start = 0, end = 0;
							for (int i=0; i<thCount; i++) 
							{
								start = i*partLen;
								end = (i+1)*partLen -1;
								if (i == (thCount - 1))
									end = mFileLen -1;
								
								threadTmp = new DownloadThread(url, file, start, end, 0, i);
								KCloudDownloadInfo downloadInfo = new KCloudDownloadInfo(url.toString(), i, start, end, 0);
								mDownloadTable.insertDownloadInfo(downloadInfo);
								mDownloadThreads.add(threadTmp);
								threadTmp.start();
								threadAdd();
							}
						} 
						else 
						{
							CldLog.d(TAG," start old ");
							DownloadThread threadTmp = null;
							for(KCloudDownloadInfo item : existList)
							{
								if(item.getDone() == (item.getEnd() - item.getStart()+1)){
									mDone += item.getDone();
									continue;
								}
								else
								{
									mDone += item.getDone();
									threadTmp = new DownloadThread(url, file,
											item.getStart(), item.getEnd(), item.getDone(), item.getThreadId());
									mDownloadThreads.add(threadTmp);
									threadTmp.start();
									threadAdd();
								}
							}
							
							if (callBack != null) 
							{
								callBack.updateDownloadProcess(mDone, mFileLen);
								checkDownload();
							}
						}
					}
					else
					{
						CldLog.d(TAG, "ResponseCode != 200");
						mErrorCount++;
						//�ٳ�������2�Σ����2�ι��󻹲��У� ����ʾ
						if (mErrorCount == 3)
						{
							mErrorCount = 0;
							setDownloadStatus(TaskStatus.DOWNLOAD_STATUS_NETERROR);
						}
						else
						{
							startDown(mUrlPath, mThreadCount);
						}
					}
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
					CldLog.e(TAG, "stackTrace: " + e.toString() + ", urlPath: " + urlPath);
					mErrorCount++;
					//�ٳ�������2�Σ����2�ι��󻹲��У� ����ʾ
					if (mErrorCount == 3)
					{
						mErrorCount = 0;
						setDownloadStatus(TaskStatus.DOWNLOAD_STATUS_ERROR);
					}
					else
					{
						startDown(mUrlPath, mThreadCount);
					}
				}
			}
		};
		new Thread(runnable).start();
	}
	
	private synchronized void addDone(long add)
	{
		mDone += add;
		if(callBack != null)
		{
			callBack.updateDownloadProcess(mDone, mFileLen);
		}
	}
	
	private void setDownloadStatus(int status)
	{
		if(mDownLoadStatus != status)
		{
			mDownLoadStatus = status;
			if(callBack != null)
			{
				callBack.updateTaskStatus(mDownLoadStatus);
			}
		}
	}
	
	private void threadAdd()
	{
		++mRunningThread;
	}
	
	private void threadSub()
	{
		--mRunningThread;
		if(mRunningThread == 0)
		{
			checkDownload();
		}
	}
	
	private void checkDownload()
	{
		if (mDone == mFileLen)
		{
			setDownloadStatus(TaskStatus.DOWNLOAD_STATUS_END);
		}
	}

	private final class DownloadThread extends Thread 
	{
		private URL url;
		private File file;
		private long start;
		private long end;
		private long done;
		private int id;
		private boolean pauseThread = false;
		private boolean exitThread = false;

		public DownloadThread(URL url, File file, long start, long end, long done, int id) 
		{
			this.url = url;
			this.file = file;
			this.start = start;
			this.end = end;
			this.done = done;
			this.id = id;
		}
		
		public void pauseThread()
		{
			pauseThread = true;
		}
		
		public void exitThread()
		{
			exitThread = true;
		}

		public void run() 
		{
			KCloudDownloadInfo info = new KCloudDownloadInfo(url.toString(), id, start, end, done);
			long start = this.start + info.getDone(); 
			long end = this.end;
			CldLog.d(TAG,"start thread " + id + "  done: " + info.getDone() 
				+ " start: " + start + " end: " + end + " retail: " + (end - start));
			if(start >= end)
				return;

			try 
			{
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setReadTimeout(10000);
				conn.setRequestProperty("Range", "bytes=" + start + "-" + end);
				RandomAccessFile raf = new RandomAccessFile(file, "rws");
				raf.seek(start);
				InputStream in = conn.getInputStream();
				byte[] buf = new byte[1024 * 10];
				int len;
				while ((len = in.read(buf)) != -1) 
				{
					if(pauseThread)
					{
						CldLog.d(TAG," pause thread " + id);
						break;
					}
					
					if(exitThread)
					{
						CldLog.d(TAG," exit thread " + id);
						/**
						 * add by zhaoqy 2016-6-24
						 * ���ȡ����������� �����첽�رգ�������������ʱ���������տɼ�ʱ�쳣
						 */
						if(callBack != null)
						{
							callBack.updateDownloadProcess(0, 0);
						}
						break;
					}
					
					raf.write(buf, 0, len);
					addDone(len);
					info.setDone(info.getDone() + len);
					mDownloadTable.updateDownloadInfo(info);
				}
	
				in.close();
				raf.close();
				CldLog.d(TAG, " pause thread " + id + 
						" close havedone: " + info.getDone() + "  Alldown: " + mDone);
				//״̬��ʾ������ɣ�ʵ��ȴû��������ɣ� ��ʱ��Ҫ����������ͬ�߳�id���߳�������
				if (!exitThread && !pauseThread) 
				{
					if( info.getDone() != (this.end - this.start + 1))
					{
						CldLog.d(TAG, "thread " + id + " restart");
						DownloadThread threadTmp = new DownloadThread(url, file, this.start, this.end, info.getDone(), this.id);
						mDownloadThreads.remove(DownloadThread.this);
						mDownloadThreads.add(threadTmp);
						threadTmp.start();
					} 
					else 
					{
						threadSub();
					}
				}
			}
			catch (Exception e)
			{
				if (!exitThread && !pauseThread)
				{
					//���������������ӳ�ʱ����Ҫ����������ͬ�߳�id���߳�������
					CldLog.d(TAG, "Exception: " + e.toString() + ", thread " + id + " restart");
					DownloadThread threadTmp = new DownloadThread(url, file, this.start, this.end, info.getDone(), this.id);
					mDownloadThreads.remove(DownloadThread.this);
					mDownloadThreads.add(threadTmp);
					threadTmp.start();
				}
			}
		}
	}

	public void pause() 
	{
		if (mDownloadThreads != null && mDownloadThreads.size() > 0)
		{
			isPause = true;
			for(DownloadThread item: mDownloadThreads)
			{
				if (item != null)
				{
					item.pauseThread();
				}
			}
			mDownloadThreads.clear();
			setDownloadStatus(TaskStatus.DOWNLOAD_STATUS_PAUSE);
		}
		CldLog.d(TAG," ++++ pause +++ ");
	}

	public void resume() 
	{
		if (isPause) 
		{
			isPause = false;
			startDown(mUrlPath, mThreadCount);
			CldLog.d(TAG," ++++ resume +++ ");
		}
	}
	
	public void delete() 
	{
		if (mDownloadThreads != null && mDownloadThreads.size() > 0)
		{
			isDelete = true;
			for(DownloadThread item: mDownloadThreads)
			{
				if (item != null)
				{
					item.exitThread();
				}
			}
			mDownloadThreads.clear();
			if (mDownloadTable != null)
			{
				mDownloadTable.deleteDownloadInfo(mUrlPath);
			}
		}
		CldLog.d(TAG," ++++ delete +++ ");
	}
}
