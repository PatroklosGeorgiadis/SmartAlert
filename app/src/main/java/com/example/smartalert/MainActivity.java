package com.example.smartalert;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

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

    public void signin(View view){
        mAuth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                .addOnCompleteListener((task)->{
                    if(task.isSuccessful()){
                        showMessage("Success!","Ok");
                    }else {
                        showMessage("Error", Objects.requireNonNull(task.getException()).getLocalizedMessage());
                    }
                });
    }
    public void go_sign_up(View view){
        Intent intent = new Intent(MainActivity.this, SignUp.class);
        startActivity(intent);
    }
    void showMessage(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }
}