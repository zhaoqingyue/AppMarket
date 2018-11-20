package cld.kcloud.custom.bean;

public class KCloudAppInfo {

	private int service_code;
	private String app_packname;
	
	public KCloudAppInfo () {
		this.service_code = 0;
		this.app_packname = "";
	}
	
	public KCloudAppInfo (int service_code, String app_packname) {
		this.service_code = service_code;
		this.app_packname = app_packname;
	}
	
	public void setServiceCode(int service_code) {
		this.service_code = service_code;
	}
	
	public int getServiceCode() {
		return service_code;
	}
	
	public void setAppPackName(String app_packname) {
		this.app_packname = app_packname;
	}
	
	public String getAppPackName() {
		return app_packname;
	}
}
