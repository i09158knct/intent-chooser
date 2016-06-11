package net.i09158knct.android.intentchooser;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;


public class ChooserActivity extends ListActivity {

    private List<AppInfo> mAppInfoList;
    private AppInfoListAdapter mAdapter;
    private AppInfoListManager mManager;
    private Intent mIntent;
    private boolean mFilterIsEnable = true;
    private boolean mIconVisibility = false;
    private boolean mAlwaysLoadFromPackageManager = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooser);

        // ユーザーによるアプリ選択後に送信するために
        // 受け取ったインテントをフィールドに格納する。
        Intent chooserIntent = getIntent();
        mIntent = chooserIntent.getParcelableExtra(Intent.EXTRA_INTENT);

        // インテントにタイトルが含まれていればそれを表示する。
        CharSequence title = chooserIntent.getCharSequenceExtra(Intent.EXTRA_TITLE);
        if (title != null) {
            setTitle(title);
        }

        // アプリマネージャーを作成し候補アプリリストを作成する。
        mManager = new AppInfoListManager(getApplicationContext());
        updateList(mFilterIsEnable, mAlwaysLoadFromPackageManager, mIconVisibility);

        registerForContextMenu(getListView());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.context_app_info, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;
        switch (item.getItemId()) {
            case R.id.context_app_info__hide:
                // 非表示アプリリストに追加し、リストを更新する。
                mManager.addHiddenApp(mIntent, mAppInfoList.get(position));
                updateList(mFilterIsEnable, mAlwaysLoadFromPackageManager, mIconVisibility);
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_chooser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // アプリアイコンを表示する。
            case R.id.menu_item_load_icon:
                mIconVisibility = true;
                mAdapter.setIconVisibility(mIconVisibility);
                mAdapter.notifyDataSetChanged();
                return true;

            // アプリリストを再読込する。
            // 内部的には、キャッシュからではなく強制的にシステムからアプリリストを取得し直している。
            case R.id.menu_item_refresh_list:
                updateList(mFilterIsEnable, true, mIconVisibility);
                return true;

            // 非表示アプリの表示非表示をトグルする。
            case R.id.menu_item_toggle_app_filter:
                mFilterIsEnable = !mFilterIsEnable;
                updateList(mFilterIsEnable, mAlwaysLoadFromPackageManager, mIconVisibility);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        AppInfo appInfo = mAppInfoList.get(position);
        Intent intent = mIntent.setClassName(appInfo.packageName, appInfo.className);

        if (mIntent.getAction().equals(Intent.ACTION_GET_CONTENT)) {
            startActivityForResult(intent, 1);
        }
        else {
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            setResult(resultCode, data);
            finish();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateList(boolean filterIsEnable, boolean forceQuerying, boolean iconVisibility) {
        mAppInfoList = mManager.getAppInfoList(mIntent, filterIsEnable, forceQuerying);
        mAdapter = new AppInfoListAdapter(this, mAppInfoList);
        mAdapter.setIconVisibility(iconVisibility);
        setListAdapter(mAdapter);
    }
}
