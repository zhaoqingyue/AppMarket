package cld.kcloud.center;

/** 
 * 应用配置文件
 */ 
public class KCloudAppConfig {
	public static final int appid = 24;	      // ols appid
	public static final int apptype = 53;     // ols apptype
	public static final int bussinessid = 12; // ols bussinessid
	public static final int cid = 1020;		  // ols cid
	
	public static final int system_code	= 1;  // 操作系统编码 运营平台定义
	public static final int device_code	= 1;  // 设备型号编码 运营平台定义
	public static final int product_code = 2; // 产品型号编码 运营平台定义
	
	public static final int device_width = 1600;    // 设备宽
	public static final int device_height = 480;	// 设备长
		
	public static final String mapver = "31200B51T0H010A1";
	public static final String appver = "K3659-L5R01-3921D0S";
	
	public static final int custom_id = 1270;	   // 客户ID
	public static final int custom_code = 100100;  // 客户编号    (飞歌：100100； 凯迪：100101)
	
	//可配置
	public static int plan_code = 100100;    // 方案商编号
	public static int device_dpi = 160;      // 设备分辨率
	public static String system_ver = "4.4"; //android系统版本
	
	public static final boolean open_position_port = false; //打开位置上报接口
	public static final boolean open_upgrade_port = false;  //打开应用升级接口
}