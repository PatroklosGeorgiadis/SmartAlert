package com.example.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    EditText email,password,data;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        email= findViewById(R.id.editTextTextPersonName2);
        password=findViewById(R.id.editTextTextPersonName3);
        //data = findViewById(R.id.editTextTextPersonName3);
        mAuth = FirebaseAuth.getInstance();
    }
}