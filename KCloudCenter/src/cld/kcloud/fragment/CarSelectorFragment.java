package cld.kcloud.fragment;

import java.util.ArrayList;
import java.util.List;
import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import cld.kcloud.center.R;
import cld.kcloud.database.KCloudCarBrandTable;
import cld.kcloud.database.KCloudCarModelTable;
import cld.kcloud.database.KCloudCarSeriesTable;
import cld.kcloud.datastore.KCloudCarStore;
import cld.kcloud.fragment.manager.BaseFragment;
import cld.kcloud.user.KCloudUserInfoActivity;
import cld.kcloud.utils.KCloudChinaUnixUtils;
import cld.kcloud.utils.KCloudCommonUtil;
import cld.kcloud.widget.controller.KCloudController;
import cld.kcloud.widget.controller.KCloudWidgetList;
import com.cld.log.CldLog;

@SuppressLint("ValidFragment") 
public class CarSelectorFragment extends BaseFragment implements OnClickListener, OnWheelChangedListener {
	private static final String TAG = "CarSelectorFragment";
	private View viewCarSelector = null;
	private KCloudWidgetList mWidgetList = new KCloudWidgetList();
	private WheelView wheelLetter;
	private WheelView wheelCarBrand;
	private WheelView wheelCarModel;
	private WheelView wheelCarSeries;
	private String jsonString;
	private static ICarSelectorListener mCallBack = null;
	private static ArrayList<CarBrand> brandList = new ArrayList<CarBrand>();
	private static ArrayList<CarModel> modelList = new ArrayList<CarModel>();
	private static ArrayList<CarSeries> seriesList = new ArrayList<CarSeries>();
	
	public interface ICarSelectorListener {
		public void onCarSelectorResult(String strBrand);
	}
	
	public CarSelectorFragment(ICarSelectorListener iSaveCallBack) {
		mCallBack = iSaveCallBack;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (viewCarSelector == null) {
			viewCarSelector = inflater.inflate(R.layout.fragment_car_carselector, container, false);
			bindControl(R.id.car_selector_getting_fragment,
					viewCarSelector.findViewById(R.id.car_selector_getting_fragment), 
					null, true, false);
			
			bindControl(R.id.car_selector_wheel, 
					viewCarSelector.findViewById(R.id.car_selector_wheel), 
					null, false, true);
			bindControl(R.id.car_selector_wheel_letter, 
					viewCarSelector.findViewById(R.id.car_selector_wheel_letter), 
					null, false, true);
			bindControl(R.id.car_selector_wheel_carbrand, 
					viewCarSelector.findViewById(R.id.car_selector_wheel_carbrand), 
					null, false, true);
			bindControl(R.id.car_selector_wheel_carmodle, 
					viewCarSelector.findViewById(R.id.car_selector_wheel_carmodle), 
					null, false, true);
			bindControl(R.id.car_selector_wheel_carseries, 
					viewCarSelector.findViewById(R.id.car_selector_wheel_carseries), 
					null, false, true);
			
			bindControl(R.id.car_selector_btn_save, 
					viewCarSelector.findViewById(R.id.car_selector_btn_save), 
					this, false, true);
		}
		return viewCarSelector;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		if (getArguments() != null) {
			boolean flag = getArguments().getBoolean("flag");
			jsonString = getArguments().getString("result");
			if (flag) {
				new Thread(mAnalyTask).start();
			} else {
				if (brandList.isEmpty()) {
					brandList = KCloudCarBrandTable.queryAllCarBrand();
				}
				if (modelList.isEmpty()) {
					modelList = KCloudCarModelTable.queryAllCarModel();
				}
				if (seriesList.isEmpty()) {
					seriesList = KCloudCarSeriesTable.queryAllCarSeries();
				}
				
				init();
			}
		}
		
		super.onActivityCreated(savedInstanceState);
	}
	
