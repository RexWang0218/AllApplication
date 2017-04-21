package com.example.rex_wang.allapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, Handler.Callback {
    private static final int MSG_LIST = 100;
    private static final String TAG = "--- MainActivity ---";
    private ListView mListView;
    private Handler mHandler = new Handler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, " onCreate ");

        mListView = (ListView) findViewById(R.id.lv_main);
        mListView.setOnItemClickListener(this);

        init();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Log.d(TAG, " onItemClick ");
        Map<String, Object> map = (Map<String, Object>) adapterView.getItemAtPosition(position);
        Intent intent = (Intent) map.get("intent");
        startActivity(intent);
    }

    @Override
    public boolean handleMessage(Message msg) {
        Log.d(TAG, " handleMessage ");
        if (msg.what == MSG_LIST) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) msg.obj;
            SimpleAdapter mAdapter = new SimpleAdapter(this, list, android.R.layout.simple_list_item_1, new String[]{"title"}, new int[]{android.R.id.text1});
            // ListView 設置 adapter
            mListView.setAdapter(mAdapter);
        }
        return false;
    }

    private void init() {
        Log.d(TAG, " initList ");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                List<Map<String, Object>> list = getData();
                mHandler.sendMessage(mHandler.obtainMessage(MSG_LIST, list));
            }
        });
        thread.start();
    }

    public List<Map<String, Object>> getData() {
        Log.d(TAG, " getData ");
        List<Map<String, Object>> PackageData = new ArrayList<Map<String, Object>>();

        Intent mIntent = new Intent(Intent.ACTION_MAIN, null);
        mIntent.addCategory(Intent.CATEGORY_SAMPLE_CODE);
        PackageManager mPackageManager = getPackageManager();

        List<ResolveInfo> List_ResolveInfo = mPackageManager.queryIntentActivities(mIntent, 0);
        if (List_ResolveInfo == null) {
            return PackageData; // 空
        }

        String packagename = getPackageName();
        int count = List_ResolveInfo.size();
        for (int i = 0; i < count; i++) {
            ResolveInfo resolveInfo = List_ResolveInfo.get(i);
            // Activity Title
            CharSequence labelSeq = resolveInfo.loadLabel(mPackageManager);
            String labelTitle = labelSeq.toString();

            if (resolveInfo.activityInfo != null && resolveInfo.activityInfo.packageName.equals(packagename)) {
                // ( Packaga Path , Activity Path )
                addItem(PackageData, labelTitle, activityIntent(resolveInfo.activityInfo.applicationInfo.packageName, resolveInfo.activityInfo.name));

            }
        }
        Collections.sort(PackageData, DisplayNameComparator);
        return PackageData;
    }


    private void addItem(List<Map<String, Object>> packageData, String labelTitle, Intent activityIntent) {
        Log.d(TAG, " addItem ");
        String activityPath = activityIntent.getComponent().getClassName(); // 路徑
        int pos = activityPath.lastIndexOf('.');

        String activityName = pos > 0 ? activityPath.substring(pos + 1) : activityPath;

        Map<String, Object> temp = new HashMap<String, Object>();
        temp.put("title", String.format("%s \n[ %s ]", activityName, labelTitle));
        temp.put("intent", activityIntent);
        packageData.add(temp);
    }

    private Intent activityIntent(String packageName, String activityName) {
        Log.d(TAG, " activityIntent ");

        Intent it = new Intent();
        it.setClassName(packageName, activityName);
        return it;
    }

    private final static Comparator<Map<String, Object>> DisplayNameComparator = new Comparator<Map<String, Object>>() {
        private final Collator collator = Collator.getInstance();

        public int compare(Map<String, Object> map1, Map<String, Object> map2) {
            Log.d(TAG, " addItem ");
            return collator.compare(map1.get("title"), map2.get("title"));
        }
    };


}
