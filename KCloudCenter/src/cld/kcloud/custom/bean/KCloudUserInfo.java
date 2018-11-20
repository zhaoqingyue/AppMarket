package cld.kcloud.custom.bean;

import com.cld.log.CldLog;
import cld.kcloud.center.R;
import cld.kcloud.utils.KCloudCommonUtil;

public class KCloudUserInfo {
	/**
	 * ������Ϣ�޸�����
	 */
	private String sex = "";			// �Ա�
	private String username = "";		// �˺�
	private String useralias = "";		// ����
	private String distname = "";		// ����
	private String mobile = "";			// �ֻ�
	private int isSuccess = -1;			// �Ƿ��ȡ����ϸ��Ϣ: -1-δ��ȡ; 0-�ɹ�; >0-ʧ��
	
	public static enum ChangeTaskEnum{
		eSEX,
		eUSERALIAS,
		eDISTNAME,
		eMOBILE,
		eALL,
	};
	
	// �����û���Ϣ��ʶ��0:�Ա�; 1:����; 2:����; 3:�ֻ�
	private int[] infoChange = { 0, 0, 0, 0 };
	
	public KCloudUserInfo() {
		sex = /*KCloudCommonUtil.getString(R.string.setting_male)*/"";
		username = "";
		useralias = "";	
		distname = "";
		mobile = "";
	}
	
	public KCloudUserInfo(KCloudUserInfo info) {
		this.sex = info.getSex();
		this.username = info.getUserName();
		this.useralias = info.getUserAlias();	
		this.distname = info.getDistName();
		this.mobile = info.getMobile();
	}

	public void assignVaule(KCloudUserInfo info) {
		this.sex = info.getSex();
		this.username = info.getUserName();
		this.useralias = info.getUserAlias();	
		this.distname = info.getDistName();
		this.mobile = info.getMobile();
	}
	
	public void reset() {
		sex = /*KCloudCommonUtil.getString(R.string.setting_male)*/"";
		username = "";
		useralias = KCloudCommonUtil.getString(R.string.setting_unset);	
		distname = KCloudCommonUtil.getString(R.string.setting_unset);
		mobile = "";
	}
	
	/**
	 * ��ȡ�Ա�
	 * @return
	 */
	public String getSex() {
		return sex;
	}
	
	/**
	 * ��ȡ�û���
	 * @return
	 */
	public String getUserName() {
		return username;
	}
	/**
	 * ��ȡ����
	 * @return
	 */
	public String getUserAlias() {
		return useralias;
	}
	
	/**
	 * ��ȡ����
	 * @return
	 */
	public String getDistName() {
		return distname;
	}
	
	/**
	 * ��ȡ�ֻ�
	 * @return
	 */
	public String getMobile() {
		return mobile;
	}
	
	/**
	 * 
	 */
	public void setSex(int sex) {
		setChangeStatus(ChangeTaskEnum.eSEX);
		this.sex = sex == 1 ? KCloudCommonUtil.getString(R.string.setting_female) : 
			KCloudCommonUtil.getString(R.string.setting_male);
		CldLog.i(" Userinfo ", " ******* sex: " + sex + ", " + this.sex + " ******* ");
	}
	
	/**
	 * 
	 * @param username
	 */
	public void setUserName(String username) {
		this.username = username;
	}
	/**
	 * 
	 * @param useralias
	 */
	public void setUserAlias(String useralias) {
		setChangeStatus(ChangeTaskEnum.eUSERALIAS);
		this.useralias = useralias;
	}
	
	/**
	 * 
	 * @param distname
	 */
	public void setDistName(String distname) {
		setChangeStatus(ChangeTaskEnum.eDISTNAME);
		this.distname = distname;
	}
	
	/**
	 * 
	 * @param mobile
	 */
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
	/**
	 * 
	 * @return
	 */
	public int[] getChangeStatus() {
		return infoChange;
	}
	
	/**
	 * 
	 * @param eTask
	 */
	public void resetChangeStatus(ChangeTaskEnum eTask) {
		switch (eTask) {
		case eSEX:
			infoChange[0] = 0;
			break;
			
		case eUSERALIAS:
			infoChange[1] = 0;
			break;
			
		case eDISTNAME:
			infoChange[2] = 0;
			break;
			
		case eMOBILE:
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
	public void setChangeStatus(ChangeTaskEnum eTask) {
		switch (eTask) {
		case eSEX:
			infoChange[0] = 1;
			break;
			
		case eUSERALIAS:
			infoChange[1] = 1;
			break;
			
		case eDISTNAME:
			infoChange[2] = 1;
			break;
			
		case eMOBILE:
			infoChange[3] = 1;
			break;
			
		default:
			break;
		}
	}
	
	/**
	 * 
	 * @param isSuccess
	 */
	public void setSuccess(int isSuccess) {
		this.isSuccess = isSuccess;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getSuccess() {
		return isSuccess;
	}
}

