/** Based on the Python example: show_recorder_collisions.py */
import java.time.Duration;

import org.carla.javacpp.api.Client;
import org.carla.javacpp.api.RecorderActorType;

public final class CarlaRecorderCollisionsExample {
    private CarlaRecorderCollisionsExample() {
    }

    public static void main(String[] args) {
        var fileName = args.length > 0 ? args[0] : "java-recording.log";
        var type1 = args.length > 1 ? args[1].charAt(0) : RecorderActorType.ALL;
        var type2 = args.length > 2 ? args[2].charAt(0) : RecorderActorType.ALL;

        try (var client = new Client("localhost", 2000)) {
            client.setTimeout(Duration.ofSeconds(10));
            System.out.println(client.showRecorderCollisions(fileName, type1, type2));
        }
    }
}
