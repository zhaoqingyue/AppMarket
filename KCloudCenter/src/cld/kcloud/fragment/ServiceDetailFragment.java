package cld.kcloud.fragment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import cld.kcloud.center.R;
import cld.kcloud.custom.bean.KCloudPackageInfo;
import cld.kcloud.custom.bean.KCloudServiceInfo;
import cld.kcloud.custom.manager.KCloudPackageManager;
import cld.kcloud.custom.view.ServiceFragmentPagerAdapter;
import cld.kcloud.fragment.manager.BaseFragment;
import cld.kcloud.user.KCloudUserInfoActivity;
import cld.kcloud.utils.KCloudCommonUtil;
import cld.kcloud.utils.control.CldCircleIndicator;
import cld.kcloud.widget.controller.KCloudController;
import cld.kcloud.widget.controller.KCloudWidgetList;

public class ServiceDetailFragment extends BaseFragment implements OnClickListener {
	private static final String TAG = "ServiceDetailFragment";
	private View viewServiceDetail;
	private KCloudWidgetList mWidgetList = new KCloudWidgetList();
	private ArrayList<ServiceDetailContentFragment> mFragmentList = new ArrayList<ServiceDetailContentFragment>();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(viewServiceDetail == null) {
			viewServiceDetail = inflater.inflate(R.layout.fragment_service_detail, container, false);
			
			bindControl(R.id.service_detail_flow,
					viewServiceDetail.findViewById(R.id.service_detail_flow),null, true, true);
			bindControl(R.id.service_detail_count,
							viewServiceDetail.findViewById(R.id.service_detail_count),null, true, true);
			bindControl(R.id.service_detail_text_service_name, 
					viewServiceDetail.findViewById(R.id.service_detail_text_service_name), null, true, true);
			bindControl(R.id.service_detail_text_service_number, 
					viewServiceDetail.findViewById(R.id.service_detail_text_service_number), null, false, false);
			
			bindControl(R.id.service_detail_text_service_flow, 
					viewServiceDetail.findViewById(R.id.service_detail_text_service_flow), null, true, true);
			bindControl(R.id.service_detail_text_service_count, 
					viewServiceDetail.findViewById(R.id.service_detail_text_service_count), null, true, true);
			bindControl(R.id.service_detail_text_service_price, 
					viewServiceDetail.findViewById(R.id.service_detail_text_service_price), null, true, true);
			bindControl(R.id.service_detail_btn_renewal, 
					viewServiceDetail.findViewById(R.id.service_detail_btn_renewal), this, true, true);
			bindControl(R.id.service_detail_pager, 
					viewServiceDetail.findViewById(R.id.service_detail_pager), null, true, true);
			bindControl(R.id.service_detail_pager_indicator, 
					viewServiceDetail.findViewById(R.id.service_detail_pager_indicator), null, true, true);
		}
		return viewServiceDetail;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		ViewPager mViewPager = (ViewPager) getControl(R.id.service_detail_pager);
		CldCircleIndicator mIndicator = (CldCircleIndicator) getControl(R.id.service_detail_pager_indicator);
		TextView tvComboName = (TextView) getControl(R.id.service_detail_text_service_name);
		TextView tvComboFlow = (TextView) getControl(R.id.service_detail_text_service_flow);
		TextView tvServiceCount = (TextView) getControl(R.id.service_detail_text_service_count);
		TextView tvComboChagre = (TextView) getControl(R.id.service_detail_text_service_price);
		
