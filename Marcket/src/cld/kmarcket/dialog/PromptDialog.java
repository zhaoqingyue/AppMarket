package cld.kmarcket.dialog;

import cld.kmarcket.R;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class PromptDialog extends Dialog implements OnClickListener
{
	private IDialogClick mListener;
	private TextView     mMessage;
	private Button       mSure;
	private Button       mCancel;
	private String       mMessageStr;
	private String       mSureStr;
	private String       mCancelStr;
	
	public PromptDialog(Context context) 
	{
		super(context);
	}
	
	public PromptDialog(Context context, String messageStr) 
	{
		super(context, R.style.dialog_style);
		mMessageStr = messageStr;
	}
	
	public PromptDialog(Context context, String messageStr, String sureStr, String cancelStr) 
	{
		super(context, R.style.dialog_style);
		mMessageStr = messageStr;
		mSureStr = sureStr;
		mCancelStr = cancelStr;
	}
	
	public PromptDialog(Context context, int theme, String messageStr, String sureStr, String cancelStr) 
	{
		super(context, theme);
		mMessageStr = messageStr;
		mSureStr = sureStr;
		mCancelStr = cancelStr;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//点击Dialog以外的区域，Dialog不关闭
		setCanceledOnTouchOutside(false);
		//设置成系统级别的Dialog，即全局性质的Dialog，在任何界面下都可以弹出来
		getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);  
		getWindow().setContentView(R.layout.dialog_prompt);
		
		initViews();
		setListener();
		setViews();
	}

	private void initViews() 
	{
		mMessage = (TextView) findViewById(R.id.id_dialog_prompt_message);
		mSure = (Button) findViewById(R.id.id_dialog_prompt_sure);
		mCancel = (Button) findViewById(R.id.id_dialog_prompt_cancel);
	}

	private void setListener() 
	{
		mSure.setOnClickListener(this);
		mCancel.setOnClickListener(this);
	}

	private void setViews() 
	{
		mMessage.setText(mMessageStr);
	}

	@Override
	public void onClick(View v) 
	{
		mListener.onClick(v);
	}
	
	public void setOnDialogClickListener(IDialogClick listener)
	{
		mListener = listener;
	}
}
