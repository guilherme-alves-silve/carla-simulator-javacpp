package org.carla.javacpp.api;

import org.carla.javacpp.binding.CarlaNative;

/**
 * A point in 3D world space, expressed in meters using the
 * CARLA left-handed coordinate system.
 *
 * <p>The {@code x} axis points forward, the {@code y} axis points
 * right, and the {@code z} axis points up. The instance is
 * immutable; use the canonical constructor to build a new
 * {@code Location}.</p>
 *
 * @param x forward distance, in meters.
 * @param y right distance, in meters.
 * @param z up distance, in meters.
 * @see Transform
 * @see Rotation
 */
public record Location(double x, double y, double z) {

    /**
     * Marshals this {@code Location} to its native counterpart for
     * use in JNI calls.
     *
     * @return a fresh native value object representing this point.
     */
    public CarlaNative.LocationValue toNative() {
        return new CarlaNative.LocationValue()
            .x(x)
            .y(y)
            .z(z);
    }

    /**
     * Builds a {@code Location} from a native value object
     * returned by the JNI layer.
     *
     * <p>Package-private: only the bridge uses this constructor.</p>
     *
     * @param value native value to convert; must be non-null.
     * @return a new {@code Location} with the same coordinates as
     *         {@code value}.
     */
    static Location fromNative(CarlaNative.LocationValue value) {
        return new Location(value.x(), value.y(), value.z());
    }
}
