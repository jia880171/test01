package com.example.unick.sensordemo.fragments;

import android.app.Notification;
import android.content.ContentValues;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Sampler;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.unick.sensordemo.GEOReverseHelper;
import com.example.unick.sensordemo.R;
import com.example.unick.sensordemo.models.AccPost;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ShowSensorData.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ShowSensorData#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShowSensorData extends Fragment{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


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
    private Double speed;
    private float bearing;


    TextView TextViewValueX;
    TextView TextViewValueY;
    TextView TextViewValueZ;
    Button button_start;
    Button button_stop;
    Handler mHandler;
    Runnable mRun;

    Handler mHandler2;
    Runnable mRun2;

    boolean flagFormRun=true;

    boolean flag_start=false;

    public ShowSensorData() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ShowSensorData.
     */
    // TODO: Rename and change types and number of parameters
    public static ShowSensorData newInstance(String param1, String param2) {
        ShowSensorData fragment = new ShowSensorData();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(sm == null){
            sm = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            int sensorType = Sensor.TYPE_ACCELEROMETER;
            sm.registerListener(myAccelerometerListener,sm.getDefaultSensor(sensorType),SensorManager.SENSOR_DELAY_NORMAL);
        }

        //set location manarger
        if (mLocationManager == null) {
            mLocationManager =
                    (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            mLocationListener = new MyLocationListener();
        }
        // 獲得地理位置的更新資料 (GPS 與 NETWORK都註冊)
        mLocationManager.requestLocationUpdates(LM_GPS, 0, 0, mLocationListener);
        mLocationManager.requestLocationUpdates(LM_NETWORK, 0, 0, mLocationListener);
        //setTitle("onResume ...");

        flagFormRun=true;
        new Thread(mRun = new Runnable() {
            @Override
            public void run() {
                while (flagFormRun){
                    try {
                        Message msg = new Message();
                        msg.what = 1;
                        mHandler.sendMessage(msg);
                        Thread.sleep(100);
                    }
                    catch (Exception e){
                    }
                }
            }
        }).start();
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                TextViewValueX.setText("x:"+xValue);
                TextViewValueY.setText("y:"+yValue);
                TextViewValueZ.setText("z:"+zValue);
                super.handleMessage(msg);
            }
        };

        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag_start = true;
                new Thread(mRun2 = new Runnable() {
                    @Override
                    public void run() {
                        int mCount = 0;
                        stringBuilder_acc = new StringBuilder();
                        while (flag_start){
                            try {
//                                if(mCount>=20){
//                                    acc_record = stringBuilder_acc.toString();
//                                    Log.d("thread for button","count :" + mCount + ", String:"+acc_record);
//                                    writeAccPost(acc_record);
//
//                                }
                                Message msg = new Message();
                                mCount +=1 ;
                                msg.what = 1;
                                msg.arg1 = mCount;
                                mHandler2.sendMessage(msg);
                                Thread.sleep(500);
                                Log.d("Thread","count"+mCount);
                            }
                            catch (Exception e){
                            }
                        }
                    }
                }).start();
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

                        // 將記錄新增到coffee資料表的參數
                        ContentValues cv = new ContentValues();
                        cv.put("Time", true);
                        cv.put("Sum", sum);
                        cv.put("x", xValue);
                        cv.put("y", yValue);
                        cv.put("z", zValue);


//                        // 執行SQL語句
//                        long id = db.insert("coffee_list", null, cv);
//                        Toast.makeText(context, "_id：" + id, Toast.LENGTH_SHORT).show();

                    }
                };


            }
        });

        button_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag_start=false;
                if(flag_start!=true){
                    acc_record = stringBuilder_acc.toString();
                    writeAccPost(acc_record);
                }
            }
        });


    }

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

//            textView1.setText("Location-GPS" + "\n" +
//                    "緯度-Latitude：" + location.getLatitude() + "\n" +
//                    "經度-Longitude：" + location.getLongitude() + "\n" +
//                    "地址-Address : " + GEOReverseHelper.getAddressByLatLng(latLng) + "\n" +
//                    "精確度-Accuracy：" + location.getAccuracy() + "\n" +
//                    "標高-Altitude：" + location.getAltitude() + "\n" +
//                    "時間-Time：" + new Date(location.getTime()) + "\n" +
//                    "速度-Speed：" + location.getSpeed()*3.6 + "km/hr" + "\n" +
//                    "方位-Bearing：" + location.getBearing());
            //setTitle("GPS位置資訊已更新");
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
    }

    private void writeAccPost(String body){
        String key = mDatabase.child("posts").push().getKey();
        AccPost post = new AccPost("01", body);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("posts/" + key,postValues);
        childUpdates.put("user-post/" + "01" + "/" + key, postValues);
        mDatabase.updateChildren(childUpdates);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myInflatedView = inflater.inflate(R.layout.fragment_show_sensor_data,container,false);
        TextViewValueX = (TextView) myInflatedView.findViewById(R.id.tvx);
        TextViewValueY = (TextView) myInflatedView.findViewById(R.id.tvy);
        TextViewValueZ = (TextView) myInflatedView.findViewById(R.id.tvz);
        button_start = (Button) myInflatedView.findViewById(R.id.button_start);
        button_stop = (Button) myInflatedView.findViewById(R.id.button_stop);

        TextViewValueX.setText("x:"+xValue);
        TextViewValueY.setText("y:"+yValue);
        TextViewValueZ.setText("z:"+zValue);
        return myInflatedView;
    }


    public double mLengthCaculate(Vector vector){
        double length;
        float temp_length = 0F;
        for(int i=0; i<3; i++){
            temp_length += (double)(float)vector.get(i) * (double) (float)vector.get(i);
        }
        length = Math.sqrt(temp_length);
        return length;

    }

    public Vector mUnit(Vector vector){
        Vector unit = new Vector();
        double length = mLengthCaculate(vector);
        for (int i=0; i<3; i++){
            float x = (float)vector.get(i) / (float)length;
            unit.add(x);
        }
        return unit;
    }

    public void onPause(){
        /*
         * 很关键的部分：注意，说明文档中提到，即使activity不可见的时候，感应器依然会继续的工作，测试的时候可以发现，没有正常的刷新频率
         * 也会非常高，所以一定要在onPause方法中关闭触发器，否则讲耗费用户大量电量，很不负责。
         * */
        sm.unregisterListener(myAccelerometerListener);
        sm = null;
        flagFormRun=false;
        if (mLocationManager != null) {
            // 移除 mLocationListener 監聽器
            mLocationManager.removeUpdates(mLocationListener);
            mLocationManager = null;
        }
        super.onPause();
    }

//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }
//
//    /**
//     * This interface must be implemented by activities that contain this
//     * fragment to allow an interaction in this fragment to be communicated
//     * to the activity and potentially other fragments contained in that
//     * activity.
//     * <p>
//     * See the Android Training lesson <a href=
//     * "http://developer.android.com/training/basics/fragments/communicating.html"
//     * >Communicating with Other Fragments</a> for more information.
//     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }
}
