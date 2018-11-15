package com.download.api;

public class TaskStatus 
{
	public static final int DOWNLOAD_STATUS_IDLE = 0;
	public static final int DOWNLOAD_STATUS_PAUSE = 1;		
	public static final int DOWNLOAD_STATUS_ING = 2;
	public static final int DOWNLOAD_STATUS_END = 3;
	public static final int DOWNLOAD_STATUS_ERROR = 4;
	public static final int DOWNLOAD_STATUS_FILEBREAK = 5;
	public static final int DOWNLOAD_STATUS_NETERROR = 6;
	public static final int INSTALL_STATUS_WAIT = 7;
	public static final int INSTALL_STATUS_INSTALLING = 8;
	public static final int INSTALL_STATUS_SUCESS = 9;
	public static final int INSTALL_STATUS_ERROR = 10;
	
	public static interface ITaskCallBack
	{
		public void updateDownloadProcess(long downLength, long fileLength);
		public void updateTaskStatus(int status);
	}
}
