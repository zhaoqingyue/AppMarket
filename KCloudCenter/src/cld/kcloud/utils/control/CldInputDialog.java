package cld.kcloud.utils.control;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.cld.log.CldLog;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.method.ReplacementTransformationMethod;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import cld.kcloud.center.R;
import cld.kcloud.utils.KCloudCommonUtil;
import cld.kcloud.utils.KCloudUserUtils;

public class CldInputDialog extends Dialog {
	public enum CldInputType {
		eInputType_None,
		eInputType_Mobil, 
		eInputType_Vericode, 
		eInputType_NickName, 
		eInputType_CarPlate, 
		eInputType_CarBody, 
		eInputType_CarEngine, 
		eInputType_ModifyMobil, 
		eInputType_Account, 
		eInputType_Password, 
		eInputType_ModifyPassword
	};

	public enum CldButtonType {
		eButton_None, 
		eButton_Confirm, 
		eButton_Save
	};

	public interface CldInputDialogListener {
		public void onOk(String strInput);
		public void onCancel();
	}

	private Button m_btnOk = null;
	private Button m_btnCancel = null;
	private TextView m_tv = null;
	private EditText m_et = null;
	private ImageView m_iv = null;
	private ImageView m_pwd = null;
	private ImageView m_clear = null;
	private Context mContext = null;
    static  boolean displayPwdFlag = false;  
	private CldInputDialogListener mListener = null;
	private static CldInputDialog mInputDialog = null;	

	public CldInputDialog(Context context) {
		super(context);
	}

	public CldInputDialog(Context context, int theme) {
		super(context, theme);
	}

	/**
	 * 
	 * @param context
	 * @param title
	 * @param hint
	 * @return
	 */
	public static CldInputDialog showInputDialog(Context context, String title,
			String hint, String input, CldInputDialogListener listener) {
		return createDialog(context, title, hint, input,
				CldInputType.eInputType_None, CldButtonType.eButton_None, listener);
	}

	/**
	 * 
	 * @param context
	 * @param title
	 * @param hint
	 * @param type
	 * @return
	 */
	public static CldInputDialog showInputDialog(Context context, String title,
			String hint, String input, CldInputType inputType,
			CldInputDialogListener listener) {
		return createDialog(context, title, hint, input, inputType,
				CldButtonType.eButton_None, listener);
	}

	/**
	 * 
	 * @param context
	 * @param title
	 * @param hint
	 * @param btnType
	 * @param listener
	 * @return
	 */
	public static CldInputDialog showInputDialog(Context context, String title,
			String hint, String input, CldButtonType btnType,
			CldInputDialogListener listener) {
		return createDialog(context, title, hint, input,
				CldInputType.eInputType_None, btnType, listener);
	}

	/**
	 * 
	 * @param context
	 * @param title
	 * @param hint
	 * @param inputType
	 * @param btnType
	 * @param listener
	 * @return
	 */
	public static CldInputDialog showInputDialog(Context context, String title,
			String hint, String input, CldInputType inputType, CldButtonType btnType,
			CldInputDialogListener listener) {
		return createDialog(context, title, hint, input, inputType, btnType,
				listener);
	}

	/**
	 * 取消等待进度条弹出框
	 * 
	 * @return void
	 */
	public static void cancelInputDialog() {
		if (mInputDialog != null) {
			mInputDialog.m_btnOk = null;
			mInputDialog.m_btnCancel = null;
			mInputDialog.m_tv = null;
			mInputDialog.m_et = null;
			mInputDialog.mListener = null;
			mInputDialog.mContext = null;
			mInputDialog = null;
		}
	}
	
	/**
	 * 是否显示
	 * 
	 * @return boolean
	 */
	public static boolean isShowInputDialog() {
		return (mInputDialog != null && mInputDialog.isShowing());
	}

