package cld.kcloud.custom.manager;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import cld.kcloud.center.KCloudAppConfig;
import cld.kcloud.center.KCloudAppUtils;
import cld.kcloud.center.KCloudCtx;
import cld.kcloud.custom.bean.KCloudInstalledInfo;
import cld.kcloud.database.KCloudInstalledTable;
import cld.kcloud.utils.KCloudLocationUtils;
import cld.kcloud.utils.KCloudLocationUtils.IKCloudLocationListener;
import cld.kcloud.utils.KCloudNetworkUtils;
import cld.kcloud.utils.KCloudRegionUtils;
import cld.kcloud.utils.KCloudUIUtils;
import cld.kmarket.download.QuiesceDownloadManager;
import com.cld.device.CldPhoneNet;
import com.cld.log.CldLog;
import com.cld.ols.tools.CldOlsThreadPool;

public class KCloudUpgradeManager 
{
	private final String TAG = "KCloudUpgradeManager";
	private Context mContext;
	private ArrayList<KCloudInstalledInfo> mUpgradeList = new ArrayList<KCloudInstalledInfo>();
	private String mLauncherVer = "";
	private int mRegionId = 0;
	private ArrayList<KCloudInstalledInfo> mInstalledInfoList = null;
	private static KCloudUpgradeManager mKCloudUpgrade = null;
	
	public static KCloudUpgradeManager getInstance(Context context) 
	{
		if (mKCloudUpgrade == null) 
		{
			synchronized(KCloudUpgradeManager.class) 
			{
				if (mKCloudUpgrade == null) 
				{
					mKCloudUpgrade = new KCloudUpgradeManager(context);
				}
			}
		}
		return mKCloudUpgrade;
	}
	
	public KCloudUpgradeManager(Context context)
	{
		mContext = context;
		mLauncherVer = KCloudUIUtils.getApkVersion(mContext, KCloudAppUtils.LAUNCHER_PACKAGE_NAME);
	}
	
	public interface IKCloudUpgradeListener 
	{
		void onResult(String jsonString);
	}
	
	public void init() 
	{
		if (!KCloudAppConfig.open_upgrade_port)
			return;
		
		//只有开放应用升级接口，才走下面的流程
		CldLog.i(TAG, "init ");
		mHandler.sendEmptyMessage(0);
	}
	
	private void getInstalledInfoList()
	{
		if (mInstalledInfoList != null)
			mInstalledInfoList.clear();
		
		mInstalledInfoList = KCloudInstalledTable.getInstance().queryInstalledInfos();
		if (mInstalledInfoList != null && mInstalledInfoList.isEmpty())
		{
			KCloudInstalledTable.getInstance().addDefaultInstalledInfos();
			mInstalledInfoList = KCloudInstalledTable.getInstance().queryInstalledInfos();
		}
	}
	
	/**
	 * 
	 * @Title: configureTest
	 * @Description: 配置测试
	 * @return: void
	 */
	@SuppressLint("NewApi") 
	private void configureTest()
	{
		CldLog.i(TAG, " configureTest ");
		//KCloudAppConfig.plan_code = 100100;  // 方案商编号
		//KCloudAppConfig.device_dpi = 160;    // 设备分辨率
		//KCloudAppConfig.system_ver = "4.4";  //android系统版本	
		
		@SuppressWarnings("static-access")
		SharedPreferences sp = mContext.getSharedPreferences("configure", mContext.MODE_PRIVATE);
		KCloudAppConfig.plan_code = sp.getInt("plan_code", -1);
		if (KCloudAppConfig.plan_code == -1)
		{
			SharedPreferences.Editor editor = sp.edit();
			editor.putInt("plan_code", 100100);
			editor.commit();
			KCloudAppConfig.plan_code = sp.getInt("plan_code", -1);
		}
		
		KCloudAppConfig.device_dpi = sp.getInt("device_dpi", -1);
		if (KCloudAppConfig.device_dpi == -1)
		{
			SharedPreferences.Editor editor = sp.edit();
			editor.putInt("device_dpi", 160);
			editor.commit();
			KCloudAppConfig.device_dpi = sp.getInt("device_dpi", -1);
		}
		
		KCloudAppConfig.system_ver = sp.getString("system_ver", "");
		if (KCloudAppConfig.system_ver.isEmpty())
		{
			SharedPreferences.Editor editor = sp.edit();
			editor.putString("system_ver", "4.4");
			editor.commit();
			KCloudAppConfig.system_ver = sp.getString("system_ver", "");
		}
		
		CldLog.i(TAG, " plan_code: " + KCloudAppConfig.plan_code);
		CldLog.i(TAG, " device_dpi: " + KCloudAppConfig.device_dpi);
		CldLog.i(TAG, " system_ver: " + KCloudAppConfig.system_ver);
	}
	
	private void initCurAddr() 
	{
		KCloudLocationUtils.startLocation(new IKCloudLocationListener() 
		{
			@Override
			public void onLocation(double latitude, double longtitude) 
			{
				CldLog.i(TAG, "latitude: " + latitude + ", longtitude: " + longtitude);
				KCloudRegionUtils.getRegionDistsName(longtitude, latitude,
						new KCloudRegionUtils.IGetRigonCallback() 
						{
							@SuppressLint("NewApi")
							@Override
							public void onResult(int regionId,
									String provinceName, String cityName,
									String distsName) {
								CldLog.i(TAG, "regionId: " + regionId);
								mRegionId = regionId;
								//深圳区域regionId: 440300
								mHandler.sendEmptyMessage(1);
							}
						});
			}
		});
	}
	
