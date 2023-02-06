package com.example.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class EmployeeActivity extends AppCompatActivity {
    TextView t;
    Iterable<DataSnapshot> dataSnapshot;
    List<String> checked = new ArrayList<>();
    long timeC,timeD;
    @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
    FirebaseDatabase database;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee);

        t = findViewById(R.id.testView);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        reference.child("Emergencies").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               //count = String.valueOf(snapshot.getChildrenCount());
               //t.setText(count);
               dataSnapshot = snapshot.getChildren();
               Iterator<DataSnapshot> iterator = dataSnapshot.iterator();
               while(iterator.hasNext()){
                   DataSnapshot d = (DataSnapshot) iterator.next();
                   //Checking for "duplicates"
                   if(checked.contains(d.getKey())){
                       break;
                   }
                   String dtd = d.child("TimeStamp").getValue().toString();
                   try {
                       //Getting first timestamp
                       Date dateD=formatter.parse(dtd);
                       assert dateD != null;
                       timeD = dateD.getTime();
                   } catch (ParseException e) {
                       throw new RuntimeException(e);
                   }
                   for(DataSnapshot c : snapshot.getChildren()){
                       String dtc = c.child("TimeStamp").getValue().toString();
                       try {
                           //Getting second timestamp
                           Date dateC=formatter.parse(dtc);
                           assert dateC != null;
                           timeC = dateC.getTime();
                       } catch (ParseException e) {
                           throw new RuntimeException(e);
                       }
                       int time = (int) (timeD - timeC);
                       /*Checking if: a) these reports are from different users or not
                       * b) if they are the same type of emergency
                       * c) if they've been reported in a time period of 30 minutes*/
                       if(!d.getKey().equals(c.getKey())
                               &&d.child("Emergency").getValue().toString().equals(c.child("Emergency").getValue().toString())
                               &&Math.abs(time)<1800000){
                           t.setText(String.valueOf(time));
                           //Removing "duplicates"
                           checked.add(c.getKey());
                       }
                   }
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}