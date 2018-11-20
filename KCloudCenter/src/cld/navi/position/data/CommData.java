/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: CommData.java
 * @Prject: KCloudCenter_M550
 * @Package: cld.navi.position.data
 * @Description: 上传数据包中的公共部分
 * @author: zhaoqy
 * @date: 2016年8月12日 上午9:14:10
 * @version: V1.0
 */

package cld.navi.position.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import cld.navi.position.frame.MainService;
import cld.navi.util.ByteUtils;

public class CommData {

	private short num = 0;                 //位置点个数
	private short cartype = 0;             //车种，目前传0
	private int remark = 0;                //附加属性，默认为0
	private int duid = -1;                 //设备ID
	private int kuid = -1;                 //用户ID
	private String session = null;         //用户会话//保证32位
	private short src = MainService.VALUE_OF_APPTYPE;//数据来源，OEM
	private short coordinate_system = 1;   //经纬度类型，这里采用GPS坐标直接置为1
	
	public CommData(short num, short cartype, int remark, int duid,
			int kuid, String session, short src, short coordinate_system)
	{
		this.num = num;
		this.cartype = cartype;
		this.remark = remark;
		this.duid = duid;
		this.kuid = kuid;
		this.session = session;
		this.src = src;
		this.coordinate_system = coordinate_system;
	}
	
	public CommData(short num, int duid, int kuid, String session)
	{
		this.num = num;
		this.duid = duid;
		this.kuid = kuid;
		this.session = session;
	}
	
	public short getNum(){
		return this.num;
	}
	
	public int getDuid(){
		return this.duid;
	}
	
	public int getKuid(){
		return this.kuid;
	}
	
    public String getSession(){
    	return this.session;
    }
    
    public void setNum(short num){
        this.num = num;	
    }
    
    public void setDuid(int duid){
    	this.duid = duid;
    }
    
    public void setKuid(int kuid){
    	this.kuid = kuid;
    }
    
    public void setSession(String session){
    	this.session = session;
    }
    
    public void setCoordinate(short flag)
    {
    	this.coordinate_system = flag;
    }
    
    @Override
    public String toString() {
    	//"\"remark\":"+remark+
        return "{" + "\"num\":" + num + "\"cartype\":" + cartype + "\"remark\":" + 
    			remark + "\"duid\":" + duid + "\"kuid\":" + kuid + "\"session\":" + 
    			session + "\"coordinate_system\":" + coordinate_system + "}";
    }
    
    public byte[] toByteArray(){
    	byte[] src_4 = new byte[4];
    	byte[] src_2 = new byte[2];
    	byte[] src_32 = new byte[32];//session保证32个字节,不然服务端解析出错
    	ByteArrayOutputStream bout = new ByteArrayOutputStream();
    	
    	try {
    		System.arraycopy(ByteUtils.short2byte(num), 0, src_2, 0, 2);
			bout.write(src_2);
			
    		System.arraycopy(ByteUtils.short2byte(cartype), 0, src_2, 0, 2);
			bout.write(src_2);
			
    		System.arraycopy(ByteUtils.int2byte(remark), 0, src_4, 0, 4);
			bout.write(src_4);
			
    		System.arraycopy(ByteUtils.int2byte(duid), 0, src_4, 0, 4);
			bout.write(src_4);
			
    		System.arraycopy(ByteUtils.int2byte(kuid), 0, src_4, 0, 4);
			bout.write(src_4);
			if(session!=null)
				System.arraycopy(session.getBytes(), 0, src_32, 0, session.length());
			
			bout.write(src_32);

			System.arraycopy(ByteUtils.short2byte(src), 0, src_2, 0, 2);
			bout.write(src_2);
			
			System.arraycopy(ByteUtils.short2byte(coordinate_system), 0, src_2, 0, 2);
			bout.write(src_2);
			
			byte[] ret = bout.toByteArray();
			bout.close();
			return ret;
		} 
    	catch (IOException e) {
			e.printStackTrace();
	    	return null;
		}
    }
}
