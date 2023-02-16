package com.example.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class EmployeeActivity extends AppCompatActivity {
    LinearLayout layout;
    Iterable<DataSnapshot> dataSnapshot;
    FirebaseDatabase database;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee);

        layout = findViewById(R.id.container);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        reference.child("Emergencies").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot card : snapshot.getChildren()){
                    addCard(card.getKey(),card.child("Emergency").getValue().toString(),
                            card.child("Location").getValue().toString(),card.child("TimeStamp").getValue().toString(),
                            card.child("Details").getValue().toString(),String.valueOf(danger_level(snapshot,card)));

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public int danger_level(DataSnapshot snapshot, DataSnapshot d){
        int danger_level = 1;

        long timeC,timeD;
        SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy",Locale.ENGLISH);
        int Radius = 6371; // radius of the earth in km
        double x,y,distance;

        dataSnapshot = snapshot.getChildren();

        String dtd = d.child("TimeStamp").getValue().toString();
        try {
            //Getting first timestamp
            Date dateD=formatter.parse(dtd);
            assert dateD != null;
            timeD = dateD.getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        Double[] lat_long1 = string2latlong(d.child("Location").getValue().toString());

        //get category in english for comparison
        String en_for_d = categoryToEnglish(d.child("Emergency").getValue().toString());
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
            //calculating the time interval between the 2 reports
            int time = (int) (timeD - timeC);
            //calculating the proximity of the 2 reports
            Double[] lat_long2 = string2latlong(c.child("Location").getValue().toString());
            x = (lat_long2[1]-lat_long1[1]) * Math.cos((lat_long1[0]+lat_long2[0])/2);
            y = (lat_long2[0]-lat_long1[0]);
            distance = Math.sqrt(x*x + y*y) * Radius;  // distance in km

            //get category in english for comparison
            String en_for_c = categoryToEnglish(c.child("Emergency").getValue().toString());

            /*Checking if: a) these reports are from different users or not
             * b) they are the same type of emergency
             * c) they've been reported in a time period of 30 minutes
             * d) they have a proximity of 3 kilometers*/
            if(!d.getKey().equals(c.getKey())
                    &&en_for_d.equals(en_for_c)
                    &&Math.abs(time)<1800000
                    &&distance<3){
                danger_level = danger_level+1;
            }
        }
        return danger_level;
    }

    private void addCard(String key,String category,String location, String timestamp, String description, String level) {
        @SuppressLint("InflateParams") final View view = getLayoutInflater().inflate(R.layout.report_layout, null);

        TextView nameView1 = view.findViewById(R.id.name);
        TextView nameView2 = view.findViewById(R.id.name2);
        TextView nameView3 = view.findViewById(R.id.name4);
        TextView nameView4 = view.findViewById(R.id.description);
        TextView nameView5 = view.findViewById(R.id.description2);
        Button edit = view.findViewById(R.id.validate);
        Button delete = view.findViewById(R.id.reject);

        nameView1.setText(category);
        nameView2.setText(timestamp);
        nameView3.setText(location);
        nameView4.setText(description);
        String danger = getString(R.string.danger_level_prompt)+level;
        nameView5.setText(danger);

        String message = "";
        String english_cat = categoryToEnglish(category);
        if(english_cat.equals(getString(R.string.earthquakes))){
            message = "";
        }
        else if(english_cat.equals(getString(R.string.fire))){
            message = "";
        }
        else if(english_cat.equals(getString(R.string.flood))){
            message = "";
        }
        else if(english_cat.equals(getString(R.string.tornado))){
            message = "";
        }
        String reporting = timestamp + "\n" + message + " at: \n" + location;
        edit.setOnClickListener(v -> {
            FcmNotifications notification = new FcmNotifications("/topics/danger",category,reporting,
                    getApplicationContext(),EmployeeActivity.this);
            notification.SendNotifications();
            layout.removeView(view);
            deleter(key);
        });

        delete.setOnClickListener(v -> {
            //db.delete("POI","title=?",new String[]{s1});
            layout.removeView(view);
            deleter(key);
        });

        layout.addView(view);
    }

    public void deleter(String key){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Emergencies").child(key).removeValue(null);
    }

    public Double[] string2latlong(String s){
        String[] arrOfStr = s.split(",", 2);
        Double[] arrOfDouble = new Double[2];
        arrOfDouble[0] = Double.parseDouble(arrOfStr[0]);
        arrOfDouble[1] = Double.parseDouble(arrOfStr[1]);
        return arrOfDouble;
    }

    public String categoryToEnglish(String category){
        if(Objects.equals(category, getString(R.string.earthquakes))||
                Objects.equals(category, getStringByLocal(this,R.string.earthquakes,"el"))){
            category = getString(R.string.earthquakes);
        }
        else if(Objects.equals(category, getString(R.string.fire))||
                Objects.equals(category, getStringByLocal(this,R.string.fire,"el"))){
            category = getString(R.string.fire);
        }
        else if(Objects.equals(category, getString(R.string.flood))||
                Objects.equals(category, getStringByLocal(this,R.string.flood,"el"))){
            category =  getString(R.string.flood);
        }
        else if(Objects.equals(category, getString(R.string.tornado))||
                Objects.equals(category, getStringByLocal(this,R.string.tornado,"el"))){
            category = getString(R.string.tornado);
        }
        return category;
    }

    public String getStringByLocal(Activity context, int id, String locale) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(new Locale(locale));
        return context.createConfigurationContext(configuration).getResources().getString(id);
    }

}