package cld.navi.position.frame;

import java.util.ArrayList;
import java.util.List;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.util.Log;

public class ApnTools {

	static final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");
	static final Uri CONTENT_URI = Uri.parse("content://telephony/carriers");

	private static final String ID = "_id";
	private static final String APN = "apn";
	private static final String TYPE = "type";

	public static final String MY_APN = "GDSZKLDX01.CLFU.GZM2MAPN";
	public static final String MY_APN_PUBLIC = "UNIM2M.GZM2MAPN";
	public static final String MY_APN_STADARD = "GDSZKLDX02.CLFU.GZM2MAPN";
	public static final String MY_NAME = "凯立德";

	public static void insertApn(Context context,String apn)
	{
		if(apn==null || "".equals(apn))
			return;
		ContentValues values = new ContentValues();
		values.put("name",MY_NAME);
		values.put("apn",apn);
		values.put("proxy", "");
		values.put("port", "");
		values.put("mmsproxy", "");
		values.put("mmsport", "");
		values.put("user", "");
		values.put("server", "");
		values.put("password", "");
		values.put("mmsc", "");          
		values.put("type","default");
		values.put("mcc", "460");
		values.put("mnc", "06");  //物联网卡使用06
		values.put("numeric", "46006");
		Uri reURI = context.getContentResolver().insert(CONTENT_URI, values);
	}
	
	/**
	 * 获取当前正在使用的apn的ID
	 */
	public static int getPreferredApnId(Context context) {
		int curId = -1;
		Cursor cursor = context.getContentResolver().query(PREFERRED_APN_URI,
				new String[] { ID }, null, null, null);
		cursor.moveToFirst();
		if (!cursor.isAfterLast()) {
			String id = cursor.getString(cursor.getColumnIndex("_id"));
			if (id != null && !"".equals(id)) {
				curId = Integer.valueOf(id).intValue();
			}
		}
		return curId;
	}

	/**
	 * 获取当前正在使用的apn的ID
	 */
	public static APN getPreferredApn(Context context){
		Cursor cursor = context.getContentResolver().query(PREFERRED_APN_URI,
				new String[] { ID, APN, TYPE }, null, null, null);
		if (cursor == null) {
			Log.i("kldtest", "无法获取当前apn");
			return null;
		}

		cursor.moveToFirst();
		APN apn = null;
		if (!cursor.isAfterLast()) {
			apn = new APN();
			apn.id = cursor.getString(cursor.getColumnIndex("_id"));
			apn.apn = cursor.getString(cursor.getColumnIndex("apn"));
			apn.type = cursor.getString(cursor.getColumnIndex("type"));
			Log.i("kldtest", "current APN: id ==" + apn.id + ",apn==" + apn.apn
					+ ",type=" + apn.type);
		}
		return apn;
	}


	/**
	 * 获取指定名称的apn的ID
	 */
	public static int getApnId(Context context, String name, String apn) {
		String projection[] = { "_id,apn,type,name" };
		Cursor cr = context.getContentResolver().query(CONTENT_URI, projection,
				"apn like ? and name like ?", new String[] { apn, name }, null);
		int idex = -1;
		while (cr != null && cr.moveToNext()) {
			String id = cr.getString(cr.getColumnIndex("_id"));
			idex = Integer.valueOf(id).intValue();
		}

		return idex;
	}

	/**
	 * 重载函数
	 */
	public static int getApnId(Context context, String apn) {
		String projection[] = { "_id,apn,type,name" };
		Cursor cr = context.getContentResolver().query(CONTENT_URI, projection,
				"apn like ?", new String[] { apn }, null);
		int idex = -1;
		while (cr != null && cr.moveToNext()) {
			String id = cr.getString(cr.getColumnIndex("_id"));
			idex = Integer.valueOf(id).intValue();
		}
		return idex;
	}

