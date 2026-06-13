package org.carla.javacpp.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;

import org.junit.jupiter.api.Test;

final class CarlaSpawnSensorIT {
    @Test
    void spawnsVehicleCameraAndLidar() {
        try (Client client = new Client("localhost", 2000)) {
            client.setTimeout(Duration.ofSeconds(10));
            try (World world = client.getWorld();
                 BlueprintLibrary blueprints = world.getBlueprintLibrary();
                 Vehicle vehicle = world.spawnVehicle(blueprints.filter("vehicle.*").get(0), world.getSpawnPoints().get(0));
                 Camera camera = world.spawnRgbCamera(
                     vehicle,
                     new Transform(new Location(1.5, 0.0, 1.7), new Rotation(0.0, 0.0, 0.0)),
                     320,
                     240,
                     90.0);
                 LidarSensor lidar = world.spawnLidar(
                     vehicle,
                     new Transform(new Location(0.0, 0.0, 2.4), new Rotation(0.0, 0.0, 0.0)),
                     LidarSensorOptions.defaults())) {

                assertNotNull(camera);
                assertNotNull(lidar);
                vehicle.destroy();
                camera.destroy();
                lidar.destroy();
            }
        }
    }
}
