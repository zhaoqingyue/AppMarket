package cld.kcloud.fragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.tsz.afinal.FinalBitmap;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Message;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import cld.kcloud.center.KCloudAppUtils;
import cld.kcloud.center.KCloudAppUtils.CLDMessageId;
import cld.kcloud.center.R;
import cld.kcloud.custom.bean.KCloudUserInfo;
import cld.kcloud.custom.bean.KCloudUserInfo.ChangeTaskEnum;
import cld.kcloud.custom.manager.KCloudMsgBoxManager;
import cld.kcloud.fragment.manager.BaseFragment;
import cld.kcloud.service.KCloudService;
import cld.kcloud.user.KCloudUser;
import cld.kcloud.user.KCloudUserActivity;
import cld.kcloud.user.KCloudUserInfoActivity;
import cld.kcloud.utils.KCloudCommonUtil;
import cld.kcloud.utils.KCloudShareUtils;
import cld.kcloud.utils.control.CldInputDialog;
import cld.kcloud.utils.control.CldProgress;
import cld.kcloud.utils.control.CldProgress.CldProgressListener;
import cld.kcloud.utils.control.CldPromptDialog;
import cld.kcloud.utils.control.CldPromptDialog.PromptDialogListener;
import cld.kcloud.widget.controller.KCloudController;
import cld.kcloud.widget.controller.KCloudWidgetList;
import com.cld.device.CldPhoneNet;
import com.cld.log.CldLog;
import com.cld.ols.api.CldKAccountAPI;
import com.cld.ols.bll.CldBllKAccount;
import com.cld.ols.dal.CldDalKAccount;
import com.cld.ols.sap.bean.CldSapKAParm.CldUserInfo;

