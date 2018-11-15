package cld.kmarcket;

import android.app.Application;
import android.content.Context;
import cld.kmarcket.service.RemoteService;
import cld.kmarcket.util.CommonUtil;
import cld.kmarcket.util.LauncherUtil;
import cld.kmarcket.util.LocationUtils;
import cld.kmarcket.util.ShareUtil;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.tencent.bugly.crashreport.CrashReport;

public class KMarcketApplication extends Application 
{
	static Context mContext = null;

	@Override
	public void onCreate() 
	{
		super.onCreate();
		mContext = getApplicationContext();				
		initImageLoaderService(mContext);
		LocationUtils.init();
		RemoteService.getInstance().bindService(mContext);
		CommonUtil.init();
		LauncherUtil.init();
		//调用众鸿的静音接口，需要先初始化
		ShareUtil.init();
		CrashReport.initCrashReport(getApplicationContext(), "900030969", false);
	}

	public static Context getContext()
	{
		return mContext;
	}
	
	/**
	 * 
	 * @Title: initImageLoaderService
	 * @Description: 初始化图片下载服务
	 * @param context
	 * @return: void
	 */
	public static void initImageLoaderService(Context context)
	{
		if(!ImageLoader.getInstance().isInited())
		{
			initImageLoader(context.getApplicationContext());
		}
	}
	
	public static void initImageLoader(Context context) 
	{
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.discCacheSize(1024 * 1024 * 10)          //SD卡缓存最大值10MB  
				.discCacheFileCount(100)                  //缓存的File数量  
				.memoryCacheSize(1024 * 1024 * 10)        //内存缓存的最大值  10MB
				.threadPriority(Thread.NORM_PRIORITY - 2) //线程优先级  
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator()) //将保存的URI名称用MD5 加密  
				.tasksProcessingOrder(QueueProcessingType.LIFO)
			/*	.memoryCache(new WeakMemoryCache())*/
				.build();
		ImageLoader.getInstance().init(config);
	}
	
	/**
	 * 
	 * @Title: unInit
	 * @Description: 反初始化图片下载服务,清除缓存
	 * @return: void
	 */
	public static void unInitImageLoaderService()
	{
		if(ImageLoader.getInstance() != null)
		{
			ImageLoader.getInstance().clearMemoryCache();
			//ImageLoader.getInstance().clearDiscCache();
		}	
	}
}
