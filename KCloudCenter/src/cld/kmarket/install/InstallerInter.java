package cld.kmarket.install;

public class InstallerInter 
{
	/**
	 * ���͸�APP�㲥
	 * ACTION_INSTALL_START: ��װ��ʼ
	 * ACTION_INSTALL_PROGRESS: ��װ����
	 * ACTION_INSTALL_COMPLETE: ��װ��ɣ��жϷ���ֵ
	 * ACTION_DELETE_START: ж�ؿ�ʼ
	 * ACTION_DELETE_PROGRESS: ж�ؽ���
	 * ACTION_DELETE_COMPLETE: ж����ɣ��жϷ���ֵ
	 */
	public static final String ACTION_INSTALL_START = "cld.installer.INSTALL_START";
	public static final String ACTION_INSTALL_PROGRESS = "cld.installer.INSTALL_PROGRESS";
	public static final String ACTION_INSTALL_COMPLETE = "cld.installer.INSTALL_COMPLETE";
	public static final String ACTION_DELETE_START = "cld.installer.DELETE_START";
	public static final String ACTION_DELETE_PROGRESS = "cld.installer.DELETE_PROGRESS";
	public static final String ACTION_DELETE_COMPLETE = "cld.installer.DELETE_COMPLETE";
	public static final String ACTION_INSTALL_VERSION = "cld.installer.INSTALL_VERSION";
	
	/**
	 * �㲥action��������
	 */
	public static final String APP_NAME = "app_name";				  
	public static final String PACKAGE_NAME = "package_name";		  
	public static final String CURRENT_PROGRESS = "current_progress"; 
	public static final String TOTAL_PROGRESS = "total_progress";	  
	public static final String RET_CODE = "ret_code";				  
	public static final String VERSION_NUMBER = "version_number";	 

	/**
	 * ��װ����ֵ���ͣ��������Ǵ��� ret_code����ֵ����
	 * INSTALL_SUCCEEDED: ��װ�ɹ�
	 * INSTALL_FAILED_INSUFFICIENT_STORAGE: �ռ䲻��
	 */
	public static final int INSTALL_SUCCEEDED = 1;					  
	public static final int INSTALL_FAILED_INSUFFICIENT_STORAGE = -4; 
	
	/**
	 * ж�ط���ֵ���ͣ��������Ǵ��� ret_code����ֵ����
	 * DELETE_SUCCEEDED: ж�سɹ�
	 * DELETE_FAILED_DEVICE_POLICY_MANAGER: ϵͳӦ��
	 */
	public static final int DELETE_SUCCEEDED = 1;					  
	public static final int DELETE_FAILED_DEVICE_POLICY_MANAGER = -2; 
	
	/**
	 * Ĭ���ܽ���
	 */
	public static final int INSTALL_TOTAL_PROGRESS = 40;
	public static final int DELETE_TOTAL_PROGRESS = 5;
}
