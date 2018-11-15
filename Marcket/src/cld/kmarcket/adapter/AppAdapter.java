package cld.kmarcket.adapter;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cld.kmarcket.R;
import cld.kmarcket.appinfo.AppInfo;
import cld.kmarcket.customview.RoundProgressBar;
import cld.kmarcket.dialog.IDialogClick;
import cld.kmarcket.dialog.PromptDialog;
import cld.kmarcket.dialog.UpdateDialog;
import cld.kmarcket.util.CommonUtil;
import cld.kmarcket.util.ConstantUtil;
import cld.kmarcket.util.FileUtil;
import cld.kmarcket.util.LauncherUtil;
import cld.kmarcket.util.LogUtil;
import cld.kmarcket.util.NetUtil;

import com.download.api.DownloadManager;
import com.download.api.DownloadWatchManager;
import com.download.api.Info;
import com.download.api.InfoDao;
import com.download.api.Status;
import com.download.api.StatusDao;
import com.download.api.TaskStatus;
import com.nostra13.universalimageloader.core.ImageLoader;

public class AppAdapter extends BaseAdapter
{
	private Context              mContext;
	private LayoutInflater       mInflater;
	private ArrayList<AppInfo>   mAppInfoList;
	private List<String>         mUndoneList;
	private InfoDao              mInfoDao;
	private StatusDao            mStatusDao;
	private DownloadManager      mDownloadManager;
	private DownloadWatchManager mWatchManager;
	
	public AppAdapter(Context context, ArrayList<AppInfo> appInfoList)
	{
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		mAppInfoList = appInfoList;
		
		mInfoDao = InfoDao.getInstance(mContext);
		mUndoneList = mInfoDao.queryUndone();
		mStatusDao = StatusDao.getInstance(mContext);
		mDownloadManager = DownloadManager.getInstance(mContext);
		mWatchManager = DownloadWatchManager.getInstance();
	}
	
	@Override
	public int getCount() 
	{
		if(mAppInfoList == null)
			return 0;
		return mAppInfoList.size();
	}

	@Override
	public Object getItem(int position) 
	{
		if(mAppInfoList == null || mAppInfoList.size() <= position 
				|| position < 0)
			return null;
		return mAppInfoList.get(position);
	}

	@Override
	public long getItemId(int position) 
	{
		return position;
	}

	@SuppressLint("NewApi")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		checkAppType(position);
		final ViewHolder holder;
		if (convertView == null) 
		{
			convertView = createConvertView(position);
			holder = new ViewHolder();
			holder.appIcon = (ImageView)convertView.findViewById(
					R.id.id_app_icon);
			holder.appName = (TextView)convertView.findViewById(
					R.id.id_app_anme);
			holder.desc = (TextView)convertView.findViewById(
					R.id.id_app_desc);
			holder.verName = (TextView)convertView.findViewById(
					R.id.id_app_version);
			holder.size = (TextView)convertView.findViewById(
					R.id.id_app_size);
			holder.tiems = (TextView)convertView.findViewById(
					R.id.id_app_download_times);
			holder.operate = (Button) convertView.findViewById(
					R.id.id_app_operate);
			holder.cancel = (Button) convertView.findViewById(
					R.id.id_app_download_cancel);
			holder.start = (Button) convertView.findViewById(
					R.id.id_app_download_start);
			holder.progressBar = (RoundProgressBar) convertView.findViewById(
					R.id.id_app_download_progressbar);
			holder.download = (RelativeLayout) convertView.findViewById(
					R.id.id_app_download);
			convertView.setTag(holder);
		} 
		else 
		{
			holder = (ViewHolder) convertView.getTag();
		}
		
		AppInfo appinfo = mAppInfoList.get(position);
		holder.appName.setText(appinfo.getAppName());
		holder.verName.setText(CommonUtil.getVerName(appinfo.getVerName()));
		holder.size.setText(CommonUtil.FormatFileSize(appinfo.getPackSize()));
		holder.tiems.setText(CommonUtil.FormatTimes(appinfo.getDownTimes()));
		
		setAppIcon(holder, position);
		updateAppStatus(holder, position);
		holder.viewlistener = new ViewListener(holder, position);
		
