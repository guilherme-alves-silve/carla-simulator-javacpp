package org.carla.javacpp.api;

import org.carla.javacpp.binding.CarlaNative;

/**
 * A pose in the world: a {@link Location} and a {@link Rotation}.
 *
 * <p>This is the value type used everywhere a CARLA API needs a
 * spatial pose: spawn points, actor transforms, sensor attachment
 * points, and so on. Instances are immutable; use the canonical
 * constructor to build new ones.</p>
 *
 * @param location the world location of the pose.
 * @param rotation the rotation (pitch, yaw, roll) of the pose.
 * @see Location
 * @see Rotation
 */
public record Transform(Location location, Rotation rotation) {

    /**
     * Marshals this {@code Transform} to its native counterpart for
     * use in JNI calls.
     *
     * @return a fresh native value object representing this pose.
     */
    public CarlaNative.TransformValue toNative() {
        return new CarlaNative.TransformValue()
            .location(location.toNative())
            .rotation(rotation.toNative());
    }

    /**
     * Builds a {@code Transform} from a native value object
     * returned by the JNI layer.
     *
     * <p>Package-private: only the bridge uses this constructor.</p>
     *
     * @param value native value to convert; must be non-null.
     * @return a new {@code Transform} with the same location and
     *         rotation as {@code value}.
     */
    static Transform fromNative(CarlaNative.TransformValue value) {
        return new Transform(
            Location.fromNative(value.location()),
            Rotation.fromNative(value.rotation()));
    }
}
