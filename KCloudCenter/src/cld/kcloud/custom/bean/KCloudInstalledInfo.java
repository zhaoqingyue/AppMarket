package cld.kcloud.custom.bean;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class KCloudInstalledInfo implements Parcelable
{
	private String mPkgName = "";     //Ӧ�ð���
	private String mAppIconUrl = "";  //Ӧ��ͼ��URL
	private String mAppName = "";     //Ӧ������
	private String mAppDesc = "";     //Ӧ������
	private String mVerName = "";     //Ӧ�ð汾����
	private int mVerCode = 0;         //Ӧ�ð汾����
	private int mPackSize = 0;        //��װ����С����λ���ֽڣ�
	private int mDownTimes = 0;       //���ش���
	private int mQuiesce = 0;         //�Ƿ�Ĭ��װ  0����; 1���� 
	private int mStatus = 0;          //Ӧ��״̬ 0:���أ� 1���򿪣� 2�����£�3��ж�أ�4����װ; 5�����ڰ�װ��6���ȴ���װ; 7:����ж��
	private String mAppUrl = "";      //apk���ص�ַ
	private String mUpgradeDesc = ""; //��������
	private Drawable mAppIcon = null; //Ӧ��ͼ��
	private int mValidate = 0;        //�Ƿ���Ҫ��֤ 0����; 1����
	private int mDownloadStatus = 0;  //Ӧ������״̬ 0:δ���أ�1����ʼ��2����ͣ��3��ȡ����4�����
	private int mChecked = 0;         //�Ƿ���  0���� 1����
	private int mIsWidget = 0;        //�Ƿ���Widget 0���� 1����
	private int mSource = 0;          //��Դ 0���ҵ�Ӧ�ã� 1��Ӧ���Ƽ�
	private long mInstallTime = 0;    //��ʾ��װ��ʱ���(ÿ�������ʾһ��)
	
	public KCloudInstalledInfo()
	{
	}
	
	public KCloudInstalledInfo(KCloudInstalledInfo item)
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
		mInstallTime = item.mInstallTime;
	}
	
	public void setAppInfo(KCloudInstalledInfo item)
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
		mInstallTime = item.getInstallTime();
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
		out.writeLong(mInstallTime);
	}
	
	public static final Parcelable.Creator<KCloudInstalledInfo> CREATOR = new Parcelable.Creator<KCloudInstalledInfo>() 
	{
		public KCloudInstalledInfo createFromParcel(Parcel in) 
		{
			return new KCloudInstalledInfo(in);
		}

		public KCloudInstalledInfo[] newArray(int size) 
		{
			return new KCloudInstalledInfo[size];
		}
	};
			
	private KCloudInstalledInfo(Parcel in)
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
		mInstallTime = in.readLong();
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
	
	public void setInstallTime(long installTime)
	{
		mInstallTime = installTime;
	}
	
	public long getInstallTime()
	{
		return mInstallTime;
	}
}
