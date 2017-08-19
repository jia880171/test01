package com.example.unick.sensordemo.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by unick on 2017/8/17.
 */

public class AccPost {
    public String uid;
    public String body;
    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();

    public AccPost() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public AccPost(String uid, String body) {
        this.uid = uid;
        this.body = body;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("body", body);
        result.put("starCount", starCount);
        result.put("stars", stars);

        return result;
    }
}
