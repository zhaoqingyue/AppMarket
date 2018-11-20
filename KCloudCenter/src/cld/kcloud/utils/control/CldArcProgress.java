package cld.kcloud.utils.control;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class CldArcProgress extends View{
	
	//灰色画笔,画剩余流量部分
	private Paint mPaintDark;
	private static final int COLOR_DARK = 0xff3e4044;
	private static final int DEFAULT_STROKE_WIDTH_DARK = 20;
	private int darkStrokeWidth;
	
	//绿色画笔,画已用流量部分
	private Paint mPaintOrange;
	private static final int COLOR_ORANGE = 0xff31b31f;
	private static final int DEFAULT_STROKE_WIDTH_ORANGE = 16;
	private int orangeStrokeWidth;
	
	//整个圆弧所占宽度及高度
	private static final float DEFAULT_ARC_WIDTH = 289.0f;
	private float arcWidth;
	private static final float DEFAULT_ARC_HEIGHT = 269.0f;
	private float arcHeight;
	
	//圆弧起始角度
	private static final int ARC_START_DEGREE = 120;
	//圆弧结束角度
	private static final int ARC_END_DEGREE = 300;
	//橙色占百分比
	private float rate = 0;

	public CldArcProgress(Context context) {
		super(context);
		init();
	}
	
	public CldArcProgress(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public CldArcProgress(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	
	/**
	 * 初始化
	 */
	private void init(){
		darkStrokeWidth = DEFAULT_STROKE_WIDTH_DARK;
		orangeStrokeWidth = DEFAULT_STROKE_WIDTH_ORANGE;
		arcWidth = DEFAULT_ARC_WIDTH;
		arcHeight = DEFAULT_ARC_HEIGHT;
		initPaint();
	}
	
	/**
	 * 初始化画笔
	 */
	private void initPaint(){
		mPaintDark = new Paint();
		mPaintDark.setColor(COLOR_DARK);
		mPaintDark.setStyle(Paint.Style.STROKE);
		mPaintDark.setStrokeCap(Cap.ROUND);
		mPaintDark.setStrokeWidth(DEFAULT_STROKE_WIDTH_DARK);
		mPaintDark.setAntiAlias(true);
		
		mPaintOrange = new Paint();
		mPaintOrange.setColor(COLOR_ORANGE);
		mPaintOrange.setStyle(Paint.Style.STROKE);
		mPaintOrange.setStrokeCap(Cap.ROUND);
		mPaintOrange.setStrokeWidth(DEFAULT_STROKE_WIDTH_ORANGE);
		mPaintOrange.setAntiAlias(true);
	}
	
	/**
	 * 设置黑色弧粗细
	 * @param width
	 */
	public void setDarkStrokeWidth(int width){
		darkStrokeWidth = width;
		mPaintDark.setStrokeWidth(darkStrokeWidth);
		invalidate();
	}
	
	/**
	 * 设置橙色弧粗细
	 * @param width
	 */
	public void setOrangeStrokeWidth(int width){
		orangeStrokeWidth = width;
		mPaintOrange.setStrokeWidth(orangeStrokeWidth);
		invalidate();
	}
	
	/**
	 * 设置控件高度 默认高度 269
	 * @param height
	 */
	public void setHeight(float height){
		this.arcHeight = height;
		invalidate();
	}
	
	/**
	 * 设置控件宽度 默认宽度 289
	 * @param width
	 */
	public void setWidth(float width){
		this.arcWidth = width;
		invalidate();
	}
	
	/**
	 * 设置橙色弧所占比例
	 * @param rate
	 */
	public void setRate(float rate){
		this.rate = rate;
		invalidate();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
	}
	
	private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = (int) DEFAULT_ARC_WIDTH + getPaddingLeft()
                    + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }

        return result;
    }
	
	private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = (int) DEFAULT_ARC_HEIGHT + getPaddingTop()
                    + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }
	
	@SuppressLint("DrawAllocation") 
	@Override
	protected void onDraw(Canvas canvas) {
		float width = (float)getWidth();
		RectF oval = new RectF(width / 2 - arcWidth / 2 + darkStrokeWidth / 2, darkStrokeWidth / 2, 
				width / 2 + arcWidth / 2 - darkStrokeWidth / 2, darkStrokeWidth / 2 + arcHeight);
		canvas.drawArc(oval, ARC_START_DEGREE, ARC_END_DEGREE, false, mPaintDark);
		//Log.d("onDraw", " ++++  rate: " + rate);
		canvas.drawArc(oval, ARC_START_DEGREE, rate * ARC_END_DEGREE, false, mPaintOrange);
	}
}