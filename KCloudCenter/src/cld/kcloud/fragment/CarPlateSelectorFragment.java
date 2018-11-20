package cld.kcloud.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.cld.device.CldPhoneNet;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ReplacementTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import cld.kcloud.center.R;
import cld.kcloud.custom.bean.KCloudUserInfo;
import cld.kcloud.datastore.KCloudCarStore;
import cld.kcloud.fragment.manager.BaseFragment;
import cld.kcloud.user.KCloudUser;
import cld.kcloud.user.KCloudUserInfoActivity;
import cld.kcloud.utils.KCloudUserUtils;
import cld.kcloud.utils.control.CldInputDialog;
import cld.kcloud.utils.KCloudCommonUtil;
import cld.kcloud.widget.controller.KCloudController;
import cld.kcloud.widget.controller.KCloudWidgetList;
import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;

@SuppressLint("ValidFragment") 
public class CarPlateSelectorFragment extends BaseFragment implements OnClickListener, OnWheelChangedListener {
	private View viewCarPlateSelector = null;
	private KCloudWidgetList mWidgetList = new KCloudWidgetList();
	private EditText editPlate;
	private Button btnSave;
	private WheelView wheelProvince;
	private WheelView wheelCityLetter;
	private List<String> provinceList;
	private Map<String, Integer> provinceIndexMap;
	private Context mContext;
	
	private static ICarPlateSelectorListener mCallBack = null;
	
	public interface ICarPlateSelectorListener {
		public void onCarPlateSelectorResult(String strBrand);
	}
	
	public CarPlateSelectorFragment(ICarPlateSelectorListener iSaveCallBack) {
		mCallBack = iSaveCallBack;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (viewCarPlateSelector == null) {
			viewCarPlateSelector = inflater.inflate(R.layout.fragment_car_plateselector, container, false);
			
			bindControl(R.id.plate_selector_wheel_province, 
					viewCarPlateSelector.findViewById(R.id.plate_selector_wheel_province),
					null, true, true);
			bindControl(R.id.plate_selector_wheel_letter, 
					viewCarPlateSelector.findViewById(R.id.plate_selector_wheel_letter), 
					null, true, true);
			bindControl(R.id.plate_selector_btn_save, 
					viewCarPlateSelector.findViewById(R.id.plate_selector_btn_save), 
					this, true, true);
			bindControl(R.id.plate_selector_edit_plate, 
					viewCarPlateSelector.findViewById(R.id.plate_selector_edit_plate), 
					this, true, true);
		}
		init();
		return viewCarPlateSelector;
	}
	
