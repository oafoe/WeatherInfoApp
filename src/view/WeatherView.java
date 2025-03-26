package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;


public class WeatherView extends JFrame {
    private JTextField cityField;
    private JButton searchButton;
    private JComboBox<String> unitCombo;
    private JLabel tempLabel;
    private JLabel humidityLabel;
    private JLabel windLabel;
    private JLabel conditionLabel;
    private JLabel timeLabel;
    private JLabel iconLabel;
    private JPanel forecastPanel;
    private JTextArea historyArea;

    public WeatherView() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Weather Information App");
        setLayout(new BorderLayout(20, 20));
        setPreferredSize(new Dimension(1200, 800));

        // Input Panel
        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.NORTH);

        // Main Content Panel
        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);

        // History Panel
        JPanel historyPanel = createHistoryPanel();
        add(historyPanel, BorderLayout.EAST);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        cityField = new JTextField(25);
        cityField.setFont(new Font("Arial", Font.PLAIN, 16));
        cityField.setMargin(new Insets(8, 8, 8, 8));

        searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 18));
        searchButton.setMargin(new Insets(10, 20, 10, 20));

        unitCombo = new JComboBox<>(new String[]{"Celsius", "Fahrenheit"});
        unitCombo.setFont(new Font("Arial", Font.PLAIN, 16));
        unitCombo.setPreferredSize(new Dimension(180, 40));

        panel.add(new JLabel("City:"));
        panel.add(cityField);
        panel.add(searchButton);
        panel.add(new JLabel("Units:"));
        panel.add(unitCombo);

        return panel;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(2, 1, 20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Current Weather Panel
        JPanel weatherPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        weatherPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Current Weather"),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        tempLabel = createStyledLabel("Temperature: ");
        humidityLabel = createStyledLabel("Humidity: ");
        windLabel = createStyledLabel("Wind Speed: ");
        conditionLabel = createStyledLabel("Conditions: ");
        timeLabel = createStyledLabel("Local Time: ");
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

        return mainPanel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Search History (Last 10)"),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        historyArea = new JTextArea(10, 25);
        historyArea.setFont(new Font("Arial", Font.PLAIN, 16));
        historyArea.setEditable(false);
        historyArea.setMargin(new Insets(10, 10, 10, 10));
        panel.add(new JScrollPane(historyArea));

        return panel;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return label;
    }

    // Public accessor methods
    public String getCityInput() {
        return cityField.getText().trim();
    }

    public String getSelectedUnit() {
        return (String) unitCombo.getSelectedItem();
    }

    // Event listener registration
    public void addSearchListener(ActionListener listener) {
        searchButton.addActionListener(listener);
    }

    public void addUnitChangeListener(ActionListener listener) {
        unitCombo.addActionListener(listener);
    }

    // UI update methods
    public void updateWeatherDisplay(String temp, String humidity, String wind,
                                     String condition, String time, Icon icon) {
        tempLabel.setText(temp);
        humidityLabel.setText(humidity);
        windLabel.setText(wind);
        conditionLabel.setText(condition);
        timeLabel.setText(time);
        iconLabel.setIcon(icon);
    }

    public void updateForecast(List<JPanel> forecastEntries) {
        forecastPanel.removeAll();
        for (JPanel entry : forecastEntries) {
            forecastPanel.add(entry);
        }
        forecastPanel.revalidate();
        forecastPanel.repaint();
    }

    public void updateHistory(List<String> history) {
        historyArea.setText(String.join("\n", history));
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}