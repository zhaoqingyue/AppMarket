/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: LocationUtils.java
 * @Prject: KMarcket_M550
 * @Package: cld.kmarcket.util
 * @Description: 定位Utils(需要用到cldbase_v3.0.0.jar)
 * @author: zhaoqy
 * @date: 2016年8月25日 上午9:42:08
 * @version: V1.0
 */

package cld.kmarcket.util;

import cld.kmarcket.KMarcketApplication;
import com.cld.location.CldLocationManager;
import com.cld.location.ICldLocationChangeListener;

public class LocationUtils {

	private static final String TAG = "KCloudLocationUtils";
	private static CldLocationManager mCldLocationMgr = null;
	private static IKCloudLocationListener mLocationListener = null;
	
	public static abstract interface IKCloudLocationListener {
		abstract void onLocation(double latitude, double longtitude);
	}
	
	public static void init() {		
		mCldLocationMgr = CldLocationManager.getInstance();
		mCldLocationMgr.setContext(KMarcketApplication.getContext());
		mCldLocationMgr.setLocationListener(new OnLocationChangeListener());
	}
	
	public static void startLocation(IKCloudLocationListener listener) {
		if (mCldLocationMgr == null) {
			return ;
		}
		
		LogUtil.i(LogUtil.TAG, "listener = " + listener.toString());
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
				LogUtil.i(LogUtil.TAG, String.valueOf(latitude) + ", " +String.valueOf(longtitude));
				
				if (mLocationListener != null) {
					LogUtil.i(LogUtil.TAG, "mLocationListener = " + mLocationListener.toString());
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
