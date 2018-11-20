/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: ShowDisclaimerReceiver.java
 * @Prject: KCloudCenter_M550
 * @Package: cld.navi.receiver
 * @Description: 特别提示
 * @author: zhaoqy
 * @date: 2016年8月11日 下午4:50:49
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
import android.content.SharedPreferences;

public class ShowDisclaimerReceiver extends BroadcastReceiver
{
	public static final String TAG = "ShowDisclaimerReceiver";
	
	@SuppressLint("WorldWriteableFiles") @SuppressWarnings("deprecation")
	@Override
	public void onReceive(Context context, Intent intent)
	{
		CldLog.i(TAG, "onReceive ShowDisclaimer");
		if (!KCloudAppConfig.open_position_port)
			return;

		if (null != intent
			&& intent.getAction().equals("com.txznet.launcher.Disclaimer.click")) {
			MainService.bIsReceiveShowDisclaimerBrcast = true;
			// show_disclaimer true:表示下次显示特别提示 false:表示下次不再显示特别提示
			boolean show_disclaimer = intent.getBooleanExtra("show_disclaimer", false);

			SharedPreferences share = context.getSharedPreferences(
					"ShowDisclaimer", Context.MODE_WORLD_WRITEABLE);
			if (null != share) {
				SharedPreferences.Editor editor = share.edit();
				editor.putBoolean("show_disclaimer", show_disclaimer);
				editor.commit();
			}
			CldLog.i(TAG,"ShowDisclaimerReceiver show_disclaimer: "+ show_disclaimer);
		}
	}
}
