/** Based on the Python example: tutorial.py */
package org.carla.javacpp.examples.java;

import java.time.Duration;
import java.util.List;
import java.util.Random;

import org.carla.javacpp.api.Actor;
import org.carla.javacpp.api.Blueprint;
import org.carla.javacpp.api.BlueprintLibrary;
import org.carla.javacpp.api.Camera;
import org.carla.javacpp.api.Client;
import org.carla.javacpp.api.Location;
import org.carla.javacpp.api.Rotation;
import org.carla.javacpp.api.Transform;
import org.carla.javacpp.api.Vehicle;
import org.carla.javacpp.api.World;

public final class CarlaTutorialExample {
    private CarlaTutorialExample() {
        throw new IllegalStateException("No CarlaTutorialExample");
    }

    public static void main(String[] args) throws Exception {
        var host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 2000;

        try (var client = new Client(host, port)) {
            client.setTimeout(Duration.ofSeconds(10));

            try (var world = client.getWorld();
                 var blueprints = world.getBlueprintLibrary()) {
                List<Blueprint> vehicles = blueprints.filter("vehicle.*");
                var vehicleBlueprint = vehicles.get(new Random().nextInt(vehicles.size()))
                    .setAttribute("role_name", "java-tutorial");

                var spawnPoint = randomSpawnPoint(world);
                try (var vehicle = world.spawnVehicle(vehicleBlueprint, spawnPoint);
                     var camera = world.spawnRgbCamera(
                         vehicle,
                         new Transform(new Location(1.5, 0.0, 2.4), new Rotation(0.0, 0.0, 0.0)),
                         800,
                         600,
                         90.0)) {

                    System.out.println("Map: " + world.getMapName());
                    System.out.println("Vehicle: " + vehicle.getId() + " " + vehicle.getTypeId());
                    System.out.println("Camera: " + camera.getId());

                    vehicle.setAutopilot(true);
                    camera.listen(image -> System.out.println("Camera frame: " + image.frame()));

                    for (int i = 0; i < 10; i++) {
                        Thread.sleep(500);
                    }

                    spawnNpcVehicles(world, blueprints, 10);
                    Thread.sleep(3_000);
                }
            }
        }
    }

    private static Transform randomSpawnPoint(World world) {
        List<Transform> spawnPoints = world.getSpawnPoints();
        return spawnPoints.get(new Random().nextInt(spawnPoints.size()));
    }

    private static void spawnNpcVehicles(World world, BlueprintLibrary blueprints, int count) {
        List<Blueprint> vehicles = blueprints.filter("vehicle.*");
        List<Transform> spawnPoints = world.getSpawnPoints();
        var random = new Random();

        for (int i = 0; i < count; i++) {
            var blueprint = vehicles.get(random.nextInt(vehicles.size()));
            var spawnPoint = spawnPoints.get(random.nextInt(spawnPoints.size()));
            var actor = world.trySpawnActor(blueprint, spawnPoint);
            if (actor != null) {
                System.out.println("NPC vehicle: " + actor.getId());
            }
        }
    }
}
