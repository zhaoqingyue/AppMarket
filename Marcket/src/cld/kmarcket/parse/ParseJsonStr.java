package cld.kmarcket.parse;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import cld.kmarcket.appinfo.AppInfo;
import cld.kmarcket.util.ConstantUtil;

public class ParseJsonStr 
{
	/**
	 * 解析应用升级接口返回的数据
	 * @param jsonStr 从服务器端得到的JSON字符串数据
	 * @return
	 */
	@SuppressLint("NewApi") 
	public static ArrayList<AppInfo> parseAppUpgradeResult(String jsonStr)
	{
		if (jsonStr == null || jsonStr.isEmpty())
		{
			return null;
		}
		ArrayList<AppInfo> appInfoList = new ArrayList<AppInfo>();
		try 
		{
			//将jsonStr字符串转换为json对象
			JSONObject jsonObj = new JSONObject(jsonStr);
			if (jsonObj.getInt("errcode") == 0)
			{
				//得到指定json key对象的value对象
				JSONArray jsonArray = jsonObj.getJSONArray("data");
				//遍历jsonArray
				for (int i=0; i<jsonArray.length(); i++) 
				{
					AppInfo appInfo = new AppInfo(); 
					//获取每一个json对象
					JSONObject jsonItem = jsonArray.getJSONObject(i);
					//获取对象的所有属性
					if(jsonItem.has("pack_name"))
					{
						appInfo.setPkgName(jsonItem.getString("pack_name"));
					}
					if(jsonItem.has("app_name"))
					{
						appInfo.setAppName(jsonItem.getString("app_name"));
					}
					if(jsonItem.has("app_icon"))
					{
						appInfo.setAppIconUrl(jsonItem.getString("app_icon"));
					}
					if(jsonItem.has("app_url"))
					{
						appInfo.setAppUrl(jsonItem.
								getString("app_url").trim());
					}
					if(jsonItem.has("upgrade_desc"))
					{
						appInfo.setUpgradeDesc(jsonItem.
								getString("upgrade_desc").trim());
					}
					if(jsonItem.has("ver_name"))
					{
						appInfo.setVerName(jsonItem.getString("ver_name"));
					}
					if(jsonItem.has("ver_code"))
					{
						appInfo.setVerCode(jsonItem.getInt("ver_code"));
					}
					if(jsonItem.has("pack_size"))
					{
						appInfo.setPackSize(jsonItem.getInt("pack_size"));
					}
					if(jsonItem.has("quiesce"))
					{
						appInfo.setQuiesce(jsonItem.getInt("quiesce"));
					}
					if(jsonItem.has("down_times"))
					{
						appInfo.setDownTimes(jsonItem.getInt("down_times"));
					}
					
					appInfo.setStatus(ConstantUtil.APP_STATUS_UPDATE); //表示可更新
					appInfoList.add(appInfo);
				}
			}
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		return appInfoList;
	}
	
	/**
	 * 解析推荐应用接口返回的数据
	 * @param jsonStr 从服务器端得到的JSON字符串数据
	 * @return
	 */
	@SuppressLint("NewApi") 
	public static ArrayList<AppInfo> parseRecdAppResult(String jsonStr)
	{
		if (jsonStr == null || jsonStr.isEmpty())
		{
			return null;
		}
		
		ArrayList<AppInfo> appInfoList = new ArrayList<AppInfo>();
		try 
		{
			//将jsonStr字符串转换为json对象
			JSONObject jsonObj = new JSONObject(jsonStr);
			if (jsonObj.getInt("errcode") == 0)
			{
				//得到指定json key对象的value对象
				JSONArray jsonArray = jsonObj.getJSONArray("data");
				//遍历jsonArray
				for (int i=0; i<jsonArray.length(); i++) 
				{
					AppInfo appInfo = new AppInfo(); 
					//获取每一个json对象
					JSONObject jsonItem = jsonArray.getJSONObject(i);
					//获取对象的所有属性
					if(jsonItem.has("pack_name"))
					{
						appInfo.setPkgName(jsonItem.getString("pack_name"));
					}
					if(jsonItem.has("app_name"))
					{
						appInfo.setAppName(jsonItem.getString("app_name"));
					}
					if(jsonItem.has("app_icon"))
					{
						appInfo.setAppIconUrl(jsonItem.getString("app_icon"));
					}
					if(jsonItem.has("app_url"))
					{
						appInfo.setAppUrl(jsonItem.
								getString("app_url").trim());
					}
					if(jsonItem.has("app_desc"))
					{
						appInfo.setAppDesc(jsonItem.
								getString("app_desc").trim());
					}
					if(jsonItem.has("ver_name"))
					{
						appInfo.setVerName(jsonItem.getString("ver_name"));
					}
					if(jsonItem.has("ver_code"))
					{
						appInfo.setVerCode(jsonItem.getInt("ver_code"));
					}
					if(jsonItem.has("pack_size"))
					{
						appInfo.setPackSize(jsonItem.getInt("pack_size"));
					}
					if(jsonItem.has("quiesce"))
					{
						appInfo.setQuiesce(jsonItem.getInt("quiesce"));
					}
					if(jsonItem.has("down_times"))
					{
						appInfo.setDownTimes(jsonItem.getInt("down_times"));
					}
					if(jsonItem.has("validate"))
					{
						appInfo.setValidate(jsonItem.getInt("validate"));
					}
					
					appInfo.setStatus(ConstantUtil.APP_STATUS_DOWNLOAD);
					appInfoList.add(appInfo);
				}
			}
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		return appInfoList;
	}
	
	@SuppressLint("NewApi") 
	public static ArrayList<String> parseAppStatusResult(String jsonStr)
	{
		if (jsonStr == null || jsonStr.isEmpty())
		{
			return null;
		}
		
		ArrayList<String> appStatus = new ArrayList<String>();
		try 
		{
			JSONObject jsonObj = new JSONObject(jsonStr);
			if (jsonObj.getInt("errcode") == 0)
			{
				JSONArray jsonArray = jsonObj.getJSONArray("packname");
				for (int i=0; i<jsonArray.length(); i++) 
				{
					appStatus.add(jsonArray.getString(i));
				}
			}
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		return appStatus;
	}
}
