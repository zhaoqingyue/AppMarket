package cld.kcloud.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.content.Context;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import cld.kcloud.center.KCloudAppUtils.InputError;

public class KCloudUserUtils {
	
	public static final int MAX_INPUT_LEN = 21;			// ����볤��
	public static final int MIN_LEN_OF_USER_NAME = 3;	// �û�����С���볤��
	public static final int MAX_LEN_OF_USER_NAME = 15;	// �û���������볤��
	public static final int MIN_LEN_OF_USER_PASS = 6;	// ������С���볤��
	public static final int MAX_LEN_OF_USER_PASS = 14;	// ����������볤��
	
	private static boolean bEmail = false;
	
	public static InputError checkInputIsValid(String strUsername, String strPassword) {
		if (null == strUsername || null == strPassword) {
			return InputError.eERROR_BASE;
		}
		
		if (TextUtils.isEmpty(strUsername)) {
			return InputError.eERROR_ACCOUNT_EMPTY;	// �˺�Ϊ��
		}
		
		if (TextUtils.isEmpty(strPassword)) {
			return InputError.eERROR_PASSWORD_EMPTY; // ����Ϊ��			
		}

		bEmail = isEmail(strUsername);
		if (bEmail && (strUsername.length() < MIN_LEN_OF_USER_NAME || strUsername.length() > MAX_INPUT_LEN - 1))	//����
		{
			return InputError.eERROR_EMAIL_INPUT;	// ����������ַ
		} else if (!bEmail && (strUsername.length() < MIN_LEN_OF_USER_NAME || strUsername.length() > MAX_LEN_OF_USER_NAME)) {
			return InputError.eERROR_ACCOUNT_INPUT;	// ��Ч������
		}

		if (strPassword.length() < MIN_LEN_OF_USER_PASS || strPassword.length() > MAX_LEN_OF_USER_PASS)
		{
			return InputError.eERROR_PASSWORD_INPUT; // ��Ч������
		}

		return InputError.eERROR_NONE;
	}
	
	private static final boolean isEmail(String strInput) {
		boolean bFlag1 = false;	//@
		boolean bFlag2 = false;	//.
		boolean bFlag3 = false;	//[a-zA-Z0-9_-]
		boolean bFlag4 = false;	//@��.���д�
		boolean bFlag5 = false;	//.���д�
		char[] stTmpInput = strInput.toCharArray();
		
		if (null == strInput || TextUtils.isEmpty(strInput)) {
			return false;
		}
		
		for (int i = 0; i < stTmpInput.length; i++)	{
			if (stTmpInput[i] == '@') {
				if (bFlag1)	{
					return false;
				}

				if (bFlag3 == false) {
					return false;
				}

				bFlag1 = true;
				continue;
			}
			else if (stTmpInput[i] == '.') {
				if (bFlag2) {
					return false;
				}
				
				if (bFlag1 == false || bFlag3 == false || bFlag4 == false) {
					return false;
				}
				bFlag2 = true;
				continue;
			}
			 
			if ((stTmpInput[i] >= 'a' && stTmpInput[i] <= 'z') 
					|| (stTmpInput[i] >= 'A' && stTmpInput[i] <= 'Z')
					|| (stTmpInput[i] >= '0' && stTmpInput[i] <= '9') 
					|| (stTmpInput[i] == '_')
					|| (stTmpInput[i] == '-')
					) 
			{
				bFlag3 = true;
				if (bFlag1 == true && bFlag2 == false)
				{
					bFlag4 = true;
				}

				if (bFlag1 == true && bFlag2 == true && bFlag4 == true)
				{
					bFlag5 = true;
				}
			}
			else
			{
				return false;	//�Ƿ���
			}

		}

		if (bFlag1 && bFlag2 && bFlag3 && bFlag4 && bFlag5) {
			return true;
		}

		return false;
	}
	
