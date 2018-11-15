package cld.kmarcket.customview;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;


public class MyAlerDialog {

	
	@SuppressLint("NewApi")
	static public AlertDialog CreateAlerDialog(Context context,
			final Rect parentRect, int theme) {

		// TODO Auto-generated constructor stub
		final AlertDialog dialog;
		if (theme == -1) {
			dialog = new AlertDialog.Builder(context).create();
		} else {
			dialog = new AlertDialog.Builder(context, theme).create();
		}

		if (parentRect == null || parentRect.width() == 0
				|| parentRect.height() == 0)
			return dialog;

		// Toast tmp = Toast.makeText(context, "aaddbbccd", 0);
		// Log.d("fbh","tosstw:"+tmp.getView().getWidth());
		// tmp.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL,0,0);
		// tmp.show();
		// Log.d("fbh","tosstw:"+tmp.getView().getWidth());

		dialog.setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface arg0) {
				// TODO Auto-generated method stub
				WindowManager.LayoutParams params = dialog.getWindow()
						.getAttributes();
				int diaogWidth = dialog.getWindow().getDecorView().getWidth();
				// Log.d("fbh",""+diaogWidth+"  rect"+parentRect);
				params.x = (parentRect.left + (parentRect.width() - diaogWidth) / 2);
				params.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;

				// int strokeWidth = 5; // 3dp 边框宽度
				// int roundRadius = 15; // 8dp 圆角半径
				// int strokeColor = Color.parseColor("#2E3135");//边框颜色
				// int fillColor = Color.parseColor("#DFDFE0");//内部填充颜色
				//
				// GradientDrawable gd = new GradientDrawable();//创建drawable
				// gd.setColor(fillColor);
				// gd.setCornerRadius(roundRadius);
				// gd.setStroke(strokeWidth, strokeColor);
				// dialog.getWindow().setBackgroundDrawable(gd);

				dialog.getWindow().setAttributes(params);
			}
		});
		
		return dialog;
	
	}
	@SuppressLint("NewApi")
	static public AlertDialog CreateAlerDialog(Context context, final Rect parentRect) {
		// TODO Auto-generated constructor stub
		
		return CreateAlerDialog(context, parentRect, -1);
		
	}
	
	static public AlertToast CreateAlertToast(Context context,
			final Rect parentRect, int theme){
		AlertDialog tmp = CreateAlerDialog(context, parentRect, theme);
		AlertToast toast = new AlertToast(tmp);
		
		return toast;
	}
	
	static public class AlertToast {
		AlertDialog mAlertDialog;
		@SuppressLint("HandlerLeak")
		Handler mHandler = new Handler(){
			@Override
			public void dispatchMessage(Message msg) {
				// TODO Auto-generated method stub
				super.dispatchMessage(msg);
				if(msg.what == 0){
					if(mAlertDialog != null)
						mAlertDialog.dismiss();
				}
			}
		};
		protected AlertToast(AlertDialog alertDialog) {
			// TODO Auto-generated constructor stub
			mAlertDialog = alertDialog;
		}
		
		public AlertDialog getAlertDialog(){
			return mAlertDialog;
		}
		
		public void setContentView(int layoutResID) {
			mAlertDialog.setContentView(layoutResID);
		}

		public void setContentView(View view) {
			mAlertDialog.setContentView(view);
		}
		
		public void setContentView(View view,ViewGroup.LayoutParams params) {
			mAlertDialog.setContentView(view, params);
		}

		public void show(){
			if(mAlertDialog != null){
				mAlertDialog.show();
				mHandler.sendEmptyMessageDelayed(0, 3000);
			}
		}
	}

}
