package cld.kcloud.fragment;

import java.util.Timer;
import java.util.TimerTask;

import com.cld.device.CldPhoneNet;
import com.cld.log.CldLog;
import com.cld.ols.api.CldKAccountAPI;
import com.cld.ols.api.CldKAccountAPI.CldBussinessCode;
import com.cld.ols.api.CldKConfigAPI;
import com.cld.ols.tools.CldErrUtil.CldOlsErrCode;

import cld.kcloud.center.KCloudAppUtils.CLDMessageId;
import cld.kcloud.center.KCloudAppUtils.InputError;
import cld.kcloud.center.R;
import cld.kcloud.fragment.manager.BaseFragment;
import cld.kcloud.user.KCloudUserActivity;
import cld.kcloud.utils.KCloudShareUtils;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * K云用户找回密码相关
 * @author wuyl
 */
public class PasswordFragment extends BaseFragment implements OnClickListener {
	private static final String TAG = "FragmentPassword";
	
	private View viewPassword = null;
	private KCloudWidgetList mWidgetList = new KCloudWidgetList();
	private Context mContext;
	private String mNewPassword = "";
	private boolean mFromPassword = false;
	
	/** 上次获取验证码时间 */
	private static final int GET_CODE_TIMEOUT = 60;
	/** 验证码等待超时 */
	private int mCodeTime = GET_CODE_TIMEOUT;
	/** 验证码即时时间 */
	private Timer mTimer = new Timer();
	/** 获取验证码定时器 */
	private TimerTask mCodeTask = null;
	/** 获取验证码任务 */
	@SuppressLint({ "DefaultLocale", "HandlerLeak" }) 
	private Handler mCodeHandler = new Handler() {		
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CLDMessageId.MSG_ID_UPDATE_CODE_RAMAIN_TIME: {// 验证码倒计时
				if (mCodeTime <= 0) {
					resetGetCodeTimer();
				} else {
					Button btn = (Button) getControl(R.id.password_btn_vericode);
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
		if (viewPassword == null) {
			viewPassword = inflater.inflate(R.layout.fragment_login_password,
					container, false);

			bindControl(R.id.password_btn_vericode,
					viewPassword.findViewById(R.id.password_btn_vericode),
					this, true, false);
			bindControl(R.id.password_btn_next,
					viewPassword.findViewById(R.id.password_btn_next), 
					this, true, false);
			bindControl(R.id.password_btn_modify,
					viewPassword.findViewById(R.id.password_btn_modify), 
					this, true, true);
			bindControl(R.id.password_btn_login,
					viewPassword.findViewById(R.id.password_btn_login),
					this, true, true);

			bindControl(R.id.password_edit_mobile,
					viewPassword.findViewById(R.id.password_edit_mobile), 
					this, true, true);
			bindControl(R.id.password_edit_vericode,
					viewPassword.findViewById(R.id.password_edit_vericode),
					this, true, true);
			bindControl(R.id.password_edit_password,
					viewPassword.findViewById(R.id.password_edit_password),
					this, true, true);
			bindControl(R.id.password_edit_password_affirm,
					viewPassword.findViewById(R.id.password_edit_password_affirm),
					this, true, true);
			bindControl(R.id.password_text_title,
					viewPassword.findViewById(R.id.password_text_title),
					null, true, true);
			
			bindControl(R.id.password_framelayout_get,
					viewPassword.findViewById(R.id.password_framelayout_get),
					null, true, true);
			bindControl(R.id.password_framelayout_modify,
					viewPassword.findViewById(R.id.password_framelayout_modify),
					null, false, true);
			bindControl(R.id.password_framelayout_ok,
					viewPassword.findViewById(R.id.password_framelayout_ok),
					null, false, true);

			initContorl();
		}
		return viewPassword;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		//强制隐藏输入法
		getActivity().getWindow().setSoftInputMode(WindowManager.
			LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		//让TextView获取焦点，防止EditText自动获取焦点
		TextView tvTitle = (TextView) getControl(R.id.password_text_title);
		tvTitle.requestFocus();
	}
	
	@Override
	public boolean onBackPressed() {
		cancelCodeTask();
		if (getActivity() != null)
			((KCloudUserActivity) getActivity()).doBack();
		return true;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (getArguments() != null) {
			if ("main".equals(getArguments().getString("activity"))) {
				if (viewPassword != null) {
					FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) 
							viewPassword.getLayoutParams();
					params.leftMargin = 72;
					viewPassword.setLayoutParams(params);
				}
			}
		}
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.password_btn_next: {
			EditText editMobile = (EditText) getControl(R.id.password_edit_mobile);
			EditText editVericode = (EditText) getControl(R.id.password_edit_vericode);

			if (editVericode != null && editVericode != null) {
				String strMobile = editMobile.getText().toString();
				String strVeriCode = editVericode.getText().toString();

				if (!CldKConfigAPI.getInstance().isPhoneNum(strMobile)) {
					KCloudCommonUtil.makeText(R.string.kaccount_input_mobile_err);
					break;
				}
				
				if (TextUtils.isEmpty(strVeriCode)) {
					KCloudCommonUtil.makeText(R.string.kaccount_vericode_has_err);
					break;
				}
				
				if (!CldPhoneNet.isNetConnected()) {
					KCloudCommonUtil.makeText(R.string.common_network_abnormal);
					break;
				}

				CldProgress.showProgress(mContext,
						new CldProgressListener() {
							public void onCancel() {
							}
						});
				
				CldLog.d(TAG, "  strMobile= " + strMobile);
				CldLog.d(TAG, "strVeriCode= " + strVeriCode);
				CldKAccountAPI.getInstance().checkMobileVeriCode(strMobile,
						strVeriCode, CldBussinessCode.RESET_PWD);
			}
			break;
		}

		case R.id.password_btn_modify: {
			String oldPwd = "", newPwd = "", affirmPwd = "";

			EditText editNew = (EditText) getControl(R.id.password_edit_password);
			EditText editAffirm = (EditText) getControl(R.id.password_edit_password_affirm);
			if (editNew != null) {
				newPwd = editNew.getText().toString();
			}
			if (editAffirm != null) {
				affirmPwd = editAffirm.getText().toString();
			}

			// oldPwd参数不需要
			InputError errorCode = KCloudUserUtils.checkRevisePwd(oldPwd, newPwd, affirmPwd);
			switch (errorCode) {
			case eERROR_OLD_PASSWORD_EMPTY:
				KCloudCommonUtil.makeText(R.string.userinfo_revise_pwd_old_not_empty);
				break;

			case eERROR_NEW_PASSWORD_EMPTY:
				KCloudCommonUtil.makeText(R.string.userinfo_revise_pwd_new_not_empty);
				break;

			case eERROR_AFFIRM_PASSWORD_EMPTY:
				KCloudCommonUtil.makeText(R.string.userinfo_revise_pwd_ensure_new);
				break;

			case eERROR_PASSWORD_INPUT:
				KCloudCommonUtil.makeText(R.string.kaccount_reset_pwd_new_err);
				break;

			case eERROR_PASSWORD_CONTAINS_SPECAIL:
				KCloudCommonUtil.makeText(R.string.kaccount_reset_pwd_new_specail);
				break;

			case eERROR_PASSWORD_LESSONENUM:
				KCloudCommonUtil.makeText(R.string.kaccount_reset_pwd_new_lessonenum);
				break;

			case eERROR_NEW_OLD_SAME:
				KCloudCommonUtil.makeText(R.string.userinfo_revise_pwd_same);
				break;

			case eERROR_NEW_AFFIRM_UNSAME:
				KCloudCommonUtil.makeText(R.string.userinfo_revise_pwd_new_old_unsame);
				break;

			case eERROR_NONE:
				if (!CldPhoneNet.isNetConnected()) {
					KCloudCommonUtil.makeText(R.string.common_network_abnormal);
					return;
				}
				CldProgress.showProgress(mContext,
						new CldProgressListener() {
							public void onCancel() {
							}
						});
				
				mNewPassword = newPwd;
				String strMobile = ((EditText) getControl(R.id.password_edit_mobile))
						.getText().toString();
				String strVercode = ((EditText) getControl(R.id.password_edit_vericode))
						.getText().toString();
				
				CldLog.d(TAG, " strMobile= " + strMobile);
				CldLog.d(TAG, "    newPwd= " + newPwd);
				CldLog.d(TAG, "strVercode= " + strVercode);
				CldKAccountAPI.getInstance().retrievePwdByMobile(strMobile,
						newPwd, strVercode, CldBussinessCode.RESET_PWD);
				
			default:
				break;
			}
		}
		break;

		case R.id.password_btn_vericode: {
			EditText editMobile = (EditText) getControl(R.id.password_edit_mobile);
			if (editMobile != null) {
				String strMobile = editMobile.getText().toString();

				if (!CldKConfigAPI.getInstance().isPhoneNum(strMobile)) {
					// 提示手机号非法
					KCloudCommonUtil.makeText(R.string.kaccount_login_mobile_err);
					return;
				}
				if (!CldPhoneNet.isNetConnected()) {
					KCloudCommonUtil.makeText(R.string.common_network_abnormal);
					return;
				}

				mCodeTime = GET_CODE_TIMEOUT;
				getControl(R.id.password_btn_vericode).setEnabled(false);
				startCodeTask();

				CldKAccountAPI.getInstance().getMobileVeriCode(strMobile,
						CldBussinessCode.RESET_PWD);
			}
			break;
		}

		case R.id.password_btn_login: {
			//直接登录
			CldProgress.showProgress(mContext, 
					KCloudCommonUtil.getString(R.string.login_text_doing), 
					new CldProgressListener() {
						public void onCancel() {
						}
					});
			mFromPassword = true;
			
			String strMobile = ((EditText) getControl(R.id.password_edit_mobile))
					.getText().toString();
			String loginName = CldKAccountAPI.getInstance().getLoginName();
			CldLog.i(TAG, "   LoginName = " + CldKAccountAPI.getInstance().getLoginName());
			
			if (loginName == null || loginName.isEmpty()) {
				loginName = strMobile;
			}
			CldLog.i(TAG, "strMobile = " + strMobile);
			CldLog.i(TAG, "mNewPassword = " + mNewPassword);
			CldKAccountAPI.getInstance().login(loginName, mNewPassword);
			break;
		}
		
		case R.id.password_edit_mobile: {
			final EditText et = (EditText) arg0;
			String title = KCloudCommonUtil.getString(R.string.input_dialog_title_mobile);
			String hint = KCloudCommonUtil.getString(R.string.input_dialog_hint_mobile);
			
			CldInputDialog.showInputDialog(mContext, title, hint, et.getText().toString(),
					CldInputDialog.CldInputType.eInputType_Mobil,
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
		
		case R.id.password_edit_vericode: {
			final EditText et = (EditText) arg0;
			String title = KCloudCommonUtil.getString(R.string.input_dialog_title_vericode);
			String hint = KCloudCommonUtil.getString(R.string.input_dialog_hint_vericode);
			
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
		
		case R.id.password_edit_password: {
			final EditText et = (EditText) arg0;
			String title = KCloudCommonUtil.getString(
					R.string.input_dialog_title_modify_password);
			String hint = KCloudCommonUtil.getString(
					R.string.input_dialog_hint_modify_password);
			
			CldInputDialog.showInputDialog(mContext, title, hint, et.getText().toString(),
					CldInputDialog.CldInputType.eInputType_Password,
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
		case R.id.password_edit_password_affirm: {
			final EditText et = (EditText) arg0;
			String title = KCloudCommonUtil.getString(
					R.string.input_dialog_title_modify_password_affirm);
			String hint = KCloudCommonUtil.getString(
					R.string.input_dialog_hint_modify_password);
			
			CldInputDialog.showInputDialog(mContext, title, hint, et.getText().toString(),
					CldInputDialog.CldInputType.eInputType_Password,
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
	 * 初始化控件
	 */
	public void initContorl() {
		//让TextView获取焦点，防止EditText自动获取焦点
		TextView tvTitle = (TextView) getControl(R.id.password_text_title);
		tvTitle.requestFocus();
		
		final EditText editMobile = (EditText) getControl(R.id.password_edit_mobile);
		final EditText editVeriCode = (EditText) getControl(R.id.password_edit_vericode);
		final EditText editPassword = (EditText) getControl(R.id.password_edit_password);
		final EditText editPasswordAffirm = (EditText) getControl(R.id.password_edit_password_affirm);

		if (editMobile != null) {
			editMobile.addTextChangedListener(new TextWatcher() {

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					EditText editMobile = (EditText) getControl(R.id.password_edit_mobile);
					EditText editVeriCode = (EditText) getControl(R.id.password_edit_vericode);

					if (editMobile != null) {
						Button btnVeriCode = (Button) getControl(R.id.password_btn_vericode);

						if (btnVeriCode != null) {
							if (!TextUtils.isEmpty(editMobile.getText().toString())
									&& editMobile.getText().toString().length() == 11) {
								btnVeriCode.setEnabled(true);
							} else {
								btnVeriCode.setEnabled(false);
							}
						}
					}

					Button btnNext = (Button) getControl(R.id.password_btn_next);
					if (btnNext != null) {
						if (!TextUtils.isEmpty(editMobile.getText().toString())
								&& !TextUtils.isEmpty(editVeriCode.getText().toString())) {
							btnNext.setEnabled(true);
						} else {
							btnNext.setEnabled(false);
						}
					}
				}
			});
			editMobile.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus == true) {
						String title = KCloudCommonUtil.getString(
								R.string.input_dialog_title_mobile);
						String hint = KCloudCommonUtil.getString(
								R.string.input_dialog_hint_mobile);
						
						CldInputDialog.showInputDialog(mContext, title, hint, 
								editMobile.getText().toString(),
								CldInputDialog.CldInputType.eInputType_Mobil,
								CldInputDialog.CldButtonType.eButton_Confirm,
								new CldInputDialog.CldInputDialogListener() {

									@Override
									public void onOk(String strInput) {
										editMobile.setText(strInput);
										editMobile.setSelection(strInput.length());
										KCloudUserUtils.setInputMethodVisible(mContext, 
												editMobile, false);
									}

									@Override
									public void onCancel() {
										KCloudUserUtils.setInputMethodVisible(mContext, 
												editMobile, false);
									}
								});
					}
				}
			});
			
			editMobile.setText(KCloudShareUtils.getString("bindMobile"));
		}

		if (editVeriCode != null) {
			editVeriCode.addTextChangedListener(new TextWatcher() {

				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
				}

				@Override
				public void onTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
				}

				@Override
				public void afterTextChanged(Editable arg0) {
					EditText editVeriCode = (EditText) getControl(R.id.password_edit_vericode);
					if (editVeriCode != null) {
						Button btnNext = (Button) getControl(R.id.password_btn_next);
						if (btnNext != null) {
							if (!TextUtils.isEmpty(editVeriCode.getText().toString())) {
								btnNext.setEnabled(true);
							} else {
								btnNext.setEnabled(false);
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
						
						CldInputDialog.showInputDialog(mContext, title, hint,
								editVeriCode.getText().toString(),
								CldInputDialog.CldInputType.eInputType_Vericode,
								CldInputDialog.CldButtonType.eButton_Confirm,
								new CldInputDialog.CldInputDialogListener() {

									@Override
									public void onOk(String strInput) {
										editVeriCode.setText(strInput);
										editVeriCode.setSelection(strInput.length());
										KCloudUserUtils.setInputMethodVisible(mContext, 
												editVeriCode, false);
									}

									@Override
									public void onCancel() {
										KCloudUserUtils.setInputMethodVisible(mContext, 
												editVeriCode, false);
									}
								});
					}
				}
			});
		}
		
		if (editPassword != null) {
			editPassword.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus == true) {
						String title = KCloudCommonUtil.getString(
								R.string.input_dialog_title_modify_password);
						String hint = KCloudCommonUtil.getString(
								R.string.input_dialog_hint_modify_password);
						
						CldInputDialog.showInputDialog(mContext, title, hint, 
								editPassword.getText().toString(),
								CldInputDialog.CldInputType.eInputType_Password,
								CldInputDialog.CldButtonType.eButton_Confirm,
								new CldInputDialog.CldInputDialogListener() {

									@Override
									public void onOk(String strInput) {
										editPassword.setText(strInput);
										editPassword.setSelection(strInput.length());
										KCloudUserUtils.setInputMethodVisible(mContext, 
												editPassword, false);
									}

									@Override
									public void onCancel() {
										KCloudUserUtils.setInputMethodVisible(mContext, 
												editPassword, false);
									}
								});
					}
				}
			});
		}
		
		if (editPasswordAffirm != null) {
			editPasswordAffirm.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus == true) {
						String title = KCloudCommonUtil.getString(
								R.string.input_dialog_title_modify_password_affirm);
						String hint = KCloudCommonUtil.getString(
								R.string.input_dialog_hint_modify_password);
						
						CldInputDialog.showInputDialog(mContext, title, hint, 
								editPasswordAffirm.getText().toString(),
								CldInputDialog.CldInputType.eInputType_Password,
								CldInputDialog.CldButtonType.eButton_Confirm,
								new CldInputDialog.CldInputDialogListener() {

									@Override
									public void onOk(String strInput) {
										editPasswordAffirm.setText(strInput);
										editPasswordAffirm.setSelection(strInput.length());
										KCloudUserUtils.setInputMethodVisible(mContext, 
												editPasswordAffirm, false);
									}

									@Override
									public void onCancel() {
										KCloudUserUtils.setInputMethodVisible(mContext, 
												editPasswordAffirm, false);
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
		Button btn = (Button) getControl(R.id.password_btn_vericode);
		cancelCodeTask();
		mCodeTime = GET_CODE_TIMEOUT;
		if (btn != null) {
			btn.setEnabled(true);
			btn.setText(KCloudCommonUtil.getString(R.string.vericode_resend));
		}
	}
	
	
	public boolean isFromPassword() {
		return mFromPassword;
	}

	public void onHandleMessage(Message message) {
		CldLog.i(TAG, String.valueOf(message.what));
		switch (message.what) {
		case CLDMessageId.MSG_ID_PASSWORD_GET_VERICODE_SUCCESS: {
			KCloudCommonUtil.makeText(R.string.kaccount_get_vericode_success);
			break;
		}

		case CLDMessageId.MSG_ID_PASSWORD_GET_VERICODE_FAILED: {
			resetGetCodeTimer();
			if (null == message.obj) {
				break;
			}
			int errCode = (Integer) message.obj;
			switch (errCode) {
			case CldOlsErrCode.NET_NO_CONNECTED:
			case CldOlsErrCode.NET_TIMEOUT:
				KCloudCommonUtil.makeText(R.string.common_network_abnormal);
				break;

			case 202:
				KCloudCommonUtil.makeText(R.string.kaccount_retrive_pwd_mobile_unbind);
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

		case CLDMessageId.MSG_ID_PASSWORD_CHECK_VERICODE_SUCCESS: {
			if (CldProgress.isShowProgress()) {
				CldProgress.cancelProgress();
			}
			cancelCodeTask();
			KCloudController.setVisibleById(R.id.password_framelayout_get, 
					false, mWidgetList);
			KCloudController.setVisibleById(R.id.password_framelayout_modify, 
					true, mWidgetList);
			KCloudController.setVisibleById(R.id.password_framelayout_ok, 
					false, mWidgetList);
			break;
		}
		
		case CLDMessageId.MSG_ID_PASSWORD_CHECK_VERICODE_FAILED: {
			if (CldProgress.isShowProgress()) {
				CldProgress.cancelProgress();
			}
			resetGetCodeTimer();
			KCloudCommonUtil.makeText(R.string.kaccount_vericode_has_err);
			break;
		}

		case CLDMessageId.MSG_ID_PASSWORD_SET_PWD_SUCCESS: {
			if (CldProgress.isShowProgress()) {
				CldProgress.cancelProgress();
			}

			KCloudController.setVisibleById(R.id.password_framelayout_get, 
					false, mWidgetList);
			KCloudController.setVisibleById(R.id.password_framelayout_modify, 
					false, mWidgetList);
			KCloudController.setVisibleById(R.id.password_framelayout_ok, 
					true, mWidgetList);
			break;
		}

		case CLDMessageId.MSG_ID_PASSWORD_SET_PWD_FAILED: {
			if (CldProgress.isShowProgress()) {
				CldProgress.cancelProgress();
			}
			if (null == message.obj) {
				break;
			}
			int errCode = (Integer) message.obj;
			switch (errCode) {
			case CldOlsErrCode.NET_NO_CONNECTED:
			case CldOlsErrCode.NET_TIMEOUT:
				KCloudCommonUtil.makeText(R.string.common_network_abnormal);
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
				KCloudCommonUtil.makeText(R.string.kaccount_reset_pwd_failed);
				break;
			}
			break;
		}
		
		case CLDMessageId.MSG_ID_LOGIN_ACCOUNT_LOGIN_FAILED:
			Log.d(TAG, " MSG_ID_LOGIN_ACCOUNT_LOGIN_FAILED ");
			mFromPassword = false;
			//直接登录失败，跳转到登录界面
			if (getActivity() != null)
				((KCloudUserActivity) getActivity()).doDirectLoginFail();
			break;

		default:
			break;
		}
	}
}