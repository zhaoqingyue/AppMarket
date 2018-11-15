package com.download.api;

import java.io.Serializable;

public class Status implements Serializable
{
	private static final long serialVersionUID = -3082761479813142545L;
	private String mPkgName = "";
	/**
	 * 0： 默认值；1： 开始下载；  2： 暂停下载；  3： 下载完成； 4： 正在安装； 5： 安装完成; 6： 正在卸载； 7： 卸载完成
	 */
	private int mType = 0;  
	
	public Status()
	{
	}
	
	public Status(String pkgName, int type) 
	{
		mPkgName = pkgName;
		mType = type;
	}
	
	public Status(Status status)
	{
		mPkgName = status.getPkgName();
		mType = status.getType();
	}

	public String getPkgName() 
	{
		return mPkgName;
	}

	public void setPkgName(String pkgName) 
	{
		this.mPkgName = pkgName;
	}

	public int getType() 
	{
		return mType;
	}

	public void setType(int type) 
	{
		mType = type;
	}
}
