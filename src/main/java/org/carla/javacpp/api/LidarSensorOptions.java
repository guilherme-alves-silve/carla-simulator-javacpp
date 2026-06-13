package org.carla.javacpp.api;

public record LidarSensorOptions(
    int channels,
    double range,
    int pointsPerSecond,
    double rotationFrequency,
    double upperFov,
    double lowerFov
) {
    public LidarSensorOptions {
        if (channels <= 0) {
            throw new IllegalArgumentException("channels must be positive");
        }
        if (range <= 0.0) {
            throw new IllegalArgumentException("range must be positive");
        }
        if (pointsPerSecond <= 0) {
            throw new IllegalArgumentException("pointsPerSecond must be positive");
        }
        if (rotationFrequency <= 0.0) {
            throw new IllegalArgumentException("rotationFrequency must be positive");
        }
    }

    public static LidarSensorOptions defaults() {
        return new LidarSensorOptions(32, 50.0, 56_000, 10.0, 10.0, -30.0);
    }
}
