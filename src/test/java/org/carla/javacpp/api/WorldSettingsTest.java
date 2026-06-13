package org.carla.javacpp.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class WorldSettingsTest {
    @Test
    void convertsToAndFromNativeSettings() {
        WorldSettings settings = new WorldSettings(true, false, 0.05);
        WorldSettings copy = WorldSettings.fromNative(settings.toNative());

        assertTrue(copy.synchronousMode());
        assertFalse(copy.noRenderingMode());
        assertEquals(0.05, copy.fixedDeltaSeconds());
    }

    @Test
    void nullFixedDeltaDisablesNativeOptional() {
        WorldSettings settings = new WorldSettings(false, true, null);
        WorldSettings copy = WorldSettings.fromNative(settings.toNative());

        assertFalse(copy.synchronousMode());
        assertTrue(copy.noRenderingMode());
        assertNull(copy.fixedDeltaSeconds());
    }
}
