package cld.kcloud.fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import cld.kcloud.center.KCloudAppUtils;
import cld.kcloud.center.KCloudAppUtils.CLDMessageId;
import cld.kcloud.center.R;
import cld.kcloud.custom.manager.KCloudAlarmManager;
import cld.kcloud.custom.manager.KCloudFlowManager;
import cld.kcloud.custom.manager.KCloudSimCardManager;
import cld.kcloud.fragment.manager.BaseFragment;
import cld.kcloud.utils.KCloudCommonUtil;
import cld.kcloud.utils.KCloudConstantUtil;
import cld.kcloud.utils.KCloudShareUtils;
import cld.kcloud.utils.control.CldArcProgress;
import cld.kcloud.widget.controller.KCloudController;
import cld.kcloud.widget.controller.KCloudWidgetList;
import com.cld.device.CldPhoneManager;
import com.cld.device.CldPhoneNet;
import com.cld.log.CldLog;

public class FlowFragment extends BaseFragment implements OnClickListener {
	private static final String TAG = "FlowFragment";
	private View viewFlow = null;
	private KCloudWidgetList mWidgetList = new KCloudWidgetList(); 

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
    	if (viewFlow == null) {	
    		viewFlow = inflater.inflate(R.layout.fragment_flow_manager, container, false);
    		
    		bindControl(R.id.flow_manager_getting_fragment, viewFlow.findViewById(R.id.flow_manager_getting_fragment), 
    				null, true, true);
    		bindControl(R.id.flow_manager_success_fragment, viewFlow.findViewById(R.id.flow_manager_success_fragment), 
    				null, false, true);
    		bindControl(R.id.flow_manager_arcprogress, viewFlow.findViewById(R.id.flow_manager_arcprogress), 
    				null, true, true);
    		bindControl(R.id.flow_manager_text_flow_remain, viewFlow.findViewById(R.id.flow_manager_text_flow_remain),
    				null, true, true);
    		bindControl(R.id.flow_manager_text_name, viewFlow.findViewById(R.id.flow_manager_text_name), 
    				null, true, true);
    		/*bindControl(R.id.flow_manager_text_flow_use, viewFlow.findViewById(R.id.flow_manager_text_flow_use),
    				null, true, true);*/
    		bindControl(R.id.flow_manager_text_flow_total, viewFlow.findViewById(R.id.flow_manager_text_flow_total),
    				null, true, true);
    		bindControl(R.id.flow_manager_text_time, viewFlow.findViewById(R.id.flow_manager_text_time),
    				null, true, true);
    		
    		bindControl(R.id.flow_manager_failed_fragment, viewFlow.findViewById(R.id.flow_manager_failed_fragment), 
    				this, false, true);
    		bindControl(R.id.id_fragment_failed,viewFlow.findViewById(R.id.id_fragment_failed),
					this, true, true);
    		bindControl(R.id.id_fragment_failed_reload, viewFlow.findViewById(R.id.id_fragment_failed_reload), 
    				this, true, true);
    		bindControl(R.id.id_fragment_abnormal, viewFlow.findViewById(R.id.id_fragment_abnormal),
    				this, true, true);
    				
    		freshView();
        }  
        return viewFlow;
    }

	@Override
	public boolean onBackPressed() {
		return false;
	}
	
	@Override
	public void onResume() {
		freshView();
		super.onResume();
	}

	@Override
	public void onClick(View v) 
	{
		switch (v.getId()) {
		case R.id.id_fragment_failed_reload:
			KCloudController.setVisibleById(R.id.flow_manager_getting_fragment, true, mWidgetList);
			KCloudController.setVisibleById(R.id.flow_manager_success_fragment, false, mWidgetList);
			KCloudController.setVisibleById(R.id.flow_manager_failed_fragment, false, mWidgetList);
			KCloudFlowManager.getInstance().resetFlowStatus();
			break;
		}
	}
	
	/**
	 * 
	 * @param id
	 * @param view
	 * @param listener
	 * @param visible
	 * @param enable
	 */
	public void bindControl(int id, View view, OnClickListener listener, 
			boolean visible, boolean enable) {
		KCloudController.bindControl(id, view, listener, visible, enable, mWidgetList);
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public View getControl(int id) {
		return KCloudController.getControlById(id, mWidgetList);
	}
	
	@Override
	public void onHandleMessage(Message message) {
		CldLog.i(TAG, String.valueOf(message.what));
		switch (message.what) {
		case CLDMessageId.MSG_ID_KLDJY_FLOW_GET_SUCCESS:
		{
			KCloudCommonUtil.sendGetFlowSuccessBroadcast();
			onSuccess();
			break;
		}
		case CLDMessageId.MSG_ID_KLDJY_FLOW_GET_FAILED:
		{
			KCloudCommonUtil.sendGetFlowFailBroadcast();
			String fail = KCloudCommonUtil.getString(R.string.appwidget_refreshing);
			//String fail = "正在刷新";
			onFailed(fail);
			break;
		}
		case CLDMessageId.MSG_ID_KLDJY_FLOW_GET_FRESH:
		{
			freshView();
			break;
		}
		default:
			break;
		}
	}
	
	private void freshView()
	{
		if (!CldPhoneManager.isSimReady())
		{
			//未插SIM卡
			KCloudController.setVisibleById(R.id.flow_manager_getting_fragment, false, mWidgetList);
			KCloudController.setVisibleById(R.id.flow_manager_success_fragment, false, mWidgetList);
			KCloudController.setVisibleById(R.id.flow_manager_failed_fragment, true, mWidgetList);
			KCloudController.setVisibleById(R.id.id_fragment_failed, false, mWidgetList);
			KCloudController.setVisibleById(R.id.id_fragment_abnormal, true, mWidgetList);
	
			TextView minor = (TextView)getControl(R.id.id_fragment_abnormal);
			if (minor != null){
				String str = KCloudCommonUtil.getString(R.string.appwidget_undetected_sim);
				minor.setText(str);
				KCloudCommonUtil.sendFreshFlowBroadcast(false);
			}
			return;
		}
		else
		{
			int simStatus = KCloudSimCardManager.getInstance().getSimStatus();
			if (simStatus != 1 && simStatus != -6 && simStatus != -999)
			{
				//卡异常
				KCloudController.setVisibleById(R.id.flow_manager_getting_fragment, false, mWidgetList);
				KCloudController.setVisibleById(R.id.flow_manager_success_fragment, false, mWidgetList);
				KCloudController.setVisibleById(R.id.flow_manager_failed_fragment, true, mWidgetList);
				KCloudController.setVisibleById(R.id.id_fragment_failed, false, mWidgetList);
				KCloudController.setVisibleById(R.id.id_fragment_abnormal, true, mWidgetList);
				
				TextView minor = (TextView)getControl(R.id.id_fragment_abnormal);
				if (minor != null){
					String str = KCloudCommonUtil.getString(R.string.appwidget_sim_abnormal);
					minor.setText(str);
					KCloudCommonUtil.sendFreshFlowBroadcast(false);
				}
				return;
			}
		}
		
		if (!CldPhoneNet.isNetConnected())
		{
			//有卡，无网络（显示上一次）
			String fail = KCloudCommonUtil.getString(R.string.appwidget_network_unconnection);
			onFailed(fail);
			KCloudCommonUtil.sendFreshFlowBroadcast(false);
			return;
		}
		
		//有卡，有网络
		int status = KCloudFlowManager.getInstance().getTaskStatus();
		switch (status) 
		{
		case KCloudFlowManager.TASK_NONE:
		{
			KCloudFlowManager.getInstance().resetFlowStatus();
			break;
		}
		case KCloudFlowManager.TASK_GETTING:
		{
			break;
		}
		case KCloudFlowManager.TASK_GETED:
		{
			onSuccess();
			KCloudCommonUtil.sendFreshFlowBroadcast(false);
			break;
		}
		default:
			break;
		}
	}

	@SuppressLint({ "NewApi", "DefaultLocale", "SimpleDateFormat" }) 
	private void onSuccess() 
	{
		float used = KCloudFlowManager.getInstance().getUse();
		float total = KCloudFlowManager.getInstance().getTotal();
		KCloudShareUtils.put(KCloudAppUtils.WIDGET_KCLOUD_TOTAL_FLOW, total);
		KCloudShareUtils.put(KCloudAppUtils.WIDGET_KCLOUD_USED_FLOW, used);
		
		int type = KCloudAlarmManager.getInstance().getAlarmStatus();
		CldArcProgress arcProgress = (CldArcProgress)getControl(R.id.flow_manager_arcprogress);
		if (arcProgress != null) {
			float remain = total - used;
			//已到期，则剩余流量清零
			if (type == -1){
				remain = 0;
			}
			int progress = (int) (remain*300/total);
			float rate = ((float)progress)/300;
			arcProgress.setRate(rate);
		}
		
		TextView tv = (TextView)getControl(R.id.flow_manager_text_name);
		if (tv != null) {
			String name = KCloudFlowManager.getInstance().getPackageName();
			if (!name.isEmpty()) {
				tv.setText(name);
				KCloudShareUtils.put(KCloudAppUtils.WIDGET_KCLOUD_NAME, name);
			} else {
				tv.setVisibility(View.GONE);
			}
		}
		
		tv = (TextView)getControl(R.id.flow_manager_text_flow_remain);
		if (tv != null) {
			String value = ""; 
			double remain = total - used;
			//已到期，则剩余流量清零
			if (type == -1){
				remain = 0;
			}

			if (remain >= KCloudConstantUtil.UNIT_GB2MB) {
				value = String.format("%.1f GB", remain/KCloudConstantUtil.UNIT_GB2MB);
			} else {
				value = String.format("%.1f MB", remain);
			}
			tv.setText(value);
		}
									
		/*tv = (TextView)getControl(R.id.flow_manager_text_flow_use);
		if (tv != null) {
			String value = ""; 
			if (used >= KCloudConstantUtil.UNIT_GB2MB) {
				value = String.format("%.1f GB", used/KCloudConstantUtil.UNIT_GB2MB);
			} else {
				value = String.format("%.1f MB", used);
			}
			tv.setText(value);
		}*/
		
		tv = (TextView)getControl(R.id.flow_manager_text_flow_total);
		if (tv != null) {
			String value = ""; 
			if (total >= KCloudConstantUtil.UNIT_GB2MB) {
				value = String.format("%.1f GB", total/KCloudConstantUtil.UNIT_GB2MB);
			} else {
				value = String.format("%.1f MB", total);
			}
			tv.setText(value);
		}
		
		tv = (TextView)getControl(R.id.flow_manager_text_time);
		if (tv != null) {
			long updatetime = System.currentTimeMillis();
			KCloudShareUtils.put(KCloudAppUtils.WIDGET_KCLOUD_UPDATE_TIME, updatetime);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date date = new Date(updatetime);
			tv.setText(sdf.format(date));
		}
		
		KCloudController.setVisibleById(R.id.flow_manager_getting_fragment, false, mWidgetList);
		KCloudController.setVisibleById(R.id.flow_manager_failed_fragment, false, mWidgetList);
		KCloudController.setVisibleById(R.id.flow_manager_success_fragment, true, mWidgetList);
	}

	@SuppressLint({ "NewApi", "DefaultLocale", "SimpleDateFormat" }) 
	private void onFailed(String fail) 
	{
		Log.d(TAG, " ++++ FlowFragment onFailed ");
		long lasttime = KCloudShareUtils.getLong(KCloudAppUtils.WIDGET_KCLOUD_UPDATE_TIME, -1);
		if (lasttime == -1)
		{
			//无缓存数据， 则显示提示信息
			KCloudController.setVisibleById(R.id.flow_manager_getting_fragment, false, mWidgetList);
			KCloudController.setVisibleById(R.id.flow_manager_success_fragment, false, mWidgetList);
			KCloudController.setVisibleById(R.id.flow_manager_failed_fragment, true, mWidgetList);
			KCloudController.setVisibleById(R.id.id_fragment_failed, false, mWidgetList);
			KCloudController.setVisibleById(R.id.id_fragment_abnormal, true, mWidgetList);
			
			TextView minor = (TextView)getControl(R.id.id_fragment_abnormal);
			if (minor != null){
				minor.setText(fail);
			}
			return;
		}
		
		float used = KCloudShareUtils.getFloat(KCloudAppUtils.WIDGET_KCLOUD_USED_FLOW);
		float total = KCloudShareUtils.getFloat(KCloudAppUtils.WIDGET_KCLOUD_TOTAL_FLOW);
		
		CldArcProgress arcProgress = (CldArcProgress)getControl(R.id.flow_manager_arcprogress);
		if (arcProgress != null) {
			float remain = total - used;
			int progress = (int) (remain*300/total);
			float rate = ((float)progress)/300;
			arcProgress.setRate(rate);
		}
		
		TextView tv = (TextView)getControl(R.id.flow_manager_text_name);
		if (tv != null) {
			String name = KCloudShareUtils.getString(KCloudAppUtils.WIDGET_KCLOUD_NAME);
			if (!name.isEmpty()) {
				tv.setText(name);
			} else {
				tv.setVisibility(View.GONE);
			}
		}
		
		tv = (TextView)getControl(R.id.flow_manager_text_flow_remain);
		if (tv != null) {
			String value = ""; 
			double remain = total - used;
			if (remain >= KCloudConstantUtil.UNIT_GB2MB) {
				value = String.format("%.1f GB", remain/KCloudConstantUtil.UNIT_GB2MB);
			} else {
				value = String.format("%.1f MB", remain);
			}
			tv.setText(value);
		}
									
		/*tv = (TextView)getControl(R.id.flow_manager_text_flow_use);
		if (tv != null) {
			String value = ""; 
			if (used >= KCloudConstantUtil.UNIT_GB2MB) {
				value = String.format("%.1f GB", used/KCloudConstantUtil.UNIT_GB2MB);
			} else {
				value = String.format("%.1f MB", used);
			}
			tv.setText(value);
		}*/
		
		tv = (TextView)getControl(R.id.flow_manager_text_flow_total);
		if (tv != null) {
			String value = ""; 
			if (total >= KCloudConstantUtil.UNIT_GB2MB) {
				value = String.format("%.1f GB", total/KCloudConstantUtil.UNIT_GB2MB);
			} else {
				value = String.format("%.1f MB", total);
			}
			tv.setText(value);
		}
		
		tv = (TextView)getControl(R.id.flow_manager_text_time);
		if (tv != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date date = new Date(lasttime);
			tv.setText(sdf.format(date));
		}
		
		KCloudController.setVisibleById(R.id.flow_manager_getting_fragment, false, mWidgetList);
		KCloudController.setVisibleById(R.id.flow_manager_success_fragment, true, mWidgetList);
		KCloudController.setVisibleById(R.id.flow_manager_failed_fragment, false, mWidgetList);
	}
}
