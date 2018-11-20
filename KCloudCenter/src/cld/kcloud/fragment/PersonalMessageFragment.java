package cld.kcloud.fragment;

import cld.kcloud.center.R;
import cld.kcloud.custom.manager.KCloudMsgBoxManager;
import cld.kcloud.custom.view.PersonalMessage;
import cld.kcloud.custom.view.PersonalMessageAdapter;
import cld.kcloud.fragment.manager.BaseFragment;
import cld.kcloud.user.KCloudUserInfoActivity;
import cld.kcloud.utils.KCloudCommonUtil;
import cld.kcloud.widget.controller.KCloudController;
import cld.kcloud.widget.controller.KCloudWidgetList;
import java.util.ArrayList;
import java.util.List;
import com.cld.cc.util.kcloud.ucenter.kcenter.CldSysMessageParce;
import com.cld.log.CldLog;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class PersonalMessageFragment extends BaseFragment {
	private static final String TAG = "PersonalMessageFragment";
	private View viewUserMessage = null;
	private PersonalMessageAdapter mAdapter = null;
	private KCloudWidgetList mWidgetList = new KCloudWidgetList();
	private List<PersonalMessage> mPersonalMessageList = null;
	private int mCurrentPosition;
	private Context mContext;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (viewUserMessage == null) {
			viewUserMessage = inflater.inflate(R.layout.fragment_user_message,
					container, false);
			bindControl(R.id.usermessage_text_null, 
					viewUserMessage.findViewById(R.id.usermessage_text_null), 
					null, true, true);
			bindControl(R.id.usermessage_text_package, 
					viewUserMessage.findViewById(R.id.usermessage_text_package), 
					null, true, true);
			bindControl(R.id.usermessgae_listview_messagelist, 
					viewUserMessage.findViewById(R.id.usermessgae_listview_messagelist),	
					null, true, true);
			updateUI();
		}
		return viewUserMessage;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		ListView listView = (ListView) getControl(R.id.usermessgae_listview_messagelist);
		List<CldSysMessageParce> list = KCloudMsgBoxManager.getInstance().getMsgList();
		if (list.isEmpty()) {
			KCloudController.setVisibleById(R.id.usermessgae_listview_messagelist, 
					false, mWidgetList);
		} else {
			KCloudController.setVisibleById(R.id.usermessage_text_null, false, mWidgetList);
			mPersonalMessageList = new ArrayList<PersonalMessage>();
			for (int i = 0; i < list.size(); i++) {
				PersonalMessage msgInfo = new PersonalMessage();
				msgInfo.setMsgId(list.get(i).getMessageId());
				msgInfo.setMsgType(list.get(i).getMsgType());
				msgInfo.setTime(list.get(i).getCreatetime());
				msgInfo.setMsgTitle(list.get(i).getTitle());
				msgInfo.setMsgContent(list.get(i).getContent());
				msgInfo.setMsgMark(list.get(i).getReadMark());
				mPersonalMessageList.add(msgInfo);
			}		
			mAdapter = new PersonalMessageAdapter(mContext, this, mPersonalMessageList);
			listView.setAdapter(mAdapter);
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
	public void onUpdate() {
		updateUI();
		super.onUpdate();
	}

	@Override
	public void onHandleMessage(Message message) {
		CldLog.i(TAG, String.valueOf(message.what));
//		switch (message.what) {
//		case CLDMessageId.MSG_ID_MSGBOX_UPDATE:
//			List<CldSysMessageParce> list = KCloudMsgBoxManager.getInstance().getMsgList();
//			
//			if (list.isEmpty()) {
//				break;
//			}
//			
//			ListView lvUserMessageList = (ListView) getControl(R.id.usermessgae_listview_messagelist);
//			if (lvUserMessageList == null) {
//				break;
//			}
//			
//			if (mPersonalMessageList == null) {
//				mPersonalMessageList = new ArrayList<PersonalMessage>();
//			}
//			
//			if (mAdapter == null) {
//				mAdapter = new PersonalMessageAdapter(getActivity(), this, mPersonalMessageList);
//				lvUserMessageList.setAdapter(mAdapter);
//			}
//			
//			for (int i = 0; i < list.size(); i++) {
//				CldSysMessageParce item = list.get(i);
//				if (mAdapter.find(item.getMessageId())) {
//					continue;
//				}
//				
//				PersonalMessage msgInfo = new PersonalMessage(item.getMessageId(), item.getMsgType(), item.getDownloadTime(),
//						item.getTitle(), item.getContent());
//				mPersonalMessageList.add(msgInfo);
//			}
//			updateUI();
//			KCloudController.setVisibleById(R.id.usermessage_text_null, false, mWidgetList);
//			KCloudController.setVisibleById(R.id.usermessgae_listview_messagelist, true, mWidgetList);
//			break;
//		}
	}

	/**
	 * 进入详详情界面
	 * 
	 * @param position
	 */
	public void gotoMessageDetail(int position) {
		if (getActivity() == null)
			return;
		
		mCurrentPosition = position;
		KCloudMsgBoxManager.getInstance().setMsgMark(mAdapter.getItemId(position));
		((KCloudUserInfoActivity) getActivity())
				.doChangeFragment(FragmentType.eFragment_PersonalMessageDetail);
	}

	/**
	 * 获取当前信息的详细内容
	 * 
	 * @return
	 */
	public PersonalMessage getPersonalMessage() {
		if (mPersonalMessageList != null) {
			if (mPersonalMessageList.isEmpty()) {
				return null;
			}

			return mPersonalMessageList.get(mCurrentPosition);
		}
		return null;
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
	
	public void updateUI() {
		TextView tv = (TextView) getControl(R.id.usermessage_text_package);
		if (tv != null) {
			
			String str1 = KCloudCommonUtil.getString(R.string.msg_message1);
			String str2 = KCloudCommonUtil.getString(R.string.msg_message2);
			String str = str1 + str2;
			String sText = String.format(str, 
					KCloudMsgBoxManager.getInstance().getMsgNum(),
					KCloudMsgBoxManager.getInstance().getUnReadMsgNum());

			int start = sText.indexOf(KCloudCommonUtil.getString(R.string.msg_message3))+3;
			int end = sText.indexOf(KCloudCommonUtil.getString(R.string.msg_message4));
			SpannableString ss = new SpannableString(sText);
			ss.setSpan(new ForegroundColorSpan(
					KCloudCommonUtil.getColor(R.color.text_green_color_userinfo)), start,
					end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			tv.setText(ss);
		}
		
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}
}
