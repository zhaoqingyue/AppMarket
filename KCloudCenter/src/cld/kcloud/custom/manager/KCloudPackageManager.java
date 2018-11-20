package cld.kcloud.custom.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import cld.kcloud.center.KCloudAppUtils;
import cld.kcloud.center.KCloudAppUtils.CLDMessageId;
import cld.kcloud.center.KCloudCtx;
import cld.kcloud.center.R;
import cld.kcloud.custom.bean.KCloudAppInfo;
import cld.kcloud.custom.bean.KCloudPackageInfo;
import cld.kcloud.custom.bean.KCloudServiceInfo;
import cld.kcloud.database.KCloudAppTable;
import cld.kcloud.database.KCloudPackageTable;
import cld.kcloud.database.KCloudServiceTable;
import cld.kcloud.user.KCloudUser;
import cld.kcloud.utils.KCloudCommonUtil;
import cld.kcloud.utils.KCloudNetworkUtils;
import cld.kcloud.utils.control.CldMessageDialog;
import cld.kcloud.utils.control.CldMessageDialog.CldMessageDialogListener;
import cld.kcloud.utils.control.CldMessageDialog.CldMessageType;
import com.cld.device.CldPhoneManager;
import com.cld.device.CldPhoneNet;
import com.cld.log.CldLog;
import com.cld.ols.api.CldKAccountAPI;
import com.cld.ols.tools.CldOlsThreadPool;

public class KCloudPackageManager {
	private static final String TAG = "KCloudPackageManager";
	
	public interface IKCloudPackageListener {
		void onResult(String jsonString);
	}
	
	public static final int TASK_NONE = 0;
	public static final int TASK_GETTING = 1;
	public static final int TASK_GETED = 2;
	
	private int mTaskStatus = TASK_NONE;
	private boolean isPackageListSuccess = false;
	private boolean isServiceAppListSuccess = false;
	private boolean mPaySuccess = false;
	private static KCloudPackageManager mKCloudPackage = null;
	private ArrayList<KCloudPackageInfo> mPackageList = new ArrayList<KCloudPackageInfo>();
	private ArrayList<KCloudServiceInfo> mServiceList = new ArrayList<KCloudServiceInfo>();
	private ArrayList<KCloudAppInfo> mAppList = new ArrayList<KCloudAppInfo>();
	
	public static KCloudPackageManager getInstance() {
		if (mKCloudPackage == null) {
			synchronized(KCloudPackageManager.class) {
				if (mKCloudPackage == null) {
					mKCloudPackage = new KCloudPackageManager();
				}
			}
		}
		return mKCloudPackage;
	}
	
	/** 初始化Handler对象 */
	private Handler mHandler = new Handler(KCloudCtx.getAppContext().getMainLooper()) {
		public void handleMessage(Message msg) {
			
			CldLog.i(TAG, String.valueOf(msg.what));
			
			switch (msg.what) {	
			case CLDMessageId.MSG_ID_KGO_GET_PAY_STATUS: {
				String msgText = "";
				Bundle bundle = msg.getData();
				if (bundle != null) {
					msgText = String.format(KCloudCommonUtil.getString(
							R.string.package_tip_purchase_uccess1), 
							bundle.getString("package_name"));
				} else {
					msgText = String.format(KCloudCommonUtil.getString(
							R.string.package_tip_purchase_uccess2));
				}
				 
				CldMessageDialog.showMessageDialog(
						KCloudCtx.getAppContext(), 
						msgText, 
						CldMessageType.eMessageType_Ok_Close, 
						KCloudCommonUtil.getString(R.string.package_tip_see_details),
						new CldMessageDialogListener() {

							@Override
							public void onOk() {
								if (KCloudCommonUtil.isRunBackground(
										KCloudAppUtils.TARGET_CLASS_NAME_USERINFO) != 2)
								{
									KCloudCommonUtil.startActivity(
										KCloudAppUtils.TARGET_CLASS_NAME_USERINFO);
									mHandler.sendEmptyMessage(1);
								} else {
									KCloudUser.getInstance().sendMessage(
											CLDMessageId.MSG_ID_SHOW_SERVICE_LIST, 0);
								}
							}

							@Override
							public void onCancel() {
							}
				});
				break;
			}
			
			case 1:
				if (KCloudCommonUtil.isRunBackground(
						KCloudAppUtils.TARGET_CLASS_NAME_USERINFO) != 2)
				{
					mHandler.sendEmptyMessageDelayed(1, 500);
					return;
				}
				KCloudUser.getInstance().sendMessage(
						CLDMessageId.MSG_ID_SHOW_SERVICE_LIST, 0);
				break;

			default:
				break;
			}
		}
	};
	
