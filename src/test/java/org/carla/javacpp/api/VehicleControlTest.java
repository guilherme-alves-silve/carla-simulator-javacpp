package org.carla.javacpp.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class VehicleControlTest {
    private static final float REQUESTED_THROTTLE = 0.5f;
    private static final float REQUESTED_STEER = -0.25f;
    private static final float REQUESTED_BRAKE = 0.75f;
    private static final float ABOVE_MAX_CONTROL_INPUT = 2.0f;
    private static final float BELOW_MIN_CONTROL_INPUT = -2.0f;
    private static final float FULL_CONTROL_INPUT = 1.0f;
    private static final float FULL_LEFT_STEER = -1.0f;
    private static final float ZERO_CONTROL_INPUT = 0.0f;

    @Test
    void defaultsMatchCarlaNeutralControl() {
        var control = new VehicleControl();

        assertEquals(ZERO_CONTROL_INPUT, control.throttle());
        assertEquals(ZERO_CONTROL_INPUT, control.steer());
        assertEquals(ZERO_CONTROL_INPUT, control.brake());
        assertFalse(control.handBrake());
        assertFalse(control.reverse());
    }

    @Test
    void settersAreFluentAndStoreValues() {
        var control = new VehicleControl();

        var returned = control
            .throttle(REQUESTED_THROTTLE)
            .steer(REQUESTED_STEER)
            .brake(REQUESTED_BRAKE)
            .handBrake(true)
            .reverse(true);

        assertSame(control, returned);
        assertEquals(REQUESTED_THROTTLE, control.throttle());
        assertEquals(REQUESTED_STEER, control.steer());
        assertEquals(REQUESTED_BRAKE, control.brake());
        assertTrue(control.handBrake());
        assertTrue(control.reverse());
    }

    @Test
    void clampsContinuousControlValues() {
        var control = new VehicleControl()
            .throttle(ABOVE_MAX_CONTROL_INPUT)
            .steer(BELOW_MIN_CONTROL_INPUT)
            .brake(BELOW_MIN_CONTROL_INPUT);

        assertEquals(FULL_CONTROL_INPUT, control.throttle());
        assertEquals(FULL_LEFT_STEER, control.steer());
        assertEquals(ZERO_CONTROL_INPUT, control.brake());
    }
}
