package controller;

import model.WeatherData;
import model.ForecastData;
import service.WeatherService;
import view.WeatherView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WeatherController {
    private final WeatherView view;
    private final WeatherService service;
    private WeatherData currentWeather;
    private final List<String> searchHistory = new ArrayList<>();

    public WeatherController(WeatherView view, WeatherService service) {
        this.view = view;
        this.service = service;
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        view.addSearchListener(this::handleSearch);
        view.addUnitChangeListener(this::handleUnitChange);
    }

    private void handleSearch(ActionEvent e) {
        String city = view.getCityInput();
        if (city.isEmpty()) {
            view.showError("Please enter a city name.");
            return;
        }

        new SwingWorker<WeatherData, Void>() {
            @Override
            protected WeatherData doInBackground() throws Exception {
                return service.fetchWeatherData(city);
            }

            @Override
            protected void done() {
                try {
                    currentWeather = get();
                    updateDisplay();
                    updateHistory(city);
                } catch (Exception ex) {
                    view.showError(ex.getMessage());
                }
            }
        }.execute();
    }

    private void handleUnitChange(ActionEvent e) {
        if (currentWeather != null) {
            updateDisplay();
        }
    }

    private void updateDisplay() {
        String selectedUnit = view.getSelectedUnit();
        UnitConverter converter = new UnitConverter(selectedUnit);

        view.updateWeatherDisplay(
                converter.formatTemperature(currentWeather.temp),
                "Humidity: " + currentWeather.humidity + "%",
                converter.formatWindSpeed(currentWeather.windSpeed),
                "Conditions: " + currentWeather.condition,
                formatLocalTime(currentWeather.timezoneOffset),
                loadWeatherIcon(currentWeather.iconCode)
        );

        List<JPanel> forecastEntries = new ArrayList<>();
        for (ForecastData fd : currentWeather.forecast) {
            forecastEntries.add(createForecastEntry(fd, converter));
        }
        view.updateForecast(forecastEntries);
    }

    private JPanel createForecastEntry(ForecastData fd, UnitConverter converter) {
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        ZonedDateTime time = Instant.ofEpochSecond(fd.time)
                .atZone(java.time.ZoneOffset.UTC)
                .plusSeconds(currentWeather.timezoneOffset);

        JLabel timeLabel = new JLabel(time.format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel tempLabel = new JLabel(converter.formatTemperature(fd.temp, true));
        tempLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel conditionLabel = new JLabel(fd.condition);
        conditionLabel.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(timeLabel);
        panel.add(tempLabel);
        panel.add(conditionLabel);

        return panel;
    }

    private String formatLocalTime(int timezoneOffset) {
        return "Local Time: " + Instant.ofEpochSecond(System.currentTimeMillis() / 1000 + timezoneOffset)
                .atZone(java.time.ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private ImageIcon loadWeatherIcon(String iconCode) {
        try {
            return new ImageIcon(new URL("http://openweathermap.org/img/wn/" + iconCode + "@2x.png"));
        } catch (Exception e) {
            return null;
        }
    }

    private void updateHistory(String city) {
        String entry = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + " - " + city;
        searchHistory.add(entry);
        if (searchHistory.size() > 10) {
            searchHistory.remove(0);
        }
        view.updateHistory(searchHistory);
    }

    private static class UnitConverter {
        private final boolean isFahrenheit;

        public UnitConverter(String unit) {
            isFahrenheit = unit.equals("Fahrenheit");
        }

        public String formatTemperature(double celsius) {
            double value = isFahrenheit ? (celsius * 9/5) + 32 : celsius;
            String unit = isFahrenheit ? "°F" : "°C";
            return String.format("Temperature: %.1f%s", value, unit);
        }

        public String formatTemperature(double celsius, boolean brief) {
            double value = isFahrenheit ? (celsius * 9/5) + 32 : celsius;
            String unit = isFahrenheit ? "°F" : "°C";
            return brief ? String.format("%.1f%s", value, unit) : formatTemperature(celsius);
        }

        public String formatWindSpeed(double metersPerSecond) {
            double value = isFahrenheit ? metersPerSecond * 2.23694 : metersPerSecond;
            String unit = isFahrenheit ? "mph" : "m/s";
            return String.format("Wind Speed: %.1f %s", value, unit);
        }
    }
}