	private static CldInputDialog createDialog(Context context, String title,
			final String hint, String input, final CldInputType inputType, final CldButtonType btnType,
			CldInputDialogListener listener) {
		
		//解决快速点击多次，导致创建多个CldInputDialog
		if (isShowInputDialog())
			return null;

		cancelInputDialog();
		if (KCloudCommonUtil.getString(R.string.setting_unset).equals(input)) {
			input = "";
		}		
		
		if (context != null) {
			if (mInputDialog == null) {
				mInputDialog = new CldInputDialog(context, R.style.dialog);
				mInputDialog.setContentView(R.layout.layout_input_dialog);
				mInputDialog.mListener = listener;
				mInputDialog.mContext = context;
				
				WindowManager.LayoutParams lp = mInputDialog.getWindow().getAttributes();
				lp.y = -55;
				lp.dimAmount = 0.85f;
				lp.gravity = Gravity.CENTER;
				mInputDialog.getWindow().setAttributes(lp);
				mInputDialog.setCanceledOnTouchOutside(false);

				mInputDialog.m_tv = (TextView) mInputDialog
						.findViewById(R.id.input_dialog_text_title);
				if (mInputDialog.m_tv != null && !TextUtils.isEmpty(title)) {
					mInputDialog.m_tv.setText(title);
				}
				
				mInputDialog.m_iv = (ImageView) mInputDialog.findViewById(R.id.input_dialog_edit_image);
				if (mInputDialog.m_iv != null) {
					switch (inputType) {
					case eInputType_Mobil:
					case eInputType_ModifyMobil:
						mInputDialog.m_iv.setImageResource(R.drawable.img_mobile);
						mInputDialog.m_iv.setVisibility(View.VISIBLE);
						break;
						
					case eInputType_Vericode:
						mInputDialog.m_iv.setImageResource(R.drawable.img_vericode);
						mInputDialog.m_iv.setVisibility(View.VISIBLE);
						break;
						
					case eInputType_Account:
						mInputDialog.m_iv.setImageResource(R.drawable.img_account);
						mInputDialog.m_iv.setVisibility(View.VISIBLE);
						break;
						
					case eInputType_Password:
						mInputDialog.m_iv.setImageResource(R.drawable.img_password);
						mInputDialog.m_iv.setVisibility(View.VISIBLE);
						
						//默认情况下， 密码不可见
						mInputDialog.m_pwd = (ImageView) mInputDialog.findViewById(R.id.input_dialog_edit_pwd);
						mInputDialog.m_pwd.setImageResource(R.drawable.img_pwd_invisible);
						mInputDialog.m_pwd.setVisibility(View.VISIBLE);
						displayPwdFlag = false;
						
						if (mInputDialog.m_pwd != null) {
							mInputDialog.m_pwd.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									displayPwdFlag = !displayPwdFlag;  
									if (displayPwdFlag) {
										mInputDialog.m_pwd.setImageResource(R.drawable.img_pwd_visible);
										mInputDialog.m_et.setTransformationMethod(HideReturnsTransformationMethod.getInstance());  
									} else {
										mInputDialog.m_pwd.setImageResource(R.drawable.img_pwd_invisible);
										mInputDialog.m_et.setTransformationMethod(PasswordTransformationMethod.getInstance());   
									}
									mInputDialog.m_et.postInvalidate();  
									mInputDialog.m_et.setSelection(mInputDialog.m_et.length());
								}
							});
						}
						break;
						
					default:
						break;
					}
				}

				mInputDialog.m_et = (EditText) mInputDialog
						.findViewById(R.id.input_dialog_edit);
				if (mInputDialog.m_et != null) {
					
					// 调整位置
					if (mInputDialog.m_iv != null && mInputDialog.m_iv.getVisibility() == View.VISIBLE) {
						int right = mInputDialog.m_et.getPaddingRight();
						int top = mInputDialog.m_et.getPaddingTop();
						int bottom = mInputDialog.m_et.getPaddingBottom();
						mInputDialog.m_et.setPadding(0, top, right, bottom);
					}
					
					switch (inputType) {
					case eInputType_Mobil:
					case eInputType_ModifyMobil:
						mInputDialog.m_et.setInputType(InputType.TYPE_CLASS_NUMBER);
						mInputDialog.m_et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
						break;
						
					case eInputType_Vericode:
						mInputDialog.m_et.setInputType(InputType.TYPE_CLASS_NUMBER);
						mInputDialog.m_et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});						
						break;
						
					case eInputType_NickName:
						mInputDialog.m_et.setInputType(InputType.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_NORMAL);  
						break;
						
