/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: CollectGPSThread.java
 * @Prject: KCloudCenter_M550
 * @Package: cld.navi.position.model
 * @Description: GPS信息收集线程
 * @author: zhaoqy
 * @date: 2016年8月15日 上午11:25:08
 * @version: V1.0
 */

package cld.navi.position.model;

import cld.kcloud.custom.manager.KCloudPositionManager;
import cld.navi.position.data.LocData;
import cld.navi.position.frame.GpsDataParam;
import cld.navi.util.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class CollectGPSThread extends Thread {

	public CollectHandler mCollectHandler = null;
	public ReportDataQueue mReportDataQueue = null;
	// private IGetParamFromNavi mIGetParamFromNavi=null;

	public final static int MSG_GPS_START = 100;
	public final static int MSG_GPS_QUIT = MSG_GPS_START + 1;
	public final static int MSG_GPS_ENTER_QUEUE = MSG_GPS_QUIT + 1;

	public boolean isActive = false;// 线程是否可用
	private boolean isWriteLog = false;
	private GetRuidCallback mGetRuidCallback = null;

	public CollectGPSThread(ReportDataQueue reportDataQueue,
			GetRuidCallback getRuidCallback) {

		mReportDataQueue = reportDataQueue;
		mGetRuidCallback = getRuidCallback;
		isWriteLog = KCloudPositionManager.getInstance().getIsWriteLog();
	}

	@Override
	public void run() {
		Looper.prepare();// 创建消息队列
		mCollectHandler = new CollectHandler();
		mCollectHandler.sendEmptyMessage(MSG_GPS_START);// 开始
		Looper.loop();// 消息循环
	}

	// GPS搜集Handler
	public class CollectHandler extends Handler {
		public boolean isActive = false;

		public CollectHandler() {
			super();
			isActive = true;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// 开始
			case MSG_GPS_START: {
				isActive = true;
			}
				break;
			case MSG_GPS_ENTER_QUEUE:// 添加数据
			{
				if (isActive) {
					LocData data = (LocData) msg.obj;
					GpsDataParam value = getRuidXY();
					if (data != null && value != null && value.roaduid > 0) {
						data.setLocX(value.x);
						data.setLocY(value.y);
						data.setRoadId(value.roaduid);

						if (value.speed > 0)
							data.setSpeed(value.speed);

						if (value.derection > 0)
							data.setDerection(value.derection);

						if (value.high > 0)
							data.setHigh(value.high);
						// 本来想用导航端GPS时间，但无奈不好用，就不用了
						// if(!MainApplication.getInstance().getIsReadLog())
						// {
						// data.setUtcTime(value.time);
						// }

					}

					if (mReportDataQueue != null) {

						FileUtils.logOut(
								"CollectGPSThread-run->" + data.toString(),
								isWriteLog);
						mReportDataQueue.enterQueue(data);// 进队列
					}
				}
			}
				break;
			case MSG_GPS_QUIT:// 停止
				getLooper().quit();
				isActive = false;
				freeRefrence();
				break;
			}
		}
	}

	public boolean sendEmptyMessage(int msg) {

		if (mCollectHandler != null) {
			mCollectHandler.sendEmptyMessage(msg);
			return true;
		}
		return false;
	}

	public boolean sendMessage(int msg, Object obj) {
		if (mCollectHandler != null) {
			Message mMsg = mCollectHandler.obtainMessage(msg, obj);
			mMsg.sendToTarget();
			return true;
		}
		return false;
	}

	/**
	 * 获取道路ID
	 */
	public GpsDataParam getRuidXY() {
		int ruid = -1;
		GpsDataParam value = null;
		if (mGetRuidCallback != null) {
			value = mGetRuidCallback.getRuidXY();
		}
		return value;
	}

	public void freeRefrence() {
		mReportDataQueue = null;
		mGetRuidCallback = null;
	}

	public interface GetRuidCallback {
		GpsDataParam getRuidXY();
	}

}
