/** Based on the Python example: show_recorder_actors_blocked.py */
import java.time.Duration;

import org.carla.javacpp.api.Client;

public final class CarlaRecorderActorsBlockedExample {
    private CarlaRecorderActorsBlockedExample() {
    }

    public static void main(String[] args) {
        var fileName = args.length > 0 ? args[0] : "java-recording.log";
        var minTime = args.length > 1 ? Double.parseDouble(args[1]) : 30.0;
        var minDistance = args.length > 2 ? Double.parseDouble(args[2]) : 10.0;

        try (var client = new Client("localhost", 2000)) {
            client.setTimeout(Duration.ofSeconds(10));
            System.out.println(client.showRecorderActorsBlocked(fileName, minTime, minDistance));
        }
    }
}
