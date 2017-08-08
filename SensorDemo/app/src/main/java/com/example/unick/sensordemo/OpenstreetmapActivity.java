package com.example.unick.sensordemo;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;


public class OpenstreetmapActivity extends AppCompatActivity {
    MyLocationNewOverlay mLocationOverlay;
    CompassOverlay mCompassOverlay;
    LocationManager mLocationManager;

    private static Context ctx;
    private static MapView map;
    private static GeoPoint startPoint;
    private static IMapController mapController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_openstreetmap);

        ctx = getApplicationContext();
        org.osmdroid.config.Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //important! set your user agent to prevent getting banned from the osm servers

        mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        Log.d("onCreate", "mLocationManager initial!!");
        openGPS(ctx);
    }

    public void onResume(){
        super.onResume();
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(10);
        //backButton = (Button) findViewById(R.id.button_backToMyPosition);

        //draw user icon
        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(ctx),map);
        mLocationOverlay.enableMyLocation();
        map.getOverlays().add(mLocationOverlay);

        if(mLocationManager == null){
            mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
            Log.d("initial", "mLocationListener");
        }else {
            Log.d("onResume", "mLocationListener and mLocationManager initial!!");
        }
        if(mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
            startPoint = new GeoPoint(mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude(), mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude());
            mapController.setCenter(startPoint);
        }
        //draw compass
        mCompassOverlay = new CompassOverlay(ctx, new InternalCompassOrientationProvider(ctx), map);
        mCompassOverlay.enableCompass();
        map.getOverlays().add(mCompassOverlay);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mLocationManager != null){
            mLocationManager = null;
        }
    }

    // 開啟 GPS
    public void openGPS(Context context) {
        boolean gps = mLocationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        Toast.makeText(context, "GPS : " + gps,
                Toast.LENGTH_SHORT).show();
        if (gps) {
            return;
        } else {
            // 開啟手動GPS設定畫面
            Intent gpsOptionsIntent = new Intent(
                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(gpsOptionsIntent);
        }
    }

}
