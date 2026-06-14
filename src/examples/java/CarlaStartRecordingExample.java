/** Based on the Python example: start_recording.py */
import java.time.Duration;

import org.carla.javacpp.api.BlueprintLibrary;
import org.carla.javacpp.api.Client;
import org.carla.javacpp.api.Vehicle;
import org.carla.javacpp.api.World;

public final class CarlaStartRecordingExample {
    private CarlaStartRecordingExample() {
    }

    public static void main(String[] args) throws Exception {
        var fileName = args.length > 0 ? args[0] : "java-recording.log";

        try (var client = new Client("localhost", 2000)) {
            client.setTimeout(Duration.ofSeconds(10));

            try (var world = client.getWorld();
                 var blueprints = world.getBlueprintLibrary()) {
                System.out.println(client.startRecorder(fileName, true));

                try {
                    var vehicle = spawnVehicle(world, blueprints);
                    vehicle.setAutopilot(true);
                    Thread.sleep(10_000);
                    vehicle.destroy();
                    vehicle.close();
                } finally {
                    client.stopRecorder();
                    System.out.println("Recording stopped: " + fileName);
                }
            }
        }
    }

    private static Vehicle spawnVehicle(World world, BlueprintLibrary blueprints) {
        var blueprint = blueprints.filter("vehicle.*").get(0).setAttribute("role_name", "java-recorder");
        for (var spawnPoint : world.getSpawnPoints()) {
            var vehicle = world.trySpawnVehicle(blueprint, spawnPoint);
            if (vehicle != null) {
                return vehicle;
            }
        }
        throw new IllegalStateException("Could not spawn vehicle");
    }
}
