package com.example.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class UserActivity extends AppCompatActivity {
    String ID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Intent prev_intent = getIntent();
        ID = prev_intent.getStringExtra("ID");
    }

    public void go_submission(View view) {
        Button btn = (Button) view;
        String emergency = btn.getText().toString();
        Intent intent = new Intent(UserActivity.this, Submission.class);
        intent.putExtra("ID", ID);
        intent.putExtra("Emergency", emergency);
        startActivity(intent);
    }
}