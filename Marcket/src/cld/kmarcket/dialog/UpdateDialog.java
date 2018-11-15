package cld.kmarcket.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import cld.kmarcket.R;

public class UpdateDialog extends Dialog 
{
	private Context mContext;
	private LinearLayout mLayout;
	private WindowManager mWindowManager;
	private WindowManager.LayoutParams wmParams;
	
	public UpdateDialog(Context context) 
	{
		super(context, R.style.dialog_fullscreen);
		mContext = context;
	}
	
	@SuppressLint("NewApi") 
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		//隐藏状态栏
		/*View decorView = getWindow().getDecorView();
		int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;;
		if((decorView.getSystemUiVisibility() & uiOptions) == 0)
			decorView.setSystemUiVisibility(uiOptions);*/
		
		wmParams = new WindowManager.LayoutParams();
		mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		wmParams.type = 2026;//WindowManager.LayoutParams.TYPE_PHONE;
		wmParams.format = PixelFormat.TRANSPARENT;
		wmParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		wmParams.width = 1600; //WindowManager.LayoutParams.MATCH_PARENT;
		wmParams.height = WindowManager.LayoutParams.MATCH_PARENT;
		wmParams.gravity = Gravity.RIGHT;

		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mLayout = (LinearLayout) inflater.inflate(R.layout.dialog_update, null);
		//int uioption = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
		//mLayout.setSystemUiVisibility(uioption);
		mWindowManager.addView(mLayout, wmParams);
		
		//点击Dialog以外的区域，Dialog不关闭
		setCanceledOnTouchOutside(false);
	}
}