		return convertView;
	}

	private View createConvertView(int position)
	{
		View convertView = null;
		AppInfo appinfo = mAppInfoList.get(position);
		switch (appinfo.getStatus()) 
		{
		case ConstantUtil.APP_STATUS_DOWNLOAD: //下载
		{
			convertView = mInflater.inflate(R.layout.item_app_download, null);
			break;
		}
		case ConstantUtil.APP_STATUS_OPEN:      //打开
		case ConstantUtil.APP_STATUS_UNINSTALL: //卸载
		{
			convertView = mInflater.inflate(R.layout.item_app_open, null);
			break;
		}
		case ConstantUtil.APP_STATUS_UPDATE: //更新
		{
			convertView = mInflater.inflate(R.layout.item_app_update, null);
			break;
		}
		case ConstantUtil.APP_STATUS_INSTALL:      //安装
		case ConstantUtil.APP_STATUS_INSTALLING:   //正在安装
		case ConstantUtil.APP_STATUS_INSTALL_WAIT: //等待安装
		{
			//应用来源 0：我的应用； 1：应用推荐
			if (appinfo.getSource() == ConstantUtil.APP_SOURCE_MYAPP)
			{
				convertView = mInflater.inflate(R.layout.item_app_update, null);
			}
			else if (appinfo.getSource() == ConstantUtil.APP_SOURCE_RECD)
			{
				convertView = mInflater.inflate(R.layout.item_app_download, null);
			}
			break;
		}
		case ConstantUtil.APP_STATUS_UNINSTALLING: //正在卸载
		{
			convertView = mInflater.inflate(R.layout.item_app_open, null);
			break;
		}
		default:
			break;
		}
		return convertView;
	}
	
	static class ViewHolder
	{
		public ImageView appIcon; //应用图标
		public TextView  appName; //应用名称
		public TextView  desc;    //介绍
		public TextView  verName; //版本名称
		public TextView  size;    //应用大小
		public TextView  tiems;   //下载次数
		public Button    operate; //下载/更新
		public Button    cancel;  //取消
		public Button    start;   //开始/暂停
		public RoundProgressBar progressBar; //下载进度
		public RelativeLayout download;
		public ViewListener viewlistener;
	}
	
	private void checkAppType(int position) 
	{
		AppInfo appinfo = mAppInfoList.get(position);
		String pkgname = appinfo.getPkgName();
		Status status = CommonUtil.getStatusDao().query(pkgname);
		if (status != null)
		{
			switch (status.getType()) 
			{
			case ConstantUtil.DOWNLOAD_STATUS_FINISH: 
			{
				//下载完成 --> 安装
				appinfo.setStatus(ConstantUtil.APP_STATUS_INSTALL);
				break;
			}
			case ConstantUtil.INSTALL_STATUS_START: 
			{
				//正在安装
				appinfo.setStatus(ConstantUtil.APP_STATUS_INSTALLING);
				break;
			}
			case ConstantUtil.INSTALL_STATUS_FINISH: 
			{
				//安装完成 --> 打开
				appinfo.setStatus(ConstantUtil.APP_STATUS_OPEN);
				//appinfo.setStatus(3); //表示卸载
				//避免后台配置升级包，因为这个状态导致不显示"更新", 而是显示"打开"
				CommonUtil.updateAppType(appinfo.getPkgName(), 
						ConstantUtil.DOWNLOAD_STATUS_DEFAULT);
				break;
			}
			case ConstantUtil.INSTALL_STATUS_FAILED: 
			{
				//安装失败 --> 安装
				appinfo.setStatus(ConstantUtil.APP_STATUS_INSTALL);
				break;
			}
			case ConstantUtil.UNINSTALL_STATUS_START: 
			{
				//正在卸载
				appinfo.setStatus(ConstantUtil.APP_STATUS_UNINSTALLING);
				break;
			}
			case ConstantUtil.UNINSTALL_STATUS_FAILED: 
			{
				//卸载失败 --> 卸载
				appinfo.setStatus(ConstantUtil.APP_STATUS_UNINSTALL);
				break;
			}
			default:
				break;
			}
		}
	}
	
	@SuppressLint("NewApi") 
	private void setAppIcon(ViewHolder holder, int position)
	{
		AppInfo appinfo = mAppInfoList.get(position);
		switch (appinfo.getStatus()) 
		{
		case ConstantUtil.APP_STATUS_DOWNLOAD: //下载
		{
			ImageLoader.getInstance().displayImage(appinfo.getAppIconUrl(),
					holder.appIcon, CommonUtil.iconOption);
			break;
		}
		case ConstantUtil.APP_STATUS_OPEN:    //打开
		case ConstantUtil.APP_STATUS_INSTALL: //安装
		{
			//应用来源 0：我的应用； 1：应用推荐
			if (appinfo.getSource() == ConstantUtil.APP_SOURCE_MYAPP)
			{
				if (appinfo.getAppIcon() != null)
				{
					holder.appIcon.setImageDrawable(appinfo.getAppIcon());
				}
				else
				{
					holder.appIcon.setImageDrawable(mContext.getResources().
							getDrawable(R.drawable.icon_default));
				}
			}
			else if (appinfo.getSource() == ConstantUtil.APP_SOURCE_RECD)
			{
				ImageLoader.getInstance().displayImage(appinfo.getAppIconUrl(),
						holder.appIcon, CommonUtil.iconOption);
			}
			break;
		}
		case ConstantUtil.APP_STATUS_UPDATE: //更新
		{
			if (appinfo.getAppIconUrl().isEmpty())
			{
				if (appinfo.getAppIcon() != null)
				{
					holder.appIcon.setImageDrawable(appinfo.getAppIcon());
				}
				else
				{
					holder.appIcon.setImageDrawable(mContext.getResources().
							getDrawable(R.drawable.icon_default));
				}
			}
			else
			{
				ImageLoader.getInstance().displayImage(appinfo.getAppIconUrl(),
						holder.appIcon, CommonUtil.iconOption);
			}
			break;
		}
		case ConstantUtil.APP_STATUS_UNINSTALL: //卸载
		{
			if (appinfo.getAppIcon() != null)
			{
				holder.appIcon.setImageDrawable(appinfo.getAppIcon());
			}
			else
			{
				holder.appIcon.setImageDrawable(mContext.getResources().
						getDrawable(R.drawable.icon_default));
			}
			break;
		}
		default:
			break;
		}
	}
	
	private void updateAppStatus(ViewHolder holder, int position)
	{
		AppInfo appinfo =  mAppInfoList.get(position);
		switch (appinfo.getStatus()) 
		{
		case ConstantUtil.APP_STATUS_DOWNLOAD: //下载
		{
			holder.desc.setText(appinfo.getAppDesc());
			holder.operate.setBackgroundResource(
					R.drawable.button_download_selector);
			holder.operate.setTextColor(
					mContext.getResources().getColor(R.color.white));
			holder.operate.setText(R.string.app_download);
			break;
		}
		case ConstantUtil.APP_STATUS_OPEN: //打开
		{
			holder.desc.setText(appinfo.getAppDesc());
			holder.operate.setBackgroundResource(
					R.drawable.button_open_selector);
			holder.operate.setTextColor(
					mContext.getResources().getColor(R.color.main_color));
			holder.operate.setText(R.string.app_open);
			break;
		}	
		case ConstantUtil.APP_STATUS_UPDATE: //更新
		{
			holder.desc.setText(appinfo.getUpgradeDesc());
			holder.operate.setBackgroundResource(
					R.drawable.button_download_selector);
			holder.operate.setTextColor(
					mContext.getResources().getColor(R.color.white));
			holder.operate.setText(R.string.app_upgrade);
			break;
		}	
		case ConstantUtil.APP_STATUS_UNINSTALL: //卸载
		{
			holder.desc.setText(appinfo.getAppDesc());
			holder.operate.setBackgroundResource(
					R.drawable.button_open_selector);
			holder.operate.setTextColor(
					mContext.getResources().getColor(R.color.main_color));
			holder.operate.setText(R.string.app_uninstall);
			break;
		}
		case ConstantUtil.APP_STATUS_INSTALL: //安装
		{
			//应用来源 0：我的应用； 1：应用推荐
			if (appinfo.getSource() == ConstantUtil.APP_SOURCE_MYAPP)
			{
				holder.desc.setText(appinfo.getUpgradeDesc());
			}
			else if (appinfo.getSource() == ConstantUtil.APP_SOURCE_RECD)
			{
				holder.desc.setText(appinfo.getAppDesc());
			}
			holder.operate.setBackgroundResource(
					R.drawable.button_download_selector);
			holder.operate.setTextColor(
					mContext.getResources().getColor(R.color.white));
			holder.operate.setText(R.string.app_install);
			break;
		}
		case ConstantUtil.APP_STATUS_INSTALLING: //正在安装
		{
			//应用来源 0：我的应用； 1：应用推荐
			if (appinfo.getSource() == ConstantUtil.APP_SOURCE_MYAPP)
			{
				holder.desc.setText(appinfo.getUpgradeDesc());
			}
			else if (appinfo.getSource() == ConstantUtil.APP_SOURCE_RECD)
			{
				holder.desc.setText(appinfo.getAppDesc());
			}
			holder.operate.setBackgroundResource(
					R.drawable.button_download_selector);
			holder.operate.setTextColor(
					mContext.getResources().getColor(R.color.white));
			holder.operate.setText(R.string.app_installing);
			break;
		}
		case ConstantUtil.APP_STATUS_INSTALL_WAIT: //等待安装
		{
			//应用来源 0：我的应用； 1：应用推荐
			if (appinfo.getSource() == ConstantUtil.APP_SOURCE_MYAPP)
			{
				holder.desc.setText(appinfo.getUpgradeDesc());
			}
			else if (appinfo.getSource() == ConstantUtil.APP_SOURCE_RECD)
			{
				holder.desc.setText(appinfo.getAppDesc());
			}
			holder.operate.setBackgroundResource(
					R.drawable.button_download_selector);
			holder.operate.setTextColor(
					mContext.getResources().getColor(R.color.white));
			holder.operate.setText(R.string.app_install_wait);
			break;
		}
		case ConstantUtil.APP_STATUS_UNINSTALLING: //正在卸载
		{
			holder.desc.setText(appinfo.getAppDesc());
			holder.operate.setBackgroundResource(
					R.drawable.button_download_selector);
			holder.operate.setTextColor(
					mContext.getResources().getColor(R.color.white));
			holder.operate.setText(R.string.app_uninstalling);
			break;
		}
		default:
			break;
		}
	}
	
	private void setDownloadStatus(ViewHolder holder, int position)
	{
		AppInfo appinfo = mAppInfoList.get(position);
		switch (appinfo.getDownloadStatus()) 
		{
		case ConstantUtil.DOWNLOAD_STATUS_START: //开始下载
		{
			holder.operate.setVisibility(View.GONE);
			holder.download.setVisibility(View.VISIBLE);
			holder.start.setBackgroundResource(
					R.drawable.app_download_start);
			CommonUtil.updateAppType(appinfo.getPkgName(), 
					ConstantUtil.DOWNLOAD_STATUS_START);
			CommonUtil.sendDownloadStatus(appinfo.getAppName(), 
					ConstantUtil.DOWNLOAD_STATUS_START);
			break;
		}
		case ConstantUtil.DOWNLOAD_STATUS_PAUSE: //暂停下载
		{
			holder.operate.setVisibility(View.GONE);
			holder.download.setVisibility(View.VISIBLE);
			holder.start.setBackgroundResource(
					R.drawable.app_download_pause);
			CommonUtil.updateAppType(appinfo.getPkgName(), 
					ConstantUtil.DOWNLOAD_STATUS_PAUSE);
			CommonUtil.sendDownloadStatus(appinfo.getAppName(), 
					ConstantUtil.DOWNLOAD_STATUS_PAUSE);
			break;
		}
		case ConstantUtil.DOWNLOAD_STATUS_CANCEL: //取消下载
		{
			holder.operate.setVisibility(View.VISIBLE);
			holder.download.setVisibility(View.GONE);
			appinfo.setDownloadStatus(ConstantUtil.DOWNLOAD_STATUS_DEFAULT);
			CommonUtil.updateAppType(appinfo.getPkgName(), 
					ConstantUtil.DOWNLOAD_STATUS_DEFAULT);
			CommonUtil.sendDownloadStatus(appinfo.getAppName(), 
					ConstantUtil.DOWNLOAD_STATUS_CANCEL);
			
			switch (appinfo.getStatus()) 
			{
			case ConstantUtil.APP_STATUS_DOWNLOAD: //下载
			{
				holder.operate.setBackgroundResource(
						R.drawable.button_download_selector);
				holder.operate.setTextColor(
						mContext.getResources().getColor(R.color.white));
				holder.operate.setText(R.string.app_download);
				break;
			}
			case ConstantUtil.APP_STATUS_UPDATE: //更新
			{
				holder.operate.setBackgroundResource(
						R.drawable.button_download_selector);
				holder.operate.setTextColor(
						mContext.getResources().getColor(R.color.white));
				holder.operate.setText(R.string.app_upgrade);
				break;
			}	
			default:
				break;
			}
			break;
		}
		case ConstantUtil.DOWNLOAD_STATUS_FINISH: //下载完成
		{
			holder.operate.setVisibility(View.VISIBLE);
			holder.download.setVisibility(View.GONE);
			CommonUtil.updateAppType(appinfo.getPkgName(), 
					ConstantUtil.DOWNLOAD_STATUS_FINISH);
			CommonUtil.sendDownloadStatus(appinfo.getAppName(), 
					ConstantUtil.DOWNLOAD_STATUS_FINISH);
			appinfo.setStatus(ConstantUtil.APP_STATUS_INSTALL);
			updateAppStatus(holder, position);
			break;
		}
		default:
			break;
		}
	}
	
	private final class ViewListener implements OnClickListener
	{
		private int position;
		private String urlPath;
		private ViewHolder viewHolder;
		private AppInfo appinfo;
		private long fileLen;
		private long tempDone;
		
		public ViewListener(ViewHolder viewHolder, int position)
		{
			this.viewHolder = viewHolder;
			this.position = position;
			this.appinfo = mAppInfoList.get(position);
			this.urlPath = this.appinfo.getAppUrl();
				
			viewHolder.operate.setOnClickListener(this);
			viewHolder.cancel.setOnClickListener(this);
			viewHolder.start.setOnClickListener(this);
			
			if (appinfo.getChecked() == ConstantUtil.APP_CHECKED_NO)
			{
				//检测是否有未完成的任务
				checkUndone();
			}
		}
		
		private void checkUndone()
		{
			appinfo.setChecked(ConstantUtil.APP_CHECKED_YES);
			mUndoneList = mInfoDao.queryUndone();
			String pkgname = appinfo.getPkgName();
			//查询是否有下载完成记录
			Status status = mStatusDao.query(pkgname);
			if (mUndoneList.contains(urlPath))
			{
				onUndone(urlPath, status);
			}
			else
			{
				//下载任务没有连接上，没有Undone记录，但是还在连接或暂停连接
				onConnecting(status);
			}
		}
		
		private void onUndone(String path, Status status)
		{
			if (status != null)
			{
				switch (status.getType()) 
				{
				case ConstantUtil.DOWNLOAD_STATUS_START: 
				{
					//不管是WiFi网络下还是4G网络下，则继续下载
					freshProgressBar(path);
					startDownload();
					break;
				}
				case ConstantUtil.DOWNLOAD_STATUS_PAUSE: 
				{
					//显示暂停
					freshProgressBar(path);
					appinfo.setDownloadStatus(
							ConstantUtil.DOWNLOAD_STATUS_PAUSE);
					setDownloadStatus(viewHolder, position);
					break;
				}	
				default:
					break;
				}
			}
		}
		
		private void freshProgressBar(String path)
		{
			long done = 0, len = 0;
			List<Info> exist = mInfoDao.queryUndone(path);
			for (int i=0; i<exist.size(); i++)
			{
				done += exist.get(i).getDone();
				if (i == exist.size()-1)
				{
					len = exist.get(i).getEnd();
				}
			}
			
			if (len > 0 && done < len)
			{
				long progress = done * 100 / len;
				LogUtil.i(LogUtil.TAG, " progress: " + progress);
				if (progress > 0)
				{
					viewHolder.progressBar.setProgress(progress);
				}
			}
		}
		
		private void onConnecting(Status status)
		{
			if (status != null)
			{
				switch (status.getType()) 
				{
				case ConstantUtil.DOWNLOAD_STATUS_START: 
				{
					viewHolder.progressBar.setProgress(0);
					startDownload();
					break;
				}
				case ConstantUtil.DOWNLOAD_STATUS_PAUSE: 
				{
					viewHolder.progressBar.setProgress(0);
					appinfo.setDownloadStatus(
							ConstantUtil.DOWNLOAD_STATUS_PAUSE);
					setDownloadStatus(viewHolder, position);
					break;
				}	
				default:
					break;
				}
			}
		}
		
		@SuppressLint("NewApi") 
		@Override
		public void onClick(View v) 
		{
			switch (v.getId()) 
			{
			case R.id.id_app_operate:
			{
				onOperate();
				break;
			}
			case R.id.id_app_download_start: 
			{
				onStartOrPause();
				break;
			}
			case R.id.id_app_download_cancel:
			{
				onCancel();
				break;
			}
			default:
				break;
			}
		}
		
		@SuppressLint("NewApi") 
		private void onOperate()
		{
			switch (appinfo.getStatus()) 
			{
			case ConstantUtil.APP_STATUS_DOWNLOAD: //下载
			case ConstantUtil.APP_STATUS_UPDATE:   //更新
			{
				onDownloadOrUpdate();
				break;
			}
			case ConstantUtil.APP_STATUS_OPEN: //打开
			{
				onOpenApk();
				break;
			}
			case ConstantUtil.APP_STATUS_UNINSTALL: //卸载
			{
				onUninstall();
				break;
			}
			case ConstantUtil.APP_STATUS_INSTALL: //安装
			{
				appinfo.setStatus(ConstantUtil.APP_STATUS_INSTALLING);
				updateAppStatus(viewHolder, position);
				if (appinfo.getPkgName().equals("com.cld.launcher"))
				{
					//显示Launcher升级提示 
					UpdateDialog dialog = new UpdateDialog(mContext);
					dialog.show();
					LauncherUtil.onLauncherUpgradeStart();
				}
				CommonUtil.updateAppType(appinfo.getPkgName(), 
						ConstantUtil.INSTALL_STATUS_START);
				CommonUtil.startSlienceInstall(urlPath);
				break;
			}
			case ConstantUtil.APP_STATUS_INSTALLING:   //正在安装
			case ConstantUtil.APP_STATUS_INSTALL_WAIT: //等待安装
			{
				//应用正在升级，请稍后尝试
				CommonUtil.makeText(R.string.toast_app_is_upgrading);
				break;
			}
			case ConstantUtil.APP_STATUS_UNINSTALLING: //正在卸载
			{
				//应用正在卸载，暂时无法使用
				CommonUtil.makeText(R.string.toast_app_is_uninstalling);
				break;
			}
			default:
				break;
			}
		}

		private void onDownloadOrUpdate()
		{
			switch (NetUtil.getNetType(mContext)) 
			{
			case ConstantUtil.NET_TYPE_MOBILE:
			{
				//只在 开始点击下载的时候判断剩余空间时候充足
				onNetMobile();
				break;
			}
			case ConstantUtil.NET_TYPE_WIFI:
			{
				if (FileUtil.isEnough(mContext, appinfo.getPackSize()))
				{
					startDownload();
				}
				else
				{
					//空间不足，建议删除一些下载的音乐文件释放空间后再尝试
					String msg = mContext.getResources().getString(
							R.string.toast_memory_not_enough);
					CommonUtil.makeText(msg);
				}
				break;
			}
			default:
				//检查网络
				String msg = mContext.getResources().getString(
						R.string.toast_net_error);
				CommonUtil.makeText(msg);
				break;
			}
		}
		
		private void onOpenApk() 
		{
			int validate = appinfo.getValidate();
			LogUtil.i(LogUtil.TAG, " validate: " + validate);
			String pkgName = appinfo.getPkgName();
			CommonUtil.openAppByPkgname(pkgName);
		}
		
		private void onUninstall()
		{
			//确定要卸载"***"吗？
			String msg = mContext.getResources().getString(
					R.string.dialog_message_uninstall);
			String text = String.format(msg, appinfo.getAppName());
			final PromptDialog dialog = new PromptDialog(mContext, text);
			dialog.setOnDialogClickListener(new IDialogClick()
			{
				@Override
				public void onClick(View v) 
				{
					switch (v.getId()) 
					{
					case R.id.id_dialog_prompt_sure:
					{
						dialog.dismiss();
						appinfo.setStatus(ConstantUtil.APP_STATUS_UNINSTALLING);
						updateAppStatus(viewHolder, position);
						CommonUtil.updateAppType(appinfo.getPkgName(), 
								ConstantUtil.UNINSTALL_STATUS_START);
						CommonUtil.startSlienceUninstall(appinfo.getPkgName());
						break;
					}
					case R.id.id_dialog_prompt_cancel:
					{
						dialog.dismiss();
						break;
					}	
					default:
						break;
					}
				}
			});
			dialog.show();
		}
		
		private void onNetMobile()
		{
			//移动网络: 您当前使用的是4G网络，在线下载应用将耗费一定的网络流量
			String msg = mContext.getResources().getString(
					R.string.dialog_message_net);
			final PromptDialog dialog = new PromptDialog(mContext, msg);
			dialog.setOnDialogClickListener(new IDialogClick()
			{
				@Override
				public void onClick(View v) 
				{
					switch (v.getId()) 
					{
					case R.id.id_dialog_prompt_sure:
					{
						dialog.dismiss();
						if (FileUtil.isEnough(mContext, appinfo.getPackSize()))
						{
							startDownload();
						}
						else
						{
							//空间不足，建议删除一些下载的音乐文件释放空间后再尝试
							String msg = mContext.getResources().getString(
									R.string.toast_memory_not_enough);
							CommonUtil.makeText(msg);
						}
						break;
					}
					case R.id.id_dialog_prompt_cancel:
					{
						dialog.dismiss();
						break;
					}	
					default:
						break;
					}
				}
			});
			dialog.show();
		}
		
		private void startDownload()
		{
			appinfo.setDownloadStatus(ConstantUtil.DOWNLOAD_STATUS_START);
			setDownloadStatus(viewHolder, position);
			LogUtil.i(LogUtil.TAG, " download urlPath: " + urlPath);
			mDownloadManager.addDownloadTask(urlPath);
			mWatchManager.registerWachter(urlPath, mDownloadCallback);
		}
		
		private void onStartOrPause()
		{
			switch (appinfo.getDownloadStatus())
			{
			case ConstantUtil.DOWNLOAD_STATUS_START: 
			{
				//由“开始下载”-->“暂停下载”
				appinfo.setDownloadStatus(ConstantUtil.DOWNLOAD_STATUS_PAUSE);
				setDownloadStatus(viewHolder, position);
				mDownloadManager.pauseDownloadTask(urlPath);
				break;
			}
			case ConstantUtil.DOWNLOAD_STATUS_PAUSE: 
			{
				//由“暂停下载”-->“开始下载”
				startDownload();
				break;
			}
			default:
				break;
			}
		}
		
		private void onCancel()
		{
			//提示“是否确定取消？”
			String msg = mContext.getResources().getString(
					R.string.dialog_message_cancel);
			final PromptDialog dialog = new PromptDialog(mContext, msg);
			dialog.setOnDialogClickListener(new IDialogClick()
			{
				@Override
				public void onClick(View v) 
				{
					switch (v.getId()) 
					{
					case R.id.id_dialog_prompt_sure:
					{				
						dialog.dismiss();
						doCancel();
						break;
					}
					case R.id.id_dialog_prompt_cancel:
					{
						dialog.dismiss();
						break;
					}	
					default:
						break;
					}
				}
			});
			dialog.show();
		}
		
		private void doCancel()
		{
			fileLen = 0;
			viewHolder.progressBar.setProgress(0);
			if (mDownloadManager.isDownloadTaskTaskExist(urlPath))
			{
				mDownloadManager.deleteDownloadTask(urlPath);
				mWatchManager.unregisterWachter(urlPath, mDownloadCallback);
			}
			else
			{
				//升级或重启后
				mInfoDao.clearAll(urlPath);
			}
			appinfo.setDownloadStatus(ConstantUtil.DOWNLOAD_STATUS_CANCEL);
			setDownloadStatus(viewHolder, position);
		}
		
		/**
		 * 如果有下载记录，则显示暂停状态，否则显示下载状态
		 */
		private void onError()
		{
			long done = 0, len = 0;
			List<Info> exist = mInfoDao.queryUndone(urlPath);
			for (int i=0; i<exist.size(); i++)
			{
				done += exist.get(i).getDone();
				if (i == exist.size()-1)
				{
					len = exist.get(i).getEnd();
				}
			}
			
			if (len > 0 && done < len)
			{
				long progress = done * 100 / len;
				LogUtil.i(LogUtil.TAG, " progress: " + progress);
				if (progress > 0)
				{
					viewHolder.progressBar.setProgress(progress);
				}
				appinfo.setDownloadStatus(ConstantUtil.DOWNLOAD_STATUS_PAUSE);
				setDownloadStatus(viewHolder, position);
			}
			else
			{
				//add 2016-6-28
				if (mDownloadManager.isDownloadTaskTaskExist(urlPath))
				{
					//避免再次下载时，进入"go on downloadtask", 导致无法再次下载
					mDownloadManager.deleteDownloadTask(urlPath);
					mWatchManager.unregisterWachter(urlPath, mDownloadCallback);
				}
				appinfo.setDownloadStatus(ConstantUtil.DOWNLOAD_STATUS_CANCEL);
				setDownloadStatus(viewHolder, position);
			}
		}
		
		private TaskStatus.ITaskCallBack mDownloadCallback = 
				new TaskStatus.ITaskCallBack()
		{
			@Override
			public void updateTaskStatus(int status) 
			{
				Message msg = handler.obtainMessage(2);
				msg.getData().putInt("status", status);
				handler.sendMessage(msg);
			}
			
			@Override
			public void updateDownloadProcess(long downLength,
					long fileLength) 
			{
				if (fileLength != fileLen) 
				{
					Message msg = handler.obtainMessage(0);
					msg.getData().putLong("fileLen", fileLength);
					handler.sendMessage(msg);
					downLength = 0;
				}
	
				Message msg = handler.obtainMessage(1);
				msg.getData().putLong("done", downLength);
				handler.sendMessage(msg);
			}
		};
		
		@SuppressLint("HandlerLeak") 
		private Handler handler = new Handler() 
		{
			@Override
			public void handleMessage(Message msg) 
			{
				switch (msg.what) 
				{
				case 0: //获取文件大小
				{
					fileLen = msg.getData().getLong("fileLen");
					tempDone = 0;
					viewHolder.progressBar.setMax(100);
					break;
				}
				case 1: //获取下载大小
				{
					if(fileLen == 0)
					{
						return;
					}
					//获取当前下载的总量
					long done = msg.getData().getLong("done");
					long progress = done * 100 / fileLen;
					if (progress > 0)
					{
						//防止进度条回滚
						if (tempDone > 0 && progress < tempDone)
							return;
						//防止界面已经显示"安装"， 再次刷进度
						if (tempDone == 100 && progress == 100)
							return;
						
						//LogUtil.i(LogUtil.TAG, "+++ setProgress: " + progress);
						viewHolder.progressBar.setProgress(progress);
						tempDone = progress;
						
						//以防DOWNLOAD_STATUS_END没有接收到，导致下载完成后，没有更新状态
						if (progress == 100)
						{
							LogUtil.i(LogUtil.TAG, "+++ download finish 1111 +++ ");
							appinfo.setDownloadStatus(ConstantUtil.DOWNLOAD_STATUS_FINISH);
							setDownloadStatus(viewHolder, position);
						}
					}
					break;
				}
				case 2: //获取下载状态
				{
					switch (msg.getData().getInt("status")) 
					{
					case TaskStatus.DOWNLOAD_STATUS_ING:
					{
						break;
					}
					case TaskStatus.DOWNLOAD_STATUS_END:
					{
						if (appinfo.getDownloadStatus() != ConstantUtil.
								DOWNLOAD_STATUS_FINISH)
						{
							LogUtil.i(LogUtil.TAG, "+++ download finish 0000 +++ ");
							appinfo.setDownloadStatus(ConstantUtil.DOWNLOAD_STATUS_FINISH);
							setDownloadStatus(viewHolder, position);
						}
						break;
					}
					case TaskStatus.DOWNLOAD_STATUS_ERROR:
					{
						LogUtil.i(LogUtil.TAG, "+++ DOWNLOAD_STATUS_ERROR +++ ");
						CommonUtil.makeText(R.string.toast_download_failed);
						onError();
						break;
					}
					case TaskStatus.DOWNLOAD_STATUS_NETERROR:
					{
						LogUtil.i(LogUtil.TAG, "+++ DOWNLOAD_STATUS_NETERROR +++ ");
						CommonUtil.makeText(R.string.toast_download_failed);
						onError();
						break;
					}
					default:
						break;
					}
				}
				default:
					break;
				}
			}
		};
	}
}