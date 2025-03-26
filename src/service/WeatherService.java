package service;

import model.WeatherData;
import model.ForecastData;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;

public class WeatherService {
    private static final String API_KEY = "API_KEY"; //I have my API key included in README.md

    public WeatherData fetchWeatherData(String city) throws IOException {
        WeatherData data = new WeatherData();

        JSONObject currentJson = fetchCurrentWeather(city);
        parseCurrentWeather(currentJson, data);

        JSONObject forecastJson = fetchForecast(city);
        parseForecast(forecastJson, data);

        return data;
    }

    private JSONObject fetchCurrentWeather(String city) throws IOException {
        String url = "http://api.openweathermap.org/data/2.5/weather?q=" +
                URLEncoder.encode(city, "UTF-8") + "&appid=" + API_KEY + "&units=metric";
        return new JSONObject(getApiResponse(url));
    }

    private JSONObject fetchForecast(String city) throws IOException {
        String url = "http://api.openweathermap.org/data/2.5/forecast?q=" +
                URLEncoder.encode(city, "UTF-8") + "&appid=" + API_KEY + "&units=metric";
        return new JSONObject(getApiResponse(url));
    }

    private void parseCurrentWeather(JSONObject json, WeatherData data) {
        JSONObject main = json.getJSONObject("main");
        JSONObject wind = json.getJSONObject("wind");
        JSONObject weather = json.getJSONArray("weather").getJSONObject(0);

        data.temp = main.getDouble("temp");
        data.humidity = main.getInt("humidity");
        data.windSpeed = wind.getDouble("speed");
        data.condition = weather.getString("main");
        data.iconCode = weather.getString("icon");
        data.timezoneOffset = json.getInt("timezone");
    }

    private void parseForecast(JSONObject json, WeatherData data) {
        JSONArray list = json.getJSONArray("list");
        data.forecast = new java.util.ArrayList<>();

        for (int i = 0; i < 6; i++) {
            JSONObject item = list.getJSONObject(i);
            ForecastData fd = new ForecastData();

            fd.time = item.getLong("dt");
            fd.temp = item.getJSONObject("main").getDouble("temp");
            fd.condition = item.getJSONArray("weather").getJSONObject(0).getString("main");

            data.forecast.add(fd);
        }
    }

    private String getApiResponse(String urlString) throws IOException {
        int retries = 3;
        IOException lastException = null;

        for (int i = 0; i < retries; i++) {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(urlString).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int code = conn.getResponseCode();
                if (code == 200) {
                    return readStream(conn.getInputStream());
                } else {
                    String error = readStream(conn.getErrorStream());
                    throw new IOException("HTTP " + code + ": " + error);
                }
            } catch (IOException e) {
                lastException = e;
                if (i < retries - 1) {
                    try { Thread.sleep(1000); }
                    catch (InterruptedException ignored) {}
                }
            } finally {
                if (conn != null) conn.disconnect();
            }
        }
        throw lastException != null ? lastException :
                new IOException("Failed after " + retries + " attempts");
    }

    private String readStream(InputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
}