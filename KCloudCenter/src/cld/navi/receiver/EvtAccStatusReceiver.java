package cld.navi.receiver;

import com.cld.log.CldLog;
import cld.kcloud.center.KCloudAppConfig;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class EvtAccStatusReceiver extends BroadcastReceiver {
	
	public static final String TAG = "EvtAccStatusReceiver";

	@SuppressLint("WorldWriteableFiles") 
	@Override
	public void onReceive(Context context, Intent intent) {
		
		CldLog.i(TAG, "onReceive EvtAccStatus");
		if (!KCloudAppConfig.open_position_port)
			return;
		
		if (null != intent && intent.getAction().equals("com.stcloud.drive.EVT_ACC_STATUS")) {
			String status = intent.getStringExtra("acc");
			CldLog.i(TAG, "MainService Receive action:com.stcloud.drive.EVT_ACC_STATUS status:" + status);

			if ("1".equals(status))
			{
				@SuppressWarnings("deprecation")
				SharedPreferences share = context.getSharedPreferences(
						"IsServiceAutoStart", Context.MODE_WORLD_WRITEABLE);
				CldLog.i(TAG, "MainService status=1");

				if (null != share) {
					SharedPreferences.Editor editor = share.edit();
					editor.putBoolean("flag", true);
					editor.commit();
					CldLog.i(TAG, "MainService editor putBoolean flag:true");
				}

				Intent mIntent = new Intent("cld.navi.mirrtalk.startService");
				mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startService(mIntent);
			}
			else if ("0".equals(status))
			{
				@SuppressWarnings("deprecation")
				SharedPreferences share = context.getSharedPreferences(
						"IsServiceAutoStart", Context.MODE_WORLD_WRITEABLE);
				CldLog.i(TAG, "MainService status=0");

				if (null != share) {
					SharedPreferences.Editor editor = share.edit();
					editor.putBoolean("flag", false);
					editor.commit();
					CldLog.i(TAG, "MainService editor putBoolean flag:false");
				}

				Intent mIntent = new Intent("CLD.NAVI.ACTION_STOPSELF");
				context.sendBroadcast(mIntent);
			}
		}
	}
}
