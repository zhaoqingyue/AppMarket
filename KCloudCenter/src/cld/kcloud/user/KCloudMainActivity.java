package cld.kcloud.user;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import cld.kcloud.center.KCloudAppUtils.CLDMessageId;
import cld.kcloud.center.R;
import cld.kcloud.fragment.LoginFragment;
import cld.kcloud.fragment.PasswordFragment;
import cld.kcloud.fragment.manager.BaseFragment;
import cld.kcloud.fragment.manager.BaseFragment.BackHandledInterface;
import cld.kcloud.user.KCloudUser.CldOnMessageInterface;
import cld.kcloud.utils.KCloudCommonUtil;
import cld.kcloud.widget.controller.KCloudController;
import cld.kcloud.widget.controller.KCloudWidgetList;
import com.cld.log.CldLog;

public class KCloudMainActivity extends FragmentActivity implements BackHandledInterface, OnClickListener {
	private LoginFragment mLoginFragment = null;
	private PasswordFragment mPasswordFragment = null;
	private KCloudWidgetList mWidgetList = new KCloudWidgetList();
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 全屏处理
		setContentView(R.layout.activity_user_main);
		//setFullScreen();

		bindControl(R.id.tv_pass, findViewById(R.id.tv_pass), this, true, true);
		bindControl(R.id.btn_back, findViewById(R.id.btn_back), this, false, true);
		bindControl(R.id.tv_back, findViewById(R.id.tv_back), this, false, true);
		
		// 添加fragment
		FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
       
        if (mLoginFragment == null) {
			Bundle args = new Bundle();
        	mLoginFragment = new LoginFragment();
			args.putString("activity", "main");
			mLoginFragment.setArguments(args);
        	fragmentTransaction.add(R.id.fragment_user_container, mLoginFragment, "FragmentLogin");
        } else {
        	fragmentTransaction.show(mLoginFragment);
        }
        fragmentTransaction.commit();	
        
		// 设置回调
		KCloudUser.getInstance().setOnMessageListener(mCldOnMessageListener);
	}	
	
	@Override
	protected void onResume() 
	{
		//setFullScreen();
		super.onResume();
		// 通知launcher, 关闭协议界面
		//KCloudCommonUtil.sendCloseServiceInterface();
	}

	@Override
	protected void onDestroy() {
		// 通知launcher
		//KCloudCommonUtil.sendCloseServiceInterface();
		super.onDestroy();
	}

	@Override
	public void setSelectedFragment(BaseFragment selectedFragment) {
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.tv_pass:			
			finish();
			break;
			
		case R.id.btn_back:
		case R.id.tv_back:
			doBack();
			break;
		}
	}
	
	/**
	 * 找回密码
	 */
	public void doLostPwd() {
		FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (mLoginFragment != null) {
        	fragmentTransaction.hide(mLoginFragment);
        }
        
        if (mPasswordFragment == null) {
			Bundle args = new Bundle();
        	mPasswordFragment = new PasswordFragment();
			args.putString("activity", "main");
			mPasswordFragment.setArguments(args);
        	fragmentTransaction.add(R.id.fragment_user_container, mPasswordFragment, "FragmentPassword");
        } else {
        	fragmentTransaction.show(mPasswordFragment);
        }
               
        fragmentTransaction.commit();	

        KCloudController.setVisibleById(R.id.tv_pass, false, mWidgetList);
        KCloudController.setVisibleById(R.id.btn_back, true, mWidgetList);
        KCloudController.setVisibleById(R.id.tv_back, true, mWidgetList);
	}
	
	/**
	 * 返回
	 */
	public void doBack() {
		FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (mPasswordFragment != null) {
        	fragmentTransaction.hide(mPasswordFragment);
        }
        
        if (mLoginFragment != null) {
        	fragmentTransaction.show(mLoginFragment);
        }
        
        fragmentTransaction.commit();		
        
        KCloudController.setVisibleById(R.id.tv_pass, true, mWidgetList);
        KCloudController.setVisibleById(R.id.btn_back, false, mWidgetList);
        KCloudController.setVisibleById(R.id.tv_back, false, mWidgetList);
	}

	private CldOnMessageInterface mCldOnMessageListener = new CldOnMessageInterface() {

		@Override
		public void OnHandleMessage(Message message) {
			if (message.what == CLDMessageId.MSG_ID_LOGIN_ACCOUNT_LOGIN_SUCCESS
					|| message.what == CLDMessageId.MSG_ID_LOGIN_MOBILE_LOGIN_SUCCESS) {
				KCloudCommonUtil.sendCloseServiceInterface();
			}
			
			if (message.what == CLDMessageId.MSG_ID_LOGIN_LOST_PWD) {
				doLostPwd();
			} else if (message.what >= CLDMessageId.MSG_ID_LOGIN_GET_QRCODE_SUCCESS
					&& message.what <= CLDMessageId.MSG_ID_LOGIN_THIRD_LOGIN_FAILED) {
				if (mLoginFragment != null) {
					mLoginFragment.onHandleMessage(message);
				}
			} else if (message.what >= CLDMessageId.MSG_ID_PASSWORD_GET_VERICODE_SUCCESS
					&& message.what <= CLDMessageId.MSG_ID_PASSWORD_SET_PWD_FAILED) {
				if (mPasswordFragment != null) {
					mPasswordFragment.onHandleMessage(message);
				}
			} else if (message.what == CLDMessageId.MSG_ID_USERINFO_GETDETAIL_SUCCESS
					|| message.what == CLDMessageId.MSG_ID_USERINFO_GETDETAIL_FAILED) {
				if (mLoginFragment != null) {
					mLoginFragment.onHandleMessage(message);
				}
			}
		}
	};
	
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
	
	/**
	 * 全屏处理
	 */
	@SuppressLint("NewApi")
	public void setFullScreen() {
		/*WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
		wmParams.type = 2026;
		wmParams.format = PixelFormat.TRANSPARENT;
		wmParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN
				| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
				| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		wmParams.width = 1600WindowManager.LayoutParams.MATCH_PARENT;
		wmParams.height = WindowManager.LayoutParams.MATCH_PARENT;
		wmParams.gravity = Gravity.RIGHT;
		
		wmParams.systemUiVisibility =  View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_IMMERSIVE;
		
		this.getWindow().setAttributes(wmParams);*/

		//众鸿提供方法需要全屏需要设置标志，否则有可能出现被压瘪的情况
		int currentApiVersion = android.os.Build.VERSION.SDK_INT;
	
	    // This work only for android 4.4+
	    CldLog.i("KCloudMainActivity", "currentApiVersion = " + currentApiVersion);
	    if (currentApiVersion >= 19) {
	    	int flags = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
	        View decorView = getWindow().getDecorView();
			decorView.setSystemUiVisibility(flags);
	    }
	}
}
