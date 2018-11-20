package cld.kcloud.fragment;

import java.util.Timer;
import java.util.TimerTask;
import com.cld.device.CldPhoneNet;
import com.cld.log.CldLog;
import com.cld.ols.api.CldKAccountAPI;
import com.cld.ols.api.CldKAccountAPI.CldBussinessCode;
import com.cld.ols.api.CldKConfigAPI;
import com.cld.ols.tools.CldErrUtil.CldOlsErrCode;
import cld.kcloud.center.R;
import cld.kcloud.center.KCloudAppUtils.CLDMessageId;
import cld.kcloud.fragment.manager.BaseFragment;
import cld.kcloud.user.KCloudUser;
import cld.kcloud.user.KCloudUserInfoActivity;
import cld.kcloud.utils.KCloudUserUtils;
import cld.kcloud.utils.control.CldInputDialog;
import cld.kcloud.utils.control.CldProgress;
import cld.kcloud.utils.KCloudCommonUtil;
import cld.kcloud.utils.control.CldProgress.CldProgressListener;
import cld.kcloud.widget.controller.KCloudController;
import cld.kcloud.widget.controller.KCloudWidgetList;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;

public class PersonalMobileFragment extends BaseFragment implements
		OnClickListener {
	private static final String TAG = "PersonalMobileFragment";
	private String mStrMobile = "";
	private View viewMobileManager = null;
	private KCloudWidgetList mWidgetList = new KCloudWidgetList();
	private Context mContext;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
	}

	/** 上次获取验证码时间 */
	private static final int GET_CODE_TIMEOUT = 60;
	/** 验证码等待超时 */
	private int mCodeTime = GET_CODE_TIMEOUT;
	/** 验证码即时时间 */
	private Timer mTimer = new Timer();
	/** 获取验证码定时器 */
	private TimerTask mCodeTask = null;
	/** 获取验证码任务 */
	private Handler mCodeHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CLDMessageId.MSG_ID_UPDATE_CODE_RAMAIN_TIME: {// 验证码倒计时
				if (mCodeTime <= 0) {
					resetGetCodeTimer();
				} else {
					Button btn = (Button) getControl(R.id.mobile_manager_btn_vericode);
					if (btn != null) {
						String sText = String.format("%s(%d)", 
								KCloudCommonUtil.getString(R.string.vericode_has_send),
								mCodeTime--);
						
						SpannableString ss = new SpannableString(sText);
						ss.setSpan(new ForegroundColorSpan(
								KCloudCommonUtil.getColor(R.color.text_count_down_color)), 
								6,
								sText.length(),
								Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
						btn.setText(ss);
					}
				}
				break;
			}
			default:
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (viewMobileManager == null) {
			viewMobileManager = inflater.inflate(
					R.layout.fragment_mobile_manager, container, false);

			bindControl(R.id.mobile_manager_edit_mobile,
					viewMobileManager.findViewById(R.id.mobile_manager_edit_mobile),
					this, true, true);
			bindControl(R.id.mobile_manager_edit_vericode,
					viewMobileManager.findViewById(R.id.mobile_manager_edit_vericode),
					this, true, true);
			bindControl(R.id.mobile_manager_btn_vericode,
					viewMobileManager.findViewById(R.id.mobile_manager_btn_vericode),
					this, true, false);
			bindControl(R.id.mobile_manager_btn_return,
					viewMobileManager.findViewById(R.id.mobile_manager_btn_return),
					this, true, true);
			bindControl(R.id.mobile_manager_btn_commit,
					viewMobileManager.findViewById(R.id.mobile_manager_btn_commit),
					this, true, false);
			bindControl(R.id.mobile_manager_framlayout_ok,
					viewMobileManager.findViewById(R.id.mobile_manager_framlayout_ok),
					this, false, true);
			bindControl(R.id.mobile_manager_framlayout_modify,
					viewMobileManager.findViewById(R.id.mobile_manager_framlayout_modify),
					null, true, true);

			initControl();
		}
		return viewMobileManager;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		//强制隐藏输入法
		getActivity().getWindow().setSoftInputMode(WindowManager.
			LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public boolean onBackPressed() {
		cancelCodeTask();
		if (getActivity() != null)
			((KCloudUserInfoActivity) getActivity()).doBack();
		return true;
	}

	@SuppressLint("NewApi")
	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.mobile_manager_btn_return: {
			if (getActivity() != null)
				((KCloudUserInfoActivity) getActivity()).doBack();
			break;
		}

		case R.id.mobile_manager_btn_vericode: {
			// 获取修改手机验证码
			EditText editMobile = (EditText) getControl(R.id.mobile_manager_edit_mobile);

			if (editMobile != null) {
				String strMobile = editMobile.getText().toString();
				if (!CldKConfigAPI.getInstance().isPhoneNum(strMobile)) {
					// 提示手机号非法
					KCloudCommonUtil.makeText(R.string.kaccount_login_mobile_err);
					return;
				}
				if (strMobile.equals(KCloudUser.getInstance().getUserInfo()
						.getMobile())) {
					// 提示手机号一致
					KCloudCommonUtil.makeText(R.string.userinfo_revise_mobile_same);
					return;
				}
				
				if (!CldPhoneNet.isNetConnected()) {
					KCloudCommonUtil.makeText(R.string.common_network_abnormal);
					return;
				}

				mCodeTime = GET_CODE_TIMEOUT;
				KCloudController.setEnabledById(
						R.id.mobile_manager_btn_vericode, false, mWidgetList);
				startCodeTask();

				CldLog.i(TAG, "strMobile: " + strMobile);
				CldLog.i(TAG, "userMobile: " + KCloudUser.getInstance().getUserInfo().getMobile());
				if (KCloudUser.getInstance().getUserInfo().getMobile().isEmpty()) {
					CldKAccountAPI.getInstance().getMobileVeriCode(strMobile, 
							CldBussinessCode.BIND_MOBILE);
				} else {
					CldKAccountAPI.getInstance().getVerCodeToReviseMobile(
							strMobile,
							KCloudUser.getInstance().getUserInfo().getMobile());
				}
			}
			break;
		}

		case R.id.mobile_manager_btn_commit: {
			if (!CldPhoneNet.isNetConnected()) {
				KCloudCommonUtil.makeText(R.string.common_network_abnormal);
				return;
			}

			EditText editMobile = (EditText) getControl(R.id.mobile_manager_edit_mobile);
			EditText editVeriCode = (EditText) getControl(R.id.mobile_manager_edit_vericode);

			if (editMobile != null && editVeriCode != null) {
				mStrMobile = editMobile.getText().toString();
				String strVeriCode = editVeriCode.getText().toString();

				if (!CldKConfigAPI.getInstance().isPhoneNum(mStrMobile)) {
					// 提示手机号非法
					KCloudCommonUtil.makeText(R.string.kaccount_input_mobile_err);
					break;
				}
				CldProgress.showProgress(mContext,
						new CldProgressListener() {
							public void onCancel() {
							}
						});
				
				if (KCloudUser.getInstance().getUserInfo().getMobile().isEmpty()) {
					CldKAccountAPI.getInstance().bindMobile(mStrMobile, strVeriCode);
				} else {
					CldKAccountAPI.getInstance().reviseMobile(mStrMobile,
							KCloudUser.getInstance().getUserInfo().getMobile(),
							strVeriCode);
				}
			}
			break;
		}
		
		case R.id.mobile_manager_edit_mobile: {
			final EditText et = (EditText) arg0;
			String title = KCloudCommonUtil.getString(
					R.string.input_dialog_title_modify_mobile);
			String hint = KCloudCommonUtil.getString(
					R.string.input_dialog_hint_modify_mobile);
			
			CldInputDialog.showInputDialog(mContext, title, hint, et.getText().toString(),
					CldInputDialog.CldInputType.eInputType_ModifyMobil,
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
						}
					});
			break;
		}
		
		case R.id.mobile_manager_edit_vericode: {
			final EditText et = (EditText) arg0;
			String title = KCloudCommonUtil.getString(
					R.string.input_dialog_title_vericode);
			String hint = KCloudCommonUtil.getString(
					R.string.input_dialog_hint_vericode);
			
			CldInputDialog.showInputDialog(mContext, title, hint, et.getText().toString(),
					CldInputDialog.CldInputType.eInputType_Vericode,
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
						}
					});
			break;
		}

		default:
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

	public void initControl() {		
		final EditText editMobile = (EditText) getControl(R.id.mobile_manager_edit_mobile);
		final EditText editVeriCode = (EditText) getControl(R.id.mobile_manager_edit_vericode);
		final Button btnVeriCode = (Button) getControl(R.id.mobile_manager_btn_vericode);
		final Button btnLogin = (Button) getControl(R.id.mobile_manager_btn_commit);

		if (editMobile != null) {
			editMobile.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable arg0) {

				}

				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {

				}

				@Override
				public void onTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
					if (btnVeriCode != null) {
						if (!TextUtils.isEmpty(editMobile.getText().toString())
								&& editMobile.getText().toString().length() == 11) {
							btnVeriCode.setEnabled(true);
						} else {
							btnVeriCode.setEnabled(false);
						}
					}

					if (editVeriCode != null) {
						if (btnLogin != null) {
							if (!TextUtils.isEmpty(editMobile.getText()
									.toString())
									&& !TextUtils.isEmpty(editVeriCode
											.getText().toString())) {
								btnLogin.setEnabled(true);
							} else {
								btnLogin.setEnabled(false);
							}
						}
					}
				}

			});
			
			editMobile.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus == true) {
						String title = KCloudCommonUtil.getString(
								R.string.input_dialog_title_modify_mobile);
						String hint = KCloudCommonUtil.getString(
								R.string.input_dialog_hint_modify_mobile);
						
						CldInputDialog.showInputDialog(mContext, title, hint, editMobile
								.getText().toString(),
								CldInputDialog.CldInputType.eInputType_ModifyMobil,
								CldInputDialog.CldButtonType.eButton_Confirm,
								new CldInputDialog.CldInputDialogListener() {

									@Override
									public void onOk(String strInput) {
										editMobile.setText(strInput);
										editMobile.setSelection(strInput.length());
										KCloudUserUtils.setInputMethodVisible(mContext, editMobile, false);
									}

									@Override
									public void onCancel() {
										KCloudUserUtils.setInputMethodVisible(mContext, editMobile, false);
									}
								});
					}
				}
			});
		}

		if (editVeriCode != null) {
			editVeriCode.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable arg0) {

				}

				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {

				}

				@Override
				public void onTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
					if (editVeriCode != null) {
						if (btnLogin != null) {
							if (!TextUtils.isEmpty(editVeriCode.getText()
									.toString())) {
								btnLogin.setEnabled(true);

							} else {
								btnLogin.setEnabled(false);
							}
						}
					}
				}
			});
			
			editVeriCode.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus == true) {
						String title = KCloudCommonUtil.getString(
								R.string.input_dialog_title_vericode);
						String hint = KCloudCommonUtil.getString(
								R.string.input_dialog_hint_vericode);
						
						CldInputDialog.showInputDialog(mContext, title, hint, editVeriCode
								.getText().toString(),
								CldInputDialog.CldInputType.eInputType_Vericode,
								CldInputDialog.CldButtonType.eButton_Confirm,
								new CldInputDialog.CldInputDialogListener() {

									@Override
									public void onOk(String strInput) {
										editVeriCode.setText(strInput);
										editVeriCode.setSelection(strInput.length());
										KCloudUserUtils.setInputMethodVisible(mContext, editVeriCode, false);
									}

									@Override
									public void onCancel() {
										KCloudUserUtils.setInputMethodVisible(mContext, editVeriCode, false);
									}
								});
					}
				}
			});
		}
	}

	public void startCodeTask() {
		cancelCodeTask();
		mCodeTask = new TimerTask() {
			@Override
			public void run() {
				if (null != mCodeHandler) {
					mCodeHandler.sendEmptyMessage(
							CLDMessageId.MSG_ID_UPDATE_CODE_RAMAIN_TIME);
				}
			}
		};
		mTimer.schedule(mCodeTask, 0, 1000);
	}

	public void cancelCodeTask() {
		if (mCodeTask != null) {
			mCodeTask.cancel();
			mCodeTask = null;
		}
	}

	public void resetGetCodeTimer() {
		Button btn = (Button) getControl(R.id.mobile_manager_btn_vericode);
		cancelCodeTask();
		mCodeTime = GET_CODE_TIMEOUT;
		if (btn != null) {
			btn.setEnabled(true);
			btn.setTextColor(KCloudCommonUtil.getColor(R.color.text_normal_color));
			btn.setText(KCloudCommonUtil.getString(R.string.vericode_resend));
		}
	}

	public void onHandleMessage(Message message) {
		CldLog.i(TAG, String.valueOf(message.what));
		switch (message.what) {

		case CLDMessageId.MSG_ID_USERINFO_BIND_MOBILE_VERICODE_SUCCESS:
		case CLDMessageId.MSG_ID_USERINFO_REVISE_MOBILE_VERICODE_SUCCESS: {
			// 获取验证码成功
			KCloudCommonUtil.makeText(R.string.kaccount_get_vericode_success);
			break;
		}

		case CLDMessageId.MSG_ID_USERINFO_BIND_MOBILE_VERICODE_FAILED:
		case CLDMessageId.MSG_ID_USERINFO_REVISE_MOBILE_VERICODE_FAILED: {
			resetGetCodeTimer();
			// 获取验证码失败
			if (null == message.obj) {
				break;
			}
			int errCode = (Integer) message.obj;
			switch (errCode) {
			case CldOlsErrCode.NET_NO_CONNECTED:
			case CldOlsErrCode.NET_TIMEOUT:
				KCloudCommonUtil.makeText(R.string.common_network_abnormal);
				break;
			case 201:
				KCloudCommonUtil.makeTextLong(R.string.userinfo_mobile_has_binded);
				break;
			case 903:
				KCloudCommonUtil.makeText(R.string.kaccount_vericode_repeat_get);
				break;
			case 906:
				KCloudCommonUtil.makeText(R.string.kaccount_vericode_timer_more);
				break;
			default:
				KCloudCommonUtil.makeText(R.string.kaccount_get_vericode_failed);
				break;
			}
			break;
		}

		case CLDMessageId.MSG_ID_USERINFO_BIND_MOBILE_SUCCESS:
		case CLDMessageId.MSG_ID_USERINFO_REVISE_MOBILE_SUCCESS: {
			if (CldProgress.isShowProgress()) {
				CldProgress.cancelProgress();
			}
			KCloudCommonUtil.makeText(R.string.userinfo_revise_mobile_success);
			KCloudUser.getInstance().getUserInfo().setMobile(mStrMobile);
			KCloudController.setVisibleById(R.id.mobile_manager_framlayout_modify,
					false, mWidgetList);
			KCloudController.setVisibleById(R.id.mobile_manager_framlayout_ok,
					true, mWidgetList);
			break;
		}
		
		case CLDMessageId.MSG_ID_USERINFO_BIND_MOBILE_FAILED:
		case CLDMessageId.MSG_ID_USERINFO_REVISE_MOBILE_FAILED:
			if (CldProgress.isShowProgress()) {
				CldProgress.cancelProgress();
			}
			// 修改手机失败
			if (null == message.obj) {
				break;
			}
			int errCode = (Integer) message.obj;
			switch (errCode) {
			case CldOlsErrCode.NET_NO_CONNECTED:
			case CldOlsErrCode.NET_TIMEOUT:
				KCloudCommonUtil.makeText(R.string.common_network_abnormal);
				break;
			case 203:
				KCloudCommonUtil.makeTextLong(R.string.userinfo_mobile_has_binded);
				break;
			case 907:
				KCloudCommonUtil.makeText(R.string.kaccount_vericode_has_invalid);
				break;
			case 908:
				KCloudCommonUtil.makeText(R.string.kaccount_vericode_err);
				break;
			case 909:
				KCloudCommonUtil.makeText(R.string.kaccount_vericode_has_used);
				break;
			default:
				KCloudCommonUtil.makeText(R.string.userinfo_revise_mobile_failed);
				break;
			}
			break;
		}
	}
}