					case eInputType_Account:
						mInputDialog.m_et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(21)});
						mInputDialog.m_et.setInputType(InputType.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_NORMAL);  
						break;
						
					case eInputType_Password:
						mInputDialog.m_et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
						mInputDialog.m_et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(14)});
						break;
						
					case eInputType_CarPlate:	
						mInputDialog.m_et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
						mInputDialog.m_et.setTransformationMethod(new CldInputDialog.AllCapTransformationMethod());
						break;
						
					case eInputType_CarBody:
					case eInputType_CarEngine:
						mInputDialog.m_et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
						mInputDialog.m_et.setTransformationMethod(new CldInputDialog.AllCapTransformationMethod());
						break;
						
					default:
						break;
					}
					
					//一键清除
					mInputDialog.m_clear = (ImageView) mInputDialog.findViewById(R.id.input_dialog_edit_clear);
					if (mInputDialog.m_clear != null) {
						mInputDialog.m_clear.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if (mInputDialog.m_et != null) {
									mInputDialog.m_et.setText("");
									mInputDialog.m_et.setSelection(0);
								}
							}
						});
					}

					if (inputType != CldInputType.eInputType_None) {
						mInputDialog.m_et.addTextChangedListener(new TextWatcher() {

							@Override
							public void afterTextChanged(Editable arg0) {
							}

							@Override
							public void beforeTextChanged(CharSequence arg0,
									int arg1, int arg2, int arg3) {
							}

							@Override
							public void onTextChanged(CharSequence s, int start, int before, int count) {
								switch (inputType) {
								case eInputType_Password: {
									if (mInputDialog.m_et.isEnabled()) {
										String strNew = mInputDialog.m_et.getText().toString();
										String regEx = "[^a-zA-Z0-9]";
										Pattern p = Pattern.compile(regEx);
										Matcher m = p.matcher(strNew);
										String str = m.replaceAll(" ").trim();
										if (!strNew.equals(str)) {
											mInputDialog.m_et.setText(str);
											mInputDialog.m_et.setSelection(str.length()); // 设置新的光标位置
										}
									}
									break;
								}
								
								case eInputType_CarPlate:
								case eInputType_CarBody:
								case eInputType_CarEngine: {
									if (mInputDialog.m_et.isEnabled() && 
											mInputDialog.m_et.length() > 0) {
										/**
										 * add 2016-7-29
										 * 添加" mInputDialog.m_et.length() > 0 "这个条件，
										 * 为了解决 点击“清空”按钮后， 弹出toast提示：请输入字母或数字
										 */
										String regx = "^[a-zA-Z0-9]+$";
										Pattern pattern = Pattern.compile(regx);
										Matcher matcher = pattern.matcher(s);
										if (!matcher.matches()) {
											String str = KCloudCommonUtil.getString(
													R.string.toast_enter_letters_or_numbers);
											KCloudCommonUtil.makeText(str);
											if (!TextUtils.isEmpty(s)) {
												mInputDialog.m_et.setText(s.subSequence(0, start));
												mInputDialog.m_et.setSelection(start);
											}
										}
									}
									break;
								}
								default:
									break;
								}
								
								//m_et的length大于0则显示清楚按钮，否则隐藏
								if (mInputDialog.m_et.isEnabled()) {
									if (mInputDialog.m_et.length() > 0) {
										mInputDialog.m_clear.setVisibility(View.VISIBLE);
									} else {
										mInputDialog.m_clear.setVisibility(View.INVISIBLE);
									}
								}
							}
						});
					}
					
					if (!TextUtils.isEmpty(hint)) {
						mInputDialog.m_et.setHint(hint);
					}
					
					if (!TextUtils.isEmpty(input)) {
						mInputDialog.m_et.setText(input);
						mInputDialog.m_et.setSelection(input.length());
					}
				}

				mInputDialog.m_btnOk = (Button) mInputDialog
						.findViewById(R.id.input_dialog_btn_save);
				if (mInputDialog.m_btnOk != null && btnType != CldButtonType.eButton_None) {
					mInputDialog.m_btnOk.setVisibility(View.VISIBLE);

					switch (btnType) {
					case eButton_Confirm:
						mInputDialog.m_btnOk.setText(
								KCloudCommonUtil.getString(R.string.input_dialog_confirm));
						break;

					case eButton_Save:
						mInputDialog.m_btnOk.setText(
								KCloudCommonUtil.getString(R.string.input_dialog_save));
						break;

					default:
						break;
					}

					mInputDialog.m_btnOk.setOnClickListener(new View.OnClickListener() {

						@SuppressLint("DefaultLocale") 
						@Override
						public void onClick(View v) {

							if (mInputDialog.mListener != null) {
								
								String str = mInputDialog.m_et.getText().toString();
								switch (inputType){
								case eInputType_CarPlate:
								case eInputType_CarBody:
								case eInputType_CarEngine:{
									str = str.toUpperCase();
									break;
								}
								default:
									break;
								}
								
								KCloudUserUtils.hideInputMethod(mInputDialog.mContext, mInputDialog.m_et);
								CldLog.i("TAG", "    getText: " + str);
								mInputDialog.mListener.onOk(str);
							}
							mInputDialog.dismiss();
						}
					});
				}
				
				mInputDialog.m_btnCancel = (Button) mInputDialog.findViewById(R.id.input_dialog_btn_cancel);
				if (mInputDialog.m_btnCancel != null) {
					mInputDialog.m_btnCancel.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {

							if (mInputDialog.mListener != null) {
								KCloudUserUtils.hideInputMethod(mInputDialog.mContext, mInputDialog.m_et);
								mInputDialog.mListener.onCancel();
							}
							mInputDialog.dismiss();
						}
					});
				}
				mInputDialog.show();
			}
		}
		return mInputDialog;
	}
	
	/**
	 * 将EditText中的小写字母转化为大写字母
	 */
	public static class AllCapTransformationMethod extends ReplacementTransformationMethod {

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
}