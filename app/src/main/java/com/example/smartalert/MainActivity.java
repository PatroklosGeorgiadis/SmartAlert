package com.example.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements LocationListener {
    EditText email, password;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference reference;


    String user;
    LocationManager locationManager;
    DatabaseReference myRef;
    Boolean b = false;
    SharedPreferences sharedPreferences;
    Double gps_long2, gps_lat2;

    float[] distance = new float[1];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        email = findViewById(R.id.editTextTextPersonName2);
        password = findViewById(R.id.editTextTextPersonName3);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
           // ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},123);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

    }
        public void signin (View view){
            mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener((task) -> {
                        if (task.isSuccessful()) {
                            reference.child("Users").child(mAuth.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if (!task.isSuccessful()) {
                                        showMessage(getString(R.string.error_title), Objects.requireNonNull(task.getException()).getLocalizedMessage());
                                    } else {
                                        if (String.valueOf(task.getResult().getValue()).equals("Employee")) {
                                            Intent intent = new Intent(MainActivity.this, EmployeeActivity.class);
                                            startActivity(intent);
                                        } else {
                                            Intent intent = new Intent(MainActivity.this, UserActivity.class);
                                            intent.putExtra("ID", mAuth.getUid());
                                            startActivity(intent);
                                        }
                                    }
                                }
                            });
                        } else {
                            showMessage(getString(R.string.error_title), Objects.requireNonNull(task.getException()).getLocalizedMessage());
                        }
                    });
        }
        public void go_sign_up (View view){
            Intent intent = new Intent(MainActivity.this, SignUp.class);
            startActivity(intent);
        }

        public void go_view_stats (View view){
            Intent intent = new Intent(MainActivity.this, Statistics.class);
            startActivity(intent);
        }

        void showMessage (String title, String message){
            new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
        }

    @Override
    public void onLocationChanged(@NonNull Location location) {
       checkdanger(location.getLongitude(),location.getLatitude());
       // checkdanger(location);
    }

    public void checkdanger(@NonNull Double x, Double y){
        Date time = Calendar.getInstance().getTime();
        Double gps_long1 = x;
        Double gps_lat1 = y;
       // Double gps_long1 = location.getLongitude();
       // Double gps_lat1 = location.getLatitude();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref2 = database.getReference().child("Emergencies");
        ref2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                  // if(time.getTime()-dataSnapshot.child("TimeStamp").getTime<86400*1000){}
                 //  gps_long2 = (Double) dataSnapshot.child("Location").getValue();
                 //  gps_lat2 = (Double) dataSnapshot.child("Location").getValue();
                   Double[] lat_long2 = string2latlong(dataSnapshot.child("Location").getValue().toString());
                   gps_long2 = lat_long2[0];
                   gps_lat2 = lat_long2[1];
                   Location.distanceBetween(gps_lat1,gps_long1,gps_lat2,gps_long2,distance);
                   if(distance[0] < 100000.0){
                      // showMessage("hey","hi");
                       Notification(dataSnapshot.child("Emergency").getValue().toString(),dataSnapshot.child("Details").getValue().toString(),gps_long2,gps_lat2,3);
                   }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //onDestroy();
    }

    public Double[] string2latlong(String s){
        String[] arrOfStr = s.split(",", 2);
        Double[] arrOfDouble = new Double[2];
        arrOfDouble[0] = Double.parseDouble(arrOfStr[0]);
        arrOfDouble[1] = Double.parseDouble(arrOfStr[1]);
        return arrOfDouble;
    }


    protected void Notification(String title,String description,Double long1,Double long2,int i) {


        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel("1245", "location2",
                        NotificationManager.IMPORTANCE_DEFAULT);
        }
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), "1245");
        builder.setContentTitle(title)
                .setSmallIcon(R.drawable.ic_launcher_background)
                //  .setContentText(description+"\n Περιορίστε δραστικά τις μετακινήσεις και ακολουθήστε τις οδηγίες των αρχών.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(description +"\n" + "Μήνυμα για " + title + " στην περιοχή:" + long1 + long2 + "\n" + "Περιορίστε δραστικά τις μετακινήσεις και ακολουθήστε τις οδηγίες των αρχών.Οδηγίες αυτοπροστασίας:https://www.civilprotection.gr/el/entona-kairika-fainomena"))
                .setAutoCancel(true);
        notificationManager.notify(i, builder.build());
    }




}

