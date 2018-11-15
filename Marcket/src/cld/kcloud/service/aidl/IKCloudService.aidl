package cld.kcloud.service.aidl;

interface IKCloudService {
	void start_KLD_app();
	String get_KLD_account();
	String get_KLD_login_result();
	void set_DDH_account(String accountJson);
	void set_DDH_login_result(String resultJson);
}

