import java.time.Duration;
import java.util.List;

import org.carla.javacpp.api.Actor;
import org.carla.javacpp.api.ActorList;
import org.carla.javacpp.api.Blueprint;
import org.carla.javacpp.api.BlueprintLibrary;
import org.carla.javacpp.api.Client;
import org.carla.javacpp.api.Location;
import org.carla.javacpp.api.Rotation;
import org.carla.javacpp.api.Transform;
import org.carla.javacpp.api.World;

public final class CarlaJavaSmokeTest {
    private CarlaJavaSmokeTest() {
    }

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 2000;

        try (Client client = new Client(host, port)) {
            client.setTimeout(Duration.ofSeconds(10));

            try (World world = client.getWorld()) {
                System.out.println("Connected to CARLA at " + host + ":" + port);
                System.out.println("Map: " + world.getMapName());

                try (ActorList actors = world.getActors()) {
                    System.out.println("Actors before spawn: " + actors.size());
                }

                Actor spawned = spawnVehicle(world);
                if (spawned == null) {
                    System.out.println("No vehicle blueprint found; connection test completed.");
                    return;
                }

                try (spawned) {
                    System.out.println("Spawned actor id=" + spawned.getId()
                        + ", type=" + spawned.getTypeId());
                    System.out.println("Transform: " + spawned.getTransform());
                    System.out.println("Destroyed: " + spawned.destroy());
                }
            }
        }
    }

    private static Actor spawnVehicle(World world) {
        try (BlueprintLibrary blueprints = world.getBlueprintLibrary()) {
            List<Blueprint> vehicles = blueprints.filter("vehicle.*");
            if (vehicles.isEmpty()) {
                return null;
            }

            Blueprint blueprint = vehicles.get(0).setAttribute("role_name", "java-smoke-test");
            Transform spawnTransform = new Transform(
                new Location(0.0, 0.0, 1.0),
                new Rotation(0.0, 0.0, 0.0));

            return world.spawnActor(blueprint, spawnTransform);
        }
    }
}
