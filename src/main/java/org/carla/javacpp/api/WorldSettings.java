package org.carla.javacpp.api;

import org.carla.javacpp.binding.CarlaNative;

/**
 * Mutable description of the server-side simulation settings.
 *
 * <p>Three flags are exposed:</p>
 * <ul>
 *     <li>{@link #synchronousMode()} — when {@code true}, the
 *         simulation only advances when {@link World#tick()} (or
 *         {@link World#tick(long)}) is called. Useful for
 *         deterministic, step-by-step execution.</li>
 *     <li>{@link #noRenderingMode()} — when {@code true}, the
 *         server skips rendering work, reducing CPU/GPU usage at
 *         the cost of no images being produced.</li>
 *     <li>{@link #fixedDeltaSeconds()} — when non-null and
 *         positive, the simulation advances by a fixed amount of
 *         simulated time per tick instead of using real wall-clock
 *         time.</li>
 * </ul>
 *
 * <p>The class is an immutable record; use the
 * {@code with*(...)} copy builders to derive modified settings,
 * then push them with {@link World#applySettings(WorldSettings)} or
 * {@link World#applySettings(WorldSettings, long)}.</p>
 *
 * @see World#getSettings()
 * @see World#applySettings(WorldSettings)
 */
public record WorldSettings(
    boolean synchronousMode,
    boolean noRenderingMode,
    Double fixedDeltaSeconds
) {

    /**
     * Marshals these settings to their native counterpart for use in
     * JNI calls.
     *
     * @return a fresh native value object representing these
     *         settings.
     */
    public CarlaNative.WorldSettingsValue toNative() {
        var value = new CarlaNative.WorldSettingsValue()
            .synchronous_mode(synchronousMode)
            .no_rendering_mode(noRenderingMode);
        if (fixedDeltaSeconds != null && fixedDeltaSeconds > 0.0) {
            value.has_fixed_delta_seconds(true);
            value.fixed_delta_seconds(fixedDeltaSeconds);
        } else {
            value.has_fixed_delta_seconds(false);
            value.fixed_delta_seconds(0.0);
        }
        return value;
    }

    /**
     * Builds a {@code WorldSettings} from a native value object
     * returned by the JNI layer.
     *
     * <p>Package-private: only the bridge uses this constructor.</p>
     *
     * @param value native value to convert; must be non-null.
     * @return a new {@code WorldSettings} with the same flags.
     */
    static WorldSettings fromNative(CarlaNative.WorldSettingsValue value) {
        return new WorldSettings(
            value.synchronous_mode(),
            value.no_rendering_mode(),
            value.has_fixed_delta_seconds() ? value.fixed_delta_seconds() : null);
    }

    /**
     * Returns a copy of these settings with the synchronous-mode
     * flag replaced.
     *
     * @param value new value for the synchronous-mode flag.
     * @return a new {@code WorldSettings}.
     */
    public WorldSettings synchronousMode(boolean value) {
        return new WorldSettings(value, noRenderingMode, fixedDeltaSeconds);
    }

    /**
     * Returns a copy of these settings with the no-rendering flag
     * replaced.
     *
     * @param value new value for the no-rendering flag.
     * @return a new {@code WorldSettings}.
     */
    public WorldSettings noRenderingMode(boolean value) {
        return new WorldSettings(synchronousMode, value, fixedDeltaSeconds);
    }

    /**
     * Returns a copy of these settings with the fixed delta seconds
     * replaced.
     *
     * <p>Pass {@code null} (or any non-positive value) to clear the
     * fixed delta and fall back to wall-clock based simulation.</p>
     *
     * @param value new value for the fixed delta seconds, in
     *              seconds, or {@code null} to clear.
     * @return a new {@code WorldSettings}.
     */
    public WorldSettings fixedDeltaSeconds(Double value) {
        return new WorldSettings(synchronousMode, noRenderingMode, value);
    }
}
