package cld.kcloud.service.aidl;

import cld.kcloud.service.aidl.IKCloudClient;
interface IKCloudService {
	void start_KLD_app();
	void start_KLD_kcenter();
	String get_KLD_account();
	String get_KLD_login_result();
	void set_DDH_account(String accountJson);
	void set_DDH_login_result(String resultJson);
	void setLoginListener(in IKCloudClient client);
	void notify_KLD_InvalidSession();
	void notify_KLD_Message(String app_name, String message, String btnText, int type);
	int isKOS_RunningApp(String app_name);
	void start_check_simcard();
	void jump_to_renew();
}

