/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: BootReceiver.java
 * @Prject: KCloudCenter_M550
 * @Package: cld.navi.receiver
 * @Description: 开机广播接收器
 * @author: zhaoqy
 * @date: 2016年8月11日 下午3:34:34
 * @version: V1.0
 */

package cld.navi.receiver;

import com.cld.log.CldLog;
import cld.kcloud.center.KCloudAppConfig;
import cld.navi.position.frame.MainService;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver
{
	public static final String TAG = "BootReceiver";
	public static final String ACC_ON = "cn.flyaudio.action.ACCON";
	public static final String NAVI_MIRRTALK = "cld.navi.mirrtalk.startService";

	@SuppressLint("NewApi") 
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		CldLog.i(TAG, "onReceive BOOT_COMPLETED");
		if (!KCloudAppConfig.open_position_port)
			return;
		
		String action =intent.getAction();
		if (action != null && !action.isEmpty())
		{
			if(action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(ACC_ON))
			{
				CldLog.i(TAG, "start Main Service");
				//Intent mIntent = new Intent(NAVI_MIRRTALK);
				Intent mIntent = new Intent(context, MainService.class);
				mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startService(mIntent);
			}
		}
	}
}
