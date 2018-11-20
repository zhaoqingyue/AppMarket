package cld.kcloud.custom.bean;

import com.cld.log.CldLog;
import cld.kcloud.center.R;
import cld.kcloud.utils.KCloudCommonUtil;

public class KCloudUserInfo {
	/**
	 * 个人信息修改内容
	 */
	private String sex = "";			// 性别
	private String username = "";		// 账号
	private String useralias = "";		// 别名
	private String distname = "";		// 区域
	private String mobile = "";			// 手机
	private int isSuccess = -1;			// 是否获取到详细信息: -1-未获取; 0-成功; >0-失败
	
	public static enum ChangeTaskEnum{
		eSEX,
		eUSERALIAS,
		eDISTNAME,
		eMOBILE,
		eALL,
	};
	
	// 更改用户信息标识，0:性别; 1:化名; 2:区域; 3:手机
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
	 * 获取性别
	 * @return
	 */
	public String getSex() {
		return sex;
	}
	
	/**
	 * 获取用户名
	 * @return
	 */
	public String getUserName() {
		return username;
	}
	/**
	 * 获取别名
	 * @return
	 */
	public String getUserAlias() {
		return useralias;
	}
	
	/**
	 * 获取区域
	 * @return
	 */
	public String getDistName() {
		return distname;
	}
	
	/**
	 * 获取手机
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

