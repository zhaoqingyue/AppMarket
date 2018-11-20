package cld.kcloud.user;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.WindowManager;
import cld.kcloud.center.KCloudAppUtils;
import cld.kcloud.center.KCloudAppUtils.CLDMessageId;
import cld.kcloud.center.R;
import cld.kcloud.fragment.LoginFragment;
import cld.kcloud.fragment.PasswordFragment;
import cld.kcloud.fragment.manager.BaseFragment;
import cld.kcloud.fragment.manager.BaseFragment.BackHandledInterface;
import cld.kcloud.user.KCloudUser.CldOnMessageInterface;
import cld.kcloud.utils.KCloudCommonUtil;
import com.cld.ols.api.CldKAccountAPI;

public class KCloudUserActivity extends FragmentActivity implements BackHandledInterface {
	private static final String TAG = "KCloudUserActivity";
	private BaseFragment mCurrentFragment;
	private LoginFragment mLoginFragment = null;
	private PasswordFragment mPasswordFragment = null;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		int extra = getIntent().getIntExtra(KCloudAppUtils.START_ACTIVITY_EXTRA, 0);
		if (CldKAccountAPI.getInstance().isLogined()) {
			// 进入用户信息界面
			Intent intent = new Intent(this, KCloudUserInfoActivity.class);
			intent.putExtra(KCloudAppUtils.START_ACTIVITY_EXTRA, extra);
			startActivity(intent);
			finish();
			return ;
		} else if (!CldKAccountAPI.getInstance().getLoginName().isEmpty()
			&& !CldKAccountAPI.getInstance().getLoginPwd().isEmpty()) {
			CldKAccountAPI.getInstance().startAutoLogin();
			
			Intent intent = new Intent(this, KCloudUserInfoActivity.class);
			intent.putExtra(KCloudAppUtils.START_ACTIVITY_EXTRA, extra);
			startActivity(intent);
			finish();
			return;			
		}

		setContentView(R.layout.activity_user);     
	
		// 添加fragment
		FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
       
        if (mLoginFragment == null) {
        	mLoginFragment = new LoginFragment();
        	fragmentTransaction.add(R.id.fragment_user_container, mLoginFragment, "FragmentLogin");
        } else {
        	fragmentTransaction.show(mLoginFragment);
        }
        fragmentTransaction.commit();
		
		// 设置回调
		KCloudUser.getInstance().setOnMessageListener(mCldOnMessageListener);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void setSelectedFragment(BaseFragment selectedFragment) {
		this.mCurrentFragment = selectedFragment;
	}
	
	@Override
	public void onBackPressed() {
		if (mCurrentFragment == null || !mCurrentFragment.onBackPressed()) {
			if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
				super.onBackPressed();
			} else {
				getSupportFragmentManager().popBackStack();
			}
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
        
    	mPasswordFragment = new PasswordFragment();
        fragmentTransaction.add(R.id.fragment_user_container, mPasswordFragment, "FragmentPassword");
        fragmentTransaction.commit();		
	}
	
	/**
	 * 
	 * @Title: doDirectLoginFail
	 * @Description: 直接登录失败
	 * @return: void
	 */
	public void doDirectLoginFail() {
		FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (mPasswordFragment != null) {
        	fragmentTransaction.hide(mPasswordFragment);
        }
        
        mLoginFragment = new LoginFragment();
        fragmentTransaction.add(R.id.fragment_user_container, mLoginFragment, "FragmentLogin");
        fragmentTransaction.commit();	
	}

	/**
	 * 返回
	 */
	public void doBack() {
		FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (mPasswordFragment != null) {
        	fragmentTransaction.remove(mCurrentFragment);
        	mPasswordFragment = null;
        }
        if (mLoginFragment != null) {
        	fragmentTransaction.show(mLoginFragment);
        }
        fragmentTransaction.commit();		
	}

	private CldOnMessageInterface mCldOnMessageListener = new CldOnMessageInterface() {

		@Override
		public void OnHandleMessage(Message message) {
			if (message.what == CLDMessageId.MSG_ID_LOGIN_LOST_PWD) {
				doLostPwd();
			} else if (message.what >= CLDMessageId.MSG_ID_LOGIN_GET_QRCODE_SUCCESS
					&& message.what <= CLDMessageId.MSG_ID_LOGIN_THIRD_LOGIN_FAILED) {
				if (mLoginFragment != null) {
					mLoginFragment.onHandleMessage(message);
				}
				
				if (message.what == CLDMessageId.MSG_ID_LOGIN_ACCOUNT_LOGIN_FAILED) {
					if (mPasswordFragment != null && mPasswordFragment.isFromPassword()) {
						mPasswordFragment.onHandleMessage(message);
					}
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
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "onResume");
		
		//通知launcher, 关闭协议界面
		KCloudCommonUtil.sendCloseServiceInterface();
		
		//强制隐藏输入法
		getWindow().setSoftInputMode(WindowManager.
				LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}
	
	@Override 
	public void onWindowFocusChanged(boolean hasFocus) { 
		//mHandler.sendEmptyMessageDelayed(1, 1000);
	    super.onWindowFocusChanged(hasFocus); 
	}
	
	@SuppressLint("HandlerLeak") 
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if (KCloudCommonUtil.isActivityShow(KCloudAppUtils.TARGET_CLASS_NAME_USER)) {
					//通知launcher, 关闭协议界面
					KCloudCommonUtil.sendCloseServiceInterface();
				} else {
					mHandler.sendEmptyMessageDelayed(0, 100);
				}
				break;
				
			case 1:
				//通知launcher, 关闭协议界面
				KCloudCommonUtil.sendCloseServiceInterface();
				break;
			default:
				break;
			}
		};
	};
}
