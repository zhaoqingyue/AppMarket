package cld.kcloud.custom.bean;

import org.json.JSONException;
import org.json.JSONObject;

public class KCloudCustomInfo {
	public static class KCloud_Logo {
		public long id;				// Ψһʶ��
		public long start_time;		// ��ʼʱ��
		public long end_time;		// ����ʱ��
		public int live_time;		// ͣ��ʱ��
		public String url;			// url��ַ
		public String target;		// ����·��
			
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