public class PersonalFragment extends BaseFragment implements OnClickListener {
	private static final String TAG = "PersonalFragment";
	private View viewUserInfo = null;
	private FinalBitmap mFinalBitmap = null;
	private KCloudUserInfo mUserInfo = null;
	private KCloudWidgetList mWidgetList = new KCloudWidgetList();
	private boolean isSexSelectorShowing = false;
	private String male = KCloudCommonUtil.getString(R.string.setting_male);
	private String female = KCloudCommonUtil.getString(R.string.setting_female);
	private String unset = KCloudCommonUtil.getString(R.string.setting_unset);
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
	public void onResume() {
		super.onResume();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (viewUserInfo == null) {
			viewUserInfo = inflater.inflate(R.layout.fragment_user_info,
					container, false);
			
			bindControl(R.id.userinfo_img_head,
					viewUserInfo.findViewById(R.id.userinfo_img_head), 
					this, true, true);

			bindControl(R.id.userinfo_btn_message,
					viewUserInfo.findViewById(R.id.userinfo_btn_message), 
					this, true, true);
			bindControl(R.id.userinfo_btn_password_modify,
					viewUserInfo.findViewById(R.id.userinfo_btn_password_modify),
					this, true, true);
			/*去掉"切换账号"*/
			bindControl(R.id.userinfo_btn_loginout,
					viewUserInfo.findViewById(R.id.userinfo_btn_loginout),
					this, false, true);
			
			bindControl(R.id.userinfo_btn_nick_modify,
					viewUserInfo.findViewById(R.id.userinfo_btn_nick_modify),
					this, true, true);
			bindControl(R.id.userinfo_btn_sex_modify,
					viewUserInfo.findViewById(R.id.userinfo_btn_sex_modify),
					this, true, true);
			bindControl(R.id.userinfo_btn_city_modify,
					viewUserInfo.findViewById(R.id.userinfo_btn_city_modify),
					this, true, true);
			bindControl(R.id.userinfo_btn_mobile_modify,
					viewUserInfo.findViewById(R.id.userinfo_btn_mobile_modify),
					this, true, true);
			
			bindControl(R.id.userinfo_imgbtn_nick_modify,
					viewUserInfo.findViewById(R.id.userinfo_imgbtn_nick_modify),
					this, true, true);
			bindControl(R.id.userinfo_imgbtn_sex_modify,
					viewUserInfo.findViewById(R.id.userinfo_imgbtn_sex_modify),
					this, true, true);
			bindControl(R.id.userinfo_imgbtn_city_modify,
					viewUserInfo.findViewById(R.id.userinfo_imgbtn_city_modify),
					this, true, true);
			bindControl(R.id.userinfo_imgbtn_mobile_modify,
					viewUserInfo.findViewById(R.id.userinfo_imgbtn_mobile_modify),
					this, true, true);
			bindControl(R.id.userinfo_imgbtn_password_modify,
					viewUserInfo.findViewById(R.id.userinfo_imgbtn_password_modify),
					this, true, true);

			bindControl(R.id.userinfo_text_account,
					viewUserInfo.findViewById(R.id.userinfo_text_account), 
					null, true, true);
			bindControl(R.id.userinfo_text_nick,
					viewUserInfo.findViewById(R.id.userinfo_text_nick), 
					this, true, true);
			bindControl(R.id.userinfo_text_sex,
					viewUserInfo.findViewById(R.id.userinfo_text_sex), 
					null, true, true);
			bindControl(R.id.userinfo_text_hint_sex,
					viewUserInfo.findViewById(R.id.userinfo_text_hint_sex), 
					null, true, true);
			bindControl(R.id.userinfo_text_city,
					viewUserInfo.findViewById(R.id.userinfo_text_city),
					this, true, true);
			bindControl(R.id.userinfo_text_mobile,
					viewUserInfo.findViewById(R.id.userinfo_text_mobile), 
					this, true, true);
			bindControl(R.id.userinfo_text_password,
					viewUserInfo.findViewById(R.id.userinfo_text_password), 
					null, true, true);
			bindControl(R.id.userinfo_sex_radio_group,
					viewUserInfo.findViewById(R.id.userinfo_sex_radio_group),
					null, false, false);

			bindControl(R.id.userinfo_sex_radio_man,
					viewUserInfo.findViewById(R.id.userinfo_sex_radio_man),
					null, true, true);
			bindControl(R.id.userinfo_sex_radio_women,
					viewUserInfo.findViewById(R.id.userinfo_sex_radio_women),
					null, true, true);

			bindControl(R.id.userinfo_layout_getting_fragment,
					viewUserInfo.findViewById(R.id.userinfo_layout_getting_fragment),
					null, true, false);
			bindControl(R.id.userinfo_layout_success_fragment,
					viewUserInfo.findViewById(R.id.userinfo_layout_success_fragment),
					null, false, true);
			
			bindControl(R.id.userinfo_layout_failed_fragment,
					viewUserInfo.findViewById(R.id.userinfo_layout_failed_fragment),
					this, false, true);
			bindControl(R.id.id_fragment_failed, viewUserInfo.findViewById(R.id.id_fragment_failed),
    				this, true, true);
			bindControl(R.id.id_fragment_failed_reload,
					viewUserInfo.findViewById(R.id.id_fragment_failed_reload),
					this, true, true);
			bindControl(R.id.id_fragment_abnormal, viewUserInfo.findViewById(R.id.id_fragment_abnormal),
    				this, true, true);
    		
    		initControl();
			updateUI();
		}
		return viewUserInfo;

	}

	@Override
	public boolean onBackPressed() {
		return false;
	}

	@Override
	public void onUpdate() {
		updateUI();
	}

