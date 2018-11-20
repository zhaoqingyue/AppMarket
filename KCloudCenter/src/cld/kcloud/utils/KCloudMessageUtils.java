package cld.kcloud.utils;

import java.util.ArrayList;
import java.util.List;

import com.cld.device.CldPhoneNet;
import com.cld.ols.api.CldKMessageAPI;
import com.cld.ols.api.CldKMessageAPI.ICldKMessageListener;
import com.cld.ols.sap.bean.CldSapKMParm.CldSysMessage;;

public class KCloudMessageUtils implements ICldKMessageListener {
	private static final String TAG = "KCloudMessageUtils";
	
	private boolean mIsFirstInit = false;	// �Ƿ����״γ�ʼ����Ϣ
	private static KCloudMessageUtils mKCloudMessageUtils = null;

	/** ������Ϣ */
	private List<CldSysMessage> mLstMsgHitroy = null;
	/** �û����������Ϣ */
	private List<CldSysMessage> mLstClick = new ArrayList<CldSysMessage>();
	
	public KCloudMessageUtils() {
		mLstMsgHitroy = new ArrayList<CldSysMessage>();

		CldKMessageAPI.getInstance().setCldKMessageListener(this);
	}
	
	public static KCloudMessageUtils getInstance() {
		if (null == mKCloudMessageUtils) {
			synchronized (KCloudMessageUtils.class) {
				mKCloudMessageUtils = new KCloudMessageUtils();
			}
		}
		return mKCloudMessageUtils;
	}
	
	@Override
	public void onRecLastestMsgHistoryResult(int arg0, int arg1,
			List<CldSysMessage> arg2, String arg3) {
		
	}

	@Override
	public void onRecNewMsgHistoryResult(int arg0, List<CldSysMessage> arg1) {
		
	}

	@Override
	public void onRecOldMsgHistoryResult(int arg0, List<CldSysMessage> arg1) {
		
	}

	@Override
	public void onSendPoiResult(int arg0) {
		
	}

	@Override
	public void onSendRouteResult(int arg0) {
		
	}
}