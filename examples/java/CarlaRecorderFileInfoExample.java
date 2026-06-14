/** Based on the Python example: show_recorder_file_info.py */
import java.time.Duration;

import org.carla.javacpp.api.Client;

public final class CarlaRecorderFileInfoExample {
    private CarlaRecorderFileInfoExample() {
    }

    public static void main(String[] args) {
        var fileName = args.length > 0 ? args[0] : "java-recording.log";
        var showAll = args.length > 1 && Boolean.parseBoolean(args[1]);

        try (var client = new Client("localhost", 2000)) {
            client.setTimeout(Duration.ofSeconds(10));
            System.out.println(client.showRecorderFileInfo(fileName, showAll));
        }
    }
}
