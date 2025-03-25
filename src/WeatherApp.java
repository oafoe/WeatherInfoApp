import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherApp extends JFrame {
    private JTextField cityField;
    private JButton searchButton;
    private JComboBox<String> unitCombo;
    private JLabel tempLabel, humidityLabel, windLabel, conditionLabel, timeLabel, iconLabel;
    private JPanel forecastPanel;
    private JTextArea historyArea;
    private WeatherData currentWeather;
    private List<String> searchHistory = new ArrayList<>();

    public WeatherApp() {
        setTitle("Weather Information App");
        setLayout(new BorderLayout(20, 20));
        setPreferredSize(new Dimension(1200, 800));

        // Set default fonts
        Font defaultFont = new Font("Arial", Font.PLAIN, 16);
        Font headingFont = new Font("Arial", Font.BOLD, 18);

        // Input Panel
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        cityField = new JTextField(25);
        cityField.setFont(defaultFont);
        cityField.setMargin(new Insets(8, 8, 8, 8));

        searchButton = new JButton("Search");
        searchButton.setFont(headingFont);
        searchButton.setMargin(new Insets(10, 20, 10, 20));

        unitCombo = new JComboBox<>(new String[]{"Celsius", "Fahrenheit"});
        unitCombo.setFont(defaultFont);
        unitCombo.setPreferredSize(new Dimension(180, 40));

        inputPanel.add(new JLabel("City:"));
        inputPanel.add(cityField);
        inputPanel.add(searchButton);
        inputPanel.add(new JLabel("Units:"));
        inputPanel.add(unitCombo);
        add(inputPanel, BorderLayout.NORTH);

        // Main Content Panel
        JPanel mainPanel = new JPanel(new GridLayout(2, 1, 20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Current Weather Panel
        JPanel weatherPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        weatherPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Current Weather"),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        tempLabel = createInfoLabel("Temperature: ");
        humidityLabel = createInfoLabel("Humidity: ");
        windLabel = createInfoLabel("Wind Speed: ");
        conditionLabel = createInfoLabel("Conditions: ");
        timeLabel = createInfoLabel("Local Time: ");
        iconLabel = new JLabel();
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        weatherPanel.add(tempLabel);
        weatherPanel.add(humidityLabel);
        weatherPanel.add(windLabel);
        weatherPanel.add(conditionLabel);
        weatherPanel.add(timeLabel);
        weatherPanel.add(iconLabel);

        // Forecast Panel
        forecastPanel = new JPanel(new GridLayout(0, 6, 20, 10));
        forecastPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("3-Hour Forecast"),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        mainPanel.add(weatherPanel);
        mainPanel.add(new JScrollPane(forecastPanel));
        add(mainPanel, BorderLayout.CENTER);

        // History Panel
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Search History (Last 10)"),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        historyArea = new JTextArea(10, 25);
        historyArea.setFont(defaultFont);
        historyArea.setMargin(new Insets(10, 10, 10, 10));
        historyPanel.add(new JScrollPane(historyArea));
        add(historyPanel, BorderLayout.EAST);

        // Event Listeners
        searchButton.addActionListener(e -> searchWeather());
        unitCombo.addActionListener(e -> updateUnits());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return label;
    }

    private void searchWeather() {
        String city = cityField.getText().trim();
        if (city.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a city name.");
            return;
        }

        new SwingWorker<WeatherData, Void>() {
            @Override
            protected WeatherData doInBackground() throws Exception {
                return fetchWeatherData(city);
            }

            @Override
            protected void done() {
                try {
                    currentWeather = get();
                    updateWeatherDisplay();
                    addToHistory(city);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(WeatherApp.this, "Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private WeatherData fetchWeatherData(String city) throws IOException {
        String apiKey = "3b3876697c59a7fa7cc36054113bb691";
        String currentUrl = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey + "&units=metric";
        String forecastUrl = "http://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=" + apiKey + "&units=metric";

        // Fetch current weather
        JSONObject currentJson = new JSONObject(getApiResponse(currentUrl));
        WeatherData data = new WeatherData();
        data.temp = currentJson.getJSONObject("main").getDouble("temp");
        data.humidity = currentJson.getJSONObject("main").getInt("humidity");
        data.windSpeed = currentJson.getJSONObject("wind").getDouble("speed");
        data.condition = currentJson.getJSONArray("weather").getJSONObject(0).getString("main");
        data.iconCode = currentJson.getJSONArray("weather").getJSONObject(0).getString("icon");
        data.timezoneOffset = currentJson.getInt("timezone");

        // Fetch forecast
        JSONObject forecastJson = new JSONObject(getApiResponse(forecastUrl));
        JSONArray forecastList = forecastJson.getJSONArray("list");
        data.forecast = new ArrayList<>();
        for (int i = 0; i < 6; i++) { // Next 6 entries (18 hours)
            JSONObject item = forecastList.getJSONObject(i);
            ForecastData fd = new ForecastData();
            fd.time = item.getLong("dt");
            fd.temp = item.getJSONObject("main").getDouble("temp");
            fd.condition = item.getJSONArray("weather").getJSONObject(0).getString("main");
            data.forecast.add(fd);
        }

        return data;
    }

    private String getApiResponse(String urlString) throws IOException {
        int retryCount = 3;
        for (int i = 0; i < retryCount; i++) {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(urlString).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    return response.toString();
                } else {
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorResponse.append(errorLine);
                    }
                    throw new IOException("API Error: " + responseCode + " - " + errorResponse.toString());
                }
            } catch (IOException e) {
                if (i == retryCount - 1) {
                    throw e;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) { }
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
        throw new IOException("Failed to fetch data after multiple retries.");
    }

    private void updateWeatherDisplay() {
        String unit = (String) unitCombo.getSelectedItem();
        double temp = currentWeather.temp;
        double windSpeed = currentWeather.windSpeed;
        String tempUnit = unit.equals("Celsius") ? "°C" : "°F";
        String speedUnit = unit.equals("Celsius") ? "m/s" : "mph";

        if (unit.equals("Fahrenheit")) {
            temp = (temp * 9 / 5) + 32;
            windSpeed *= 2.23694;
        }

        tempLabel.setText(String.format("Temperature: %.1f%s", temp, tempUnit));
        humidityLabel.setText(String.format("Humidity: %d%%", currentWeather.humidity));
        windLabel.setText(String.format("Wind Speed: %.1f %s", windSpeed, speedUnit));
        conditionLabel.setText("Conditions: " + currentWeather.condition);

        try {
            ImageIcon icon = new ImageIcon(new URL("http://openweathermap.org/img/wn/" + currentWeather.iconCode + "@2x.png"));
            iconLabel.setIcon(icon);
        } catch (MalformedURLException e) {
            iconLabel.setIcon(null);
        }

        long currentTime = System.currentTimeMillis() / 1000;
        long localTime = currentTime + currentWeather.timezoneOffset;
        ZonedDateTime zdt = Instant.ofEpochSecond(localTime).atZone(java.time.ZoneOffset.UTC);
        timeLabel.setText("Local Time: " + zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // Update forecast
        forecastPanel.removeAll();
        for (ForecastData fd : currentWeather.forecast) {
            JPanel entryPanel = new JPanel(new GridLayout(3, 1, 5, 5));
            entryPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));

            ZonedDateTime time = Instant.ofEpochSecond(fd.time).atZone(java.time.ZoneOffset.UTC).plusSeconds(currentWeather.timezoneOffset);
            String timeStr = time.format(DateTimeFormatter.ofPattern("HH:mm"));
            double forecastTemp = fd.temp;
            if (unit.equals("Fahrenheit")) {
                forecastTemp = (forecastTemp * 9 / 5) + 32;
            }

            JLabel timeLabel = new JLabel(timeStr);
            timeLabel.setFont(new Font("Arial", Font.BOLD, 14));
            timeLabel.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel tempLabel = new JLabel(String.format("%.1f%s", forecastTemp, tempUnit));
            tempLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            tempLabel.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel conditionLabel = new JLabel(fd.condition);
            conditionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            conditionLabel.setHorizontalAlignment(SwingConstants.CENTER);

            entryPanel.add(timeLabel);
            entryPanel.add(tempLabel);
            entryPanel.add(conditionLabel);
            forecastPanel.add(entryPanel);
        }
        forecastPanel.revalidate();
        forecastPanel.repaint();
    }

    private void addToHistory(String city) {
        String entry = java.time.LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + " - " + city;
        searchHistory.add(entry);
        if (searchHistory.size() > 10) {
            searchHistory.remove(0);
        }
        historyArea.setText(String.join("\n", searchHistory));
    }

    private void updateUnits() {
        if (currentWeather != null) {
            updateWeatherDisplay();
        }
    }

    private static class WeatherData {
        double temp;
        int humidity;
        double windSpeed;
        String condition;
        String iconCode;
        int timezoneOffset;
        List<ForecastData> forecast;
    }

    private static class ForecastData {
        long time;
        double temp;
        String condition;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WeatherApp());
    }
}

// code is now on github, and CVS works fine.
