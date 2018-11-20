package cld.kcloud.custom.bean;

import org.json.JSONException;
import org.json.JSONObject;

public class KCloudCustomInfo {
	public static class KCloud_Logo {
		public long id;				// 唯一识别
		public long start_time;		// 开始时间
		public long end_time;		// 结束时间
		public int live_time;		// 停留时间
		public String url;			// url地址
		public String target;		// 本地路径
			
		public KCloud_Logo() {
			id = 0;
			start_time = 0;
			end_time = 0;
			live_time = 0;
			url = "";
			target = "";
		}
		
		public JSONObject toJSON() {
			JSONObject json = null;
			try {
				json = new JSONObject();
				json.put("id", id);
				json.put("start_time", start_time);
				json.put("end_time", end_time);
				json.put("live_time", live_time);
				json.put("url", url);
				json.put("target", target);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return json;
		}
	}
}
