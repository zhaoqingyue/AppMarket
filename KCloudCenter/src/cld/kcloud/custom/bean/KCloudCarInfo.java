package cld.kcloud.custom.bean;

public class KCloudCarInfo {
	public String brand;	    // 品牌
	public String car_model;	// 车型
	public String series;	    // 车系
	public String plate_num;	// 车牌号
	public String frame_num;    // 车架号后6位
	public String engine_num;	// 发动机号后 6位
	
	public static enum CarInfoTaskEnum{
		eSeries,
		ePlateNum,
		eFrameNum,
		eEngineNum,
		eAll,
	}
	
	// 更改用户信息标识，0:车系, 1:车牌, 2:车架号, 3:发动机号
	public int[] infoChange = { 0, 0, 0, 0 };
	
	public KCloudCarInfo() {
		brand = "";
		car_model = "";
		series = "";
		plate_num = "";
		frame_num = "";
		engine_num = "";
	}
	
	public KCloudCarInfo(KCloudCarInfo info) {
		this.brand = info.brand;
		this.car_model = info.car_model;
		this.series = info.series;
		this.plate_num = info.plate_num;
		this.frame_num = info.frame_num;
		this.engine_num = info.engine_num;
	}
	
	public void assignVaule(KCloudCarInfo info) {
		this.brand = info.brand;
		this.car_model = info.car_model;
		this.series = info.series;
		this.plate_num = info.plate_num;
		this.frame_num = info.frame_num;
		this.engine_num = info.engine_num;
	}
	
	public int[] getChangeStatus() {
		return infoChange;
	}
	
	/**
	 * 
	 * @param eTask
	 */
	public void resetChangeStatus(CarInfoTaskEnum eTask) {
		switch (eTask) {
		case eSeries:
			infoChange[0] = 0;
			break;
			
		case ePlateNum:
			infoChange[1] = 0;
			break;
			
		case eFrameNum:
			infoChange[2] = 0;
			break;
			
		case eEngineNum:
			infoChange[3] = 0;
			break;
			
		default:
			infoChange[0] = 0;
			infoChange[1] = 0;
			infoChange[2] = 0;
			infoChange[3] = 0;
			break;
		}
	}
	/**
	 * 
	 * @param eTask
	 */
	public void setChangeStatus(CarInfoTaskEnum eTask) {
		switch (eTask) {
		case eSeries:
			infoChange[0] = 1;
			break;
			
		case ePlateNum:
			infoChange[1] = 1;
			break;
			
		case eFrameNum:
			infoChange[2] = 1;
			break;
			
		case eEngineNum:
			infoChange[3] = 1;
			break;
			
		default:
			break;
		}
	}
	
	public void clear() {
		//this.brand = "";
		//this.car_model = "";
		//this.series = "";
		//this.plate_num = "";	这个不能为空
		//this.frame_num = "";
		//this.engine_num = "";
		resetChangeStatus(CarInfoTaskEnum.eAll);
	}
	
	public void setSeries(String brand, String car_model, String series) {
		setChangeStatus(CarInfoTaskEnum.eSeries);
		this.brand = brand;
		this.car_model = car_model;
		this.series = series;
	}
	
	public void setPlateNum(String plate_num) {
		setChangeStatus(CarInfoTaskEnum.ePlateNum);
		this.plate_num = plate_num;
	}
	
	public void setFrameNum(String frame_num) {
		setChangeStatus(CarInfoTaskEnum.eFrameNum);
		this.frame_num = frame_num;
	}
	
	public void setEngineNum(String engine_num) {
		setChangeStatus(CarInfoTaskEnum.eEngineNum);
		this.engine_num = engine_num;
	}
}
