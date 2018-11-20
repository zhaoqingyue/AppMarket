package cld.kcloud.fragment;

import java.lang.reflect.Field;
import net.tsz.afinal.FinalBitmap;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import cld.kcloud.widget.controller.KCloudController;
import cld.kcloud.widget.controller.KCloudWidgetList;
import cld.kcloud.center.R;
import cld.kcloud.fragment.manager.BaseFragment;

public class ServiceDetailContentFragment extends BaseFragment {
	private View viewServiceDetailContent;
	private String icon1;
	private String icon2;
	private String icon3;
	private String icon4;
	private int mServiceCount;
	private FinalBitmap mFinalBitmap;
	private KCloudWidgetList mWidgetList = new KCloudWidgetList();
	private Context mContext;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
	}
	
	public ServiceDetailContentFragment(int serviceCount, String icon1,
			String icon2, String icon3, String icon4) {
		this.mServiceCount = serviceCount;
		this.icon1 = icon1;
		this.icon2 = icon2;
		this.icon3 = icon3;
		this.icon4 = icon4;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(viewServiceDetailContent == null) {
			viewServiceDetailContent = inflater.inflate(R.layout.fragment_service_detail_content, 
					container, false);
			
			bindControl(R.id.service_detail_item_one, 
					viewServiceDetailContent.findViewById(R.id.service_detail_item_one), null, true, true);
			
			if(mServiceCount >= 2) {
				bindControl(R.id.service_detail_item_two, 
						viewServiceDetailContent.findViewById(R.id.service_detail_item_two), null, true, true);
			}else {
				bindControl(R.id.service_detail_item_two, 
					viewServiceDetailContent.findViewById(R.id.service_detail_item_two), null, false, true);
			}
			
			if(mServiceCount >= 3) {
				bindControl(R.id.service_detail_item_three, 
						viewServiceDetailContent.findViewById(R.id.service_detail_item_three), null, true, true);
			}else {
				bindControl(R.id.service_detail_item_three, 
						viewServiceDetailContent.findViewById(R.id.service_detail_item_three), null, false, true);
			}
			
			if(mServiceCount == 4) {
				bindControl(R.id.service_detail_item_four, 
						viewServiceDetailContent.findViewById(R.id.service_detail_item_four), null, true, true);
			}else {
				bindControl(R.id.service_detail_item_four, 
						viewServiceDetailContent.findViewById(R.id.service_detail_item_four), null, false, true);
			}
			
			// ͼƬ
			if (mFinalBitmap == null) {
				mFinalBitmap = FinalBitmap.create(mContext);
				mFinalBitmap.configLoadfailImage(R.drawable.img_service_default);
				mFinalBitmap.configLoadingImage(R.drawable.img_service_default);
			}
		}
		return viewServiceDetailContent;
	}

	@SuppressLint("NewApi") 
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		ImageView iv1 = (ImageView) getControl(R.id.service_detail_item_one);
		if (iv1 != null) {
			if (icon1.isEmpty()) {
				iv1.setImageResource(R.drawable.img_service_default);
			} else {
				mFinalBitmap.display(iv1, this.icon1);
			}
		}
		
		ImageView iv2 = (ImageView) getControl(R.id.service_detail_item_two);
		if (iv2 != null) {
			if (icon2.isEmpty()) {
				iv2.setImageResource(R.drawable.img_service_default);
			} else {
				mFinalBitmap.display(iv2, this.icon2);
			}
		}
		
		ImageView iv3 = (ImageView) getControl(R.id.service_detail_item_three);
		if (iv3 != null) {
			if (icon3.isEmpty()) {
				iv3.setImageResource(R.drawable.img_service_default);
			} else {
				mFinalBitmap.display(iv3, this.icon3);
			}
		}
		
		ImageView iv4 = (ImageView) getControl(R.id.service_detail_item_four);
		if (iv4 != null) {
			if (icon4.isEmpty()) {
				iv4.setImageResource(R.drawable.img_service_default);
			} else {
				mFinalBitmap.display(iv4, this.icon4);
			}
		}
		
	}

	@Override
	public boolean onBackPressed() {
		return false;
	}

	@Override
	public void onHandleMessage(Message message) {

	}

	/**
	 * 
	 * @param id
	 * @param view
	 * @param listener
	 * @param visible
	 * @param enable
	 */
	public void bindControl(int id, View view, OnClickListener listener, boolean visible, boolean enable) {
		KCloudController.bindControl(id, view, listener, visible, enable, mWidgetList);
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public View getControl(int id) {
		return KCloudController.getControlById(id, mWidgetList);
	}
}
