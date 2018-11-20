package cld.kcloud.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import cld.kcloud.custom.manager.KCloudSimCardManager;
import cld.kcloud.utils.KCloudCommonUtil;
import com.cld.device.CldPhoneNet;
import com.cld.log.CldLog;

public class KCloudNetBroadcastReceiver extends BroadcastReceiver 
{
	private static final String TAG = "KCloudNetBroadcastReceiver";
   
    @Override
    public void onReceive(Context context, Intent intent) 
    {
    	String action = intent.getAction();
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) 
        {
        	// 网络状态变化
        	if (CldPhoneNet.isNetConnected()) 
        	{
        		CldLog.i(TAG, "NET_CHANGE_ACTION");
        		//网络连接之后，刷新widget
        		KCloudCommonUtil.sendFreshFlowBroadcast(true);
        		
        		KCloudSimCardManager.getInstance().checkSimStatus();
        	}
        }         
    }
}
