package org.carla.javacpp.api;

import org.carla.javacpp.binding.CarlaNative;

public record Rotation(double pitch, double yaw, double roll) {
    public CarlaNative.RotationValue toNative() {
        return new CarlaNative.RotationValue()
            .pitch(pitch)
            .yaw(yaw)
            .roll(roll);
    }

    static Rotation fromNative(CarlaNative.RotationValue value) {
        return new Rotation(value.pitch(), value.yaw(), value.roll());
    }
}
