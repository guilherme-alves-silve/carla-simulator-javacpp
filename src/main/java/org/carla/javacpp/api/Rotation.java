package org.carla.javacpp.api;

import org.carla.javacpp.binding.CarlaNative;

/**
 * A rotation in 3D space, expressed in degrees using the CARLA
 * convention.
 *
 * <p>The rotation is applied in the order pitch ({@code x} axis),
 * then yaw ({@code z} axis), then roll ({@code y} axis). The
 * instance is immutable; use the canonical constructor to build a
 * new {@code Rotation}.</p>
 *
 * @param pitch rotation around the right axis, in degrees.
 * @param yaw   rotation around the up axis, in degrees.
 * @param roll  rotation around the forward axis, in degrees.
 * @see Transform
 * @see Location
 */
public record Rotation(double pitch, double yaw, double roll) {

    /**
     * Marshals this {@code Rotation} to its native counterpart for
     * use in JNI calls.
     *
     * @return a fresh native value object representing this
     *         rotation.
     */
    public CarlaNative.RotationValue toNative() {
        return new CarlaNative.RotationValue()
            .pitch(pitch)
            .yaw(yaw)
            .roll(roll);
    }

    /**
     * Builds a {@code Rotation} from a native value object
     * returned by the JNI layer.
     *
     * <p>Package-private: only the bridge uses this constructor.</p>
     *
     * @param value native value to convert; must be non-null.
     * @return a new {@code Rotation} with the same angles as
     *         {@code value}.
     */
    static Rotation fromNative(CarlaNative.RotationValue value) {
        return new Rotation(value.pitch(), value.yaw(), value.roll());
    }
}
