package cld.kmarket.install;

public class InstallerInter 
{
	/**
	 * 发送给APP广播
	 * ACTION_INSTALL_START: 安装开始
	 * ACTION_INSTALL_PROGRESS: 安装进度
	 * ACTION_INSTALL_COMPLETE: 安装完成，判断返回值
	 * ACTION_DELETE_START: 卸载开始
	 * ACTION_DELETE_PROGRESS: 卸载进度
	 * ACTION_DELETE_COMPLETE: 卸载完成，判断返回值
	 */
	public static final String ACTION_INSTALL_START = "cld.installer.INSTALL_START";
	public static final String ACTION_INSTALL_PROGRESS = "cld.installer.INSTALL_PROGRESS";
	public static final String ACTION_INSTALL_COMPLETE = "cld.installer.INSTALL_COMPLETE";
	public static final String ACTION_DELETE_START = "cld.installer.DELETE_START";
	public static final String ACTION_DELETE_PROGRESS = "cld.installer.DELETE_PROGRESS";
	public static final String ACTION_DELETE_COMPLETE = "cld.installer.DELETE_COMPLETE";
	public static final String ACTION_INSTALL_VERSION = "cld.installer.INSTALL_VERSION";
	
	/**
	 * 广播action接收数据
	 */
	public static final String APP_NAME = "app_name";				  
	public static final String PACKAGE_NAME = "package_name";		  
	public static final String CURRENT_PROGRESS = "current_progress"; 
	public static final String TOTAL_PROGRESS = "total_progress";	  
	public static final String RET_CODE = "ret_code";				  
	public static final String VERSION_NUMBER = "version_number";	 

	/**
	 * 安装返回值类型，其它都是错误 ret_code返回值类型
	 * INSTALL_SUCCEEDED: 安装成功
	 * INSTALL_FAILED_INSUFFICIENT_STORAGE: 空间不足
	 */
	public static final int INSTALL_SUCCEEDED = 1;					  
	public static final int INSTALL_FAILED_INSUFFICIENT_STORAGE = -4; 
	
	/**
	 * 卸载返回值类型，其它都是错误 ret_code返回值类型
	 * DELETE_SUCCEEDED: 卸载成功
	 * DELETE_FAILED_DEVICE_POLICY_MANAGER: 系统应用
	 */
	public static final int DELETE_SUCCEEDED = 1;					  
	public static final int DELETE_FAILED_DEVICE_POLICY_MANAGER = -2; 
	
	/**
	 * 默认总进度
	 */
	public static final int INSTALL_TOTAL_PROGRESS = 40;
	public static final int DELETE_TOTAL_PROGRESS = 5;
}
