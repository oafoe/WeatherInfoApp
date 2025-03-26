import view.WeatherView;
import service.WeatherService;
import controller.WeatherController;
import javax.swing.SwingUtilities;

public class AppLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WeatherView view = new WeatherView();
            WeatherService service = new WeatherService();
            new WeatherController(view, service);
            view.setVisible(true);
        });
    }
}