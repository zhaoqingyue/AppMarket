package cld.kcloud.custom.bean;

public class KCloudServiceInfo {
	private int combo_code;			// 套餐编码
	private int combo_status;       // 套餐状态
	private int service_code;		// 服务编码
	private String service_name;	// 服务名称
	private int month;				// 服务期限（月）
	private int charge;				// 费用（元）
	private String service_desc;	// 服务详情
	private int service_status;		// 服务状态1:已开通; 2:即将到期; 3:已到期
	private String service_icon;	// 图片地址
	
	public KCloudServiceInfo() {
		this.combo_code = 0;
		this.combo_status = 0;
		this.service_code = 0;
		this.service_name = "";
		this.service_desc = "";
		this.service_icon = "";
		this.service_status = 0;
		this.month = 0;
		this.charge = 0;
	}
	
	public KCloudServiceInfo(KCloudServiceInfo serviceInfo) {
		this.combo_code = serviceInfo.combo_code;
		this.combo_status = serviceInfo.combo_status;
		this.service_code = serviceInfo.service_code;
		this.service_icon = serviceInfo.service_icon;
		this.service_name = serviceInfo.service_name;
		this.service_desc = serviceInfo.service_desc;
		this.service_status = serviceInfo.service_status;
		this.month = serviceInfo.month;
		this.charge = serviceInfo.charge;
	}
	
	public KCloudServiceInfo(int combo_code, int combo_status, int service_code, 
			String service_icon, String service_name, String service_desc, 
			int month, int charge, int service_status) {
		this.combo_code = combo_code;
		this.combo_status = combo_status;
		this.service_code = service_code;
		this.service_icon = service_icon;
		this.service_name = service_name;
		this.service_desc = service_desc;
		this.service_status = service_status;
		this.month = month;
		this.charge = charge;
	}
	
	public void setComboCode(int combo_code) {
		this.combo_code = combo_code;
	}
	
	public int getComboCode() {
		return this.combo_code;
	}
	
	public void setComboStatus(int combo_status) {
		this.combo_status = combo_status;
	}
	
	public int getComboStatus() {
		return this.combo_status;
	}
	
	public void setServiceCode(int service_code) {
		this.service_code = service_code;
	}
	
	public int getServiceCode() {
		return this.service_code;
	}
	
	public void setServiceIcon(String service_icon) {
		this.service_icon = service_icon;
	}
	
	public String getServiceIcon() {
		return this.service_icon;
	}
	
	public void setServiceName(String service_name) {
		this.service_name = service_name;
	}
	
	public String getServiceName() {
		return this.service_name;
	}
	
	public void setServiceDesc(String service_desc) {
		this.service_desc = service_desc;
	}
	
	public String getServiceDesc() {
		return this.service_desc;
	}
	
	public void setServiceStatus(int service_status) {
		this.service_status = service_status;
	}
	
	public int getServiceStatus() {
		return this.service_status;
	}
	
	public void setServiceMonth(int month) {
		this.month = month;
	}
	
	public int getServiceMonth() {
		return this.month;
	}
	
	public void setServiceCharge(int charge) {
		this.charge = charge;
	}
	
	public int getServiceCharge() {
		return this.charge;
	}
}