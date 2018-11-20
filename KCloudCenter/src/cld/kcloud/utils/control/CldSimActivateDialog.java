/**
 * 
 * Copyright ? 2016Careland. All rights reserved.
 *
 * @Title: CldSimActivateDialog.java
 * @Prject: KCloudCenter_M550
 * @Package: cld.kcloud.utils.control
 * @Description: Sim¿¨¼¤»î½çÃæ
 * @author: zhaoqy
 * @date: 2016Äê7ÔÂ23ÈÕ ÏÂÎç6:01:36
 * @version: V1.0
 */

package cld.kcloud.utils.control;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import cld.kcloud.center.R;

public class CldSimActivateDialog {

	public interface CldSimActivateDialogListener {
		public void onActivate();
	}

	private static LinearLayout mLayout;
	private static WindowManager mWindowManager;
	private static WindowManager.LayoutParams wmParams;
	private static CldSimActivateDialog mDialog = null;
	

	/**
	 * 
	 * @param context
	 * @param title
	 * @param hint
	 * @return
	 */
	public static void showDialog(Context context, String pkdesc, CldSimActivateDialogListener listener) {
		createDialog(context, pkdesc, listener);
	}

	public static void cancelDialog() {
		if (mDialog != null) {
			mWindowManager.removeView(mLayout);

			mDialog = null;
			wmParams = null;
			mWindowManager = null;
		}
	}
	
	@SuppressLint("NewApi")
	private static void createDialog(Context context, String pkdesc, 
			final CldSimActivateDialogListener listener) {

		cancelDialog();

		if (mDialog == null) {
			mDialog = new CldSimActivateDialog();
			
			wmParams = new WindowManager.LayoutParams();
			mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			wmParams.type = 2026;//WindowManager.LayoutParams.TYPE_PHONE;
			wmParams.format = PixelFormat.TRANSPARENT;
			wmParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
			wmParams.width = 1600;//WindowManager.LayoutParams.MATCH_PARENT;
			wmParams.height = WindowManager.LayoutParams.MATCH_PARENT;
			wmParams.gravity = Gravity.RIGHT;
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mLayout = (LinearLayout) inflater.inflate(R.layout.layout_sim_activate_dialog, null);
			//int uioption = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
			//mLayout.setSystemUiVisibility(uioption);
			mWindowManager.addView(mLayout, wmParams);
			
			TextView tv = (TextView)mLayout.findViewById(R.id.activate_dialog_text);
			Button btnOk = (Button)mLayout.findViewById(R.id.activate_dialog_btn_ok);
			
			if (tv != null) {
				tv.setText(pkdesc);
			}
			
			if (btnOk != null) {
				btnOk.setOnClickListener(new View.OnClickListener() {
				
					@Override
					public void onClick(View arg0) {
						if (listener != null) {
							listener.onActivate();
						}
						cancelDialog();
					}
				});
			}

		}
	}
}