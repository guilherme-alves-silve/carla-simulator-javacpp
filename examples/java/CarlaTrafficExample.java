/** Based on the Python example: generate_traffic.py */
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.carla.javacpp.api.Blueprint;
import org.carla.javacpp.api.BlueprintLibrary;
import org.carla.javacpp.api.Client;
import org.carla.javacpp.api.Transform;
import org.carla.javacpp.api.Vehicle;
import org.carla.javacpp.api.World;

public final class CarlaTrafficExample {
    private CarlaTrafficExample() {
    }

    public static void main(String[] args) throws Exception {
        int count = args.length > 0 ? Integer.parseInt(args[0]) : 20;
        List<Vehicle> vehicles = new ArrayList<>();

        try (Client client = new Client("localhost", 2000)) {
            client.setTimeout(Duration.ofSeconds(10));
            try (World world = client.getWorld();
                 BlueprintLibrary blueprints = world.getBlueprintLibrary()) {
                List<Blueprint> vehicleBlueprints = blueprints.filter("vehicle.*");
                List<Transform> spawnPoints = world.getSpawnPoints();
                Random random = new Random();

                for (int i = 0; i < count; i++) {
                    Blueprint blueprint = vehicleBlueprints.get(random.nextInt(vehicleBlueprints.size()));
                    Transform spawnPoint = spawnPoints.get(random.nextInt(spawnPoints.size()));
                    Vehicle vehicle = world.trySpawnVehicle(blueprint, spawnPoint);
                    if (vehicle != null) {
                        vehicle.setAutopilot(true);
                        vehicles.add(vehicle);
                        System.out.println("Spawned vehicle " + vehicle.getId());
                    }
                }

                Thread.sleep(60_000);
            } finally {
                for (Vehicle vehicle : vehicles) {
                    vehicle.destroy();
                    vehicle.close();
                }
            }
        }
    }
}
