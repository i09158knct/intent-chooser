package net.i09158knct.android.intentchooser;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;

public class AppInfo {

    public static List<AppInfo> convertFromResolveInfoList(PackageManager pm, List<ResolveInfo> apps) {
        ArrayList<AppInfo> appInfoList = new ArrayList<>(apps.size());
        for (ResolveInfo app : apps) {
            ActivityInfo info = app.activityInfo;
            AppInfo appInfo = new AppInfo(pm, info);
            appInfoList.add(appInfo);
        }
        return appInfoList;
    }

    public String packageName;
    public String className;
    public String label;

    public AppInfo(String packageName, String className, String label) {
        this.packageName = packageName;
        this.className = className;
        this.label = label;
    }

    public AppInfo(PackageManager pm, ActivityInfo info) {
        this(info.packageName, info.name, info.loadLabel(pm).toString());
    }

    public String getKey() {
        return packageName + className;
    }
}
