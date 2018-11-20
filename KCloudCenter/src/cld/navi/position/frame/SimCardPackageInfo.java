package cld.navi.position.frame;

import java.io.Serializable;

public class SimCardPackageInfo implements Serializable{
	/**
	  * @Fields serialVersionUID
	  */
	
	private static final long serialVersionUID = 1L;
	
	int pkid; // 套餐编码
	String pkalias; // 套餐别名（外部显示）
	int pkmonths; // 套餐有效期（单位月数）
	String pktraffic; // 套餐包流量（M或G）
	String pkabletime; // 套餐生效日期（年月日）

	public SimCardPackageInfo() {
		pkid = 0;
		pkalias = "xxxx";
		pkmonths = 4;
		pktraffic = "5M";
		pkabletime = "00:00:00";
	}
	
	public SimCardPackageInfo(SimCardPackageInfo pkInfo) {
		pkid = pkInfo.pkid;
		pkalias = pkInfo.pkalias;
		pkmonths = pkInfo.pkmonths;
		pktraffic = pkInfo.pktraffic;
		pkabletime = pkInfo.pkabletime;
	}
}
