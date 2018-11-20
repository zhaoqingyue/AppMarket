package cld.kcloud.service.aidl;

interface IKCloudClient {
	void onLoginListener(String result);
	void onLogoutListener(String result);
	void onUpdateInfoListener(String result);
}

