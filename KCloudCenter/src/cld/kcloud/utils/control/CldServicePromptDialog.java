package cld.kcloud.utils.control;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;
import cld.kcloud.center.R;

public class CldServicePromptDialog extends Dialog {
	
	private static CldServicePromptDialog mServicePromptDialog = null;
	
	public static CldServicePromptDialog createServicePromptDialog(Context context, String s) {
		if (mServicePromptDialog == null) {
			mServicePromptDialog = new CldServicePromptDialog(context);
		}
		
		if (mServicePromptDialog != null) {
			mServicePromptDialog.setContentView(R.layout.layout_service_prompt_dialog);
			
			WindowManager.LayoutParams lp = mServicePromptDialog.getWindow().getAttributes();
			lp.x = 70;
			lp.y = -15;
			lp.dimAmount = 0.6f;
			lp.gravity = Gravity.CENTER;
			mServicePromptDialog.getWindow().setAttributes(lp);
			
			TextView tv = (TextView) mServicePromptDialog.findViewById(R.id.service_prompt_dialog_text);
			if (tv != null) {
				tv.setText(s);
			}
		}

		return mServicePromptDialog;
	}

	public CldServicePromptDialog(Context context) {
		super(context, R.style.dialog);
	}
	
	@Override
	public void dismiss() {
		mServicePromptDialog = null;
		super.dismiss();
	}
}
