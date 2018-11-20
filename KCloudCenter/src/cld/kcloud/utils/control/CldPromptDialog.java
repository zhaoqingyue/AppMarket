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
 * ��ʾ�Ի���
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

	/** �Ƿ������onkeyDown�¼���������onkeydown֮��Żᴦ��onkeyup�¼����������Ϸ��� */
	private boolean hasOnKeyDown;

	private static CldPromptDialog instance;

	/** ȷ�ϰ�ťĬ�����ұ� */
	private boolean isSubmitAlignRight = true;

	/**
	 * ���ַ�������Դid
	 */
	public static final int RES_NONE_STRING = -1;

	public interface PromptDialogListener {
		public void onCancel();
		public void onSure();
	}

	/**
	 * @param context
	 * @param title ��������
	 * @param content ��������
	 * @param sureBtnName ȷ����ť����
	 * @param cancelBtnName ȡ����ť����
	 * @param listener �������ص�������
	 */
	public static void createPromptDialog(Context context, CharSequence title,
			CharSequence content, String sureBtnName, String cancelBtnName,
			PromptDialogListener listener) {
		new CldPromptDialog(context, title, content, false, sureBtnName,
				cancelBtnName, listener);
	}

	/**
	 * @param context
	 * @param title ��������
	 * @param content ��������
	 * @param sureBtnName ȷ����ť����
	 * @param cancelBtnName ȡ����ť����
	 * @param listener �������ص�������
	 */
	public static void createPromptDialog(Context context, CharSequence title,
			CharSequence content, String sureBtnName, String cancelBtnName,
			boolean isSubmitAlignRight, PromptDialogListener listener) {
		new CldPromptDialog(context, title, content, false, sureBtnName,
				cancelBtnName, isSubmitAlignRight, listener);
	}

	/**
	 * @param context
	 * @param title ��������
	 * @param content ��������
	 * @param contentBold ���������Ƿ�Ӵ�
	 * @param sureBtnName ȷ����ť����
	 * @param cancelBtnName ȡ����ť����
	 * @param listener �������ص�������
	 */
	public static void createPromptDialog(Context context, CharSequence title,
			CharSequence content, boolean contentBold, String sureBtnName,
			String cancelBtnName, PromptDialogListener listener) {
		new CldPromptDialog(context, title, content, contentBold, sureBtnName,
				cancelBtnName, listener);
	}

	/**
	 * �Ƿ�������ʾ
	 * @return void
	 * @date 2015��8��18�� ����2:49:21
	 */
	public static boolean isShow() {
		if (instance == null) {
			return false;
		}
		return instance.isShowing();
	}

	/**
	 * ȡ���Ի�����ʾ
	 * @return void
	 * @date 2015��8��13�� ����6:32:59
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
	 * @param title ��������
	 * @param content ��������
	 * @param sureBtnName ȷ����ť����
	 * @param cancelBtnName ȡ����ť����
	 * @param listener �������ص�������
	 */
	public static void createPromptDialog(Context context, int title,
			int content, int sureBtnName, int cancelBtnName,
			PromptDialogListener listener) {
		new CldPromptDialog(context, getString(context, title), getString(
				context, content), false, getString(context, sureBtnName),
				getString(context, cancelBtnName), listener);
	}

	/**
	 * ������Դid��ȡ�ַ�����idΪRES_NONE_STRING�������Ҳ�����Դʱ ����null
	 * @param context
	 * @param res
	 * @return String
	 * @date 2015��7��2�� ����7:55:50
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
	 * @param title ��������
	 * @param content ��������
	 * @param sureBtnName ȷ����ť����
	 * @param cancelBtnName ȡ����ť����
	 * @param contentBold ���������Ƿ�Ӵ�
	 * @param listener �������ص�������
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
	 * @param title ��������
	 * @param content ��������
	 * @param sureBtnName ȷ����ť����
	 * @param cancelBtnName ȡ����ť����
	 * @param contentBold ���������Ƿ�Ӵ�
	 * @param listener �������ص�������
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
				if (contents.length == 1) { // ����
					lay_content.addView(getTextView(content, Gravity.CENTER));
				} else { // ����
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
		// ����
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