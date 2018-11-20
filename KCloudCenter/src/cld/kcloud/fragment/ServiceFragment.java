package cld.kcloud.fragment;

import java.util.ArrayList;
import com.cld.device.CldPhoneManager;
import com.cld.device.CldPhoneNet;
import com.cld.log.CldLog;
import cld.kcloud.center.KCloudAppUtils.CLDMessageId;
import cld.kcloud.center.R;
import cld.kcloud.custom.bean.KCloudPackageInfo;
import cld.kcloud.custom.manager.KCloudPackageManager;
import cld.kcloud.custom.manager.KCloudSimCardManager;
import cld.kcloud.custom.view.ServiceFragmentPagerAdapter;
import cld.kcloud.database.KCloudAppTable;
import cld.kcloud.database.KCloudPackageTable;
import cld.kcloud.database.KCloudServiceTable;
import cld.kcloud.fragment.manager.BaseFragment;
import cld.kcloud.user.KCloudUserInfoActivity;
import cld.kcloud.utils.control.CldCircleIndicator;
import cld.kcloud.utils.KCloudCommonUtil;
import cld.kcloud.widget.controller.KCloudController;
import cld.kcloud.widget.controller.KCloudWidgetList;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ServiceFragment extends BaseFragment implements OnClickListener {
	private static final String TAG = "ServiceFragment";
	private View viewService;
	public static ViewPager mViewPager;
	private CldCircleIndicator mIndicator;
	private KCloudWidgetList mWidgetList = new KCloudWidgetList();
	private ArrayList<ServiceContentFragment> fragmentList = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (viewService == null) {
			viewService = inflater.inflate(R.layout.fragment_service_manager,
					container, false);
			bindControl(R.id.service_layout_getting_fragment,
					viewService.findViewById(R.id.service_layout_getting_fragment),
					null, true, false);
			bindControl(R.id.service_layout_success_fragment,
					viewService.findViewById(R.id.service_layout_success_fragment),
					null, false, true);
			bindControl(R.id.service_pager,
					viewService.findViewById(R.id.service_pager), 
					null, true, true);
			bindControl(R.id.service_pager_indicator,
					viewService.findViewById(R.id.service_pager_indicator),
					null, true, true);
			bindControl(R.id.service_layout_failed_fragment,
					viewService.findViewById(R.id.service_layout_failed_fragment),
					this, false, true);
			bindControl(R.id.id_fragment_failed,
					viewService.findViewById(R.id.id_fragment_failed),
					this, true, true);
			bindControl(R.id.id_fragment_failed_reload,
					viewService.findViewById(R.id.id_fragment_failed_reload),
					this, true, true);
    		bindControl(R.id.id_fragment_abnormal, 
    				viewService.findViewById(R.id.id_fragment_abnormal),
    				this, true, true);
			updateUI();
		}
		return viewService;
	}

	@Override
	public boolean onBackPressed() {
		return false;
	}

	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		
		if (hidden) {
		} else {
			if (KCloudPackageManager.getInstance().getPaySuccess()) {
				updateUI();
				KCloudPackageManager.getInstance().setPaySuccess(false);
			}
		}
	}

	@Override
	public void onHandleMessage(Message message) {
		if (getActivity() == null)
			return;

		switch (message.what) {
		case CLDMessageId.MSG_ID_KGO_GETCODE_FAILED:
		case CLDMessageId.MSG_ID_KGO_GET_USER_PACKAGE_LIST_FAILED: {
			CldLog.d(TAG, " MSG_ID_KGO_GET_USER_PACKAGE_LIST_FAILED ");
			ArrayList<KCloudPackageInfo> temp = KCloudPackageTable.queryPackageInfos();
			if (temp.size() > 0) {
				KCloudPackageManager.getInstance().setPackageList(temp);
				KCloudPackageManager.getInstance().setServiceList(KCloudServiceTable.queryServiceInfos());
				KCloudPackageManager.getInstance().setAppList(KCloudAppTable.queryAppInfos());
				
				onGetUserPackageSuccess();
			} else {
				KCloudController.setVisibleById(R.id.service_layout_getting_fragment, false, mWidgetList);
				KCloudController.setVisibleById(R.id.service_layout_success_fragment, false, mWidgetList);
				KCloudController.setVisibleById(R.id.service_layout_failed_fragment, true, mWidgetList);
				KCloudController.setVisibleById(R.id.id_fragment_failed, true, mWidgetList);
				KCloudController.setVisibleById(R.id.id_fragment_abnormal, false, mWidgetList);
			}
			break;
		}

		case CLDMessageId.MSG_ID_KGO_GET_USER_PACKAGE_LIST_SUCCESS: {
			onGetUserPackageSuccess();
			break;
		}

		default:
			break;
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		/*case R.id.service_layout_failed_fragment:*/ 
		case R.id.id_fragment_failed_reload: {
			KCloudController.setVisibleById(
					R.id.service_layout_getting_fragment, true, mWidgetList);
			KCloudController.setVisibleById(
					R.id.service_layout_success_fragment, false, mWidgetList);
			KCloudController.setVisibleById(
					R.id.service_layout_failed_fragment, false, mWidgetList);

			KCloudPackageManager.getInstance().resetPackageList();
			break;
		}
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
		KCloudController.bindControl(id, view, listener, visible, enable,
				mWidgetList);
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public View getControl(int id) {
		return KCloudController.getControlById(id, mWidgetList);
	}

	public void updateUI() {
		if (!CldPhoneManager.isSimReady()){
			KCloudController.setVisibleById(R.id.service_layout_getting_fragment, false, mWidgetList);
			KCloudController.setVisibleById(R.id.service_layout_success_fragment, false, mWidgetList);
			KCloudController.setVisibleById(R.id.service_layout_failed_fragment, true, mWidgetList);
			KCloudController.setVisibleById(R.id.id_fragment_failed, false, mWidgetList);
			KCloudController.setVisibleById(R.id.id_fragment_abnormal, true, mWidgetList);
			
			TextView minor = (TextView)getControl(R.id.id_fragment_abnormal);
			if (minor != null){
				minor.setText(KCloudCommonUtil.getString(R.string.appwidget_undetected_sim));
			}
			return;
		}else{
			int simStatus = KCloudSimCardManager.getInstance().getSimStatus();
			if (simStatus != 1 && simStatus != -6 && simStatus != -999){
				KCloudController.setVisibleById(R.id.service_layout_getting_fragment, false, mWidgetList);
				KCloudController.setVisibleById(R.id.service_layout_success_fragment, false, mWidgetList);
				KCloudController.setVisibleById(R.id.service_layout_failed_fragment, true, mWidgetList);
				KCloudController.setVisibleById(R.id.id_fragment_failed, false, mWidgetList);
				KCloudController.setVisibleById(R.id.id_fragment_abnormal, true, mWidgetList);
				
				TextView minor = (TextView)getControl(R.id.id_fragment_abnormal);
				if (minor != null){
					minor.setText(KCloudCommonUtil.getString(R.string.appwidget_sim_abnormal));
				}
				return;
			}
		}
		
		if (!CldPhoneNet.isNetConnected()){
			KCloudController.setVisibleById(R.id.service_layout_getting_fragment, false, mWidgetList);
			KCloudController.setVisibleById(R.id.service_layout_success_fragment, false, mWidgetList);
			KCloudController.setVisibleById(R.id.service_layout_failed_fragment, true, mWidgetList);
			KCloudController.setVisibleById(R.id.id_fragment_failed, false, mWidgetList);
			KCloudController.setVisibleById(R.id.id_fragment_abnormal, true, mWidgetList);
			
			TextView minor = (TextView)getControl(R.id.id_fragment_abnormal);
			if (minor != null){
				minor.setText(KCloudCommonUtil.getString(R.string.appwidget_network_unconnection));
			}
			return;
		}
		
		switch (KCloudPackageManager.getInstance().getTaskStatus()) {
		case KCloudPackageManager.TASK_NONE:
			KCloudPackageManager.getInstance().resetPackageList();
			break;
			
		case KCloudPackageManager.TASK_GETTING:
			break;
			
		case KCloudPackageManager.TASK_GETED:
			onGetUserPackageSuccess();
			break;
		}
	}
	
	private void onGetUserPackageSuccess() {
		CldLog.d(TAG, " onGetUserPackageSuccess ");
		KCloudController.setVisibleById(R.id.service_layout_getting_fragment, false, mWidgetList);
		KCloudController.setVisibleById(R.id.service_layout_success_fragment, true, mWidgetList);
		KCloudController.setVisibleById(R.id.service_layout_failed_fragment, false, mWidgetList);
		
		if (KCloudPackageManager.getInstance().getPackageSize() > 0) {
			if (fragmentList != null) {
				fragmentList.clear();
			} else {
				fragmentList = new ArrayList<ServiceContentFragment>();
			}
			
			ArrayList<KCloudPackageInfo> list = KCloudPackageManager.getInstance().getPackageList();
			for (int i=0; i<list.size(); i++) {
				KCloudPackageInfo info = list.get(i);
				fragmentList.add(new ServiceContentFragment(new KCloudPackageInfo(info)));
			}

			mViewPager = (ViewPager) getControl(R.id.service_pager);
			if (mViewPager != null) {
				mViewPager.setAdapter(new ServiceFragmentPagerAdapter(
						getChildFragmentManager(), fragmentList));
			}
			
			//每次更新套餐列表，都要刷新页码
			if (fragmentList.size() <= 1) {
				KCloudController.setVisibleById(R.id.service_pager_indicator, 
						false, mWidgetList);
			} else {
				KCloudController.setVisibleById(R.id.service_pager_indicator, 
						true, mWidgetList);
				mIndicator = (CldCircleIndicator) getControl(R.id.service_pager_indicator);
				if (mIndicator != null) {
					mIndicator.setViewPager(mViewPager);
				}
			}
		} else {
			if (getActivity() != null) {
				//服务套餐个数小于0时，自动进入续费界面
				((KCloudUserInfoActivity) getActivity()).
				doChangeFragment(FragmentType.eFragment_ServiceRenewal);
			}
		}
	}
}
