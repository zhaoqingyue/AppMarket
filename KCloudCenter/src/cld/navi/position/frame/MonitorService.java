/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: MonitorService.java
 * @Prject: KCloudCenter_M550
 * @Package: cld.navi.position.frame
 * @Description: 监控服务类，用于监控主服务是否在运行
 * @author: zhaoqy
 * @date: 2016年8月15日 上午9:41:32
 * @version: V1.0
 */

package cld.navi.position.frame;

import java.util.Timer;
import java.util.TimerTask;

import cld.kcloud.custom.manager.KCloudPositionManager;
import cld.kcloud.utils.KCloudCommonUtil;
import cld.navi.util.FileUtils;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class MonitorService extends Service {

	//位置上报服务的类名，用于判断服务是否在运行
	public static String SERVICE_CLASS_NAME = "cld.navi.position.frame.MainService";
	private Timer mTimer = null;
	private TimerTask mTimerTask = null;
	private boolean isLogToFile = false;
	private MonitorServiceReceiver mMonitorServiceReceiver = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// return super.onStartCommand(intent, START_STICKY, startId);
		return START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		//KCloudCommonUtil.makeText("MonitorService onCreate");

		mMonitorServiceReceiver = new MonitorServiceReceiver();
		IntentFilter mFilter = new IntentFilter(
				MonitorServiceReceiver.ACTION_STOPSELF);
		registerReceiver(mMonitorServiceReceiver, mFilter);

		mTimer = new Timer();
		mTimerTask = new TimerTask() {

			@SuppressLint("WorldReadableFiles") 
			@Override
			public void run() {
				boolean isSerExist = MainService.isServiceRunning(
						MonitorService.this, SERVICE_CLASS_NAME);
				FileUtils.logOut("MonitorService-ACTION_TIME_TICK-isMainExist="
						+ isSerExist, isLogToFile);
				if (!isSerExist) {
					Log.d("CldNavi", "MonitorService 1111111");
					@SuppressWarnings("deprecation")
					SharedPreferences share = getApplicationContext()
							.getSharedPreferences("IsServiceAutoStart",
									Context.MODE_WORLD_READABLE);
					if (null != share) {
						boolean bIsAutoStart = share.getBoolean("flag", true);
						Log.d("CldNavi", "MonitorService bIsAutoStart:"
								+ bIsAutoStart);

						if (true == bIsAutoStart) {
							startMainService();
						}
					}
				}
			}
		};

		isLogToFile = KCloudPositionManager.getInstance().getIsWriteLog();
		if (mTimer != null && mTimerTask != null)
			mTimer.schedule(mTimerTask, 3000, 3000);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mTimerTask.cancel();
		mTimerTask = null;
		mTimer = null;

		if (null != mMonitorServiceReceiver) {
			unregisterReceiver(mMonitorServiceReceiver);
		}
	}

	private void startMainService() {
		Intent mIntent = new Intent(MonitorService.this, MainService.class);
		// mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		MonitorService.this.startService(mIntent);
	}

	private class MonitorServiceReceiver extends BroadcastReceiver {
		static final String ACTION_STOPSELF = "CLD.NAVI.ACTION_STOPSELF"; //M330

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action != null && action.equals(ACTION_STOPSELF)) {
				Log.d("CldNavi", "MonitorServiceReceiver action:"
						+ ACTION_STOPSELF);
				MonitorService.this.stopSelf();
			}
		}
	}
}
