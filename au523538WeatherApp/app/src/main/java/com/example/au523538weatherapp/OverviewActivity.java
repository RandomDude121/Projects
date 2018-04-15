package com.example.au523538weatherapp;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.au523538weatherapp.models.CityWeatherData;
import com.example.au523538weatherapp.models.ListViewAdaptor;

import java.util.ArrayList;
import java.util.List;

public class OverviewActivity extends AppCompatActivity {

    public final static int REQUEST_CODE_DETAILS_ACTIVITY = 100;
    private List<CityWeatherData> savedList;

    TextView txtFavCity;
    TextView txtFavTemp;
    ImageView imgFavIcon;
    ListView listview;
    BackgroundService mService;
    boolean favCityChosen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(getString(R.string.newData)));
        startService(new Intent(OverviewActivity.this, BackgroundService.class));

        Intent intent = new Intent(this, BackgroundService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        txtFavCity = findViewById(R.id.txtFavCity);
        txtFavTemp = findViewById(R.id.txtFavTemp);
        imgFavIcon = findViewById(R.id.imgFavIcon);
        listview = findViewById(R.id.listview);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<CityWeatherData> list = mService.getAllCitiesWeather();
                CityWeatherData data = list.get(position);

                Intent i = new Intent(OverviewActivity.this, DetailsActivity.class);
                i.putExtra("cityName", data.getCityName());
                startActivity(i);
            }
        });

        Log.d("OverActivity", "OverActivity created");
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(mService != null){
                Log.d("OverActivity", "Received broadcast");
                savedList = mService.getAllCitiesWeather();
                ListViewAdaptor adaptor = new ListViewAdaptor(getApplicationContext(), savedList);
                listview.setAdapter(adaptor);
                adaptor.notifyDataSetChanged();

                String favCity = mService.getFavCity();
                for (CityWeatherData c : savedList) {
                    String cityName = c.getCityName();
                    if(cityName.equals(favCity)){
                        txtFavCity.setText(c.getCityName());
                        txtFavTemp.setText(c.getTemperature().toString() + getString(R.string.degree));
                        imgFavIcon.setImageResource(getApplicationContext().getResources().getIdentifier("i" + c.getWeatherIcon(), "drawable", getApplicationContext().getPackageName()));
                    }
                }
            }
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("OverviewActivity", "Bind succesful");
            BackgroundService.LocalBinder binder = (BackgroundService.LocalBinder) service;
            mService = binder.getService();

            savedList = mService.getAllCitiesWeather();
            ListViewAdaptor adaptor = new ListViewAdaptor(getApplicationContext(), savedList);
            listview.setAdapter(adaptor);
            adaptor.notifyDataSetChanged();

            if(mService.getFavCity().equals("noData")){
                mService.setFavCity("Aarhus");
                favCityChosen = true;
            }

            String favCity = mService.getFavCity();
            for (CityWeatherData c : savedList) {
                String cityName = c.getCityName();
                if(cityName.equals(favCity)){
                    txtFavCity.setText(c.getCityName());
                    txtFavTemp.setText(c.getTemperature().toString() + getString(R.string.degree));
                    imgFavIcon.setImageResource(getApplicationContext().getResources().getIdentifier("i" + c.getWeatherIcon(), "drawable", getApplicationContext().getPackageName()));
                }
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("OverviewActivity", "Bind unsuccesful");
            mService = null;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.refresh_list:
                Log.d("OverviewActivity", "Refresh button has been pressed");

                ArrayList<String> stringList = new ArrayList<>();
                for (CityWeatherData c: savedList) {
                    stringList.add(c.getCityName());
                }
                mService.forceCheck(stringList);
                break;

            case R.id.set_city:
                Log.d("OverviewActivity", "Set Fav City has been pressed");
                AlertDialog.Builder builder = new AlertDialog.Builder(OverviewActivity.this);
                builder.setTitle(R.string.choose_fav_city);
                final List<CityWeatherData> list = mService.getAllCitiesWeather();
                ListViewAdaptor listViewAdaptor = new ListViewAdaptor(getApplicationContext(), list);
                builder.setAdapter(listViewAdaptor, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CityWeatherData data = list.get(which);
                        txtFavCity.setText(data.getCityName());
                        txtFavTemp.setText(data.getTemperature().toString() + getString(R.string.degree));
                        imgFavIcon.setImageResource(getApplicationContext().getResources().getIdentifier("i" + data.getWeatherIcon(), "drawable", getApplicationContext().getPackageName()));

                        mService.setFavCity(data.getCityName());
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }
}
