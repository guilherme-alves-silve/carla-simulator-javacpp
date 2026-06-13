package org.carla.javacpp.api;

import org.carla.javacpp.binding.CarlaNative;

public record Transform(Location location, Rotation rotation) {
    public CarlaNative.TransformValue toNative() {
        return new CarlaNative.TransformValue()
            .location(location.toNative())
            .rotation(rotation.toNative());
    }

    static Transform fromNative(CarlaNative.TransformValue value) {
        return new Transform(
            Location.fromNative(value.location()),
            Rotation.fromNative(value.rotation()));
    }
}
