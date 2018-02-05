package com.my.unick.sensordemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by unick on 2018/2/3.
 */

public class PowerManagerReceiver extends BootBroadcastReceiver{
    private boolean isCharging;

    @Override
    public void onReceive(Context context, Intent intent) {

        //dynamic register battery manager
        Intent i = new ContextWrapper(context).registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int battery = (int) (level / (float) scale * 100);

        //network
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();


        Log.d("charging test","onReceive");
        int status = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL);

        Log.d("charging test","status: " + status);
        Log.d("charging test","isCharging: " + isCharging);
        if(status == BatteryManager.BATTERY_STATUS_CHARGING){
            Notification(context, "charging"+ " ,net: " +isConnected);
            Log.d("charging test","charging!" + " ,net: " +isConnected);
        }else if(status == BatteryManager.BATTERY_STATUS_FULL){
            Notification(context, "full, is charging: "+ isCharging+ " ,net: " +isConnected);
            Log.d("charging test","full!"+ " ,net: " +isConnected);
        }else {
            Notification(context, "status: " + status + " level: " + level + " battery: " + battery+ " ,net: " +isConnected);
            Log.d("charging test","else!");
        }

        if((isCharging==true)&&(isConnected==true)){
            Log.d("charging test","charging or full"+ " ,net: " +isConnected);
            context.startService(new Intent(context, uploadChargingService.class));
        }
    }
    public void Notification(Context context, String message) {
        // Set Notification Title
        String strtitle = context.getString(R.string.broadcastReceiverTitle);
        // Open NotificationView Class on Notification Click
        Intent intent = new Intent(context, MainActivity.class);
        // Send data to NotificationView Class
        intent.putExtra("title", strtitle);
        intent.putExtra("text", message);
        // Open NotificationView.java Activity
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Create Notification using NotificationCompat.Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context)
                // Set Icon
                .setSmallIcon(R.drawable.cast_ic_notification_small_icon)
                // Set Ticker Message
                .setTicker(message)
                // Set Title
                .setContentTitle(context.getString(R.string.broadcastReceiverTitle))
                // Set Text
                .setContentText(message)
                // Set PendingIntent into Notification
                .setContentIntent(pIntent)
                // Dismiss Notification
                .setAutoCancel(true);

        // Create Notification Manager
        NotificationManager notificationmanager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        // Build Notification with Notification Manager
        notificationmanager.notify(0, builder.build());

    }
}
