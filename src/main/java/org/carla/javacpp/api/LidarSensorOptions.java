package org.carla.javacpp.api;

/**
 * Configuration for a {@link LidarSensor}.
 *
 * <p>The options are validated eagerly by the canonical record
 * constructor: every numeric field except the field-of-view angles
 * must be strictly positive. Use {@link #defaults()} as a starting
 * point for custom configurations.</p>
 *
 * @param channels          number of vertical layers the LIDAR
 *                          fires. Must be strictly positive.
 * @param range             maximum detection distance, in meters.
 *                          Must be strictly positive.
 * @param pointsPerSecond   target number of points per second
 *                          emitted by the sensor. Must be strictly
 *                          positive.
 * @param rotationFrequency rotation speed of the sensor, in
 *                          hertz. Must be strictly positive.
 * @param upperFov          upper field-of-view bound, in degrees.
 * @param lowerFov          lower field-of-view bound, in degrees.
 */
public record LidarSensorOptions(
    int channels,
    double range,
    int pointsPerSecond,
    double rotationFrequency,
    double upperFov,
    double lowerFov
) {

    /**
     * Canonical constructor with eager validation.
     *
     * @throws IllegalArgumentException if any of {@code channels},
     *                                  {@code range},
     *                                  {@code pointsPerSecond} or
     *                                  {@code rotationFrequency} is
     *                                  non-positive.
     */
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

    /**
     * Returns a configuration that roughly matches CARLA's default
     * LIDAR sensor.
     *
     * <p>Concretely: 32 channels, 50 m range, 56 000 points per
     * second, 10 Hz rotation, an upper FOV of 10 degrees and a
     * lower FOV of {@code -30} degrees.</p>
     *
     * @return a fresh default {@code LidarSensorOptions}.
     */
    public static LidarSensorOptions defaults() {
        return new LidarSensorOptions(32, 50.0, 56_000, 10.0, 10.0, -30.0);
    }
}
