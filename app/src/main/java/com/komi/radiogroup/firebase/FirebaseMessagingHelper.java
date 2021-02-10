package com.komi.radiogroup.firebase;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FirebaseMessagingHelper {

    final String API_TOKEN_KEY = "AAAAMJ5RH1k:APA91bG5hD4dwWDrFFdK6QUYLmm_sLW1VvfHzwh-wwZGRar93y8ZTcyUAVU_O3pGEeKWqWe4FGgUe0Rs1VD5Vym6mQ9LnHUXhv6K5K1vlMwhCLkrpMIW0P0_6gD7ZLH5DA4u8jhNmkjz";

    private static FirebaseMessagingHelper instance;
    private static FirebaseMessaging firebaseMessaging;
    private static Context context;

    public static FirebaseMessagingHelper getInstance(Context gcontext) {
        context = gcontext;
        if (instance == null)
            instance = new FirebaseMessagingHelper();
        return instance;
    }

    public FirebaseMessagingHelper() {
        firebaseMessaging = FirebaseMessaging.getInstance();
    }

    public void sendMessageToTopic(String topic, String message) {

        String URL = "https://fcm.googleapis.com/fcm/send";

        final JSONObject rootObject = new JSONObject();

        try {
            rootObject.put("to", "/topics/" + topic);
        }catch (JSONException exception){
            exception.printStackTrace();
        }

        try {
            rootObject.put("data", new JSONObject().put("message", message));
        } catch (JSONException exception) {
            exception.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest request = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorization","key=" + API_TOKEN_KEY);
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return rootObject.toString().getBytes();
            }
        };
        queue.add(request);
        queue.start();
    }

    public void subscribeToTopic(String topicName) {
        firebaseMessaging.subscribeToTopic(topicName);
    }

    public void unsubscribeFromTopic(String topicName) {
        firebaseMessaging.unsubscribeFromTopic(topicName);
    }

}