	public void init() {
		mTaskStatus = TASK_NONE;
		start_getPackageList_Running();
	}
	
	public void test() {
		mPackageList.clear();
		mServiceList.clear();
		
		testAddPackageInfo(100, 0);
		testAddPackageInfo(101, 1);
		testAddPackageInfo(100, 2);
		testAddAppInfo();
		
		new Thread(insertPackageAndServiceList).start();
		setPaySuccessNotify("测试");
	}
	
	private void testAddPackageInfo(int code, int status){
		KCloudPackageInfo packageInfo = new KCloudPackageInfo();
		packageInfo.setComboIcon("");
		packageInfo.setComboCode(code);
		packageInfo.setComboName("test" + code);
		packageInfo.setComboDesc("t");
		packageInfo.setCharges(190);
		packageInfo.setPayTimes(10);
		packageInfo.setFlow(2000);
		packageInfo.setStatus(status);
		mPackageList.add(packageInfo);
		
		KCloudServiceInfo serviceInfo = new KCloudServiceInfo();
		serviceInfo.setComboCode(packageInfo.getComboCode());
		serviceInfo.setComboStatus(packageInfo.getStatus());
		serviceInfo.setServiceCode(1);
		serviceInfo.setServiceIcon("http://test.careland.com.cn/img.careland.com.cn/st/ka/photo/13/36/30096307_14708868403613.png");
		serviceInfo.setServiceDesc("haha");
		serviceInfo.setServiceName("service_name");
		serviceInfo.setServiceStatus(0);
		serviceInfo.setServiceMonth(12);
		serviceInfo.setServiceCharge(199);
		mServiceList.add(serviceInfo);
	}
	
	private void testAddAppInfo(){
		
		KCloudAppInfo item = new KCloudAppInfo();
		item.setServiceCode(1);
		item.setAppPackName("com.cld.navi.cc");
		mAppList.add(item);
	}
	
	public void resetPackageList() {
		init();
	}

