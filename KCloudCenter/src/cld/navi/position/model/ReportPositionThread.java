/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: ReportPositionThread.java
 * @Prject: KCloudCenter_M550
 * @Package: cld.navi.position.model
 * @Description: 上报位置的线程
 * @author: zhaoqy
 * @date: 2016年8月15日 上午9:01:58
 * @version: V1.0
 */

package cld.navi.position.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import cld.kcloud.custom.manager.KCloudPositionManager;
import cld.navi.position.data.CommData;
import cld.navi.position.data.LocData;
import cld.navi.position.data.ReportData;
import cld.navi.util.FileUtils;
import cld.navi.util.NetWorkRequest;
import cld.navi.util.NetWorkUtil;

public class ReportPositionThread extends Thread {

	public static final String TAG = "UpPositionThread";
	private static final String POSITON_SUB_URL = "add_posi_by_bin_multicoord.php";
	private static final int MAX_NUM_OF_UPPOSITON = 1500;// 这个其实没用到，正常情况下是大于2个小时
	private static final int PER_NUM_OF_UPPOSITON = 40;// 每次上传点的个数
	private static final int PER_NUM_OF_OTHERCOORUPPOSITON = 10;// 下一个坐标类型点个数。
	private static final int PER_INTERVALMS = 50;// 每次休眠时间

	// 本线程要处理的消�?
	public static final int MSG_UPPOSITION_START = 200;
	public static final int MSG_UPPOSITON_QUIT = MSG_UPPOSITION_START + 1;// 线程停止消息
	public static final int MSG_UPPOSITON_UPDATE = MSG_UPPOSITON_QUIT + 1;// 上报位置的消息

	private ReportDataQueue mReportDataQueue = null;
	private UpPositionHandler mUpPositionHandler = null;

	public boolean isAcitive = false;// 线程是否可以接受消息
	private String upPositionHead = null;// 上报位置的url
	private String upPositonUrl = null;// 上报位置的完整路径
	private int mDuid = 0;// 设备ID由于设备ID较为稳定，所以只在服务启动的时候传进来就行了
	private GetKuidSessionCallback mGetKuidSessionCallback = null;
	private boolean isWriteLog = false;

	// private ReferenceQueue<ReportData> weakRefQueue = new
	// ReferenceQueue<ReportData>();

