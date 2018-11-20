package cld.kcloud.fragment;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import cld.kcloud.center.KCloudAppUtils;
import cld.kcloud.center.KCloudAppUtils.CLDMessageId;
import cld.kcloud.center.R;
import cld.kcloud.custom.bean.KCloudCarInfo;
import cld.kcloud.database.KCloudCarSeriesTable;
import cld.kcloud.datastore.KCloudCarStore;
import cld.kcloud.fragment.CarPlateSelectorFragment.ICarPlateSelectorListener;
import cld.kcloud.fragment.CarSelectorFragment.CarSeries;
import cld.kcloud.fragment.CarSelectorFragment.ICarSelectorListener;
import cld.kcloud.fragment.manager.BaseFragment;
import cld.kcloud.service.KCloudService;
import cld.kcloud.user.KCloudUserInfoActivity;
import cld.kcloud.utils.KCloudCommonUtil;
import cld.kcloud.utils.KCloudNetworkUtils;
import cld.kcloud.utils.KCloudShareUtils;
import cld.kcloud.utils.control.CldInputDialog;
import cld.kcloud.utils.control.CldProgress;
import cld.kcloud.utils.control.CldProgress.CldProgressListener;
import cld.kcloud.utils.control.CldPromptDialog;
import cld.kcloud.utils.control.CldPromptDialog.PromptDialogListener;
import cld.kcloud.widget.controller.KCloudController;
import cld.kcloud.widget.controller.KCloudWidgetList;
import com.cld.device.CldPhoneNet;
import com.cld.log.CldLog;

public class CarFragment extends BaseFragment implements OnClickListener, ICarPlateSelectorListener, ICarSelectorListener {
	private static final String TAG = "CarFragment";

