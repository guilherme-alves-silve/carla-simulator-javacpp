package org.carla.javacpp.api;

import org.carla.javacpp.binding.CarlaNative;

public record WorldSettings(
    boolean synchronousMode,
    boolean noRenderingMode,
    Double fixedDeltaSeconds
) {
    public CarlaNative.WorldSettingsValue toNative() {
        CarlaNative.WorldSettingsValue value = new CarlaNative.WorldSettingsValue()
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

    static WorldSettings fromNative(CarlaNative.WorldSettingsValue value) {
        return new WorldSettings(
            value.synchronous_mode(),
            value.no_rendering_mode(),
            value.has_fixed_delta_seconds() ? value.fixed_delta_seconds() : null);
    }

    public WorldSettings synchronousMode(boolean value) {
        return new WorldSettings(value, noRenderingMode, fixedDeltaSeconds);
    }

    public WorldSettings noRenderingMode(boolean value) {
        return new WorldSettings(synchronousMode, value, fixedDeltaSeconds);
    }

    public WorldSettings fixedDeltaSeconds(Double value) {
        return new WorldSettings(synchronousMode, noRenderingMode, value);
    }
}
