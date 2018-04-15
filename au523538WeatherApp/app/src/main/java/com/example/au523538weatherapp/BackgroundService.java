package com.example.au523538weatherapp;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.au523538weatherapp.models.CityWeatherData;
import com.example.au523538weatherapp.utils.Globals;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends Service {

    private static final Double KELVIN = 273.15;
    private static int FOREGROUND_SERVICE = 1337;
    private final IBinder mBinder = new LocalBinder();
    private Timer mTimer;
    private RequestQueue rq;
    private List<String> cityList;
    private ArrayList<CityWeatherData> citiesInformation;
    private SharedPreferences sharedPreferences;
    private boolean found;
    private Date weatherUpdate;
    private int whenToBroadcast;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder{
        BackgroundService getService(){
            return BackgroundService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Background Service", "Background service started");
        rq = Volley.newRequestQueue(getApplicationContext());
        sharedPreferences = getSharedPreferences("DB", MODE_PRIVATE);
        cityList = new ArrayList<>();
        citiesInformation = new ArrayList<>();

        cityList.add("Copenhagen");
        cityList.add("Aarhus");
        cityList.add("Odense");
        cityList.add("Aalborg");
        cityList.add("Esbjerg");
        cityList.add("Randers");
        cityList.add("Kolding");
        cityList.add("Horsens");
        cityList.add("Vejle");
        cityList.add("Roskilde");

        for (String s: cityList) {
            getCity(s);
        }

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (String s: cityList) {
                    getCity(s);
                }

                weatherUpdate = new Date();
                startForeground(FOREGROUND_SERVICE, buildNotification());

                Intent intent = new Intent(getString(R.string.newData));
                LocalBroadcastManager.getInstance(BackgroundService.this).sendBroadcast(intent);
                Log.d("Background Service", "New data has arrived");
            }
        }, 0, 10000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    public List<CityWeatherData> getAllCitiesWeather(){
        return citiesInformation;
    }

    public String getFavCity(){
        String favCity = sharedPreferences.getString(getString(R.string.favCity), "noData");
        return favCity;
    }

    public void setFavCity(String cityName){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.favCity), cityName);
        editor.apply();
    }

    public CityWeatherData getCityWeather(String cityName){
        for (CityWeatherData c: citiesInformation) {
            String dataCityName = c.getCityName();
            if(dataCityName.equals(cityName)){
                return c;
            }
        }
        return null;
    }

    public void forceCheck(ArrayList<String> list){
        for (String s: list) {
            getCity(s);
        }
    }

    public void getCity( String cityName){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                Globals.WEATHER_API_CALL_HTTPS + cityName + Globals.WEATHER_API_KEY,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("Background Service", "Got response from request");
                            CityWeatherData data = new CityWeatherData();

                            //City Name
                            String name = response.getString("name");
                            data.setCityName(name);

                            //Temperature
                            JSONObject j = response.optJSONObject("main");
                            if(j != null){
                                Double temp = j.getDouble("temp") - KELVIN;
                                Double deg = BigDecimal.valueOf(temp).setScale(1, RoundingMode.FLOOR).doubleValue();
                                data.setTemperature(deg);
                            }

                            //Weather Description and Icon
                            JSONArray j2 = response.optJSONArray("weather");
                            JSONObject jsonWeather = j2.getJSONObject(0);
                            if(jsonWeather != null){
                                String desc = jsonWeather.getString("description");
                                String icon = jsonWeather.getString("icon");
                                data.setWeatherDesc(desc);
                                data.setWeatherIcon(icon);
                            }

                            //WindSpeed
                            JSONObject j3 = response.optJSONObject("wind");
                            if(j3 != null){
                                Double windSpeed = j3.getDouble("speed");
                                data.setWindSpeed(windSpeed);
                            }

                            //Timestamp
                            long time = response.getLong("dt");
                            data.setTimestamp(time);

                            //Add city to cities information list
                            for (CityWeatherData c: citiesInformation) {
                                if(c.getCityName().equals(data.getCityName())){
                                    c = data;
                                    found = true;
                                    break;
                                }
                            }

                            if(!found){
                                citiesInformation.add(data);
                            }

                            ++whenToBroadcast;
                            if(whenToBroadcast == citiesInformation.size()){
                                Intent intent = new Intent(getString(R.string.newData));
                                LocalBroadcastManager.getInstance(BackgroundService.this).sendBroadcast(intent);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(BackgroundService.this, getString(R.string.badApiResponse), Toast.LENGTH_SHORT).show();
                    }
                }
        );
        rq.add(jsonObjectRequest);
    }

    private Notification buildNotification(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        DateFormat d = new SimpleDateFormat("HH:mm:ss");
        String date = d.format(weatherUpdate);

        builder.setOngoing(true)
                .setContentTitle(getString(R.string.notificationTitle))
                .setContentText(getString(R.string.weatherCheckedAt) + date)
                .setSmallIcon(R.drawable.notification_small_icon);

        return builder.build();
    }
}
