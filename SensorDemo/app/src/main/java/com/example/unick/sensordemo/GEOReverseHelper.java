package com.example.unick.sensordemo;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Created by unick on 2017/8/8.
 */

public class GEOReverseHelper {
    public static String DOMAIN =
            "http://maps.googleapis.com/maps/api/geocode/json";
    public static String getAddressByLatLng(LatLng latLng){
        String addr = "";
        LatLngToAddress ga = new LatLngToAddress(latLng);
        FutureTask<String> future = new FutureTask<String>(ga);
        new Thread(future).start();
        try{
            addr = future.get();
        }catch (Exception e){

        }
        return addr;
    }
    private static class HttpUtil {
        public static String get(String urlString) throws Exception {
            InputStream is = null;
            Reader reader = null;
            StringBuilder str = new StringBuilder();
            URL url = new URL(urlString);
            URLConnection URLConn = url.openConnection();
            URLConn.setRequestProperty("User-agent", "IE/6.0");
            is = URLConn.getInputStream();
            reader = new InputStreamReader(is, "UTF-8");
            char[] buffer = new char[1];
            while (reader.read(buffer) != -1) {
                str.append(new String(buffer));
            }
            return str.toString();
        }
    }
    // LatLng 轉 Address
    private static class LatLngToAddress implements Callable<String> {

        private String queryURLString = DOMAIN
                + "?latlng=%s,%s&sensor=true&language=zh_tw";
        private String address = null;
        private LatLng latLng;

        LatLngToAddress(LatLng latLng) {
            this.latLng = latLng;
        }

        @Override
        public String call() {
            // 輸入緯經度得到地址
            address = getAddressByLocation();
            return address;
        }

        private String getAddressByLocation() {
            String urlString = String.format(queryURLString, latLng.latitude,
                    latLng.longitude);
            try {
                // 取得 json string
                String jsonStr = HttpUtil.get(urlString);
                // 取得 json 根陣列節點 results
                JSONArray results = new JSONObject(jsonStr)
                        .getJSONArray("results");
                if (results.length() >= 1) {
                    // 取得 results[0]
                    JSONObject jsonObject = results.getJSONObject(0);
                    // 取得 formatted_address 屬性內容
                    address = jsonObject.optString("formatted_address");
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
            return address;
        }
    }
}
