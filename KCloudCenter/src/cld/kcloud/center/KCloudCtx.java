package cld.kcloud.center;

import java.io.File;
import com.cld.setting.CldSetting;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

/**
 * K�����Ļ����������
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
	//����·��
	private static final String ENV_PATH = "kcloud_path";
	// app path
	private static String mAppPath = "";
	
	/**
	 * ��ȡContext
	 * @return Context
	 */
	public static Context getAppContext() {
		return mAppCtx;
	}

	/**
	 * ����Context
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
	 * ��ȡ����·��
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
	 * ���ò����ļ�·��
	 * @return String
	 */
	public static void setAppPath(String appPath) {
		mAppPath = appPath;	
		if (!TextUtils.isEmpty(appPath)) {
			CldSetting.put(ENV_PATH, appPath);
		}
	}
	
	/**
	 * @Description ��ȡ�û������ļ�·��
	 * @return String
	 */
	public static String getAppParamFilePath() {
		return getAppPath() + "/" + USER_PARAM_DIR;
	}
}
