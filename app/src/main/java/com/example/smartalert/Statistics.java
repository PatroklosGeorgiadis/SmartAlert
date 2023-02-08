package com.example.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Statistics extends AppCompatActivity {

    FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);


        TextView nameView1 = this.findViewById(R.id.textView10);
        TextView nameView2 = this.findViewById(R.id.textView11);
        TextView nameView3 = this.findViewById(R.id.textView12);
        TextView nameView4 = this.findViewById(R.id.textView14);
        TextView nameView5 = this.findViewById(R.id.textView15);

        database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String eq = snapshot.child("ReportsData").child("Earthquake").getValue().toString();
                String fire = snapshot.child("ReportsData").child("Fire").getValue().toString();
                String flood = snapshot.child("ReportsData").child("Flood").getValue().toString();
                String tornado = snapshot.child("ReportsData").child("Tornado").getValue().toString();
                int total = Integer.parseInt(eq) + Integer.parseInt(fire) + Integer.parseInt(flood)
                        + Integer.parseInt(tornado);
                nameView1.setText(String.valueOf(total));
                nameView2.setText(eq);
                nameView3.setText(fire);
                nameView4.setText(flood);
                nameView5.setText(tornado);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


}