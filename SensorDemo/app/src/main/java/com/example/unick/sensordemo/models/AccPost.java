package com.example.unick.sensordemo.models;

import com.google.firebase.database.Exclude;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by unick on 2017/8/17.
 */

public class AccPost {
    public String uid;
    public JSONArray jsonArray=null;
    public String body=null;
//    public int starCount = 0;
//    public Map<String, Boolean> stars = new HashMap<>();

    public AccPost() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public AccPost(String uid, String body) {
        this.uid = uid;
        this.body = body;
    }

    public AccPost(String uid, JSONArray jsonArray) {
        this.uid = uid;
        this.jsonArray = jsonArray;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        if(jsonArray==null){
            result.put("body", body);
        }else{
            result.put("jsonArray", jsonArray);
        }
        return result;
    }
}
