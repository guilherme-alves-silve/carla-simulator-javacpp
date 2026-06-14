/** Based on the Python example: visualize_multiple_sensors.py */
import java.awt.GridLayout;
import java.time.Duration;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.carla.javacpp.api.BlueprintLibrary;
import org.carla.javacpp.api.Camera;
import org.carla.javacpp.api.Client;
import org.carla.javacpp.api.CollisionSensor;
import org.carla.javacpp.api.LidarSensor;
import org.carla.javacpp.api.LidarSensorOptions;
import org.carla.javacpp.api.Location;
import org.carla.javacpp.api.Rotation;
import org.carla.javacpp.api.Transform;
import org.carla.javacpp.api.Vehicle;
import org.carla.javacpp.api.World;

public final class CarlaMultiSensorViewer {
    private CarlaMultiSensorViewer() {
    }

    public static void main(String[] args) throws Exception {
        JLabel cameraLabel = new JLabel("camera: waiting");
        JLabel lidarLabel = new JLabel("lidar: waiting");
        JLabel collisionLabel = new JLabel("collision: none");

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("CARLA Multi Sensor Viewer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new GridLayout(3, 1));
            frame.add(cameraLabel);
            frame.add(lidarLabel);
            frame.add(collisionLabel);
            frame.setSize(500, 180);
            frame.setVisible(true);
        });

        try (Client client = new Client("localhost", 2000)) {
            client.setTimeout(Duration.ofSeconds(10));
            try (World world = client.getWorld();
                 BlueprintLibrary blueprints = world.getBlueprintLibrary();
                 Vehicle vehicle = world.spawnVehicle(blueprints.filter("vehicle.*").get(0), world.getSpawnPoints().get(0));
                 Camera camera = world.spawnRgbCamera(vehicle, new Transform(new Location(1.6, 0.0, 1.7), new Rotation(0.0, 0.0, 0.0)), 800, 600, 90.0);
                 LidarSensor lidar = world.spawnLidar(vehicle, new Transform(new Location(0.0, 0.0, 2.5), new Rotation(0.0, 0.0, 0.0)), LidarSensorOptions.defaults());
                 CollisionSensor collision = world.spawnCollisionSensor(vehicle, new Transform(new Location(0.0, 0.0, 0.0), new Rotation(0.0, 0.0, 0.0)))) {

                vehicle.setAutopilot(true);
                camera.listen(image -> SwingUtilities.invokeLater(() -> cameraLabel.setText("camera frame: " + image.frame())));
                lidar.listen(measurement -> SwingUtilities.invokeLater(() -> lidarLabel.setText("lidar frame: " + measurement.frame()
                    + " points: " + measurement.pointCount())));
                collision.listen(event -> SwingUtilities.invokeLater(() -> collisionLabel.setText("collision with "
                    + event.otherActorTypeId() + " impulse=" + event.normalImpulseLength())));

                Thread.sleep(60_000);
            }
        }
    }
}
