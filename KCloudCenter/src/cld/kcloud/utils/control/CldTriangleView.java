package cld.kcloud.utils.control;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * 服务界面提示框下面的小三角形
 * @author yehf
 *
 */

public class CldTriangleView extends View {
	private Paint mPaint;
	
	private static int DEFAULT_WIDTH = 30;
	private static int DEFAULT_HEIGHT = 26;
	private static int DEFAULT_COLOR = 0xff63676f;

	public CldTriangleView(Context context) {
		super(context);
		init();
	}
	
	public CldTriangleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public CldTriangleView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	
	private void init() {
		mPaint = new Paint();
		mPaint.setColor(DEFAULT_COLOR);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setAntiAlias(true);
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
            result = (int) DEFAULT_WIDTH + getPaddingLeft()
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
            result = (int) DEFAULT_HEIGHT + getPaddingTop()
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
		Path path = new Path();
		path.moveTo(0, 0);
		path.lineTo(DEFAULT_WIDTH / 2, DEFAULT_HEIGHT);
		path.lineTo(DEFAULT_WIDTH, 0);
		path.close();
		canvas.drawPath(path, mPaint);
	}
}
