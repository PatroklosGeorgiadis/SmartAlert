package com.example.smartalert;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity{
    EditText email, password;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{POST_NOTIFICATIONS},101);
            }
        }

        email = findViewById(R.id.editTextTextPersonName2);
        password = findViewById(R.id.editTextTextPersonName3);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
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

}

