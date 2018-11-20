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


	//�����������־
	private Pattern pXy,pUtc,pAltitude,pBearing,pSpeed;
	private Pattern pXyGet,pUtcGet,pCommGet;

	//��ȡ��γ��
	public static String regXYGet = "\\d{1,3}\\.\\d{1,}";
	public static String regXY = ".*onLocationChanged.*Longitude,Latitude.*";

	//��ȡUTCʱ��
	public static String regUTC = ".*onLocationChanged.*utcTime";
	public static String regUTCGet = "\\d{5,}";


	//��ȡ�߶�
	public static String regAltitude = ".*onLocationChanged.*Altitude";
	public static String regAltitudeGet = "\\d*\\.\\d*";

	//��ȡ����
	public static String regBearing =  ".*onLocationChanged.*Bearing";
	public static String regBearingGet= "\\d*\\.\\d*";

	//��ȡ�ٶ�
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
				isActive = true;//�̼߳���
				//��γ��ƥ��ģʽ
				pXy = Pattern.compile(regXY);
				pXyGet = Pattern.compile(regXYGet);

				//ʱ��ƥ��ģʽ
				pUtc = Pattern.compile(regUTC);
				pUtcGet = Pattern.compile(regUTCGet);

				//�߶ȡ������ٶ�ƥ��ģʽ
				pAltitude = Pattern.compile(regAltitude);
				pBearing = Pattern.compile(regBearing);
				pSpeed = Pattern.compile(regSpeed);

				//�߶ȡ������ٶ���Ҫ��ȡ�����ֵĹ���ƥ��ģʽ
				pCommGet = Pattern.compile(regCommGet);
				logPath = KCloudPositionManager.getInstance().getPath()+File.separator+"log_out.txt";

				Log.i("fangwx", "logPath:"+logPath);

				//��ʼ����־
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

							if(pXy.matcher(line).find())//��γ�Ƚ���
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
							else if(pUtc.matcher(line).find())//utcʱ�����
							{
								Matcher mMatcher = pUtcGet.matcher(line);
								if(mMatcher.find())
								{
									String utctime = mMatcher.group(0);
									long mTime = Long.valueOf(utctime).longValue();
									time = (int)(mTime/1000);
									time = (int)(System.currentTimeMillis()/1000);//����web��ͼ�����ֻ֧�ֵ����ʱ�䣬���Ը�������־ʱ��ϵͳʱ�䡣
									Log.w("js", "time=========="+time);
								}
							}
							else if(pAltitude.matcher(line).find())//�߶�
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
							else if(pBearing.matcher(line).find())//����
							{
								Matcher mMatcher = pCommGet.matcher(line);
								if(mMatcher.find()){
									String strBearing =  mMatcher.group(0);
									Float floatBearing = Float.valueOf(strBearing);
									derection = (short)floatBearing.intValue();
									Log.w("js", "derection=========="+derection);
								}
							}
							else if(pSpeed.matcher(line).find())//�ٶ�
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
									if(count>=mRecorRate)//��������Ƶ�ʼ�¼����
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
						//mReadLogHandler.sendEmptyMessage(READ_LOG_QUIT);//����־������ֹͣ�߳�
						//2015-09-06��Ϊѭ������־
						Log.i("js-over", "��־������*************************************************************************************");
						mReadLogHandler.sendEmptyMessage(READ_LOG_START);//����־������ֹͣ�߳�
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
	 * ���ߺ���
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
	 * �ͷű����еĶ���
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
