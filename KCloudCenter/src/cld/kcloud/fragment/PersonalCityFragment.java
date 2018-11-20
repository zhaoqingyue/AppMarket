package cld.kcloud.fragment;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import com.cld.log.CldLog;
import com.cld.ols.api.CldKAccountAPI;
import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;
import cld.kcloud.center.R;
import cld.kcloud.center.KCloudAppUtils.CLDMessageId;
import cld.kcloud.custom.bean.KCloudUserInfo;
import cld.kcloud.fragment.manager.BaseFragment;
import cld.kcloud.user.KCloudUser;
import cld.kcloud.user.KCloudUserInfoActivity;
import cld.kcloud.utils.KCloudLocationUtils;
import cld.kcloud.utils.KCloudLocationUtils.IKCloudLocationListener;
import cld.kcloud.utils.KCloudRegionUtils;
import cld.kcloud.utils.control.CldProgress;
import cld.kcloud.utils.KCloudCommonUtil;
import cld.kcloud.utils.control.CldProgress.CldProgressListener;
import cld.kcloud.widget.controller.KCloudController;
import cld.kcloud.widget.controller.KCloudWidgetList;
import cld.navi.region.CldRegionEx;
import cld.navi.region.CldRegionEx.CityLevel;
import cld.navi.region.CldRegionEx.DistrictLevel;
import cld.navi.region.CldRegionEx.ProvinceLevel;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;

