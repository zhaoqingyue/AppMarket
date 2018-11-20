package cld.kcloud.utils.control;

/**
 * K���û��������Ƚ������
 * @author wuyl
 *
 */

import cld.kcloud.center.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class CldProgress extends Dialog implements OnClickListener{
	
	private static CldProgress progressDialog = null;
	private static CldProgressListener listener;
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}



	public interface CldProgressListener {
		public void onCancel();
	}
	
	public CldProgress(Context context) {
		super(context);
	}

	public CldProgress(Context context, int theme) {
		super(context, theme);
	}
	
	/**
	 * ����������
	 * 
	 * @param msg
	 *            ��ʾ����
	 * @param listener
	 *            ���ȡ������
	 * @return CldProgress
	 */
	private static CldProgress createDialog(Context context, final String msg, 
			final CldProgressListener listener) {
		CldProgress.listener = listener;
		if (null != context) {
			final Activity act = (Activity) context;
			if (null != act) {
				act.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (progressDialog == null) {
							progressDialog = new CldProgress(act,
									R.style.dialog);
							progressDialog
									.setContentView(R.layout.layout_progress_dialog);
							progressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
							progressDialog.setCanceledOnTouchOutside(false);
							
							if (!TextUtils.isEmpty(msg)) {
								TextView tv_msg = (TextView) progressDialog.findViewById(R.id.progress_loading_text);
								if (null != tv_msg) {
									tv_msg.setText(msg);
								}
							}
							
							// û������ȡ������������ʾx��ť
							if (null == listener) {
								ImageView cancel = (ImageView) progressDialog.findViewById(R.id.progress_btn_cancel);
								cancel.setVisibility(View.GONE);
							}
							
							progressDialog.show();
						}
					}
				});
			}
		}
		return progressDialog;
	}
	
	public void onWindowFocusChanged(boolean hasFocus) {

		try {
			if (progressDialog == null) {
				return;
			}

			ImageView btn_cancel = (ImageView) progressDialog.findViewById(R.id.progress_btn_cancel);
			btn_cancel.setOnClickListener(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ��ʾ�ȴ�������������
	 * @param context
	 * @return
	 */
	public static CldProgress showProcess(Context context) {
		createDialog(context, null, null);
		return progressDialog;
	}
	
	/**
	 * ��ʾ�ȴ�������������
	 * 
	 * @param message ��ʾ��Ϣ
	 * @return CldProgress

	 */
	public static CldProgress showProgress(Context context, String message) {
		createDialog(context, message, null);
		return progressDialog;
	}
	
	/**
	 * ��ʾ�ȴ�������������
	 * 
	 * @param listener ������ȡ����ť������
	 * @return CldProgress
	 */
	public static CldProgress showProgress(Context context, CldProgressListener listener) {
		createDialog(context, null, listener);
		return progressDialog;
	}
	
	/**
	 * ��ʾ�ȴ�������������
	 * 
	 * @param context
	 * @param message
	 *            ��ʾ����
	 * @param listener
	 *            ������ȡ����ť������
	 * @return CldProgress
	 */
	public static CldProgress showProgress(Context context, String message,
			CldProgressListener listener) {
		createDialog(context, message, listener);
		return progressDialog;
	}
	
	/**
	 * �ȴ��������������Ƿ���ʾ
	 * 
	 * @return boolean
	 */
	public static boolean isShowProgress() {
		return (null != progressDialog && progressDialog.isShowing());
	}
	
	/**
	 * ȡ���ȴ�������������
	 * 
	 * @return void
	 */
	public static void cancelProgress() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.cancel();
			progressDialog = null;
		}
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.progress_btn_cancel:
			if (null != listener) {
				listener.onCancel();
			}
			cancelProgress();
			break;

		default:
			break;
		}
	}  

}
