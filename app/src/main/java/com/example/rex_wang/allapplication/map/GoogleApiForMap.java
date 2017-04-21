package com.example.rex_wang.allapplication.map;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class GoogleApiForMap extends AppCompatActivity implements Toolbar.OnMenuItemClickListener, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private String TAG = "--- GoogleApiForMap ---";
    private String MAP_URL = "http://163.18.42.22/gmap/web/tinymap";
    private Toolbar mToolbar;
    private WebView mWebView;

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationRequest mLocationRequest;
    private String lat, lng;
    private boolean webReady = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_api_for_map);
        Log.d(TAG, " onCreate ");

        // import support.v7.widget.Toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar_map_googleapi);
        mWebView = (WebView) findViewById(R.id.webview_map_googleapi);
        init();
    }

    private void init() {
        Log.d(TAG, " init ");
        setLocationGoogleApi();
        setSupportActionBar(mToolbar);
        mToolbar.setTitle(R.string.GoogleApiForMap_name);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, " onResume ");

        if (!NetworkSetting.isConnected(this)) {
            startActivity(NetworkSetting.linkWifi());
        } else {
            mGoogleApiClient.connect();
            linkWebView();
            mToolbar.setOnMenuItemClickListener(this);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, " onPause ");
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
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


    private void setLocationGoogleApi() {
        Log.d(TAG, " setLocationGoogleApi ");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
        mWebView = (WebView) findViewById(R.id.webview_map_googleapi);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Toast.makeText(GoogleApiForMap.this, " 已加載完畢! ", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, " onConnected ");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, " onConnectionSuspended ");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, " onConnectionFailed ");
    }
}
