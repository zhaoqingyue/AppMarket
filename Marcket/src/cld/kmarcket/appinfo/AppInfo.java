package cld.kmarcket.appinfo;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class AppInfo implements Parcelable
{
	private String mPkgName = "";     //应用包名
	private String mAppIconUrl = "";  //应用图标URL
	private String mAppName = "";     //应用名称
	private String mAppDesc = "";     //应用描述
	private String mVerName = "";     //应用版本名称
	private int mVerCode = 0;         //应用版本编码
	private int mPackSize = 0;        //安装包大小（单位：字节）
	private int mDownTimes = 0;       //下载次数
	private int mQuiesce = 0;         //是否静默安装  0：否; 1：是 
	private int mStatus = 0;          //应用状态 0:下载； 1：打开； 2：更新；3：卸载；4：安装; 5：正在安装；6：等待安装; 7:正在卸载
	private String mAppUrl = "";      //apk下载地址
	private String mUpgradeDesc = ""; //更新描述
	private Drawable mAppIcon = null; //应用图标
	private int mValidate = 0;        //是否需要验证 0：否; 1：是
	private int mDownloadStatus = 0;  //应用下载状态 0:未下载；1：开始；2：暂停；3：取消；4：完成
	private int mChecked = 0;         //是否检测  0：否； 1：是
	private int mIsWidget = 0;        //是否是Widget 0：否； 1：是
	private int mSource = 0;          //来源 0：我的应用； 1：应用推荐
	
	public AppInfo()
	{
	}
	
	public AppInfo(AppInfo item)
	{
		mPkgName = item.mPkgName;
		mAppIconUrl = item.mAppIconUrl;
		mAppName = item.mAppName;
		mAppDesc = item.mAppDesc;
		mVerName = item.mVerName;
		mVerCode = item.mVerCode;
		mPackSize = item.mPackSize;
		mDownTimes = item.mDownTimes;
		mQuiesce = item.mQuiesce;
		mStatus = item.mStatus;
		mAppUrl = item.mAppUrl;
		mUpgradeDesc = item.mUpgradeDesc;
		mAppIcon = item.mAppIcon;
		mValidate = item.mValidate;
		mDownloadStatus = item.mDownloadStatus;
		mChecked = item.mChecked;
		mIsWidget = item.mIsWidget;
		mSource = item.mSource;
	}
	
	public void setAppInfo(AppInfo item)
	{
		mPkgName = item.getPkgName();
		mAppIconUrl = item.getAppIconUrl();
		mAppName = item.getAppName();
		mAppDesc = item.getAppDesc();
		mVerName = item.getVerName();
		mVerCode = item.getVerCode();
		mPackSize = item.getPackSize();
		mDownTimes = item.getDownTimes();
		mQuiesce = item.getQuiesce();
		mStatus = item.getStatus();
		mAppUrl = item.getAppUrl();
		mUpgradeDesc = item.getUpgradeDesc();
		mAppIcon = item.getAppIcon();
		mValidate = item.getValidate();
		mDownloadStatus = item.getDownloadStatus();
		mChecked = item.getChecked();
		mIsWidget = item.getIsWidget();
		mSource = item.getSource();
	}
	
	@Override
	public int describeContents() 
	{
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel out, int flags) 
	{
		out.writeString(mPkgName);
		out.writeString(mAppIconUrl);
		out.writeString(mAppName);
		out.writeString(mAppDesc);
		out.writeString(mVerName);
		out.writeInt(mVerCode);
		out.writeInt(mPackSize);
		out.writeInt(mDownTimes);
		out.writeInt(mQuiesce);
		out.writeInt(mStatus);
		out.writeString(mAppUrl);
		out.writeString(mUpgradeDesc);
		out.writeInt(mValidate);
		out.writeInt(mDownloadStatus);
		out.writeInt(mChecked);
		out.writeInt(mIsWidget);
		out.writeInt(mSource);
	}
	
	public static final Parcelable.Creator<AppInfo> CREATOR = new Parcelable.Creator<AppInfo>() 
	{
		public AppInfo createFromParcel(Parcel in) 
		{
			return new AppInfo(in);
		}

		public AppInfo[] newArray(int size) 
		{
			return new AppInfo[size];
		}
	};
			
	private AppInfo(Parcel in)
	{
		mPkgName = in.readString();
		mAppIconUrl = in.readString();
		mAppName = in.readString();
		mAppDesc = in.readString();
		mVerName = in.readString();
		mVerCode = in.readInt();
		mPackSize = in.readInt();
		mDownTimes = in.readInt();
		mQuiesce = in.readInt();
		mStatus = in.readInt();
		mAppUrl = in.readString();
		mUpgradeDesc = in.readString();
		mValidate = in.readInt();
		mDownloadStatus = in.readInt();
		mChecked = in.readInt();
		mIsWidget = in.readInt();
		mSource = in.readInt();
	}
	
	public void setPkgName(String pkgName)
	{
		mPkgName = pkgName;
	}
	
	public String getPkgName()
	{
		return mPkgName;
	}
	
	public void setAppIconUrl(String appIconUrl)
	{
		mAppIconUrl = appIconUrl;
	}
	
	public String getAppIconUrl()
	{
		return mAppIconUrl;
	}
	
	public void setVerCode(int verCode)
	{
		mVerCode = verCode;
	}
	
	public int getVerCode()
	{
		return mVerCode;
	}
	
	public void setAppName(String appName)
	{
		mAppName = appName;
	}
	
	public String getAppName()
	{
		return mAppName;
	}
	
	public void setAppDesc(String appDesc)
	{
		mAppDesc = appDesc;
	}
	
	public String getAppDesc()
	{
		return mAppDesc;
	}
	
	public void setVerName(String verName)
	{
		mVerName = verName;
	}
	
	public String getVerName()
	{
		return mVerName;
	}
	
	public void setPackSize(int packSize)
	{
		mPackSize = packSize;
	}
	
	public int getPackSize()
	{
		return mPackSize;
	}
	
	public void setDownTimes(int downTimes)
	{
		mDownTimes = downTimes;
	}
	
	public int getDownTimes()
	{
		return mDownTimes;
	}
	
	public void setQuiesce(int quiesce)
	{
		mQuiesce = quiesce;
	}
	
	public int getQuiesce()
	{
		return mQuiesce;
	}
	
	public void setStatus(int status)
	{
		mStatus = status;
	}
	
	public int getStatus()
	{
		return mStatus;
	}
	
	public void setAppUrl(String apkUrl)
	{
		mAppUrl = apkUrl;
	}
	
	public String getAppUrl()
	{
		return mAppUrl;
	}
	
	public void setUpgradeDesc(String upgradeDesc)
	{
		mUpgradeDesc = upgradeDesc;
	}
	
	public String getUpgradeDesc()
	{
		return mUpgradeDesc;
	}
	
	public void setAppIcon(Drawable appIconBitmap) 
	{
		mAppIcon = appIconBitmap;
	}
	
	public Drawable getAppIcon() 
	{
		return mAppIcon;
	}
	
	public void setValidate(int validate)
	{
		mValidate = validate;
	}
	
	public int getValidate()
	{
		return mValidate;
	}
	
	public void setDownloadStatus(int downloadStatus)
	{
		mDownloadStatus = downloadStatus;
	}
	
	public int getDownloadStatus()
	{
		return mDownloadStatus;
	}
	
	public void setIsWidget(int isWidget)
	{
		mIsWidget = isWidget;
	}
	
	public int getIsWidget()
	{
		return mIsWidget;
	}
	
	public void setChecked(int checked)
	{
		mChecked = checked;
	}
	
	public int getChecked()
	{
		return mChecked;
	}
	
	public void setSource(int source)
	{
		mSource = source;
	}
	
	public int getSource()
	{
		return mSource;
	}
}
