package org.carla.javacpp.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ValueTypesTest {
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
    void valueObjectsExposePythonLikeFields() {
        var transform = new Transform(
            new Location(LOCATION_X, LOCATION_Y, LOCATION_Z),
            new Rotation(ROTATION_PITCH, ROTATION_YAW, ROTATION_ROLL));

        assertEquals(LOCATION_X, transform.location().x());
        assertEquals(ROTATION_YAW, transform.rotation().yaw());
    }

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
