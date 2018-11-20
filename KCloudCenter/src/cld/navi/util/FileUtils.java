package cld.navi.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import cld.kcloud.custom.manager.KCloudPositionManager;

@SuppressLint("DefaultLocale") public class FileUtils {

	private static String mCommonLogFile = null;
	private static String mLogPath = null;

	public static boolean readLogFile(){

		if(readFile("LogRel.txt") == 1)
			return true;
		else return false;
	}

	public static boolean readServeFile(){

		if(readFile("UpRel.txt") == 1)
			return true;
		else return false;
	}

	public static boolean writeLogFile(){

		if(readFile("LogRel.txt") == 2)
			return true;
		else return false;
	}

	@SuppressWarnings("unused")
	public static int readFile(String filename){
		String path = KCloudPositionManager.getInstance().getPath();
		if(path!=null)
		{
			String logFileName = path + File.separator+filename;
			Log.d("fbh", "logFileName:" + logFileName);
			File logFile = new File(logFileName);
			if(!logFile.exists()) return 0;

			try {
				FileReader fread = new FileReader(logFile);
				if (null == fread) {
					return 0;
				}
				BufferedReader breader = new BufferedReader(fread);
				if (null == breader) {
					if (null != fread) {
						fread.close();
						fread = null;
					}
					return 0;
				}

				String tempLine = breader.readLine();

				if (null == tempLine) {
					if (null != fread) {
						fread.close();
						fread = null;
					}
					if (null != breader) {
						breader.close();
						breader = null;
					}
					return 0;
				}

				String result = tempLine.trim();
				//String result = breader.readLine().trim();
				breader.close();
				fread.close();
				fread = null;
				breader = null;

				if (null == result) {
					return 0;
				}

				if("0".equals(result)) return 0;
				else if("1".equals(result)) return 1;
				else if("2".equals(result)) return 2;
				return 0;

			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return 0;
			} catch (IOException e) {
				e.printStackTrace();
				return 0;
			}
		}
		return 0;
	}
	
	public static int logOut(String log,boolean isLogToFile){

		if (isLogToFile) {
			if (mCommonLogFile == null) {
				if (mLogPath == null) {
					mLogPath = KCloudPositionManager.getInstance().getPath();
				}
				if (mLogPath != null && !mLogPath.equals("")) {
					int[] data = null;
					data = new int[8];
					getCurrentTime(data);
					String timestr = String.format("%04d%02d%02d_%02d%02d%02d",
							data[0], data[1], data[3], data[4], data[5],
							data[6]);

					mCommonLogFile = mLogPath + "/LOG/log_out_" + timestr
							+ ".txt";
					File filePath = new File(mCommonLogFile);
					try {
						createFile(filePath);
					} catch (IOException e) {
						e.printStackTrace();
						return -1;
					}
				} else {
					return -1;
				}
			}
			try {
				FileOutputStream fout = new FileOutputStream(mCommonLogFile,
						true);
				Calendar calendar = new GregorianCalendar();
				String outPut = String.format("[%02d:%02d:%02d]    %s\r\n",
						calendar.get(Calendar.HOUR_OF_DAY),
						calendar.get(Calendar.MINUTE),
						calendar.get(Calendar.SECOND), log);

				fout.write(outPut.getBytes());
				fout.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			Log.i("js", log);
		}

		return 0;
	}

	public static int getCurrentTime(int[] date) 
	{
		Calendar calendar = new GregorianCalendar();

		if (date == null) {
			return -1;
		}

		if (date.length > 0) {
			date[0] = calendar.get(Calendar.YEAR);
			if (date.length > 1) {
				date[1] = calendar.get(Calendar.MONTH) + 1;
				if (date.length > 2) {
					date[2] = calendar.get(Calendar.DAY_OF_WEEK);
					if (date.length > 3) {
						date[3] = calendar.get(Calendar.DAY_OF_MONTH);
						if (date.length > 4) {
							date[4] = calendar.get(Calendar.HOUR_OF_DAY);
							if (date.length > 5) {
								date[5] = calendar.get(Calendar.MINUTE);
								if (date.length > 6) {
									date[6] = calendar.get(Calendar.SECOND);

									if (date.length > 7) {
										date[7] = calendar.get(Calendar.MILLISECOND);
										return 8;
									}

								}
							}
						}
					}
				}
			}
		}

		return date.length;
	}

	public static void makeDir(File dir)
	{
		if (!dir.getParentFile().exists()) {
			makeDir(dir.getParentFile());
		}
		dir.mkdir();
	}

	public static boolean createFile(File file) throws IOException
	{
		if (!file.exists()) {
			makeDir(file.getParentFile());
		}
		return file.createNewFile();
	}

	public static String readAssetsFile(Context context,int type)//type=0,1,2,3,4
	{
		AssetManager am = context.getAssets();
		try {
			InputStream in = am.open("DeviceCfg.txt");
			if(in!=null)
			{
				BufferedReader bReader = new BufferedReader(new InputStreamReader(in));
				char buffer[] = new char[1024];
				bReader.read(buffer);
				String str = new String(buffer);
				str = str.trim();
				String temp[] = str.split("\n");
				if(type<0||type>4)
					return null;

				if(temp[type]==null) return null;
				int index = temp[type].indexOf(':');
				return index>=0?temp[type].substring(index+1):null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}

	public static String readCommonFile(String filepath,int len)
	{
		File file = new File(filepath);
		char[] buffer = new char[len];

		if(file == null || !file.exists())
			return null;
		try {
			@SuppressWarnings("resource")
			BufferedReader bReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(file)));
			bReader.read(buffer);
			String str = new String(buffer);
			return str;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
