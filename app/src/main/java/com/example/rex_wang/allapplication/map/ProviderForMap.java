package com.example.rex_wang.allapplication.map;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
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
import com.example.rex_wang.allapplication.utility.NetworkSetting;
import com.google.android.gms.location.LocationListener;

public class ProviderForMap extends AppCompatActivity implements Toolbar.OnMenuItemClickListener, LocationListener {
    private String TAG = "--- ProviderForMap ---";
    private String MAP_URL = "http://163.18.42.22/gmap/web/tinymap";
    private Toolbar mToolbar;
    private WebView mWebView;
    private LocationManager mLocationManager;
    private Location mLocation;
    private String lat, lng;
    private boolean webReady = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_for_map);
        Log.d(TAG, " onCreate ");

        // import support.v7.widget.Toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar_map_provider);
        mWebView = (WebView) findViewById(R.id.webview_map_provider);
        init();
    }

    private void init() {
        Log.d(TAG, " init ");
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle(R.string.ProviderForMap_name);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, " onResume ");

        if (!NetworkSetting.isConnected(this)) {
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
                sendInfo();
                break;
        }
        return false;
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
        if (mLocation != null && webReady) {
            // 經度
            lng = String.valueOf(mLocation.getLongitude());
            // 緯度
            lat = String.valueOf(mLocation.getLatitude());

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

    //---------------------------------------------------------------------------------------------//

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, " onLocationChanged ");
        if (location != null) {
            mLocation = location;
        }
    }
}
