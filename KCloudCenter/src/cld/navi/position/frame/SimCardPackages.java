package cld.navi.position.frame;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import cld.navi.util.NetWorkUtil;

import android.content.Context;
import android.nfc.Tag;
import android.text.StaticLayout;
import android.util.Log;

public class SimCardPackages implements Serializable {

	/**
	 * @Fields serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	public String strId;
	public ArrayList<SimCardPackageInfo> cardpkList;

	static private String fileName = "/SimCardPackage.cld";
	static private String Tag = "YAN";

	public SimCardPackages() {
		strId = "";
		cardpkList = new ArrayList<SimCardPackageInfo>(1);
	}

	static public SimCardPackages readFromFile(Context context) {
		String filePath = context.getCacheDir().getAbsolutePath() + fileName;
		Log.i(Tag, "SimCardPackages-readFromFile:" + filePath);
		File file = new File(filePath);
		FileInputStream in;
		try {
			in = new FileInputStream(file);
			ObjectInputStream objIn = new ObjectInputStream(in);
			SimCardPackages simCardPackages = (SimCardPackages) objIn
					.readObject();
			objIn.close();
			in.close();

			if (simCardPackages != null) {
				Log.i(Tag,
						"read object sucess!"
								+ simCardPackages.cardpkList.size()
								+ simCardPackages.strId);

				// String iccidNum = "89860616090005647845";
				String iccidNum = NetWorkUtil.getICCIDNum(context);
				String imsiNum = NetWorkUtil.getImsi(context);
				String phoneNum = NetWorkUtil.getPhoneNum(context);

				Log.i(Tag,
						"read object sucess!"
								+ simCardPackages.cardpkList.size());

				if (iccidNum != null && iccidNum.equals(simCardPackages.strId)
						|| imsiNum != null
						&& imsiNum.equals(simCardPackages.strId)
						|| phoneNum != null
						&& phoneNum.equals(simCardPackages.strId)) {
					return simCardPackages;
				}
			}
			return null;
		} catch (Exception e) {
			Log.i(Tag, "read object fail!");
			e.printStackTrace();
		}
		return null;
	}

	public void saveToFile(Context context) {
		String filePath = context.getCacheDir().getAbsolutePath() + fileName;

		Log.i(Tag, "SimCardPackages-saveToFile:" + filePath);

		File file = new File(filePath);

		Log.i(Tag, "SimCardPackages-saveToFile_2");
		FileOutputStream out;
		try {
			out = new FileOutputStream(file);
			ObjectOutputStream objOut = new ObjectOutputStream(out);
			objOut.writeObject(this);

			objOut.flush();
			objOut.close();
			Log.i(Tag, "write object success");
		} catch (IOException e) {
			Log.i(Tag, "write object faile");
			e.printStackTrace();
		}
	}
}
