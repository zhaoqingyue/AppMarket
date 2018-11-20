package cld.kcloud.custom.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import cld.kcloud.center.KCloudAppUtils;
import cld.kcloud.center.R;
import cld.kcloud.center.KCloudAppUtils.CLDMessageId;
import cld.kcloud.center.KCloudCtx;
import cld.kcloud.custom.bean.KCloudPackageInfo;
import cld.kcloud.user.KCloudUser;
import cld.kcloud.utils.KCloudNetworkUtils;
import cld.kcloud.utils.KCloudShareUtils;
import cld.kcloud.utils.control.CldMessageDialog;
import cld.kcloud.utils.KCloudCommonUtil;
import cld.kcloud.utils.control.CldMessageDialog.CldMessageDialogListener;
import cld.kcloud.utils.control.CldMessageDialog.CldMessageType;
import com.cld.log.CldLog;
import com.cld.ols.tools.CldOlsThreadPool;

public class KCloudAlarmManager {

	private final String TAG = "KCloudAlarmManager";
	private final int DEFAULT_DAYS_FIRST = 7;		// 默认第一次提醒剩余天数
	private final int DEFAULT_DATS_SECOND = 1;		// 默认第二次提醒剩余天数
	private final int DEFAULT_FLOW_FIRST = 20;		// 默认第一次提醒剩余流量百分比
	private final int DEFAULT_FLOW_SECOND = 5;		// 默认第二次提醒剩余流量百分比
	
	private final String FIRST_DAY_ALARM_TIP = KCloudCommonUtil.getString(R.string.alarm_tip_first_day);
	private final String SECOND_DAY_ALARM_TIP = KCloudCommonUtil.getString(R.string.alarm_tip_second_day);
	private final String FIRST_FLOW_ALARM_TIP = KCloudCommonUtil.getString(R.string.alarm_tip_first_flow);
	private final String SECOND_FLOW_ALARM_TIP = KCloudCommonUtil.getString(R.string.alarm_tip_second_flow);
	
	private class days_alarm {
		int day_first;				// 第一次提醒剩余天数
		int day_second;				// 第二次提醒剩余天数
		
		public days_alarm() {
			this.day_first = DEFAULT_DAYS_FIRST;
			this.day_second = DEFAULT_DATS_SECOND;
		}
	};
	
	private class floatrate_alarm {
		int rate_first;			// 第一次提醒剩余流量百分比
		int rate_second;		// 第二次提醒剩余流量百分比
		
		public floatrate_alarm() {
			this.rate_first = DEFAULT_FLOW_FIRST;
			this.rate_second = DEFAULT_FLOW_SECOND;
		}
	}
	
	public interface IKCloudAlarmListener {
		void onResult(String jsonString);
	}
	
	/** 定时流量信息  */
	private int mTipType = -1;
	private boolean mIsTip = false;
	private int mTipTime = 60*1000;
	private days_alarm daysAlarm = new days_alarm();;
	private floatrate_alarm flowAlarm = new floatrate_alarm();
	private static KCloudAlarmManager mKCloudAlarm = null;
	
