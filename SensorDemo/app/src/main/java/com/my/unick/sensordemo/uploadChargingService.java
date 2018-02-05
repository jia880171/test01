package com.my.unick.sensordemo;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.my.unick.sensordemo.models.AccPost;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by unick on 2018/2/3.
 */

public class uploadChargingService extends Service {

    private DatabaseReference mDatabase;
    private Context context;
    private String fName = "textFile.txt";
    private Notification.Builder myNotificationBuilder;
    private Notification notification;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("charging test","uploadChargingService onStartCommand");

        myNotificationBuilder = new Notification.Builder (this.getApplicationContext()); //获取一个Notification构造器
        Intent nfIntent = new Intent(this, MainActivity.class);

        myNotificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0))
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.cast_ic_notification_small_icon))
                .setContentTitle("UBI APP")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("感測到充電且連網，上傳資料")
                .setWhen(System.currentTimeMillis());

        notification = myNotificationBuilder.build();
        notification.defaults = Notification.DEFAULT_SOUND;
        startForeground(120, notification);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        context = this;
        readFile(new File(context.getFilesDir().getAbsolutePath(), fName));
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void readFile(File file) {
        if(!file.exists()){
            //textView1.setText("無資料");
        }
        char[] buffer = new char[1];
        FileReader fr = null;
        StringBuilder sb = new StringBuilder();
        try {
            fr = new FileReader(file);
            while (fr.read(buffer)!= -1) {
                sb.append(new String(buffer));
            }
            if(file.exists()){
                //textView1.setText(file.getAbsolutePath() + "\n\n" + sb.toString());
                writeToFirebase(sb.toString());
            }
        }
        catch (IOException e) { }
        finally {
            try {
                if(fr!=null) {
                    fr.close(); // 關閉檔案
                }
            }
            catch (IOException e) { }
        }
    }

    private void deletFile(File file){
        if (file.exists()){
            file.delete();
            Toast.makeText(getBaseContext(),
                    "File deleted",
                    Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getBaseContext(),
                    "File doesn't exist",
                    Toast.LENGTH_SHORT).show();
        }
    }
    private void writeToFirebase(String body){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String key = mDatabase.child("data2/" + sdf.format(new Date()) + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid()).push().getKey();

        Log.d("uploadService","write acc key:"+key);
        AccPost post = new AccPost("01", body);
        //01 need to be change to UID
        Map<String, Object> postValues = post.toMap();
        Map<String, Object> childUpdates = new HashMap<>();


        childUpdates.put("data2/" + sdf.format(new Date()) + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + key,postValues);
        mDatabase.updateChildren(childUpdates);
        deletFile(new File(context.getFilesDir().getAbsolutePath(), fName));
    }
}
