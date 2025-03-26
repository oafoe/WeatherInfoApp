package model;

import java.util.List;

public class WeatherData {
    public double temp;
    public int humidity;
    public double windSpeed;
    public String condition;
    public String iconCode;
    public int timezoneOffset;
    public List<ForecastData> forecast;
}