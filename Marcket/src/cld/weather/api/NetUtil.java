package cld.weather.api;

public class NetUtil {
	static private boolean mIsTestVersion = false;
	static public boolean isTestVersion(){
		return mIsTestVersion;
	}
	
	static public void setTestVersion(boolean isTest){
		mIsTestVersion = isTest;
	}
	
	static public boolean isNetConnected(){
		
		return true;
	}
}
