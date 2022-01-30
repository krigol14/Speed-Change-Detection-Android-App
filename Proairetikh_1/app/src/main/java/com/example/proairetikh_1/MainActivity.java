package com.example.proairetikh_1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements LocationListener {

    static final int REQ_LOC_CODE = 23;
    Button button2;
    Button button3;
    TextView textView;
    TextView textView2;
    LocationManager locationManager;
    FirebaseDatabase database;
    DatabaseReference reference2;
    DatabaseReference reference3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        textView.setVisibility(View.INVISIBLE);
        textView2.setVisibility(View.INVISIBLE);

        // initialize a firebase reference as well as separate branches for the purposes of the project
        database = FirebaseDatabase.getInstance();
        reference2 = database.getReference("Current Data");
        reference3 = database.getReference("Acceleration & Deceleration Values");

        // when application starts, update current data branch on firebase with default values
        String timestamp = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now());
        Data initial_data = new Data(timestamp, 0.0, 0.0, 0.0);
        reference2.setValue(initial_data);

        // add listener to retrieve data from firebase when they are updated
        // the data are updated from the onLocationChange() method below
        reference2.addValueEventListener(new ValueEventListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Data data = snapshot.getValue(Data.class);
                double old_speed = Double.parseDouble(textView2.getText().toString());
                double latitude = data.getLatitude();
                double longitude = data.getLongitude();
                double new_speed = data.getSpeed();
                String timestamp = data.getTimestamp();
                textView2.setText(String.format("%.1f", new_speed));

                // call calculate() with parameters the data we retrieved in order to calculate the speed change
                // and if its significant add a marker in the map
                calculate(old_speed, new_speed, latitude, longitude, timestamp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void show_map(View view) {
        startActivity(new Intent(this, MapsActivity.class));
    }

    public void show_speed(View view) {
        textView.setVisibility(View.VISIBLE);
        textView2.setVisibility(View.VISIBLE);

        // press button every 5 sec to store continuously every 5 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                button3.performClick();
            }
        }, 5000);

        // ask for permissions if not given
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOC_CODE);
        } else {
            // calculate current speed and display it
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @SuppressLint({"DefaultLocale", "SetTextI18n"})
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    // create a new Data object with current data to push in firebase
                    String timestamp = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now());
                    double speed = Double.parseDouble(String.format("%.1f", location.getSpeed() * 3.6));
                    Data data = new Data(timestamp, speed, location.getLongitude(), location.getLatitude());
                    reference2.setValue(data);
                }
            });
        }
    }

    public void calculate(double old_speed, double new_speed, double lat, double lng, String timestamp) {
        double speed_change = new_speed - old_speed;
        double acc_dec = speed_change / 5;      // Dt = 5sec as we take into consideration speed changes in 5 seconds time span

        // we consider a speed change (acceleration or deceleration) as significant if it's at least 40km/h in 5seconds
        if (Math.abs(acc_dec) > 8) {
            // if the speed change is considered significant store its value and metadata about it in a separate firebase branch
            Data data = new Data(acc_dec, lng, lat, old_speed);
            reference3.child(timestamp).setValue(data);
        }
    }

    public void end_route(View view){
        show_map(view);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {}
}