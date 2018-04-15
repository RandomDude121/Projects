package com.example.au523538weatherapp.models;

public class CityWeatherData {
    private String CityName;
    private Double Temperature;
    private String WeatherIcon;
    private Double WindSpeed;
    private String WeatherDesc;
    private long Timestamp;

    public String getCityName() {
        return CityName;
    }

    public void setCityName(String cityName) {
        CityName = cityName;
    }

    public Double getTemperature() {
        return Temperature;
    }

    public void setTemperature(Double temperature) {
        Temperature = temperature;
    }

    public String getWeatherIcon() {
        return WeatherIcon;
    }

    public void setWeatherIcon(String weatherIcon) {
        WeatherIcon = weatherIcon;
    }

    public Double getWindSpeed() {
        return WindSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        WindSpeed = windSpeed;
    }

    public String getWeatherDesc() {
        return WeatherDesc;
    }

    public void setWeatherDesc(String weatherDesc) {
        WeatherDesc = weatherDesc;
    }

    public long getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(long timestamp) {
        Timestamp = timestamp;
    }
}
