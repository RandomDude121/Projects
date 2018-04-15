package com.example.au523538weatherapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.au523538weatherapp.models.CityWeatherData;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailsActivity extends AppCompatActivity {

    BackgroundService mService;

    TextView txtCityName;
    TextView txtTemperatureValue;
    TextView txtWindSpeedValue;
    TextView txtDescriptionValue;
    TextView txtTimestamp;
    ImageView imgIcon;
    Button btnOkay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        startService(new Intent(DetailsActivity.this, BackgroundService.class));
        Intent intent = new Intent(this, BackgroundService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        txtCityName = findViewById(R.id.txtCityName);
        txtTemperatureValue = findViewById(R.id.txtTemperatureValue);
        txtWindSpeedValue = findViewById(R.id.txtWindSpeedValue);
        txtDescriptionValue = findViewById(R.id.txtDescriptionValue);
        txtTimestamp = findViewById(R.id.txtTimestamp);
        imgIcon = findViewById(R.id.imgIcon);
        btnOkay = findViewById(R.id.btnOkay);

        btnOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if(savedInstanceState != null){
            String cityName = savedInstanceState.getString("cityName");
            String cityTemperature = savedInstanceState.getString("cityTemperature");
            String cityDesc = savedInstanceState.getString("cityDesc");
            String cityWindspeed = savedInstanceState.getString("cityWindspeed");
            String cityTimestamp = savedInstanceState.getString("cityTimestamp");
            String cityIcon = savedInstanceState.getString("cityIcon");

            txtCityName.setText(cityName);
            txtTemperatureValue.setText(cityTemperature + getString(R.string.degree));
            txtWindSpeedValue.setText(cityWindspeed + getString(R.string.meterPerSec));
            txtDescriptionValue.setText(cityDesc);
            txtTimestamp.setText(cityTimestamp);
            imgIcon.setImageResource(getApplicationContext().getResources().getIdentifier(cityIcon, "drawable", getApplicationContext().getPackageName()));
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("DetailsActivity", "Bind succesful");
            BackgroundService.LocalBinder binder = (BackgroundService.LocalBinder) service;
            mService = binder.getService();

            Intent i = getIntent();
            String cityName = i.getStringExtra("cityName");

            CityWeatherData data = mService.getCityWeather(cityName);
            txtCityName.setText(data.getCityName());
            txtTemperatureValue.setText(data.getTemperature().toString() + getString(R.string.degree));
            txtWindSpeedValue.setText(data.getWindSpeed().toString() + getString(R.string.meterPerSec));
            txtDescriptionValue.setText(data.getWeatherDesc());

            Log.d("DetailsActivity", "Time: " + data.getTimestamp());
            Date d = new java.util.Date(data.getTimestamp() * 1000L);
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            String date = formatter.format(d);
            txtTimestamp.setText(getString(R.string.weatherCheckedAt) + date);
            imgIcon.setImageResource(getApplicationContext().getResources().getIdentifier("i" + data.getWeatherIcon(), "drawable", getApplicationContext().getPackageName()));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("DetailsActivity", "Bind unsuccesful");
            mService = null;
        }
    };

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("cityName", txtCityName.getText().toString());
        savedInstanceState.putString("cityTemperature", txtTemperatureValue.getText().toString());
        savedInstanceState.putString("cityDesc", txtDescriptionValue.getText().toString());
        savedInstanceState.putString("cityWindspeed", txtWindSpeedValue.getText().toString());
        savedInstanceState.putString("cityTimestamp", txtTimestamp.getText().toString());
        savedInstanceState.putString("cityIcon", imgIcon.getDrawable().toString());
        super.onSaveInstanceState(savedInstanceState);
    }
}