	public ReportPositionThread(ReportDataQueue reportDataQueue, String url,
			int duid, GetKuidSessionCallback getKuidSessionCallback) {
		mReportDataQueue = reportDataQueue;// 数据循环队列�?
		upPositionHead = url;// 上报位置的url�?
		upPositonUrl = upPositionHead + POSITON_SUB_URL;// 上报位置的完整路径，此路径头每次从服务器上取来后
														// 都应当保存在本地�?
		mDuid = duid;// device ID 设备ID
		mGetKuidSessionCallback = getKuidSessionCallback;
		isWriteLog = KCloudPositionManager.getInstance().getIsWriteLog();
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run() 数据上报线程执行
	 */
	@Override
	public void run() {
		Looper.prepare();
		mUpPositionHandler = new UpPositionHandler();
		mUpPositionHandler.sendEmptyMessage(MSG_UPPOSITION_START);// �?���?
		Looper.loop();
	}

	/**
	 * 数据上报线程专用Handler
	 */
	private class UpPositionHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			int message = msg.what;
			// �?��
			if (message == MSG_UPPOSITION_START) {
				isAcitive = true;
			}
			// 停止
			else if (message == MSG_UPPOSITON_QUIT) {
				getLooper().quit();
				isAcitive = false;
				freeRefrence();
			}// �?��上报数据
			else if (message == MSG_UPPOSITON_UPDATE) {
				// 无网络可用直接return
				FileUtils.logOut("startUpPosition-->收到上报消息", isWriteLog);
				if (!NetWorkUtil.isNetAvailable())
					return;

				int kuid = getKuid();
				String session = getSession();
				CommData commdata = new CommData((short) 0, mDuid, kuid,
						session);

				if (mReportDataQueue != null) { // 开始上报数据
					FileUtils.logOut("startUpPosition-->开始上报", isWriteLog);
					startUpPosition(commdata);
				}
			}
		}

	}

	/**
	 * 删除队列里面的消息
	 */
	public void removeMessge(int msg) {
		if (mUpPositionHandler != null) {
			mUpPositionHandler.removeMessages(msg);
		}
	}

	/**
	 * 给线程发空消息
	 */
	public boolean sendEmptyMessage(int msg) {

		if (mUpPositionHandler != null) {
			mUpPositionHandler.sendEmptyMessage(msg);
			return true;
		}
		return false;
	}

	/**
	 * 给线程发消息的函数，附带有数据对象�?
	 */
	public boolean sendMessage(int msg, Bundle bundle) {
		if (mUpPositionHandler != null) {
			Message mMsg = mUpPositionHandler.obtainMessage();
			mMsg.what = msg;
			mMsg.setData(bundle);
			// Message mMsg = mUpPositionHandler.obtainMessage(msg, obj);
			mMsg.sendToTarget();
			return true;
		}
		return false;
	}

	/**
	 * 数据上报函数
	 */
	public int startUpPosition(CommData commdata) {
		CommData mCommdata = commdata;// 公共部分，一般变化不大�?
		// ReportData reportData = new
		// ReportData(mCommdata);//构�?�?��用于上传数据的数据包�?
		ReportData reportData = new ReportData();// 公共数据类型需要变化
		int locDataCount = 0;// 用于查看队列里面可用的数据的数量�?
		int curCoordinateCount = 0;// 查看当前坐标类型的点的个数
		FileUtils.logOut("startUpPosition-->start", isWriteLog);
		while (isAcitive) {
			// 这个地方设计的不是很好
			// synchronized (mReportDataQueue) {
			// 非空
			reportData.clearLocData();// 先把原数据清�?
			if (!mReportDataQueue.isQueueEmpty()) {
				locDataCount = mReportDataQueue.getCountUpPosion();// 总的点的个数
				curCoordinateCount = mReportDataQueue.getCountCurCoordinate();// 当前坐标类型的数据个数
				short coordinateFlag = mReportDataQueue.getCoordinateFlag();// 坐标类型（GPS、凯立德）
				if (mCommdata != null)
					mCommdata.setCoordinate(coordinateFlag);
				if (reportData != null)
					reportData.setCommData(mCommdata);

				if (curCoordinateCount > PER_NUM_OF_UPPOSITON)
					curCoordinateCount = PER_NUM_OF_UPPOSITON;// 每次最多上传这么多点，参考以前CC代码。
				FileUtils.logOut("startUpPosition-->locDataCount=="
						+ locDataCount + ",curCoordinateCount=="
						+ curCoordinateCount, isWriteLog);
				// 队列取点
				for (int i = 0; i < curCoordinateCount; i++) {
					LocData locData = mReportDataQueue.removeQueue();// 队列里面取出定位数据
					if (locData != null)
						reportData.addLocData(locData);// 添加到上报的数据
				}
			}// 无数据
			else {
				locDataCount = 0;
				FileUtils.logOut("startUpPosition-->locDataCount-isEmpty",
						isWriteLog);
				return 0;
			}
			// }
			// 为空就返回�?
			// if(locDataCount == 0)
			// {
			// //CC六部的代码在这里做了睡眠，我觉得不用，为空就继续等待下一次上报数据消息�?直接return�?
			// // sleep();//睡眠等待�?
			// // continue;
			// return 0;
			// }

			// 取完数据后开始上报数�?
			byte[] reportBytes = reportData.toByteArray();
			NetWorkRequest netRequest = KCloudPositionManager.getInstance()
					.getNetRequest();
			FileUtils.logOut("reportData-->" + reportData.toString()
					+ " ,locDataCount-->" + locDataCount, isWriteLog);

			for (; isAcitive;) {

				FileUtils.logOut("startUpPosition-->sendPostBytes-pre",
						isWriteLog);
				JSONObject jsonRsult = netRequest.sendPostBytes(upPositonUrl,
						reportBytes);
				FileUtils.logOut("startUpPosition-->sendPostBytes-after",
						isWriteLog);
				if (jsonRsult != null) {
					FileUtils.logOut(
							"startUpPosition-->" + jsonRsult.toString(),
							isWriteLog);
					try {
						int errcode = jsonRsult.getInt("errcode");
						String errmsg = jsonRsult.getString("errmsg");
						Log.w("js", "startUpPosition-->errmsg==" + errmsg);
						// 成功
						if (errcode == 0)
							break;// 本次上报过程结束

					} catch (JSONException e) {
						e.printStackTrace();
						break;// 这个地方丢掉为好
					}

				} else { // 上传失败
					FileUtils.logOut("startUpPosition-->jsonRsult==NULL",
							isWriteLog);
					if (mReportDataQueue.isQueueFull())
						break;// 如果满了，就不继续了
				}
				upSleep(2000);// 失败或网络状况不好休眠一下，再继续
			}

			// 上报结束了
			if (!isAcitive)
				break;// 如果，线程被要求�?��了，直接完整�?��上报过程

			int remainCount = mReportDataQueue.getCountUpPosion();
			int remainOtherCoorCount = mReportDataQueue.getCountCurCoordinate();
			if (remainCount < PER_NUM_OF_OTHERCOORUPPOSITON
					&& remainOtherCoorCount > PER_NUM_OF_OTHERCOORUPPOSITON / 2)// 少于这么多点就等待下次来消息了再上报吧
				break;
			sleep();// 稍微休息
		}
		FileUtils.logOut("startUpPosition-->end", isWriteLog);

		return 0;
	}

	/**
	 * @Description: 睡眠函数,参六部CC代码的睡眠时间
	 */
	private void sleep() {
		try {
			Thread.sleep(PER_INTERVALMS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * 从导航获取KUID
	 */
	public int getKuid() {
		int kuid = -1;
		if (mGetKuidSessionCallback != null) {
			kuid = mGetKuidSessionCallback.getKuid();
		}
		return kuid;
	}

	/**
	 * 从导航获取session
	 */
	public String getSession() {
		String session = "";
		if (mGetKuidSessionCallback != null) {
			session = mGetKuidSessionCallback.getSession();
		}
		return session;
	}

	private void upSleep(long t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return;
		}
	}

	public interface GetKuidSessionCallback {
		int getKuid();

		String getSession();
	}

	// 释放资源
	public void freeRefrence() {
		mReportDataQueue = null;
		mGetKuidSessionCallback = null;
		mUpPositionHandler = null;
	}
}
