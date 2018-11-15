package cld.kmarcket.packages;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.Context;
import cld.kmarcket.KMarcketApplication;
import cld.kmarcket.R;
import cld.kmarcket.appinfo.AppInfo;
import cld.kmarcket.util.CommonUtil;

public class DefaultPackages 
{
	public static ArrayList<AppInfo> getDefaultPackages()
	{
		ArrayList<AppInfo> appInfoList = new ArrayList<AppInfo>();
		Context context = KMarcketApplication.getContext();
		String[] packages = context.getResources().getStringArray(R.array.internal_package);
		String[] des = context.getResources().getStringArray(R.array.package_des);
		int[] widgets = context.getResources().getIntArray(R.array.package_widget);
		for (int i=0; i<packages.length; i++)
		{
			AppInfo appifno = new AppInfo();
			appifno.setPkgName(packages[i]);
			appifno.setVerCode(CommonUtil.getVercodeByPkgname(packages[i]));
			appifno.setIsWidget(widgets[i]);
			appifno.setAppDesc(des[i]);
			appInfoList.add(appifno);
		}
		return appInfoList;
	}
	
	@SuppressLint("NewApi")
	public static boolean isDefaultPackage(String pkgname)
	{
		if (pkgname != null && !pkgname.isEmpty())
		{
			Context context = KMarcketApplication.getContext();
			String[] packages = context.getResources().getStringArray(R.array.internal_package);
			for (int i=0; i<packages.length; i++)
			{
				if (pkgname.equals(packages[i]))
				{
					return true;
				}
			}
		}
		return false;
	}
}
