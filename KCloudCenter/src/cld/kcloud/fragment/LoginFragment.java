package cld.kcloud.fragment;

import java.util.Timer;
import java.util.TimerTask;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cld.kcloud.center.KCloudAppUtils;
import cld.kcloud.center.KCloudAppUtils.CLDMessageId;
import cld.kcloud.center.KCloudAppUtils.InputError;
import cld.kcloud.center.R;
import cld.kcloud.fragment.manager.BaseFragment;
import cld.kcloud.user.KCloudUser;
import cld.kcloud.user.KCloudUserInfoActivity;
import cld.kcloud.utils.KCloudCommonUtil;
import cld.kcloud.utils.KCloudShareUtils;
import cld.kcloud.utils.KCloudUserUtils;
import cld.kcloud.utils.control.CldInputDialog;
import cld.kcloud.utils.control.CldProgress;
import cld.kcloud.utils.control.CldProgress.CldProgressListener;
import cld.kcloud.utils.control.CldQRCode;
import cld.kcloud.widget.controller.KCloudController;
import cld.kcloud.widget.controller.KCloudWidgetList;
import com.cld.device.CldPhoneNet;
import com.cld.log.CldLog;
import com.cld.ols.api.CldKAccountAPI;
import com.cld.ols.api.CldKAccountAPI.CldBussinessCode;
import com.cld.ols.api.CldKConfigAPI;
import com.cld.ols.dal.CldDalKAccount;
import com.cld.ols.tools.CldErrUtil.CldOlsErrCode;
import com.google.zxing.WriterException;

/**
 * K云用户登录相关
 * @author wuyl
 */
public class LoginFragment extends BaseFragment implements OnClickListener {
	private static final String TAG = "FragmentLogin";
	private View viewLogin = null;
	private boolean mIsUsingAccount = false;
	private KCloudWidgetList mWidgetList = new KCloudWidgetList();
	private Context mContext;
	/**
	 * 获取二维码次数
	 */
	private static final int GET_QRCODE_TIMEOUT = 5;
	private int mQRCodeTime = GET_QRCODE_TIMEOUT;

	/** 上次获取验证码时间 */
	private static final int GET_CODE_TIMEOUT = 60;
	/** 验证码等待超时 */
	private int mCodeTime = GET_CODE_TIMEOUT;
	/** 验证码即时时间 */
	private Timer mTimer = new Timer();
	/** 获取验证码定时器 */
	private TimerTask mCodeTask = null;
	/** 获取验证码任务 */
	
