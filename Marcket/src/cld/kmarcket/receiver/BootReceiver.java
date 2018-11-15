package cld.kmarcket.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import cld.kmarcket.service.CheckNewService;
import cld.kmarcket.util.LauncherUtil;
import cld.kmarcket.util.LogUtil;
import cld.kmarcket.util.ShareUtil;

public class BootReceiver extends BroadcastReceiver 
{
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
		{
			LogUtil.d(LogUtil.TAG, "ACTION_BOOT_COMPLETED");
			if (ShareUtil.getInt("launcher_status", 0) == 1)
			{
				ShareUtil.put("launcher_status", 0);
				//Laucher升级成功后，恢复手势、恢复音量
				LauncherUtil.wakeupGesture();
				//初始化成功后，才可以调用接口
				if (LauncherUtil.getInitResult())
				{
					LauncherUtil.mute(false);
				}
				else
				{
					LauncherUtil.init();
					mHandler.sendEmptyMessageDelayed(0, 1000);
				}
			}
		
			Intent checkintent = new Intent(context, CheckNewService.class);   
	        context.startService(checkintent);
		}
	}
	
	@SuppressLint("HandlerLeak") 
	private Handler mHandler = new Handler() 
	{
		public void handleMessage(Message msg) 
		{
			switch (msg.what) 
			{
			case 0:
			{
				if (LauncherUtil.getInitResult())
				{
					LauncherUtil.mute(false);
				}
				else
				{
					mHandler.sendEmptyMessageDelayed(0, 1000);
				}
				break;
			}
			default:
				break;
			}
		};
	};
}
