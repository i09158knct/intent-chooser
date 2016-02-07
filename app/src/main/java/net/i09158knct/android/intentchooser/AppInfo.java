package net.i09158knct.android.intentchooser;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;

public class AppInfo {

    private static final String SEPARATOR_ROW = "\n";
    private static final String SEPARATOR_COL = "\t";

    public static List<AppInfo> createList(PackageManager pm, List<ResolveInfo> apps) {
        ArrayList<AppInfo> appInfoList = new ArrayList<>(apps.size());
        for (ResolveInfo app : apps) {
            ActivityInfo info = app.activityInfo;
            AppInfo appInfo = new AppInfo(pm, info);
            appInfoList.add(appInfo);
        }
        return appInfoList;
    }

    public static AppInfo deserialize(String appInfoString) {
        String[] appInfo = appInfoString.split(SEPARATOR_COL);
        return new AppInfo(
                appInfo[0],
                appInfo[1],
                appInfo[2]
        );
    }

    public static List<AppInfo> deserializeList(String appInfoListString) {
        String[] appInfoStringList = appInfoListString.split(SEPARATOR_ROW);
        ArrayList<AppInfo> appInfoList = new ArrayList<>();
        for (int i = 0; i < appInfoStringList.length - 1; i++) {
            String appInfoString = appInfoStringList[i];
            AppInfo app = deserialize(appInfoString);
            appInfoList.add(app);
        }
        return appInfoList;
    }

    public static String serializeList(List<AppInfo> apps) {
        StringBuilder builder = new StringBuilder();
        for (AppInfo app : apps) {
            String s = app.serialize();
            builder.append(s).append(SEPARATOR_ROW);
        }
        return builder.toString();
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

    public String serialize() {
        return packageName + SEPARATOR_COL + className + SEPARATOR_COL + label;
    }

}
