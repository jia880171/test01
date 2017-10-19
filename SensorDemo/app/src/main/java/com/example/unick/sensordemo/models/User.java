package com.example.unick.sensordemo.models;

/**
 * Created by unick on 2017/8/17.
 */

public class User {
    public String username;
    public String email;
    public String carID;
    public String phoneNumber;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email,String carID, String phoneNumber) {
        this.username = username;
        this.email = email;
        this.carID = carID;
        this.phoneNumber = phoneNumber;
    }
}
