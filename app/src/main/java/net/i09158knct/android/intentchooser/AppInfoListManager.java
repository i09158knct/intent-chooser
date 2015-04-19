package net.i09158knct.android.intentchooser;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppInfoListManager {

    private static final String PREFERENCE_NAME_HIDDEN_APP_LIST = "hidden_app_list";
    private static final String PREFERENCE_NAME_APP_LIST = "app_list";
    private static final String LINE_BREAK = "\n";
    private static final String SEPARATOR = "\t";

    private static AppInfo deserializeAppInfo(String appInfoString) {
        String[] appInfo = appInfoString.split(SEPARATOR);
        return new AppInfo(
                appInfo[0],
                appInfo[1],
                appInfo[2]
        );
    }

    private static List<AppInfo> parseAppInfoListString(String appInfoListString) {
        String[] appInfoStringList = appInfoListString.split(LINE_BREAK);
        ArrayList<AppInfo> appInfoList = new ArrayList<>();
        for (int i = 0; i < appInfoStringList.length - 1; i++) {
            String appInfoString = appInfoStringList[i];
            appInfoList.add(deserializeAppInfo(appInfoString));
        }
        return appInfoList;
    }

    private static String serializeAppInfo(AppInfo app) {
        return app.packageName + SEPARATOR + app.className + SEPARATOR + app.label;
    }

    private static String convertAppInfoListToString(List<AppInfo> apps) {
        StringBuilder builder = new StringBuilder();
        for (AppInfo app : apps) {
            builder.append(serializeAppInfo(app)).append(LINE_BREAK);
        }
        return builder.toString();
    }

    private static String extractPreferenceKey(Intent intent) {
        return intent.getAction() + intent.getScheme() + intent.getType();
    }


    private final Context mContext;
    private final SharedPreferences mAppListPreference;
    private final SharedPreferences mHiddenListPreference;

    public AppInfoListManager(Context context) {
        mContext = context;
        mAppListPreference = context.getSharedPreferences(PREFERENCE_NAME_APP_LIST, 0);
        mHiddenListPreference = context.getSharedPreferences(PREFERENCE_NAME_HIDDEN_APP_LIST, 0);
    }

    public List<AppInfo> getAppInfoList(Intent intent, boolean filterIsEnable, boolean forceQuerying) {
        String key = extractPreferenceKey(intent);
        List<AppInfo> appInfoList;
        if (mAppListPreference.contains(key) && !forceQuerying) {
            appInfoList = loadAppInfoList(key);
        } else {
            appInfoList = queryAppInfoList(intent);
            saveAppInfoList(key, appInfoList);
        }

        if (filterIsEnable) {
            List<String> hiddenAppList = loadHiddenList(key);
            return filterAppList(key, appInfoList, hiddenAppList);
        } else {
            return appInfoList;
        }
    }

    public List<AppInfo> getAppInfoList(Intent intent) {
        return getAppInfoList(intent, true, false);
    }

    public void addHiddenApp(Intent intent, AppInfo appInfo) {
        String key = extractPreferenceKey(intent);
        List<String> hiddenAppList = loadHiddenList(key);
        hiddenAppList.add(appInfo.getKey());
        saveHiddenList(key, hiddenAppList);
    }

    private List<AppInfo> filterAppList(String key, List<AppInfo> appInfoList, List<String> hiddenAppList) {
        List<AppInfo> filteredAppList = new ArrayList<>(appInfoList.size());
        for (int i = 0; i < appInfoList.size(); i++) {
            AppInfo appInfo = appInfoList.get(i);
            if (!hiddenAppList.contains(appInfo.getKey())) {
                filteredAppList.add(appInfo);
            }
        }
        return filteredAppList;
    }

    private List<String> loadHiddenList(String key) {
        return new ArrayList<>(Arrays.asList(mHiddenListPreference.getString(key, "").split(LINE_BREAK)));
    }

    private void saveHiddenList(String key, List<String> hiddenList) {
        StringBuilder builder = new StringBuilder();
        for (String hiddenAppKey : hiddenList) {
            builder.append(hiddenAppKey).append(LINE_BREAK);
        }

        mHiddenListPreference.edit()
                .putString(key, builder.toString())
                .apply();
    }

    private List<AppInfo> queryAppInfoList(Intent intent) {
        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> resolveInfos = mContext.getPackageManager().queryIntentActivities(intent, 0);
        return AppInfo.convertFromResolveInfoList(pm, resolveInfos);
    }

    private List<AppInfo> loadAppInfoList(String key) {
        return parseAppInfoListString(mAppListPreference.getString(key, ""));
    }

    private void saveAppInfoList(String key, List<AppInfo> appInfoList) {
        mAppListPreference.edit()
                .putString(key, convertAppInfoListToString(appInfoList))
                .apply();
    }
}
