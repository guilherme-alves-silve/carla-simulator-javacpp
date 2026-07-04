package org.carla.javacpp.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

final class LidarSensorOptionsTest {
    private static final int DEFAULT_CHANNELS = 32;
    private static final double DEFAULT_RANGE_METERS = 50.0;
    private static final int DEFAULT_POINTS_PER_SECOND = 56_000;
    private static final double DEFAULT_ROTATION_FREQUENCY = 10.0;
    private static final double DEFAULT_UPPER_FOV = 10.0;
    private static final double DEFAULT_LOWER_FOV = -30.0;
    private static final int CUSTOM_CHANNELS = 64;
    private static final double CUSTOM_RANGE_METERS = 120.0;
    private static final int CUSTOM_POINTS_PER_SECOND = 100_000;
    private static final double CUSTOM_ROTATION_FREQUENCY = 20.0;
    private static final double CUSTOM_UPPER_FOV = 15.0;
    private static final double CUSTOM_LOWER_FOV = -25.0;
    private static final int INVALID_ZERO_CHANNELS = 0;
    private static final double INVALID_ZERO_RANGE = 0.0;
    private static final int INVALID_ZERO_POINTS_PER_SECOND = 0;
    private static final double INVALID_ZERO_ROTATION_FREQUENCY = 0.0;
    private static final int MIN_VALID_CHANNELS = 1;
    private static final int MIN_VALID_POINTS_PER_SECOND = 1;
    private static final double MIN_VALID_ROTATION_FREQUENCY = 1.0;

    @Test
    void defaultsMatchExpectedSensorConfiguration() {
        var options = LidarSensorOptions.defaults();

        assertEquals(DEFAULT_CHANNELS, options.channels());
        assertEquals(DEFAULT_RANGE_METERS, options.range());
        assertEquals(DEFAULT_POINTS_PER_SECOND, options.pointsPerSecond());
        assertEquals(DEFAULT_ROTATION_FREQUENCY, options.rotationFrequency());
        assertEquals(DEFAULT_UPPER_FOV, options.upperFov());
        assertEquals(DEFAULT_LOWER_FOV, options.lowerFov());
    }

    @Test
    void storesCustomConfiguration() {
        var options = new LidarSensorOptions(
            CUSTOM_CHANNELS,
            CUSTOM_RANGE_METERS,
            CUSTOM_POINTS_PER_SECOND,
            CUSTOM_ROTATION_FREQUENCY,
            CUSTOM_UPPER_FOV,
            CUSTOM_LOWER_FOV);

        assertEquals(CUSTOM_CHANNELS, options.channels());
        assertEquals(CUSTOM_RANGE_METERS, options.range());
        assertEquals(CUSTOM_POINTS_PER_SECOND, options.pointsPerSecond());
        assertEquals(CUSTOM_ROTATION_FREQUENCY, options.rotationFrequency());
        assertEquals(CUSTOM_UPPER_FOV, options.upperFov());
        assertEquals(CUSTOM_LOWER_FOV, options.lowerFov());
    }

    @Test
    void rejectsNonPositiveRequiredValues() {
        assertThrows(IllegalArgumentException.class, () -> new LidarSensorOptions(
            INVALID_ZERO_CHANNELS,
            DEFAULT_RANGE_METERS,
            MIN_VALID_POINTS_PER_SECOND,
            MIN_VALID_ROTATION_FREQUENCY,
            DEFAULT_UPPER_FOV,
            DEFAULT_LOWER_FOV));
        assertThrows(IllegalArgumentException.class, () -> new LidarSensorOptions(
            MIN_VALID_CHANNELS,
            INVALID_ZERO_RANGE,
            MIN_VALID_POINTS_PER_SECOND,
            MIN_VALID_ROTATION_FREQUENCY,
            DEFAULT_UPPER_FOV,
            DEFAULT_LOWER_FOV));
        assertThrows(IllegalArgumentException.class, () -> new LidarSensorOptions(
            MIN_VALID_CHANNELS,
            DEFAULT_RANGE_METERS,
            INVALID_ZERO_POINTS_PER_SECOND,
            MIN_VALID_ROTATION_FREQUENCY,
            DEFAULT_UPPER_FOV,
            DEFAULT_LOWER_FOV));
        assertThrows(IllegalArgumentException.class, () -> new LidarSensorOptions(
            MIN_VALID_CHANNELS,
            DEFAULT_RANGE_METERS,
            MIN_VALID_POINTS_PER_SECOND,
            INVALID_ZERO_ROTATION_FREQUENCY,
            DEFAULT_UPPER_FOV,
            DEFAULT_LOWER_FOV));
    }
}
