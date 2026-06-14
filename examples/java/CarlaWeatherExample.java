/** Based on the Python example: dynamic_weather.py */
import java.time.Duration;

import org.carla.javacpp.api.Client;
import org.carla.javacpp.api.WeatherParameters;
import org.carla.javacpp.api.World;

public final class CarlaWeatherExample {
    private CarlaWeatherExample() {
    }

    public static void main(String[] args) throws Exception {
        try (Client client = new Client("localhost", 2000)) {
            client.setTimeout(Duration.ofSeconds(10));
            try (World world = client.getWorld()) {
                WeatherParameters original = world.getWeather();
                try {
                    for (int i = 0; i <= 100; i += 5) {
                        WeatherParameters weather = WeatherParameters.clearNoon()
                            .cloudiness(i)
                            .precipitation(Math.max(0, i - 30))
                            .sunAzimuthAngle(i * 3.6f)
                            .sunAltitudeAngle(60.0f - i * 0.8f);
                        world.setWeather(weather);
                        System.out.println("Weather cloudiness=" + i + " precipitation=" + Math.max(0, i - 30));
                        Thread.sleep(500);
                    }
                } finally {
                    world.setWeather(original);
                }
            }
        }
    }
}
