package cld.kcloud.fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import net.tsz.afinal.FinalBitmap;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cld.kcloud.center.R;
import cld.kcloud.custom.bean.KCloudPackageInfo;
import cld.kcloud.custom.manager.KCloudPackageManager;
import cld.kcloud.fragment.manager.BaseFragment;
import cld.kcloud.user.KCloudUserInfoActivity;
import cld.kcloud.utils.KCloudCommonUtil;
import cld.kcloud.utils.control.CldServicePromptDialog;
import cld.kcloud.widget.controller.KCloudController;
import cld.kcloud.widget.controller.KCloudWidgetList;

@SuppressLint("ValidFragment") 
public class ServiceContentFragment extends BaseFragment implements OnClickListener {
	public static final String TAG = "ServiceContentFragment";
	private int comboServiceCount = 0;
	private View viewServiceContent;
	private FinalBitmap mFinalBitmap;
	private KCloudPackageInfo mPackageInfo;
	private KCloudWidgetList mWidgetList = new KCloudWidgetList();
	private Context mContext;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
	}
	
	public ServiceContentFragment(KCloudPackageInfo packageInfo) {
		this.mPackageInfo = packageInfo;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (viewServiceContent == null) {
			viewServiceContent = inflater.inflate(R.layout.fragment_service_content, container, false);
			bindControl(R.id.service_icon, 
					viewServiceContent.findViewById(R.id.service_icon), null, true, true);
			bindControl(R.id.service_layout_flow, 
					viewServiceContent.findViewById(R.id.service_layout_flow), null, true, true);
			bindControl(R.id.service_text_service_name, 
					viewServiceContent.findViewById(R.id.service_text_service_name), null, true, true);
			bindControl(R.id.service_text_service_number, 
					viewServiceContent.findViewById(R.id.service_text_service_number), null, false, false);
			
			bindControl(R.id.service_text_service_flow,
					viewServiceContent.findViewById(R.id.service_text_service_flow), null, true, true);
			bindControl(R.id.service_text_service_count,
					viewServiceContent.findViewById(R.id.service_text_service_count), null, true, true);
			bindControl(R.id.service_text_service_end_time,
					viewServiceContent.findViewById(R.id.service_text_service_end_time), null, true, true);
			bindControl(R.id.service_btn_watch_detail, 
					viewServiceContent.findViewById(R.id.service_btn_watch_detail), this, true, true);
			bindControl(R.id.service_btn_renewal, 
					viewServiceContent.findViewById(R.id.service_btn_renewal), this, false, true);
			bindControl(R.id.service_btn_prompt, 
					viewServiceContent.findViewById(R.id.service_btn_prompt), this, true, true);
			bindControl(R.id.service_status,
					viewServiceContent.findViewById(R.id.service_status), null, true, true);
					
			if (mFinalBitmap == null) {
				mFinalBitmap = FinalBitmap.create(mContext);
				mFinalBitmap.configLoadfailImage(R.drawable.img_combo_default);
				mFinalBitmap.configLoadingImage(R.drawable.img_combo_default);
			}
		}
		return viewServiceContent;
	}

	@SuppressLint({ "NewApi", "SimpleDateFormat" })
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		ImageView imgServiceIcon = (ImageView) getControl(R.id.service_icon);
		TextView tvServiceName = (TextView) getControl(R.id.service_text_service_name);
		TextView tvServiceFlow = (TextView) getControl(R.id.service_text_service_flow);
		TextView tvServiceCount = (TextView) getControl(R.id.service_text_service_count);
		TextView tvServiceEndTime = (TextView) getControl(R.id.service_text_service_end_time);
		ImageView imgServiceStatus = (ImageView) getControl(R.id.service_status);
		
		if (mPackageInfo == null || KCloudPackageManager.getInstance().
				getServiceList(mPackageInfo.getComboCode(), mPackageInfo.getStatus()) == null)
			return;
		
		comboServiceCount = KCloudPackageManager.getInstance().getServiceList(
				mPackageInfo.getComboCode(), mPackageInfo.getStatus()).size();
		
		// 图片
		if (imgServiceIcon != null) {
			if (mPackageInfo.getComboIcon().isEmpty()) {
				imgServiceIcon.setImageResource(R.drawable.img_combo_default);
			} else {
				mFinalBitmap.display(imgServiceIcon, mPackageInfo.getComboIcon());
			}
		}
		
		// 套餐名
		if (tvServiceName != null) {
			tvServiceName.setText(mPackageInfo.getComboName());
		}
		
		//相同套餐份数
		int num = mPackageInfo.getNumber();
		if (num > 1) {
			String str = KCloudCommonUtil.getString(R.string.service_package_number);
			String number = String.format(str, num);
			TextView tvServiceNum = (TextView) getControl(R.id.service_text_service_number);
			if (tvServiceNum != null) {
				KCloudController.setVisibleById(R.id.service_text_service_number, true, mWidgetList);
				tvServiceNum.setText(number);
			}
		} 
		
		// 包含流量
		if (mPackageInfo.getFlow() == 0) {
			KCloudController.setVisibleById(R.id.service_layout_flow, false, mWidgetList);
		} else if (tvServiceFlow != null) {
			tvServiceFlow.setText(String.valueOf(mPackageInfo.getFlow()/1000) + "G");
		}
		
		// 服务个数
		if (tvServiceCount != null) {
			String text = KCloudCommonUtil.getString(R.string.service_package_count);
			String str = String.format(text, comboServiceCount);
			tvServiceCount.setText(str);
		}
		
		// 到期时间
		if (tvServiceEndTime != null) {
			switch (mPackageInfo.getStatus()) 
			{
			case 0:
			{
				tvServiceEndTime.setText(KCloudCommonUtil.getString(R.string.service_package_tobe_open));
				break;
			}
			case 1:
			{
				long endtime = mPackageInfo.getEndtime() * 1000L;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date date = new Date(endtime);
				tvServiceEndTime.setText(sdf.format(date));
				break;
			}
			case 2:
			{
				tvServiceEndTime.setText(KCloudCommonUtil.getString(R.string.service_package_expired));
				break;
			}
			case 3:
			{
				tvServiceEndTime.setText(KCloudCommonUtil.getString(R.string.service_package_disabled));
				break;
			}
			default:
				break;
			}
		}
		
		// 状态
		if (imgServiceStatus != null) {
			switch (mPackageInfo.getStatus()) {
			
			case 0: // 未启用
				imgServiceStatus.setImageResource(R.drawable.img_package_disnable);
				KCloudController.setVisibleById(R.id.service_btn_renewal, false, mWidgetList);
				break;
			
			case 1:	// 已启用 
				imgServiceStatus.setImageResource(R.drawable.img_package_enable);
				KCloudController.setVisibleById(R.id.service_btn_renewal, true, mWidgetList);
				break;
				
			case 2: // 已过期
				imgServiceStatus.setImageResource(R.drawable.img_package_overdue);
				//解决：只有已过期的套餐，不能续费的bug add by zhaoqy 2016-8-17
				if (KCloudPackageManager.getInstance().getEnablePackage() == null) {
					//如果没有已启用的套餐, 则在已过期的套餐页面上显示“续费”按钮，提供用户续费的入口
					KCloudController.setVisibleById(R.id.service_btn_renewal, true, mWidgetList);
				} else {
					//如果有已启用的套餐，则不需要显示“续费”按钮
					KCloudController.setVisibleById(R.id.service_btn_renewal, false, mWidgetList);
				}
				break;
				
			case 3: // 已禁用（后台禁用）
				imgServiceStatus.setImageResource(R.drawable.img_package_forbid);
				KCloudController.setVisibleById(R.id.service_btn_renewal, false, mWidgetList);
				break;
				
			default: 
				break;
			}
		}
		
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.service_btn_watch_detail:
			if (getActivity() != null) {
				((KCloudUserInfoActivity) getActivity()).
				doChangeFragment(FragmentType.eFragment_ServiceDetail);
			}	
			break;
		case R.id.service_btn_renewal:
			if (getActivity() != null) {
				((KCloudUserInfoActivity) getActivity()).
				doChangeFragment(FragmentType.eFragment_ServiceRenewal);
			}
			break;
		case R.id.service_btn_prompt:
			CldServicePromptDialog.createServicePromptDialog(
					mContext,
					KCloudCommonUtil.getString(R.string.service_prompt_text))
					.show();
		}
	}

	@Override
	public boolean onBackPressed() {
		return false;
	}

	@Override
	public void onHandleMessage(Message message) {

	}

	/**
	 * 
	 * @param id
	 * @param view
	 * @param listener
	 * @param visible
	 * @param enable
	 */
	public void bindControl(int id, View view, OnClickListener listener, boolean visible, boolean enable) {
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
	
	/**
	 * 获取套餐编码,
	 * @return
	 */
	public int getComboCode() {
		if (mPackageInfo == null)
			return -1;
		
		return mPackageInfo.getComboCode();
	}
	
	public int getComboStatus() {
		if (mPackageInfo == null)
			return -1;
		
		return mPackageInfo.getStatus();
	}
}
