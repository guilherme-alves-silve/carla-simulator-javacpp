package org.carla.javacpp.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class CollisionEventTest {
    private static final long FRAME = 5;
    private static final double TIMESTAMP = 0.5;
    private static final long ACTOR_ID = 10;
    private static final long OTHER_ACTOR_ID = 20;
    private static final String OTHER_ACTOR_TYPE_ID = "vehicle.audi.a2";
    private static final double NORMAL_IMPULSE_X = 3.0;
    private static final double NORMAL_IMPULSE_Y = 4.0;
    private static final double NORMAL_IMPULSE_Z = 12.0;
    private static final double EXPECTED_NORMAL_IMPULSE_LENGTH = 13.0;

    @Test
    void computesNormalImpulseLength() {
        var event = new CollisionEvent(
            FRAME,
            TIMESTAMP,
            ACTOR_ID,
            OTHER_ACTOR_ID,
            OTHER_ACTOR_TYPE_ID,
            NORMAL_IMPULSE_X,
            NORMAL_IMPULSE_Y,
            NORMAL_IMPULSE_Z);

        assertEquals(EXPECTED_NORMAL_IMPULSE_LENGTH, event.normalImpulseLength());
    }
}
