package cld.kcloud.widget.controller;

import android.view.View;
import android.view.View.OnClickListener;

public class KCloudController {
	
	/**
	 * 
	 * @param id
	 * @param view
	 * @param listener
	 * @param visible
	 * @param enable
	 * @param controller
	 */
	public static void 	bindControl(int id, View view, OnClickListener listener, 
			boolean visible, boolean enable, KCloudWidgetList list) {
		if (list == null || view == null) {
			return ;
		}
		
		if (listener != null) {
			view.setOnClickListener(listener);
		}
		
		if (visible) {
   			view.setVisibility(View.VISIBLE);
   		} else {
   			view.setVisibility(View.GONE);
   		}
		
		view.setEnabled(enable);
		list.add(id, view);	
	}
	
	/**
	 * 
	 * @param id
	 * @param controller
	 * @return
	 */
	public static View getControlById(int id, KCloudWidgetList list) {
		if (list == null) {
			return null;
		}
		
		return list.get(id);
	}
	
	/**
	 * 
	 * @param id
	 * @param controller
	 * @return
	 */
	public static boolean setVisibleById(int id, boolean visible, KCloudWidgetList list) {
		if (list == null) {
			return false;
		}
		
		View view = list.get(id);
		if (view != null) {
			if (visible && view.getVisibility() == View.GONE) {
				view.setVisibility(View.VISIBLE);
			} else if (!visible && view.getVisibility() == View.VISIBLE) {
				view.setVisibility(View.GONE);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param id
	 * @param enable
	 * @param controller
	 * @return
	 */
	public static boolean setEnabledById(int id, boolean enabled, KCloudWidgetList list) {
		if (list == null) {
			return false;
		}
		
		View view = list.get(id);
		if (view != null) {
			view.setEnabled(enabled);
		}
		return false;
	}
}