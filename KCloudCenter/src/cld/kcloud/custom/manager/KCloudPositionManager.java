package cld.kcloud.custom.manager;

import com.cld.log.CldLog;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Environment;
import cld.kcloud.center.KCloudAppConfig;
import cld.kcloud.center.KCloudCtx;
import cld.navi.util.DeviceUtils;
import cld.navi.util.FileUtils;
import cld.navi.util.NetWorkRequest;

public class KCloudPositionManager {

	private final String TAG = "KCloudPositionManager";
	private NetWorkRequest netRequest;
	private SharedPreferences pref;
	private ConnectivityManager mConnectivityManager;
	private boolean isWriteLog;
	private boolean isTestServer;
	private boolean isReadLog;
	private String naviPath = null;
	private Context mContext = null;
	private static KCloudPositionManager mKCloudPositionManager = null;
	
	public KCloudPositionManager() 
	{
		mContext = KCloudCtx.getAppContext();
	}

	public static KCloudPositionManager getInstance() 
	{
		if (mKCloudPositionManager == null) 
		{
			synchronized(KCloudPositionManager.class) 
			{
				if (mKCloudPositionManager == null) 
				{
					mKCloudPositionManager = new KCloudPositionManager();
				}
			}
		}
		return mKCloudPositionManager;
	}
	
	@SuppressWarnings("static-access")
	public void init() 
	{
		if (!KCloudAppConfig.open_position_port)
			return;
		
		//只有开放位置上报服务，才走下面的流程
		CldLog.d(TAG, "init");
		naviPath = getNaviPath();
		pref = mContext.getSharedPreferences("MrrTalk", mContext.MODE_PRIVATE);
		isWriteLog = FileUtils.writeLogFile();
		isReadLog = FileUtils.readLogFile();
		isTestServer = FileUtils.readServeFile();
		setIsWriteLog(isWriteLog);
		netRequest = new NetWorkRequest();
		mConnectivityManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
	}
	
	public boolean getIsReadLog()
	{
		return isReadLog;
	}
	
	public void setIsReadLog(boolean b)
	{
		isReadLog = b;
	}
	
    public boolean getIsWriteLog()
    {
    	return isWriteLog;
    }
    
    public boolean getIsTestServer()
    {
    	return isTestServer;
    }
    
    public void setIsTestServer(boolean b)
    {
    	isTestServer = b;
    }
    
    public void setIsWriteLog(boolean b)
    {
    	isWriteLog = b;
    }
    
	public NetWorkRequest getNetRequest()
	{
		return netRequest;
	}
	
	public SharedPreferences getSharedPreferences()
	{
		return pref;
	}
	
	public ConnectivityManager getConnectivityManager()
	{
		return mConnectivityManager;
	}
	
	public String getPath()
	{
		return naviPath;
	}
	
	private String getNaviPath()
	{
		String result = null;
        for(String path : DeviceUtils.paths){
        	if(DeviceUtils.isNaviCardExists(path))
        	{
        		result = path;
        		break;
        	}
        }
        
        if(result == null || (DeviceUtils.isNeedChangeParameterDirectory && 
        		android.os.Build.VERSION.SDK_INT>=19))
        {
        	result = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        CldLog.d(TAG, "naviPath: " + result);
        return result;
	}
}
