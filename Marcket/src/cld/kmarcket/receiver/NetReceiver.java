package cld.kmarcket.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import cld.kmarcket.R;
import cld.kmarcket.util.CommonUtil;
import cld.kmarcket.util.NetUtil;

public class NetReceiver extends BroadcastReceiver 
{
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction()))
		{
			if (!NetUtil.isNetAvailable(context))
			{
				/*CommonUtil.showToast(context.getResources().getString(
						R.string.toast_net_error));*/
				//CommonUtil.makeText(R.string.toast_net_error);
			}
		}
	}
}
