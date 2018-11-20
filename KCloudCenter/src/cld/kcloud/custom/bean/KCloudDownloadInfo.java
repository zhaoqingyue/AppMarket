package cld.kcloud.custom.bean;

public class KCloudDownloadInfo 
{
	private String mPath = "";
	private int  mThreadId = 0;
	private long mStart = 0;
	private long mEnd = 0;
	private long mDone = 0;
	
	public KCloudDownloadInfo() 
	{
		mPath = "";
		mThreadId = 0;
		mStart = 0;
		mEnd = 0;
		mDone = 0;
	}

	public KCloudDownloadInfo(String path, int threadId, long start, long end, long done) 
	{
		mPath = path;
		mThreadId = threadId;
		mStart = start;
		mEnd = end;
		mDone = done;
	}
	
	public KCloudDownloadInfo(KCloudDownloadInfo info)
	{
		mPath = info.getPath();
		mThreadId = info.getThreadId();
		mStart = info.getStart();
		mEnd = info.getEnd();
		mDone = info.getDone();
	}

	public long getStart() 
	{
		return mStart;
	}

	public void setStart(long start) 
	{
		this.mStart = start;
	}

	public long getEnd() 
	{
		return mEnd;
	}

	public void setEnd(long end) 
	{
		this.mEnd = end;
	}

	public String getPath() 
	{
		return mPath;
	}

	public void setPath(String mPath) 
	{
		this.mPath = mPath;
	}

	public int getThreadId() 
	{
		return mThreadId;
	}

	public void setThreadId(int mThreadId) 
	{
		this.mThreadId = mThreadId;
	}

	public long getDone() 
	{
		return mDone;
	}

	public void setDone(long mDone) 
	{
		this.mDone = mDone;
	}
}
