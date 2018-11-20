package cld.navi.position.model;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.TimeZone;

import android.content.Context;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import cld.kcloud.custom.manager.KCloudPositionManager;
import cld.navi.position.data.LocData;
import cld.navi.util.FileUtils;

public class GpsCallBackListen {

	private     Context context=null;
	private     LocationManager lm=null;
	private 	Location location=null;
	
	private     int lLatitude = 0;  //GPS
	private     int lLongtitude = 0;
	private     short lAltitude = 0;
	private     short lBearing = 0;//方向
	private     int lTime = 0;
	private     int lSpeed = 0;
	private     int roadid = -1;
	
	private     final int SCALE = 3600000;
	private     long mLocLastUpdateTime = 0;
	
	private     int number = 0;//卫星个数
    private     int mUseLocSatellite = 0;//有效星颗�?
	private     boolean isProviderEnable = false;
	private     int recordRate = 10;//记录数据的频率（s�?
	private     CollectGPSThread mCollectGPSThread = null;//采集GPS数据的线程�?
	private     Criteria lCriteria = null;
	private     String lLocationProvider = null;
	private     boolean isWriteLog = false;
	
	//GPS状�?监听
	private GpsStatus.Listener statusListener = new GpsStatus.Listener() {
	        public void onGpsStatusChanged(int event) {
	            switch (event) {
	            case GpsStatus.GPS_EVENT_FIRST_FIX:

	                break;
                    //卫星状�?改变
	            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:

	                GpsStatus gpsStatus=lm.getGpsStatus(null);
	                int maxSatellites = gpsStatus.getMaxSatellites();
	                Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
	                int count = 0;   

	                while (iters.hasNext() && count <= maxSatellites) {     
	                    GpsSatellite s = iters.next();
	                    if(s.usedInFix())
	                    	mUseLocSatellite++;
	                    count++;     
	                }   
	                number=count;
	                break;
	            case GpsStatus.GPS_EVENT_STARTED:
	                break;
	            case GpsStatus.GPS_EVENT_STOPPED:
	                break;
	            }
	        };
	    };
	    
	    
//位置改变回调
	 private LocationListener locationListener = new LocationListener() {

	 		public void onLocationChanged(Location location) {

	 			//位置改变回调，我们做的工作主要在这里�?
	 			/********************************************************/
	 			Calendar stdC = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
				stdC.setTimeInMillis(location.getTime());
				//stdC.getTimeInMillis();
				FileUtils.logOut("[onLocationChanged]-->utcTime="
							+ String.valueOf(location.getTime()), isWriteLog);
				FileUtils.logOut("[onLocationChanged]-->locTime=["
							+ stdC.get(Calendar.YEAR) + ","
							+ (stdC.get(Calendar.MONTH) + 1) + ","
							+ stdC.get(Calendar.DAY_OF_MONTH) + ","
							+ stdC.get(Calendar.HOUR_OF_DAY) + ","
							+ stdC.get(Calendar.MINUTE) + ","
							+ stdC.get(Calendar.SECOND) + ","
							+ stdC.get(Calendar.MILLISECOND) + "]", isWriteLog);
				FileUtils.logOut("[onLocationChanged]-->(Longitude,Latitude)=("
						+ String.valueOf(location.getLongitude()) + ","
						+ String.valueOf(location.getLatitude()) + ")", isWriteLog);
				FileUtils.logOut("[onLocationChanged]-->hasAltitude="
						+ String.valueOf(location.hasAltitude())
						+ ", Altitude="
						+ String.valueOf(location.getAltitude()), isWriteLog);
				FileUtils.logOut("[onLocationChanged]-->hasBearing="
			  			+ String.valueOf(location.hasBearing()) + ", Bearing="
						+ String.valueOf(location.getBearing()), isWriteLog);
				FileUtils.logOut("[onLocationChanged]-->hasSpeed="
						+ String.valueOf(location.hasSpeed()) + ", Speed="
						+ String.valueOf(location.getSpeed()), isWriteLog);

	 			/******************************************************/
						lLatitude = convertCoorinate(location.getLatitude());
						lLongtitude = convertCoorinate(location.getLongitude());//(int)location.getLongitude()*SCALE;
						lAltitude = (short)(location.getAltitude());
						lSpeed = (int)((location.hasSpeed() == true ? location.getSpeed() : 0.0f)*3600);//m/h
						lBearing = (short)(location.hasBearing() == true ? location.getBearing() : 0.0f);
						lTime =  (int)(location.getTime()/1000);//得转换为�?
						roadid = -1;
					    long currentTime = System.currentTimeMillis();
					    if(currentTime-mLocLastUpdateTime >= recordRate*1000)//根据服务器配置的频率要求记录�?
					    {
					    	LocData data = new LocData(lLongtitude,lLatitude, lSpeed, lAltitude, lBearing, lTime, roadid);
					    	if(mCollectGPSThread!=null)
					    	{
					    		mCollectGPSThread.sendMessage(CollectGPSThread.MSG_GPS_ENTER_QUEUE, data);

						        mLocLastUpdateTime = currentTime;
					         }
			             }
	 		}

	 		public void onStatusChanged(String provider, int status, Bundle extras) {
	 			switch (status) {
	 			case LocationProvider.AVAILABLE:
	 				break;
	 			case LocationProvider.OUT_OF_SERVICE:
	 				break;
	 			case LocationProvider.TEMPORARILY_UNAVAILABLE:
	 				break;
	 			}
	 		}

			@Override
			public void onProviderDisabled(String provider) {
				isProviderEnable = false;
			}

			@Override
			public void onProviderEnabled(String provider) {
				isProviderEnable = true;
			}
	 	};
	
	//构造函数
	public  GpsCallBackListen(Context context,int recordrate, CollectGPSThread collectGPSThread){
		this.context=context;
		recordRate = recordrate;//频率，需要从外面传进来，这个变量应该是每次服务启动的时候从服务器上取，应该要缓存在本地，取失败就用本地的�?
	    mCollectGPSThread = collectGPSThread;//采集GPS数据的线// 其实这里应该用个弱引用TODO
		isWriteLog = KCloudPositionManager.getInstance().getIsWriteLog();
		lCriteria = new Criteria(); 
		lCriteria.setAccuracy(Criteria.ACCURACY_FINE);
		lCriteria.setAltitudeRequired(true);
		lCriteria.setBearingRequired(true);
		lCriteria.setSpeedRequired(true);
	}
	
	//GPS注册
	public int registerGPS() {
		lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		if(lm==null)
			return -2;//没有注册
        
		 if(lLocationProvider==null)
		 {
			 lLocationProvider = lm.getBestProvider(lCriteria, true);
			 if(lLocationProvider == null )lLocationProvider = LocationManager.GPS_PROVIDER;
			 if(lLocationProvider!=null)
			 {
		         if(statusListener!=null && locationListener!=null){
		        	 lm.addGpsStatusListener(statusListener);
	            	 lm.requestLocationUpdates(lLocationProvider, 1000, 0,
	 						locationListener);
	            	 return 0;//正常注册
		         }
			 }
		 }
       return -1;
	}
    //GPS注销
 	public void unRegisterGPS(){
          
 		if(lm!=null){
 	 		lm.removeGpsStatusListener(statusListener);
 	        lm.removeUpdates(locationListener);	
 		}
 		lm = null;

 	}
 	
 	
 	//按照协议文档要求进行经纬度的转换
 	public int convertCoorinate(double data)
 	{
 		int result;
 		result = (int)(data*SCALE);
 		return result;

 	}
}
