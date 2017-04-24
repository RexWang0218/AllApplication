package com.example.rex_wang.allapplication.map;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.rex_wang.allapplication.R;
import com.example.rex_wang.allapplication.beaccon.BeaconFormat;
import com.example.rex_wang.allapplication.beaccon.BeaconList;
import com.example.rex_wang.allapplication.utility.BluetoothSetting;
import com.example.rex_wang.allapplication.utility.DL;
import com.example.rex_wang.allapplication.utility.NetworkSetting;
import com.google.android.gms.location.LocationListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.List;

public class ProviderForMap extends AppCompatActivity implements Toolbar.OnMenuItemClickListener, LocationListener, BluetoothAdapter.LeScanCallback {
    private String TAG = "--- ProviderForMap ---";
    private String MAP_URL = "http://163.18.42.22/gmap/web/tinymap";
    private String beaconURL = "";
    private Toolbar mToolbar;
    private WebView mWebView;
    private LocationManager mLocationManager;
    private Location mLocation;
    private String lat, lng;
    private boolean webReady = false, isScanning;
    private BluetoothAdapter mBluetoothAdapter;
    private String firstBeacon;
    private Double beaconDistance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_for_map);
        Log.d(TAG, " onCreate ");

        init();
    }

    private void init() {
        Log.d(TAG, " init ");
        // import support.v7.widget.Toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar_map_provider);
        mWebView = (WebView) findViewById(R.id.webview_map_provider);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle(R.string.ProviderForMap_name);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, " onResume ");

        if (!BluetoothSetting.isSupport(this)) {
            Toast.makeText(this, "此設備不支援藍芽技術", Toast.LENGTH_SHORT).show();
            finish();
        } else if (!NetworkSetting.isConnected(this)) {
            startActivity(NetworkSetting.linkWifi());

        } else if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Intent it = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(it);
        } else {
            linkWebView();
            setLocationProvider();
            mToolbar.setOnMenuItemClickListener(this);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, " onPause ");
        mWebView.stopLoading();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 讓 Toolbar 的 Menu 有作用
        Log.d(TAG, " onCreateOptionsMenu ");
        getMenuInflater().inflate(R.menu.menu_layout, menu);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Log.d(TAG, " onMenuItemClick ");
        switch (item.getItemId()) {
            case R.id.menu_location:
                lat = new String();
                lng = new String();
                startScan();
                sendInfo();
                break;
        }
        return false;
    }

    private void startScan() {
        if (BluetoothSetting.isConnected(this)) {
            startActivity(BluetoothSetting.turnOnBluetooth());
        } else {
            mBluetoothAdapter = BluetoothSetting.bluetoothAdapter;
            if (isScanning) {
                stopScan();
            }
            mBluetoothAdapter.startLeScan(this);
            isScanning = true;
        }
    }

    private void stopScan() {
        if (isScanning) {
            mBluetoothAdapter.stopLeScan(this);
            isScanning = false;
        }
    }

    // 找出定位法
    private void setLocationProvider() {
        Log.d(TAG, " setLocationProvider ");

        Criteria mCriteria = new Criteria();
        mCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        mCriteria.setAltitudeRequired(false);
        mCriteria.setBearingRequired(false);
        mCriteria.setCostAllowed(true);
        mCriteria.setPowerRequirement(Criteria.POWER_LOW);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocation = mLocationManager.getLastKnownLocation(mLocationManager.getBestProvider(mCriteria, true));
    }

    private void sendInfo() {
        if (lat == null || lng == null) {
            if (mLocation != null && webReady) {
                // 經度
                lng = String.valueOf(mLocation.getLongitude());
                // 緯度
                lat = String.valueOf(mLocation.getLatitude());
            } else {
                Toast.makeText(this, "定位失敗", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        //由輸入的經緯度值標註在地圖上，呼叫在googlemaps.html中的mark函式
        final String markURL = "javascript:mark(" +
                lat + "," +
                lng + ")";
        mWebView.loadUrl(markURL);

        //畫面移至標註點位置，呼叫在googlemaps.html中的centerAt函式
        final String centerURL = "javascript:centerAt(" +
                lat + "," +
                lng + ")";
        mWebView.loadUrl(centerURL);
    }

    private void linkWebView() {
        mWebView = (WebView) findViewById(R.id.webview_map_provider);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Toast.makeText(ProviderForMap.this, " 已加載完畢! ", Toast.LENGTH_SHORT).show();
                webReady = true;
            }
        });
        mWebView.loadUrl(MAP_URL);
    }

    private class SearchLocationInfo extends AsyncTask<String, Integer, String> {
        private ProgressDialog dialog;

        @Override
        protected String doInBackground(String... params) {
            Log.i(TAG, params[0]);
            InputStream is = DL.getURL(params[0]);
            String JsonString = DL.streamToString(is, "UTF-8");
            return JsonString;
        }

        @Override
        protected void onPreExecute() {
            //背景執行續進度對話框
            dialog = DL.createProgressDialog(ProviderForMap.this, "獲取資料中...");
            dialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            dialog.dismiss();
            Log.i("資料測試", s);
            Boolean Received = false;
            try {
                JSONArray jsonArray = new JSONArray(s);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    if (firstBeacon.equals(jsonObject.getString("address"))) {
                        Log.i(TAG, "--------------------");

                        lat = jsonObject.getString("lat");
                        lng = jsonObject.getString("lng");

                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    //---------------------------------------------------------------------------------------------//

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, " onLocationChanged ");
        if (location != null) {
            mLocation = location;
        }
    }

    @Override
    public void onLeScan(final BluetoothDevice bluetoothDevice, final int rssi, final byte[] scanBytes) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String ScanResult = BeaconList.decode(bluetoothDevice, rssi, scanBytes);
                switch (ScanResult) {
                    case "success":
                        stopScan();
//                        SQLiteDatabase dw = DB.getWritableDatabase();
//                        SQLiteDatabase dr = DB.getReadableDatabase();
//                        Cursor cursor = dr.rawQuery("SELECT * FROM BeaconTable", null);

//                        if (cursor.getCount() != 0) {
//                            Log.i(TAG, String.valueOf(cursor.getCount()));
//                            dw.execSQL("DROP TABLE IF EXISTS BeaconTable");
//                            DB.onCreate(dw);
//                        }

                        Boolean contain = true;
                        List<BeaconFormat> BL = BeaconList.mList;
                        for (int i = 0; i < BL.size(); i++) {
                            BeaconFormat mBeaconFormat = BL.get(i);
                            if (mBeaconFormat.getDistance().equals("0.00")) {
                                contain = false;
                                break;
                            } else {
                                Log.i(TAG, mBeaconFormat.getAddress());
                                Log.i(TAG, mBeaconFormat.getName());
                                Log.i(TAG, String.valueOf(mBeaconFormat.getRssi()));
                                Log.i(TAG, mBeaconFormat.getDistance());
                            }
                        }


                        if (contain) {
                            BeaconFormat mBeaconFormat = BL.get(0);
                            firstBeacon = new String();
                            firstBeacon = mBeaconFormat.getAddress();
                            beaconDistance = mBeaconFormat.getDistanceDouble();
                            if (beaconDistance < 8) {
                                SearchLocationInfo searchLocationInfo = new SearchLocationInfo();
                                searchLocationInfo.execute(beaconURL);
                            } else {
                                return;
                            }

                        } else {
                            Log.i(TAG, " Scan有誤 ");
                            BeaconList.cleanList();
                            startScan();
                        }
                        break;

                    case "搜尋不到適合的裝置...":
                        stopScan();
                        Toast.makeText(ProviderForMap.this, "", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }


}
