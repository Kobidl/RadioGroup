package com.komi.radiogroup.firebase;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessaging;
import com.komi.radiogroup.interfaces.FirebaseMessagingHelperInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FirebaseMessagingHelper implements FirebaseMessagingHelperInterface {

    final String API_TOKEN_KEY = API_KEY;

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

    public void sendMessageToTopic(String fileUrl,String userId,String topic) {

        final JSONObject rootObject = new JSONObject();
        try {
            rootObject.put("to", "/topics/" + topic);
            rootObject.put("data", new JSONObject().put("message", fileUrl).put("sender_id", userId));
            String url = "https://fcm.googleapis.com/fcm/send";
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i("testrecording","error when sending recording "+error.toString());
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "key=" + API_TOKEN_KEY);
                    return headers;
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    return rootObject.toString().getBytes();
                }
            };
            queue.add(request);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void subscribeToTopic(String topicName) {
        firebaseMessaging.subscribeToTopic(topicName);
    }

    public void unsubscribeFromTopic(String topicName) {
        firebaseMessaging.unsubscribeFromTopic(topicName);
    }

}
