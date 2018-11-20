package cld.navi.position.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import cld.kcloud.custom.manager.KCloudPositionManager;
import cld.navi.position.data.LocData;

public class ReadGpsLogThread extends Thread {

	public static int READ_LOG_ACTIVE = 300;
	public static int READ_LOG_START = READ_LOG_ACTIVE+1;
	public static int READ_LOG_QUIT = READ_LOG_START +1;
	public static long internalTime = 1000;
	
	//	public static String regXy = ".*onLocationChanged.*Longitude.*Latitude.*";
	//	public static String regXyGet = "\\d{1,3}\\.\\d+";
	//	
	//	public static String regutcTime = ".*onLocationChanged.*utcTime.*";
	//	public static String regutcTimeGet = "\\d{5,}";
	//	
	//	public static String regAltitude = ".*onLocationChanged.*Altitude.*";
	//	public static String regBearing = ".*onLocationChanged.*Bearing.*";
	//	public static String regSpeed = ".*onLocationChanged.*Speed.*";
	//	public static String regCommGet = "\\d\\.\\d+";


	//正则表达解析日志
	private Pattern pXy,pUtc,pAltitude,pBearing,pSpeed;
	private Pattern pXyGet,pUtcGet,pCommGet;

	//获取经纬度
	public static String regXYGet = "\\d{1,3}\\.\\d{1,}";
	public static String regXY = ".*onLocationChanged.*Longitude,Latitude.*";

	//获取UTC时间
	public static String regUTC = ".*onLocationChanged.*utcTime";
	public static String regUTCGet = "\\d{5,}";


	//获取高度
	public static String regAltitude = ".*onLocationChanged.*Altitude";
	public static String regAltitudeGet = "\\d*\\.\\d*";

	//获取方向
	public static String regBearing =  ".*onLocationChanged.*Bearing";
	public static String regBearingGet= "\\d*\\.\\d*";

	//获取速度
	public static String regSpeed = ".*onLocationChanged.*Speed";
	public static String regSpeedGet= "\\d*\\.\\d*";

	public static String regCommGet = "\\d*\\.\\d*";



	private boolean isActive = false;
	private int mRecorRate = 10;

	public String logPath = "log_out.txt";
	public ReadLogHandler mReadLogHandler= null;
	private CollectGPSThread mCollectGPSThread;
	public ReadGpsLogThread(CollectGPSThread collectGPSThread,int rate){
		mCollectGPSThread = collectGPSThread;
		mRecorRate = rate;
	}


	@Override
	public void run() {
		Looper.prepare();
		mReadLogHandler = new ReadLogHandler();
		mReadLogHandler.sendEmptyMessage(READ_LOG_ACTIVE);
		Looper.loop();
	}

