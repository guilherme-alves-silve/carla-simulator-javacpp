/** Based on the Python example: open3d_lidar.py */
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.carla.javacpp.api.BlueprintLibrary;
import org.carla.javacpp.api.Client;
import org.carla.javacpp.api.LidarMeasurement;
import org.carla.javacpp.api.LidarSensor;
import org.carla.javacpp.api.LidarSensorOptions;
import org.carla.javacpp.api.Location;
import org.carla.javacpp.api.Rotation;
import org.carla.javacpp.api.Transform;
import org.carla.javacpp.api.Vehicle;
import org.carla.javacpp.api.World;

public final class CarlaLidarViewer {
    private CarlaLidarViewer() {
    }

    public static void main(String[] args) throws Exception {
        var panel = new LidarPanel();
        SwingUtilities.invokeLater(() -> {
            var frame = new JFrame("CARLA LiDAR Viewer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 800);
            frame.setContentPane(panel);
            frame.setVisible(true);
        });

        try (var client = new Client("localhost", 2000)) {
            client.setTimeout(Duration.ofSeconds(10));
            try (var world = client.getWorld();
                 var blueprints = world.getBlueprintLibrary();
                 var vehicle = world.spawnVehicle(blueprints.filter("vehicle.*").get(0), firstSpawn(world));
                 var lidar = world.spawnLidar(
                     vehicle,
                     new Transform(new Location(0.0, 0.0, 2.5), new Rotation(0.0, 0.0, 0.0)),
                     new LidarSensorOptions(64, 80.0, 120_000, 20.0, 15.0, -35.0))) {

                vehicle.setAutopilot(true);
                lidar.listen(measurement -> SwingUtilities.invokeLater(() -> panel.setMeasurement(measurement)));
                Thread.sleep(60_000);
            }
        }
    }

    private static Transform firstSpawn(World world) {
        List<Transform> spawnPoints = world.getSpawnPoints();
        return spawnPoints.get(0);
    }

    private static final class LidarPanel extends JPanel {
        private BufferedImage image;

        void setMeasurement(LidarMeasurement measurement) {
            int size = 760;
            var next = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            var graphics = next.getGraphics();
            graphics.setColor(Color.BLACK);
            graphics.fillRect(0, 0, size, size);
            graphics.setColor(Color.WHITE);

            float scale = size / 160.0f;
            int center = size / 2;
            for (int i = 0; i < measurement.pointCount(); i++) {
                int x = center + Math.round(measurement.y(i) * scale);
                int y = center - Math.round(measurement.x(i) * scale);
                if (x >= 0 && x < size && y >= 0 && y < size) {
                    next.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
            graphics.dispose();
            image = next;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (image != null) {
                graphics.drawImage(image, 0, 0, getWidth(), getHeight(), null);
            }
        }
    }
}
