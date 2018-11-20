package cld.kcloud.utils.control;

import cld.kcloud.center.R;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 提示对话框
 * 
 */
public class CldPromptDialog extends Dialog implements OnClickListener {

	private TextView tv_title;
	private LinearLayout lay_content;
	private Button btn_cancel;
	private Button btn_sure;
	private View v_btn_divider;
	private PromptDialogListener listener;

	private CharSequence titile;
	private CharSequence content;
	private String sureBtnName;
	private String cancelBtnName;
	private boolean contentBold;

	/** 是否接收了onkeyDown事件，接收了onkeydown之后才会处理onkeyup事件，否则向上发送 */
	private boolean hasOnKeyDown;

	private static CldPromptDialog instance;

	/** 确认按钮默认在右边 */
	private boolean isSubmitAlignRight = true;

	/**
	 * 空字符串的资源id
	 */
	public static final int RES_NONE_STRING = -1;

	public interface PromptDialogListener {
		public void onCancel();
		public void onSure();
	}

	/**
	 * @param context
	 * @param title 标题名称
	 * @param content 内容文字
	 * @param sureBtnName 确定按钮名称
	 * @param cancelBtnName 取消按钮名称
	 * @param listener 处理结果回调监听器
	 */
	public static void createPromptDialog(Context context, CharSequence title,
			CharSequence content, String sureBtnName, String cancelBtnName,
			PromptDialogListener listener) {
		new CldPromptDialog(context, title, content, false, sureBtnName,
				cancelBtnName, listener);
	}

	/**
	 * @param context
	 * @param title 标题名称
	 * @param content 内容文字
	 * @param sureBtnName 确定按钮名称
	 * @param cancelBtnName 取消按钮名称
	 * @param listener 处理结果回调监听器
	 */
	public static void createPromptDialog(Context context, CharSequence title,
			CharSequence content, String sureBtnName, String cancelBtnName,
			boolean isSubmitAlignRight, PromptDialogListener listener) {
		new CldPromptDialog(context, title, content, false, sureBtnName,
				cancelBtnName, isSubmitAlignRight, listener);
	}

	/**
	 * @param context
	 * @param title 标题名称
	 * @param content 内容文字
	 * @param contentBold 内容字体是否加粗
	 * @param sureBtnName 确定按钮名称
	 * @param cancelBtnName 取消按钮名称
	 * @param listener 处理结果回调监听器
	 */
	public static void createPromptDialog(Context context, CharSequence title,
			CharSequence content, boolean contentBold, String sureBtnName,
			String cancelBtnName, PromptDialogListener listener) {
		new CldPromptDialog(context, title, content, contentBold, sureBtnName,
				cancelBtnName, listener);
	}

	/**
	 * 是否正在显示
	 * @return void
	 * @date 2015年8月18日 下午2:49:21
	 */
	public static boolean isShow() {
		if (instance == null) {
			return false;
		}
		return instance.isShowing();
	}

	/**
	 * 取消对话框显示
	 * @return void
	 * @date 2015年8月13日 下午6:32:59
	 */
	public static void canclePromptDialog() {
		if (instance != null) {
			if (isShow()) {
				if (null != instance.listener) {
					instance.listener.onCancel();
				}
				instance.dismiss();
				instance = null;
			}
		}
	}

	/**
	 * @param context
	 * @param title 标题名称
	 * @param content 内容文字
	 * @param sureBtnName 确定按钮名称
	 * @param cancelBtnName 取消按钮名称
	 * @param listener 处理结果回调监听器
	 */
	public static void createPromptDialog(Context context, int title,
			int content, int sureBtnName, int cancelBtnName,
			PromptDialogListener listener) {
		new CldPromptDialog(context, getString(context, title), getString(
				context, content), false, getString(context, sureBtnName),
				getString(context, cancelBtnName), listener);
	}

