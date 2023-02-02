package com.example.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;

public class Submission extends AppCompatActivity implements LocationListener {
    String ID,emergency,location_data;
    EditText details;
    FirebaseDatabase database;
    LocationManager locationManager;
    private int locationRequestCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission);

        database = FirebaseDatabase.getInstance();
        Intent prev_intent = getIntent();
        ID = prev_intent.getStringExtra("ID");
        emergency = prev_intent.getStringExtra("Emergency");
        details = findViewById(R.id.DetailsTab);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},locationRequestCode);
        }
    }

    public void submit(View view){
        DatabaseReference reference = database.getReference();
        locationFinder();
        Date currentTime = Calendar.getInstance().getTime();
        reference.get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()) {
                            showMessage("Success!", "Emergency has been reported");
                            reference.child("Emergencies").child(ID).child("Emergency").setValue(emergency);
                            reference.child("Emergencies").child(ID).child("Details").setValue(details.getText().toString());
                            reference.child("Emergencies").child(ID).child("Location").setValue(location_data);
                            reference.child("Emergencies").child(ID).child("TimeStamp").setValue(currentTime.toString());
                        } else {
                            showMessage("Error", task.getException().getLocalizedMessage());
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==123){
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void locationFinder() {
        locationRequestCode = 123;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},locationRequestCode);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        location_data = loc.getLatitude()+","+loc.getLongitude();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        location_data = location.getLatitude()+","+location.getLongitude();
    }

    void showMessage(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                }).show();
    }
}