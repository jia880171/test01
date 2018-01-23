package com.example.unick.ubiinspector;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String userUID;
    private DatabaseReference mDatabase;
    private String UserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user!=null){
                    Log.d("onAuthStateChanged", "登入："+user.getUid());
                    userUID = user.getUid();
                    Intent intent = new Intent();
                    intent.putExtra("username",UserName);
                    LoginActivity.this.setResult(RESULT_OK, intent);
                    LoginActivity.this.finish();
                }
                else{
                    Log.d("onAuthStateChanged", "已登出");
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    public void logIn(final View v){
        String email = ((EditText)findViewById(R.id.email)).getText().toString();
        String password = ((EditText)findViewById(R.id.password)).getText().toString();

        if (email.matches("") || password.matches("")) {
            new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("帳號密碼不可為空！")
                    .setPositiveButton("確認", null)
                    .show();
            return;//執行權還給呼叫方不繼續往下執行
        }
        if(password.length()<6){
            new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("密碼長度不可小於6")
                    .setPositiveButton("確認", null)
                    .show();
            return;
        }

        final String femail = email;
        final String fpassword = password;

        Log.d("AUTH", email+"/"+password);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    Log.d("======onComplete", "登入失敗");
                    //register(femail, fpassword);
                    new AlertDialog.Builder(LoginActivity.this)
                            .setTitle("登入失敗！！ 請確認是否輸入正確 新用戶請點註冊")
                            .setPositiveButton("確認", null)
                            .show();
                }
                Log.d("======onComplete", "登入成功");
            }
        });

    }
    public void goToSignUp(View v){
        Intent intent = new Intent();
        intent.setClass(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
