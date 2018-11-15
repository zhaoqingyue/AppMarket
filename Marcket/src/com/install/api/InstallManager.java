package com.install.api;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import com.download.api.TaskStatus;

public class InstallManager 
{
	static private InstallManager mInstance = null;
	static public InstallManager getInstance(Context context) 
	{
		if (mInstance == null) 
		{
			synchronized (InstallManager.class) 
			{
				if (mInstance == null) 
				{
					mInstance = new InstallManager(context);
				}
			}
		}
		return mInstance;
	}

	Context mContext = null;
	List<InstallInfo> mInstallTaskList = new ArrayList<InstallInfo>(8);
	List<InstallInfo> mDoTaskList = new ArrayList<InstallInfo>(8);

	private InstallManager(Context context) 
	{
		mContext = context;
	}
	
	@SuppressLint("HandlerLeak") 
	private Handler mHandler = new Handler() 
	{
		@Override
		public void handleMessage(Message msg) 
		{
			super.handleMessage(msg);
			if (msg.what == 0) 
			{
				removeMessages(0);
				doAllOperate();
			}
		}
	};
	
	private void doAllOperate() 
	{
		mDoTaskList.addAll(mInstallTaskList);
		mInstallTaskList.clear();
		for (InstallInfo item : mDoTaskList) 
		{
			doOneOperate(item);
		}
		mDoTaskList.clear();
	}
	
	private void doOneOperate(final InstallInfo installInfo) 
	{
		if(installInfo == null)
			return;
		
		if(mContext == null)
		{
			File file = new File(installInfo.filePath);
			if(!file.exists() && installInfo.callBack != null)
			{
				installInfo.callBack.updateTaskStatus(
						TaskStatus.INSTALL_STATUS_ERROR);
			}
			return;
		}
		
		new Thread()
		{
			@Override
			public void run() 
			{
				super.run();
				Intent intent = new Intent();
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setAction(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(
						new File(installInfo.filePath)), 
						"application/vnd.android.package-archive");
				mContext.startActivity(intent);
			}
			
		}.start();
		
		if(installInfo.callBack != null)
			installInfo.callBack.updateTaskStatus(
					TaskStatus.INSTALL_STATUS_INSTALLING);
	}

	public boolean addInstallTask(String filePath,
			TaskStatus.ITaskCallBack callBack) 
	{
		if(filePath == null || callBack == null
				|| filePath.equalsIgnoreCase(""))
			return false;
		
		if(taskExist(filePath))
			return false;
		
		mInstallTaskList.add(new InstallInfo(filePath, callBack));
		callBack.updateTaskStatus(TaskStatus.INSTALL_STATUS_WAIT);
		mHandler.obtainMessage(0).sendToTarget();
		return true;
	}
	
	boolean taskExist(String filePath)
	{
		if(mInstallTaskList.size() <= 0 || filePath.equalsIgnoreCase(""))
			return false;
		
		for(InstallInfo item: mInstallTaskList)
		{
			if(item.filePath.equalsIgnoreCase(filePath))
				return true;
		}
		return false;
	}

	static class InstallInfo 
	{
		public String filePath = "";
		public TaskStatus.ITaskCallBack callBack = null;

		public InstallInfo(String filePath, TaskStatus.ITaskCallBack callBack)
		{
			this.filePath = filePath;
			this.callBack = callBack;
		}
	}
}
