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
import static com.google.android.gms.internal.zzagz.runOnUiThread;

/**
 * Created by unick on 2017/9/5.
 */

public class UploadService extends Service {
    @Nullable

    private DatabaseReference mDatabase;

    private StringBuilder stringBuilder_acc;
    private String acc_record;

    public final String LM_GPS = LocationManager.GPS_PROVIDER;
    public final String LM_NETWORK = LocationManager.NETWORK_PROVIDER;

    private int mCount=0;
    // 定位管理器
    private LocationManager mLocationManager;
    // 定位監聽器
    private LocationListener mLocationListener;
    private LatLng latLng;
    //private String add;
    private Date time;
    private Double speed = 0.0;
    //轉向器
    private SensorManager sensor_manager;
    private MySensorEventListener listener;
    private float degree;

    Runnable mRun;
    private boolean flag_mRun;
    private boolean flag_recording;
    private boolean flag_have_record;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, final int flags, int startId) {
        Log.i("my Service Log","onStartCommand()");

        //set fireBase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //set location manager
        if (mLocationManager == null) {
            mLocationManager =
                    (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            mLocationListener = new MyLocationListener();
            // 獲得地理位置的更新資料 (GPS 與 NETWORK都註冊)
            mLocationManager.requestLocationUpdates(LM_GPS, 0, 0, mLocationListener);
            mLocationManager.requestLocationUpdates(LM_NETWORK, 0, 0, mLocationListener);
        }
        //set getOrientation
        sensor_manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor aSensor = sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor mfSensor = sensor_manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        listener = new MySensorEventListener();
        sensor_manager.registerListener(listener, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensor_manager.registerListener(listener, mfSensor, SensorManager.SENSOR_DELAY_NORMAL);

        //thread for checking speed
        stringBuilder_acc = new StringBuilder();
        flag_mRun = true;
        flag_have_record = false;
        new Thread(mRun = new Runnable() {
            @Override
            public void run() {
                while (flag_mRun){
                    try {
                        if(flag_recording==false && speed>=15){
                            flag_recording=true;
                            Log.d("inService","speed >= 15, start recording!");
                        } else if(flag_recording){
                            Log.d("inService","speed = " + speed);
                            Log.d("inService","recording...");
                            stringBuildAppend();
                            if(speed>5){
                                Log.d("inService","set mCount to 0");
                                mCount=0;
                            } else if(speed<5){
                                mCount = mCount +1;
                                Log.d("inService","mCount:" + mCount);
                                if(mCount>=5){
                                    flag_recording=false;
                                    Log.d("inService","speed = " + speed);
                                    Log.d("inService","speed <5, start uploading!");
                                    acc_record = stringBuilder_acc.toString();
                                    writeAccPost(acc_record);//upload to fireBase
                                }
                            }
                            Thread.sleep(500);//record rate : 2 per/sec
                        } else {
                            Log.d("checking","60 per/min");
                            stringBuilder_acc = new StringBuilder();
                            Thread.sleep(60000);//checking rate : 1 per/min
                        }
                    }
                    catch (Exception e){
                    }
                }
            }
        }).start();
        return Service.START_STICKY;
    }

    public void stringBuildAppend(){
        stringBuilder_acc.append("[");
        stringBuilder_acc.append("time: ");
        stringBuilder_acc.append(time);
        stringBuilder_acc.append(", latitude and longitude: ");
        stringBuilder_acc.append(latLng);
        stringBuilder_acc.append(", speed: ");
        stringBuilder_acc.append(speed);
        stringBuilder_acc.append(", degree: ");
        stringBuilder_acc.append(degree);
        stringBuilder_acc.append("]");
    }

    @Override
    public void onDestroy() {
        if (mLocationManager != null) {
            // 移除 mLocationListener 監聽器
            mLocationManager.removeUpdates(mLocationListener);
            mLocationManager = null;
        }
        sensor_manager.unregisterListener(listener);
        flag_mRun = false;
        super.onDestroy();
    }

    // 定位監聽器實作
    private class MyLocationListener implements LocationListener {
        // GPS位置資訊已更新
        public void onLocationChanged(Location location) {
            latLng = new LatLng(location.getLatitude(),location.getLongitude());
            time = new Date(location.getTime());
            //add = GEOReverseHelper.getAddressByLatLng(latLng);
            speed = location.getSpeed()*3.6;
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
        //childUpdates.put("user-post/" + "01" + "/" + key, postValues);
        mDatabase.updateChildren(childUpdates);
    }

    //轉向器
    private class MySensorEventListener implements SensorEventListener {

        private float[] accelerometerValues;
        private float[] magneticFieldValues;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = (float[]) event.values.clone();
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticFieldValues = (float[]) event.values.clone();
            }
            if (accelerometerValues != null && magneticFieldValues != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 計算方位
                        calculateOrientation();
                    }
                });
            }
        }
        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {
            // TODO Auto-generated method stub
        }

        // 計算方位
        private void calculateOrientation() {
            //(-180~180) 0:正北，90:正東，180/-180:正南，-90:正西
            float[] values = new float[3];
            float[] inR = new float[9];
            SensorManager.getRotationMatrix(inR, null, accelerometerValues, magneticFieldValues);

            // 利用重映方向參考坐標系 (非必要)
            float[] outR = new float[9];
            SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);

            SensorManager.getOrientation(inR, values); // 第一個參數可以置換 inR 或 outR

            values[0] = (float) Math.toDegrees(values[0]);
            degree = values[0];
        }

    }
}