	@SuppressLint("HandlerLeak") 
	private Handler mCodeHandler = new Handler() {
		@SuppressLint("DefaultLocale") 
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CLDMessageId.MSG_ID_UPDATE_CODE_RAMAIN_TIME: {// 验证码倒计时
				if (mCodeTime <= 0) {
					resetGetCodeTimer();
				} else {
					Button btn = (Button) getControl(R.id.login_btn_vericode);
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
		if (viewLogin == null) {
			viewLogin = inflater.inflate(R.layout.fragment_login_account,
					container, false);

			bindControl(R.id.login_layout_left,
					viewLogin.findViewById(R.id.login_layout_left), 
					null, true, true);
			
			//手机登录
			bindControl(R.id.login_framelayout_mobile,
					viewLogin.findViewById(R.id.login_framelayout_mobile), 
					null, true, true);
			bindControl(R.id.login_text_title_mobile,
					viewLogin.findViewById(R.id.login_text_title_mobile), 
					null, true, false);
			bindControl(R.id.login_edit_mobile,
					viewLogin.findViewById(R.id.login_edit_mobile), 
					null, true, true);
			bindControl(R.id.login_edit_vericode,
					viewLogin.findViewById(R.id.login_edit_vericode), 
					null, true, true);	
			bindControl(R.id.login_btn_vericode,
					viewLogin.findViewById(R.id.login_btn_vericode), 
					this, true, false);
			bindControl(R.id.login_btn_mobile_login,
					viewLogin.findViewById(R.id.login_btn_mobile_login), 
					this, true, false);
			bindControl(R.id.login_btn_account,
					viewLogin.findViewById(R.id.login_btn_account),
					this, true, true);
			
			//凯立德账号登录
			bindControl(R.id.login_framelayout_account,
					viewLogin.findViewById(R.id.login_framelayout_account),
					null, false, true);
			bindControl(R.id.login_text_title_account,
					viewLogin.findViewById(R.id.login_text_title_account),
					null, true, false);
			bindControl(R.id.login_edit_account,
					viewLogin.findViewById(R.id.login_edit_account), 
					null, true, true);
			bindControl(R.id.login_edit_password,
					viewLogin.findViewById(R.id.login_edit_password),
					null, true, true);
			bindControl(R.id.login_btn_lostpwd,
					viewLogin.findViewById(R.id.login_btn_lostpwd),
					this, true, true);
			bindControl(R.id.login_btn_account_login,
					viewLogin.findViewById(R.id.login_btn_account_login),
					this, true, false);
			bindControl(R.id.login_btn_mobile,
					viewLogin.findViewById(R.id.login_btn_mobile), 
					this, true, true);
			
			//二维码
			bindControl(R.id.login_layout_right,
					viewLogin.findViewById(R.id.login_layout_right), 
					null, true, false);
			bindControl(R.id.login_layout_login_name,
					viewLogin.findViewById(R.id.login_layout_login_name), 
					null, true, true);
			bindControl(R.id.login_image_qrcode_failed,
					viewLogin.findViewById(R.id.login_image_qrcode_failed),
					this, false, true);
			bindControl(R.id.login_image_qrcode_scaning,
					viewLogin.findViewById(R.id.login_image_qrcode_scaning),
					null, true, true);
			bindControl(R.id.login_image_qrcode,
					viewLogin.findViewById(R.id.login_image_qrcode),
					null, false, true);
			
			initControl();
		}
		return viewLogin;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (getArguments() != null) {
			if ("main".equals(getArguments().getString("activity"))) {
				RelativeLayout layout = (RelativeLayout) getControl(R.id.login_layout_left);
				if (layout != null) {
					RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout
							.getLayoutParams();
					params.leftMargin = 72;
					layout.setLayoutParams(params);
				}
			}
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		
		Log.d(TAG, " onHiddenChanged " + hidden);
		if (hidden) {
			// 不在最前端界面显示
		} else {
			// 重新显示到最前端中
			if (getControl(R.id.login_framelayout_mobile).
					getVisibility() == View.VISIBLE) {
				setFocusTitle(R.id.login_text_title_mobile);
			} else if (getControl(R.id.login_framelayout_account).
					getVisibility() == View.VISIBLE) {
				setFocusTitle(R.id.login_text_title_account);
			}
		}
	}
	
	private void setFocusTitle(int id)
	{
		//让TextView获取焦点，防止EditText自动获取焦点弹出软键盘
		TextView textTitle = (TextView) getControl(id);
		textTitle.setFocusable(true);
		textTitle.setFocusableInTouchMode(true);
		textTitle.requestFocus();
		textTitle.requestFocusFromTouch();			
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public boolean onBackPressed() {
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login_btn_mobile: //切换到手机登录
		{
			mIsUsingAccount = false;
			setFocusTitle(R.id.login_text_title_mobile);
			// 登录按钮
			EditText editMobile = (EditText) getControl(R.id.login_edit_mobile);
			EditText editVericode = (EditText) getControl(R.id.login_edit_vericode);
			if (editMobile != null && editVericode != null) {
				Button btnLogin = (Button) getControl(R.id.login_btn_mobile_login);

				if (!TextUtils.isEmpty(editMobile.getText().toString())
						&& !TextUtils.isEmpty(editVericode.getText().toString())) {
					if (btnLogin != null) {
						btnLogin.setEnabled(true);
					}
				} else {
					if (btnLogin != null) {
						btnLogin.setEnabled(false);
					}
				}
			}

			KCloudController.setVisibleById(R.id.login_text_title_mobile, 
					true, mWidgetList);
			KCloudController.setVisibleById(R.id.login_btn_account, 
					true, mWidgetList);
			KCloudController.setVisibleById(R.id.login_framelayout_mobile, 
					true, mWidgetList);
			
			KCloudController.setVisibleById(R.id.login_text_title_account,
					false, mWidgetList);
			KCloudController.setVisibleById(R.id.login_btn_mobile, 
					false, mWidgetList);
			KCloudController.setVisibleById(R.id.login_framelayout_account,
					false, mWidgetList);
			break;
		}

		case R.id.login_btn_account: //切换到凯立德账号登录
		{
			mIsUsingAccount = true;
			setFocusTitle(R.id.login_text_title_account);
			Button btnLogin = ((Button) getControl(R.id.login_btn_account_login));
			if (btnLogin != null) {
				btnLogin.setEnabled(true);
			}

			KCloudController.setVisibleById(R.id.login_text_title_account,
					true, mWidgetList);
			KCloudController.setVisibleById(R.id.login_btn_mobile, 
					true, mWidgetList);
			KCloudController.setVisibleById(R.id.login_framelayout_account, 
					true, mWidgetList);
			
			KCloudController.setVisibleById(R.id.login_text_title_mobile, 
					false, mWidgetList);
			KCloudController.setVisibleById(R.id.login_btn_account, 
					false, mWidgetList);
			KCloudController.setVisibleById(R.id.login_framelayout_mobile, 
					false, mWidgetList);
			break;
		}

		case R.id.login_btn_vericode: //获取验证码
		{
			EditText editMobile = (EditText) getControl(R.id.login_edit_mobile);
			if (editMobile != null) {
				String strMobile = editMobile.getText().toString();

				if (!CldKConfigAPI.getInstance().isPhoneNum(strMobile)) {
					// 提示手机号非法: 请输入有效的手机号
					KCloudCommonUtil.makeText(R.string.kaccount_login_mobile_err);
					return;
				}
				if (!CldPhoneNet.isNetConnected()) {
					KCloudCommonUtil.makeText(R.string.common_network_abnormal);
					return;
				}

				mCodeTime = GET_CODE_TIMEOUT;
				KCloudController.setEnabledById(R.id.login_btn_vericode, 
						false, mWidgetList);
				startCodeTask();

				CldKAccountAPI.getInstance().getMobileVeriCode(strMobile,
						CldBussinessCode.FAST_LOGIN);
			}
			break;
		}

		case R.id.login_btn_lostpwd: {
			KCloudUser.getInstance().sendMessage(CLDMessageId.MSG_ID_LOGIN_LOST_PWD, 0);
			break;
		}
		
		case R.id.login_btn_mobile_login: //手机登录
		{
			EditText editMobile = (EditText) getControl(R.id.login_edit_mobile);
			EditText editVericode = (EditText) getControl(R.id.login_edit_vericode);

			// 手机登录
			if (!CldPhoneNet.isNetConnected()) {
				KCloudCommonUtil.makeText(R.string.common_network_abnormal);
				break;
			}

			if (null != editMobile && null != editVericode) {
				String strMobile = editMobile.getText().toString();
				String strVeriCode = editVericode.getText().toString();

				if (!TextUtils.isEmpty(strMobile)
						&& !TextUtils.isEmpty(strVeriCode)) {
					if (!CldKConfigAPI.getInstance().isPhoneNum(strMobile)) {
						// 提示手机号非法
						KCloudCommonUtil.makeText(R.string.kaccount_login_mobile_err);
						break;
					} else {
						CldProgress.showProgress(mContext,
								new CldProgressListener() {
									public void onCancel() {
									}
								});
						// 设置登录状态
						KCloudShareUtils.put("login_status", 1);
						CldKAccountAPI.getInstance().fastLogin(strMobile, strVeriCode);
					}
				}
			}
			break;
		}

		case R.id.login_btn_account_login: //凯立德账号登录
		{
			EditText editAccount = (EditText) getControl(R.id.login_edit_account);
			EditText editPassword = (EditText) getControl(R.id.login_edit_password);

			if (null != editAccount && null != editPassword) {
				String strAccount = editAccount.getText().toString()
						.replaceAll(" ", "");
				String strPassword = editPassword.getText().toString()
						.replaceAll(" ", "");

				InputError errorCode = KCloudUserUtils.checkInputIsValid(
						strAccount, strPassword);
				switch (errorCode) {
				case eERROR_ACCOUNT_EMPTY:
					KCloudCommonUtil.makeText(R.string.kaccount_account_empty);
					return;
				case eERROR_PASSWORD_EMPTY:
					KCloudCommonUtil.makeText(R.string.kaccount_pasword_empty);
					return;
				case eERROR_ACCOUNT_INPUT:
					KCloudCommonUtil.makeText(R.string.kaccount_account_error);
					return;
				case eERROR_PASSWORD_INPUT:
					KCloudCommonUtil.makeText(R.string.kaccount_password_error);
					return;
				case eERROR_EMAIL_INPUT:
					KCloudCommonUtil.makeText(R.string.kaccount_email_error);
					return;
				case eERROR_NONE:
					CldProgress.showProgress(mContext, 
							KCloudCommonUtil.getString(R.string.login_text_doing),
							new CldProgressListener() {
								public void onCancel() {
								}
							});
					// 设置登录状态
					KCloudShareUtils.put("login_status", 1);
					CldKAccountAPI.getInstance().login(strAccount, strPassword);
					CldLog.i(TAG, "strAccount = " + strAccount 
							+ "strPassword = " + strPassword);
					return;
				default:
					break;
				}
			}
			break;
		}

		case R.id.login_image_qrcode_failed: {
			if (CldPhoneNet.isNetConnected()) {
				KCloudController.setVisibleById(R.id.login_image_qrcode_scaning, 
						true, mWidgetList);
				KCloudController.setVisibleById(R.id.login_image_qrcode_failed,
						false, mWidgetList);
				mQRCodeTime = GET_QRCODE_TIMEOUT;
				CldKAccountAPI.getInstance().getQRcode(0);
			} else {
				KCloudCommonUtil.makeText(R.string.common_network_abnormal);
			}
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
	public void initControl() {
		setFocusTitle(R.id.login_text_title_mobile);
		TextView name = (TextView) getControl(R.id.login_layout_login_name);
		String string = KCloudCommonUtil.getString(R.string.login_text_scan);
		name.setText(Html.fromHtml(string));
		
		final EditText editMobile = (EditText) getControl(R.id.login_edit_mobile);
		final EditText editVeriCode = (EditText) getControl(R.id.login_edit_vericode);
		final EditText editAccount = (EditText) getControl(R.id.login_edit_account);
		final EditText editPassword = (EditText) getControl(R.id.login_edit_password);
		
		if (editMobile != null) {
			editMobile.setInputType(InputType.TYPE_NULL);  
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
					if (mIsUsingAccount) {
						return ;
					}
					
					if (editMobile != null) {
						Button btnVeriCode = (Button) getControl(R.id.login_btn_vericode);
						if (btnVeriCode != null) {
							if (!TextUtils.isEmpty(editMobile.getText().toString())
									&& editMobile.getText().toString().length() == 11) {
								btnVeriCode.setEnabled(true);
							} else {
								btnVeriCode.setEnabled(false);
							}
						}
					}

					if (editVeriCode != null) {
						Button btnLogin = (Button) getControl(R.id.login_btn_mobile_login);
						if (btnLogin != null) {
							if (!TextUtils.isEmpty(editMobile.getText().toString())
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
					if (hasFocus) {
						String title = KCloudCommonUtil.getString(
								R.string.input_dialog_title_mobile);
						String hint = KCloudCommonUtil.getString(
								R.string.input_dialog_hint_mobile);

						CldInputDialog.showInputDialog(mContext, title,
								hint, editMobile.getText().toString(),
								CldInputDialog.CldInputType.eInputType_Mobil,
								CldInputDialog.CldButtonType.eButton_Confirm,
								new CldInputDialog.CldInputDialogListener() {

									@Override
									public void onOk(String strInput) {
										editMobile.setText(strInput);
										editMobile.setSelection(strInput.length());
										
										setFocusTitle(R.id.login_text_title_mobile);
									}

									@Override
									public void onCancel() {
										setFocusTitle(R.id.login_text_title_mobile);
									}
								});
					}
				}
			});
			
			editMobile.setText(KCloudShareUtils.getString("bindMobile"));
		}

		if (editVeriCode != null) {
			editVeriCode.setInputType(InputType.TYPE_NULL);  
			editVeriCode.addTextChangedListener(new TextWatcher() {

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
					if (mIsUsingAccount) {
						return ;
					}
					if (editVeriCode != null) {
						Button btnLogin = (Button) getControl(R.id.login_btn_mobile_login);

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
					if (hasFocus) {
						String title = KCloudCommonUtil.getString(
								R.string.input_dialog_title_vericode);
						String hint = KCloudCommonUtil.getString(
								R.string.input_dialog_hint_vericode);

						CldInputDialog.showInputDialog(
								mContext,
								title,
								hint,
								editVeriCode.getText().toString(),
								CldInputDialog.CldInputType.eInputType_Vericode,
								CldInputDialog.CldButtonType.eButton_Confirm,
								new CldInputDialog.CldInputDialogListener() {

									@Override
									public void onOk(String strInput) {
										editVeriCode.setText(strInput);
										editVeriCode.setSelection(strInput.length());
										
										setFocusTitle(R.id.login_text_title_mobile);
									}

									@Override
									public void onCancel() {
										setFocusTitle(R.id.login_text_title_mobile);
									}
						      });
					}
				}
			});
		}
		
		if (editAccount != null) {
			editAccount.setInputType(InputType.TYPE_NULL);  
			editAccount.setText(CldKAccountAPI.getInstance().getLoginName());
			editAccount.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						String title = KCloudCommonUtil.getString(
								R.string.input_dialog_title_account);
						String hint = KCloudCommonUtil.getString(
								R.string.input_dialog_hint_account);

						CldInputDialog.showInputDialog(mContext, title, hint,
								editAccount.getText().toString(),
								CldInputDialog.CldInputType.eInputType_Account,
								CldInputDialog.CldButtonType.eButton_Confirm,
								new CldInputDialog.CldInputDialogListener() {

									@Override
									public void onOk(String strInput) {
										editAccount.setText(strInput);
										editAccount.setSelection(strInput.length());
										
										setFocusTitle(R.id.login_text_title_account);
									}

									@Override
									public void onCancel() {
										setFocusTitle(R.id.login_text_title_account);
									}
								});
					}
				}
			});
		}
		
		if (editPassword != null) {
			//设置InputType.TYPE_NULL后，设置的密码类型无效果了
			//editPassword.setInputType(InputType.TYPE_NULL);  
			editPassword.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						String title = KCloudCommonUtil.getString(
								R.string.input_dialog_title_password);
						String hint = KCloudCommonUtil.getString(
								R.string.input_dialog_hint_password);

						CldInputDialog.showInputDialog(mContext, title, hint,
								editPassword.getText().toString(),
								CldInputDialog.CldInputType.eInputType_Password,
								CldInputDialog.CldButtonType.eButton_Confirm,
								new CldInputDialog.CldInputDialogListener() {

									@Override
									public void onOk(String strInput) {
										editPassword.setText(strInput);
										editPassword.setSelection(strInput.length());
										
										setFocusTitle(R.id.login_text_title_account);
									}

									@Override
									public void onCancel() {
										setFocusTitle(R.id.login_text_title_account);
									}
								});
					}
				}
			});
		}

		Button btn_mobile = (Button) getControl(R.id.login_btn_mobile);
		if (btn_mobile != null) {
			btn_mobile.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
			btn_mobile.getPaint().setAntiAlias(true);//抗锯齿
		}
		
		Button btn_account = (Button) getControl(R.id.login_btn_account);
		if (btn_account != null) {
			btn_account.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
			btn_account.getPaint().setAntiAlias(true);//抗锯齿
		}

		// 获取二维码登录信息
		mQRCodeTime = GET_QRCODE_TIMEOUT;
		CldKAccountAPI.getInstance().getQRcode(0);
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
		Button btn = (Button) getControl(R.id.login_btn_vericode);
		cancelCodeTask();
		mCodeTime = GET_CODE_TIMEOUT;
		if (btn != null) {
			btn.setEnabled(true);
			btn.setTextColor(KCloudCommonUtil.getColor(R.color.text_normal_color));
			btn.setText(KCloudCommonUtil.getString(R.string.vericode_resend));
		}
	}

	public void onHandleMessage(Message message) {
		if (getActivity() == null)
			return;
		
		switch (message.what) {
		case CLDMessageId.MSG_ID_LOGIN_GET_VERICODE_SUCCESS: {
			// 获取验证码成功
			KCloudCommonUtil.makeText(R.string.kaccount_get_vericode_success);
			break;
		}

		case CLDMessageId.MSG_ID_LOGIN_GET_VERICODE_FAILED: {
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

		case CLDMessageId.MSG_ID_LOGIN_GET_QRCODE_SUCCESS: {
			//获取二维码成功
			String Head = "";
			String Tag = "cldqr://f=l&p=";
			String QRText = CldKAccountAPI.getInstance().getQRcodeValue();
			
			if (KCloudAppUtils.isTestVersion()) {
				Head = "http://test.careland.com.cn/kz/web/webapp/user/login.php?f=cldqr&p=";
			} else {
				Head = "http://mpage.careland.com.cn/user/login.php?f=cldqr&p=";
			}
						
			if (!TextUtils.isEmpty(QRText)) {
				String url = Head + QRText.substring(Tag.length());				
				CldLog.i(TAG, "QRText = " + url);
				ImageView imageView = (ImageView) getControl(R.id.login_image_qrcode);
				if (imageView != null) {
					Bitmap bmpQR = null;
					try {
						bmpQR = CldQRCode.createQRCode(
										url,
										imageView.getWidth() > 0 ? imageView
												.getWidth() : 128, 5);
					} catch (WriterException e) {
						e.printStackTrace();
					}
					imageView.setImageBitmap(bmpQR);
				}
			}
			// 不同终端同步登陆状态
			CldKAccountAPI.getInstance().getLoginStatusByQRcode(
					CldDalKAccount.getInstance().getGuid());
			
			KCloudController.setVisibleById(R.id.login_image_qrcode_failed,
					false, mWidgetList);
			KCloudController.setVisibleById(R.id.login_image_qrcode_scaning,
					false, mWidgetList);
			KCloudController.setVisibleById(R.id.login_image_qrcode, 
					true, mWidgetList);
			break;
		}
		case CLDMessageId.MSG_ID_LOGIN_GET_QRCODE_FAILED: {
			
			// 获取二维码登录信息
			if (mQRCodeTime-- <= 0) {
				
				KCloudController.setVisibleById(R.id.login_image_qrcode,
						false, mWidgetList);
				KCloudController.setVisibleById(R.id.login_image_qrcode_scaning, 
						false, mWidgetList);
				KCloudController.setVisibleById(R.id.login_image_qrcode_failed,
						true, mWidgetList);
			} else {
				KCloudController.setVisibleById(R.id.login_image_qrcode_failed,
						false, mWidgetList);
				KCloudController.setVisibleById(R.id.login_image_qrcode_scaning,
						true, mWidgetList);
				CldKAccountAPI.getInstance().getQRcode(0);
			}
			break;
		}

		case CLDMessageId.MSG_ID_LOGIN_MOBILE_LOGIN_SUCCESS:
		case CLDMessageId.MSG_ID_LOGIN_ACCOUNT_LOGIN_SUCCESS: {
			break;
		}
		
		case CLDMessageId.MSG_ID_USERINFO_GETDETAIL_SUCCESS:
		case CLDMessageId.MSG_ID_USERINFO_GETDETAIL_FAILED: {
			if (CldProgress.isShowProgress()) {
				CldProgress.cancelProgress();
			}
			cancelCodeTask();
			getActivity().finish();
			// 进入用户信息界面
			Intent intent = new Intent(mContext, KCloudUserInfoActivity.class);
			startActivity(intent);
			getActivity().overridePendingTransition(android.R.anim.fade_in,
					android.R.anim.fade_out);
			break;
		}
		
		case CLDMessageId.MSG_ID_LOGIN_MOBILE_LOGIN_FAILED: {
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
				KCloudCommonUtil.makeText(R.string.kaccount_vericode_has_err);
				break;
			case 909:
				KCloudCommonUtil.makeText(R.string.kaccount_vericode_has_used);
				break;
			default:
				KCloudCommonUtil.makeText(R.string.kaccount_login_failed);
				break;
			}
			break;
		}

		case CLDMessageId.MSG_ID_LOGIN_ACCOUNT_LOGIN_FAILED: {
			if (CldProgress.isShowProgress()) {
				CldProgress.cancelProgress();
			}
			cancelCodeTask();

			if (null == message.obj) {
				break;
			}
			int errCode = (Integer) message.obj;
			switch (errCode) {
			case -1:
			case -2:
			case -105:
				KCloudCommonUtil.makeText(R.string.common_network_abnormal);
				break;
			case 102:
				KCloudCommonUtil.makeText(R.string.kaccount_login_user_err);
				break;
			case 104:
				KCloudCommonUtil.makeText(R.string.kaccount_login_userpwd_err);
				break;
			default:
				KCloudCommonUtil.makeText(R.string.kaccount_login_failed);
				break;
			}
			break;
		}

		default:
			break;
		}
	}
}