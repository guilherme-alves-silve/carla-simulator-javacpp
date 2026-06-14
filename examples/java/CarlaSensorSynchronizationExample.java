/** Based on the Python example: sensor_synchronization.py */
import java.time.Duration;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.carla.javacpp.api.Blueprint;
import org.carla.javacpp.api.BlueprintLibrary;
import org.carla.javacpp.api.Camera;
import org.carla.javacpp.api.Client;
import org.carla.javacpp.api.LidarSensor;
import org.carla.javacpp.api.LidarSensorOptions;
import org.carla.javacpp.api.Location;
import org.carla.javacpp.api.Rotation;
import org.carla.javacpp.api.Transform;
import org.carla.javacpp.api.Vehicle;
import org.carla.javacpp.api.World;
import org.carla.javacpp.api.WorldSettings;

public final class CarlaSensorSynchronizationExample {
    private CarlaSensorSynchronizationExample() {
    }

    public static void main(String[] args) throws Exception {
        BlockingQueue<String> queue = new LinkedBlockingQueue<>();

        try (Client client = new Client("localhost", 2000)) {
            client.setTimeout(Duration.ofSeconds(10));

            try (World world = client.getWorld();
                 BlueprintLibrary blueprints = world.getBlueprintLibrary()) {
                WorldSettings original = world.getSettings();
                world.applySettings(new WorldSettings(true, false, 0.05));

                try {
                    Blueprint vehicleBlueprint = blueprints.filter("vehicle.*").get(0);
                    Vehicle vehicle = world.spawnVehicle(vehicleBlueprint, world.getSpawnPoints().get(0));
                    Camera camera = world.spawnRgbCamera(
                        vehicle,
                        new Transform(new Location(1.5, 0.0, 2.0), new Rotation(0.0, 0.0, 0.0)),
                        800,
                        600,
                        90.0);
                    LidarSensor lidar = world.spawnLidar(
                        vehicle,
                        new Transform(new Location(0.0, 0.0, 2.4), new Rotation(0.0, 0.0, 0.0)),
                        LidarSensorOptions.defaults());

                    camera.listen(image -> queue.offer("camera frame=" + image.frame()));
                    lidar.listen(measurement -> queue.offer("lidar frame=" + measurement.frame()
                        + " points=" + measurement.pointCount()));

                    for (int frame = 0; frame < 30; frame++) {
                        long worldFrame = world.tick();
                        System.out.println("World frame: " + worldFrame);
                        for (int i = 0; i < 2; i++) {
                            String event = queue.poll(2, TimeUnit.SECONDS);
                            System.out.println("  " + (event == null ? "missing sensor data" : event));
                        }
                    }

                    camera.close();
                    lidar.close();
                    vehicle.close();
                } finally {
                    world.applySettings(original);
                }
            }
        }
    }
}
