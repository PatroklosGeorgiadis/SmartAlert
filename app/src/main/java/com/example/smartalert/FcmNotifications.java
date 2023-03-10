package com.example.smartalert;

import android.app.Activity;
import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FcmNotifications  {

    String userFcmToken;
    String title;
    String body;
    Context mContext;
    Activity mActivity;


    RequestQueue requestQueue;
    private final String postUrl = "https://fcm.googleapis.com/fcm/send";
    private final String fcmServerKey ="AAAA2AViGBY:APA91bGTZ8CbzjNjlGu_poiL7tIziCEUmDcXNhDmQtfpo0DaA0tl-yUVtEYhk4n4WPV9-_ksnLkxSbBqxXmcEEZoH2pgIW5Z18GSZ9GBeiAvnjTIYxKjQeGBcW04zYSj9fqCcOp5jDsM";

    public FcmNotifications(String userFcmToken, String title, String body, Context mContext, Activity mActivity) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
        this.mContext = mContext;
        this.mActivity = mActivity;
    }

    public void SendNotifications() {

        requestQueue = Volley.newRequestQueue(mActivity);
        JSONObject mainObj = new JSONObject();
        try {
            mainObj.put("to", userFcmToken);
            JSONObject notiObject = new JSONObject();
            notiObject.put("title", title);
            notiObject.put("body", body);
            notiObject.put("icon", R.mipmap.ic_launcher_round); // enter icon that exists in drawable only
            mainObj.put("notification", notiObject);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, mainObj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=" + fcmServerKey);
                    return header;
                }
            };
            requestQueue.add(request);


        } catch (JSONException e) {
            e.printStackTrace();
        }




    }
}
