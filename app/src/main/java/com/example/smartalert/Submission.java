package com.example.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class Submission extends AppCompatActivity implements LocationListener {
    String ID,emergency,location_data,toEng;
    EditText details;
    FirebaseDatabase database;
    LocationManager locationManager;
    private int locationRequestCode, num;
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
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                toEng = categoryToEnglish(emergency);
                num = Integer.parseInt(snapshot.child("ReportsData").child(toEng).getValue().toString());
                num = num + 1;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        reference.get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()) {
                            showMessage(getString(R.string.success_title),getString(R.string.emergency_reported) );
                            reference.child("Emergencies").child(ID).child("Emergency").setValue(emergency);
                            reference.child("Emergencies").child(ID).child("Details").setValue(details.getText().toString());
                            reference.child("Emergencies").child(ID).child("Location").setValue(location_data);
                            reference.child("Emergencies").child(ID).child("TimeStamp").setValue(currentTime.toString());
                            reference.child("ReportsData").child(toEng).setValue(String.valueOf(num));
                        } else {
                            showMessage(getString(R.string.error_title), task.getException().getLocalizedMessage());
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

    public String categoryToEnglish(String category){
        if(Objects.equals(category, getString(R.string.earthquakes))||
                Objects.equals(category, getStringByLocal(this,R.string.earthquakes,"el"))){
            category = "Earthquake";
        }
        else if(Objects.equals(category, getString(R.string.fire))||
                Objects.equals(category, getStringByLocal(this,R.string.fire,"el"))){
            category = "Fire";
        }
        else if(Objects.equals(category, getString(R.string.flood))||
                Objects.equals(category, getStringByLocal(this,R.string.flood,"el"))){
            category =  "Flood";
        }
        else if(Objects.equals(category, getString(R.string.tornado))||
                Objects.equals(category, getStringByLocal(this,R.string.tornado,"el"))){
            category = "Tornado";
        }
        return category;
    }

    public String getStringByLocal(Activity context, int id, String locale) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(new Locale(locale));
        return context.createConfigurationContext(configuration).getResources().getString(id);
    }
}