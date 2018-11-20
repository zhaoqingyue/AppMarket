/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: ReportDataQueue.java
 * @Prject: KCloudCenter_M550
 * @Package: cld.navi.position.model
 * @Description: 用于存放数据的循环队列类，同时兼具缓冲的作用
 * @author: zhaoqy
 * @date: 2016年8月15日 上午9:00:35
 * @version: V1.0
 */

package cld.navi.position.model;

import cld.navi.position.data.LocData;

public class ReportDataQueue {

	final static int MAX_NUM_OF_UPPOSION = 1500;// 默认队列的大小，可以缓存1500个数据点，这是要大于2小时以上的�?
	final static int PER_NUM_OF_UPPOSITION = 40; // 每次上报上限点数
	final static int PER_INTERVALMS = 100; // 睡眠时间，参考六部CC代码的。

	private boolean isEmpty = true;
	private boolean isFull = false;
	private boolean isStop = false;

	private int mMaxNumPonit = MAX_NUM_OF_UPPOSION;
	private int front = 0;// 队头
	private int rear = 0; // 队尾,始终指向下一个空位。

	private LocData[] locDatas = null;

	// 弱引用队列�?
	// public static ReferenceQueue<LocData> refQueue = new
	// ReferenceQueue<LocData>();

	public ReportDataQueue() {
		if (locDatas == null)
			locDatas = new LocData[MAX_NUM_OF_UPPOSION];
		mMaxNumPonit = MAX_NUM_OF_UPPOSION;
		isEmpty = true;
		isFull = false;
		isStop = false;
		front = 0;
		rear = 0;
	}

	public ReportDataQueue(int size) {
		if (locDatas == null)
			locDatas = new LocData[size];
		mMaxNumPonit = size;
		isEmpty = true;
		isFull = false;
		isStop = false;
		front = 0;
		rear = 0;

	}

	/*
	 * 数据进队�?
	 */
	public synchronized boolean enterQueue(LocData data) {

		if (locDatas == null)
			return false;

		locDatas[rear] = data;
		rear = (rear + 1) % mMaxNumPonit;

		if (isFull)
			front = rear;

		if (front == rear)
			this.isFull = true;
		this.isEmpty = false;
		notifyAll();// 告诉别人有数据了。
		return true;

		// //非空
		// if(!isEmpty){
		// rear = (rear+1==mMaxNumPonit)?0:rear+1;
		// if(this.isFull){
		// //队列满的时�?，覆盖最老的点
		// LocData tmepLocData = locDatas[rear];
		// //WeakReference<LocData> ref = new
		// WeakReference<LocData>(tmepLocData,
		// refQueue);//���������ö��У��ȴ������١�
		// front = (front+1==mMaxNumPonit)?0:front+1;
		// }
		// }
		// if(locDatas!=null)
		// {
		// locDatas[rear] = data;
		// //加入弱引用队列，防止队列迭代造成内存溢出。
		// //WeakReference<LocData> tempLocData = new
		// WeakReference<LocData>(locDatas[rear], refQueue);
		// }
		// else return false;
		//
		// if(!isEmpty)
		// isFull = (front == (rear+1)%mMaxNumPonit)?true:false;
		// isEmpty = false;
		// notifyAll();//告诉别人有数据了。
		// return true;
	}

	/*
	 * 移出数据，出队列
	 */
	public synchronized LocData removeQueue() {

		LocData data = null;
		if (locDatas == null)
			return null;
		// 数据为空，此地上报线程等待
		try {
			while (isEmpty)
				wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}

		if (isEmpty)
			return null;

		data = locDatas[front];
		front = (front + 1) % mMaxNumPonit;
		if (front == rear)
			isEmpty = true;
		isFull = false;
		return data;

		// LocData data = null;
		// //数据为空，此地上报线程等待
		// try {
		// while(isEmpty)
		// wait();
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// return null;
		// }
		// if(isEmpty) return null;
		// //此时如果front == rear 就是队列只有一个数据了，那么出队列后就为空了
		// if(front == rear){
		// isEmpty = true;
		// rear = (rear+1==mMaxNumPonit)?0:rear+1;//移动队尾
		// }
		// data = locDatas[front];
		// front = (front+1==mMaxNumPonit)?0:front+1;//移动队头
		// isFull = false;//取走了点，不可能为满
		// return data;

	}

	// 获取当前可上传点的个数
	public synchronized int getCountUpPosion() {
		if (isEmpty)
			return 0;
		if (isFull)
			return mMaxNumPonit;

		if (front <= rear)
			return (rear - front);
		else
			return (mMaxNumPonit - (front - rear));
	}

	/*
	 * 获取当前第一个点的坐标类型
	 * 
	 * @return -1 无数据，1GPS坐标，0凯立德坐标
	 */
	public synchronized short getCoordinateFlag() {
		if (locDatas == null || isEmpty)
			return -1;
		if (locDatas[front].getRoadId() != -1)
			return 0;
		return 1;// 默认GPS坐标
	}

	/*
	 * 获取当前坐标类型连续个数
	 */
	public synchronized int getCountCurCoordinate() {
		if (locDatas == null || isQueueEmpty())
			return 0;

		int count = 0;
		int flag = locDatas[front].getRoadId() == -1 ? 0 : 1;
		int dataCount = getCountUpPosion();
		if (flag == 0)// GPS坐标
		{
			int tempIndex = front;
			for (int i = 0; i < dataCount; i++) {
				if (locDatas[tempIndex].getRoadId() == -1)
					count++;
				else
					break;
				tempIndex = ++tempIndex % mMaxNumPonit;
			}
		} else// 凯立德坐标
		{
			int tempIndex = front;
			for (int i = 0; i < dataCount; i++) {
				if (locDatas[tempIndex].getRoadId() != -1)
					count++;
				else
					break;
				tempIndex = ++tempIndex % mMaxNumPonit;
			}
		}
		return count;
	}

	public synchronized boolean isQueueEmpty() {
		return this.isEmpty;
	}

	public synchronized boolean isQueueFull() {
		return this.isFull;
	}

	/*
	 * 主要是在结束的时候调用
	 */
	public synchronized void notifyQueue() {
		notifyAll();
	}
}
