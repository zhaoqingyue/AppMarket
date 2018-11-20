/**
 * 
 * Copyright © 2016Careland. All rights reserved.
 *
 * @Title: ReportData.java
 * @Prject: KCloudCenter_M550
 * @Package: cld.navi.position.data
 * @Description: 上报数据包
 * @author: zhaoqy
 * @date: 2016年8月12日 上午9:24:40
 * @version: V1.0
 */

package cld.navi.position.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReportData {
	
	private CommData commdata;
	private List<LocData> locdatas;
	
	public ReportData(CommData commdata){
		this.commdata = commdata;
		locdatas = new ArrayList<LocData>();
	}
	
	public ReportData()
	{
		locdatas = new ArrayList<LocData>();
	}
	
	public CommData getCommData(){
		return this.commdata;
	}
	
	public List<LocData> getLocDatas(){
		return this.locdatas;
	}
	
	public void setCommData(CommData commdata){
		this.commdata = null;
		this.commdata = commdata;
	}
	
	public void setLocDatas(List<LocData> locdatas){
		this.locdatas = null;
		this.locdatas = locdatas;
	}
	
	public void addLocData(LocData data)
	{
		locdatas.add(data);
		
		if(commdata!=null)
		{   
			short num = (short)locdatas.size();
			commdata.setNum(num);
		}
	}
	
	public void clearLocData()
	{
		locdatas.clear();
	}
	
	public byte[] toByteArray(){
		
		if(locdatas!=null&&locdatas.size()!=0&&commdata!=null)
		{
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
		
			try {
				byte[] locBytes = null;
				byte[] commBytes = commdata.toByteArray();
				bout.write(commBytes);
				for(int i=0;i<locdatas.size();i++){
					locBytes = locdatas.get(i).toByteArray();
					bout.write(locBytes);
					locBytes = null;
				}
				
				byte[] ret = bout.toByteArray();
				bout.close();
				return ret;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;	
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.commdata.toString());
		sb.append("\nITEM:\n");
		for(int i=0; i<this.locdatas.size(); i++)
		{
			sb.append(locdatas.get(i).toString());
			sb.append('\n');
		}
		return sb.toString();
	}
}
