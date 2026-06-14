/** Based on the Python example: start_replaying.py */
import java.time.Duration;

import org.carla.javacpp.api.Client;

public final class CarlaReplayRecordingExample {
    private CarlaReplayRecordingExample() {
    }

    public static void main(String[] args) throws Exception {
        var fileName = args.length > 0 ? args[0] : "java-recording.log";
        var start = args.length > 1 ? Double.parseDouble(args[1]) : 0.0;
        var duration = args.length > 2 ? Double.parseDouble(args[2]) : 0.0;
        var followId = args.length > 3 ? Long.parseLong(args[3]) : 0L;

        try (var client = new Client("localhost", 2000)) {
            client.setTimeout(Duration.ofSeconds(10));
            System.out.println(client.replayFile(fileName, start, duration, followId));
            Thread.sleep(10_000);
            client.stopReplayer(false);
        }
    }
}
