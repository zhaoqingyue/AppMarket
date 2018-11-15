package cld.kmarcket.util;

import java.util.HashMap;
import com.javy.gesture.GestureUtils;
import com.zhonghong.conn.ConnZui.InitListener;
import com.zhonghong.conn.ZHRequest;
import com.zhonghong.jarmain.ZHCar;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import cld.kmarcket.KMarcketApplication;

public class LauncherUtil 
{
	private static final String TAG = "LauncherUtil";
	private static Context mContext;
	private static boolean mInitSuc = false;
	
	public static void init()
	{
		if (mInitSuc)
			return;
		
		mInitSuc = true;
		mContext = KMarcketApplication.getContext();
		ZHCar.getInstance().init(mContext, mZhInitListener);
	}
	
	public static void onLauncherUpgradeStart()
	{
		//关闭语音、关闭手势、静音
		exitVoiceRobot();
		sleepGesture();
		//调用众鸿静音接口
		if (getInitResult())
		{
			mute(true);
		}
	}
	
	/**
	 * 恢复手势、恢复音量、重启(恢复手势、恢复音量 在重启之后做，避免重启之前有短暂的声音)
	 */
	public static void onLauncherUpgradeFinish()
	{
		//launcher_status: 0-未升级； 1-升级成功
		ShareUtil.put("launcher_status", 1);
		reboot();
	}
	
	/**
	 * 重启
	 */
	public static void reboot()
	{
        Intent reboot = new Intent(Intent.ACTION_REBOOT);  
        reboot.putExtra("nowait", 1);  
        reboot.putExtra("interval", 1);  
        reboot.putExtra("window", 0);  
        mContext.sendBroadcast(reboot);      
	}
	
	/**
	 * 发送关闭语音广播
	 */
	public static void exitVoiceRobot()
	{
		Intent intent = new Intent(ConstantUtil.ACTION_VOICE_ROBOT_EXIT);
		mContext.sendBroadcast(intent);
	}
	
	/**
	 * 唤醒手势
	 */
	public static void wakeupGesture()
	{
		GestureUtils gestureUtils = GestureUtils.getInstance();
		if (gestureUtils != null) 
		{
			gestureUtils.setGestureWakeup();
		}
	}
	
	/**
	 * 关闭手势
	 */
	public static void sleepGesture()
	{
		GestureUtils gestureUtils = GestureUtils.getInstance();
		if (gestureUtils != null) 
		{
			gestureUtils.setGestureSleep();
		}
	}
	
	static HashMap<String, String> mRequestData = new HashMap<String, String>();
	
	/**
	 * 声音控制
	 * @param mute "true" 静音 , "false" 解除静音
	 * @return
	 */
	public static String mute(boolean mute)
	{
		mRequestData.clear();
		mRequestData.put("req", "setmute");
		mRequestData.put("val", mute ? "true":"false");
		String retString = ZHRequest.getInstance().httpPostFormAidl("/system", mRequestData);
		Log.d(TAG, "mute = " + mute + ", retString = " + retString);
		return retString;
	}
	
	/**
	 * 初始化状态监听
	 */
	static InitListener mZhInitListener = new InitListener() 
	{
		@Override
		public void state(int state, String content) 
		{
			switch(state)
			{
			case InitListener.STATE_SUCCESS:
			{
				//测试发送， 必须在初始化成功后才能发送，其实就是简单的一个aidl连接状态
				mInitSuc = true;
				Log.d(TAG, " mZhInitListener Success ");
				break;
			}
			case InitListener.STATE_FAIL:
				break;
			case InitListener.STATE_RECONN:
				break;
			} 
		}
	}; 
	
	public static boolean getInitResult()
	{
		Log.d(TAG, " mInitSuc: " + mInitSuc);
		return mInitSuc;
	}
	
	public static void setStreamMute(Context context)
	{
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		ShareUtil.put("valume", volume);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_PLAY_SOUND);  
	}
	
	public static void resetStreamVolume(Context context)
	{
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		int volume = getStreamVolume(context);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_PLAY_SOUND);  
	}
	
	public static int getStreamVolume(Context context)
	{
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		int defvolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		return ShareUtil.getInt("valume", defvolume);
	}
}
