package cld.kcloud.fragment.manager;

import java.lang.reflect.Field;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;

public abstract class BaseFragment extends Fragment {
	
	public enum FragmentType {
		eFragment_Personal,
		eFragment_PersonalMessage,
		eFragment_PersonalMessageDetail,
		eFragment_PersonalPassword,
		eFragment_PersonalCity,
		eFragment_PersonalMobile,
		eFragment_Car,
		eFragment_CarSelector,
		eFragment_CarPlateSelector,
		eFragment_Service,
		eFragment_ServiceDetail,
		eFragment_ServiceRenewal,
		eFragment_Flow,
	};
	
	protected BackHandledInterface mBackHandledInterface; 
	/**
	 * ���м̳�BackHandledFragment�����඼�������������ʵ������Back�����º���߼�
	 */
	public abstract boolean onBackPressed();

	/**
	 * fragment��Ϣ����
	 * @param message
	 */
	public abstract void onHandleMessage(Message message);
	
	/**
	 * fragmentˢ��
	 */
	public void onUpdate() {};

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!(getActivity() instanceof BackHandledInterface)) {
			throw new ClassCastException(
				"Hosting Activity must implement BackHandledInterface");
		} else {
			this.mBackHandledInterface = (BackHandledInterface) getActivity();
		}
	}
 
    @Override
    public void onStart() {
        // ����FragmentActivity����ǰFragment��ջ��
        mBackHandledInterface.setSelectedFragment(this);
    	super.onStart();
    }
    
    @Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
    	if (!hidden) {
    		mBackHandledInterface.setSelectedFragment(this);
    	}
		super.onHiddenChanged(hidden);
	}

	public interface BackHandledInterface {
        public abstract void setSelectedFragment(BaseFragment selectedFragment);
    }
}
