package com.example.smartalert;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class FirebaseCloudMessagingNotificationsService extends FirebaseMessagingService implements LocationListener {

    String location_data;
    LocationManager locationManager;
    private int locationRequestCode;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            System.out.println("Message data payload: " + remoteMessage.getData());
        }

        String[] array = remoteMessage.getNotification().getBody().split("at: \n ");
        //sendNotification(remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody());



        locationFinder();
        Double[] lat_long2 = string2latlong(array[1]);
        Double[] lat_long1 = string2latlong(location_data);
        double x, y, distance;
        int Radius = 6371;
        x = (lat_long2[1]-lat_long1[1]) * Math.cos((lat_long1[0]+lat_long2[0])/2);
        y = (lat_long2[0]-lat_long1[0]);
        distance = Math.sqrt(x*x + y*y) * Radius;
        if (distance < 3){
            sendNotification(remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody());

        }


    }

    private void sendNotification(String messageTitle,String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        String channelId = "fcm_default_channel";
        //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle(messageTitle)
                        .setAutoCancel(true)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(messageBody));
                        //.setSound(defaultSoundUri)

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        location_data = location.getLatitude() + "," + location.getLongitude();
    }
    public void locationFinder() {
        locationRequestCode = 123;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        location_data = loc.getLatitude()+","+loc.getLongitude();
    }

    public Double[] string2latlong(String s){
        String[] arrOfStr = s.split(",", 2);
        Double[] arrOfDouble = new Double[2];
        arrOfDouble[0] = Double.parseDouble(arrOfStr[0]);
        arrOfDouble[1] = Double.parseDouble(arrOfStr[1]);
        return arrOfDouble;
    }

}