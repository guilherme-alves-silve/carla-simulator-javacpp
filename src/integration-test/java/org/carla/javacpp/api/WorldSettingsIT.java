package org.carla.javacpp.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Native-bridge round-trip tests for {@link WorldSettings}. Runs
 * the JavaCPP {@code Loader.load()} under the hood, so the
 * {@code jniCarlaNative} shared library must be on the classpath.
 * Build it with the {@code native} profile and enable the
 * integration-test source set with the {@code integration-tests}
 * profile:
 *
 * <pre>{@code
 * mvn -Pnative -Pintegration-tests verify
 * }</pre>
 *
 * No running CARLA simulator is required for this class.
 */
final class WorldSettingsIT {
    private static final double FIXED_DELTA_SECONDS = 0.05;

    @Test
    void convertsToAndFromNativeSettings() {
        var settings = new WorldSettings(true, false, FIXED_DELTA_SECONDS);
        var copy = WorldSettings.fromNative(settings.toNative());

        assertTrue(copy.synchronousMode());
        assertFalse(copy.noRenderingMode());
        assertEquals(FIXED_DELTA_SECONDS, copy.fixedDeltaSeconds());
    }

    @Test
    void nullFixedDeltaDisablesNativeOptional() {
        var settings = new WorldSettings(false, true, null);
        var copy = WorldSettings.fromNative(settings.toNative());

        assertFalse(copy.synchronousMode());
        assertTrue(copy.noRenderingMode());
        assertNull(copy.fixedDeltaSeconds());
    }
}
