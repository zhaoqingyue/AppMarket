package cld.kcloud.appwidget;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import cld.kcloud.center.KCloudAppUtils;
import cld.kcloud.center.KCloudCtx;
import cld.kcloud.center.R;
import cld.kcloud.custom.manager.KCloudAlarmManager;
import cld.kcloud.custom.manager.KCloudFlowManager;
import cld.kcloud.custom.manager.KCloudSimCardManager;
import cld.kcloud.utils.KCloudCommonUtil;
import cld.kcloud.utils.KCloudConstantUtil;
import cld.kcloud.utils.KCloudShareUtils;
import com.cld.device.CldPhoneManager;
import com.cld.device.CldPhoneNet;

@SuppressLint("DefaultLocale") 
public class KCloudWidget extends AppWidgetProvider
{
	private static final String TAG = "KCloudWidget";
	static AppWidgetManager mAppWidgetManager;
	static RemoteViews mRemoteViews;
	static ComponentName mComponentName;
	static boolean mIsSuccessLayoutShow = false;
	
	private int[] appwidgetflow = 
	{
		R.drawable.appwidget_flow_0,
		R.drawable.appwidget_flow_1,
		R.drawable.appwidget_flow_2,
		R.drawable.appwidget_flow_3,
		R.drawable.appwidget_flow_4,
		R.drawable.appwidget_flow_5,
		R.drawable.appwidget_flow_6,
		R.drawable.appwidget_flow_7,
		R.drawable.appwidget_flow_8,
		R.drawable.appwidget_flow_9,
		R.drawable.appwidget_flow_10,
		R.drawable.appwidget_flow_11,
		R.drawable.appwidget_flow_12
	};
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		super.onReceive(context, intent);
		if (context == null)
			context = KCloudCtx.getAppContext();
		
		if (intent.getAction().equals(KCloudAppUtils.ACTION_FLOW_FRESH))
		{
			resetRemoteView(context);
			freshView(context);
		}
		else if (intent.getAction().equals(KCloudAppUtils.ACTION_FLOW_GET_SUCCESS))
		{
			resetRemoteView(context);
			onSuccess(context);
		}
		else if (intent.getAction().equals(KCloudAppUtils.ACTION_FLOW_GET_FAILED))
		{
			resetRemoteView(context);
			String fail = KCloudCommonUtil.getString(R.string.appwidget_refreshing);
			onFailed(context, fail);
		}
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		if (context == null)
			context = KCloudCtx.getAppContext();

