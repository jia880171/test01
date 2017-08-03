package com.example.unick.sensordemo;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class OpenstreetmapActivity extends AppCompatActivity {
    MyLocationNewOverlay mLocationOverlay;
    CompassOverlay mCompassOverlay;
    LocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_openstreetmap);

        Context ctx = getApplicationContext();
        org.osmdroid.config.Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //important! set your user agent to prevent getting banned from the osm servers

        MapView map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        //move the map on a default view point
        IMapController mapController = map.getController();
        mapController.setZoom(10);

        //draw user icon
        mLocationOverlay = new MyLocationNewOverlay(new MyGPSProvider(ctx),map);
        mLocationOverlay.enableMyLocation();
        map.getOverlays().add(mLocationOverlay);

        //set start point
        mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        Location currentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        GeoPoint startPoint = new GeoPoint(currentLocation.getLatitude(),currentLocation.getLongitude());
        mapController.setCenter(startPoint);

        //draw compass
        mCompassOverlay = new CompassOverlay(ctx, new InternalCompassOrientationProvider(ctx), map);
        mCompassOverlay.enableCompass();
        map.getOverlays().add(mCompassOverlay);
    }

    public void onResume(){
        super.onResume();
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
    }

}