package cld.kmarcket.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import cld.kmarcket.R;

public class RoundProgressBar extends View
{
	/**
	 * 画笔对象的引用
	 */
	private Paint paint;
	
	/**
	 * 圆环的颜色
	 */
	private int roundColor;
	
	/**
	 * 圆环进度的颜色
	 */
	private int roundProgressColor;
	
	/**
	 * 圆环的宽度
	 */
	private float roundWidth;
	
	/**
	 * 最大进度
	 */
	private int max;
	
	/**
	 * 当前进度
	 */
	private long progress;
	
	public RoundProgressBar(Context context) 
	{
		this(context, null);
	}

	public RoundProgressBar(Context context, AttributeSet attrs) 
	{
		this(context, attrs, 0);
	}
	
	public RoundProgressBar(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
		
		paint = new Paint();
		TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.RoundProgressBar);
		//获取自定义属性和默认值
		roundColor = ta.getColor(R.styleable.RoundProgressBar_roundColor, 
				context.getResources().getColor(R.color.transparent));
		roundProgressColor = ta.getColor(R.styleable.RoundProgressBar_roundProgressColor, 
				context.getResources().getColor(R.color.transparent));
		roundWidth = ta.getDimension(R.styleable.RoundProgressBar_roundWidth, 2);
		max = ta.getInteger(R.styleable.RoundProgressBar_max, 100);
		progress = ta.getInteger(R.styleable.RoundProgressBar_progress, 0);
		ta.recycle();
	}
	
	@SuppressLint("DrawAllocation") @Override
	protected void onDraw(Canvas canvas) 
	{
		super.onDraw(canvas);
		/**
		 * 画最外层的大圆环
		 */
		int centre = getWidth()/2; //获取圆心的x坐标
		int radius = (int) (centre - roundWidth/2); //圆环的半径
		paint.setColor(roundColor); //设置圆环的颜色
		paint.setStyle(Paint.Style.STROKE); //设置空心
		paint.setStrokeWidth(roundWidth); //设置圆环的宽度
		paint.setAntiAlias(true);  //消除锯齿 
		canvas.drawCircle(centre, centre, radius, paint); //画出圆环
		
		/**
		 * 画圆弧 ，画圆环的进度
		 */
		//设置进度是实心还是空心
		paint.setStrokeWidth(roundWidth); //设置圆环的宽度
		paint.setColor(roundProgressColor);  //设置进度的颜色
		RectF oval = new RectF(centre - radius, centre - radius, centre
				+ radius, centre + radius);  //用于定义的圆弧的形状和大小的界限
		paint.setStyle(Paint.Style.STROKE);
		if (progress >= 0)
		{
			//根据进度画圆弧
			canvas.drawArc(oval, 270, 360 * progress / max, false, paint);  
		}
	}
	
	public synchronized int getMax() 
	{
		return max;
	}

	/**
	 * 设置进度的最大值
	 * @param max
	 */
	public synchronized void setMax(int max) 
	{
		if(max < 0)
		{
			throw new IllegalArgumentException("max not less than 0");
		}
		this.max = max;
	}

	/**
	 * 获取进度.需要同步
	 * @return
	 */
	public synchronized long getProgress() 
	{
		return progress;
	}

	/**
	 * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步
	 * 刷新界面调用postInvalidate()能在非UI线程刷新
	 * @param progress
	 */
	public synchronized void setProgress(long progress) 
	{
		if(progress < 0)
		{
			throw new IllegalArgumentException("progress not less than 0");
		}
		if(progress > max)
		{
			progress = max;
		}
		if(progress <= max)
		{
			this.progress = progress;
			postInvalidate();
			onSizeChanged(getWidth(), getHeight(), 0, 0);
		}
	}
	
	public int getCricleColor() 
	{
		return roundColor;
	}

	public void setCricleColor(int cricleColor) 
	{
		this.roundColor = cricleColor;
	}

	public int getCricleProgressColor() 
	{
		return roundProgressColor;
	}

	public void setCricleProgressColor(int cricleProgressColor) 
	{
		this.roundProgressColor = cricleProgressColor;
	}

	public float getRoundWidth() 
	{
		return roundWidth;
	}

	public void setRoundWidth(float roundWidth) 
	{
		this.roundWidth = roundWidth;
	}
}