		mAppWidgetManager = appWidgetManager;
		mComponentName	= new ComponentName(context, KCloudWidget.class);
		mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_kcloud);
		
		Intent clickIntent = new Intent();
		clickIntent.setClassName(KCloudAppUtils.TARGET_PACKAGE_NAME, 
				KCloudAppUtils.TARGET_CLASS_NAME_USER);
		clickIntent.putExtra(KCloudAppUtils.START_ACTIVITY_EXTRA, KCloudAppUtils.FRAGMENT_FLOW);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, clickIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
		mRemoteViews.setOnClickPendingIntent(R.id.id_widget_kcloud, pendingIntent);
		mRemoteViews.setOnClickPendingIntent(R.id.id_widget_kcloud_background, pendingIntent);
		freshView(context);
	}
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) 
	{
		super.onDeleted(context, appWidgetIds);
		mAppWidgetManager = null;
		mRemoteViews = null;
		mComponentName = null;
		mIsSuccessLayoutShow = false;
	}
	
	private void resetRemoteView(Context context)
	{
		if (mRemoteViews != null)
			mRemoteViews = null;
		
		if (mAppWidgetManager == null)
			mAppWidgetManager = null;
		
		if (mComponentName == null)
			mComponentName = null;
		
		mAppWidgetManager = AppWidgetManager.getInstance(context);
		mComponentName	= new ComponentName(context, KCloudWidget.class);
		mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_kcloud);
		
		Intent clickIntent = new Intent();
		clickIntent.setClassName(KCloudAppUtils.TARGET_PACKAGE_NAME, 
				KCloudAppUtils.TARGET_CLASS_NAME_USER);
		clickIntent.putExtra(KCloudAppUtils.START_ACTIVITY_EXTRA, KCloudAppUtils.FRAGMENT_FLOW);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, clickIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
		mRemoteViews.setOnClickPendingIntent(R.id.id_widget_kcloud, pendingIntent);
		mRemoteViews.setOnClickPendingIntent(R.id.id_widget_kcloud_background, pendingIntent);
	}
	
	private void freshView(Context context)
	{
		if (!CldPhoneManager.isSimReady())
		{
			mIsSuccessLayoutShow = false;
			//未插SIM卡
			mRemoteViews.setViewVisibility(R.id.id_widget_kcloud_layout_getting, View.GONE);
			mRemoteViews.setViewVisibility(R.id.id_widget_kcloud_layout_success, View.GONE);
			mRemoteViews.setViewVisibility(R.id.id_widget_kcloud_layout_failed, View.VISIBLE);
			mRemoteViews.setImageViewResource(R.id.id_widget_kcloud_failed_icon, R.drawable.appwidget_icon_exception);
			String str = KCloudCommonUtil.getString(R.string.appwidget_undetected_sim);
			mRemoteViews.setTextViewText(R.id.id_widget_kcloud_failed_tip, str);
			mAppWidgetManager.updateAppWidget(mComponentName, mRemoteViews);
			return;
		}
		else
		{
			int simStatus = KCloudSimCardManager.getInstance().getSimStatus();
			Log.d(TAG, " ++++ simStatus: " + simStatus);
			if (simStatus != 1 && simStatus != -6 && simStatus != -999)
			{
				mIsSuccessLayoutShow = false;
				//卡异常
				mRemoteViews.setViewVisibility(R.id.id_widget_kcloud_layout_getting, View.GONE);
				mRemoteViews.setViewVisibility(R.id.id_widget_kcloud_layout_success, View.GONE);
				mRemoteViews.setViewVisibility(R.id.id_widget_kcloud_layout_failed, View.VISIBLE);
				mRemoteViews.setImageViewResource(R.id.id_widget_kcloud_failed_icon, R.drawable.appwidget_icon_exception);
				String str = KCloudCommonUtil.getString(R.string.appwidget_sim_abnormal);
				mRemoteViews.setTextViewText(R.id.id_widget_kcloud_failed_tip, str);
				mAppWidgetManager.updateAppWidget(mComponentName, mRemoteViews);
				return;
			}
		}
		
		if (!CldPhoneNet.isNetConnected())
		{
			Log.d(TAG, " ++++ 网络未连接 ++++ ");
			String fail = KCloudCommonUtil.getString(R.string.appwidget_network_unconnection);
			onFailed(context, fail);
			return;
		}
		
		//有卡，有网络
		int status = KCloudFlowManager.getInstance().getTaskStatus();
		switch (status) 
		{
		case KCloudFlowManager.TASK_NONE:
		{
			//防止widget界面由正常显示 --> 正在加载
			if (mIsSuccessLayoutShow)
				return;
			
			mRemoteViews.setViewVisibility(R.id.id_widget_kcloud_layout_getting, View.VISIBLE);
			mRemoteViews.setViewVisibility(R.id.id_widget_kcloud_layout_success, View.GONE);
			mRemoteViews.setViewVisibility(R.id.id_widget_kcloud_layout_failed, View.GONE);
			mAppWidgetManager.updateAppWidget(mComponentName, mRemoteViews);
			break;
		}
		case KCloudFlowManager.TASK_GETTING:
		{
			break;
		}
		case KCloudFlowManager.TASK_GETED:
		{
			//有卡，有网络, 获取成功
			onSuccess(context);
			break;
		}
		default:
			break;
		}
	}
	
	@SuppressLint({ "NewApi", "SimpleDateFormat" }) 
	private void onSuccess(Context context)
	{
		mIsSuccessLayoutShow = true;
		mRemoteViews.setViewVisibility(R.id.id_widget_kcloud_layout_failed, View.GONE);
		mRemoteViews.setViewVisibility(R.id.id_widget_kcloud_layout_getting, View.GONE);
		mRemoteViews.setViewVisibility(R.id.id_widget_kcloud_layout_success, View.VISIBLE);
		
		String str = KCloudCommonUtil.getString(R.string.appwidget_updated_just);
		mRemoteViews.setTextViewText(R.id.id_widget_kcloud_update_time, str);
		
		long updatetime = System.currentTimeMillis();
		Log.d(TAG, " updatetime: " + updatetime);
		KCloudShareUtils.put(KCloudAppUtils.WIDGET_KCLOUD_UPDATE_TIME, updatetime);
		
		//Widget更新时间
		//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		//Date date = new Date(updatetime);
		//String curtime = sdf.format(date);
		//Log.d(TAG, " curtime: " + curtime);
		//mRemoteViews.setTextViewText(R.id.id_widget_kcloud_curtime, curtime);
		
		int type = KCloudAlarmManager.getInstance().getAlarmStatus();
		Log.d(TAG, " AlarmStatus: " + type);
		KCloudShareUtils.put(KCloudAppUtils.WIDGET_KCLOUD_STATUS, type);
		mRemoteViews.setTextViewText(R.id.id_widget_kcloud_maturity, getTipByStatus());
		
		float used = KCloudFlowManager.getInstance().getUse();
		float total = KCloudFlowManager.getInstance().getTotal();
		float remain = total - used;
		//Log.d(TAG, " ++++   used: " + used);
		//Log.d(TAG, " ++++  total: " + total);
	
		int progress = 0;
		String value = "";
		String unit = "";
		if (remain >= KCloudConstantUtil.UNIT_GB2MB) 
		{
			value = String.format("%.1f", remain/KCloudConstantUtil.UNIT_GB2MB);
			unit = "GB";
		}
		else 
		{
			value = String.format("%.1f", remain);
			unit = "MB";
		}
		
		if (total > 0 && remain >= 0 && remain <= total)
		{
			progress = (int) (remain*12/total);
			if (progress == 0 && remain > 0)
			{
				progress = 1;
			}
		}
		
		//已到期，则剩余流量清零
		if (type == -1)
		{
			value = "0";
			unit = "MB";
			progress = 0;
		}		

		mRemoteViews.setImageViewResource(R.id.id_widget_kcloud_progress, appwidgetflow[progress]);
		mRemoteViews.setTextViewText(R.id.id_widget_kcloud_flow_remain, value);
		mRemoteViews.setTextViewText(R.id.id_widget_kcloud_flow_unit, unit);
		
		KCloudShareUtils.put(KCloudAppUtils.WIDGET_KCLOUD_PROGRESS, progress);
		KCloudShareUtils.put(KCloudAppUtils.WIDGET_KCLOUD_REMAIN_FLOW, value);
		KCloudShareUtils.put(KCloudAppUtils.WIDGET_KCLOUD_FLOW_UNIT, unit);
		
		String name = KCloudFlowManager.getInstance().getPackageName();
		mRemoteViews.setTextViewText(R.id.id_widget_kcloud_name, name);
		KCloudShareUtils.put(KCloudAppUtils.WIDGET_KCLOUD_NAME, name);
		mAppWidgetManager.updateAppWidget(mComponentName, mRemoteViews);
	}
	
	private void onFailed(Context context, String fail)
	{
		long lasttime = KCloudShareUtils.getLong(KCloudAppUtils.WIDGET_KCLOUD_UPDATE_TIME, -1);
		if (lasttime == -1)
		{
			mIsSuccessLayoutShow = false;
			//无缓存数据， 则显示提示信息
			mRemoteViews.setViewVisibility(R.id.id_widget_kcloud_layout_getting, View.GONE);
			mRemoteViews.setViewVisibility(R.id.id_widget_kcloud_layout_success, View.GONE);
			mRemoteViews.setViewVisibility(R.id.id_widget_kcloud_layout_failed, View.VISIBLE);
			mRemoteViews.setImageViewResource(R.id.id_widget_kcloud_failed_icon, R.drawable.appwidget_icon_failed);
			mRemoteViews.setTextViewText(R.id.id_widget_kcloud_failed_tip, fail);
			mAppWidgetManager.updateAppWidget(mComponentName, mRemoteViews);
			return;
		}
		
		mIsSuccessLayoutShow = true;
		mRemoteViews.setViewVisibility(R.id.id_widget_kcloud_layout_getting, View.GONE);
		mRemoteViews.setViewVisibility(R.id.id_widget_kcloud_layout_success, View.VISIBLE);
		mRemoteViews.setViewVisibility(R.id.id_widget_kcloud_layout_failed, View.GONE);
		
		long curtime = System.currentTimeMillis();
		long between = curtime - lasttime;
		//Log.d(TAG, " curtime: " + curtime);
		//Log.d(TAG, "lasttime: " + lasttime);
		//Log.d(TAG, " between: " + between);
		mRemoteViews.setTextViewText(R.id.id_widget_kcloud_update_time, getUpdateTime(between));
		int progress =  KCloudShareUtils.getInt(KCloudAppUtils.WIDGET_KCLOUD_PROGRESS);
		String value = KCloudShareUtils.getString(KCloudAppUtils.WIDGET_KCLOUD_REMAIN_FLOW);
		String unit = KCloudShareUtils.getString(KCloudAppUtils.WIDGET_KCLOUD_FLOW_UNIT);
		mRemoteViews.setImageViewResource(R.id.id_widget_kcloud_progress, appwidgetflow[progress]);
		mRemoteViews.setTextViewText(R.id.id_widget_kcloud_flow_remain, value);
		mRemoteViews.setTextViewText(R.id.id_widget_kcloud_flow_unit, unit);

		String name = KCloudShareUtils.getString(KCloudAppUtils.WIDGET_KCLOUD_NAME);
		mRemoteViews.setTextViewText(R.id.id_widget_kcloud_name, name);
		mRemoteViews.setTextViewText(R.id.id_widget_kcloud_maturity, getTipByStatus());
		mAppWidgetManager.updateAppWidget(mComponentName, mRemoteViews);
	}
	
	private String getUpdateTime(long between)
	{
		String updatetime = "";
		if (between < 0)
		{
			//当前系统时间出错
		}
		else if (between >= 0 && between < 3600*1000L)
		{
			//小于1小时
			updatetime = KCloudCommonUtil.getString(R.string.appwidget_updated_just);
		}
		else if (between >= 3600*1000L && between < 3600*1000*24L)
		{
			//大于或等于1小时，小于1天
			updatetime = between/(3600*1000L) + 
					KCloudCommonUtil.getString(R.string.appwidget_updated_hours_ago);
		}
		else if (between >= 3600*1000*24L && between < 3600*1000*24*30L)
		{
			//大于或等于1天，小于1月
			updatetime = between/(3600*1000*24L) + 
					KCloudCommonUtil.getString(R.string.appwidget_updated_days_ago);
		}
		else if (between >= 3600*1000*24*30L && between < 3600*1000*24*30*12L)
		{
			//大于或等于1月，小于1年
			updatetime = between/(3600*1000*24*30L) + 
					KCloudCommonUtil.getString(R.string.appwidget_updated_months_ago);
		}
		else
		{
			//大于或等于1年
			updatetime = between/(3600*1000*24*30*12L) + 
					KCloudCommonUtil.getString(R.string.appwidget_updated_years_ago);
		}
		return updatetime;
	}
	
	private String getTipByStatus()
	{
		String tips = "";
		int type = KCloudShareUtils.getInt(KCloudAppUtils.WIDGET_KCLOUD_STATUS);
		switch (type) 
		{
		case 0:
		{
			tips = KCloudCommonUtil.getString(R.string.appwidget_status_using);
			break;
		}
		case -1:
		{
			tips = KCloudCommonUtil.getString(R.string.appwidget_status_expired);
			break;
		}
		case -2:
		{
			tips = KCloudCommonUtil.getString(R.string.appwidget_status_expiring);
			break;
		}	
		default:
			break;
		}
		return tips;
	}
}
