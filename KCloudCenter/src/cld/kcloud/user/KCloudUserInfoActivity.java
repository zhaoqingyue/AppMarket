package cld.kcloud.user;

import java.util.List;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import cld.kcloud.center.KCloudAppUtils;
import cld.kcloud.center.KCloudAppUtils.CLDMessageId;
import cld.kcloud.center.R;
import cld.kcloud.custom.bean.KCloudPackageInfo;
import cld.kcloud.custom.manager.KCloudPackageManager;
import cld.kcloud.custom.view.PersonalMessage;
import cld.kcloud.fragment.CarFragment;
import cld.kcloud.fragment.CarPlateSelectorFragment;
import cld.kcloud.fragment.CarSelectorFragment;
import cld.kcloud.fragment.FlowFragment;
import cld.kcloud.fragment.PersonalCityFragment;
import cld.kcloud.fragment.PersonalFragment;
import cld.kcloud.fragment.PersonalMessageDetailFragment;
import cld.kcloud.fragment.PersonalMessageFragment;
import cld.kcloud.fragment.PersonalMobileFragment;
import cld.kcloud.fragment.PersonalPasswordFragment;
import cld.kcloud.fragment.ServiceDetailFragment;
import cld.kcloud.fragment.ServiceFragment;
import cld.kcloud.fragment.ServiceRenewalFragment;
import cld.kcloud.fragment.manager.BaseFragment;
import cld.kcloud.fragment.manager.BaseFragment.BackHandledInterface;
import cld.kcloud.fragment.manager.BaseFragment.FragmentType;
import cld.kcloud.fragment.manager.ManagerFragment;
import cld.kcloud.user.KCloudUser.CldOnMessageInterface;
import cld.kcloud.widget.controller.KCloudController;
import cld.kcloud.widget.controller.KCloudWidgetList;

/**
 * K云用户信息
 * @author wuyl
 */

@SuppressLint("NewApi")
public class KCloudUserInfoActivity extends FragmentActivity implements BackHandledInterface, OnCheckedChangeListener, OnClickListener {

	public static enum LayoutState {
		eLayout_personal, 
		eLayout_carinfo, 
		eLayout_service, 
		eLayout_netdata,
	};

