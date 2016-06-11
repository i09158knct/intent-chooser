package net.i09158knct.android.intentchooser;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppInfoListManager {

    private static final String PREFERENCE_NAME_HIDDEN_APP_LIST = "hidden_app_list";
    private static final String PREFERENCE_NAME_APP_LIST = "app_list";
    private static final String SEPARATOR_ROW = "\n";

    // PackageManager.MATCH_ALLはAPI Levelが23以上でないと使えないので自前で定義しておく。
    private static final int MATCH_ALL = 131072;

    private static String extractPreferenceKey(Intent intent) {
        // TODO WebからPDFを開くときにPDFリーダーが候補に出ないことがあるので要調査。
        String action = intent.getAction();
        String scheme = intent.getScheme();
        String type = intent.getType();
        return action + scheme + type;
    }


    private final PackageManager mPackageManager;
    private final SharedPreferences mAppListPreference;
    private final SharedPreferences mHiddenListPreference;

    public AppInfoListManager(Context context) {
        mPackageManager = context.getPackageManager();
        mAppListPreference = context.getSharedPreferences(PREFERENCE_NAME_APP_LIST, 0);
        mHiddenListPreference = context.getSharedPreferences(PREFERENCE_NAME_HIDDEN_APP_LIST, 0);
    }

    public List<AppInfo> getAppInfoList(Intent intent, boolean enableFilter, boolean loadFromPackageManagerAnyway) {
        String key = extractPreferenceKey(intent);
        List<AppInfo> appInfoList;
        if (mAppListPreference.contains(key) && !loadFromPackageManagerAnyway) {
            appInfoList = load(key);
        } else {
            appInfoList = loadFromPackageManager(intent);
            save(key, appInfoList);
        }

        if (enableFilter) {
            List<String> hiddenAppList = loadHiddenList(key);
            return filter(key, appInfoList, hiddenAppList);
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

    private List<AppInfo> filter(String key, List<AppInfo> appInfoList, List<String> hiddenAppList) {
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
        String textHidden = mHiddenListPreference.getString(key, "");
        String[] hidden = textHidden.split(SEPARATOR_ROW);
        return new ArrayList<>(Arrays.asList(hidden));
    }

    private void saveHiddenList(String key, List<String> hiddenList) {
        StringBuilder builder = new StringBuilder();
        for (String hiddenAppKey : hiddenList) {
            builder.append(hiddenAppKey).append(SEPARATOR_ROW);
        }

        mHiddenListPreference.edit()
                .putString(key, builder.toString())
                .apply();
    }

    private List<AppInfo> loadFromPackageManager(Intent intent) {
        List<ResolveInfo> resolveInfos = mPackageManager.queryIntentActivities(intent, MATCH_ALL);
        return AppInfo.createList(mPackageManager, resolveInfos);
    }

    private List<AppInfo> load(String key) {
        String textApps = mAppListPreference.getString(key, "");
        return AppInfo.deserializeList(textApps);
    }

    private void save(String key, List<AppInfo> appInfoList) {
        String text = AppInfo.serializeList(appInfoList);
        mAppListPreference.edit()
                .putString(key, text)
                .apply();
    }
}