	public class ReadLogHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == READ_LOG_ACTIVE) {
				isActive = true;//线程激活
				//经纬度匹配模式
				pXy = Pattern.compile(regXY);
				pXyGet = Pattern.compile(regXYGet);

				//时间匹配模式
				pUtc = Pattern.compile(regUTC);
				pUtcGet = Pattern.compile(regUTCGet);

				//高度、方向、速度匹配模式
				pAltitude = Pattern.compile(regAltitude);
				pBearing = Pattern.compile(regBearing);
				pSpeed = Pattern.compile(regSpeed);

				//高度、方向、速度所要提取的数字的公共匹配模式
				pCommGet = Pattern.compile(regCommGet);
				logPath = KCloudPositionManager.getInstance().getPath()+File.separator+"log_out.txt";

				Log.i("fangwx", "logPath:"+logPath);

				//开始跑日志
				mReadLogHandler.sendEmptyMessage(READ_LOG_START);

			} else if (msg.what == READ_LOG_START) {
				File logFile = new File(logPath);
				Log.w("js", "Log-----------------------------------------------");
				if (!logFile.exists())
					return;


				try {
					int x=0,y=0,speed=0,time=0,ruid = -1;
					short derection=0,high=0;
					FileReader fReader = new FileReader(logFile);
					BufferedReader bufReader = new BufferedReader(fReader);
					if (bufReader != null) {
						String line = bufReader.readLine();
						long pretime = System.currentTimeMillis();
						long curtime = pretime;
						int  count = 0;
						while (isActive&&(line!=null)) {

							if(pXy.matcher(line).find())//经纬度解析
							{
								Matcher mMatcher = pXyGet.matcher(line);
								if(mMatcher.find()){
									String xStr = mMatcher.group(0);
									Double xDouble = Double.valueOf(xStr);
									x=(int)(xDouble.doubleValue()*3600000); 
									Log.w("js", "x=========="+x);
								}

								if(mMatcher.find())
								{
									String yStr = mMatcher.group(0);
									Double yDouble = Double.valueOf(yStr);
									y=(int)(yDouble.doubleValue()*3600000);
									Log.w("js", "y=========="+y);
								}

							}
							else if(pUtc.matcher(line).find())//utc时间解析
							{
								Matcher mMatcher = pUtcGet.matcher(line);
								if(mMatcher.find())
								{
									String utctime = mMatcher.group(0);
									long mTime = Long.valueOf(utctime).longValue();
									time = (int)(mTime/1000);
									time = (int)(System.currentTimeMillis()/1000);//由于web地图服务端只支持当天的时间，所以改用跑日志时的系统时间。
									Log.w("js", "time=========="+time);
								}
							}
							else if(pAltitude.matcher(line).find())//高度
							{
								Matcher mMatcher = pCommGet.matcher(line);
								if(mMatcher.find()){
									String strAltitude =  mMatcher.group(0);
									Log.w("js", "strAltitude=========="+strAltitude);
									Float floatAltitude = Float.valueOf(strAltitude);
									high = (short)floatAltitude.intValue();

									Log.w("js", "high=========="+high);
								}
							}
							else if(pBearing.matcher(line).find())//方向
							{
								Matcher mMatcher = pCommGet.matcher(line);
								if(mMatcher.find()){
									String strBearing =  mMatcher.group(0);
									Float floatBearing = Float.valueOf(strBearing);
									derection = (short)floatBearing.intValue();
									Log.w("js", "derection=========="+derection);
								}
							}
							else if(pSpeed.matcher(line).find())//速度
							{
								Matcher mMatcher = pCommGet.matcher(line);
								if(mMatcher.find())
								{
									String strSpeed =  mMatcher.group(0);
									Float floatSpeed = Float.valueOf(strSpeed);
									float lastSpeed = floatSpeed.floatValue()*3600;
									speed = (int)lastSpeed;
									curtime = System.currentTimeMillis();
									if(curtime - pretime < internalTime)
										logSleep(internalTime-(curtime - pretime));
									else logSleep(internalTime);
									pretime = System.currentTimeMillis();
									count++;
									if(count>=mRecorRate)//根据配置频率记录数据
									{
										LocData locdata = new LocData(x, y, speed, high, derection, time, ruid);
										mCollectGPSThread.sendMessage(CollectGPSThread.MSG_GPS_ENTER_QUEUE, locdata);
										count = 0;
									}
								}
							}

							line = bufReader.readLine();
							long threadId = Thread.currentThread().getId();
							Log.w("js", "threadId="+threadId+"line="+line);
						}
						bufReader.close();
						fReader.close();
						//mReadLogHandler.sendEmptyMessage(READ_LOG_QUIT);//跑日志结束，停止线程
						//2015-09-06改为循环跑日志
						Log.i("js-over", "日志跑完了*************************************************************************************");
						mReadLogHandler.sendEmptyMessage(READ_LOG_START);//跑日志结束，停止线程
					}

				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return;
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}

			}
			else if(msg.what == READ_LOG_QUIT)
			{
				isActive = false;
				this.getLooper().quit();
				freeReference();
			}
		}
	}

	/**
	 * 休眠函数
	 */
	public void logSleep(long time){
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * 释放本类中的对象
	 */
	public void freeReference(){
		mCollectGPSThread = null;
		pXy=null;
		pUtc=null;
		pAltitude=null;
		pBearing=null;
		pSpeed=null;
		pXyGet=null;
		pUtcGet=null;
		pCommGet=null;
	}
}
