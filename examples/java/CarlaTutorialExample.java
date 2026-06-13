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
    }

    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 2000;

        try (Client client = new Client(host, port)) {
            client.setTimeout(Duration.ofSeconds(10));

            try (World world = client.getWorld();
                 BlueprintLibrary blueprints = world.getBlueprintLibrary()) {
                List<Blueprint> vehicles = blueprints.filter("vehicle.*");
                Blueprint vehicleBlueprint = vehicles.get(new Random().nextInt(vehicles.size()))
                    .setAttribute("role_name", "java-tutorial");

                Transform spawnPoint = randomSpawnPoint(world);
                try (Vehicle vehicle = world.spawnVehicle(vehicleBlueprint, spawnPoint);
                     Camera camera = world.spawnRgbCamera(
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
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            Blueprint blueprint = vehicles.get(random.nextInt(vehicles.size()));
            Transform spawnPoint = spawnPoints.get(random.nextInt(spawnPoints.size()));
            Actor actor = world.trySpawnActor(blueprint, spawnPoint);
            if (actor != null) {
                System.out.println("NPC vehicle: " + actor.getId());
            }
        }
    }
}
