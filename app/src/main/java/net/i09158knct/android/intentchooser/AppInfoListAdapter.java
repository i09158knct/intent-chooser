package net.i09158knct.android.intentchooser;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class AppInfoListAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mLayoutInflater;
	private PackageManager mPackageManager;
	private List<AppInfo> mAppInfoList;
	private boolean mIconVisibility;

	public AppInfoListAdapter(Activity activity, List<AppInfo> appInfoList) {
		mContext = activity.getApplicationContext();
		mLayoutInflater = activity.getLayoutInflater();
		mPackageManager = activity.getPackageManager();
		mAppInfoList = appInfoList;
	}

	@Override
	public int getCount() {
		return mAppInfoList.size();
	}

	@Override
	public AppInfo getItem(int position) {
		return mAppInfoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
		}
		AppInfo appInfo = mAppInfoList.get(position);

		TextView name = (TextView) convertView;
		name.setText(appInfo.label);

		if (mIconVisibility) {
			try {
				ComponentName componentName = new ComponentName(appInfo.packageName, appInfo.className);
				Drawable activityIcon = mPackageManager.getActivityIcon(componentName);
				name.setCompoundDrawablesWithIntrinsicBounds(activityIcon, null, null, null);
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
		}

		return convertView;
	}

	public void setIconVisibility(boolean visible) {
		mIconVisibility = visible;
	}
}