	private RadioGroup mFragmentSelector = null;
	private KCloudWidgetList mWidgetList = new KCloudWidgetList();
	private LayoutState mLayoutState = LayoutState.eLayout_personal;
	private ManagerFragment mPersonal_Info_Manager = new ManagerFragment(); // 个人信息
	private ManagerFragment mCar_Info_Manager = new ManagerFragment();      // 爱车信息
	private ManagerFragment mService_Info_Manager = new ManagerFragment();  // 服务信息
	private ManagerFragment mFlow_Info_Manager = new ManagerFragment();     // 流量信息
	private boolean mDestroy = false;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_userinfo);

		mDestroy = false;
		// 设置回调
		KCloudUser.getInstance().setOnMessageListener(mCldOnMessageListener);
		// 初始化控件
		initControl();
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//强制隐藏输入法
		getWindow().setSoftInputMode(WindowManager.
				LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDestroy = true;
	}
	
	@Override
	public void setSelectedFragment(BaseFragment selectedFragment) {

	}

	@Override
	public void onBackPressed() {
		boolean result = false;

		switch (mLayoutState) {
		case eLayout_personal:
			result = mPersonal_Info_Manager.backPressed();
			break;

		case eLayout_carinfo:
			result = mCar_Info_Manager.backPressed();
			break;

		case eLayout_service:
			result = mService_Info_Manager.backPressed();
			break;

		case eLayout_netdata:
			result = mFlow_Info_Manager.backPressed();
			break;

		default:
			break;
		}

		if (!result) {
			super.onBackPressed();
		}
	}
	
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
			case R.id.userinfo_layout_personal: {
				if (mLayoutState == LayoutState.eLayout_personal)
					return;
				break;
			}
			case R.id.userinfo_layout_carinfo: {
				if (mLayoutState == LayoutState.eLayout_carinfo)
					return;
				break;
			}
			case R.id.userinfo_layout_service: {
				if (mLayoutState == LayoutState.eLayout_service)
					return;
				break;
			}
			case R.id.userinfo_layout_netdata: {
				if (mLayoutState == LayoutState.eLayout_netdata)
					return;
				break;
			}
		}
		
		// 更换manger
		changeLayoutState(checkedId);
	}

	public void bindControl(int id, View view, OnClickListener listener, boolean visible, boolean enable) {
		KCloudController.bindControl(id, view, listener, visible, enable, mWidgetList);
	}

	public View getControl(int id) {
		return KCloudController.getControlById(id, mWidgetList);
	}

	public void initControl() {
		mFragmentSelector = (RadioGroup) findViewById(R.id.fragment_selector);
		mFragmentSelector.setOnCheckedChangeListener(this);	
		
		// android RadioButton 添加事件，点击时出现点击声音的效果 
		findViewById(R.id.userinfo_layout_personal).setOnClickListener(
				new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			}
		});
		
		findViewById(R.id.userinfo_layout_carinfo).setOnClickListener(
				new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			}
		});
		
		findViewById(R.id.userinfo_layout_service).setOnClickListener(
				new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			}
		});
		
		findViewById(R.id.userinfo_layout_netdata).setOnClickListener(
				new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			}
		});
		
		// 添加fragment
		BaseFragment fragment;
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		
		int extra = getIntent().getIntExtra(KCloudAppUtils.START_ACTIVITY_EXTRA, 0);
		switch (extra) {
		case KCloudAppUtils.FRAGMENT_FLOW:
			mFragmentSelector.check(R.id.userinfo_layout_netdata);
			changeLayoutState(R.id.userinfo_layout_netdata);
			break;
		case KCloudAppUtils.FRAGMENT_RENEWAL:
			if (mLayoutState != LayoutState.eLayout_service) {
				mFragmentSelector.check(R.id.userinfo_layout_service);
				changeLayoutState(R.id.userinfo_layout_service);
			}
			
			if (!showFragment(mService_Info_Manager, "eFragment_ServiceRenewal")) {
				fragment = new ServiceRenewalFragment();
				// 获取最近过期的套餐
				KCloudPackageInfo info = null;
				// 已到期，则取最近过期的套餐
				info = KCloudPackageManager.getInstance().getNewExpirationPackage();
				
				if (info != null) {
					Bundle args = new Bundle();
					args.putInt("comboCode", info.getComboCode());
					args.putInt("comboStatus", info.getStatus());
					fragment.setArguments(args);
				}
				hideFragments(mService_Info_Manager);
				addFragment(mService_Info_Manager, fragment, "eFragment_ServiceRenewal");
			}
			break;
		default:
			// 个人信息fragment
			fragment = new PersonalFragment();
			fragmentTransaction.add(R.id.fragment_userinfo_container, fragment, "eFragment_Personal");
			mPersonal_Info_Manager.pushFragment(fragment, "eFragment_Personal");
			//fragmentTransaction.commit();
			fragmentTransaction.commitAllowingStateLoss();
			break;
		}
	}

	public void changeLayoutState(int id) {
		BaseFragment fragment = null;

		switch (mLayoutState) {
		case eLayout_personal: // 个人信息
			hideFragments(mPersonal_Info_Manager);
			break;

		case eLayout_carinfo: // 爱车信息
			hideFragments(mCar_Info_Manager);
			break;

		case eLayout_service: // 服务信息
			hideFragments(mService_Info_Manager);
			break;

		case eLayout_netdata: // 流量信息
			hideFragments(mFlow_Info_Manager);
			break;

		default:
			break;
		}

		switch (id) {
		case R.id.userinfo_layout_personal: { // 个人信息
			mLayoutState = LayoutState.eLayout_personal;
			if (!showFragment(mPersonal_Info_Manager, null)) {
				fragment = new PersonalFragment();
				addFragment(mPersonal_Info_Manager, fragment, "eFragment_Personal");
			}
			
			findViewById(R.id.userinfo_layout_left_spilt_personal_carinfo).setVisibility(View.INVISIBLE);
			findViewById(R.id.userinfo_layout_left_spilt_carinfo_service).setVisibility(View.VISIBLE);
			findViewById(R.id.userinfo_layout_left_spilt_service_flow).setVisibility(View.VISIBLE);
			break;
		}

		case R.id.userinfo_layout_carinfo: { // 爱车信息
			mLayoutState = LayoutState.eLayout_carinfo;
			if (!showFragment(mCar_Info_Manager, null)) {
				fragment = new CarFragment();
				addFragment(mCar_Info_Manager, fragment, "eFragment_Car");
			}
			
			findViewById(R.id.userinfo_layout_left_spilt_personal_carinfo).setVisibility(View.INVISIBLE);
			findViewById(R.id.userinfo_layout_left_spilt_carinfo_service).setVisibility(View.INVISIBLE);
			findViewById(R.id.userinfo_layout_left_spilt_service_flow).setVisibility(View.VISIBLE);
			break;
		}

		case R.id.userinfo_layout_service: { // 服务信息
			mLayoutState = LayoutState.eLayout_service;
			if (!showFragment(mService_Info_Manager, null)) {
				fragment = new ServiceFragment();
				addFragment(mService_Info_Manager, fragment, "eFragment_Service");
			}
			
			findViewById(R.id.userinfo_layout_left_spilt_personal_carinfo).setVisibility(View.VISIBLE);
			findViewById(R.id.userinfo_layout_left_spilt_carinfo_service).setVisibility(View.INVISIBLE);
			findViewById(R.id.userinfo_layout_left_spilt_service_flow).setVisibility(View.INVISIBLE);
			break;
		}

		case R.id.userinfo_layout_netdata: { // 流量信息
			mLayoutState = LayoutState.eLayout_netdata;
			if (!showFragment(mFlow_Info_Manager, null)) {
				fragment = new FlowFragment();
				addFragment(mFlow_Info_Manager, fragment, "eFragment_Flow");
			}
			
			findViewById(R.id.userinfo_layout_left_spilt_personal_carinfo).setVisibility(View.VISIBLE);
			findViewById(R.id.userinfo_layout_left_spilt_carinfo_service).setVisibility(View.VISIBLE);
			findViewById(R.id.userinfo_layout_left_spilt_service_flow).setVisibility(View.INVISIBLE);
			break;
		}
		}
	}

	/**
	 * 添加fragment
	 * 
	 * @param manager
	 * @param fragment
	 * @param Tag
	 */
	public void addFragment(ManagerFragment manager, BaseFragment basefragment, String Tag) {
		if (manager != null && basefragment != null) {
			BaseFragment fragment = null;
			fragment = manager.findFragmentByTag(Tag);
			if (fragment == null) {
				FragmentManager fragmentManager = getSupportFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

				fragmentTransaction.add(R.id.fragment_userinfo_container, basefragment, Tag);
				manager.pushFragment(basefragment, Tag);
				//fragmentTransaction.commit();
				fragmentTransaction.commitAllowingStateLoss();
			}
		}
	}

	/**
	 * 
	 * @param manager
	 * @param basefragment
	 */
	public void removeFragment(ManagerFragment manager, BaseFragment basefragment) {
		if (manager != null && basefragment != null) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction.remove(basefragment);
			manager.popFragment();
			//fragmentTransaction.commit();
			fragmentTransaction.commitAllowingStateLoss();
		}
	}

	/**
	 * 隐藏fragment组
	 * 
	 * @param manager
	 */
	public void hideFragments(ManagerFragment manager) {
		if (manager != null) {
			List<BaseFragment> list = manager.getFragments();
			if (list != null) {
				FragmentManager fragmentManager = getSupportFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				for (int i = 0; i < list.size(); i++) {
					BaseFragment fragment = list.get(i);
					if (fragment != null) {
						fragmentTransaction.hide(fragment);
					}
				}
				//fragmentTransaction.commit();
				fragmentTransaction.commitAllowingStateLoss();
			}
		}
	}

	/**
	 * 
	 * @param manager
	 * @param fragment
	 */
	public boolean showFragment(ManagerFragment manager, String Tag) {
		if (manager != null) {
			BaseFragment fragment = null;

			if (Tag == null || "".equals(Tag)) {
				fragment = manager.getLastFragment();
			} else {
				fragment = manager.findFragmentByTag(Tag);
			}

			if (fragment != null) {
				FragmentManager fragmentManager = getSupportFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				fragmentTransaction.show(fragment);
				//fragmentTransaction.commit();
				fragmentTransaction.commitAllowingStateLoss();
				return true;
			}
		}

		return false;
	}

	/**
	 * FragmentType 类型
	 * 
	 * @param eType
	 */
	public void doChangeFragment(FragmentType eType) {
		BaseFragment fragment;

		switch (eType) {
		// 个人信息
		case eFragment_PersonalMessage: {
			if (!showFragment(mPersonal_Info_Manager,
					"eFragment_PersonalMessage")) {
				fragment = new PersonalMessageFragment();
				hideFragments(mPersonal_Info_Manager);
				addFragment(mPersonal_Info_Manager, fragment,
						"eFragment_PersonalMessage");
			}
			break;
		}

		// 信息详情
		case eFragment_PersonalMessageDetail: {
			if (!showFragment(mPersonal_Info_Manager,
					"eFragment_PersonalMessageDetail")) {
				fragment = new PersonalMessageDetailFragment();
				BaseFragment parentFragment = mPersonal_Info_Manager
						.findFragmentByTag("eFragment_PersonalMessage");
				if (parentFragment != null) {
					PersonalMessage message = ((PersonalMessageFragment) parentFragment)
							.getPersonalMessage();

					Bundle args = new Bundle();
					args.putLong("msgId", message.getMsgId());
					args.putLong("msgTime", message.getTime());
					args.putString("msgTitle", message.getMsgTitle());
					args.putString("msgContent", message.getMsgContent());
					fragment.setArguments(args);
				}

				hideFragments(mPersonal_Info_Manager);
				addFragment(mPersonal_Info_Manager, fragment,
						"eFragment_PersonalMessageDetail");
			}
			break;
		}	

		// 修改城市
		case eFragment_PersonalCity: {
			if (!showFragment(mPersonal_Info_Manager, "eFragment_PersonalCity")) {
				fragment = new PersonalCityFragment();
				hideFragments(mPersonal_Info_Manager);
				addFragment(mPersonal_Info_Manager, fragment,
						"eFragment_PersonalCity");
			}
			break;
		}

		// 修改手机
		case eFragment_PersonalMobile: {
			if (!showFragment(mPersonal_Info_Manager,
					"eFragment_PersonalMobile")) {
				fragment = new PersonalMobileFragment();
				hideFragments(mPersonal_Info_Manager);
				addFragment(mPersonal_Info_Manager, fragment,
						"eFragment_PersonalMobile");
			}
			break;
		}
		
		// 修改密码
		case eFragment_PersonalPassword: {
			if (!showFragment(mPersonal_Info_Manager,
					"eFragment_PersonalPassword")) {
				fragment = new PersonalPasswordFragment();
				hideFragments(mPersonal_Info_Manager);
				addFragment(mPersonal_Info_Manager, fragment,
						"eFragment_PersonalPassword");
			}
			break;
		}		
		
		//车辆选择
		case eFragment_CarSelector: {
			if (!showFragment(mCar_Info_Manager, "eFragment_CarSelector")) {
				BaseFragment parentFragment = mCar_Info_Manager
						.findFragmentByTag("eFragment_Car");
				fragment = new CarSelectorFragment((CarFragment)parentFragment);
				if (parentFragment != null) {
					boolean flag = ((CarFragment) parentFragment).getCarsInfoFlag();
					String result = ((CarFragment) parentFragment).getCarsInfo();
					Bundle args = new Bundle();
					args.putBoolean("flag", flag);
					args.putString("result", result);
					fragment.setArguments(args);
				}
				
				hideFragments(mCar_Info_Manager);
				addFragment(mCar_Info_Manager, fragment, "eFragment_CarSelector");
			}
			break;
		}
		
		//车牌选择
		case eFragment_CarPlateSelector: {
			if (!showFragment(mCar_Info_Manager, "eFragment_CarPlateSelector")) {
				BaseFragment parentFragment = mCar_Info_Manager
						.findFragmentByTag("eFragment_Car");
				fragment = new CarPlateSelectorFragment((CarFragment)parentFragment);
				hideFragments(mCar_Info_Manager);
				addFragment(mCar_Info_Manager, fragment, "eFragment_CarPlateSelector");
			}
			break;
		}
		
		// 服务
		case eFragment_Service: {
			if (!showFragment(mService_Info_Manager, "eFragment_Service")) {
				fragment = new ServiceFragment();
				hideFragments(mService_Info_Manager);
				addFragment(mService_Info_Manager, fragment, "eFragment_Service");
			}
			break;
		}

		// 服务续费
		case eFragment_ServiceRenewal: {
			if (!showFragment(mService_Info_Manager, "eFragment_ServiceRenewal")) {
				fragment = new ServiceRenewalFragment();
				
				if (KCloudPackageManager.getInstance().getPackageSize() > 0) {
					int index = ServiceFragment.mViewPager.getCurrentItem();
					KCloudPackageInfo packageInfo = KCloudPackageManager.getInstance().
							getPackageList().get(index);
					if(packageInfo != null){
						Bundle args = new Bundle();
						args.putInt("comboCode", packageInfo.getComboCode());
						args.putInt("comboStatus", packageInfo.getStatus());
						fragment.setArguments(args);
					}
				}
				
				hideFragments(mService_Info_Manager);
				addFragment(mService_Info_Manager, fragment,
						"eFragment_ServiceRenewal");
			}
			break;
		}
		
		// 服务详情
		case eFragment_ServiceDetail: {
			if (!showFragment(mService_Info_Manager, "eFragment_ServiceDetail")) {
				fragment = new ServiceDetailFragment();
				
				if (KCloudPackageManager.getInstance().getPackageSize() > 0) {
					int index = ServiceFragment.mViewPager.getCurrentItem();
					KCloudPackageInfo packageInfo = KCloudPackageManager.getInstance().
							getPackageList().get(index);
					if(packageInfo != null){
						Bundle args = new Bundle();
						args.putInt("comboCode", packageInfo.getComboCode());
						args.putInt("comboStatus", packageInfo.getStatus());
						fragment.setArguments(args);
					}
				}
				
				hideFragments(mService_Info_Manager);
				addFragment(mService_Info_Manager, fragment,
						"eFragment_ServiceDetail");
			}
			break;
		}

		default:
			break;
		}
	}

	/**
	 * 返回
	 */
	public void doBack() {
		switch (mLayoutState) {
		case eLayout_personal: {
			BaseFragment fragment = mPersonal_Info_Manager.getLastFragment();
			removeFragment(mPersonal_Info_Manager, fragment);
			showFragment(mPersonal_Info_Manager, null);
			break;
		}

		case eLayout_carinfo: {
			BaseFragment fragment = mCar_Info_Manager.getLastFragment();
			removeFragment(mCar_Info_Manager, fragment);
			showFragment(mCar_Info_Manager, null);
			break;
		}

		case eLayout_service: {
			BaseFragment fragment = mService_Info_Manager.getLastFragment();
			removeFragment(mService_Info_Manager, fragment);
			showFragment(mService_Info_Manager, null);
			break;
		}

		case eLayout_netdata: {
			hideFragments(mFlow_Info_Manager);
			showFragment(mFlow_Info_Manager, null);
			break;
		}

		default:
			break;
		}
	}

	private CldOnMessageInterface mCldOnMessageListener = new CldOnMessageInterface() {
		@Override
		public void OnHandleMessage(Message message) {
			//已退出用户信息界面，则不响应
			if (mDestroy)
				return;
			
			switch (message.what) {
			case CLDMessageId.MSG_ID_PASSWORD_SET_PWD_SUCCESS:
			case CLDMessageId.MSG_ID_PASSWORD_SET_PWD_FAILED:
			case CLDMessageId.MSG_ID_USERINFO_GETDETAIL_SUCCESS:
			case CLDMessageId.MSG_ID_USERINFO_GETDETAIL_FAILED:
			case CLDMessageId.MSG_ID_USERINFO_REVISE_MOBILE_VERICODE_SUCCESS:
			case CLDMessageId.MSG_ID_USERINFO_REVISE_MOBILE_VERICODE_FAILED:
			case CLDMessageId.MSG_ID_USERINFO_REVISE_MOBILE_SUCCESS:
			case CLDMessageId.MSG_ID_USERINFO_REVISE_MOBILE_FAILED:
			case CLDMessageId.MSG_ID_USERINFO_BIND_MOBILE_VERICODE_SUCCESS:
			case CLDMessageId.MSG_ID_USERINFO_BIND_MOBILE_VERICODE_FAILED:
			case CLDMessageId.MSG_ID_USERINFO_BIND_MOBILE_SUCCESS:
			case CLDMessageId.MSG_ID_USERINFO_BIND_MOBILE_FAILED:
			case CLDMessageId.MSG_ID_USERINFO_PWD_VERICODE_SUCCESS:
			case CLDMessageId.MSG_ID_USERINFO_PWD_VERICODE_FAILED:
			case CLDMessageId.MSG_ID_USERINFO_PWD_CHECK_SUCCESS:
			case CLDMessageId.MSG_ID_USERINFO_PWD_CHECK_FAILED:
			case CLDMessageId.MSG_ID_USERINFO_UPDATE_SUCCESS:
			case CLDMessageId.MSG_ID_USERINFO_UPDATE_FAILED:
			case CLDMessageId.MSG_ID_USERINFO_LOGOUT_SUCCESS:	
			case CLDMessageId.MSG_ID_USERINFO_LOGOUT_FAILED:
			case CLDMessageId.MSG_ID_LOCATION_CHANGE:
			case CLDMessageId.MSG_ID_MSGBOX_UPDATE: {
				BaseFragment fragment = mPersonal_Info_Manager.getLastFragment();
				if (fragment != null) {
					fragment.onHandleMessage(message);
				}
				break;
			}

			case CLDMessageId.MSG_ID_KGO_GET_CARLIST_SUCCESS:
			case CLDMessageId.MSG_ID_KGO_GET_CARLIST_FAILED: {
				BaseFragment fragment = mCar_Info_Manager.getLastFragment();
				if (fragment != null) {
					fragment.onHandleMessage(message);
				}
			}
			
			case CLDMessageId.MSG_ID_KLDJY_FLOW_GET_SUCCESS:
			case CLDMessageId.MSG_ID_KLDJY_FLOW_GET_FAILED: {
				BaseFragment fragment = mFlow_Info_Manager.getLastFragment();
				if (fragment != null) {
					fragment.onHandleMessage(message);
				}
				break;
			}
			
			case CLDMessageId.MSG_ID_KGO_GET_USER_PACKAGE_LIST_SUCCESS: 
			case CLDMessageId.MSG_ID_KGO_GET_USER_PACKAGE_LIST_FAILED: {
				BaseFragment fragment = mService_Info_Manager.getLastFragment();
				if (fragment != null) {
					fragment.onHandleMessage(message);
				}
				break;
			}
			
			case CLDMessageId.MSG_ID_SHOW_SERVICE_LIST: {
				BaseFragment fragment = mService_Info_Manager.getLastFragment();
				if (fragment != null) {
					while (!"eFragment_Service".equals(fragment.getTag())) {
						removeFragment(mService_Info_Manager, fragment);
						fragment = mService_Info_Manager.getLastFragment();
					}
				}
				
				if (mLayoutState != LayoutState.eLayout_service) {
					mFragmentSelector.check(R.id.userinfo_layout_service);
					changeLayoutState(R.id.userinfo_layout_service);
				}
				else {
					//解决：点击"立即查看"后，套餐列表界面显示空白
					showFragment(mService_Info_Manager, null);
				}
				break;
			}
			
			case CLDMessageId.MSG_ID_SHOW_RENEWAL_QRCODE: {
				BaseFragment fragment = null;
				
				if (mLayoutState != LayoutState.eLayout_service) {
					mFragmentSelector.check(R.id.userinfo_layout_service);
					changeLayoutState(R.id.userinfo_layout_service);
				}
				
				if (!showFragment(mService_Info_Manager, "eFragment_ServiceRenewal")) {
					fragment = new ServiceRenewalFragment();
					
					// 获取最近过期的套餐
					KCloudPackageInfo info = null;
					if ((Integer)message.obj == 1) {
						// 即将到期
						info = KCloudPackageManager.getInstance().getEnablePackage();
					} else {
						// 已到期，则取最近过期的套餐
						info = KCloudPackageManager.getInstance().getNewExpirationPackage();
					}
					
					if (info != null) {
						Bundle args = new Bundle();
						args.putInt("comboCode", info.getComboCode());
						args.putInt("comboStatus", info.getStatus());
						fragment.setArguments(args);
					}
					hideFragments(mService_Info_Manager);
					addFragment(mService_Info_Manager, fragment, "eFragment_ServiceRenewal");
				}
				break;
			}
			
			case CLDMessageId.MSG_ID_LOGIN_SESSION_INVAILD: {				
				BaseFragment fragment = mPersonal_Info_Manager.getLastFragment();
				if (fragment != null) {
					fragment.onHandleMessage(message);
				}
				break;
			}
			}
		}
	};

	@Override
	public void onClick(View arg0) {
		
	}
}
