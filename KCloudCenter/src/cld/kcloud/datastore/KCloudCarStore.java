package cld.kcloud.datastore;

import android.annotation.SuppressLint;
import cld.kcloud.custom.bean.KCloudCarInfo;

public class KCloudCarStore {
	
	private static KCloudCarStore mKCloudCarStore = null;
	private KCloudCarInfo mKCloudCarInfo = new KCloudCarInfo();			// 用户车辆信息
	private KCloudCarInfo mKCloudCarInfoTmp = new KCloudCarInfo();		// 临时存储，用于判断是否更新
	
	public static KCloudCarStore getInstance() {
		if (mKCloudCarStore == null) {
			synchronized (KCloudCarStore.class) {
				if (mKCloudCarStore == null) {
					mKCloudCarStore = new KCloudCarStore();
				}
			}
		}
		return mKCloudCarStore;
	}
	
	/**
	 * 添加车辆信息
	 * @param info
	 */
	public void addCarInfo(KCloudCarInfo info) {
		mKCloudCarInfo.assignVaule(info);
		
		mKCloudCarInfoTmp.brand = info.brand;
		mKCloudCarInfoTmp.car_model = info.car_model;
		mKCloudCarInfoTmp.series = info.series;
		mKCloudCarInfoTmp.plate_num = info.plate_num;	// 车牌号先赋值，更新时有用
		mKCloudCarInfoTmp.frame_num = info.frame_num;
		mKCloudCarInfoTmp.engine_num = info.engine_num;
	}
	
	/**
	 * 更新车辆信息
	 */
	public void update() {
		int[] status = mKCloudCarInfoTmp.getChangeStatus(); 
		
		if (status[0] == 1) {
			mKCloudCarInfo.setSeries(mKCloudCarInfoTmp.brand, mKCloudCarInfoTmp.car_model, mKCloudCarInfoTmp.series);
		} else if (status[1] == 1) {
			mKCloudCarInfo.setPlateNum(mKCloudCarInfoTmp.plate_num);
		} else if (status[2] == 1) {
			mKCloudCarInfo.setFrameNum(mKCloudCarInfoTmp.frame_num);
		} else if (status[3] == 1) {
			mKCloudCarInfo.setEngineNum(mKCloudCarInfoTmp.engine_num);
		}
		
		resetTemp();
	}

	/**
	 * 获取车辆信息
	 * @return
	 */
	public KCloudCarInfo get() {		
		return mKCloudCarInfo;
	}
	
	/**
	 * 
	 * @return
	 */
	@SuppressLint("NewApi") 
	public boolean hasCar() {
		if (!mKCloudCarInfo.brand.isEmpty()
				|| !mKCloudCarInfo.car_model.isEmpty()
				|| !mKCloudCarInfo.series.isEmpty()
				|| !mKCloudCarInfo.plate_num.isEmpty()
				|| !mKCloudCarInfo.frame_num.isEmpty()
				|| !mKCloudCarInfo.engine_num.isEmpty()) {
			return true;
		}

		return false;
	}
	
	/**
	 * 重置临时存储信息
	 */
	public void resetTemp() {
		mKCloudCarInfoTmp.clear();
	}
	
	/**
	 * 获取车辆临时信息
	 * @return
	 */
	public KCloudCarInfo getTemp() {
		return mKCloudCarInfoTmp;
	}
}
