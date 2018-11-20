/*
 * @Title��HMIQRCode.java
 * @Copyright��Copyright 2010-2014 Careland Software Co,.Ltd All Rights Reserved.
 * @Description�����ɶ�ά��
 * @author��xiaoquan
 * @date��2014-10-17 ����9:20:59
 * @version��1.0
 */
package cld.kcloud.utils.control;


import java.util.Hashtable;

import android.graphics.Bitmap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

/**
 * @Description ���ɶ�ά�� ��Ҫzxing-core-1.6.jar��֧��
 * @author xiaoquan
 * @date 2014-10-17 ����9:20:59
 */
public class CldQRCode {
	private static final int BLACK = 0xff000000;

	public static Bitmap createQRCode(String str, int widthAndHeight)
			throws WriterException {
		Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
		BitMatrix matrix = new MultiFormatWriter().encode(str,
				BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight, hints);
		// �Զ���ױ߱߿��� //
		int margin = 5;
		// �����µ�bitMatrix ȥ���ױ� //
		matrix = updateBit(matrix, margin);
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		int[] pixels = new int[width * height];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (matrix.get(x, y)) {
					pixels[y * width + x] = BLACK;
				} else {
					pixels[y * width + x] = 0xFFFFFFFF;
				}
			}
		}
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		if (width != widthAndHeight) {
			// ȥ�˰ױ߹��󣬿��ܴ�С��Ŀ���С��һ�£���һ������ //
			Bitmap scaleBitmap = Bitmap.createScaledBitmap(bitmap,
					widthAndHeight, widthAndHeight, false);
			return scaleBitmap;
		} else {
			return bitmap;
		}
	}
	
	/** 
	 * 
	 * @param str ��ά���ַ�
	 * @param widthAndHeight ��ά��ͼƬ�߿�
	 * @param margin �ױ߿��
	 * @return Bitmap
	 * @author Jiangli
	 */ 
	public static Bitmap createQRCode(String str, int widthAndHeight,int margin)
			throws WriterException {
		Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
		BitMatrix matrix = new MultiFormatWriter().encode(str,
				BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight, hints);
		// �Զ���ױ߱߿��� //
		matrix = updateBit(matrix, margin);
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		int[] pixels = new int[width * height];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (matrix.get(x, y)) {
					pixels[y * width + x] = BLACK;
				} else {
					pixels[y * width + x] = 0xFFFFFFFF;
				}
			}
		}
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		if (width != widthAndHeight) {
			// ȥ�˰ױ߹��󣬿��ܴ�С��Ŀ���С��һ�£���һ������ //
			Bitmap scaleBitmap = Bitmap.createScaledBitmap(bitmap,
					widthAndHeight, widthAndHeight, false);
			return scaleBitmap;
		} else {
			return bitmap;
		}
	}

	/**
	 * @Description �Զ�����ưױ߿��
	 * @author��xiaoquan
	 * @date��2015-1-5 ����5:12:31
	 * @param matrix
	 * @param margin �߿���
	 * @return
	 * @return BitMatrix
	 */
	private static BitMatrix updateBit(BitMatrix matrix, int margin) {
		int tempM = margin * 2;
		// ��ȡ��ά��ͼ�������� //
		int[] rec = matrix.getEnclosingRectangle();
		int resWidth = rec[2] + tempM;
		int resHeight = rec[3] + tempM;
		// �����Զ���߿������µ�BitMatrix //
		BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
		resMatrix.clear();
		for (int i = margin; i < resWidth - margin; i++) {
			// ѭ��������ά��ͼ�����Ƶ��µ�bitMatrix�� //
			for (int j = margin; j < resHeight - margin; j++) {
				if (matrix.get(i - margin + rec[0], j - margin + rec[1])) {
					resMatrix.set(i, j);
				}
			}
		}
		return resMatrix;
	}

	/**
	 * ���ܶ�ά���ַ���
	 * 
	 * @param ch
	 * @return
	 */
	public static int GetIndexEncrypt(char ch) {
		if (ch == '-')
			return 0;
		else if (ch == ';')
			return 1;
		else if (ch >= '0' && ch <= '9')
			return (int) (ch - '0' + 2);
		else if (ch >= 'A' && ch <= 'Z')
			return (int) (ch - 'A' + 12);
		return 0;
	}

	/**
	 * ���ܶ�ά���ַ���
	 * 
	 * @param index
	 * @return
	 */
	public static char GetChEncrpyt(int index) {
		if (index >= 0 && index <= 25)
			return (char) ('a' + index);
		else if (index >= 26 && index <= 35)
			return (char) ('0' + index - 26);
		else if (index >= 36 && index <= 61)
			return (char) ('A' + index - 36);
		return '0';
	}
}