	@SuppressLint("NewApi")
	private Runnable mAnalyTask = new Runnable(){

		@Override
		public void run() {
			try {
				JSONArray jsonArray = null;
				JSONObject jsonObject = new JSONObject(jsonString);

				if (jsonObject.has("data")) {
					jsonArray = jsonObject.getJSONArray("data");
				}

				if (jsonArray != null) {
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject json = jsonArray.getJSONObject(i);
						
						if (!json.getString("brand").isEmpty()) {
							CarBrand brand = new CarBrand();

							brand.firstStr = KCloudChinaUnixUtils
									.getFirstLetter(json.getString("brand"));
							brand.name = json.getString("brand");
							brandList.add(brand);

							if (!json.getString("car").isEmpty()) {
								CarModel model = new CarModel();
								
								model.brand = brand.name;
								model.name = json.getString("car");
								modelList.add(model);
								
								if (!json.getString("models").isEmpty()) {
									CarSeries series = new CarSeries();
									
									series.model = model.name;
									series.name = json.getString("models").substring(series.model.length());
									seriesList.add(series);
								}
							}
						}
					}
				}
				
				mHandler.sendEmptyMessage(0);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
	};
	
	@SuppressLint("HandlerLeak") 
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			//防止已返回上一个页面
			if (getContext() == null)
				return;
			
			switch (msg.what) 
			{
			case 0:
			{
				init();
				
				new Thread(insert).start();
				//延时2秒之后，把品牌、车型、车系信息插入到数据库(防止阻塞刷新页面)
				//mHandler.sendEmptyMessageDelayed(1, 2000);
				break;
			}
			case 1:
			{
				//new Thread(init).start();
				break;
			}
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	private Runnable insert = new Runnable() 
	{
		public void run() 
		{
			KCloudCarBrandTable.insertCarBrand(brandList);
			KCloudCarModelTable.insertCarModel(modelList);
			KCloudCarSeriesTable.insertCarSeries(seriesList);
		}
	};
	
	public void init() {
		wheelLetter = (WheelView) getControl(R.id.car_selector_wheel_letter);
		wheelCarBrand = (WheelView) getControl(R.id.car_selector_wheel_carbrand);
		wheelCarModel = (WheelView) getControl(R.id.car_selector_wheel_carmodle);
		wheelCarSeries = (WheelView) getControl(R.id.car_selector_wheel_carseries);
		
		KCloudController.setVisibleById(R.id.car_selector_getting_fragment, false, mWidgetList);
		KCloudController.setVisibleById(R.id.car_selector_wheel, true, mWidgetList);
		KCloudController.setVisibleById(R.id.car_selector_wheel_letter, true, mWidgetList);
		KCloudController.setVisibleById(R.id.car_selector_wheel_carbrand, true, mWidgetList);
		KCloudController.setVisibleById(R.id.car_selector_wheel_carmodle, true, mWidgetList);
		KCloudController.setVisibleById(R.id.car_selector_wheel_carseries, true, mWidgetList);
		KCloudController.setVisibleById(R.id.car_selector_btn_save, true, mWidgetList);
	
		initWheel();
	}
	
	public void initWheel(){
		//阴影颜色，与背景色一致，实现字体的渐变效果
		wheelLetter.setShadowColor(0xff2e2e2e, 0xbb2e2e2e, 0x772e2e2e);
		wheelLetter.setWheelBackground(R.drawable.dialog_wheel_bg);
		wheelLetter.setWheelForeground(0);
		wheelLetter.setVisibleItems(7);
		wheelLetter.setViewAdapter(new LetterAdapter(getContext()));
		
		wheelCarBrand.setVisibleItems(3);
		wheelCarBrand.setShadowColor(0xff2e2e2e, 0xbb2e2e2e, 0x772e2e2e);
		wheelCarBrand.setCenterRectColor(0xff57595d);
		wheelCarBrand.setWheelBackground(R.drawable.dialog_wheel_bg);
		
		// 显示品牌
		CharSequence cs = ((LetterAdapter)wheelLetter.getViewAdapter()).getItemText(0);
		wheelCarBrand.setViewAdapter(new CarBrandAdapter(getContext(), cs));
		
		wheelCarModel.setVisibleItems(3);
		wheelCarModel.setShadowColor(0xff2e2e2e, 0xbb2e2e2e, 0x772e2e2e);
		wheelCarModel.setCenterRectColor(0xff57595d);
		wheelCarModel.setWheelBackground(R.drawable.dialog_wheel_bg);
		wheelCarModel.setViewAdapter(new CarModelAdapter(getContext(), ""));
		
		wheelCarSeries.setVisibleItems(3);
		wheelCarSeries.setShadowColor(0xff2e2e2e, 0xbb2e2e2e, 0x772e2e2e);
		wheelCarSeries.setCenterRectColor(0xff57595d);
		wheelCarSeries.setWheelBackground(R.drawable.dialog_wheel_bg);
		wheelCarSeries.setViewAdapter(new CarSeriesAdapter(getContext(), ""));
		
		wheelLetter.addChangingListener(this);
		wheelCarBrand.addChangingListener(this);
		wheelCarModel.addChangingListener(this);
		wheelCarSeries.addChangingListener(this);
		
		//触发onChanged以显示车辆信息,onChanged默认oldValue为0
		wheelLetter.setCurrentItem(1);
		wheelLetter.setCurrentItem(0);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.car_selector_btn_save : 
			{
				if (getActivity() == null)
					return;
				
				if (wheelCarBrand == null || 
					wheelCarBrand.getViewAdapter() == null ||
					((CarBrandAdapter)wheelCarBrand.getViewAdapter()).
						getItemText(wheelCarBrand.getCurrentItem()) == null) {
					return;
				}
				
				if (wheelCarModel == null || 
					wheelCarModel.getViewAdapter() == null ||
					((CarModelAdapter)wheelCarModel.getViewAdapter()).
						getItemText(wheelCarModel.getCurrentItem()) == null) {
					return;
				}
				
				if (wheelCarSeries == null || 
					wheelCarSeries.getViewAdapter() == null ||
					((CarSeriesAdapter)wheelCarSeries.getViewAdapter()).
						getItemText(wheelCarSeries.getCurrentItem()) == null) {
					return;
				}
				
				String brand =  ((CarBrandAdapter)wheelCarBrand.getViewAdapter()).getItemText(wheelCarBrand.getCurrentItem()).toString();
				String model = ((CarModelAdapter)wheelCarModel.getViewAdapter()).getItemText(wheelCarModel.getCurrentItem()).toString();
				String series = ((CarSeriesAdapter)wheelCarSeries.getViewAdapter()).getItemText(wheelCarSeries.getCurrentItem()).toString();
				CldLog.i(TAG, "brand = " + brand + " model = " + model + " series = " +series);
				KCloudCarStore.getInstance().getTemp().setSeries(brand, model, series);
				
				JSONObject jsonObject = new JSONObject();
				if (jsonObject != null) {
					try {
						jsonObject.put("brand", brand);
						jsonObject.put("model", model);
						jsonObject.put("series", series);
						if (mCallBack != null) {
							mCallBack.onCarSelectorResult(jsonObject.toString());
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				
				((KCloudUserInfoActivity) getActivity()).doBack();
				break;
			}
		}
	}
	
	@Override
	public void onChanged(WheelView wheel, int oldValue, int newValue) {
		if(wheel == wheelLetter){
			updateCarBrands();
		} else if(wheel == wheelCarBrand){
			updateCarModels();
		} else if(wheel == wheelCarModel){
			updateCarSerieses();
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
	 * Updates the car brand wheel
	 */
	private void updateCarBrands() {
		if (getContext() == null)
			return;
		
		int current = wheelLetter.getCurrentItem();
		CharSequence cs = ((LetterAdapter)wheelLetter.getViewAdapter()).getItemText(current);
		wheelCarBrand.setViewAdapter(new CarBrandAdapter(getContext(), cs));
		if (wheelCarBrand.getViewAdapter().getItemsCount() > 0) {
			wheelCarBrand.setCurrentItem(0);
		}

		updateCarModels();
	}
	
	/**
	 * Updates the car modle wheel
	 */
	private void updateCarModels() {
		if (getContext() == null)
			return;
		
		int current = wheelCarBrand.getCurrentItem();
		CharSequence cs = ((CarBrandAdapter)wheelCarBrand.getViewAdapter()).getItemText(current);
		wheelCarModel.setViewAdapter(new CarModelAdapter(getContext(), cs));
		if (wheelCarModel.getViewAdapter().getItemsCount() > 0) {
			wheelCarModel.setCurrentItem(0);
		}
		
		updateCarSerieses();
	}

	/**
	 * Updates the car series wheel
	 */
	private void updateCarSerieses() {
		if (getContext() == null)
			return; 
		
		int current = wheelCarModel.getCurrentItem();	
		CharSequence cs = ((CarModelAdapter)wheelCarModel.getViewAdapter()).getItemText(current);
		wheelCarSeries.setViewAdapter(new CarSeriesAdapter(getContext(), cs));
		if (wheelCarSeries.getViewAdapter().getItemsCount() >0) {
			wheelCarSeries.setCurrentItem(0);
		}
	}
	
	private class LetterAdapter extends AbstractWheelTextAdapter {
		
		private List<String> letters = new ArrayList<String>();

		protected LetterAdapter(Context context) {
			super(context);
			for(int i = 0 + 65; i < 91; i++) {
				if (i == 69 || i == 73 || i == 84 || i == 85) {
					continue;
				}
				letters.add(String.valueOf((char)i));
			}
			setItemResource(R.layout.layout_car_selector_wheel_item_min);
			setItemTextResource(R.id.car_selector_wheel_item_text_min);
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
		protected CharSequence getItemText(int index) {
			if (letters.size() <= 0) {
				return null;
			}
			return letters.get(index);
		}
	}
	
	private class CarBrandAdapter extends AbstractWheelTextAdapter {
		
		private List<CarBrand> list = new ArrayList<CarBrand>();

		private boolean has(String str) {
			boolean result = false;
			
			for (CarBrand item : list) {
				if (str.equals(item.name)) {
					result = true;
					break;
				}
			}
			
			return result;
		}
		
		protected CarBrandAdapter(Context context, CharSequence cs) {
			super(context);
			if (cs.length() <= 0) {
				return ;
			}
			
			for (int i = 0; i < brandList.size(); i++) {
				CarBrand item = brandList.get(i);
				if (item != null && cs.equals(item.firstStr)) {
					if (has(item.name)) {
						continue;
					}
					list.add(item);
				}
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
			return list.size();
		}

		@Override
		protected CharSequence getItemText(int index) {
			if (list.size() <= 0)
				return null;
			
			CarBrand item = list.get(index);
			if (item != null) {
				return item.name;
			}
			return null;
		}
	}
	
	private class CarModelAdapter extends AbstractWheelTextAdapter {
		
		private List<CarModel> list = new ArrayList<CarModel>();

		private boolean has(String str) {
			boolean result = false;
			
			for (CarModel item : list) {
				if (str.equals(item.name)) {
					result = true;
					break;
				}
			}
			
			return result;
		}
		
		protected CarModelAdapter(Context context, CharSequence cs) {
			super(context);
			if(cs != null){
				if (cs.length() > 0) {
					for (int i = 0; i < modelList.size(); i++) {
						CarModel item = modelList.get(i);
						if (item != null && cs.equals(item.brand)) {
							if (has(item.name)) {
								continue;
							}
							list.add(item);
						}
					}
				} else {
					CarModel model = new CarModel();
					model.name = KCloudCommonUtil.getString(R.string.car_select_models);
					list.add(model);
				}
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
			return list.size();
		}

		@Override
		protected CharSequence getItemText(int index) {
			if (list.size() <= 0)
				return null;
			
			CarModel item = list.get(index);
			if (item != null) {
				return item.name;
			}
			return null;
		}
	}
	
	private class CarSeriesAdapter extends AbstractWheelTextAdapter {
		
		private List<CarSeries> list = new ArrayList<CarSeries>();
		
		private boolean has(String str) {
			boolean result = false;
			
			for (CarSeries item : list) {
				if (str.equals(item.name)) {
					result = true;
					break;
				}
			}
			
			return result;
		}
		
		protected CarSeriesAdapter(Context context, CharSequence cs) {
			super(context);
			
			if(cs != null){
				if (cs.length() > 0) {
					for (int i = 0; i < seriesList.size(); i++) {
						CarSeries item = seriesList.get(i);
						if (item != null && cs.equals(item.model)) {
							if (has(item.name)) {
								continue;
							}
							list.add(item);
						}
					}
				} else {
					CarSeries series = new CarSeries();
					series.name = KCloudCommonUtil.getString(R.string.car_select_series);
					list.add(series);
				}
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
			return list.size();
		}

		@Override
		protected CharSequence getItemText(int index) {
			if (list.size() <= 0) {
				return null;
			}
			
			CarSeries item = list.get(index);
			if (item != null) {
				return item.name;
			}
			return null;
		}
	}
	
	public static class CarBrand {
		public String firstStr = "";	// 首字母
		public String name = "";

		@Override
		public String toString() {
			return "fristStr: " + firstStr + " name " + name;
		}
	}
	
	public static class CarModel {
		public String brand = "";	// 品牌
		public String name = "";

		@Override
		public String toString() {
			return "brand: " + brand + " name： " + name;
		}
	}
	
	public static class CarSeries {
		public String model = "";	// 车型
		public String name = "";
		
		@Override
		public String toString() {
			return "model: " + model + " name： " + name;
		}
	}
}
