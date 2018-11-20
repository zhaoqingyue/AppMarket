package cld.kcloud.fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.cld.log.CldLog;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.View.OnClickListener;
import cld.kcloud.center.R;
import cld.kcloud.fragment.manager.BaseFragment;
import cld.kcloud.user.KCloudUserInfoActivity;
import cld.kcloud.widget.controller.KCloudController;
import cld.kcloud.widget.controller.KCloudWidgetList;

public class PersonalMessageDetailFragment extends BaseFragment {
	private static final String TAG = "FragmentUserMessageDetail";
	private View viewUserMessageDetail = null;
	private KCloudWidgetList mWidgetList = new KCloudWidgetList();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (viewUserMessageDetail == null) {
			viewUserMessageDetail = inflater.inflate(
					R.layout.fragment_user_message_detail, container, false);
			
			bindControl(R.id.message_detail_text_title,
					viewUserMessageDetail.findViewById(R.id.message_detail_text_title), null, true, true);			
			bindControl(R.id.message_detail_text_content,
					viewUserMessageDetail.findViewById(R.id.message_detail_text_content), null, true, true);
			bindControl(R.id.message_detail_text_time,
					viewUserMessageDetail.findViewById(R.id.message_detail_text_time), null, true, true);
		}
		return viewUserMessageDetail;
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		TextView tvMsgTitle = (TextView) getControl(R.id.message_detail_text_title);
		TextView tvMsgContent = (TextView) getControl(R.id.message_detail_text_content);
		TextView tvTime = (TextView) getControl(R.id.message_detail_text_time);

		if (getArguments() != null) {
			tvMsgTitle.setText(getArguments().getString("msgTitle"));
			tvMsgContent.setText(getArguments().getString("msgContent"));
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date date = new Date(getArguments().getLong("msgTime")*1000L);
			tvTime.setText(sdf.format(date));
		}
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public boolean onBackPressed() {
		if (getActivity() != null) 
			((KCloudUserInfoActivity) getActivity()).doBack();
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
}
