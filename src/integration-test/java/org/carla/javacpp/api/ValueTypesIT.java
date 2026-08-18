package org.carla.javacpp.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Native-bridge round-trip tests for the value types. These tests
 * load the {@code jniCarlaNative} shared library through JavaCPP
 * and therefore require it to be on the classpath. The library is
 * built by the {@code native} Maven profile; the tests are wired
 * in by the {@code integration-tests} profile, so the canonical
 * invocation is:
 *
 * <pre>{@code
 * mvn -Pnative -Pintegration-tests verify
 * }</pre>
 *
 * Unlike the full {@code CarlaConnectionIT} / {@code CarlaSpawnSensorIT}
 * tests, this class does not need a running CARLA simulator; the
 * value types are pure POD marshalling and exercise the JNI glue
 * in isolation.
 */
final class ValueTypesIT {
    private static final double LOCATION_X = 1.0;
    private static final double LOCATION_Y = 2.0;
    private static final double LOCATION_Z = 3.0;
    private static final double ROTATION_PITCH = 4.0;
    private static final double ROTATION_YAW = 5.0;
    private static final double ROTATION_ROLL = 6.0;
    private static final double NEGATIVE_LOCATION_X = -1.5;
    private static final double DECIMAL_LOCATION_Y = 2.25;
    private static final double DECIMAL_LOCATION_Z = 3.75;
    private static final double NEGATIVE_ROTATION_PITCH = -10.0;
    private static final double DECIMAL_ROTATION_YAW = 90.5;
    private static final double HALF_TURN_ROLL = 180.0;
    private static final double NEGATIVE_TRANSFORM_LOCATION_X = -1.0;
    private static final double NEGATIVE_TRANSFORM_ROTATION_YAW = -5.0;

    @Test
    void locationRoundTripsThroughNativeValue() {
        var location = new Location(NEGATIVE_LOCATION_X, DECIMAL_LOCATION_Y, DECIMAL_LOCATION_Z);

        var copy = Location.fromNative(location.toNative());

        assertEquals(location, copy);
    }

    @Test
    void rotationRoundTripsThroughNativeValue() {
        var rotation = new Rotation(NEGATIVE_ROTATION_PITCH, DECIMAL_ROTATION_YAW, HALF_TURN_ROLL);

        var copy = Rotation.fromNative(rotation.toNative());

        assertEquals(rotation, copy);
    }

    @Test
    void transformRoundTripsThroughNativeValue() {
        var transform = new Transform(
            new Location(NEGATIVE_TRANSFORM_LOCATION_X, LOCATION_Y, LOCATION_Z),
            new Rotation(ROTATION_PITCH, NEGATIVE_TRANSFORM_ROTATION_YAW, ROTATION_ROLL));

        var copy = Transform.fromNative(transform.toNative());

        assertEquals(transform, copy);
    }
}
