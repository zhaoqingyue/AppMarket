package cld.kcloud.center;

import java.io.File;
import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Environment;
import cld.kcloud.user.KCloudUser;
import com.cld.base.CldBase;
import com.cld.base.CldBaseParam;
import com.cld.db.utils.CldDbUtils;
import com.cld.device.CldPhoneManager;
import com.cld.log.CldLog;
import com.cld.ols.base.CldOlsBase;
import com.cld.ols.base.CldOlsBase.CldOlsBaseParam;
import com.cld.ols.base.CldOlsBase.CldOlsUpdateParam;
import com.cld.ols.base.CldOlsBase.ICldOlsBaseInitListener;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * K云中心入口
 * @author wuyl
 *
 */
public class KCloudCenterApp extends Application {
	
	@Override
	public void onCreate() {

		// 应用程序上下文
		KCloudCtx.setApplication(this);
		KCloudCtx.setAppContext(this);
		
		// 初始化基础库
		CldBaseParam param = new CldBaseParam(); 
		param.ctx = this.getApplicationContext(); 
		CldBase.init(param);
		
		//设置替换接口示例：(替换cldbase_v3.0.0.jar后, 众鸿机器使用)
		CldPhoneManager.setReplaceFunc(new CldPhoneManager.IReplaceFunc() {
			@Override
			public String getImei() {
				// return "12345678";
				return KCloudDevice.getDeviceID();
			}
		});		

		// 先创建数据库
		CldDbUtils.create(2);
		
		// 在线服务初始化 
		CldOlsBaseParam initParam = new CldOlsBaseParam();
		initParam.isTestVersion = KCloudAppUtils.isTestVersion();
		initParam.appid = KCloudAppConfig.appid;
		initParam.apptype = KCloudAppConfig.apptype;
		initParam.bussinessid = KCloudAppConfig.bussinessid;
		initParam.cid = KCloudAppConfig.cid;
		initParam.appPath = KCloudCtx.getAppPath();
		CldOlsBase.getInstance().initBaseEnv(initParam);
		
		CldOlsUpdateParam updateParam = new CldOlsUpdateParam();
		updateParam.appver = KCloudAppConfig.appver;
		updateParam.mapver = KCloudAppConfig.mapver;

		CldOlsBase.getInstance().updateBaseEnv(updateParam, new ICldOlsBaseInitListener() {

			@Override
			public void onInitDuid() {
				
			}

			@Override
			public void onUpdateConfig() {
				
			}
		});
		
		// 创建目录
		createLogDir();
		
		// 日志
		CldLog.setLogEMode(true);
		CldLog.setLogFileName(KCloudCtx.getAppPath()+"/cldlog.txt");

		// 初始化
		KCloudUser.getInstance().init();

		super.onCreate();
		CrashReport.initCrashReport(getApplicationContext(), "900030969", false);
	}

	@Override
	public void onTerminate() {
		KCloudUser.getInstance().uninit();
		super.onTerminate();
	}

	@SuppressLint("SdCardPath") 
	public void createLogDir() {
		String saveDir = "";
		File extFile = Environment.getExternalStorageDirectory();
		if ((extFile.exists()) && (extFile.isDirectory()) && (extFile.canWrite())) {
			saveDir = extFile.getAbsolutePath() + "/KCloudCenter";
		} else {
			saveDir = "/mnt/sdcard/" + "/KCloudCenter";
		}
		
		File file = new File(saveDir); 
		
		if (!file.exists()) {
            file.mkdir();
        }
	}
}