	/**
	 * 遍历所有的APN
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List getAPNList(Context context){  
		// current不为空表示可以使用的APN
		String projection[] = { "_id,apn,type,current" };
		Cursor cr = context.getContentResolver().query(CONTENT_URI, projection,
				null, null, null);
		List list = new ArrayList();
		if (cr == null)
			return null;
		while (cr != null && cr.moveToNext()) {
			APN a = new APN();
			a.id = cr.getString(cr.getColumnIndex("_id"));
			a.apn = cr.getString(cr.getColumnIndex("apn"));
			a.type = cr.getString(cr.getColumnIndex("type"));
			list.add(a);
			Log.i("kldtest", a.toString());
		}
		return list;
	}

	/**
	 * 设置默认的apn
	 */
	public static boolean setDefaultApn(Context context,int apnId) {
		boolean res = false;
		ContentResolver resolver = context.getContentResolver();
		ContentValues values = new ContentValues();
		values.put("apn_id", apnId);

		try {
			resolver.update(PREFERRED_APN_URI, values, null, null);
			Cursor c = resolver.query(PREFERRED_APN_URI, new String[] { "name",
					"apn" }, "_id=" + apnId, null, null);
			if (c != null) {
				res = true;
				c.close();
			}
		} catch (SQLException e) {
		}
		return res;
	}

	/**
	 * 更新apn
	 */
	public static int updateApn(Context context) {
		boolean res = false;
		ContentResolver resolver = context.getContentResolver();
		ContentValues values = new ContentValues();
		values.put("apn_id", 5);
		int result = -1;
		try {
			result = resolver.update(PREFERRED_APN_URI, values, null, null);
			if(result == -1 || result == 0)
				return -1;
			Cursor c = resolver.query(PREFERRED_APN_URI, new String[] { "name",
					"apn" }, "_id=" + 5, null, null);
			if (c != null) {
				res = true;
				c.close();
				result =  1;
			}
			else
			{
				result = 0;
			}
		} catch (SQLException e) {
		}
		return result;

	}
	
	/**
	 * 查询是否存在指定的apn类型
	 */
	public static boolean isExisApn(Context context,String apn)
	{
		String projection[] = { "_id,apn,type,name" };
		Cursor cr = context.getContentResolver().query(CONTENT_URI, projection,
				"apn like ?", new String[] { apn }, null);
		boolean isExist = false;
		while (cr != null && cr.moveToNext()) {
			String apntpm = cr.getString(cr.getColumnIndex("apn"));
			if (apntpm != null && apntpm.equals(apn)) {
				isExist = true;
				break;
			}
		}

		return isExist;
	}

	/**
	 * 查询是否存在指定的apn类型
	 */
	public static boolean isExisApn(Context context,String apn,String name)
	{
		String projection[] = { "_id,apn,type,name" };
		Cursor cr = context.getContentResolver().query(CONTENT_URI, projection,
				"apn like ? and name like ?", new String[] { apn, name }, null);
		boolean isExist = false;
		while (cr != null && cr.moveToNext()) {
			String apntpm = cr.getString(cr.getColumnIndex("apn"));
			if (apntpm != null && apn.equals(apn)) {
				isExist = true;
				break;
			}
		}

		return isExist;
	}

	/**
	 * 当前apn是否属于凯立德共网
	 */
	public static boolean isCurApnBelongKldPublicNet(Context context)
	{
		boolean result = false;
		if(isExisApn(context, MY_APN_PUBLIC))
		{
			int prefId = getPreferredApnId(context);
			int targetId =getApnId(context,MY_APN_PUBLIC);
			if(prefId==targetId)
				result = true;
		}

		return result;
	}

	/**
	 * 当前apn是否属于凯立德定向网
	 */
	public static boolean isCurApnBelongKldLimitNet(Context context)
	{
		boolean result = false;
		if(isExisApn(context, MY_APN))
		{
			int prefId = getPreferredApnId(context);
			int targetId =getApnId(context,MY_APN);
			if(prefId==targetId)
				result = true;
		}

		return result;
	}
	public static class APN
	{
		String id;
		String apn;
		String type;

		@Override
		public String toString() {
			return "id=" + this.id + ",apn=" + this.apn + ",type=" + this.type;
		}
	}
}
