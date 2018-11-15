package cld.kmarcket.util;

import android.annotation.SuppressLint;
import android.content.Context;

public class ConstantUtil 
{
	public static int height = 0;
	
	/**
	 * Handler: msg.what
	 * 0: 获取推荐应用; 1: 获取应用升级； 2：获取应用状态； 3：启动服务； 4: 安装成功；5：替换成功； 6：卸载成功
	 */
	public final static int MSG_GET_APP_RECD_SUC = 0;      
	public final static int MSG_GET_APP_UPGRADE_SUC = 1;          
	public final static int MSG_GET_APP_STATUS_SUC = 2;          
	public final static int MSG_START_SERVICE = 3;
	public final static int MSG_ADDED_SUC = 4;
	public final static int MSG_REPLACED_SUC = 5;
	public final static int MSG_REMOVED_SUC = 6;
	public final static int MSG_ADDED_FAILED = 7;
	public final static int MSG_REMOVED_FAILED = 8;
	
	/**
	 * 网络类型  0:移动网络，1:WiFi
	 */
	public final static int NET_TYPE_MOBILE = 0;
	public final static int NET_TYPE_WIFI = 1;
	
	/**
	 * 应用状态 0：未下载；  1：开始下载； 2：暂停下载； 3：取消下载； 4：下载完成； 5：正在安装； 6：安装完成； 7：安装失败；8：正在卸载； 9：卸载失败；
	 */
	public final static int DOWNLOAD_STATUS_DEFAULT = 0;
	public final static int DOWNLOAD_STATUS_START = 1;
	public final static int DOWNLOAD_STATUS_PAUSE = 2;
	public final static int DOWNLOAD_STATUS_CANCEL = 3;
	public final static int DOWNLOAD_STATUS_FINISH = 4;
	public final static int INSTALL_STATUS_START = 5;
	public final static int INSTALL_STATUS_FINISH = 6;
	public final static int INSTALL_STATUS_FAILED = 7;
	public final static int UNINSTALL_STATUS_START = 8;
	public final static int UNINSTALL_STATUS_FAILED = 9;
	
	/**
	 * 操作状态   0：下载; 1：打开; 2：更新; 3：卸载; 4：安装; 5：正在安装; 6：等待安装; 7：正在卸载; 8：开始下载; 9：暂停下载;
	 */
	public final static int APP_STATUS_DOWNLOAD = 0;
	public final static int APP_STATUS_OPEN = 1;
	public final static int APP_STATUS_UPDATE = 2;
	public final static int APP_STATUS_UNINSTALL = 3;
	public final static int APP_STATUS_INSTALL = 4;
	public final static int APP_STATUS_INSTALLING = 5;
	public final static int APP_STATUS_INSTALL_WAIT = 6;
	public final static int APP_STATUS_UNINSTALLING = 7;
	public final static int APP_STATUS_DOWNLOAD_START = 8;
	public final static int APP_STATUS_DOWNLOAD_PAUSE = 9;
	
	/**
	 * 应用来源 0：我的应用; 1：应用推荐
	 */
	public final static int APP_SOURCE_MYAPP = 0;
	public final static int APP_SOURCE_RECD = 1;
	
	/**
	 * 是否静默安装  0：否; 1：是 
	 */
	public final static int APP_QUIESCE_NO = 0;
	public final static int APP_QUIESCE_YES = 1;
	
	/**
	 * 是否是Widget 0：否； 1：是
	 */
	public final static int APP_WIDGET_NO = 0;
	public final static int APP_WIDGET_YES = 1;
	
	/**
	 * 是否已检测  0：否； 1：是
	 */
	public final static int APP_CHECKED_NO = 0;
	public final static int APP_CHECKED_YES = 1;
	
	
	public static final String TARGET_FIELD_DUID = "duid";
	public static final String TARGET_FIELD_KUID = "kuid";
	
	public static final String PREFERENCE_PACKAGE = "cld.kcloud.center";  
	public static final String PREFERENCE_NAME = "cld.kcloud.user";  
	@SuppressLint("InlinedApi") @SuppressWarnings("deprecation")
	public static int MODE = Context.MODE_WORLD_READABLE | Context.MODE_MULTI_PROCESS; 
	
	
	/**
	 * ACTION_EXIT_APP: 退出应用
	 */
	public static final String  ACTION_EXIT_APP = "action_exit_app";
	
	/**
	 * ACTION_ADDED_SUC      : 安装成功
	 * ACTION_REPLACED_SUC   : 替换成功
	 * ACTION_REMOVED_SUC    : 卸载成功
	 * ACTION_ADDED_FAILED   : 安装失败
	 * ACTION_REMOVED_FAILED : 下载失败
	 */
	public static final String ACTION_ADDED_SUC = "action_added_suc";
	public static final String ACTION_REPLACED_SUC = "action_replaced_suc";
	public static final String ACTION_REMOVED_SUC = "action_removed_suc";
	public static final String ACTION_ADDED_FAILED = "action_added_failed";
	public static final String ACTION_REMOVED_FAILED = "action_removed_failed";
	
	/**
	 * 关闭语音
	 */
	public final static String ACTION_VOICE_ROBOT_EXIT = "android.intent.action.voice.robot.EXIT";
	
	/**
	 * 关机重启
	 */
	public static final String ACTION_REBOOT = "android.intent.action.REBOOT"; 
	
	/**
	 * ACTION_LAUNCHER_UPGRADE_START: Launcher升级开始
	 */
	public static final String ACTION_LAUNCHER_UPGRADE_START = "action_launcher_upgrade_start";
	public static final String ACTION_LAUNCHER_UPGRADE_SUCCESS = "action_launcher_upgrade_success";
	public static final String ACTION_LAUNCHER_UPGRADE_FAILED = "action_launcher_upgrade_failed";
	
	/*安装开始*/
	public static final String ACTION_INSTALL_START = "com.cldpackageinstaller.InstallerInter.INSTALL_START";
	/*安装完成，判断返回值*/
	public static final String ACTION_INSTALL_COMPLETE = "com.cldpackageinstaller.InstallerInter.INSTALL_COMPLETE";
	/*安装进度*/
	public static final String ACTION_INSTALL_PROGRESS = "com.cldpackageinstaller.InstallerInter.INSTALL_PROGRESS";
	/*卸载开始*/
	public static final String ACTION_DELETE_START = "com.cldpackageinstaller.InstallerInter.DELETE_START";
	/*卸载完成，判断返回值*/
	public static final String ACTION_DELETE_COMPLETE = "com.cldpackageinstaller.InstallerInter.DELETE_COMPLETE";
	/*卸载进度*/
	public static final String ACTION_DELETE_PROGRESS = "com.cldpackageinstaller.InstallerInter.DELETE_PROGRESS";
}
