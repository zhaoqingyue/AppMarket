package com.download.api;

import java.util.ArrayList;
import cld.kmarcket.util.LogUtil;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class StatusDao 
{
	private static StatusDao mInstance = null;
	public static StatusDao getInstance(Context context) 
	{
		if (mInstance == null) 
		{
			synchronized (StatusDao.class) 
			{
				if (mInstance == null) 
				{
					mInstance = new StatusDao(context);
				}
			}
		}
		return mInstance;
	}
	
	private SQLiteDatabase mDatabase;
	private ArrayList<Bundle> mOperateList = new ArrayList<Bundle>(128);
	private ArrayList<Bundle> mOperateDoList = new ArrayList<Bundle>(128);
	
	static final String STR_OPERATE = "OPERATE";
	static final String STR_STATUS = "STATUS";
	static final int OPERATE_INSERT = 1;
	static final int OPERATE_DELET = 2;
	static final int OPERATE_UPDATE_YTPE = 3;

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
	
	public StatusDao(Context context) 
	{
		mDatabase = DatabaseManager.getWritableDatabase(context);
	}
	
	private void doAllOperate() 
	{
		mOperateDoList.addAll(mOperateList);
		mOperateList.clear();
		for (Bundle item : mOperateDoList) 
		{
			doOneOperate(item);
		}
		mOperateDoList.clear();
	}
	
	private void doOneOperate(Bundle bundle) 
	{
		if (bundle == null)
			return;

		int opreate = bundle.getInt(STR_OPERATE, 0);
		Status status = (Status) bundle.getSerializable(STR_STATUS);
		if (opreate == 0 || status == null)
			return;

		switch (opreate) 
		{
		case OPERATE_INSERT: 
		{
			insert(status, true);
			break;
		}
		case OPERATE_DELET: 
		{
			delete(status.getPkgName(), true);
			break;
		}
		case OPERATE_UPDATE_YTPE: 
		{
			updateType(status, true);
			break;
		}
		default:
			break;
		}
	}
	
	public void insert(Status status) 
	{
		Bundle bundle = new Bundle();
		bundle.putInt(STR_OPERATE, OPERATE_INSERT);
		bundle.putSerializable(STR_STATUS, new Status(status));
		mOperateList.add(bundle);
		mHandler.obtainMessage(0).sendToTarget();
	}

	private void insert(Status status, boolean dbOprate) 
	{
		Status temp = query(status.getPkgName());
		if (temp != null)
		{
			LogUtil.i(LogUtil.TAG, " insert packagename is exist ");
			return;
		}
		LogUtil.i(LogUtil.TAG, "insert packagename: " + status.getPkgName());
		mDatabase.execSQL("INSERT INTO status(packagename, type) VALUES(?, ?)",
				new Object[] { status.getPkgName(), status.getType()});
	}

	public void delete(String packagename) 
	{
		Bundle bundle = new Bundle();
		bundle.putInt(STR_OPERATE, OPERATE_DELET);
		Status status = new Status();
		status.setPkgName(packagename);
		status.setType(1);
		bundle.putSerializable(STR_STATUS, new Status(status));
		mOperateList.add(bundle);
		mHandler.obtainMessage(0).sendToTarget();
	}

	private void delete(String packagename, boolean dbOprate) 
	{
		LogUtil.i(LogUtil.TAG, "delete packagename: " + packagename);
		mDatabase.execSQL("DELETE FROM status WHERE packagename=?", 
				new Object[] {packagename});
	}
	
	public void updateType(Status status) 
	{
		Bundle bundle = new Bundle();
		bundle.putInt(STR_OPERATE, OPERATE_UPDATE_YTPE);
		bundle.putSerializable(STR_STATUS, status);
		mOperateList.add(bundle);
		mHandler.obtainMessage(0).sendToTarget();
	}

	private void updateType(Status status, boolean dbOprate) 
	{
		LogUtil.i(LogUtil.TAG, "update packagename: " 
			+ status.getPkgName() + ", type: " + status.getType());
		mDatabase.execSQL("UPDATE status SET type=? WHERE packagename=?",
				new Object[] { status.getType(), status.getPkgName() });
	}
	
	public Status query(String packagename) 
	{
		Cursor c = mDatabase.rawQuery(
				"SELECT * FROM status WHERE packagename=?",
				new String[] { packagename });

		if (c == null)
			return null;

		Status status = null;
		if (c.moveToNext())
			status = new Status(c.getString(0), c.getInt(1));
		c.close();

		return status;
	}
}
