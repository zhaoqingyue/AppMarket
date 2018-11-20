/*
 * @Title：HMIQRCode.java
 * @Copyright：Copyright 2010-2014 Careland Software Co,.Ltd All Rights Reserved.
 * @Description：生成二维码
 * @author：xiaoquan
 * @date：2014-10-17 上午9:20:59
 * @version：1.0
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
 * @Description 生成二维码 需要zxing-core-1.6.jar包支持
 * @author xiaoquan
 * @date 2014-10-17 上午9:20:59
 */
public class CldQRCode {
	private static final int BLACK = 0xff000000;

	public static Bitmap createQRCode(String str, int widthAndHeight)
			throws WriterException {
		Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
		BitMatrix matrix = new MultiFormatWriter().encode(str,
				BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight, hints);
		// 自定义白边边框宽度 //
		int margin = 5;
		// 生成新的bitMatrix 去除白边 //
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
			// 去了白边过后，可能大小与目标大小不一致，做一下缩放 //
			Bitmap scaleBitmap = Bitmap.createScaledBitmap(bitmap,
					widthAndHeight, widthAndHeight, false);
			return scaleBitmap;
		} else {
			return bitmap;
		}
	}
	
	/** 
	 * 
	 * @param str 二维码字符
	 * @param widthAndHeight 二维码图片高宽
	 * @param margin 白边宽带
	 * @return Bitmap
	 * @author Jiangli
	 */ 
	public static Bitmap createQRCode(String str, int widthAndHeight,int margin)
			throws WriterException {
		Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
		BitMatrix matrix = new MultiFormatWriter().encode(str,
				BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight, hints);
		// 自定义白边边框宽度 //
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
			// 去了白边过后，可能大小与目标大小不一致，做一下缩放 //
			Bitmap scaleBitmap = Bitmap.createScaledBitmap(bitmap,
					widthAndHeight, widthAndHeight, false);
			return scaleBitmap;
		} else {
			return bitmap;
		}
	}

	/**
	 * @Description 自定义控制白边宽度
	 * @author：xiaoquan
	 * @date：2015-1-5 下午5:12:31
	 * @param matrix
	 * @param margin 边框宽度
	 * @return
	 * @return BitMatrix
	 */
	private static BitMatrix updateBit(BitMatrix matrix, int margin) {
		int tempM = margin * 2;
		// 获取二维码图案的属性 //
		int[] rec = matrix.getEnclosingRectangle();
		int resWidth = rec[2] + tempM;
		int resHeight = rec[3] + tempM;
		// 按照自定义边框生成新的BitMatrix //
		BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
		resMatrix.clear();
		for (int i = margin; i < resWidth - margin; i++) {
			// 循环，将二维码图案绘制到新的bitMatrix中 //
			for (int j = margin; j < resHeight - margin; j++) {
				if (matrix.get(i - margin + rec[0], j - margin + rec[1])) {
					resMatrix.set(i, j);
				}
			}
		}
		return resMatrix;
	}

	/**
	 * 加密二维码字符串
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
	 * 解密二维码字符串
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
