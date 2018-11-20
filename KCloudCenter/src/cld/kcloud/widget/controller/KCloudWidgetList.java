package cld.kcloud.widget.controller;

import java.util.ArrayList;
import java.util.List;

import android.view.View;

public class KCloudWidgetList {
	private class KCloudWidget {

		int id;
		View view;
		
		public KCloudWidget(int id, View view) {
			this.id = id;
			this.view = view;
		}
	}
	
	private List<KCloudWidget> mWidgetList = null;
		
	public KCloudWidgetList() {
		mWidgetList = new ArrayList<KCloudWidget>();
	}
		
	public boolean add(int id, View view) {
		KCloudWidget item = new KCloudWidget(id, view);
		return mWidgetList.add(item);
	}
		
	public void remove(int id) {
		if (!mWidgetList.isEmpty()) {
			for (int i=0; i<mWidgetList.size(); i++) {
				KCloudWidget item = mWidgetList.get(i);
				if (item.id == id) {
					mWidgetList.remove(i);
				}
			}
		}
	}
		
	public void clear() {
		if (!mWidgetList.isEmpty()) {
			mWidgetList.clear();
		}
	}
		
	public View get(int id) {		
		if (!mWidgetList.isEmpty()) {
			for (int i=0; i<mWidgetList.size(); i++) {
				KCloudWidget item = mWidgetList.get(i);
				if (item.id == id) {
					return item.view;
				}
			}
		}
		return null;
	}
		
	public int getSize() {	
		return mWidgetList.size();
	}
}
