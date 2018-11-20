package cld.kcloud.utils;

import cld.kcloud.center.KCloudCtx;

import com.cld.location.CldLocationManager;
import com.cld.location.ICldLocationChangeListener;
import com.cld.log.CldLog;

public class KCloudLocationUtils {

	private static final String TAG = "KCloudLocationUtils";
	private static CldLocationManager mCldLocationMgr = null;
	private static IKCloudLocationListener mLocationListener = null;
	
	public static abstract interface IKCloudLocationListener {
		abstract void onLocation(double latitude, double longtitude);
	}
	
	public static void init() {		
		mCldLocationMgr = CldLocationManager.getInstance();
		mCldLocationMgr.setContext(KCloudCtx.getAppContext());
		mCldLocationMgr.setLocationListener(new OnLocationChangeListener());
	}
	
	public static void startLocation(IKCloudLocationListener listener) {
		if (mCldLocationMgr == null) {
			return ;
		}
		
		CldLog.i(TAG, "listener = " + listener.toString());
		mLocationListener = listener;
		mCldLocationMgr.startLocation();
	}
	
	public static void stopLocation() {
		if (mCldLocationMgr == null) {
			return ;
		}
		
		mLocationListener = null;
		mCldLocationMgr.stopLocation();
	}
	

	/**
	 * @Description 定位回调
	 */
	private static class OnLocationChangeListener implements ICldLocationChangeListener {

		/**
		 * @see com.cld.navicm.location.CldLocationChangeListener#onLocationChange(int,
		 *      double, double, int, double, float, long, float)
		 */
		@Override
		public void onLocationChange(int locType, double latitude,
				double longtitude, int accuracy, double altitude,
				float bearing, long time, float speed) {
			synchronized (OnLocationChangeListener.class) {
				CldLog.i(TAG, String.valueOf(latitude) + ", " +String.valueOf(longtitude));
				
				if (mLocationListener != null) {
					CldLog.i(TAG, "mLocationListener = " + mLocationListener.toString());
					mLocationListener.onLocation(latitude, longtitude);
				}
				stopLocation();
			}
		}

		/**
		 * @see com.cld.navicm.location.CldLocationChangeListener#onSatelliteStatusChange(int,
		 *      int[], float[], float[], float[])
		 */
		@Override
		public void onSatelliteStatusChange(int mSvCount, int[] mPrns,
				float[] mSnrs, float[] mElev, float[] mAzim) {
		}
	}
	
}
