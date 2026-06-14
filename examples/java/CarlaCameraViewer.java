/** Based on the Python example: manual_control.py */
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.carla.javacpp.api.Blueprint;
import org.carla.javacpp.api.BlueprintLibrary;
import org.carla.javacpp.api.Camera;
import org.carla.javacpp.api.Client;
import org.carla.javacpp.api.Location;
import org.carla.javacpp.api.Rotation;
import org.carla.javacpp.api.Transform;
import org.carla.javacpp.api.Vehicle;
import org.carla.javacpp.api.VehicleControl;
import org.carla.javacpp.api.World;

public final class CarlaCameraViewer {
    private CarlaCameraViewer() {
    }

    public static void main(String[] args) throws InterruptedException {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 2000;

        ImagePanel panel = new ImagePanel();
        AtomicBoolean running = new AtomicBoolean(true);
        AtomicReference<CameraMode> cameraMode = new AtomicReference<>(CameraMode.THIRD_PERSON);
        KeyboardControl keyboard = new KeyboardControl();
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("CARLA RGB Camera");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent event) {
                    running.set(false);
                }
            });
            frame.setSize(820, 640);
            frame.setContentPane(panel);
            frame.addKeyListener(keyboard);
            keyboard.setCameraToggle(() -> {
                CameraMode next = cameraMode.updateAndGet(CameraMode::next);
                System.out.println("Camera mode: " + next.label());
            });
            frame.setVisible(true);
            frame.requestFocusInWindow();
        });

        try (Client client = new Client(host, port)) {
            client.setTimeout(Duration.ofSeconds(10));

            try (World world = client.getWorld();
                 BlueprintLibrary blueprints = world.getBlueprintLibrary()) {
                Blueprint vehicleBlueprint = first(blueprints.filter("vehicle.*"), "vehicle.*");
                Blueprint cameraBlueprintMarker = first(blueprints.filter("sensor.camera.rgb"), "sensor.camera.rgb");
                System.out.println("Using camera blueprint: " + cameraBlueprintMarker.getId());

                try (Vehicle vehicle = spawnVehicle(world, vehicleBlueprint.setAttribute("role_name", "java-camera-test"));
                     Camera thirdPersonCamera = world.spawnRgbCamera(
                         vehicle,
                         new Transform(new Location(-5.5, 0.0, 2.8), new Rotation(-15.0, 0.0, 0.0)),
                         800,
                         600,
                         90.0);
                     Camera firstPersonCamera = world.spawnRgbCamera(
                         vehicle,
                         new Transform(new Location(1.6, 0.0, 1.7), new Rotation(0.0, 0.0, 0.0)),
                         800,
                         600,
                         90.0)) {

                    System.out.println("Vehicle id: " + vehicle.getId());
                    System.out.println("Third person camera id: " + thirdPersonCamera.getId());
                    System.out.println("First person camera id: " + firstPersonCamera.getId());
                    System.out.println("Controls: W/UP accelerate, S/DOWN brake, A/LEFT steer left, D/RIGHT steer right, SPACE handbrake, R reverse, C camera");

                    thirdPersonCamera.listen(image -> {
                        if (cameraMode.get() == CameraMode.THIRD_PERSON) {
                            SwingUtilities.invokeLater(() -> panel.setImage(image.toBufferedImage()));
                        }
                    });
                    firstPersonCamera.listen(image -> {
                        if (cameraMode.get() == CameraMode.FIRST_PERSON) {
                            SwingUtilities.invokeLater(() -> panel.setImage(image.toBufferedImage()));
                        }
                    });

                    while (running.get()) {
                        VehicleControl control = keyboard.currentControl();
                        vehicle.applyControl(control);
                        Thread.sleep(50);
                    }

                    thirdPersonCamera.stop();
                    firstPersonCamera.stop();
                    thirdPersonCamera.destroy();
                    firstPersonCamera.destroy();
                    vehicle.destroy();
                }
            }
        }
    }

    private static Blueprint first(List<Blueprint> blueprints, String pattern) {
        if (blueprints.isEmpty()) {
            throw new IllegalStateException("No blueprint found for " + pattern);
        }
        return blueprints.get(0);
    }

    private static Vehicle spawnVehicle(World world, Blueprint blueprint) {
        List<Transform> spawnPoints = world.getSpawnPoints();
        if (spawnPoints.isEmpty()) {
            throw new IllegalStateException("Current map has no recommended spawn points");
        }

        Random random = new Random();
        int start = random.nextInt(spawnPoints.size());
        for (int i = 0; i < spawnPoints.size(); i++) {
            Transform spawnPoint = spawnPoints.get((start + i) % spawnPoints.size());
            Vehicle vehicle = world.trySpawnVehicle(blueprint, spawnPoint);
            if (vehicle != null) {
                System.out.println("Spawn point: " + spawnPoint);
                return vehicle;
            }
        }

        throw new IllegalStateException("Could not spawn vehicle at any recommended spawn point");
    }

    private enum CameraMode {
        THIRD_PERSON("third person"),
        FIRST_PERSON("first person");

        private final String label;

        CameraMode(String label) {
            this.label = label;
        }

        String label() {
            return label;
        }

        CameraMode next() {
            return this == THIRD_PERSON ? FIRST_PERSON : THIRD_PERSON;
        }
    }

    private static final class ImagePanel extends JPanel {
        private BufferedImage image;

        void setImage(BufferedImage image) {
            this.image = image;
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

    private static final class KeyboardControl extends KeyAdapter {
        private final Set<Integer> pressed = new HashSet<>();
        private boolean reverse;
        private Runnable cameraToggle;

        synchronized void setCameraToggle(Runnable cameraToggle) {
            this.cameraToggle = cameraToggle;
        }

        @Override
        public synchronized void keyPressed(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.VK_R) {
                reverse = !reverse;
                System.out.println("Reverse: " + reverse);
            }
            if (event.getKeyCode() == KeyEvent.VK_C && !pressed.contains(KeyEvent.VK_C) && cameraToggle != null) {
                cameraToggle.run();
            }
            pressed.add(event.getKeyCode());
        }

        @Override
        public synchronized void keyReleased(KeyEvent event) {
            pressed.remove(event.getKeyCode());
        }

        synchronized VehicleControl currentControl() {
            boolean accelerate = pressed.contains(KeyEvent.VK_W) || pressed.contains(KeyEvent.VK_UP);
            boolean brakeKey = pressed.contains(KeyEvent.VK_S) || pressed.contains(KeyEvent.VK_DOWN);
            boolean left = pressed.contains(KeyEvent.VK_A) || pressed.contains(KeyEvent.VK_LEFT);
            boolean right = pressed.contains(KeyEvent.VK_D) || pressed.contains(KeyEvent.VK_RIGHT);
            boolean handBrake = pressed.contains(KeyEvent.VK_SPACE);

            float throttle = accelerate ? 0.65f : 0.0f;
            float brake = brakeKey ? 0.75f : 0.0f;
            float steer = 0.0f;
            if (left && !right) {
                steer = -0.45f;
            } else if (right && !left) {
                steer = 0.45f;
            }

            return new VehicleControl()
                .throttle(throttle)
                .steer(steer)
                .brake(brake)
                .handBrake(handBrake)
                .reverse(reverse);
        }
    }
}
