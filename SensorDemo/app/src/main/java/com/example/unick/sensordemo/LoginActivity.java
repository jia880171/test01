package com.example.unick.sensordemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.unick.sensordemo.models.User;
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

//    private void register(final String email, final String password){
//        new AlertDialog.Builder(LoginActivity.this)
//                .setTitle("登入問題")
//                .setMessage("無此帳號，是否要以此帳號與密碼註冊?")
//                .setPositiveButton("註冊",
//                        new DialogInterface.OnClickListener(){
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                createUser(email, password);
//                            }
//                        })
//                .setNeutralButton("取消", null)
//                .show();
//    }
//    private void createUser(String email, String password){
//        UserName = usernameFromEmail(email);
//        mAuth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(
//                        new OnCompleteListener<AuthResult>() {
//                            @Override
//                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                String message = task.isSuccessful() ? "註冊成功" : "註冊失敗";
//                                if(task.isSuccessful()){
//                                    userUID = task.getResult().getUser().getUid();
//                                    String email = task.getResult().getUser().getEmail();
//                                    String name = usernameFromEmail(email);
//                                    writeNewUser(userUID,name,email);
//                                }
//                            }
//                        });
//    }

//    private String usernameFromEmail(String email) {
//        if (email.contains("@")) {
//            return email.split("@")[0];
//        } else {
//            return email;
//        }
//    }

//    private void writeNewUser(String userId, String name, String email) {
//        User user = new User(name, email);
//
//        mDatabase.child("users").child(userId).setValue(user);
//    }

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
