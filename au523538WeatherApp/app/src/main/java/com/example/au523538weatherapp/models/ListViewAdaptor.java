package com.example.au523538weatherapp.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.au523538weatherapp.R;
import com.example.au523538weatherapp.models.CityWeatherData;

import java.util.List;

public class ListViewAdaptor extends BaseAdapter{

    private Context context;
    private List<CityWeatherData> cityList;

    public ListViewAdaptor(Context c, List<CityWeatherData> list){
        this.context = c;
        this.cityList = list;
    }

    @Override
    public int getCount() {
        if(cityList != null){
            return cityList.size();
        }
        else{
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        if(cityList != null){
            return cityList.get(position);
        }
        else{
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater listInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = listInflater.inflate(R.layout.listview_item, null);
        }

        CityWeatherData weatherData = cityList.get(position);
        if(weatherData != null){
            TextView itemCityName = convertView.findViewById(R.id.itemCityName);
            itemCityName.setText(weatherData.getCityName());

            TextView itemTemperature = convertView.findViewById(R.id.itemTemperature);
            itemTemperature.setText(String.valueOf(weatherData.getTemperature()) + "°");

            ImageView itemIcon = convertView.findViewById(R.id.itemIcon);
            itemIcon.setImageResource(context.getResources().getIdentifier("i" + weatherData.getWeatherIcon(), "drawable", context.getPackageName()));
        }
        return convertView;
    }
}
