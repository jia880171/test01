package com.example.unick.ubiinspector.models;

/**
 * Created by unick on 2018/1/19.
 */

public class User {
    public String username;
    public String birthday;
    public String personalID;
    public String email;
    public String carID;
    public String carID_2;
    public String phoneNumber;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String birthday, String personalID, String email, String carID, String carID_2, String phoneNumber) {
        this.username = username;
        this.birthday = birthday;
        this.personalID = personalID;
        this.email = email;
        this.carID = carID;
        this.carID_2 = carID_2;
        this.phoneNumber = phoneNumber;
    }
}