	private synchronized void start_getPackageList_Running() {
		CldOlsThreadPool.submit(new Runnable() {

			@Override
			public void run() {
				if (!CldPhoneNet.isNetConnected() || !CldPhoneManager.isSimReady()
						|| CldKAccountAPI.getInstance().getKuid() == 0L) {
					KCloudUser.getInstance().sendMessage(CLDMessageId.MSG_ID_KGO_GET_USER_PACKAGE_LIST_FAILED, 0);
					return ;
				}
				
				mTaskStatus = TASK_GETTING;
				KCloudNetworkUtils.getKGoUserPackageList(new IKCloudPackageListener() {

					@SuppressLint("NewApi") 
					@Override
					public void onResult(String jsonString) {
						CldLog.i(TAG, "getKGoUserPackageList = " + jsonString);
						if (jsonString != null && !jsonString.isEmpty()) {
							try {
								JSONObject jsonObject = new JSONObject(jsonString);
								if (jsonObject.getInt("errcode") == 0) {
									isPackageListSuccess = true;
									setPackageList(jsonString);
									KCloudAlarmManager.getInstance().init();
									if (isPackageListSuccess && isServiceAppListSuccess) {
										mTaskStatus = TASK_GETED;
										KCloudUser.getInstance().sendMessage(CLDMessageId.MSG_ID_KGO_GET_USER_PACKAGE_LIST_SUCCESS, 0);
									}
								} else {
									mTaskStatus = TASK_NONE;
									KCloudUser.getInstance().sendMessage(CLDMessageId.MSG_ID_KGO_GET_USER_PACKAGE_LIST_FAILED, 0);
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
							
						} else {
							mTaskStatus = TASK_NONE;						
							KCloudUser.getInstance().sendMessage(CLDMessageId.MSG_ID_KGO_GET_USER_PACKAGE_LIST_FAILED, 0);
						}
					}
					
				});
				KCloudNetworkUtils.getKGoServicesAppList(new IKCloudPackageListener() {

					@SuppressLint("NewApi") 
					@Override
					public void onResult(String jsonString) {
						CldLog.i(TAG, "getKGoServicesAppList = " + jsonString);
						if (jsonString != null && !jsonString.isEmpty()) {
							try {
								JSONObject jsonObject = new JSONObject(jsonString);
								if (jsonObject.getInt("errcode") == 0) {
									isServiceAppListSuccess = true;
									setServicesAppList(jsonString);	
									
									if (isPackageListSuccess && isServiceAppListSuccess) {
										mTaskStatus = TASK_GETED;
										KCloudUser.getInstance().sendMessage(CLDMessageId.MSG_ID_KGO_GET_USER_PACKAGE_LIST_SUCCESS, 0);
									}
								} else {
									mTaskStatus = TASK_NONE;
									KCloudUser.getInstance().sendMessage(CLDMessageId.MSG_ID_KGO_GET_USER_PACKAGE_LIST_FAILED, 0);
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
							
						} else {
							mTaskStatus = TASK_NONE;						
							KCloudUser.getInstance().sendMessage(CLDMessageId.MSG_ID_KGO_GET_USER_PACKAGE_LIST_FAILED, 0);
						}
					}
				});
			}
		});
	}
	
	private void reStart_getPackageList_Running(final String package_name) {
		final boolean[] isSuccess = new boolean[2];
		CldOlsThreadPool.submit(new Runnable() {

			@Override
			public void run() {
				if (!CldPhoneNet.isNetConnected()
						|| CldKAccountAPI.getInstance().getKuid() == 0) {
					return ;
				}
				
				KCloudNetworkUtils.getKGoUserPackageList(new IKCloudPackageListener() {

					@SuppressLint("NewApi") 
					@Override
					public void onResult(String jsonString) {
						CldLog.i(TAG, "getKGoUserPackageList = " + jsonString);
						if (jsonString != null && !jsonString.isEmpty()) {
							try {
								JSONObject jsonObject = new JSONObject(jsonString);
								if (jsonObject.getInt("errcode") == 0) {
									isSuccess[0] = true;
									setPackageList(jsonString);
									
									if (isSuccess[0] && isSuccess[1]) {
										setPaySuccessNotify(package_name);
									}
								} 
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				});
				
				KCloudNetworkUtils.getKGoServicesAppList(new IKCloudPackageListener() {

					@SuppressLint("NewApi") 
					@Override
					public void onResult(String jsonString) {
						CldLog.i(TAG, "getKGoServicesAppList = " + jsonString);
						if (jsonString != null && !jsonString.isEmpty()) {
							try {
								JSONObject jsonObject = new JSONObject(jsonString);
								if (jsonObject.getInt("errcode") == 0) {
									isSuccess[1] = true;
									setServicesAppList(jsonString);	
									
									if (isSuccess[0] && isSuccess[1]) {
										setPaySuccessNotify(package_name);
									}
								} 
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}	
				});
				
			}
			
		});
	}
	
	private synchronized void setPackageList(String jsonString) {
		if (!mServiceList.isEmpty()) {
			mServiceList.clear();
		}
		
		if (!mPackageList.isEmpty()) {
			mPackageList.clear();
		}
		
		try {
			JSONArray jsonArray = null;
			JSONObject jsonObject = new JSONObject(jsonString);
			if (jsonObject.has("data")) {
				jsonArray = jsonObject.getJSONArray("data");
			}
			if (jsonArray != null) {
				for (int i = 0; i < jsonArray.length(); i++) {
					
					int combo_code = jsonArray.getJSONObject(i).getInt("combo_code");
					int status = jsonArray.getJSONObject(i).getInt("status");
					if (isSamePackage(combo_code, status)) {
						int index = getSamePackageIndex(combo_code, status);
						if (index >= 0) {
							CldLog.i(TAG, "isSamePackage: " + mPackageList.get(index).getComboName());
							int number = mPackageList.get(index).getNumber();
							number += 1;
							mPackageList.get(index).setNumber(number);
						}
						continue;
					}
					
					KCloudPackageInfo packageInfo = new KCloudPackageInfo();
					packageInfo.setComboIcon(jsonArray.getJSONObject(i).getString("combo_icon"));
					packageInfo.setComboCode(jsonArray.getJSONObject(i).getInt("combo_code"));
					packageInfo.setComboName(jsonArray.getJSONObject(i).getString("combo_name"));
					packageInfo.setComboDesc(jsonArray.getJSONObject(i).getString("combo_desc"));
					packageInfo.setCharges(jsonArray.getJSONObject(i).getInt("charges"));
					packageInfo.setPayTimes(jsonArray.getJSONObject(i).getInt("pay_times"));
					packageInfo.setFlow(jsonArray.getJSONObject(i).getInt("flowrate"));
					packageInfo.setStatus(jsonArray.getJSONObject(i).getInt("status"));

					if (jsonArray.getJSONObject(i).has("service")) {
						JSONArray jsonServiceArray = jsonArray.getJSONObject(i)
								.getJSONArray("service");
						//状态为1：已启用 时，才解析endtime
						if (packageInfo.getStatus() == 1) {
							packageInfo.setEndtime(jsonServiceArray.getJSONObject(0).getLong("endtime"));
						}
						for (int j = 0; j < jsonServiceArray.length(); j++) {
							KCloudServiceInfo serviceInfo = new KCloudServiceInfo();
							serviceInfo.setComboCode(packageInfo.getComboCode());
							serviceInfo.setComboStatus(packageInfo.getStatus());
							serviceInfo.setServiceCode(jsonServiceArray.getJSONObject(j).getInt("service_code"));
							serviceInfo.setServiceIcon(jsonServiceArray.getJSONObject(j).getString("service_icon"));
							serviceInfo.setServiceDesc(jsonServiceArray.getJSONObject(j).getString("service_desc"));
							serviceInfo.setServiceName(jsonServiceArray.getJSONObject(j).getString("service_name"));
							serviceInfo.setServiceStatus(jsonServiceArray.getJSONObject(j).getInt("service_status"));
							serviceInfo.setServiceMonth(jsonServiceArray.getJSONObject(j).getInt("month"));
							serviceInfo.setServiceCharge(jsonServiceArray.getJSONObject(j).getInt("charge"));
							//添加服务
							mServiceList.add(serviceInfo);
						}
					}
					//添加套餐
					mPackageList.add(packageInfo);
					CldLog.i(TAG, "mPackageList.add " + mPackageList.size());
				}
				new Thread(insertPackageAndServiceList).start();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private boolean isSamePackage(int combo_code, int status)
	{
		if (mPackageList != null && !mPackageList.isEmpty()) {
			for (int i=0; i<mPackageList.size(); i++) {
				if (mPackageList.get(i).getComboCode() == combo_code &&
					mPackageList.get(i).getStatus() == status) {
					//套餐编码和套餐状态相同，则表示相同的套餐
					return true;
				}
			}
		}
		return false;
	}
	
	private int getSamePackageIndex(int combo_code, int status)
	{
		if (mPackageList != null && !mPackageList.isEmpty()) {
			for (int i=0; i<mPackageList.size(); i++) {
				if (mPackageList.get(i).getComboCode() == combo_code &&
					mPackageList.get(i).getStatus() == status) {
					//套餐编码和套餐状态相同，则表示相同的套餐
					return i;
				}
			}
		}
		return -1;
	}
	
	public void setPackageList(ArrayList<KCloudPackageInfo> packageInfoList) {
		if (packageInfoList != null && !packageInfoList.isEmpty()) {
			mPackageList.clear();
			mPackageList = packageInfoList;
		}
	}
	
	public void setServiceList(ArrayList<KCloudServiceInfo> ServiceInfoList) {
		if (ServiceInfoList != null && !ServiceInfoList.isEmpty()) {
			mServiceList.clear();
			mServiceList = ServiceInfoList;
		}
	}
	
	public void setAppList(ArrayList<KCloudAppInfo> appInfoList) {
		if (appInfoList != null && !appInfoList.isEmpty()) {
			mAppList.clear();
			mAppList = appInfoList;
		}
	}
	
	private void sortPackageList() {
		//套餐个数大于1，才排序
		if (mPackageList.size() > 1) {
			//按照状态值排序：未启用 --> 已启用 --> 已过期
			Collections.sort(mPackageList, new StatusComparator());
			KCloudPackageInfo first = mPackageList.get(0);
			int index = -1;
			for (int i=0; i<mPackageList.size(); i++) {
				if (mPackageList.get(i).getStatus() == 1) {
					index = i;
				}
			}
			
			//已启用的索引大于0， 将已启用的套餐移到最前面
			if (index > 0) {
				mPackageList.set(0, mPackageList.get(index));
				mPackageList.set(index, first);
			}
		}
	}
	
	private Runnable insertPackageAndServiceList = new Runnable() {
		@Override
		public void run() {
			if (mPackageList != null && mPackageList.size() > 0) {
				
				//按照状态值排序
				sortPackageList();
				//将后台返回的套餐列表插入到KCloudPackageTable中
				KCloudPackageTable.deletePackageInfos();
				KCloudPackageTable.insertPackageInfos(mPackageList);
			}
			
			if (mServiceList != null && mServiceList.size() > 0) {
				//将后台返回的服务列表插入到KCloudServiceTable中
				KCloudServiceTable.deleteServiceInfos();
				KCloudServiceTable.insertServiceInfos(mServiceList);
			}
		}
	};
	
	private Runnable insertAppList = new Runnable() {
		@Override
		public void run() {
			if (mAppList != null && mAppList.size() > 0) {
				//将后台返回的应用列表插入到KCloudAppTable中
				KCloudAppTable.deleteAppInfos();
				KCloudAppTable.insertAppInfos(mAppList);
			}
		}
	};
	
	private synchronized void setServicesAppList(String jsonString) {		
		if (!mAppList.isEmpty()) {
			mAppList.clear();
		}
		
		try {
			JSONArray jsonArray = null;
			JSONObject jsonObject = new JSONObject(jsonString);

			if (jsonObject.has("data")) {
				jsonArray = jsonObject.getJSONArray("data");
			}
			
			if (jsonArray != null) {
				for (int i = 0; i < jsonArray.length(); i++) {		
					for (int j = 0; j < jsonArray.getJSONObject(i).getJSONArray("app_packname").length(); j++) {
						KCloudAppInfo item = new KCloudAppInfo();
						
						item.setServiceCode(jsonArray.getJSONObject(i).getInt("service_code"));
						item.setAppPackName(jsonArray.getJSONObject(i).getJSONArray("app_packname").getString(j));
						
						mAppList.add(item);
					}
				}
				new Thread(insertAppList).start();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}	
	}
	
	private void setPaySuccessNotify(String package_name) {
		Bundle bundle = new Bundle();
		bundle.putString("package_name", package_name);
		Message message = mHandler.obtainMessage();
		message.what = CLDMessageId.MSG_ID_KGO_GET_PAY_STATUS;
		message.setData(bundle);
		mHandler.sendMessage(message);
		mPaySuccess = true;
	}
	
	public boolean getPaySuccess() 
	{
		return mPaySuccess;
	}
	
	public void setPaySuccess(boolean paySuccess) 
	{
		mPaySuccess = paySuccess;
	}
	
	/**
	 * 支付结果
	 */
	@SuppressLint("NewApi") 
	public boolean setPayResult(String jsonString) {
		if (jsonString == null || jsonString.isEmpty()) {
			return false;
		}
		CldLog.i(TAG, "setPayResult = " + jsonString.toString());
		
		try {
			JSONObject json = new JSONObject(jsonString);
			
			if (json.getInt("errcode") == 1 && json.has("data")) {
				reStart_getPackageList_Running(json.getJSONObject("data").getString("pkalias"));
				return true;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
			
		return false;
	}
	
	public int getPackageSize() {
		CldLog.i(TAG, "mPackageList.size() = " + mPackageList.size());
		return mPackageList.size();
	}
	
	public ArrayList<KCloudPackageInfo> getPackageList() {
		return mPackageList;
	}
	
	public KCloudPackageInfo getEnablePackage() {
		for (KCloudPackageInfo item : mPackageList) {
			if (item.getStatus() == 1) {
				return item;	// 已启用的套餐
			}
		}
		return null;
	}
	
	private KCloudPackageInfo search(ArrayList<KCloudPackageInfo> list) {
		KCloudPackageInfo info = null;
		
		for (int i = 0; i < list.size(); i++) {
			KCloudPackageInfo itemA = list.get(i);
			if (itemA.getStatus() != 2) {
				continue;
			}
			
			info = itemA;
			for (int j = i; j < list.size(); j++) {
				KCloudPackageInfo itemB = list.get(j);
				if (itemB.getStatus() != 2) {
					continue;
				}
				
				if (itemA.getEndtime() < itemB.getEndtime()) {
					info = itemB;
				}
				
				i = j;				
			}	
		}
	    
	    return info;
	}
	
	/**
	 * 刚过期的套餐
	 * @return
	 */
	public KCloudPackageInfo getNewExpirationPackage() {
		KCloudPackageInfo info = search(mPackageList);
		return info;
	}
	
	public KCloudPackageInfo getPackageInfoById(int packageId, int packageStatus) {
		if (mPackageList.isEmpty()) {
			return null;
		}
		
		for (KCloudPackageInfo item : mPackageList) {
			if (item.getComboCode() == packageId && 
					item.getStatus() == packageStatus) {
				return item;
			}
		}
		
		return null;
	}
	
	public ArrayList<KCloudServiceInfo> getServiceList(int packageId, int packagesStatus) {
		if (mServiceList.isEmpty()) {
			return null;
		}

		ArrayList<KCloudServiceInfo> list = new ArrayList<KCloudServiceInfo>();
		for (KCloudServiceInfo item : mServiceList) {
			if (item.getComboCode() == packageId && 
					item.getComboStatus() == packagesStatus) {
				list.add(item);
			}
		}
		return list;
	}
	
	public boolean isRunningApp(String app_name) {
		if (mAppList.isEmpty()) {
			return false;
		}
		
		for (KCloudAppInfo item : mAppList) {
			if (item.getAppPackName().equals(app_name)) {
				return true;
			}
		}
		
		return false;
	}
	
	public int getTaskStatus() {
		if (isPackageListSuccess && isServiceAppListSuccess) {
			return TASK_GETED;
		}
		
		return mTaskStatus;
	}
	
	/**
	 * @Description:自定义比较器：按状态降序排序  
	 */
	class StatusComparator implements Comparator<Object> {
		@SuppressLint("UseValueOf")
		//实现接口中的方法
		public int compare(Object object1, Object object2) {
			//强制转换
			KCloudPackageInfo p1 = (KCloudPackageInfo) object1;
			KCloudPackageInfo p2 = (KCloudPackageInfo) object2;
			//降序
			//return new Integer(p2.getStatus()).compareTo(new Integer(p1.getStatus()));
			//升序
			return new Integer(p1.getStatus()).compareTo(new Integer(p2.getStatus()));
		}
	}
}