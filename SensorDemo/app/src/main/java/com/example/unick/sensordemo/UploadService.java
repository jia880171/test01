package com.example.unick.sensordemo;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.unick.sensordemo.fragments.ShowSensorData;
import com.example.unick.sensordemo.models.AccPost;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by unick on 2017/9/5.
 */

public class UploadService extends Service {
    @Nullable

    private DatabaseReference mDatabase;
    private SensorManager sm ;
    private static final float G = 9.8F;

    private StringBuilder stringBuilder_acc;
    private String acc_record;

    float xValue;
    float yValue;
    float zValue;

    public final String LM_GPS = LocationManager.GPS_PROVIDER;
    public final String LM_NETWORK = LocationManager.NETWORK_PROVIDER;
    // 定位管理器
    private LocationManager mLocationManager;
    // 定位監聽器
    private LocationListener mLocationListener;
    private Double Lat;
    private Double Lon;
    private String add;
    private Date time;
    private Double speed = 0.0;
    private float bearing;

    Handler mHandler;
    Runnable mRun;

    Thread thread_append;
    Handler mHandler2;
    Runnable mRun2;

    boolean flag_mRun;
    boolean flag_start = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("my Service Log","onStartCommand()");

        //set fireBase
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //set sensor manager
        if(sm == null){
            sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            int sensorType = Sensor.TYPE_ACCELEROMETER;
            sm.registerListener(myAccelerometerListener,sm.getDefaultSensor(sensorType),SensorManager.SENSOR_DELAY_NORMAL);
        }

        //set location manager
        if (mLocationManager == null) {
            mLocationManager =
                    (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            mLocationListener = new MyLocationListener();
            // 獲得地理位置的更新資料 (GPS 與 NETWORK都註冊)
            mLocationManager.requestLocationUpdates(LM_GPS, 0, 0, mLocationListener);
            mLocationManager.requestLocationUpdates(LM_NETWORK, 0, 0, mLocationListener);
        }

        //thread for checking speed
        flag_mRun = true;
        new Thread(mRun = new Runnable() {
            @Override
            public void run() {
                while (flag_mRun){
                    try {
                        Message msg = new Message();
                        msg.what = 1;
                        mHandler.sendMessage(msg);
                        Thread.sleep(60000);//checking rate : 1 per/min
                    }
                    catch (Exception e){
                    }
                }
            }
        }).start();
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(speed>=15){
                    Log.d("inService","speed >= 15 ");
                    Log.d("inService checkingSpeed","speed = " + speed);

                    //thread for recording
                    flag_start = true;
                    thread_append = new Thread(mRun2 = new Runnable() {
                        @Override
                        public void run() {
                            int mCount = 0;
                            stringBuilder_acc = new StringBuilder();
                            while (flag_start){
                                try {
                                    Message msg = new Message();
                                    mCount +=1 ;
                                    msg.what = 1;
                                    msg.arg1 = mCount;
                                    mHandler2.sendMessage(msg);
                                    Thread.sleep(500);//record rate : 2 per/second
                                    Log.d("Thread","count"+mCount);
                                    // stop record when speed <=10
                                    if(speed<10){
                                        Log.d("inService","speed < 10 ");
                                        Log.d("inService checkingSpeed","speed = " + speed);
                                        flag_start = false;
                                        acc_record = stringBuilder_acc.toString();
                                        writeAccPost(acc_record);//upload to fireBase
                                    }
                                }
                                catch (Exception e){
                                }
                            }
                        }
                    });
                    thread_append.start();
                    mHandler2 = new Handler(){
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            float sum = Math.abs(xValue)+Math.abs(yValue)+Math.abs(zValue);
                            stringBuilder_acc.append("[");
                            stringBuilder_acc.append("index: ");
                            stringBuilder_acc.append(msg.arg1);
                            stringBuilder_acc.append(", time: ");
                            stringBuilder_acc.append(time);
                            stringBuilder_acc.append(", sum: ");
                            stringBuilder_acc.append(sum);
                            stringBuilder_acc.append(", x: ");
                            stringBuilder_acc.append(xValue);
                            stringBuilder_acc.append(", y: ");
                            stringBuilder_acc.append(yValue);
                            stringBuilder_acc.append(", z: ");
                            stringBuilder_acc.append(zValue);
                            stringBuilder_acc.append(", latitude: ");
                            stringBuilder_acc.append(Lat);
                            stringBuilder_acc.append(", longitude: ");
                            stringBuilder_acc.append(Lon);
                            stringBuilder_acc.append(", address: ");
                            stringBuilder_acc.append(add);
                            stringBuilder_acc.append(", speed: ");
                            stringBuilder_acc.append(speed);
                            stringBuilder_acc.append(", bearing ");
                            stringBuilder_acc.append(bearing);
                            stringBuilder_acc.append("]");
                        }
                    };
                    try {
                        thread_append.join();//wait for child thread
                    } catch (InterruptedException e){

                    }
                }
                super.handleMessage(msg);
            }
        };

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        sm.unregisterListener(myAccelerometerListener);
        sm = null;
        if (mLocationManager != null) {
            // 移除 mLocationListener 監聽器
            mLocationManager.removeUpdates(mLocationListener);
            mLocationManager = null;
        }
        flag_mRun = false;
        super.onDestroy();
    }


    final SensorEventListener myAccelerometerListener = new SensorEventListener(){

        public void onSensorChanged(SensorEvent sensorEvent){
            if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                xValue = sensorEvent.values[0] / G;
                yValue = sensorEvent.values[1] / G;
                zValue = sensorEvent.values[2] / G;
            }
        }

        public void onAccuracyChanged(Sensor sensor , int accuracy){
            Log.i(TAG, "onAccuracyChanged");
        }
    };


    // 定位監聽器實作
    private class MyLocationListener implements LocationListener {
        // GPS位置資訊已更新
        public void onLocationChanged(Location location) {
            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

            time = new Date(location.getTime());
            Lat = location.getLatitude();
            Lon = location.getLongitude();
            add = GEOReverseHelper.getAddressByLatLng(latLng);
            speed = location.getSpeed()*3.6;
            bearing = location.getBearing();

            Log.d("mLocationManager","speed: " +speed);
            Log.d("mLocationManager","add: " +add);
        }
        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }
        // GPS位置資訊的狀態被更新
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    //setTitle("服務中");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    //setTitle("沒有服務");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    //setTitle("暫時不可使用");
                    break;
            }
        }
    }//end of 定位監聽器實作

    private void writeAccPost(String body){
        String key = mDatabase.child("posts").push().getKey();
        AccPost post = new AccPost("01", body);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("posts/" + key,postValues);
        childUpdates.put("user-post/" + "01" + "/" + key, postValues);
        mDatabase.updateChildren(childUpdates);
    }
}