	private void init(){
		wheelProvince = (WheelView) getControl(R.id.plate_selector_wheel_province);
		wheelCityLetter = (WheelView) getControl(R.id.plate_selector_wheel_letter);
		btnSave = (Button) getControl(R.id.plate_selector_btn_save);
		editPlate = (EditText) getControl(R.id.plate_selector_edit_plate);

		btnSave.requestFocus();
		btnSave.setOnClickListener(this);
		editPlate.setTransformationMethod(new AllCapTransformationMethod());
		editPlate.addTextChangedListener(new NumberLetterTextWatcher());
		
		final EditText et = (EditText) getControl(R.id.plate_selector_edit_plate);
		if (et != null) {
			et.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus == true) {
						String title = KCloudCommonUtil.getString(
								R.string.input_dialog_title_plate);
						String hint = KCloudCommonUtil.getString(
								R.string.input_dialog_hint_plate);
						
						CldInputDialog.showInputDialog(mContext, title, hint, et
								.getText().toString(),
								CldInputDialog.CldInputType.eInputType_CarPlate,
								CldInputDialog.CldButtonType.eButton_Confirm,
								new CldInputDialog.CldInputDialogListener() {

									@Override
									public void onOk(String strInput) {
										et.setText(strInput);
										et.setSelection(strInput.length());
										KCloudUserUtils.setInputMethodVisible(mContext, et, false);
									}

									@Override
									public void onCancel() {
										KCloudUserUtils.setInputMethodVisible(mContext, et, false);
										Button btnSave = (Button) getControl(R.id.plate_selector_btn_save);
										if (btnSave != null) {
											btnSave.requestFocus();
										}
									}
								});
					}
				}
			});
		}
		
		initProvinceList();
		initWheel();
	}
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private void initWheel(){
		//防止已返回上一个页面
		if (getContext() == null)
			return;
		
		wheelProvince.setVisibleItems(3);
		wheelProvince.setShadowColor(0xff2e2e2e, 0xbb2e2e2e, 0x772e2e2e);
		wheelProvince.setWheelBackground(R.drawable.dialog_wheel_bg);
		wheelProvince.setCenterRectColor(0xff57595d);
		wheelProvince.setViewAdapter(new ProvinceAdapter(getContext()));
		
		wheelCityLetter.setVisibleItems(3);
		wheelCityLetter.setShadowColor(0xff2e2e2e, 0xbb2e2e2e, 0x772e2e2e);
		wheelCityLetter.setWheelBackground(R.drawable.dialog_wheel_bg);
		wheelCityLetter.setCenterRectColor(0xff57595d);
		wheelCityLetter.setViewAdapter(new CityLetterAdapter(getContext()));

		wheelProvince.addChangingListener(this);
		wheelCityLetter.addChangingListener(this);
		
		int cityIndex = 0;
		int provinceIndex = 0;
		if (KCloudCarStore.getInstance().get().plate_num.isEmpty()) {
			KCloudUserInfo info = KCloudUser.getInstance().getUserInfo();
			if (info != null && !info.getDistName().isEmpty()
					&& !KCloudCommonUtil.getString(R.string.setting_unset).equals(info.getDistName())) {
				// 根据用户所在地区设置默认车牌简称
				provinceIndex = provinceIndexMap.get(info.getDistName().substring(0, 2));
			}
		} else {
			int size = ((ProvinceAdapter)wheelProvince.getViewAdapter()).getItemsCount();
			for (int i = 0; i < size; i++) {
				String st = ((ProvinceAdapter)wheelProvince.getViewAdapter()).getItemText(i).toString();
				if (st.equals(KCloudCarStore.getInstance().get().plate_num.substring(0, 1))) {
					provinceIndex = i;
					break;
				}
			}
			
			size = ((CityLetterAdapter)wheelCityLetter.getViewAdapter()).getItemsCount();
			for (int i = 0; i < size; i++) {
				String st = ((CityLetterAdapter)wheelCityLetter.getViewAdapter()).getItemText(i).toString();
				if (st.equals(KCloudCarStore.getInstance().get().plate_num.substring(1, 2))) {
					cityIndex = i;
					break;
				}
			}
			
			if (editPlate != null) {
				editPlate.setText(KCloudCarStore.getInstance().get().plate_num.substring(2));
			}
		}
		
		wheelProvince.setCurrentItem(provinceIndex);
		wheelCityLetter.setCurrentItem(cityIndex);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@SuppressLint({ "NewApi", "DefaultLocale" }) 
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.plate_selector_btn_save : {
				if (getActivity() == null) 
					return;
				
				int index = wheelProvince.getCurrentItem();
				CharSequence province = ((ProvinceAdapter) wheelProvince
						.getViewAdapter()).getItemText(index);

				index = wheelCityLetter.getCurrentItem();
				CharSequence cityLetter = ((CityLetterAdapter) wheelCityLetter
						.getViewAdapter()).getItemText(index);

				String strBrand = editPlate.getText().toString().replaceAll(" ", "");
				if (strBrand.isEmpty()) {
					KCloudCommonUtil.makeText(R.string.userinfo_set_car_num_empty);
					return;
				}
				
				if (strBrand.contains("I") || strBrand.contains("O")) {
					KCloudCommonUtil.makeText(R.string.userinfo_set_car_num_error);
					return;
				}
				
				if (strBrand.length() != 5) {
					KCloudCommonUtil.makeText(R.string.userinfo_set_car_num_error);
					return;
				}
				
				if (!CldPhoneNet.isNetConnected()) {
					KCloudCommonUtil.makeText(R.string.common_network_abnormal);
					return;
				}

				if (mCallBack != null) {
					mCallBack.onCarPlateSelectorResult(province.toString() +
							cityLetter.toString() + " " + strBrand.toUpperCase());
				}
				
				((KCloudUserInfoActivity) getActivity()).doBack();
				break;
			}
			
			case R.id.plate_selector_edit_plate: {
				final EditText et = (EditText) v;
				String title = KCloudCommonUtil.getString(
						R.string.input_dialog_title_plate);
				String hint = KCloudCommonUtil.getString(
						R.string.input_dialog_hint_plate);
				
				CldInputDialog.showInputDialog(mContext, title, hint, 
						et.getText().toString(),
						CldInputDialog.CldInputType.eInputType_CarPlate,
						CldInputDialog.CldButtonType.eButton_Confirm,
						new CldInputDialog.CldInputDialogListener() {

							@Override
							public void onOk(String strInput) {
								et.setText(strInput);
								et.setSelection(strInput.length());
								KCloudUserUtils.setInputMethodVisible(mContext, et, false);
							}

							@Override
							public void onCancel() {
								KCloudUserUtils.setInputMethodVisible(mContext, et, false);
								Button btnSave = (Button) getControl(R.id.plate_selector_btn_save);
								if (btnSave != null) {
									btnSave.requestFocus();
								}
							}
						});
				break;
			}
				
		}
	}
	
	@Override
	public void onChanged(WheelView arg0, int arg1, int arg2) {
		
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
	
	private class ProvinceAdapter extends AbstractWheelTextAdapter {

		protected ProvinceAdapter(Context context) {
			super(context);;
			
			setItemResource(R.layout.layout_car_selector_wheel_item_mid);
			setItemTextResource(R.id.car_selector_wheel_item_text_mid);
		}
		
		@Override
		public View getItem(int index, View convertView, ViewGroup parent) {
			View view = super.getItem(index, convertView, parent);
			return view;
		}

		@Override
		public int getItemsCount() {
			return provinceList.size();
		}

		@Override
		public CharSequence getItemText(int index) {
			return provinceList.get(index);
		}
	}
	
	private class CityLetterAdapter extends AbstractWheelTextAdapter {
		
		private List<String> letters = new ArrayList<String>();

		protected CityLetterAdapter(Context context) {
			super(context);
			for(int i = 0 + 65; i < 91; i++) {
				if (i == 73 || i == 79) {
					continue;
				}
				letters.add(String.valueOf((char)i));
			}
			
			setItemResource(R.layout.layout_car_selector_wheel_item_mid);
			setItemTextResource(R.id.car_selector_wheel_item_text_mid);
		}
		
		@Override
		public View getItem(int index, View convertView, ViewGroup parent) {
			View view = super.getItem(index, convertView, parent);
			return view;
		}

		@Override
		public int getItemsCount() {
			return letters.size();
		}

		@Override
		public CharSequence getItemText(int index) {
			return letters.get(index);
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
	
	/**
	 * 将EditText中的小写字母转化为大写字母
	 */
	private class AllCapTransformationMethod extends ReplacementTransformationMethod {

		@Override
		protected char[] getOriginal() {
			char[] aa = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
					'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
			return aa;
		}

		@Override
		protected char[] getReplacement() {
			char[] cc = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
					'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
			return cc;
		}

	}
	
	/**
	 * 监听EditText内容，只能输入大小写字母和数字
	 */
	private class NumberLetterTextWatcher implements TextWatcher{

		@Override
		public void afterTextChanged(Editable arg0) {
			
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (editPlate.isEnabled()) {
				String regx = "^[a-zA-Z0-9]+$";
				Pattern pattern = Pattern.compile(regx);
				Matcher matcher = pattern.matcher(s);
				if (!matcher.matches()) {
					if (!TextUtils.isEmpty(s)) {
						editPlate.setText(s.subSequence(0, start));
						editPlate.setSelection(start);
					}
				}
			}
		}
	}
	
	private void initProvinceList(){
		provinceList = new ArrayList<String>();
		provinceIndexMap = new HashMap<String, Integer>();
		String[] province_short = KCloudCommonUtil.getStringArray(R.array.province_short);
		String[] province_name = KCloudCommonUtil.getStringArray(R.array.province_name);
		for (int i=0; i<province_short.length; i++)
		{
			provinceList.add(province_short[i]);
			provinceIndexMap.put(province_name[i], i);
		}
	}
}
