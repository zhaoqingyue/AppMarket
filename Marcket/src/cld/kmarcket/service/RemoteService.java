package cld.kmarcket.service;

import cld.kcloud.service.aidl.IKCloudService;
import cld.kmarcket.util.LogUtil;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

public class RemoteService 
{
	private static RemoteService mInstance = null;
	private IKCloudService mIKCloudService;
	private Context mContext;
	
	public static RemoteService getInstance()
	{
		if(mInstance == null)
		{
			synchronized(RemoteService.class)
			{
				if(mInstance == null)
				{
					mInstance = new RemoteService();
				}
			}
		}
		return mInstance;
	}
	
	public void bindService(Context context)
    {
		mContext = context;
    	//创建所需要绑定的Service的Intent   
        Intent intent = new Intent();   
        intent.setAction("cld.kcloud.center.aidl.service");   
        //绑定远程的服务   
        context.bindService(intent, mConn, Service.BIND_AUTO_CREATE);
    }
	
	private ServiceConnection mConn = new ServiceConnection() 
	{   
        @Override   
        public void onServiceDisconnected(ComponentName name) 
        {   
        	mIKCloudService = null;   
        }   
   
        @Override   
        public void onServiceConnected(ComponentName name, IBinder service) 
        {   
            //获取远程Service的onBinder方法返回的对象代理   
        	mIKCloudService = IKCloudService.Stub.asInterface(service);   
        }   
    }; 
	
    //打开应用时，调用result
	public String getKldLoginResult()
	{
		String result = "";
		try 
		{
			if (mIKCloudService != null)
			{
				result = mIKCloudService.get_KLD_login_result();
			}
		} 
	    catch (RemoteException e) 
	    {
			e.printStackTrace();
		}
		LogUtil.i(LogUtil.TAG, "result: " + result);
		return result;
	}
	
	public void unbindService()
	{
		if (mContext != null)
		{
			mContext.unbindService(mConn);   
		}
	}
	
	public void static_release()
	{
		if(mInstance != null)
		{
			mInstance = null;
		}
	}
}
