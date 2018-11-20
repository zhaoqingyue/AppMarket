package cld.kcloud.utils;

import java.io.UnsupportedEncodingException;

/**
 * ȡ�ø������ִ�������ĸ��,����ĸ�� Title: ChineseCharToEn ע��ֻ֧��GB2312�ַ����еĺ���
 * 
 * @author wuyl
 * 
 */
public class KCloudChinaUnixUtils {
	private final static int[] li_SecPosVaule = { 1601, 1637, 1833, 2078, 2274,
			2302, 2433, 2594, 2787, 3106, 3212, 3472, 3635, 3722, 3730, 3858,
			4027, 4086, 4390, 4558, 4684, 4925, 5249, 5590 };
	private final static String[] lc_FirstLetter = { "A", "B", "C", "D", "E",
			"F", "G", "H", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
			"T", "W", "X", "Y", "Z" };

	/**
	 * ȡ�ø������ִ�������ĸ��,����ĸ��
	 * 
	 * @param str
	 *            �������ִ�
	 * @return ��ĸ��
	 */
	public static String getAllFirstLetter(String str) {
		if (str == null || str.trim().length() == 0) {
			return "";
		}

		String _str = "";
		for (int i = 0; i < str.length(); i++) {
			_str = _str + getFirstLetter(str.substring(i, i + 1));
		}

		return _str;
	}

	/**
	 * ȡ�ø������ֵ�����ĸ,����ĸ
	 * 
	 * @param chinese
	 *            �����ĺ���
	 * @return �������ֵ���ĸ
	 */
	public static String getFirstLetter(String chinese) {
		if (chinese == null || chinese.trim().length() == 0) {
			return "";
		}

		chinese = conversionStr(chinese, "GB2312", "ISO8859-1");
		if (chinese.length() > 1) {// �ж��ǲ��Ǻ���
			int li_SectorCode = (int) chinese.charAt(0);
			int li_PostionCode = (int) chinese.charAt(1);

			li_SectorCode = li_SectorCode - 160; // ��������
			li_PostionCode = li_PostionCode - 160; // ����λ��

			int li_SecPosCode = li_SectorCode * 100 + li_PostionCode; // //
																		// ������λ��
			if (li_SecPosCode > 1600 && li_SecPosCode < 5590) {
				for (int i = 0; i < 23; i++) {
					if (li_SecPosCode >= li_SecPosVaule[i]
							&& li_SecPosCode < li_SecPosVaule[i + 1]) {
						chinese = lc_FirstLetter[i];
						break;
					}
				}
			} else // �Ǻ����ַ�,��ͼ�η��Ż�ASCII��
			{
				chinese = conversionStr(chinese, "ISO8859-1", "GB2312");
				chinese = chinese.substring(0, 1);
			}
		}
		return chinese;
	}

	/**
	 * �ַ�������ת��
	 * 
	 * @param str
	 *            Ҫת��������ַ���
	 * @param charsetName
	 *            ԭ���ı���
	 * @param toCharsetName
	 *            ת����ı���
	 * @return ��������ת������ַ���
	 */
	private static String conversionStr(String str, String charsetName,
			String toCharsetName) {
		try {
			str = new String(str.getBytes(charsetName), toCharsetName);
		} catch (UnsupportedEncodingException e) {
			System.out.println("�ַ�������ת���쳣��" + e.getMessage());
		}
		return str;
	}
}
