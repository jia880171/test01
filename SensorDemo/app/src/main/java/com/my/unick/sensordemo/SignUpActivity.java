package com.my.unick.sensordemo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.my.unick.sensordemo.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    private String userUID;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private String name;
    private String birthday;
    private String email;
    private String password;
    private String personalID;
    private String carID;
    private String carID_2;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onResume() {
        Log.d("life cycle","onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d("life cycle","onPause");
        super.onPause();
    }

    public void createUser(View v){
        name = ((EditText)findViewById(R.id.textView_name)).getText().toString();
        birthday = ((EditText)findViewById(R.id.editText_year)).getText().toString()+"/"+((EditText)findViewById(R.id.editText_month)).getText().toString()+"/"+((EditText)findViewById(R.id.editText_day)).getText().toString();;;
        email = ((EditText)findViewById(R.id.textView_email)).getText().toString();
        password = ((EditText)findViewById(R.id.textView_password)).getText().toString();
        personalID = ((EditText)findViewById(R.id.textView_personalID)).getText().toString();
        carID = ((EditText)findViewById(R.id.carID)).getText().toString();
        carID_2 = ((EditText)findViewById(R.id.carID_2)).getText().toString();
        phoneNumber = ((EditText)findViewById(R.id.phoneNumber)).getText().toString();

        if(((EditText)findViewById(R.id.editText_year)).getText().toString().matches("")||((EditText)findViewById(R.id.editText_month)).getText().toString().matches("")||((EditText)findViewById(R.id.editText_day)).getText().toString().matches("")){
            new AlertDialog.Builder(SignUpActivity.this)
                    .setTitle("生日不可為空！")
                    .setPositiveButton("確認", null)
                    .show();
            return;
        } else if(name.matches("")){
            new AlertDialog.Builder(SignUpActivity.this)
                    .setTitle("姓名不可為空！")
                    .setPositiveButton("確認", null)
                    .show();
            return;
        } else if(isValidTWPID(personalID)==false){
            new AlertDialog.Builder(SignUpActivity.this)
                    .setTitle("身分證字號格式錯誤！！！")
                    .setPositiveButton("確認", null)
                    .show();
            return;//執行權還給呼叫方不繼續往下執行
        } else if(isValidMSISDN(phoneNumber)==false){
            new AlertDialog.Builder(SignUpActivity.this)
                    .setTitle("手機格式錯誤！！！")
                    .setPositiveButton("確認", null)
                    .show();
            return;//執行權還給呼叫方不繼續往下執行
        } else if(isValidEmail(email)==false){
            new AlertDialog.Builder(SignUpActivity.this)
                    .setTitle("email格式錯誤！！！")
                    .setPositiveButton("確認", null)
                    .show();
            return;
        } else if (email.matches("") || password.matches("")) {
            Log.d("in SignUpActivity","email: "+email+" password: "+password);
            new AlertDialog.Builder(SignUpActivity.this)
                    .setTitle("帳號密碼不可為空！")
                    .setPositiveButton("確認", null)
                    .show();
            return;
        }else if(password.length()<6){
            new AlertDialog.Builder(SignUpActivity.this)
                    .setTitle("密碼長度不可小於6")
                    .setPositiveButton("確認", null)
                    .show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                String message = task.isSuccessful() ? "註冊成功" : "註冊失敗";
                                if(task.isSuccessful()){
                                    Log.d("in SignUpActivity","sign up success");

                                    userUID = task.getResult().getUser().getUid();
                                    Log.d("in SignUpActivity","UserId: "+userUID);
                                    String email = task.getResult().getUser().getEmail();
                                    //String name = usernameFromEmail(email);
                                    writeNewUser(userUID);
                                    Intent intent = new Intent();
                                    intent.setClass(SignUpActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }else{
                                    String userCreateFailedMessage=" ";
                                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                        userCreateFailedMessage="該信箱已被使用";
                                    }
                                    Log.d("failed to create:", "reason: "+task.getException() );
                                    new AlertDialog.Builder(SignUpActivity.this)
                                            .setTitle(message)
                                            .setMessage(userCreateFailedMessage)
                                            .setPositiveButton("確認", null)
                                            .show();
                                    return;//執行權還給呼叫方不繼續往下執行
                                }
                            }
                        });
    }

    public static boolean isValidEmail(String email) {
        boolean result = false;
        Pattern EMAIL_PATTERN = Pattern
                .compile("^\\w+\\.*\\w+@(\\w+\\.){1,5}[a-zA-Z]{2,3}$");
        if (EMAIL_PATTERN.matcher(email).matches()) {
            result = true;
        }
        return result;
    }

    public static boolean isValidMSISDN(String msisdn) {
        boolean result = false;
        Pattern MSISDN_PATTERN = Pattern
                .compile("[+-]?\\d{10,12}");
        if (MSISDN_PATTERN.matcher(msisdn).matches()) {
            result = true;
        }
        return result;
    }

    public static boolean isvalidCarID(String carID){
        boolean result = false;
        Pattern CAR_PATTERN = Pattern.compile("^[A-Za-z]{1}[A-Za-z_0-9]{5}$");
        if (CAR_PATTERN.matcher(carID).matches()){
            result = true;
        }
        return result;
    }


    /**
     * 身分證字號檢查程式，身分證字號規則：
     * 字母(ABCDEFGHJKLMNPQRSTUVXYWZIO)對應一組數(10~35)，
     * 令其十位數為X1，個位數為X2；( 如Ａ：X1=1 , X2=0 )；D表示2~9數字
     * Y = X1 + 9*X2 + 8*D1 + 7*D2 + 6*D3 + 5*D4 + 4*D5 + 3*D6 + 2*D7+ 1*D8 + D9
     * 如Y能被10整除，則表示該身分證號碼為正確，否則為錯誤。
     * 臺北市(A)、臺中市(B)、基隆市(C)、臺南市(D)、高雄市(E)、臺北縣(F)、
     * 宜蘭縣(G)、桃園縣(H)、嘉義市(I)、新竹縣(J)、苗栗縣(K)、臺中縣(L)、
     * 南投縣(M)、彰化縣(N)、新竹市(O)、雲林縣(P)、嘉義縣(Q)、臺南縣(R)、
     * 高雄縣(S)、屏東縣(T)、花蓮縣(U)、臺東縣(V)、金門縣(W)、澎湖縣(X)、
     * 陽明山(Y)、連江縣(Z)
     * @since 2006/07/19
     */
    public static boolean isValidTWPID(String twpid) {
        boolean result = false;
        Pattern TWPID_PATTERN = Pattern
                .compile("[ABCDEFGHJKLMNPQRSTUVXYWZIO][12]\\d{8}");
        String pattern = "ABCDEFGHJKLMNPQRSTUVXYWZIO";
        if (TWPID_PATTERN.matcher(twpid.toUpperCase()).matches()) {
            int code = pattern.indexOf(twpid.toUpperCase().charAt(0)) + 10;
            int sum = 0;
            sum = (int) (code / 10) + 9 * (code % 10) + 8 * (twpid.charAt(1) - '0')
                    + 7 * (twpid.charAt(2) - '0') + 6 * (twpid.charAt(3) - '0')
                    + 5 * (twpid.charAt(4) - '0') + 4 * (twpid.charAt(5) - '0')
                    + 3 * (twpid.charAt(6) - '0') + 2 * (twpid.charAt(7) - '0')
                    + 1 * (twpid.charAt(8) - '0') + (twpid.charAt(9) - '0');
            if ( (sum % 10) == 0) {
                result = true;
            }
        }
        return result;
    }

    private String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

    private void writeNewUser(String userId) {
        User user = new User(name, birthday, personalID, email, carID, carID_2, phoneNumber);
        mDatabase.child("users").child(userId).setValue(user);
    }
}