	/**
	 * �������ٺ�һ����ĸһ������
	 * 
	 * @param strPassword
	 * @return
	 * @return boolean
	 */
	public static boolean isNewPwdInValid(String strPassword) {
		boolean isInvalid = false;
		
		String regx = "[a-zA-Z]";
		Pattern pattern = Pattern.compile(regx);
		Matcher matcher = pattern.matcher(strPassword);
		if (matcher.find()) {
			isInvalid = true;
		}
		regx = "[0-9]";
		pattern = Pattern.compile(regx);
		matcher = pattern.matcher(strPassword);
		if (!matcher.find()) {
			isInvalid = false;
		}
		return isInvalid;
	}
	
	/**
	 * �Ƿ��������ַ�
	 * 
	 * @param strPassword
	 * @return
	 * @return boolean
	 */
	public static boolean isNewPwdContainsSpecail(String strPassword) {
		String regx = "^[a-zA-Z0-9_\u4e00-\u9fa5]+$";
		Pattern pattern = Pattern.compile(regx);
		Matcher matcher = pattern.matcher(strPassword);
		if (!matcher.matches()) {
			// �����������ַ�
			return false;
		}
		return true;
	}
	
	/**
	 * У����������
	 * @param strPassword
	 * @return
	 */
	public static InputError checkRetrivePwd(String strPassword) {
		if (TextUtils.isEmpty(strPassword)) {
			return InputError.eERROR_PASSWORD_EMPTY; // ����Ϊ��
		} else if (strPassword.length() < MIN_LEN_OF_USER_PASS 
				|| strPassword.length() > MAX_LEN_OF_USER_PASS) {
			return InputError.eERROR_PASSWORD_INPUT; // ��Ч������
		} else if (!isNewPwdContainsSpecail(strPassword)) {
			return InputError.eERROR_PASSWORD_CONTAINS_SPECAIL; // ������������ַ�
		} else if (!isNewPwdInValid(strPassword)) {
			return InputError.eERROR_PASSWORD_LESSONENUM; // ���������ٰ���1�����ֺ�1����ĸ
		} 
		
		return InputError.eERROR_NONE;
	}
	
	/**
	 * У���޸�����NewPwd
	 * 
	 * @param mContext
	 * @param oldPwd
	 * @param newPwd
	 * @param newPwdConfirm
	 * @return void
	 */
	public static InputError checkRevisePwd(String oldPwd, String newPwd,
			String newPwdConfirm) {
		// if (TextUtils.isEmpty(oldPwd)) {
		// return InputError.eERROR_OLD_PASSWORD_EMPTY;
		// } else
		if (TextUtils.isEmpty(newPwd)) {
			return InputError.eERROR_NEW_PASSWORD_EMPTY;
		} else if (TextUtils.isEmpty(newPwdConfirm)) {
			return InputError.eERROR_AFFIRM_PASSWORD_EMPTY;
		} else if (newPwd.length() < MIN_LEN_OF_USER_PASS
				|| newPwd.length() > MAX_LEN_OF_USER_PASS) {
			return InputError.eERROR_PASSWORD_INPUT; // ��Ч������
		} else if (!isNewPwdContainsSpecail(newPwd)) {
			return InputError.eERROR_PASSWORD_CONTAINS_SPECAIL; // ������������ַ�
		} else if (!isNewPwdInValid(newPwd)) {
			return InputError.eERROR_PASSWORD_LESSONENUM; // ���������ٰ���1�����ֺ�1����ĸ
		}
		// else if (newPwd.equals(oldPwd)) {
		// return InputError.eERROR_NEW_OLD_SAME; // �����벻�����������ͬ
		// }
		else if (!newPwd.equals(newPwdConfirm)) {
			return InputError.eERROR_NEW_AFFIRM_UNSAME; // ������������벻һ��
		}

		return InputError.eERROR_NONE;
	}
	
	public static void setInputMethodVisible(Context context, EditText edit, boolean visible) {
		/*InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		
		if (visible) {
			imm.showSoftInput(edit, 0);  
		} else if (imm.isActive()){
			imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
		}*/
	}
	
	public static void hideInputMethod(Context context, EditText edit)
	{
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.showSoftInput(edit, InputMethodManager.SHOW_FORCED); 
		imm.hideSoftInputFromWindow(edit.getWindowToken(), 0); //ǿ�����ؼ���  
	}
}
