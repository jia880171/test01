package com.my.unick.sensordemo;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class localDataActivity extends AppCompatActivity {
    private Context context;
    private TextView textView1;
    private String fName = "textFile.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_data);
        context = this;
        textView1 = (TextView) findViewById(R.id.textView1);
        textView1.setMovementMethod(ScrollingMovementMethod.getInstance());
    }
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button1:
                createFile("textFile.txt");
                break;
            case R.id.button2:
                //createCacheFile("cachefile.txt");
                readFile(new File(context.getFilesDir().getAbsolutePath(), fName));
                Log.d("localDataActivity","Reading");
                break;
            case R.id.button3:
                //createCacheFile("cachefile.txt");
                deletFile(new File(context.getFilesDir().getAbsolutePath(), fName));
                break;
        }
    }

    // 建立私有文擋
    private void createFile(String fName) {
        try {
            // 建立應用程式私有文件
            FileOutputStream fOut = openFileOutput(fName, MODE_APPEND);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);
            // 寫入資料
            String test="[{\"time\":1521368527739,\"latLng\":\"lat\\/lng: (24.96609268,121.19062186)\",\"speed\":29.448001098632812,\"degree\":-172.8938446044922,\"type\":0},{\"time\":1521368528243,\"latLng\":\"lat\\/lng: (24.96609268,121.19062186)\",\"speed\":29.448001098632812,\"degree\":-165.43907165527344,\"type\":0}][{\"time\":1521368527739,\"latLng\":\"lat\\/lng: (24.96609268,121.19062186)\",\"speed\":29.448001098632812,\"degree\":-172.8938446044922,\"type\":0},{\"time\":1521368528243,\"latLng\":\"lat\\/lng: (24.96609268,121.19062186)\",\"speed\":29.448001098632812,\"degree\":-165.43907165527344,\"type\":0}][{\"time\":1521368527739,\"latLng\":\"lat\\/lng: (24.96609268,121.19062186)\",\"speed\":29.448001098632812,\"degree\":-172.8938446044922,\"type\":0},{\"time\":1521368528243,\"latLng\":\"lat\\/lng: (24.96609268,121.19062186)\",\"speed\":29.448001098632812,\"degree\":-165.43907165527344,\"type\":0}]";
            osw.write(test);
            osw.close();
            Toast.makeText(context, "File saved successfully!",
                    Toast.LENGTH_SHORT).show();
            // 讀取文擋資料
//            readFile(new File(context.getFilesDir().getAbsolutePath(),
//                    fName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // 讀取文擋資料
    private void readFile(File file) {
        if(!file.exists()){
            textView1.setText("無資料");
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
                textView1.setText(file.getAbsolutePath() + "\n\n" +
                        sb.toString());
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

}