	/**
	 * 根据资源id获取字符串，id为RES_NONE_STRING，或者找不到资源时 返回null
	 * @param context
	 * @param res
	 * @return String
	 * @date 2015年7月2日 下午7:55:50
	 */
	private static String getString(Context context, int res) {
		if (res == RES_NONE_STRING)
			return null;
		try {
			return context.getResources().getString(res);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @param context
	 * @param title 标题名称
	 * @param content 内容文字
	 * @param sureBtnName 确定按钮名称
	 * @param cancelBtnName 取消按钮名称
	 * @param contentBold 内容字体是否加粗
	 * @param listener 处理结果回调监听器
	 */
	public CldPromptDialog(Context context, CharSequence title,
			CharSequence content, boolean contentBold, String sureBtnName,
			String cancelBtnName, PromptDialogListener listener) {
		super(context);
		this.titile = title;
		this.content = content;
		this.sureBtnName = sureBtnName;
		this.cancelBtnName = cancelBtnName;
		this.listener = listener;
		this.contentBold = contentBold;
		this.show();
		instance = this;
	}

	/**
	 * @param context
	 * @param title 标题名称
	 * @param content 内容文字
	 * @param sureBtnName 确定按钮名称
	 * @param cancelBtnName 取消按钮名称
	 * @param contentBold 内容字体是否加粗
	 * @param listener 处理结果回调监听器
	 */
	public CldPromptDialog(Context context, CharSequence title,
			CharSequence content, boolean contentBold, String sureBtnName,
			String cancelBtnName, Boolean isSubmitAlignRight,
			PromptDialogListener listener) {
		super(context);
		this.titile = title;
		this.content = content;
		this.sureBtnName = sureBtnName;
		this.cancelBtnName = cancelBtnName;
		this.listener = listener;
		this.contentBold = contentBold;
		this.isSubmitAlignRight = isSubmitAlignRight;
		this.show();
		instance = this;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		//this.setCanceledOnTouchOutside(false);
		this.getContext().setTheme(R.style.dialog);
		setContentView(R.layout.layout_prompt_dialog);
		tv_title = (TextView) findViewById(R.id.prompt_title_text);
		lay_content = (LinearLayout) findViewById(R.id.prompt_layout_content);
		btn_cancel = (Button) findViewById(R.id.prompt_btn_cancel);
		btn_sure = (Button) findViewById(R.id.prompt_btn_sure);
		v_btn_divider = findViewById(R.id.btn_divider);
		if (!TextUtils.isEmpty(titile)) {
			tv_title.setText(titile);
		} else {
			tv_title.setVisibility(View.INVISIBLE);
		}
		
		if (!TextUtils.isEmpty(content)) {
			if (content instanceof String) {
				String[] contents = ((String) content).split("\n");
				if (contents.length == 1) { // 单行
					lay_content.addView(getTextView(content, Gravity.CENTER));
				} else { // 多行
					for (String string : contents) {
						lay_content.addView(getTextView(string, Gravity.LEFT));
					}
				}
			} else {
				lay_content.addView(getTextView(content, Gravity.CENTER));
			}
		} else {
			lay_content.setVisibility(View.GONE);
		}
		
		if (!TextUtils.isEmpty(sureBtnName)) {
			btn_sure.setText(sureBtnName);
		} else {
			btn_sure.setVisibility(View.GONE);
			if (v_btn_divider != null) {
				v_btn_divider.setVisibility(View.GONE);
			}
		}
		
		if (!TextUtils.isEmpty(cancelBtnName)) {
			btn_cancel.setText(cancelBtnName);
		} else {
			btn_cancel.setVisibility(View.GONE);
			if (v_btn_divider != null) {
				v_btn_divider.setVisibility(View.GONE);
			}
		}
		btn_cancel.setOnClickListener(this);
		btn_sure.setOnClickListener(this);
		btn_sure.setSelected(true);
	}
	
	private TextView getTextView(CharSequence content, int gravity) {
		TextView tv = new TextView(getContext());
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		tv.setLayoutParams(params);
		if (contentBold) {
			tv.getPaint().setFakeBoldText(true);
		}
		tv.setText(content);
		tv.setGravity(gravity);
		Configuration config = getContext().getResources().getConfiguration();
		// 竖版
		if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
			tv.setTextColor(Color.WHITE);
			tv.setTextSize(24);
		} else {
			tv.setTextColor(Color.WHITE);
			tv.setTextSize(28);
		}
		return tv;
	}

	@Override
	public void onClick(View v) {

		this.dismiss();
		switch (v.getId()) {
			case R.id.prompt_btn_cancel :
				if (null != listener) {
					if (isSubmitAlignRight) {
						listener.onCancel();
					} else {
						listener.onSure();
					}

				}
				dismiss();
				break;

			case R.id.prompt_btn_sure :
				if (null != listener) {
					if (isSubmitAlignRight) {
						listener.onSure();
					} else {
						listener.onCancel();
					}
				}
				break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			hasOnKeyDown = true;
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (hasOnKeyDown) {
				hasOnKeyDown = false;
				if (null != listener) {
					listener.onCancel();
				}
				dismiss();
				return true;
			}

		}

		hasOnKeyDown = false;
		return super.onKeyUp(keyCode, event);
	}
}