	@SuppressLint("NewApi") 
	@Override
	public void onClick(View v) {
		if (isSexSelectorShowing) {
			isSexSelectorShowing = false;
			KCloudController.setVisibleById(R.id.userinfo_sex_radio_group,
					false, mWidgetList);
			((TextView) getControl(R.id.userinfo_text_hint_sex)).
				setTextColor(KCloudCommonUtil.getColor(R.color.text_normal_color));
			((TextView) getControl(R.id.userinfo_text_sex)).
				setTextColor(KCloudCommonUtil.getColor(R.color.text_hightlight_color));
			((Button) getControl(R.id.userinfo_btn_sex_modify)).
				setTextColor(KCloudCommonUtil.getColor(R.color.text_normal_color));
			((ImageButton) getControl(R.id.userinfo_imgbtn_sex_modify)).
				setImageResource(R.drawable.modify_down_unclicked);
			return;
		}
		
		switch (v.getId()) 
		{
		case R.id.userinfo_btn_message: //消息
		{
			//test
			//KCloudPackageManager.getInstance().test();
			//KCloudAlarmManager.getInstance().test();
			//KCloudHeartbeatManager.getInstance().init();
			//KCloudUpgradeManager.getInstance(mContext).init();
			//KCloudSimCardManager.getInstance().test();
			
			if (getActivity() != null) {
				((KCloudUserInfoActivity) getActivity())
				.doChangeFragment(FragmentType.eFragment_PersonalMessage);
			}
			break;
		}
		case R.id.userinfo_imgbtn_password_modify:
		case R.id.userinfo_btn_password_modify: //绑定密码
		{
			if (getActivity() != null) {
				((KCloudUserInfoActivity) getActivity())
				.doChangeFragment(FragmentType.eFragment_PersonalPassword);
			}
			break;
		}
		case R.id.userinfo_text_nick: //昵称
		{
			TextView textNick = (TextView) getControl(R.id.userinfo_text_nick);
			if (textNick != null) {
				if (textNick.getText().toString().equals(unset)) {
					onClick(getControl(R.id.userinfo_btn_nick_modify));
				}
			}
			break;
		}
		case R.id.userinfo_imgbtn_nick_modify:
		case R.id.userinfo_btn_nick_modify: //修改昵称
		{
			final TextView tv = (TextView) getControl(R.id.userinfo_text_nick);
			String title = KCloudCommonUtil.getString(
					R.string.input_dialog_title_nickname);
			String hint = KCloudCommonUtil.getString(
					R.string.input_dialog_hint_nickname);
			
			CldInputDialog.showInputDialog(mContext, title, hint, 
					/*tv.getText().toString()*/mUserInfo.getUserAlias(),
					CldInputDialog.CldInputType.eInputType_NickName,
					CldInputDialog.CldButtonType.eButton_Confirm,
					new CldInputDialog.CldInputDialogListener() {

						@Override
						public void onOk(String strInput) {
							if (strInput != null) {
								String strNick = strInput.replaceAll(" ", "");
								if (strNick.isEmpty()) {
									return;
								}
								if (!CldPhoneNet.isNetConnected()) {
									KCloudCommonUtil.makeText(R.string.common_network_abnormal);
									return;
								}
								if (strNick.length() <= 1) {
									KCloudCommonUtil.makeText(R.string.userinfo_useralis_lenth);
									return;
								}
								String regx = "^[a-zA-Z0-9_\u4e00-\u9fa5]+$";
								Pattern pattern = Pattern.compile(regx);
								Matcher matcher = pattern.matcher(strNick);
								if (!matcher.matches()) {
									// 不包含特殊字符
									KCloudCommonUtil.makeText(R.string.userinfo_useralis_specchar);
									return;
								}
								if (!strNick.equals(mUserInfo.getUserAlias())) {
									int sex = mUserInfo.getSex().equals(male) ? 2 : 1;

									CldProgress.showProgress(mContext, 
											KCloudCommonUtil.getString(R.string.common_network_data_update),
											new CldProgressListener() {
												public void onCancel() {
												}
											});

									KCloudUser.getInstance().getTmpUserInfo()
											.setUserAlias(strNick);
									CldKAccountAPI.getInstance().updateUserInfo(null, strNick,
											null, null, sex, null, -1, -1, -1, null);
								}
							}
						}

						@Override
						public void onCancel() {
							
						}
					});
			break;
		}

		case R.id.userinfo_imgbtn_sex_modify:
		case R.id.userinfo_btn_sex_modify: //修改性别
		{
			RadioButton radioMan = (RadioButton) getControl(R.id.userinfo_sex_radio_man);
			RadioButton radioWomen = (RadioButton) getControl(R.id.userinfo_sex_radio_women);

			if (mUserInfo.getSex().equals(male)) {
				if (radioMan != null) {
					radioMan.setChecked(true);
				}
				if (radioWomen != null) {
					radioWomen.setChecked(false);
				}
			} else {
				if (radioMan != null) {
					radioMan.setChecked(false);
				}
				if (radioWomen != null) {
					radioWomen.setChecked(true);
				}
			}
			
			isSexSelectorShowing = true;
			KCloudController.setVisibleById(R.id.userinfo_sex_radio_group,
					true, mWidgetList);
			((TextView) getControl(R.id.userinfo_text_hint_sex)).
				setTextColor(KCloudCommonUtil.getColor(R.color.text_green_color_userinfo));
			((TextView) getControl(R.id.userinfo_text_sex)).
				setTextColor(KCloudCommonUtil.getColor(R.color.text_green_color_userinfo));
			((Button) getControl(R.id.userinfo_btn_sex_modify)).
				setTextColor(KCloudCommonUtil.getColor(R.color.text_green_color_userinfo));
			((ImageButton) getControl(R.id.userinfo_imgbtn_sex_modify)).
				setImageResource(R.drawable.modify_up_clicked);
			break;
		}
		
		case R.id.userinfo_text_city: //所在地区
		{
			TextView textCity = (TextView) getControl(R.id.userinfo_text_city);
			if (textCity != null) {
				if (textCity.getText().toString().equals(unset)) {
					onClick(getControl(R.id.userinfo_btn_city_modify));
				}
			}
			break;
		}
		case R.id.userinfo_imgbtn_city_modify:
		case R.id.userinfo_btn_city_modify: //修改所在地区
		{
			if (getActivity() != null) {
				((KCloudUserInfoActivity) getActivity())
				.doChangeFragment(FragmentType.eFragment_PersonalCity);
			}
			break;
		}
		
		case R.id.userinfo_text_mobile: //手机号码
		{
			TextView textMobile = (TextView) getControl(R.id.userinfo_text_mobile);
			if (textMobile != null) {
				if (textMobile.getText().toString().equals(unset)) {
					onClick(getControl(R.id.userinfo_btn_mobile_modify));
				}
			}
			break;
		}
		case R.id.userinfo_imgbtn_mobile_modify:
		case R.id.userinfo_btn_mobile_modify: //修改手机号码
		{
			if (getActivity() != null) {
				((KCloudUserInfoActivity) getActivity())
				.doChangeFragment(FragmentType.eFragment_PersonalMobile);
			}
			break;
		}
		
		case R.id.userinfo_btn_loginout: //切换账号
		{
			CldPromptDialog.createPromptDialog(
					mContext, "", 
					KCloudCommonUtil.getString(R.string.dialog_msg_exit),
					KCloudCommonUtil.getString(R.string.dialog_sure), 
					KCloudCommonUtil.getString(R.string.dialog_cancel), 
					new PromptDialogListener() {

						@Override
						public void onSure() {
							CldKAccountAPI.getInstance().loginOut();
					        CldDalKAccount.getInstance().setLoginPwd("");
							CldBllKAccount.getInstance().setLoginStatus(0);
							CldKAccountAPI.getInstance().uninit();
							
							// 同步导航
							KCloudService.onClientListener(1, 0, "");
							
							// 修改本地状态
							KCloudUser.getInstance().setLoginStatus(0);
							KCloudUser.getInstance().getUserInfo().reset();
							KCloudCommonUtil.makeText(R.string.kaccount_logout);
							
							// 进入用户登录界面
							Intent intent = new Intent(mContext, KCloudUserActivity.class);
							startActivity(intent);
							getActivity().finish();
						}

						@Override
						public void onCancel() {
						}
					});
			break;
		}
		/*case R.id.userinfo_layout_failed_fragment: */
		case R.id.id_fragment_failed_reload: 
		{
			/*if (CldPhoneNet.isNetConnected()) */{
				CldKAccountAPI.getInstance().getUserInfo();
				KCloudController.setVisibleById(R.id.userinfo_layout_getting_fragment, 
						true, mWidgetList);
				KCloudController.setVisibleById(R.id.userinfo_layout_success_fragment, 
						false, mWidgetList);
				KCloudController.setVisibleById(R.id.userinfo_layout_failed_fragment,
						false, mWidgetList);
			}
			break;
		}
		default:
			break;
		}
	}

