package cld.kcloud.custom.manager;

import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import cld.kcloud.center.KCloudAppUtils.CLDMessageId;
import cld.kcloud.custom.bean.KCloudPackageInfo;
import cld.kcloud.user.KCloudUser;
import cld.kcloud.utils.KCloudCommonUtil;
import cld.kcloud.utils.KCloudNetworkUtils;
import com.cld.log.CldLog;
import com.cld.ols.tools.CldOlsThreadPool;

public class KCloudFlowManager {
	private final String TAG = "KCloudFlowManager";
	public interface IKCloudFlowListener {
		void onResult(String jsonString);
	}
	
	public static final int TASK_NONE = 0;
	public static final int TASK_GETTING = 1;
	public static final int TASK_GETED = 2;
	private static KCloudFlowManager mKCloudFlow = null;
	
	private String mTotalFlow = "";
	private String mUsedFlow = "";
	private int mRemaindays = 0;
	private String mPackageName = "";
	private int mTaskStatus = TASK_NONE;
	private boolean mIsStopRunning = false;
	
	public static KCloudFlowManager getInstance() {
		if (mKCloudFlow == null) {
			synchronized(KCloudFlowManager.class) {
				if (mKCloudFlow == null) {
					mKCloudFlow = new KCloudFlowManager();
				}
			}
		}
		return mKCloudFlow;
	}
	
	public void init() {
		mIsStopRunning = false;
		mTaskStatus = TASK_NONE;
		start_getSimCardStatus_Running();
	}
	
	public void uninit() {
		mIsStopRunning = true;
	}
	
	private void start_getSimCardStatus_Running() {
		CldOlsThreadPool.submit(new Runnable() {
			@Override
			public void run() {
				while (!mIsStopRunning) {
					mTaskStatus = TASK_GETTING;
					KCloudNetworkUtils.getSimCarStatus(new IKCloudFlowListener() {
						@SuppressLint("NewApi") 
						@Override
						public void onResult(String jsonString) {
							CldLog.i(TAG, "jsonString = " + jsonString);
							if (jsonString != null && !jsonString.isEmpty()) {
								try {
									JSONObject json = new JSONObject(jsonString);
									
									if (json.getInt("errcode") == 0) {
										mTotalFlow = json.getString("total");
										mUsedFlow = json.getString("used");
										mPackageName = json.getString("pkalias");
										mRemaindays = json.getInt("remaindays");
										
										mTaskStatus = TASK_GETED;
										KCloudUser.getInstance().sendMessage(CLDMessageId.MSG_ID_KLDJY_FLOW_GET_SUCCESS, 0);
										KCloudCommonUtil.sendGetFlowSuccessBroadcast();
										
										//检测sim卡的流量状态
										KCloudAlarmManager.getInstance().checkAlarmStatus();
									} else {
										mTaskStatus = TASK_NONE;
										KCloudUser.getInstance().sendMessage(CLDMessageId.MSG_ID_KLDJY_FLOW_GET_FAILED, 0);
										KCloudCommonUtil.sendGetFlowFailBroadcast();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							} else {
								mTaskStatus = TASK_NONE;
								KCloudUser.getInstance().sendMessage(CLDMessageId.MSG_ID_KLDJY_FLOW_GET_FAILED, 0);
								KCloudCommonUtil.sendGetFlowFailBroadcast();
							}
						}
					});
					
					try {
						//1分钟检测一次
						Thread.sleep(60*1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});	
	}
	
	public void resetFlowStatus() {
		if (KCloudPackageManager.getInstance().getTaskStatus() != KCloudPackageManager.TASK_GETED) {
			KCloudUser.getInstance().sendMessage(CLDMessageId.MSG_ID_KLDJY_FLOW_GET_FAILED, 0);
			return ;
		}
		
		init();
	}
	
	public int getSimCardStatus() {
		return mTaskStatus;
	}
	
	@SuppressLint("NewApi")
	public float getTotal() {
		if (mTotalFlow == null || mTotalFlow.isEmpty()) {
			return 0;
		} else {
			return Float.valueOf(mTotalFlow);
		}
	}
	
	@SuppressLint("NewApi") 
	public float getUse() {
		if (mUsedFlow == null || mUsedFlow.isEmpty()) {
			return 0;
		} else {
			return Float.valueOf(mUsedFlow);
		}
	}
	
	@SuppressLint("NewApi") 
	public String getPackageName() {
		if (mPackageName == null || mPackageName.isEmpty()) {
			KCloudPackageInfo info = KCloudPackageManager.getInstance().getEnablePackage();
			
			if (info != null) {
				return info.getComboName();
			} else {
				return "";
			}
		}
		
		return mPackageName;
	}
	
	public int getRemainDays() {
		return mRemaindays;
	}
	
	public int getCurRemFlowPercent() {
		float fUsedFlow = getUse();
		float fTotalFlow = getTotal();
		if (fTotalFlow > 0 && fUsedFlow >= 0 && fUsedFlow <= fTotalFlow) {
			float fSurplusFlow = fTotalFlow - fUsedFlow;
			return (int)((fSurplusFlow / fTotalFlow)*100  + 0.5);
		} else {
			return 0;
		}
	}
	
	public int getTaskStatus() {
		return mTaskStatus;
	}
}