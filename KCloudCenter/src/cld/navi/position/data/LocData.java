/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: LocData.java
 * @Prject: KCloudCenter_M550
 * @Package: cld.navi.position.data
 * @Description: 单点上传位置信息
 * @author: zhaoqy
 * @date: 2016年8月12日 上午9:18:28
 * @version: V1.0
 */

package cld.navi.position.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import cld.navi.util.ByteUtils;

public class LocData {

	private int x = 0;           //纬度，使用千分之一秒为单位，即*3600000
	private int y = 0;           //经度，使用千分之一秒为单位，即*3600000
	private int speed = 0;       //当前速度
	private short high = 0;      //高度
	private short derection = 0; //行驶方向（正北）
	private int time = 0;        //采集时间（UTC时间）
	private int roaduid = -1;    //道路ID无ID时传-1
	
	public LocData(int x, int y, int speed, short high, short derection, 
			int time, int roaduid){
		this.x = x;
		this.y = y;
		this.speed = speed;
		this.high = high;
		this.derection = derection;
		this.time = time;
		this.roaduid = roaduid;
	}
	
	public LocData(){
		
	}
	
	public int getLocX(){
		return this.x;
	}
	
	public int getLocY(){
		return this.y;
	}
	
	public int getSpeed(){
		return this.speed;
	}
	
	public short getHigh(){
		return this.high;
	}
	
	public short getDerection(){
		return this.derection;
	}
	
	public int getUtcTime(){
		return this.time;
	}
	
	public int getRoadId(){
		return this.roaduid;
	}
	
	public void setLocX(int x){
		this.x = x;
	}
	
	public void setLocY(int y){
		this.y = y;
	}
	
	public void setSpeed(int speed){
		this.speed = speed;
	}
	
	public void setHigh(short hight){
	   this.high = hight;	
	}
	
	public void setDerection(short derection){
		this.derection = derection;
	}
	
	public void setUtcTime(int time){
		this.time = time;
	}
	
	public void setRoadId(int roadid){
		this.roaduid = roadid;
	}

	@Override
	public String toString() {
		return "{" + "\"x\":" + "\"" + String.valueOf(x) + "\"" + "," + "\"y\":" + 
			   "\"" + String.valueOf(y) + "\"" + "," + "\"speed\":" + "\"" + 
			   String.valueOf(speed) + "\"" + "," + "\"high\":" + "\"" + 
			   String.valueOf(high) + "\"" + "," + "\"derection\":" + "\"" + 
			   String.valueOf(derection) + "\"" + "," + "\"time\":" + "\"" + 
			   String.valueOf(time) + "\"" + "," + "\"roaduid\":" + "\"" + 
			   String.valueOf(roaduid) + "\"" + "}";
	}

	public JSONObject toJSON(){
		String temp = toString();
		JSONObject json=null;
		try {
			json = new JSONObject(temp);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return json;
	}
	
	public byte[] toByteArray() {
		byte[] src_4 = new byte[4];
		byte[] src_2 = new byte[2];
		ByteArrayOutputStream bout = new ByteArrayOutputStream();

		try {
			System.arraycopy(ByteUtils.int2byte(x), 0, src_4, 0, 4);
			bout.write(src_4);

			System.arraycopy(ByteUtils.int2byte(y), 0, src_4, 0, 4);
			bout.write(src_4);

			System.arraycopy(ByteUtils.int2byte(speed), 0, src_4, 0, 4);
			bout.write(src_4);

			System.arraycopy(ByteUtils.short2byte(high), 0, src_2, 0, 2);
			bout.write(src_2);

			System.arraycopy(ByteUtils.short2byte(derection), 0, src_2, 0, 2);
			bout.write(src_2);

			System.arraycopy(ByteUtils.int2byte(time), 0, src_4, 0, 4);
			bout.write(src_4);

			System.arraycopy(ByteUtils.int2byte(roaduid), 0, src_4, 0, 4);
			bout.write(src_4);

			byte[] ret = bout.toByteArray();
			bout.close();
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
