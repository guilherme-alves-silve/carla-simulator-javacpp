package org.carla.javacpp.api;

import org.carla.javacpp.binding.CarlaNative;

public record Location(double x, double y, double z) {
    public CarlaNative.LocationValue toNative() {
        return new CarlaNative.LocationValue()
            .x(x)
            .y(y)
            .z(z);
    }

    static Location fromNative(CarlaNative.LocationValue value) {
        return new Location(value.x(), value.y(), value.z());
    }
}