		if(getArguments() != null) {
			//获取从ServiceContentFragment传递过来的值
			int comboCode = getArguments().getInt("comboCode");
			int comboStatus = getArguments().getInt("comboStatus");
			KCloudPackageInfo info = KCloudPackageManager.getInstance().
					getPackageInfoById(comboCode, comboStatus);
			if (info != null) {
				ArrayList<KCloudServiceInfo> list = KCloudPackageManager.getInstance().
						getServiceList(comboCode, comboStatus);
				
				if (tvComboName != null) {
					tvComboName.setText(info.getComboName());
				}
				
				//相同套餐份数
				int num = info.getNumber();
				Log.d(TAG, " num: " + num);
				if (num > 1) {
					String str = KCloudCommonUtil.getString(R.string.service_package_number);
					String number = String.format(str, num);
					TextView tvServiceNum = (TextView) getControl(R.id.service_detail_text_service_number);
					if (tvServiceNum != null) {
						KCloudController.setVisibleById(R.id.service_detail_text_service_number, true, mWidgetList);
						tvServiceNum.setText(number);
					}
				} 
				
				if (tvComboFlow != null) {
					long flow = (long)(info.getFlow()/1000);
					if (flow == 0) {	// 无流量信息不显示流量
						tvComboFlow.setVisibility(View.GONE);
						KCloudController.setVisibleById(R.id.service_detail_flow, false, mWidgetList);
						TextView tv = (TextView) getControl(R.id.service_detail_count);
						if (tv != null) {
							LinearLayout.LayoutParams params = (LayoutParams) tv.getLayoutParams();
							params.leftMargin = 0;
							tv.setLayoutParams(params); 	
						}
					} else {
						tvComboFlow.setText(String.valueOf(flow )+ "G");
					}
				}
				
				if (tvServiceCount != null) {
					String text = KCloudCommonUtil.getString(R.string.service_package_count);
					String str = String.format(text, list.size());
					tvServiceCount.setText(str);
				}
				
				if (tvComboChagre != null) {
					String text = KCloudCommonUtil.getString(R.string.service_package_chagre);
					String str = String.format(text, info.getCharges());
					tvComboChagre.setText(str);
				}
				
				int div = list.size()/4;
				int mod = list.size() % 4;
				for (int i = 0; i < div; i++) {
					mFragmentList.add(new ServiceDetailContentFragment(4, list.get(i*4 + 0).getServiceIcon(), 
							list.get(i*4 + 1).getServiceIcon(), list.get(i*4 + 2).getServiceIcon(), list.get(i*4 + 3).getServiceIcon()));
				}
				
				switch (mod) {
				case 1:
					mFragmentList.add(new ServiceDetailContentFragment(mod, 
								list.get(div*4 + 0).getServiceIcon(), "", "", ""));
					break;
						
				case 2:
					mFragmentList.add(new ServiceDetailContentFragment(mod, 
							list.get(div*4 + 0).getServiceIcon(), list.get(div*4 + 1).getServiceIcon(), "", ""));
					break;
						
				case 3:
					mFragmentList.add(new ServiceDetailContentFragment(mod, 
							list.get(div*4 + 0).getServiceIcon(), list.get(div*4 + 1).getServiceIcon(), list.get(div*4 + 2).getServiceIcon(), ""));
					break;
				}

				mViewPager.setAdapter(new ServiceFragmentPagerAdapter(
						getChildFragmentManager(), mFragmentList));
				mIndicator.setViewPager(mViewPager);
				if (list.size() <= 4) {
					KCloudController.setVisibleById(R.id.service_detail_pager_indicator, 
							false, mWidgetList);
				}
				
				//状态不为1 ：已启用，则不显示“续费”按钮
				if (info.getStatus() != 1) {
					KCloudController.setVisibleById(R.id.service_detail_btn_renewal,
							false, mWidgetList);
				}
			}
		}
		
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.service_detail_btn_renewal:
				if (getActivity() != null) {
					((KCloudUserInfoActivity) getActivity())
					.doChangeFragment(FragmentType.eFragment_ServiceRenewal);
				}
				break;
		}
		
	}

	@Override
	public boolean onBackPressed() {
		if (getActivity() != null) 
			((KCloudUserInfoActivity) getActivity()).doBack();
		return true;
	}

	@Override
	public void onHandleMessage(Message message) {
		
	}

	public void bindControl(int id, View view, OnClickListener listener,
			boolean visible, boolean enable) {
		KCloudController.bindControl(id, view, listener, visible, enable,
				mWidgetList);
	}

	public View getControl(int id) {
		return KCloudController.getControlById(id, mWidgetList);
	}
	
	@Override
	public void onDetach() {
	    super.onDetach();

	    try {
	        Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
	        childFragmentManager.setAccessible(true);
	        childFragmentManager.set(this, null);

	    } catch (NoSuchFieldException e) {
	        throw new RuntimeException(e);
	    } catch (IllegalAccessException e) {
	        throw new RuntimeException(e);
	    }
	}
}