	private void startUpgradeListRunning() 
	{
		CldOlsThreadPool.submit(new Runnable() 
		{
			@Override
			public void run() 
			{
				getInstalledInfoList();
				//configureTest();
				KCloudNetworkUtils.getKGoUpgradeList(mLauncherVer, mRegionId,
						mInstalledInfoList,  
						new IKCloudUpgradeListener()
				{
					@SuppressLint("NewApi") 
					@Override
					public void onResult(String jsonString) 
					{
						CldLog.i(TAG, "jsonString = " + jsonString);
						if (jsonString != null && !jsonString.isEmpty())
						{
							try 
							{
								JSONObject jsonObject = new JSONObject(jsonString);
								if (jsonObject.getInt("errcode") == 0) 
								{
									parseUpgradeListResult(jsonString);
									checkUpgradeListResult();
								}
							} 
							catch (JSONException e) 
							{
								e.printStackTrace();
							}
						}
					}
				});
			}
		});
	}
	
	private void checkUpgradeListResult() 
	{
		if (mUpgradeList != null && !mUpgradeList.isEmpty())
		{
			CldLog.i(TAG, "checkUpgradeListResult size: " + mUpgradeList.size());
			for (int i=0; i<mUpgradeList.size(); i++)
			{
				KCloudInstalledInfo appInfo = mUpgradeList.get(i);
				QuiesceDownloadManager.getInstance(mContext).startQuiesceDownload(appInfo);
			}
		}
	}
	
	private Handler mHandler = new Handler(KCloudCtx.getAppContext().getMainLooper())
	{
		public void handleMessage(android.os.Message msg) 
		{
			switch (msg.what) 
			{
			case 0:
			{
				if (!CldPhoneNet.isNetConnected())
				{
					mHandler.sendEmptyMessageDelayed(0, 30*1000);
				}
				else
				{
					initCurAddr();
				}
				break;
			}
			case 1:
			{
				startUpgradeListRunning();
				//60分钟获取一次
				mHandler.sendEmptyMessageDelayed(0, /*60*/5*60*1000); //(测试5分钟)
				break;
			}
			default:
				break;
			}
		};
	};
	
	@SuppressLint("NewApi") 
	public KCloudInstalledInfo getInstalledInfo(String pkgname)
	{
		if (pkgname == null || pkgname.isEmpty())
			return null;
		
		if (mUpgradeList != null && !mUpgradeList.isEmpty())
		{
			for (int i=0; i<mUpgradeList.size(); i++)
			{
				if (pkgname.equals(mUpgradeList.get(i).getPkgName()))
				{
					return mUpgradeList.get(i);
				}
			}
		}
		return null;
	}
	
	/**
	 * 解析应用升级接口返回的数据
	 * @param jsonStr 从服务器端得到的JSON字符串数据
	 * @return
	 */
	private void parseUpgradeListResult(String jsonString) 
	{
		if (mUpgradeList != null && !mUpgradeList.isEmpty())
			mUpgradeList.clear();
		
		try 
		{
			//将jsonStr字符串转换为json对象
			JSONObject jsonObj = new JSONObject(jsonString);
			JSONArray jsonArray = null;
			if (jsonObj.has("data")) 
			{
				//得到指定json key对象的value对象
				jsonArray = jsonObj.getJSONArray("data");
			}
			
			if (jsonArray != null)
			{
				//遍历jsonArray
				for (int i=0; i<jsonArray.length(); i++) 
				{
					KCloudInstalledInfo installedInfo = new KCloudInstalledInfo(); 
					//获取每一个json对象
					JSONObject jsonItem = jsonArray.getJSONObject(i);
					//获取对象的所有属性
					if(jsonItem.has("pack_name"))
					{
						installedInfo.setPkgName(jsonItem.getString("pack_name"));
					}
					if(jsonItem.has("app_name"))
					{
						installedInfo.setAppName(jsonItem.getString("app_name"));
					}
					if(jsonItem.has("app_icon"))
					{
						installedInfo.setAppIconUrl(jsonItem.getString("app_icon"));
					}
					if(jsonItem.has("app_url"))
					{
						installedInfo.setAppUrl(jsonItem.getString("app_url").trim());
					}
					if(jsonItem.has("upgrade_desc"))
					{
						installedInfo.setUpgradeDesc(jsonItem.getString("upgrade_desc").trim());
					}
					if(jsonItem.has("ver_name"))
					{
						installedInfo.setVerName(jsonItem.getString("ver_name"));
					}
					if(jsonItem.has("ver_code"))
					{
						installedInfo.setVerCode(jsonItem.getInt("ver_code"));
					}
					if(jsonItem.has("pack_size"))
					{
						installedInfo.setPackSize(jsonItem.getInt("pack_size"));
					}
					if(jsonItem.has("quiesce"))
					{
						installedInfo.setQuiesce(jsonItem.getInt("quiesce"));
					}
					if(jsonItem.has("down_times"))
					{
						installedInfo.setDownTimes(jsonItem.getInt("down_times"));
					}
					mUpgradeList.add(installedInfo);
				}
			}
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
	}
}