	private View viewCar = null;
	private KCloudWidgetList mWidgetList = new KCloudWidgetList();
	private static ArrayList<CarSeries> seriesList = new ArrayList<CarSeries>();
	//�Ƿ��ڼ�������,����ڼ����У�����Button���ɵ��
	private boolean isDataLoading = false;
	private boolean carsInfoFlag = false;
	private String carsInfo = "";
	private String unset = KCloudCommonUtil.getString(R.string.setting_unset);
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
		if (viewCar == null) {
			viewCar = inflater.inflate(R.layout.fragment_car_manager, container, false);

			bindControl(R.id.carinfo_layout_getting_fragment,
					viewCar.findViewById(R.id.carinfo_layout_getting_fragment),
					this, true, true);
			
			bindControl(R.id.carinfo_layout_success_fragment,
					viewCar.findViewById(R.id.carinfo_layout_success_fragment),
					this, false, true);
			bindControl(R.id.carinfo_btn_series_modify,
					viewCar.findViewById(R.id.carinfo_btn_series_modify), 
					this, true, true);
			bindControl(R.id.carinfo_btn_number_modify,
					viewCar.findViewById(R.id.carinfo_btn_number_modify), 
					this, true, true);
			bindControl(R.id.carinfo_btn_body_modify,
					viewCar.findViewById(R.id.carinfo_btn_body_modify), 
					this, true, true);
			bindControl(R.id.carinfo_btn_engine_modify,
					viewCar.findViewById(R.id.carinfo_btn_engine_modify), 
					this, true, true);
					
			bindControl(R.id.carinfo_imgbtn_series_modify,
					viewCar.findViewById(R.id.carinfo_imgbtn_series_modify), 
					this, true, true);
			bindControl(R.id.carinfo_imgbtn_number_modify,
					viewCar.findViewById(R.id.carinfo_imgbtn_number_modify), 
					this, true, true);
			bindControl(R.id.carinfo_imgbtn_body_modify,
					viewCar.findViewById(R.id.carinfo_imgbtn_body_modify), 
					this, true, true);
			bindControl(R.id.carinfo_imgbtn_engine_modify,
					viewCar.findViewById(R.id.carinfo_imgbtn_engine_modify), 
					this, true, true);
			
			bindControl(R.id.carinfo_text_logo,
					viewCar.findViewById(R.id.carinfo_text_logo), 
					this, true, true);
			bindControl(R.id.carinfo_text_model,
					viewCar.findViewById(R.id.carinfo_text_model), 
					this, true, true);
			bindControl(R.id.carinfo_text_series,
					viewCar.findViewById(R.id.carinfo_text_series), 
					this, true, true);
			bindControl(R.id.carinfo_text_number,
					viewCar.findViewById(R.id.carinfo_text_number), 
					this, true, true);
			bindControl(R.id.carinfo_text_body,
					viewCar.findViewById(R.id.carinfo_text_body), 
					this, true, true);
			bindControl(R.id.carinfo_text_engine,
					viewCar.findViewById(R.id.carinfo_text_engine), 
					this, true, true);
			
			bindControl(R.id.carinfo_progress_getting,
					viewCar.findViewById(R.id.carinfo_progress_getting), 
					null, false, false);
			
			bindControl(R.id.carinfo_layout_failed_fragment, 
					viewCar.findViewById(R.id.carinfo_layout_failed_fragment),
					this, false, true);
			bindControl(R.id.id_fragment_failed, 
					viewCar.findViewById(R.id.id_fragment_failed),
    				this, false, true);
			bindControl(R.id.id_fragment_failed_reload, 
					viewCar.findViewById(R.id.id_fragment_failed_reload),
					this, true, true);
    		bindControl(R.id.id_fragment_abnormal, 
    				viewCar.findViewById(R.id.id_fragment_abnormal),
    				this, true, true);

			initControl();
			updateUI();
		}
		return viewCar;
	}

	@Override
	public void onResume() {
		updateUI();
		super.onResume();
	}

	@Override
	public boolean onBackPressed() {
		return false;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD) 
	@Override
	public void onClick(View v) {
		if (isDataLoading == true) 
			return;
		
		if (getActivity() == null) 
			return;
		
		switch (v.getId()) {
		case R.id.carinfo_text_logo: //Ʒ��
		{
			TextView tvLogo = (TextView) getControl(R.id.carinfo_text_logo);
			if (tvLogo != null) {
				if (tvLogo.getText().toString().equals(unset)) {
					onClick(getControl(R.id.carinfo_btn_series_modify));
				}
			}
			break;
		}
		case R.id.carinfo_text_model: //����
		{
			TextView tvModel = (TextView) getControl(R.id.carinfo_text_model);
			if (tvModel != null) {
				if (tvModel.getText().toString().equals(unset)) {
					onClick(getControl(R.id.carinfo_btn_series_modify));
				}
			}
			break;
		}
		case R.id.carinfo_text_series: //��ϵ
		{
			TextView tvSeries = (TextView) getControl(R.id.carinfo_text_series);
			if (tvSeries != null) {
				if (tvSeries.getText().toString().equals(unset)) {
					onClick(getControl(R.id.carinfo_btn_series_modify));
				}
			}
			break;
		}
		case R.id.carinfo_text_number: //���ƺ�
		{
			TextView tvNum = (TextView) getControl(R.id.carinfo_text_number);
			if (tvNum != null) {
				if (tvNum.getText().toString().equals( unset + " *")) {
					onClick(getControl(R.id.carinfo_btn_number_modify));
				}
			}
			break;
		}
		case R.id.carinfo_text_body: //���ܺ�
		{
			TextView tvBody = (TextView) getControl(R.id.carinfo_text_body);
			if (tvBody != null) {
				if (tvBody.getText().toString().equals(unset)) {
					onClick(getControl(R.id.carinfo_btn_body_modify));
				}
			}
			break;
		}
		case R.id.carinfo_text_engine: //��������
		{
			TextView tvEngine = (TextView) getControl(R.id.carinfo_text_engine);
			if (tvEngine != null) {
				if (tvEngine.getText().toString().equals(unset)) {
					onClick(getControl(R.id.carinfo_btn_engine_modify));
				}
			}
			break;
		}
		
		case R.id.carinfo_imgbtn_series_modify:
		case R.id.carinfo_btn_series_modify:  //�޸�Ʒ�ơ����͡���ϵ
		{
			// ���ƺŲ���Ϊ��
			TextView tvNum = (TextView) getControl(R.id.carinfo_text_number);
			if (tvNum == null)
				return;

			if (tvNum.getText().toString().equals(unset + " *")) {
				CldPromptDialog.createPromptDialog(
						mContext, "",
						KCloudCommonUtil.getString(R.string.dialog_msg_fill_plate), 
						KCloudCommonUtil.getString(R.string.dialog_sure), 
						KCloudCommonUtil.getString(R.string.dialog_cancel), 
						new PromptDialogListener() {
							@Override
							public void onSure() {
								// �����޸ĳ��ƺ�ҳ��
								((KCloudUserInfoActivity) getActivity())
										.doChangeFragment(FragmentType.eFragment_CarPlateSelector);
							}

							@Override
							public void onCancel() {
							}
						});
				return;
			}
			
			// ���ƺŲ�Ϊ�գ������޸ĳ�����Ϣҳ��
			KCloudController.setVisibleById(R.id.carinfo_progress_getting, 
					true, mWidgetList);
			carsInfo = KCloudShareUtils.getString(KCloudAppUtils.CAR_SERIE_RESULT);
			if (carsInfo.isEmpty()) {
				KCloudNetworkUtils.getUserCarList(mHandler);
				isDataLoading = true;
				carsInfoFlag = true;
			} else {
				// 30���Ժ���ȥȡ������Ϣ
				long gettime = KCloudShareUtils.getLong(KCloudAppUtils.CAR_SERIE_GET_TIME);
				long curtime = System.currentTimeMillis();
				if ((curtime > gettime)
						&& ((curtime - gettime) / (3600*1000*24*30) > 0)) {
					KCloudNetworkUtils.getUserCarList(mHandler);
					isDataLoading = true;
					carsInfoFlag = true;
				} else {
					isDataLoading = false;
					carsInfoFlag = false;
					
					if (seriesList != null && seriesList.size() <= 0) {
						seriesList = KCloudCarSeriesTable.queryAllCarSeries();
						if (seriesList != null && seriesList.size() <= 0) {
							carsInfoFlag = true;
						}
					}
					
					KCloudController.setVisibleById(R.id.carinfo_progress_getting, 
							false, mWidgetList);
					((KCloudUserInfoActivity) getActivity())
							.doChangeFragment(FragmentType.eFragment_CarSelector);
				}
			}
			break;
		}
		case R.id.carinfo_imgbtn_number_modify:
		case R.id.carinfo_btn_number_modify: //�޸ĳ��ƺ�
		{
			((KCloudUserInfoActivity) getActivity())
					.doChangeFragment(FragmentType.eFragment_CarPlateSelector);
			break;
		}

		case R.id.carinfo_imgbtn_body_modify:
		case R.id.carinfo_btn_body_modify: //�޸ĳ��ܺ�
		{
			// ���ƺŲ���Ϊ��
			TextView tvNum = (TextView) getControl(R.id.carinfo_text_number);
			if (tvNum == null)
				return;

			if (tvNum.getText().toString().equals(unset + " *")) {
				CldPromptDialog.createPromptDialog(
						mContext, "",
						KCloudCommonUtil.getString(R.string.dialog_msg_fill_plate), 
						KCloudCommonUtil.getString(R.string.dialog_sure), 
						KCloudCommonUtil.getString(R.string.dialog_cancel),
						new PromptDialogListener() {
							@Override
							public void onSure() {
								// �����޸ĳ��ƺ�ҳ��
								((KCloudUserInfoActivity) getActivity())
										.doChangeFragment(FragmentType.eFragment_CarPlateSelector);
							}

							@Override
							public void onCancel() {
							}
						});
				return;
			}

			// ���ƺŲ�Ϊ�գ������޸ĳ��ܺ�
			final TextView tv = (TextView) getControl(R.id.carinfo_text_body);
			String title = KCloudCommonUtil.getString(
					R.string.input_dialog_title_car_body);
			String hint = KCloudCommonUtil.getString(
					R.string.input_dialog_hint_car_body);

			CldInputDialog.showInputDialog(mContext, title, hint, tv.getText().toString(),
					CldInputDialog.CldInputType.eInputType_CarBody,
					CldInputDialog.CldButtonType.eButton_Confirm,
					new CldInputDialog.CldInputDialogListener() {
						@Override
						public void onOk(String strInput) {
							if (strInput != null) {
								String strBodyNum = strInput.replaceAll(" ", "");
								if (strBodyNum.isEmpty()) {
									return;
								}
								if (!CldPhoneNet.isNetConnected()) {
									KCloudCommonUtil.makeText(R.string.common_network_abnormal);
									return;
								}
								if (strBodyNum.length() != 6) {
									KCloudCommonUtil.makeText(R.string.userinfo_set_car_body_error);
									return;
								}

								// �Ƿ񱣴�
								KCloudCarInfo info = KCloudCarStore.getInstance().get();
								if (!strBodyNum.equals(info.frame_num)) {
									CldProgress.showProgress(mContext,
													KCloudCommonUtil.getString(R.string.common_network_data_update),
													new CldProgressListener() {
														public void onCancel() {
														}
													});

									KCloudCarStore.getInstance().getTemp()
											.setFrameNum(strBodyNum);
									KCloudNetworkUtils.updateCarInfo(mHandler);
								}
							}
						}

						@Override
						public void onCancel() {
						}
					});
			break;
		}
		case R.id.carinfo_imgbtn_engine_modify:
		case R.id.carinfo_btn_engine_modify: //�޸ķ�������
		{
			// ���ƺŲ���Ϊ��
			TextView tvNum = (TextView) getControl(R.id.carinfo_text_number);
			if (tvNum == null)
				return;

			if (tvNum.getText().toString().equals(unset + " *")) {
				CldPromptDialog.createPromptDialog(
						mContext, "",
						KCloudCommonUtil.getString(R.string.dialog_msg_fill_plate), 
						KCloudCommonUtil.getString(R.string.dialog_sure), 
						KCloudCommonUtil.getString(R.string.dialog_cancel),
						new PromptDialogListener() {
							@Override
							public void onSure() {
								// �����޸ĳ��ƺ�ҳ��
								((KCloudUserInfoActivity) getActivity())
										.doChangeFragment(FragmentType.eFragment_CarPlateSelector);
							}

							@Override
							public void onCancel() {
							}
						});
				return;
			}

			// ���ƺŲ�Ϊ�գ������޸ķ�������
			final TextView tv = (TextView) getControl(R.id.carinfo_text_engine);
			String title = KCloudCommonUtil.getString(
					R.string.input_dialog_title_car_engine);
			String hint = KCloudCommonUtil.getString(
					R.string.input_dialog_hint_car_engine);

			CldInputDialog.showInputDialog(mContext, title, hint, tv.getText().toString(),
					CldInputDialog.CldInputType.eInputType_CarEngine,
					CldInputDialog.CldButtonType.eButton_Confirm,
					new CldInputDialog.CldInputDialogListener() {
						@Override
						public void onOk(String strInput) {
							if (strInput != null) {
								String strEngineNum = strInput.replaceAll(" ", "");

								if (strEngineNum.isEmpty()) {
									return;
								}
								if (!CldPhoneNet.isNetConnected()) {
									KCloudCommonUtil.makeText(R.string.common_network_abnormal);
									return;
								}
								if (strEngineNum.length() != 6) {
									KCloudCommonUtil.makeText(R.string.userinfo_set_car_engine_error);
									return;
								}

								// �Ƿ񱣴�
								KCloudCarInfo info = KCloudCarStore.getInstance().get();
								if (!strEngineNum.equals(info.engine_num)) {
									CldProgress.showProgress(mContext,
											KCloudCommonUtil.getString(R.string.common_network_data_update),
													new CldProgressListener() {
														public void onCancel() {
														}
													});

									KCloudCarStore.getInstance().getTemp()
											.setEngineNum(strEngineNum);
									KCloudNetworkUtils.updateCarInfo(mHandler);
								}
							}
						}

						@Override
						public void onCancel() {
						}
					});
			break;
		}
		/* case R.id.carinfo_layout_failed_fragment: */
		case R.id.id_fragment_failed_reload: 
		{
			/*if (CldPhoneNet.isNetConnected())*/ {
				KCloudNetworkUtils.getUserCarInfo(mHandler);
				KCloudController.setVisibleById(R.id.carinfo_layout_getting_fragment,
						true, mWidgetList);
				KCloudController.setVisibleById(R.id.carinfo_layout_success_fragment, 
						false, mWidgetList);
				KCloudController.setVisibleById(R.id.carinfo_layout_failed_fragment,
						false, mWidgetList);
			}
			break;
		}
		default:
			break;
		}
	}
	
	public void onNetConnected(){
		
	}

	@Override
	public void onHandleMessage(Message message) {
		CldLog.i(TAG, String.valueOf(message.what));
	}

	@SuppressLint("HandlerLeak") 
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			switch (msg.what) {
			case CLDMessageId.MSG_ID_CAR_GET_FAILED: {
				// ��ȡ�û�������Ϣʧ��
				KCloudController.setVisibleById(R.id.carinfo_layout_getting_fragment, false, mWidgetList);
				KCloudController.setVisibleById(R.id.carinfo_layout_success_fragment, false, mWidgetList);
				KCloudController.setVisibleById(R.id.carinfo_layout_failed_fragment, true, mWidgetList);
				KCloudController.setVisibleById(R.id.id_fragment_failed, true, mWidgetList);
				KCloudController.setVisibleById(R.id.id_fragment_abnormal, false, mWidgetList);
				break;
			}
			
			case CLDMessageId.MSG_ID_CAR_GET_SUCCESS: {
				// ��ȡ�û�������Ϣ�ɹ�
				Bundle bundle = msg.getData();
				if (bundle != null) {
					String result = bundle.getString("result");
					if (result != null) {
						try {
							JSONArray jsonArray = null;
							JSONObject jsonObject = new JSONObject(result);

							if (jsonObject.has("carinfo")) {
								jsonArray = jsonObject.getJSONArray("carinfo");
							}
							
							if (jsonArray != null && jsonArray.length() > 0) {
								KCloudCarInfo carInfo = new KCloudCarInfo();
								carInfo.brand = jsonArray.getJSONObject(0).getString("brand");
								carInfo.car_model = jsonArray.getJSONObject(0).getString("car_model");
								carInfo.series = jsonArray.getJSONObject(0).getString("series");
								carInfo.plate_num = jsonArray.getJSONObject(0).getString("plate_num");
								carInfo.frame_num = jsonArray.getJSONObject(0).getString("frame_num");
								carInfo.engine_num = jsonArray.getJSONObject(0).getString("engine_num");

								// ����û�������Ϣ
								KCloudCarStore.getInstance().addCarInfo(carInfo);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}

				updateUI();
				KCloudController.setVisibleById(R.id.carinfo_layout_getting_fragment, false, mWidgetList);
				KCloudController.setVisibleById(R.id.carinfo_layout_success_fragment, true, mWidgetList);
				KCloudController.setVisibleById(R.id.carinfo_layout_failed_fragment, false, mWidgetList);
				break;
			}
			
			case CLDMessageId.MSG_ID_CAR_UPDATE_FAILED: {
				// �����û�������Ϣʧ��
				if (CldProgress.isShowProgress()) {
					CldProgress.cancelProgress();
				}
				
				int[] status = KCloudCarStore.getInstance().getTemp().getChangeStatus();
				if (status[0] == 1) {
					KCloudCommonUtil.makeText(R.string.userinfo_set_car_series_failed);
				} else if (status[1] == 1) {
					KCloudCommonUtil.makeText(R.string.userinfo_set_car_num_failed);
				} else if (status[2] == 1) {
					KCloudCommonUtil.makeText(R.string.userinfo_set_car_body_failed);
				} else if (status[3] == 1) {
					KCloudCommonUtil.makeText(R.string.userinfo_set_car_engine_failed);
				}
				
				// ������ʱ��Ϣ
				updateUI();
				KCloudCarStore.getInstance().resetTemp();
				break;
			}

			case CLDMessageId.MSG_ID_CAR_UPDATE_SUCCESS: {
				
				// �����û�������Ϣ�ɹ�
				if (CldProgress.isShowProgress()) {
					CldProgress.cancelProgress();
				}
				KCloudCarStore.getInstance().update();
				updateUI();
				KCloudService.onClientListener(2, 0, "MSG_ID_CAR_UPDATE_SUCCESS");
				break;
			}
			
			case CLDMessageId.MSG_ID_KGO_GET_CARLIST_FAILED: {
				
				// ��ȡ�����б�ʧ��
				KCloudController.setVisibleById(R.id.carinfo_progress_getting,
						false, mWidgetList);
				isDataLoading = false;
				
				KCloudCommonUtil.makeText(R.string.common_network_data_get_failed);
				break;
			}

			case CLDMessageId.MSG_ID_KGO_GET_CARLIST_SUCCESS: {
				// ��ȡ�����б�ɹ�
				Bundle bundle = msg.getData();
				String result = bundle.getString("result");

				KCloudController.setVisibleById(R.id.carinfo_progress_getting,
						false, mWidgetList);
				isDataLoading = false;
				carsInfo = result;
				//��������Ʒ����Ϣ
				KCloudShareUtils.put(KCloudAppUtils.CAR_SERIE_RESULT, result);
				long gettime = System.currentTimeMillis();
				KCloudShareUtils.put(KCloudAppUtils.CAR_SERIE_GET_TIME, gettime);
				
				if (getActivity() != null) {
					((KCloudUserInfoActivity) getActivity())
						.doChangeFragment(FragmentType.eFragment_CarSelector);
				}
				break;
			}
			}
		}
	};

	public void initControl() {	
		if (!CldPhoneNet.isNetConnected()){
			KCloudController.setVisibleById(R.id.carinfo_layout_getting_fragment, false, mWidgetList);
			KCloudController.setVisibleById(R.id.carinfo_layout_success_fragment, false, mWidgetList);
			KCloudController.setVisibleById(R.id.carinfo_layout_failed_fragment, true, mWidgetList);
			KCloudController.setVisibleById(R.id.id_fragment_failed, false, mWidgetList);
			KCloudController.setVisibleById(R.id.id_fragment_abnormal, true, mWidgetList);
			
			TextView minor = (TextView)getControl(R.id.id_fragment_abnormal);
			if (minor != null){
				minor.setText(KCloudCommonUtil.getString(R.string.appwidget_network_unconnection));
			}
			return;
		}
		// ��ȡ�û�������Ϣ
		KCloudNetworkUtils.getUserCarInfo(mHandler);
	}

	public void updateUI() {
		CldLog.d(TAG, "updateUI");
		if (!CldPhoneNet.isNetConnected()){
			KCloudController.setVisibleById(R.id.carinfo_layout_getting_fragment, false, mWidgetList);
			KCloudController.setVisibleById(R.id.carinfo_layout_success_fragment, false, mWidgetList);
			KCloudController.setVisibleById(R.id.carinfo_layout_failed_fragment, true, mWidgetList);
			KCloudController.setVisibleById(R.id.id_fragment_failed, false, mWidgetList);
			KCloudController.setVisibleById(R.id.id_fragment_abnormal, true, mWidgetList);
			
			TextView minor = (TextView)getControl(R.id.id_fragment_abnormal);
			if (minor != null){
				minor.setText(KCloudCommonUtil.getString(R.string.appwidget_network_unconnection));
			}
			return;
		}

		KCloudCarInfo carInfo = KCloudCarStore.getInstance().get();

		// Ʒ��
		TextView tvLogo = (TextView) getControl(R.id.carinfo_text_logo);
		if (tvLogo != null) {
			if (!TextUtils.isEmpty(carInfo.brand)) {
				tvLogo.setText(carInfo.brand);
			} else {
				tvLogo.setText(unset);
			}
		}

		// ����
		TextView tvModel = (TextView) getControl(R.id.carinfo_text_model);
		if (tvModel != null) {
			if (!TextUtils.isEmpty(carInfo.car_model)) {
				tvModel.setText(carInfo.car_model);
			} else {
				tvModel.setText(unset);
			}
		}

		// ��ϵ
		TextView tvSeries = (TextView) getControl(R.id.carinfo_text_series);
		if (tvSeries != null) {
			if (!TextUtils.isEmpty(carInfo.series)) {
				tvSeries.setText(carInfo.series);
			} else {
				tvSeries.setText(unset);
			}
		}

		// ���ƺ�
		TextView tvNum = (TextView) getControl(R.id.carinfo_text_number);
		if (tvNum != null) {
			if (!TextUtils.isEmpty(carInfo.plate_num)) {
				tvNum.setText(carInfo.plate_num);
			} else {
				String sText = unset + " *";
				SpannableString ss = new SpannableString(sText);
				ss.setSpan(new ForegroundColorSpan(
						KCloudCommonUtil.getColor(R.color.text_red_color)), 
						sText.indexOf('*'),
						sText.length(),
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					tvNum.setText(ss);
			}
		}

		// ���ܺ�
		TextView tvBody = (TextView) getControl(R.id.carinfo_text_body);
		if (tvBody != null) {
			if (!TextUtils.isEmpty(carInfo.frame_num)) {
				tvBody.setText(carInfo.frame_num);
			} else {
				tvBody.setText(unset);
			}
		}

		// ��������
		TextView tvEngine = (TextView) getControl(R.id.carinfo_text_engine);
		if (tvEngine != null) {
			if (!TextUtils.isEmpty(carInfo.engine_num)) {
				tvEngine.setText(carInfo.engine_num);
			} else {
				tvEngine.setText(unset);
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
	
	public String getCarsInfo() {
		return this.carsInfo;
	}
	
	public boolean getCarsInfoFlag() {
		return carsInfoFlag;
	}
	
	
	@Override
	public void onCarPlateSelectorResult(String strBrand) {
		KCloudCarInfo info = KCloudCarStore.getInstance().get();
		if (!strBrand.equals(info.plate_num)) {
			CldProgress.showProgress(mContext,
					KCloudCommonUtil.getString(R.string.common_network_data_update),
			new CldProgressListener() {
				public void onCancel() {
				}
			});
		}

		KCloudCarStore.getInstance().getTemp().setPlateNum(strBrand);
		KCloudNetworkUtils.updateCarInfo(mHandler);
	}

	@SuppressLint("NewApi") 
	@Override
	public void onCarSelectorResult(String jsonString) {
		// ���ƺŲ���Ϊ��
		TextView tvNum = (TextView) getControl(R.id.carinfo_text_number);
		if (tvNum != null && tvNum.getText().toString().isEmpty()) {
			KCloudCommonUtil.makeText(R.string.userinfo_set_car_num_empty);
		} else {
			CldProgress.showProgress(mContext,
					KCloudCommonUtil.getString(R.string.common_network_data_update),
					new CldProgressListener() {
						public void onCancel() {
						}
					});

			KCloudNetworkUtils.updateCarInfo(mHandler);
		}
		
		if (jsonString != null && !jsonString.isEmpty()) {
			JSONObject json = null;
			try {
				json = new JSONObject(jsonString);
				
				TextView tv = (TextView) getControl(R.id.carinfo_text_logo);
				if (tv != null && json.has("brand")) {
					tv.setText(json.getString("brand"));
				}
				
				tv = (TextView) getControl(R.id.carinfo_text_model);
				if (tv != null && json.has("model")) {
					tv.setText(json.getString("model"));
				}
				
				tv = (TextView) getControl(R.id.carinfo_text_series);
				if (tv != null && json.has("series")) {
					tv.setText(json.getString("series"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
