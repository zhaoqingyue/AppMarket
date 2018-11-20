/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: InstallInfo.java
 * @Prject: KCloudCenter_M550
 * @Package: cld.kmarket.install
 * @Description: 安装包信息
 * @author: zhaoqy
 * @date: 2016年8月3日 下午3:55:32
 * @version: V1.0
 */

package cld.kmarket.install;

public class InstallInfo 
{
	private String mAppName;			 //应用名
	private String mPkgName;			 //包名	
	private String mPath;				 //安装包路径
	private boolean mInstallFlag = true; //安装标志  true：安装； false：卸载
	private int mCurProgress = 0;		 //当前进度
	private int mTotalProgress = 0;		 //总进度
	
	public String getAppName() 
	{
		return mAppName;
	}
	
	public void setAppName(String appName) 
	{
		mAppName = appName;
	}
	
	public String getPkgName() 
	{
		return mPkgName;
	}
	
	public void setPkgName(String pkgName) 
	{
		mPkgName = pkgName;
	}
	
	public String getPath() 
	{
		return mPath;
	}
	
	public void setPath(String path) 
	{
		mPath = path;
	}
	
	public boolean isInstallFlag() 
	{
		return mInstallFlag;
	}
	
	public void setInstallFlag(boolean installFlag) 
	{
		mInstallFlag = installFlag;
	}
	
	public int getCurProgress() 
	{
		return mCurProgress;
	}
	
	public void setCurProgress(int curProgress) 
	{
		mCurProgress = curProgress;
	}
	
	public int getTotalProgress() 
	{
		return mTotalProgress;
	}
	
	public void setTotalProgress(int totalProgress) 
	{
		mTotalProgress = totalProgress;
	}
}