public class PersonalCityFragment extends BaseFragment implements
		OnClickListener, OnWheelChangedListener {
	private static final String TAG = "PersonalCityFragment";

	private View viewCity = null;
	private WheelView province = null;
	private WheelView city = null;
	private WheelView district = null;
	private KCloudWidgetList mWidgetList = new KCloudWidgetList();

	private Timer checkTimer = null; // 检测超时定时器 5s超时后直接取消定位
	private String curLocatName = null; // 文本显示
	private RotateAnimation animToLocate; // 定位动画
	private String male = KCloudCommonUtil.getString(R.string.setting_male);
	private Context mContext;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (viewCity == null) {
			viewCity = inflater.inflate(R.layout.fragment_city_manager,
					container, false);

			bindControl(R.id.city_manager_layout_location,
					viewCity.findViewById(R.id.city_manager_layout_location),
					this, true, true);
			bindControl(R.id.city_manager_image_tolocation,
					viewCity.findViewById(R.id.city_manager_image_tolocation),
					this, true, true);
			bindControl(R.id.city_manager_image_showlocation,
					viewCity.findViewById(R.id.city_manager_image_showlocation),
					this, false, true);
			bindControl(R.id.city_manager_text_location,
					viewCity.findViewById(R.id.city_manager_text_location),
					this, true, true);
			bindControl(R.id.city_manager_edit_location,
					viewCity.findViewById(R.id.city_manager_edit_location),
					this, true, false);
			bindControl(R.id.city_manager_btn_save,
					viewCity.findViewById(R.id.city_manager_btn_save), 
					this, true, false);
			// 省
			province = (WheelView) viewCity.findViewById(R.id.city_manager_wheel_province);
			province.setVisibleItems(3);
			province.setWheelBackground(R.drawable.dialog_wheel_bg2);
			province.setCenterRectColor(0xff707070);
			province.setShadowColor(0xff2e2e2e, 0xbb2e2e2e, 0x772e2e2e);
			province.setViewAdapter(new ProvinceAdapter(mContext));

			// 市
			city = (WheelView) viewCity.findViewById(R.id.city_manager_wheel_city);
			city.setVisibleItems(3);
			city.setCenterRectColor(0xff707070);
			city.setWheelBackground(R.drawable.dialog_wheel_bg2);
			city.setShadowColor(0xff2e2e2e, 0xbb2e2e2e, 0x772e2e2e);

			// 区
			district = (WheelView) viewCity.findViewById(R.id.city_manager_wheel_district);
			district.setVisibleItems(3);
			district.setCenterRectColor(0xff707070);
			district.setWheelBackground(R.drawable.dialog_wheel_bg2);
			district.setShadowColor(0xff2e2e2e, 0xbb2e2e2e, 0x772e2e2e);

			province.addChangingListener(this);
			city.addChangingListener(this);
			district.addChangingListener(this);
			
			//触发onChanged以显示市、区,onChanged默认oldValue为0
			province.setCurrentItem(1);
			province.setCurrentItem(0);

			// 定位动画
			Interpolator _Interpolator = new LinearInterpolator();
			animToLocate = new RotateAnimation(0, 359,
					Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			animToLocate.setInterpolator(_Interpolator);
			animToLocate.setDuration(650);
			animToLocate.setRepeatCount(Integer.MAX_VALUE);
			animToLocate.setFillAfter(true);

			// 定位
			initCurAddr();
		}
		return viewCity;
	}

	@Override
	public void onDestroy() {
		KCloudLocationUtils.stopLocation();
		super.onDestroy();
	}

	@Override
	public boolean onBackPressed() {
		stopTimeoutTask();
		if (getActivity() != null)
			((KCloudUserInfoActivity) getActivity()).doBack();
		return true;
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.city_manager_image_tolocation:
		case R.id.city_manager_text_location:
		case R.id.city_manager_edit_location:
		case R.id.city_manager_layout_location: {
			initCurAddr();
			break;
		}

		case R.id.city_manager_btn_save: {
			// 保存区域名称
			KCloudUserInfo uif = KCloudUser.getInstance().getUserInfo();
			int sex = male.equals(uif.getSex()) ? 2 : 1;

			int index = province.getCurrentItem();
			CharSequence provinceName = ((ProvinceAdapter) province
					.getViewAdapter()).getItemText(index);
			curLocatName = provinceName.toString();

			int id = ((ProvinceAdapter) province.getViewAdapter())
					.getItemId(index);
			
			// 直辖市
			if (CldRegionEx.getInstance().isMunicipality(id)) {
				curLocatName += KCloudCommonUtil.getString(R.string.person_city);
			} else if (CldRegionEx.getInstance().isSpecialDistrict(id)) {
				curLocatName += KCloudCommonUtil.getString(R.string.person_special_region);
			} else {
				// 非直辖市或港、澳、台
				index = city.getCurrentItem();
				CharSequence cityName = ((CityAdapter) city.getViewAdapter())
						.getItemText(index);

				if (CldRegionEx.getInstance().isAutonomousRegions(id)) {
					switch (id/10000) {
					case 45:
						curLocatName += KCloudCommonUtil.getString(R.string.person_zhuang_region);
						break;					
					case 64:
						curLocatName += KCloudCommonUtil.getString(R.string.person_hui_region);
						break;
					case 65:
						curLocatName += KCloudCommonUtil.getString(R.string.person_uygur_region);
						break;
					case 15:
					case 54:
						curLocatName += KCloudCommonUtil.getString(R.string.person_auto_region);
						break;
					default:
						curLocatName += KCloudCommonUtil.getString(R.string.person_province);
						break;
					}
				}

				curLocatName += cityName.toString();
			}

			if (district.getViewAdapter().getItemsCount() > 0) {
				index = district.getCurrentItem();
				CharSequence distsName = ((DistrictAdapter) district
						.getViewAdapter()).getItemText(index);
				curLocatName += distsName.toString();
			}

			CldProgress.showProgress(mContext,
					KCloudCommonUtil.getString(R.string.common_network_data_update),
					new CldProgressListener() {
						public void onCancel() {
						}
					});

			KCloudUser.getInstance().getTmpUserInfo().setDistName(curLocatName);
			CldKAccountAPI.getInstance().updateUserInfo(null, null, null, null,
					sex, null, -1, -1, -1, curLocatName);

			break;
		}
		default:
			break;
		} 
	}

	@SuppressLint("HandlerLeak") 
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 101:
				// 定位成功
				stopLocatAnim();
				Bundle bundle = msg.getData();
				String provinceName = bundle.getString("provinceName");
				String cityName = bundle.getString("cityName");
				String distsName = bundle.getString("distsName");

				// 省
				if (!TextUtils.isEmpty(provinceName)) {
					ProvinceLevel provinceItem = CldRegionEx.getInstance()
							.searchProvince(provinceName);
					if (CldRegionEx.getInstance().isMunicipality(
							provinceItem.id)
							|| CldRegionEx.getInstance().isSpecialDistrict(
									provinceItem.id)) {
						curLocatName = provinceName + distsName;
					} else {
						curLocatName = provinceName + cityName + distsName;
					}

					int result = ((ProvinceAdapter) province.getViewAdapter())
							.getIndexof(provinceItem);
					if (result != -1) {
						CldLog.i(TAG, String.valueOf(result));
						province.setCurrentItem(result);
					}
				}

				// 市
				if (!TextUtils.isEmpty(cityName)) {
					CityLevel CityItem = CldRegionEx.getInstance().searchCity(
							cityName);
					int result = ((CityAdapter) city.getViewAdapter())
							.getIndexof(CityItem);
					if (result != -1) {
						CldLog.i(TAG, String.valueOf(result));
						city.setCurrentItem(result);
					}
				}

				// 区
				if (!TextUtils.isEmpty(distsName)) {
					DistrictLevel DistrictItem = CldRegionEx.getInstance()
							.searchDistrict(distsName);
					int result = ((DistrictAdapter) district.getViewAdapter())
							.getIndexof(DistrictItem);
					if (result != -1) {
						CldLog.i(TAG, String.valueOf(result));
						district.setCurrentItem(result);
					}
				}

				break;
			case 102:
				// 定位失败
				stopLocatAnim();
				KCloudCommonUtil.makeText(R.string.kaccount_locate_failed);
				break;
			}

			EditText editLocator = (EditText) getControl(R.id.city_manager_edit_location);
			if (editLocator != null) {
				editLocator.setText(curLocatName);
			}
		};
	};

	/**
	 * 开始定位动画
	 * 
	 * @return void
	 */
	private void startLocatAnim() {
		KCloudController.setVisibleById(R.id.city_manager_image_tolocation,
				false, mWidgetList);
		ImageView imv = (ImageView) getControl(R.id.city_manager_image_showlocation);
		if (imv != null) {
			imv.setVisibility(View.VISIBLE);
			imv.startAnimation(animToLocate);
		}
	}

	/**
	 * 停止定位动画
	 * 
	 * @return void
	 */
	private void stopLocatAnim() {
		KCloudController.setVisibleById(R.id.city_manager_image_tolocation,
				true, mWidgetList);
		ImageView imv = (ImageView) getControl(R.id.city_manager_image_showlocation);
		if (imv != null) {
			imv.setVisibility(View.GONE);
			imv.clearAnimation();
		}
	}

	/**
	 * 初始化当前位置
	 * 
	 * @return void
	 */
	private void initCurAddr() {
		CldLog.i(TAG, "initCurAddr");

		curLocatName = KCloudCommonUtil.getString(R.string.person_location);
		startLocatAnim();
		startTimeoutTask();
		KCloudLocationUtils.startLocation(new IKCloudLocationListener () {

			@Override
			public void onLocation(double latitude, double longtitude) {
				stopTimeoutTask();

				if (KCloudCommonUtil.getString(R.string.person_unable_location).equals(curLocatName)) {
					return ;
				}

				KCloudRegionUtils.getRegionDistsName(longtitude, latitude,
						new KCloudRegionUtils.IGetRigonCallback() {

							@SuppressLint("NewApi") 
							@Override
							public void onResult(int regionId, String provinceName, String cityName,
									String distsName) {
								
								CldLog.i(TAG, "regionId: " + regionId);
								CldLog.i(TAG, provinceName + cityName + distsName);
								if (!distsName.isEmpty()) {
									Message message = mHandler.obtainMessage();
									Bundle bundle = new Bundle();
									bundle.putString("provinceName", provinceName);
									bundle.putString("cityName", cityName);
									bundle.putString("distsName", distsName);
									message.what = 101;
									message.setData(bundle);
									mHandler.sendMessage(message);
								}
							}
						});
			}
			
		});

		EditText editLocator = (EditText) getControl(R.id.city_manager_edit_location);
		if (editLocator != null) {
			editLocator.setText(curLocatName);
		}
	}

	@Override
	public void onChanged(WheelView wheel, int oldValue, int newValue) {
		if (wheel == province) {
			updateCities();
		} else if (wheel == city) {
			updateDistricts();
		}
		
		KCloudController.setEnabledById(R.id.city_manager_btn_save, true, mWidgetList);
	}

	/**
	 * Updates the city wheel
	 */
	private void updateCities() {
		if (getActivity() == null)
			return;
		
		int current = province.getCurrentItem();
		int id = ((ProvinceAdapter) province.getViewAdapter())
				.getItemId(current);
		city.setViewAdapter(new CityAdapter(mContext, id));
		city.setCurrentItem(0);
		updateDistricts();
	}

	/**
	 * Updates the District wheel
	 */
	private void updateDistricts() {
		if (getActivity() == null)
			return;
		
		int current = city.getCurrentItem();
		int id = ((CityAdapter) city.getViewAdapter()).getItemId(current);
		district.setViewAdapter(new DistrictAdapter(mContext, id));
		district.setCurrentItem(0);
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

	/**
	 * Adapter for countries
	 */
	private class ProvinceAdapter extends AbstractWheelTextAdapter {
		// Countries names
		private ArrayList<ProvinceLevel> list = new ArrayList<ProvinceLevel>();

		/**
		 * Constructor
		 */
		protected ProvinceAdapter(Context context) {
			super(context);

			setItemResource(R.layout.layout_city_selector_wheel_item);
			setItemTextResource(R.id.city_selector_wheel_item_text);
			list = CldRegionEx.getInstance().getProvinceList();
		}

		@Override
		public View getItem(int index, View cachedView, ViewGroup parent) {
			View view = super.getItem(index, cachedView, parent);
			return view;
		}

		@Override
		public int getItemsCount() {
			return list.size();
		}

		@Override
		protected CharSequence getItemText(int index) {
			ProvinceLevel item = list.get(index);
			if (item != null) {
				return item.name;
			}
			return null;
		}

		public int getItemId(int index) {
			ProvinceLevel item = list.get(index);
			if (item != null) {
				return item.id;
			}

			return 0;
		}

		public int getIndexof(ProvinceLevel item) {
			return list.indexOf(item);
		}
	}

	/**
	 * Adapter for countries
	 */
	private class CityAdapter extends AbstractWheelTextAdapter {
		// Countries names
		private ArrayList<CityLevel> list = new ArrayList<CityLevel>();

		/**
		 * Constructor
		 */
		protected CityAdapter(Context context, int provinceId) {
			super(context);

			if (provinceId <= 0)
				return;

			setItemResource(R.layout.layout_city_selector_wheel_item);
			setItemTextResource(R.id.city_selector_wheel_item_text);
			list = CldRegionEx.getInstance()
					.getCityListByProvinceId(provinceId);
		}

		@Override
		public View getItem(int index, View cachedView, ViewGroup parent) {
			View view = super.getItem(index, cachedView, parent);
			return view;
		}

		@Override
		public int getItemsCount() {
			return list.size();
		}

		@Override
		protected CharSequence getItemText(int index) {
			CityLevel item = list.get(index);
			if (item != null) {
				return item.name;
			}
			return null;
		}

		public int getItemId(int index) {
			CityLevel item = list.get(index);
			if (item != null) {
				return item.id;
			}

			return 0;
		}

		public int getIndexof(CityLevel item) {
			return list.indexOf(item);
		}
	}

	/**
	 * Adapter for countries
	 */
	private class DistrictAdapter extends AbstractWheelTextAdapter {
		// Countries names
		private ArrayList<DistrictLevel> list = new ArrayList<DistrictLevel>();

		/**
		 * Constructor
		 */
		protected DistrictAdapter(Context context, int cityId) {
			super(context);

			if (cityId <= 0)
				return;
			setItemResource(R.layout.layout_city_selector_wheel_item);
			setItemTextResource(R.id.city_selector_wheel_item_text);
			list = CldRegionEx.getInstance().getDistrictListByCityId(cityId);
		}

		@Override
		public View getItem(int index, View cachedView, ViewGroup parent) {
			View view = super.getItem(index, cachedView, parent);
			return view;
		}

		@Override
		public int getItemsCount() {
			return list.size();
		}

		@Override
		protected CharSequence getItemText(int index) {
			DistrictLevel item = list.get(index);
			if (item != null) {
				return item.name;
			}
			return null;
		}

		public int getIndexof(DistrictLevel item) {
			return list.indexOf(item);
		}
	}

	/**
	 * 
	 * @param message
	 */
	public void onHandleMessage(Message message) {
		CldLog.i(TAG, String.valueOf(message.what));

		switch (message.what) {
		case CLDMessageId.MSG_ID_USERINFO_UPDATE_SUCCESS: {
			if (CldProgress.isShowProgress()) {
				CldProgress.cancelProgress();
			}
			if (getActivity() != null)
				((KCloudUserInfoActivity) getActivity()).doBack();
			break;
		}

		case CLDMessageId.MSG_ID_USERINFO_UPDATE_FAILED: {
			if (CldProgress.isShowProgress()) {
				CldProgress.cancelProgress();
			}
			KCloudCommonUtil.makeText(R.string.userinfo_set_distname_failed);
			break;
		}
		}
	}

	/**
	 * 停止超时检测定时器
	 * 
	 * @return void
	 */
	private void stopTimeoutTask() {
		if (null != checkTimer) {
			checkTimer.cancel();
			checkTimer = null;
		}
	}

	/**
	 * 定位
	 * 
	 * @return void
	 */
	private void startTimeoutTask() {
		stopTimeoutTask();
		if (null == checkTimer) {
			checkTimer = new Timer();
			checkTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					if (KCloudCommonUtil.getString(R.string.person_location).equals(curLocatName)) {
						curLocatName = KCloudCommonUtil.getString(R.string.person_unable_location);
						CldLog.i(TAG, curLocatName);
						mHandler.sendEmptyMessage(102);
					} else {
						mHandler.sendEmptyMessage(101);
					}
				}
			}, 10000);
		}
	}
}
