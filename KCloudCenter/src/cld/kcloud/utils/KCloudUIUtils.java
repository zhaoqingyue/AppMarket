package cld.kcloud.utils;

import java.util.List;

import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

/**
 * @author sfli
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB) 
public class KCloudUIUtils {
	private static final String TAG = "UIUtils";
	public static int ICON_WIDTH = 55;
	public static int ICON_HEIGHT = 55;
	public static int sIconPadingLeft = 30;

	public static Drawable getOtherAppIcon(Context context,String packageName, int width, int height){
		try {
			PackageManager pm = context.getPackageManager();
			ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
			Drawable appIcon = pm.getDrawable(packageName, ai.icon, ai);
			if(appIcon != null){
				return getScaleSizeDrawable(context, appIcon,
						width == 0 ? ICON_WIDTH : width,
						height == 0 ? ICON_HEIGHT : height);	
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Drawable getOtherAppIcon(Context context,String packageName,String className, int width, int height){
		PackageManager pm = context.getPackageManager();
		  Intent intent = new Intent(Intent.ACTION_MAIN);  
		  intent.addCategory(Intent.CATEGORY_LAUNCHER);
		  List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent,PackageManager.GET_RESOLVED_FILTER);
			for(int i=0;i<resolveInfos.size();i++) {  
				ResolveInfo resolveInfo = resolveInfos.get(i);
				if(resolveInfo.activityInfo.packageName.equals(packageName)
						&& resolveInfo.activityInfo.name.equals(className)){
					Drawable appIcon = resolveInfo.loadIcon(context.getPackageManager());
					if(appIcon != null){
						return getScaleSizeDrawable(context, appIcon,
								width == 0 ? ICON_WIDTH : width,
								height == 0 ? ICON_HEIGHT : height);	
					}
				}
			}
		return null;
	}
	
	/**
	 * get res package image
	 * @param context
	 * @param packageName 第三方包名
	 * @return
	 */
	public static Drawable getResIconDrawable(Context context,String packageName, String className, int width, int height){
		Drawable iconDrawable = null;

		iconDrawable = getAppIcon(context,"zg03_" + packageName,"zg03_" + className);
		if(iconDrawable != null){
			return getScaleSizeDrawable(context, iconDrawable,
					width == 0 ? ICON_WIDTH : width,
					height == 0 ? ICON_HEIGHT : height);	
		}
		return null;
	}
	
	@SuppressLint("DefaultLocale") 
	private static Drawable getAppIcon(Context context, String packageName, String className){
		String name;
		if (packageName != null) {
			name = packageName.replace(".", "_");
			int iconId = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
			if(iconId == 0){
				if(className != null){
					name = className.toLowerCase().replace(".", "_");
					iconId = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
				}
			}
			Log.d(TAG, "getAppIcon name = " + name);
			if(iconId == 0){
				Log.d(TAG, "getAppIcon = null");
				return null;
			}
			return context.getResources().getDrawable(iconId);
		}
		return null;
	}
	
	public static Drawable getScaleSizeDrawable(Context context, Drawable drawable,int scaleSize,int maxSize){
		return new BitmapDrawable(context.getResources(), getScaleSizeBitmap(((BitmapDrawable)drawable).getBitmap(), scaleSize, maxSize));
	}
	
	public static Bitmap getScaleSizeBitmap(Bitmap bitmap,int scaleSize,int maxSize){
		Bitmap sBitmap = scaleBitmap(bitmap, scaleSize, scaleSize);
		
		Bitmap resizedBitmap = Bitmap.createBitmap(maxSize,maxSize, Config.ARGB_8888);
		Canvas canvas = new Canvas(resizedBitmap);

		canvas.drawBitmap(sBitmap, (maxSize - scaleSize)/2, 0,null);
		canvas.setBitmap(null);

		if(sBitmap != bitmap){
			sBitmap.recycle();
		}
		
		return resizedBitmap;
	}
	
	 private static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight) {
		int oldWidth = bitmap.getWidth();
		int oldHeight = bitmap.getHeight();

		// calculate the scale
		float scaleWidth = (float) newWidth / oldWidth;
		float scaleHeight = (float) newHeight / oldHeight;

		// create a matrix for the manipulation
		Matrix matrix = new Matrix();

		// resize the Bitmap
		matrix.postScale(scaleWidth, scaleHeight);

		// create the new Bitmap object
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, oldWidth,
				oldHeight, matrix, true);

		return resizedBitmap;
	}
	
	public static void viewXMotion(final View view,int target,AnimatorListener listener){
		int startX = (int) view.getTranslationX();
		ValueAnimator animator = ValueAnimator.ofInt(startX,target).setDuration(300);
		animator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int value = (Integer) animation.getAnimatedValue();
				view.setTranslationX(value);
				view.requestLayout();
			}
		});
		if(listener != null)
			animator.addListener(listener);
		animator.start();
	}
	
	public static void viewAlphaMotion(final View view,float target,AnimatorListener listener){
		float startAlpha = view.getAlpha();
		ValueAnimator animator = ValueAnimator.ofFloat(startAlpha,target).setDuration(300);
		animator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float value = (Float) animation.getAnimatedValue();
				view.setAlpha(value);
				view.requestLayout();
			}
		});
		if(listener != null)
			animator.addListener(listener);
		animator.start();
	}
	
	public static void viewYMotion(final View view,int target,AnimatorListener listener){
		int startY = (int) view.getTranslationY();
		ValueAnimator animator = ValueAnimator.ofInt(startY,target).setDuration(400);
		animator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int value = (Integer) animation.getAnimatedValue();
				//Log.d("ZGLauncherMenu", "Y = "+value);
				view.setTranslationY(value);
				view.requestLayout();
			}
		});
		if(listener != null)
			animator.addListener(listener);
		//animator.setInterpolator(new DecelerateInterpolator());
		animator.start();
	}
	
	@SuppressWarnings("deprecation")
	public static int getWinWidth(Context context){
		return ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).
				getDefaultDisplay().getWidth();
	}
	
	@SuppressWarnings("deprecation")
	public static int getWinHeight(Context context){
		return ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).
				getDefaultDisplay().getHeight();
	}
	
	public static String getApkVersion(Context context, String packageName){
		PackageManager pm = context.getPackageManager();
		PackageInfo info;
		try {
			info = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
			if (info != null) {
				return info.versionName;
			} 
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return "";
	}
}
