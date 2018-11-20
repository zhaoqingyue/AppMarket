package cld.kcloud.custom.bean;

public class KCloudCarInfo {
	public String brand;	    // Ʒ��
	public String car_model;	// ����
	public String series;	    // ��ϵ
	public String plate_num;	// ���ƺ�
	public String frame_num;    // ���ܺź�6λ
	public String engine_num;	// �������ź� 6λ
	
	public static enum CarInfoTaskEnum{
		eSeries,
		ePlateNum,
		eFrameNum,
		eEngineNum,
		eAll,
	}
	
	// �����û���Ϣ��ʶ��0:��ϵ, 1:����, 2:���ܺ�, 3:��������
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
		//this.plate_num = "";	�������Ϊ��
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