	public void bindControl(int id, View view, OnClickListener listener,
			boolean visible, boolean enable) {
		KCloudController.bindControl(id, view, listener, visible, enable,
				mWidgetList);
	}

	public View getControl(int id) {
		return KCloudController.getControlById(id, mWidgetList);
	}

	@SuppressLint("NewApi") 
	public void initControl() {
		final RadioGroup radiogroup = (RadioGroup) getControl(R.id.userinfo_sex_radio_group);
		if (radiogroup != null) {
			radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					if (radiogroup.getVisibility() != View.VISIBLE)
						return;
					
					TextView textSex = (TextView) getControl(R.id.userinfo_text_sex);
					int sex = 2;
					switch (checkedId) {
						case R.id.userinfo_sex_radio_man:
							textSex.setText(male);
							sex = 2;
							break;
						case R.id.userinfo_sex_radio_women:
							textSex.setText(female);
							sex = 1;
							break;
					}
					if ((sex == 2 && !mUserInfo.getSex().equals(male))
							|| (sex == 1 && mUserInfo.getSex().equals(male))) {

						CldProgress.showProgress(mContext, 
								KCloudCommonUtil.getString(R.string.common_network_data_update),
								new CldProgressListener() {
									public void onCancel() {
									}
								});

						KCloudUser.getInstance().getTmpUserInfo().setSex(sex);
						
						CldKAccountAPI.getInstance().updateUserInfo(null, null,
								null, null, sex, null, -1, -1, -1, null);
					}
					
					isSexSelectorShowing = false;
					KCloudController.setVisibleById(R.id.userinfo_sex_radio_group,
							false, mWidgetList);
					((TextView) getControl(R.id.userinfo_text_hint_sex)).
						setTextColor(KCloudCommonUtil.getColor(R.color.text_normal_color));
					((TextView) getControl(R.id.userinfo_text_sex)).
						setTextColor(KCloudCommonUtil.getColor(R.color.text_hightlight_color));
					((Button) getControl(R.id.userinfo_btn_sex_modify)).
						setTextColor(KCloudCommonUtil.getColor(R.color.text_normal_color));
					((ImageButton) getControl(R.id.userinfo_imgbtn_sex_modify)).
						setImageResource(R.drawable.modify_down_unclicked);
				}
			});
		}

		// ??
		if (mFinalBitmap == null) {
			mFinalBitmap = FinalBitmap.create(mContext);
			mFinalBitmap.configLoadfailImage(R.drawable.img_head);
			mFinalBitmap.configLoadingImage(R.drawable.img_head);
			CldUserInfo cldinfo = CldKAccountAPI.getInstance().getUserInfoDetail();
			CldLog.i(TAG, "img_head: " + cldinfo.getPhotoPath());
			if (!cldinfo.getPhotoPath().isEmpty()) {
				mFinalBitmap.display(getControl(R.id.userinfo_img_head), cldinfo.getPhotoPath());
			}
		}
		
		mUserInfo = KCloudUser.getInstance().getUserInfo();
		switch (mUserInfo.getSuccess()) {
		case -1:
			KCloudController.setVisibleById(
					R.id.userinfo_layout_getting_fragment, true, mWidgetList);
			KCloudController.setVisibleById(
					R.id.userinfo_layout_success_fragment, false, mWidgetList);
			KCloudController.setVisibleById(
					R.id.userinfo_layout_failed_fragment, false, mWidgetList);
			break;
			
		case 0:
			KCloudController.setVisibleById(
					R.id.userinfo_layout_getting_fragment, false, mWidgetList);
			KCloudController.setVisibleById(
					R.id.userinfo_layout_success_fragment, true, mWidgetList);
			KCloudController.setVisibleById(
					R.id.userinfo_layout_failed_fragment, false, mWidgetList);
			break;
			
		default:
			KCloudController.setVisibleById(
					R.id.userinfo_layout_getting_fragment, false, mWidgetList);
			KCloudController.setVisibleById(
					R.id.userinfo_layout_success_fragment, false, mWidgetList);
			KCloudController.setVisibleById(
					R.id.userinfo_layout_failed_fragment, true, mWidgetList);
			KCloudController.setVisibleById(
					R.id.id_fragment_failed, true, mWidgetList);
			KCloudController.setVisibleById(
					R.id.id_fragment_abnormal, false, mWidgetList);
			break;
		}
	}

	public void onHandleMessage(Message message) {
		CldLog.i(TAG, String.valueOf(message.what));
		if (getActivity() == null) 
			return;
		
		switch (message.what) {
		case CLDMessageId.MSG_ID_USERINFO_GETDETAIL_SUCCESS: {
			updateUI();
			mUserInfo.setSuccess(0);
			KCloudController.setVisibleById(
					R.id.userinfo_layout_getting_fragment, false, mWidgetList);
			KCloudController.setVisibleById(
					R.id.userinfo_layout_success_fragment, true, mWidgetList);
			KCloudController.setVisibleById(
					R.id.userinfo_layout_failed_fragment, false, mWidgetList);
			break;
		}

		case CLDMessageId.MSG_ID_USERINFO_GETDETAIL_FAILED: {
			mUserInfo.setSuccess(1);
			KCloudController.setVisibleById(
					R.id.userinfo_layout_getting_fragment, false, mWidgetList);
			KCloudController.setVisibleById(
					R.id.userinfo_layout_success_fragment, false, mWidgetList);
			KCloudController.setVisibleById(
					R.id.userinfo_layout_failed_fragment, true, mWidgetList);
			KCloudController.setVisibleById(
					R.id.id_fragment_failed, true, mWidgetList);
			KCloudController.setVisibleById(
					R.id.id_fragment_abnormal, false, mWidgetList);
			break;
		}

		case CLDMessageId.MSG_ID_USERINFO_UPDATE_FAILED: {
			if (CldProgress.isShowProgress()) {
				CldProgress.cancelProgress();
			}

			int[] status = mUserInfo.getChangeStatus();

			if (status[0] == 1) {
				mUserInfo.resetChangeStatus(ChangeTaskEnum.eSEX);
				KCloudCommonUtil.makeText(R.string.userinfo_set_sex_failed);
			} else if (status[1] == 1) {
				mUserInfo.resetChangeStatus(ChangeTaskEnum.eUSERALIAS);
				KCloudCommonUtil.makeText(R.string.userinfo_set_nick_failed);
			} else if (status[2] == 1) {
				mUserInfo.resetChangeStatus(ChangeTaskEnum.eDISTNAME);
				KCloudCommonUtil.makeText(R.string.userinfo_set_distname_failed);
			}

			break;
		}

		case CLDMessageId.MSG_ID_USERINFO_UPDATE_SUCCESS: {						
			if (CldProgress.isShowProgress()) {
				CldProgress.cancelProgress();
			}
			updateUI();
			
			//保存昵称，用于快捷设置上显示
			KCloudShareUtils.put(KCloudAppUtils.TAGGET_FIELD_NICKNAME, 
					mUserInfo.getUserAlias());
			//通知更新昵称
			KCloudUser.getInstance().setLoginStatus(2);
			break;
		}
		
		case CLDMessageId.MSG_ID_LOGIN_SESSION_INVAILD: {
			
			CldKAccountAPI.getInstance().loginOut();
			CldDalKAccount.getInstance().setLoginPwd("");
			CldBllKAccount.getInstance().setLoginStatus(0);
			CldKAccountAPI.getInstance().uninit();
			
			// 同步导航
			KCloudService.onClientListener(1, 0, "");
			
			// 修改本地状态
			KCloudUser.getInstance().setLoginStatus(0);
			KCloudUser.getInstance().getUserInfo().reset();
			
			KCloudCommonUtil.makeText(R.string.kaccount_invalid);
			
			// 进入用户登录界面
			Intent intent = new Intent(mContext, KCloudUserActivity.class);
			startActivity(intent);
			getActivity().finish();
			break;
		}
		
		case CLDMessageId.MSG_ID_USERINFO_LOGOUT_SUCCESS: {
			break;
		}
		
		case CLDMessageId.MSG_ID_USERINFO_LOGOUT_FAILED: {
			break;
		}
		
		case CLDMessageId.MSG_ID_MSGBOX_UPDATE: {
			// ????
			ImageButton imgBtn = (ImageButton) getControl(R.id.userinfo_btn_message);
			if (imgBtn != null) {
				if (KCloudMsgBoxManager.getInstance().getUnReadMsgNum() > 0) {
					imgBtn.setImageResource(R.drawable.img_message_on);
				} else {
					imgBtn.setImageResource(R.drawable.img_message_off);
				}
			}
			break;
		}

		default:
			break;
		}
	}

	public void downLoadUserPhoto(final String webPhotoPath,
			final boolean isThirdLogin) {

	}

	@SuppressLint("NewApi") 
	public void updateUI() {
		CldLog.d(TAG, "updateUI");
		
		/*CldUserInfo info = CldKAccountAPI.getInstance().getUserInfoDetail();
		CldLog.i(TAG, " ************************* ");
		CldLog.i(TAG, " UserName: " + info.getLoginName());
		CldLog.i(TAG, "UserAlias: " + info.getUserAlias());
		CldLog.i(TAG, "      Sex: " + info.getSex());
		CldLog.i(TAG, " DistName: " + info.getAddress());
		CldLog.i(TAG, "   Mobile: " + info.getMobile());
		CldLog.i(TAG, " ************************* ");*/
		
		if (!CldPhoneNet.isNetConnected()){
			KCloudController.setVisibleById(R.id.userinfo_layout_getting_fragment, false, mWidgetList);
			KCloudController.setVisibleById(R.id.userinfo_layout_success_fragment, false, mWidgetList);
			KCloudController.setVisibleById(R.id.userinfo_layout_failed_fragment, true, mWidgetList);
			KCloudController.setVisibleById(R.id.id_fragment_failed, false, mWidgetList);
			KCloudController.setVisibleById(R.id.id_fragment_abnormal, true, mWidgetList);
			
			TextView minor = (TextView)getControl(R.id.id_fragment_abnormal);
			if (minor != null){
				minor.setText(KCloudCommonUtil.getString(R.string.appwidget_network_unconnection));
			}
			return;
		}
		
		if (mUserInfo == null)
			return;
		
		/*CldLog.e(TAG, " ************************* ");
		CldLog.e(TAG, " UserName: " + mUserInfo.getUserName());
		CldLog.e(TAG, "UserAlias: " + mUserInfo.getUserAlias());
		CldLog.e(TAG, "      Sex: " + mUserInfo.getSex());
		CldLog.e(TAG, " DistName: " + mUserInfo.getDistName());
		CldLog.e(TAG, "   Mobile: " + mUserInfo.getMobile());
		CldLog.e(TAG, " ************************* ");*/
		
		if (!TextUtils.isEmpty(mUserInfo.getUserName())) {
			TextView textAccount = (TextView) getControl(R.id.userinfo_text_account);
			if (textAccount != null) {
				textAccount.setText(mUserInfo.getUserName());
			}
		}

		final TextView textNick = (TextView) getControl(R.id.userinfo_text_nick);
		if (textNick != null) {
			//Log.d(TAG, "NickName: " + mUserInfo.getUserAlias());
			if (!TextUtils.isEmpty(mUserInfo.getUserAlias())) {
				Rect bounds = new Rect();  
				String text = mUserInfo.getUserAlias();  
				TextPaint paint = textNick.getPaint();  
				paint.getTextBounds(text, 0, text.length(), bounds);  
				int textwidth = bounds.width();  
				//Log.d(TAG, "textwidth: " + textwidth);
		
				//textNick在xml中的宽度是643
				if (textwidth < 643) {
					textNick.setText(mUserInfo.getUserAlias());
				} else {
					/**
					 * 如果字符串最后一个字符是一个中文，显示的省略号就是三个点，如果是英文的那么有可能只显示一个点或者两个点
					 * 解决方法如下：
					 */
					final String nackName = mUserInfo.getUserAlias();
					textNick.post(new Runnable() {  
		                @Override  
		                public void run() {  
		                   
		                    String ellipsizeStr = (String) TextUtils.ellipsize(
		                    		nackName, 
		                    		(TextPaint) textNick.getPaint(), 
		                    		textNick.getMeasuredWidth() - 10, 
		                    		TextUtils.TruncateAt.END);  
		                    textNick.setText(ellipsizeStr);  
		                }  
		            });
				}
			} else {
				textNick.setText(unset);
			}
		}

		TextView textSex = (TextView) getControl(R.id.userinfo_text_sex);
		if (textSex != null) {
			textSex.setText(mUserInfo.getSex());
			if (male.equals(mUserInfo.getSex()) || mUserInfo.getSex().isEmpty()) {
				if (mUserInfo.getSex().isEmpty()) {
					textSex.setText(male);
				}
		
				RadioButton radioMan = (RadioButton) getControl(R.id.userinfo_sex_radio_man);
				RadioButton radioWomen = (RadioButton) getControl(R.id.userinfo_sex_radio_women);
				if (radioMan != null) {
					radioMan.setChecked(true);
				}
				if (radioWomen != null) {
					radioWomen.setChecked(false);
				}
			} else {
				RadioButton radioMan = (RadioButton) getControl(R.id.userinfo_sex_radio_man);
				RadioButton radioWomen = (RadioButton) getControl(R.id.userinfo_sex_radio_women);
				if (radioMan != null) {
					radioMan.setChecked(false);
				}
				if (radioWomen != null) {
					radioWomen.setChecked(true);
				}
			}
		}

		TextView textCity = (TextView) getControl(R.id.userinfo_text_city);
		if (textCity != null) {
			String distName = mUserInfo.getDistName();
			if (!TextUtils.isEmpty(distName)) {
				textCity.setText(distName);
			} else {
				textCity.setText(unset);
			}
		}

		TextView textMobile = (TextView) getControl(R.id.userinfo_text_mobile);
		if (textMobile != null) {
			String mobile = mUserInfo.getMobile();
			if (!TextUtils.isEmpty(mobile)) {
				String temp = mobile;
				temp = mobile.substring(0, 3) + "****"
						+ mobile.substring(7, 11);
				textMobile.setText(temp);
			} else {
				textMobile.setText(unset);
			}
		}
		
		TextView tvPassword = (TextView) getControl(R.id.userinfo_text_password);
		if (tvPassword != null) {
			tvPassword.setText("******");
		}
		
		ImageButton imgBtn = (ImageButton) getControl(R.id.userinfo_btn_message);
		if (imgBtn != null) {
			if (KCloudMsgBoxManager.getInstance().getUnReadMsgNum() > 0) {
				imgBtn.setImageResource(R.drawable.img_message_on);
			} else {
				imgBtn.setImageResource(R.drawable.img_message_off);
			}
		}
	}
}
