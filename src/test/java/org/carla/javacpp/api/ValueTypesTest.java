package org.carla.javacpp.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ValueTypesTest {
    @Test
    void valueObjectsExposePythonLikeFields() {
        Transform transform = new Transform(
            new Location(1.0, 2.0, 3.0),
            new Rotation(4.0, 5.0, 6.0));

        assertEquals(1.0, transform.location().x());
        assertEquals(5.0, transform.rotation().yaw());
    }
}
