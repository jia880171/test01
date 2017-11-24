package com.example.unick.sensordemo.fragments;

import android.Manifest;
import static android.Manifest.permission.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.unick.sensordemo.R;
import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

import static com.google.android.gms.internal.zzagz.runOnUiThread;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ShowGPS.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ShowGPS#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShowGPS extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public final String LM_GPS = LocationManager.GPS_PROVIDER;
    public final String LM_NETWORK = LocationManager.NETWORK_PROVIDER;

    private Context context;
    private TextView textView1;
    private Button button;
    // 定位管理器
    private LocationManager mLocationManager;
    // 定位監聽器
    private LocationListener mLocationListener;
    //轉向監視器

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private SensorManager sensor_manager;
    private MySensorEventListener listener = new MySensorEventListener();
    private TextView textView2;

    Sensor aSensor;
    Sensor mfSensor;

    //private OnFragmentInteractionListener mListener;

    public ShowGPS() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ShowGPS.
     */
    // TODO: Rename and change types and number of parameters
    public static ShowGPS newInstance(String param1, String param2) {
        ShowGPS fragment = new ShowGPS();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        context = getActivity().getApplicationContext();
    }


    // 獲得地理位置的更新資料 (GPS 與 NETWORK都註冊) 搭配ActivityCompat.requestPermissions
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d("lepu in service", "permission granted on request");
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("lepu in service", "permission granted on request");
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request


        }
    }

    @Override
    public void onResume() {

        //要求權限
        int permission = ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 無權限，向使用者請求
            Log.d("lepu in service", "ask permission");
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }else{
            //已有權限，執行儲存程式
            Log.d("lepu in service", "have permission");
            if (mLocationManager == null) {
                mLocationManager =
                        (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                mLocationListener = new MyLocationListener();
                Log.d("lepu in service", "have permission new locatio manager");
            }

            mLocationManager.requestLocationUpdates(LM_GPS, 0, 0, mLocationListener);
            mLocationManager.requestLocationUpdates(LM_NETWORK, 0, 0, mLocationListener);

        }

        if(sensor_manager == null){
            sensor_manager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            // 方向偵測器
            aSensor = sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mfSensor = sensor_manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
        sensor_manager.registerListener(listener, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensor_manager.registerListener(listener, mfSensor, SensorManager.SENSOR_DELAY_NORMAL);

        super.onResume();
    }

    @Override
    public void onPause() {
        if (mLocationManager != null) {
            // 移除 mLocationListener 監聽器
            mLocationManager.removeUpdates(mLocationListener);
            mLocationManager = null;
        }
        if(sensor_manager != null){
            sensor_manager.unregisterListener(listener);
        }
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View myInflatedView = inflater.inflate(R.layout.fragment_show_g, container,false);
        mLocationManager =
                (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new MyLocationListener();
        textView1 = (TextView) myInflatedView.findViewById(R.id.textView1);
        textView1.setText("haven't got GPS");

        textView2 = (TextView) myInflatedView.findViewById(R.id.textView2);

        button = (Button) myInflatedView.findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button1OnClick();
            }
        });

        openGPS(context);
        return myInflatedView;
    }

    // TODO: Rename method, update argument and hook method into UI event
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
//
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

    private class MySensorEventListener implements SensorEventListener {

        private float[] accelerometerValues;
        private float[] magneticFieldValues;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = event.values.clone();
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticFieldValues = event.values.clone();
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
            float degress = values[0];

            textView2.setText("Degrees:" + degress);
            if (values[0] >= -5 && values[0] < 5) {
                textView2.setText("Degrees:" + degress + "\n正北");
            } else if (values[0] >= 40 && values[0] < 50) {
                textView2.setText("Degress:" + degress + "\n東北");
            } else if (values[0] >= 85 && values[0] <= 95) {
                textView2.setText("Degress:" + degress + "\n正東");
            } else if (values[0] >= 130 && values[0] < 140) {
                textView2.setText("Degress:" + degress + "\n東南");
            } else if ((values[0] >= 175 && values[0] <= 180)
                    || (values[0]) >= -180 && values[0] < -175) {
                textView2.setText("Degress:" + degress + "\n正南");
            } else if (values[0] >= -140 && values[0] < -130) {
                textView2.setText("Degress:" + degress + "\n西南");
            } else if (values[0] >= -95 && values[0] < -85) {
                textView2.setText("Degress:" + degress + "\n正西");
            } else if (values[0] >= -50 && values[0] < -40) {
                textView2.setText("Degress:" + degress + "\n西北");
            }
        }

    }


    // 定位監聽器實作
    private class MyLocationListener implements LocationListener {
        // GPS位置資訊已更新
        public void onLocationChanged(Location location) {
            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

            textView1.setText("Location-GPS" + "\n" +
                    "緯度-Latitude：" + location.getLatitude() + "\n" +
                    "經度-Longitude：" + location.getLongitude() + "\n" +
                    "精確度-Accuracy：" + location.getAccuracy() + "\n" +
                    "標高-Altitude：" + location.getAltitude() + "\n" +
                    "時間-Time：" + new Date(location.getTime()) + "\n" +
                    "速度-Speed：" + location.getSpeed()*3.6 + "km/hr" + "\n" +
                    "方位-Bearing：" + location.getBearing());
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

    public void openGPS(Context context) {
        boolean gps = mLocationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = mLocationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Toast.makeText(context, "GPS : " + gps + ", Network : " + network,
                Toast.LENGTH_SHORT).show();
        if (gps || network) {
            return;
        } else {
            // 開啟手動GPS設定畫面
            Intent gpsOptionsIntent = new Intent(
                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(gpsOptionsIntent);
        }
    }

    public void button1OnClick() {
        openGPS(context);
    }
}
