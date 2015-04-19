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


public class MainActivity extends ListActivity {

    private List<AppInfo> mAppInfoList;
    private AppInfoListAdapter mAdapter;
    private AppInfoListManager mManager;
    private Intent mIntent;
    private boolean mFilterIsEnable = true;
    private boolean mIconVisibility = false;
    private boolean mForceQuerying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent chooserIntent = getIntent();
        mIntent = chooserIntent.getParcelableExtra(Intent.EXTRA_INTENT);

        CharSequence title = chooserIntent.getCharSequenceExtra(Intent.EXTRA_TITLE);
        if (title != null) {
            setTitle(title);
        }

        mManager = new AppInfoListManager(getApplicationContext());
        updateList(mFilterIsEnable, mForceQuerying, mIconVisibility);
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
                mManager.addHiddenApp(mIntent, mAppInfoList.get(position));
                updateList(mFilterIsEnable, mForceQuerying, mIconVisibility);
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_load_icon:
                mIconVisibility = true;
                mAdapter.setIconVisibility(mIconVisibility);
                mAdapter.notifyDataSetChanged();
                return true;

            case R.id.menu_item_refresh_list:
                updateList(mFilterIsEnable, true, mIconVisibility);
                return true;

            case R.id.menu_item_disable_app_filter:
                mFilterIsEnable = !mFilterIsEnable;
                updateList(mFilterIsEnable, mForceQuerying, mIconVisibility);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        AppInfo appInfo = mAppInfoList.get(position);
        startActivity(mIntent.setClassName(appInfo.packageName, appInfo.className));
        finish();
    }

    private void updateList(boolean filterIsEnable, boolean forceQuerying, boolean iconVisibility) {
        mAppInfoList = mManager.getAppInfoList(mIntent, filterIsEnable, forceQuerying);
        mAdapter = new AppInfoListAdapter(this, mAppInfoList);
        mAdapter.setIconVisibility(iconVisibility);
        setListAdapter(mAdapter);
    }
}