	/** 初始化Handler对象 */
	private Handler mHandler = new Handler(KCloudCtx.getAppContext().getMainLooper()) {
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				mHandler.removeMessages(0);
				mHandler.sendEmptyMessageDelayed(0, mTipTime);

				mTipType = -1;
				mIsTip = false;
				
				if (KCloudFlowManager.getInstance().getTaskStatus() != KCloudFlowManager.TASK_GETED) {
					CldLog.i(TAG, "KCloudFlowManagergetTaskStatus() != KCloudFlowManager.TASK_GETED");
					return ;
				}
				
				int status = getAlarmStatus();
				if (status == 0) {
					return ;
				}
				
				int curRemFlowPercent = KCloudFlowManager.getInstance().getCurRemFlowPercent();
				int curRemainDay = KCloudFlowManager.getInstance().getRemainDays();
				CldLog.i(TAG, "curRemFlowPercent = " + curRemFlowPercent);
				CldLog.i(TAG, "curRemainDay = " + curRemainDay);
				
				if (curRemFlowPercent <= getRemainFlowAlarm()) {
					mTipType = 0;	// 提醒流量
				} else if (curRemainDay <= getRemainDaysAlarm()) {
					mTipType = 1;	// 提醒套餐
				}
				
				if (mTipType >= 0) {
					KCloudCommonUtil.sendFreshFlowBroadcast(false);
					
					// 需要提醒
					String msgText = "";
					long lastTipTime = KCloudShareUtils.getLong(KCloudAppUtils.TARGET_FIELD_LAST_TIP_TIME);
					CldLog.i(TAG, "lastTipTime = " + lastTipTime);
					
					if (status == -2) {
						if (mTipType == 0) {
							msgText = FIRST_FLOW_ALARM_TIP;
						} else {
							msgText = FIRST_DAY_ALARM_TIP;
						}
					} else {
						if (mTipType == 0) {
							msgText = SECOND_FLOW_ALARM_TIP;
						} else {
							msgText = SECOND_DAY_ALARM_TIP;
						}
					}
					
					if (!KCloudCommonUtil.isSameDay(lastTipTime, System.currentTimeMillis())) {
						mIsTip = true;
					}
							
					if (mIsTip) {
						//提示续费提醒之后，将周期改为3分钟
						mTipTime = 3*60*1000;
						//如果可用套餐不为空，则保存可用套餐的套餐编码
						if (KCloudPackageManager.getInstance().getEnablePackage() != null) {
							KCloudShareUtils.put(KCloudAppUtils.TARGET_FIELD_LAST_TIP_PKID, 
									KCloudPackageManager.getInstance().getEnablePackage().getComboCode());
						}
							
						KCloudShareUtils.put(KCloudAppUtils.TARGET_FIELD_LAST_TIP_TIME, 
								System.currentTimeMillis());
						KCloudShareUtils.put(KCloudAppUtils.TARGET_FIELD_LAST_REMAIN_DAY, 
								KCloudFlowManager.getInstance().getRemainDays());
						KCloudShareUtils.put(KCloudAppUtils.TARGET_FIELD_LAST_REMAIN_FLOW, 
								KCloudFlowManager.getInstance().getCurRemFlowPercent());
						
						CldMessageDialog.showMessageDialog(
								KCloudCtx.getAppContext(), 
								msgText, 
								CldMessageType.eMessageType_Ok_Close, 
								KCloudCommonUtil.getString(R.string.alarm_tip_renewal), 
								new CldMessageDialogListener() {
	
									@Override
									public void onOk() {
										CldLog.i(TAG, "mTipType: " + mTipType);
										if (KCloudCommonUtil.isRunBackground(
												KCloudAppUtils.TARGET_CLASS_NAME_USERINFO) != 2)
										{
											KCloudCommonUtil.startActivity(
												KCloudAppUtils.TARGET_CLASS_NAME_USERINFO);
											mHandler.sendEmptyMessage(1);
										} else {
											if (mTipType == 2) {
												KCloudUser.getInstance().sendMessage(
														CLDMessageId.MSG_ID_SHOW_RENEWAL_QRCODE, 1);
											} else {
												KCloudUser.getInstance().sendMessage(
														CLDMessageId.MSG_ID_SHOW_RENEWAL_QRCODE, 2);
											}
										}
									}
	
									@Override
									public void onCancel() {
										
									}
						});
					}
				}
			}
			else if (msg.what == 1) 
			{
				if (KCloudCommonUtil.isRunBackground(
						KCloudAppUtils.TARGET_CLASS_NAME_USERINFO) != 2)
				{
					mHandler.sendEmptyMessageDelayed(1, 1000);
					return;
				}
				
				if (mTipType == 2) {
					KCloudUser.getInstance().sendMessage(CLDMessageId.MSG_ID_SHOW_RENEWAL_QRCODE, 1);
				} else {
					KCloudUser.getInstance().sendMessage(CLDMessageId.MSG_ID_SHOW_RENEWAL_QRCODE, 2);
				}
			}
		}
	}; 
	
	public void test() {
		mTipType = 2;
		CldMessageDialog.showMessageDialog(
				KCloudCtx.getAppContext(), 
				FIRST_FLOW_ALARM_TIP, 
				CldMessageType.eMessageType_Ok_Close, 
				KCloudCommonUtil.getString(R.string.alarm_tip_renewal), 
				new CldMessageDialogListener() {

					@Override
					public void onOk() {
						CldLog.i(TAG, "mTipType: " + mTipType);
						if (KCloudCommonUtil.isRunBackground(
								KCloudAppUtils.TARGET_CLASS_NAME_USERINFO) != 2)
						{
							KCloudCommonUtil.startActivity(
								KCloudAppUtils.TARGET_CLASS_NAME_USERINFO);
							mHandler.sendEmptyMessage(1);
						} else {
							if (mTipType == 2) {
								KCloudUser.getInstance().sendMessage(CLDMessageId.MSG_ID_SHOW_RENEWAL_QRCODE, 1);
							} else {
								KCloudUser.getInstance().sendMessage(CLDMessageId.MSG_ID_SHOW_RENEWAL_QRCODE, 2);
							}
						}
					}

					@Override
					public void onCancel() {
						
					}
		});
	}
	
	public static KCloudAlarmManager getInstance() {
		if (mKCloudAlarm == null) {
			synchronized(KCloudAlarmManager.class) {
				if (mKCloudAlarm == null) {
					mKCloudAlarm = new KCloudAlarmManager();
				}
			}
		}
		return mKCloudAlarm;
	}
	
	public void init() {	
		start_getAlarmSetting_Running();
		mHandler.sendEmptyMessageDelayed(0, 1*60*1000);
	}
	
	/**
	 * @Title: getAlarmStatus
	 * @Description: 0: 正在使用; -1: 已到期 ; -2: 即将到期
	 * @return: int
	 */
	@SuppressLint("SimpleDateFormat") 
	public int getAlarmStatus() {
		int curRemFlowPercent = KCloudFlowManager.getInstance().getCurRemFlowPercent();
		int curRemainDay = KCloudFlowManager.getInstance().getRemainDays();
		Log.d(TAG, " curRemFlowPercent: " + curRemFlowPercent);
		Log.d(TAG, "      curRemainDay: " + curRemainDay);
		
		int status = 0; // 正在使用
		if (curRemainDay <= 0 || curRemFlowPercent <= 0) {
			status = -1;	// 已到期 
		} else if ((curRemainDay <= daysAlarm.day_first)
				|| (curRemFlowPercent <= flowAlarm.rate_first)){
			status = -2;	// 即将到期
		}
		return status;	
	}
	
	/**
	 * @Title: CheckAlarmStatus
	 * @Description: 检测sim卡的流量状态, 如果发生变化则重新获取套餐列表
	 */
	public void checkAlarmStatus()
	{
		int alarmStatus = getAlarmStatus();
		int lastStatus = KCloudShareUtils.getInt(
				KCloudAppUtils.TARGET_FIELD_LAST_CARD_STATUS, 1);
		CldLog.i(TAG, "alarmStatus: " + alarmStatus + ", lastStatus: " + lastStatus);
		if (lastStatus >= -2 && lastStatus <= 0) {
			if (lastStatus != alarmStatus) {
				KCloudPackageManager.getInstance().resetPackageList();
				notifyNavi();
			}
		} else {
			notifyNavi();
		}
	}
	
	public void notifyNavi()
	{
		CldLog.i(TAG, " notifyNavi ");
		//提供给导航
		int alarmStatus = getAlarmStatus();
		int simcardStatus = KCloudSimCardManager.getInstance().getSimStatus();
		KCloudShareUtils.put(KCloudAppUtils.TARGET_FIELD_LAST_CARD_STATUS, alarmStatus);
		KCloudShareUtils.put("flowcard_status", alarmStatus);
		KCloudShareUtils.put("simcard_status", simcardStatus);
		Intent broadcast = new Intent("kclound_flowcard_status_changed");
		broadcast.putExtra("flowcard_status", alarmStatus);
		broadcast.putExtra("simcard_status", simcardStatus);
		KCloudCtx.getAppContext().sendBroadcast(broadcast, null);
	}
	
	public void jump2Renew()
	{
		CldLog.i(TAG, " jump2Renew ");
		Intent intent = new Intent();
		intent.setClassName(KCloudAppUtils.TARGET_PACKAGE_NAME, 
				KCloudAppUtils.TARGET_CLASS_NAME_USER);
		intent.putExtra(KCloudAppUtils.START_ACTIVITY_EXTRA, 
				KCloudAppUtils.FRAGMENT_RENEWAL);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		KCloudCtx.getAppContext().startActivity(intent);
	}
	
	private void start_getAlarmSetting_Running() {
		CldOlsThreadPool.submit(new Runnable() {

			@Override
			public void run() {
				KCloudPackageInfo info = KCloudPackageManager.getInstance()
						.getEnablePackage();
				if (info == null) 
					return;

				KCloudNetworkUtils.getKGoAlarmSetting(info.getComboCode(),
						new IKCloudAlarmListener() {

							@SuppressLint("NewApi") 
							@Override
							public void onResult(String jsonString) {
								CldLog.i(TAG, "jsonString = " + jsonString);
								if (jsonString != null && !jsonString.isEmpty()) {
									try {
										JSONObject jsonObject = new JSONObject(
												jsonString);
										if (jsonObject.getInt("errcode") == 0) {
											JSONArray jsonArray = null;
											// 日期提醒
											if (jsonObject.has("days_alarm")) {
												jsonArray = jsonObject.getJSONArray("days_alarm");
											}

											if (jsonArray != null) {
												jsonArray = toSort(jsonArray);
											}

											if (jsonArray != null) {
												setDaysAlarm(Integer.parseInt(jsonArray.getString(0)), 1); // 首次提醒
												if (jsonArray.length() > 1) {
													setDaysAlarm(Integer.parseInt(jsonArray.getString(1)), 2); // 再次提醒
												}
											}

											// 流量提醒
											jsonArray = null;
											if (jsonObject.has("floatrate_alarm")) {
												jsonArray = jsonObject.getJSONArray("floatrate_alarm");
											}

											if (jsonArray != null) {
												jsonArray = toSort(jsonArray);
											}

											if (jsonArray != null) {
												KCloudAlarmManager.getInstance().setFlowAlarm(
														Integer.parseInt(jsonArray.getString(0)),	1); // 首次提醒
												if (jsonArray.length() > 1) {
													KCloudAlarmManager.getInstance().setFlowAlarm(
																	Integer.parseInt(jsonArray.getString(1)), 2); // 再次提醒
												}
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
	
	private JSONArray toSort(JSONArray jsonArray) {
		if (jsonArray == null) {
			return null;
		}

		List<String> strList = new ArrayList<String>();
	    for (int i = 0; i < jsonArray.length(); i++) {
	    	try {
				strList.add(jsonArray.getString(i));
			} catch (JSONException e) {
				e.printStackTrace();
			}
	    }
	
	    Collections.sort(strList, new Comparator<String>() {
			@Override
			public int compare(String argA, String argB) {
				int valueA = Integer.parseInt(argA);
				int valueB = Integer.parseInt(argB);
				
				if (valueA < valueB) {
					return 1;
				} else if (valueA > valueB) {
					return -1;
				}				
				return 0;
			}
	    });
	    
	    return new JSONArray(strList);
	}
	
	public int getRemainDaysAlarm() {
		int alarmDay = daysAlarm.day_first != 0 ? daysAlarm.day_first : DEFAULT_DAYS_FIRST;
		int lastRemainDay = KCloudShareUtils.getInt(KCloudAppUtils.TARGET_FIELD_LAST_REMAIN_DAY);
		long lastTipTime = KCloudShareUtils.getLong(KCloudAppUtils.TARGET_FIELD_LAST_TIP_TIME);
		
		if (KCloudPackageManager.getInstance().getEnablePackage() != null)
		{
			int lsatpkid = KCloudShareUtils.getInt(KCloudAppUtils.TARGET_FIELD_LAST_TIP_PKID);
			int pkid = KCloudPackageManager.getInstance().getEnablePackage().getComboCode();
			//Log.d(TAG, " ++++ lsatpkid: " + lsatpkid);
			//Log.d(TAG, " ++++     pkid: " + pkid);
			if (lsatpkid != pkid)
			{
				KCloudShareUtils.put(KCloudAppUtils.TARGET_FIELD_LAST_TIP_TIME, 0L);
				lastTipTime = 0L;
			}
		}
		
		if (lastTipTime != 0 && lastRemainDay < alarmDay) {
			alarmDay = daysAlarm.day_second != 0 ? daysAlarm.day_second : DEFAULT_DATS_SECOND;
		}
		CldLog.i(TAG, "getRemainDaysAlarm = " + alarmDay);
		return alarmDay;
	}
	
	public int getRemainFlowAlarm() {
		int alarmFlow = flowAlarm.rate_first != 0 ? flowAlarm.rate_first : DEFAULT_FLOW_FIRST;
		int lastRemainFlow = KCloudShareUtils.getInt(KCloudAppUtils.TARGET_FIELD_LAST_REMAIN_FLOW);
		long lastTipTime = KCloudShareUtils.getLong(KCloudAppUtils.TARGET_FIELD_LAST_TIP_TIME);
		
		if (KCloudPackageManager.getInstance().getEnablePackage() != null)
		{
			int lsatpkid = KCloudShareUtils.getInt(KCloudAppUtils.TARGET_FIELD_LAST_TIP_PKID);
			int pkid = KCloudPackageManager.getInstance().getEnablePackage().getComboCode();
			//Log.d(TAG, " ++++ lsatpkid: " + lsatpkid);
			//Log.d(TAG, " ++++     pkid: " + pkid);
			if (lsatpkid != pkid)
			{
				KCloudShareUtils.put(KCloudAppUtils.TARGET_FIELD_LAST_TIP_TIME, 0L);
				lastTipTime = 0L;
			}
		}
		
		if (lastTipTime != 0 && lastRemainFlow < alarmFlow) {
			alarmFlow = flowAlarm.rate_second != 0 ? flowAlarm.rate_second : DEFAULT_FLOW_SECOND;
		}
		
		CldLog.i(TAG, "getRemainFlowAlarm = " + alarmFlow);
		return alarmFlow;
	}
	
	public void setDaysAlarm(int days, int index) {
		switch (index) {
		case 1:
			daysAlarm.day_first = days;
			break;
			
		case 2:
			daysAlarm.day_second = days;
			break;
		}
	}
	
	public void setFlowAlarm(int rate, int index) {
		switch (index) {
		case 1:
			flowAlarm.rate_first = rate;
			break;
			
		case 2:
			flowAlarm.rate_second = rate;
			break;
		}
	}
}
