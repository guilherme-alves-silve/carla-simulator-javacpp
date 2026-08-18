package org.carla.javacpp.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Pure Java unit tests for the value types. Tests that round-trip
 * through the native bridge (and therefore require
 * {@code jniCarlaNative} to be on the classpath) live in
 * {@code ValueTypesIT} under {@code src/integration-test/java}.
 */
class ValueTypesTest {
    private static final double LOCATION_X = 1.0;
    private static final double LOCATION_Y = 2.0;
    private static final double LOCATION_Z = 3.0;
    private static final double ROTATION_PITCH = 4.0;
    private static final double ROTATION_YAW = 5.0;
    private static final double ROTATION_ROLL = 6.0;

    @Test
    void valueObjectsExposePythonLikeFields() {
        var transform = new Transform(
            new Location(LOCATION_X, LOCATION_Y, LOCATION_Z),
            new Rotation(ROTATION_PITCH, ROTATION_YAW, ROTATION_ROLL));

        assertEquals(LOCATION_X, transform.location().x());
        assertEquals(ROTATION_YAW, transform.rotation().yaw());
    }
}
