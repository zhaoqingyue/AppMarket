package cld.kcloud.utils.control;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cld.kcloud.center.R;
import cld.kcloud.utils.KCloudCommonUtil;

public class CldMessageDialog {	
	public enum CldMessageIcon {
		eMessageIcon_App,			// 应用
		eMessageIcon_Navi, 			// 导航
		eMessageIcon_System,		// 系统
		eMessageIcon_business,		// 运营
	}
	
	public enum CldMessageType {
		eMessageType_None, 
		eMessageType_Ok,
		eMessageType_Close,
		eMessageType_Ok_Close,
	};
	
	public enum CldMessageGravity {
		eMessageGravity_Left,
		eMessageGravity_Center,
		eMessageGravity_Right,
	};

	public interface CldMessageDialogListener {
		public void onOk();
		public void onCancel();
	}
	private String mMsg = "";
	private TextView mTv = null;
	private TimerTask mTask = null;
	private Timer mTimer = new Timer();
	private int nCurrentIndex = 0;
	private ArrayList<String> mArrayList = null;
	private Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				if (nCurrentIndex == mArrayList.size() - 1) {
					nCurrentIndex = 0;
				} else {
					nCurrentIndex++;
				}

				if (mTv != null) {
					mTv.setText(mArrayList.get(nCurrentIndex));
				}
			} else {
				mMessageDialog.splitString(mTv, mMsg);
				if (mMessageDialog.mArrayList.size() > 1) {
					mMessageDialog.startTask();
				}
				mTv.setText(mMessageDialog.mArrayList.get(0));
			}
		}
		
	};
	
	private static WindowManager mWindowManager;
	private static WindowManager.LayoutParams wmParams;
	private static LinearLayout mFloatLayout;
	private static CldMessageDialog mMessageDialog = null;
	private static CldMessageDialogListener mListener = null;
	
	public CldMessageDialog() {
		nCurrentIndex = 0;
		mArrayList = new ArrayList<String>();
	}
	
	public void clear() {
		nCurrentIndex = 0;
		mArrayList.clear();
		mArrayList = null;
	}
	
	public void startTask() {
		cancelTask();
		mTask = new TimerTask() {
			@Override
			public void run() {
				if (null != mHandler) {
					mHandler.sendEmptyMessage(0);
				}
			}
		};
		mTimer.schedule(mTask, 4000, 4000);
	}

	public void cancelTask() {
		if (mTask != null) {
			mTask.cancel();
			mTask = null;
		}
	}
	
	/**
	 * 
	 * @param context
	 * @param title
	 * @param hint
	 * @return
	 */
	public static void showMessageDialog(Context context, String msg,
			CldMessageType type, String btnText, CldMessageDialogListener listener) {
		createDialog(context, msg, type, CldMessageIcon.eMessageIcon_System, btnText, "", listener);
	}
	
	/**
	 * 
	 * @param context
	 * @param title
	 * @param hint
	 * @return
	 */
	public static void showMessageDialog(Context context, String msg,
			CldMessageType type, CldMessageIcon icon, String btnText,
			String packName, CldMessageDialogListener listener) {
		createDialog(context, msg, type, icon, btnText, packName, listener);
	}

	public static void cancelMessageDialog() {
		if (mMessageDialog != null) {
			mWindowManager.removeView(mFloatLayout);

			wmParams = null;
			mWindowManager = null;
			mMessageDialog.cancelTask();
			mMessageDialog.clear();
			mMessageDialog.mHandler.removeCallbacksAndMessages(null);
			mMessageDialog = null;
		}
	}
	
	private void splitString(final TextView tv, final String rawText) {
		final Paint tvPaint = tv.getPaint();
		final float tvWidth = tv.getWidth() - tv.getPaddingLeft() - tv.getPaddingRight() - 5;	// 控件可用宽度
		
		// 拆分文本
		String[] rawTextLines = rawText.replaceAll("\r", "").split("\n");
		
		for (String rawTextLine : rawTextLines) {
			StringBuilder sbNewText = new StringBuilder();
			if (tvPaint.measureText(rawTextLine) < tvWidth) {
				//如果整行宽度在控件可用宽度之内，就不处理了
				sbNewText.append(rawTextLine);
				mArrayList.add(sbNewText.toString());
			} else {
				//如果整行宽度超过控件可用宽度，则按字符测量，在超过可用宽度的前一个字符处手动换行
				float lineWidth = 0;
				for (int cnt = 0; cnt != rawTextLine.length(); ++cnt) {
					char ch = rawTextLine.charAt(cnt);
					lineWidth += tvPaint.measureText(String.valueOf(ch));
					if (lineWidth <= tvWidth) {
						sbNewText.append(ch);
					} else {
						sbNewText.append("\n");
						mArrayList.add(sbNewText.toString());
						sbNewText.delete(0, sbNewText.length());
						lineWidth = 0;
						--cnt;
					}
				}
				sbNewText.append("\n");
				mArrayList.add(sbNewText.toString());
			}
		}
	}
	
	// drawable 转换成bitmap
	public static Bitmap drawableToBitmap(Drawable drawable) {
		int width = drawable.getIntrinsicWidth(); // 取drawable的长宽
		int height = drawable.getIntrinsicHeight();
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565; // 取drawable的颜色格式
		Bitmap bitmap = Bitmap.createBitmap(width, height, config); // 建立对应bitmap
		Canvas canvas = new Canvas(bitmap); // 建立对应bitmap的画布
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas); // 把drawable内容画到画布中
		return bitmap;
	}
	
	// 
	public static Drawable zoomDrawable(Context context, Drawable drawable, int w, int h) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap oldbmp = drawableToBitmap(drawable);// drawable转换成bitmap
		Matrix matrix = new Matrix(); // 创建操作图片用的Matrix对象
		float scaleWidth = ((float) w / width); // 计算缩放比例
		float scaleHeight = ((float) h / height);
		matrix.postScale(scaleWidth, scaleHeight); // 设置缩放比例
		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
				matrix, true); // 建立新的bitmap，其内容是对原bitmap的缩放后的图
		return new BitmapDrawable(context.getResources(), newbmp); // 把bitmap转换成drawable并返回
	}

	    
	@SuppressLint("NewApi") 
	private static void createDialog(Context context, String msg,
			final CldMessageType type, CldMessageIcon icon, String btnText,
			String packName, CldMessageDialogListener listener) {
		
		cancelMessageDialog();

		mListener = listener;
		
		if (mMessageDialog == null) {
			mMessageDialog = new CldMessageDialog();
			
			//获取LayoutParams对象
			wmParams = new WindowManager.LayoutParams();
	
			//获取的是LocalWindowManager对象
			mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			wmParams.type = LayoutParams.TYPE_PHONE;
			wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;//|LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
			wmParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;   //调整悬浮窗口至左上角，便于调整坐标
			wmParams.alpha = 0.9f;
			wmParams.y = 384;
			wmParams.format = PixelFormat.TRANSPARENT;
			wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
			wmParams.height = 89;
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mFloatLayout = (LinearLayout) inflater.inflate(R.layout.layout_message_dialog, null);
			mWindowManager.addView(mFloatLayout, wmParams);
	    
			TextView tv = (TextView)mFloatLayout.findViewById(R.id.message_title);
			Button btnOk = (Button)mFloatLayout.findViewById(R.id.message_btn_ok);
			ImageView img = (ImageView)mFloatLayout.findViewById(R.id.message_image);
			Button btnCancel = (Button)mFloatLayout.findViewById(R.id.message_btn_cancel);
			
			switch (type) {
			case eMessageType_Ok:
				if (btnCancel != null) {
					btnCancel.setVisibility(View.GONE);
				}
				break;
				
			case eMessageType_Close:			
				if (btnOk != null) {
					btnOk.setVisibility(View.GONE);
				}
				
				break;
				
			case eMessageType_Ok_Close:
				break;
				
			case eMessageType_None:
			default:				
				if (btnOk != null) {
					btnOk.setVisibility(View.GONE);
				}
				
				if (btnCancel != null) {
					btnCancel.setVisibility(View.GONE);
				}
				break;
			}
			
			if (img != null) {
				switch (icon) {
				case eMessageIcon_App:
					if (!packName.isEmpty()) {
						LinearLayout.LayoutParams p = (LinearLayout.LayoutParams)img.getLayoutParams();
						p.leftMargin = 25;
						img.setLayoutParams(p);
						try {
							PackageManager pm = context.getPackageManager();
							ApplicationInfo info = pm.getApplicationInfo(packName, 0);
							img.setImageDrawable(zoomDrawable(context, info.loadIcon(pm), 55, 55));
						} catch (NameNotFoundException e) {
							e.printStackTrace();
						}
			        } 
					break;
					
				case eMessageIcon_Navi:
					img.setImageResource(R.drawable.img_navigation);
					break;
					
				case eMessageIcon_business:
					img.setImageResource(R.drawable.img_business);
					break;
					
				case eMessageIcon_System:
				default:
				}
			}
			
			mMessageDialog.mMsg = msg;
			if (tv != null) {
				if (KCloudCommonUtil.getString(R.string.simcard_tip_check).equals(msg)) {
					tv.setGravity(Gravity.CENTER);
				}
				mMessageDialog.mTv = tv;
				mMessageDialog.mHandler.sendEmptyMessage(1);
			}
			
			if (btnOk != null) {
				btnOk.setText(btnText);
				btnOk.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						if (mListener != null) {
							mListener.onOk();
						}
						
						cancelMessageDialog();
					}
				});
				
				// 有按钮且可见时，点击文本也能执行按钮操作。
				if (btnOk.getVisibility() == View.VISIBLE) {
					tv.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View arg0) {
							if (mListener != null) {
								mListener.onOk();
							}
							
							cancelMessageDialog();
						}
					});
				}
			}
						
			if (btnCancel != null) {
				btnCancel.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						if (mListener != null) {
							mListener.onCancel();
						}
	
						cancelMessageDialog();
					}
				});
			}
		}
	}
}
