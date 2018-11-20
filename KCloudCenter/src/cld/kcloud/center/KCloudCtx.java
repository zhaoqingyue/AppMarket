package cld.kcloud.center;

import java.io.File;
import com.cld.setting.CldSetting;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

/**
 * K云中心环境变量相关
 * @author wuyl
 */
public class KCloudCtx {
	// app context
	private static Context mAppCtx = null;
	private static Application mApplication;
	// app directory
	private static final String DEFAULT_DIR = "KCloudCenter";
	// param path
	private static final String USER_PARAM_DIR = "KCloudUserParam";
	//导航路径
	private static final String ENV_PATH = "kcloud_path";
	// app path
	private static String mAppPath = "";
	
	/**
	 * 获取Context
	 * @return Context
	 */
	public static Context getAppContext() {
		return mAppCtx;
	}

	/**
	 * 设置Context
	 * @param ctx
	 */
	public static void setAppContext(Context ctx) {
		mAppCtx = ctx;
	}
	
	public static Application getApplication() {
		return mApplication;
	}
	
	/**
	 * @param application
	 */
	public static void setApplication(Application application) {
		mApplication = application;
	}
	
	/**
	 * 获取程序路径
	 * @return String
	 */
	@SuppressLint("SdCardPath") 
	public static String getAppPath() {
		if (!TextUtils.isEmpty(mAppPath)) {
			return mAppPath;
		}
		
		File extFile = Environment.getExternalStorageDirectory();
		if ((extFile.exists()) && (extFile.isDirectory()) && (extFile.canWrite())) {
			mAppPath = extFile.getAbsolutePath() + "/" + DEFAULT_DIR;
		} else {
			mAppPath = "/mnt/sdcard/" + DEFAULT_DIR;
		}
		
		setAppPath(mAppPath);
		return mAppPath;
	}
	
	/**
	 * 设置参数文件路径
	 * @return String
	 */
	public static void setAppPath(String appPath) {
		mAppPath = appPath;	
		if (!TextUtils.isEmpty(appPath)) {
			CldSetting.put(ENV_PATH, appPath);
		}
	}
	
	/**
	 * @Description 获取用户参数文件路径
	 * @return String
	 */
	public static String getAppParamFilePath() {
		return getAppPath() + "/" + USER_PARAM_DIR;
	}
}
