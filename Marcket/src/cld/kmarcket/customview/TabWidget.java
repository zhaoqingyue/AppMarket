package cld.kmarcket.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cld.kmarcket.R;

public class TabWidget extends RelativeLayout 
{
	private Context   mContext;    
	private ImageView mIcon;      
	private TextView  mText;
	private ImageView mIndicate;
	private String    mTextStr;
	private int mIconNormalId = 0, mIconSelId = 0;
	
	@SuppressLint("Recycle") 
	public TabWidget(Context context) 
	{
		super(context);
	}
	
	@SuppressLint("Recycle") 
	public TabWidget(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		mContext = context;
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TabWidget);
		mTextStr = ta.getString(R.styleable.TabWidget_tab_text);
		ta.recycle();
		init();
	}
	
	public TabWidget(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
	}
	
	private void init()
	{
		inflate(mContext, R.layout.view_tab_widget, this);
		mIcon = (ImageView) findViewById(R.id.id_tab_item_icon);
		mText = (TextView) findViewById(R.id.id_tab_item_name);
		mIndicate = (ImageView) findViewById(R.id.id_tab_item_indicate);
		mText.setText(mTextStr);
	}
	
	public void setIconSelector(int nomalId, int selID)
	{
		mIconNormalId = nomalId;
		mIconSelId = selID;
	}
	
	@SuppressLint("NewApi") 
	public void setTabSelected(boolean isSelected)
	{
		if (isSelected)
		{
			mIcon.setImageResource(mIconSelId);
			mText.setTextColor(mContext.getResources().getColor(R.color.main_color));
		}
		else
		{
			mIcon.setImageResource(mIconNormalId);
			mText.setTextColor(mContext.getResources().getColor(R.color.white));
		}
	}
	
	public void setIndicateDisplay(boolean visible) 
	{
		mIndicate.setVisibility(visible ? View.VISIBLE : View.GONE);
	}
}
