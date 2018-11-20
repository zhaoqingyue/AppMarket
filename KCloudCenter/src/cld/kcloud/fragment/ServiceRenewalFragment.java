package cld.kcloud.fragment;

import com.cld.log.CldLog;
import com.cld.ols.tools.CldOlsThreadPool;
import com.google.zxing.WriterException;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View.OnClickListener;
import cld.kcloud.center.R;
import cld.kcloud.custom.bean.KCloudPackageInfo;
import cld.kcloud.custom.manager.KCloudPackageManager;
import cld.kcloud.fragment.manager.BaseFragment;
import cld.kcloud.user.KCloudUserInfoActivity;
import cld.kcloud.utils.KCloudNetworkUtils;
import cld.kcloud.utils.control.CldQRCode;
import cld.kcloud.utils.KCloudCommonUtil;
import cld.kcloud.widget.controller.KCloudController;
import cld.kcloud.widget.controller.KCloudWidgetList;

public class ServiceRenewalFragment extends BaseFragment implements OnClickListener{

	public interface IKCloudRenewalListener {
		void onResult(String jsonString);
	}
	
	private static final String TAG = "ServiceRenewalFragment";
	private View viewServiceRenewal = null;
	private boolean mIsStopRunning = false;
	private long mCurTimeId = 0;
	private KCloudWidgetList mWidgetList = new KCloudWidgetList();
	private int comboCode = -1;
	private int comboStatus = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (viewServiceRenewal == null) {
			viewServiceRenewal = inflater.inflate(R.layout.fragment_service_renewal,
					container, false);
			
			bindControl(R.id.service_renewal_qrcode, 
					viewServiceRenewal.findViewById(R.id.service_renewal_qrcode),
					null, false, true);
			
			bindControl(R.id.service_renewal_getting, 
					viewServiceRenewal.findViewById(R.id.service_renewal_getting), 
					null, true, true);
			bindControl(R.id.service_renewal_failed, 
					viewServiceRenewal.findViewById(R.id.service_renewal_failed), 
					this, false, true);
			bindControl(R.id.service_renewal_combo_name, 
					viewServiceRenewal.findViewById(R.id.service_renewal_combo_name), 
					null, true, true);
		}
		return viewServiceRenewal;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {		
		if (getArguments() != null) {
			comboCode = getArguments().getInt("comboCode");
			comboStatus = getArguments().getInt("comboStatus");
		}
		
		KCloudPackageInfo info = KCloudPackageManager.getInstance().getPackageInfoById(comboCode, comboStatus);
		if (info != null) {
			TextView tvRenewalName = (TextView) getControl(R.id.service_renewal_combo_name);
			if (tvRenewalName != null) {
				String text = KCloudCommonUtil.getString(R.string.service_package_name);
				String str = String.format(text, info.getComboName());
				tvRenewalName.setText(str);
			}
		}
		
		setQRCode();
		super.onActivityCreated(savedInstanceState);
	}
	

	@Override
	public void onClick(View v) 
	{
		switch (v.getId()) 
		{
		case R.id.service_renewal_qrcode:
		{
			//test
			//KCloudPackageManager.getInstance().test();
			break;
		}
		case R.id.service_renewal_failed:
		{
			KCloudController.setVisibleById(R.id.service_renewal_failed, false, mWidgetList);
			KCloudController.setVisibleById(R.id.service_renewal_getting, true, mWidgetList);
			
			new Handler().postDelayed(new Runnable(){  
	            public void run() {
	            	setQRCode();
	            } 
	        }, 1000);  
			break;
		}
		default:
			break;
		}
	}
	
	private void setQRCode() {
		mCurTimeId = System.currentTimeMillis();
		String QRCode = KCloudNetworkUtils.getRenewalQRCode(comboCode, mCurTimeId);			
		if (!TextUtils.isEmpty(QRCode)) {
			KCloudController.setVisibleById(R.id.service_renewal_getting, false, mWidgetList);
			KCloudController.setVisibleById(R.id.service_renewal_qrcode, true, mWidgetList);
			
			ImageView imageView = (ImageView) getControl(R.id.service_renewal_qrcode);
			if (imageView != null) {
				Bitmap bmpQR = null;
				try {
					bmpQR = CldQRCode.createQRCode(QRCode,
							imageView.getWidth() > 0 ? imageView.getWidth()
									: 247, 5);
				} catch (WriterException e) {
					e.printStackTrace();
				}
				imageView.setImageBitmap(bmpQR);
			}

			mIsStopRunning = false;
			start_checkRenewalStatus_Running();
		} else {
			KCloudController.setVisibleById(R.id.service_renewal_getting, false, mWidgetList);
			KCloudController.setVisibleById(R.id.service_renewal_failed, true, mWidgetList);
		}
	}
	
	@Override
	public void onDestroy() {
		mIsStopRunning = true;
		super.onDestroy();
	}

	@Override
	public boolean onBackPressed() {
		if (getActivity() != null) {
			if (KCloudPackageManager.getInstance().getPackageSize() > 0) {
				((KCloudUserInfoActivity) getActivity()).doBack();
			} else {
				//套餐个数为0时，从服务界面直接进入续费界面，按返回键时， 直接退出K云
				((KCloudUserInfoActivity) getActivity()).finish();
			}
		}
		return true;
	}

	@Override
	public void onHandleMessage(Message message) {
		CldLog.i(TAG, String.valueOf(message.what));
	}

	/**
	 * 
	 * @param id
	 * @param view
	 * @param listener
	 * @param visible
	 * @param enable
	 */
	public void bindControl(int id, View view, OnClickListener listener,
			boolean visible, boolean enable) {
		KCloudController.bindControl(id, view, listener, visible, enable,
				mWidgetList);
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public View getControl(int id) {
		return KCloudController.getControlById(id, mWidgetList);
	}
	
	private void start_checkRenewalStatus_Running() {
		CldOlsThreadPool.submit(new Runnable() {
			public void run() {
				while (!mIsStopRunning) {
					KCloudNetworkUtils.getRenewalStatus(mCurTimeId, new IKCloudRenewalListener() {
	
						@Override
						public void onResult(String jsonString) {
							if (KCloudPackageManager.getInstance().setPayResult(jsonString)) {
								return ;
							}
						}
					});
					
					try {
						Thread.sleep(30*1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}	// 1分钟检查一次
				}
			}
		});
	}
}
