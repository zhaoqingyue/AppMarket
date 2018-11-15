package com.download.api;

import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

@SuppressLint("NewApi")
public class InfoDao 
{
	private static InfoDao mInstance = null;
	public static InfoDao getInstance(Context context) 
	{
		if (mInstance == null) 
		{
			synchronized (InfoDao.class) 
			{
				if (mInstance == null) 
				{
					mInstance = new InfoDao(context);
				}
			}
		}
		return mInstance;
	}

	private SQLiteDatabase mDatabase;
	private ArrayList<Bundle> mOperateList = new ArrayList<Bundle>(128);
	private ArrayList<Bundle> mOperateDoList = new ArrayList<Bundle>(128);

	static final String STR_OPERATE = "OPERATE";
	static final String STR_INFO = "INFO";
	static final int OPERATE_INSERT = 1;
	static final int OPERATE_DELET = 2;
	static final int OPERATE_UPDATE = 3;
	static final int OPERATE_DELETALL = 4;
	static final int OPERATE_CLEARALL = 5;

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

	private InfoDao(Context context) 
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
		Info info = (Info) bundle.getSerializable(STR_INFO);
		if (opreate == 0 || info == null)
			return;

		switch (opreate) 
		{
		case OPERATE_INSERT: 
		{
			insert(info, true);
			break;
		}
		case OPERATE_DELET: 
		{
			delete(info.getPath(), info.getThreadId(), true);
			break;
		}
		case OPERATE_UPDATE: 
		{
			update(info, true);
			break;
		}
		case OPERATE_DELETALL: 
		{
			deleteAllIfDownloadOk(info.getPath(), info.getDone(), true);
			break;
		}
		case OPERATE_CLEARALL: 
		{
			clearAll(info.getPath(), true);
			break;
		}
		default:
			break;
		}
	}

	public void insert(Info info) 
	{
		if (query(info.getPath(), info.getThreadId()) != null)
		{
			//卸载后重新下载会报错 add by zhaoqy 2016-5-23
			delete(info.getPath(), info.getThreadId());
		}
		
		Bundle bundle = new Bundle();
		bundle.putInt(STR_OPERATE, OPERATE_INSERT);
		bundle.putSerializable(STR_INFO, new Info(info));
		mOperateList.add(bundle);
		mHandler.obtainMessage(0).sendToTarget();
	}

	private void insert(Info info, boolean dbOprate) 
	{
		mDatabase.execSQL(
			"INSERT INTO info(path, thid, start, end, done) VALUES(?, ?, ?, ?, ?)",
			new Object[] { info.getPath(), info.getThreadId(),
						info.getStart(), info.getEnd(), info.getDone()});
	}

	public void delete(String path, int thid) 
	{
		Bundle bundle = new Bundle();
		bundle.putInt(STR_OPERATE, OPERATE_DELET);
		bundle.putSerializable(STR_INFO, new Info(path, thid, 0, 0, 0));
		mOperateList.add(bundle);
		mHandler.obtainMessage(0).sendToTarget();
	}

	private void delete(String path, int thid, boolean dbOprate) 
	{
		mDatabase.execSQL("DELETE FROM info WHERE path=? AND thid=?", 
				new Object[] {path, thid });
	}

	public void update(Info info) {
		Bundle bundle = new Bundle();
		bundle.putInt(STR_OPERATE, OPERATE_UPDATE);
		bundle.putSerializable(STR_INFO, info);
		mOperateList.add(bundle);
		mHandler.obtainMessage(0).sendToTarget();
	}

	private void update(Info info, boolean dbOprate) 
	{
		mDatabase.execSQL(
				"UPDATE info SET done=? WHERE path=? AND thid=?",
				new Object[] { info.getDone(), info.getPath(),
						info.getThreadId() });
	}
	
	public Info query(String path) {
		Cursor c = mDatabase.rawQuery("SELECT * FROM info WHERE path=?",
				new String[] { path });

		if (c == null)
			return null;

		Info info = null;
		if (c.moveToNext())
			info = new Info(c.getString(0), c.getInt(1), 
					c.getLong(2), c.getLong(3), c.getLong(4));
		c.close();

		return info;
	}

	public Info query(String path, int thid) 
	{
		Cursor c = mDatabase.rawQuery(
				"SELECT * FROM info WHERE path=? AND thid=?",
				new String[] { path, String.valueOf(thid) });

		if (c == null)
			return null;

		Info info = null;
		if (c.moveToNext())
			info = new Info(c.getString(0), c.getInt(1), 
					c.getLong(2), c.getLong(3), c.getLong(4));
		c.close();
		return info;
	}

	public void deleteAllIfDownloadOk(String path, long len) 
	{
		Bundle bundle = new Bundle();
		bundle.putInt(STR_OPERATE, OPERATE_DELETALL);
		bundle.putSerializable(STR_INFO, new Info(path, 0, 0, 0, len));
		mOperateList.add(bundle);
		mHandler.obtainMessage(0).sendToTarget();
	}

	private void deleteAllIfDownloadOk(String path, long len, boolean dbOprate)
	{
		Cursor c = mDatabase.rawQuery(
				"SELECT SUM(done) FROM info WHERE path=?",
				new String[] { path });
		if (c.moveToNext()) 
		{
			long result = c.getLong(0);
			if (result == len)
				mDatabase.execSQL("DELETE FROM info WHERE path=? ",
						new Object[] { path });
		}

	}

	public void clearAll(String path) 
	{
		Bundle bundle = new Bundle();
		bundle.putInt(STR_OPERATE, OPERATE_CLEARALL);
		bundle.putSerializable(STR_INFO, new Info(path, 0, 0, 0, 0));
		mOperateList.add(bundle);
		mHandler.obtainMessage(0).sendToTarget();
	}

	private void clearAll(String path, boolean dbOprate) 
	{
		mDatabase.execSQL("DELETE FROM info WHERE path=? ", 
				new Object[] { path });
	}

	public List<String> queryUndone() 
	{
		Cursor c = mDatabase.rawQuery("SELECT DISTINCT path FROM info", null);
		List<String> pathList = new ArrayList<String>();
		while (c.moveToNext())
			pathList.add(c.getString(0));
		c.close();
		return pathList;
	}

	public List<Info> queryUndone(String path) 
	{
		Cursor c = mDatabase.rawQuery("SELECT * FROM info WHERE path=?",
				new String[] { path });
		List<Info> pathList = new ArrayList<Info>();
		while (c.moveToNext())
			pathList.add(new Info(c.getString(0), 
					 c.getInt(1), c.getLong(2), 
					 c.getLong(3), c.getLong(4)));
		c.close();
		return pathList;
	}
}
