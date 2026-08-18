package org.carla.javacpp.api;

/**
 * Mutable builder for a single vehicle control command.
 *
 * <p>Each value is automatically clamped to its valid range when
 * set, so a partially-initialised builder is always safe to send
 * to {@link Vehicle#applyControl(VehicleControl)}.</p>
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 * var control = new VehicleControl()
 *     .throttle(0.6f)
 *     .steer(-0.1f)
 *     .brake(0.0f);
 * vehicle.applyControl(control);
 * }</pre>
 */
public final class VehicleControl {

    private float throttle;
    private float steer;
    private float brake;
    private boolean handBrake;
    private boolean reverse;

    /**
     * Creates a vehicle control with every field set to its neutral
     * default (zero throttle, zero steer, zero brake, no hand brake,
     * no reverse).
     */
    public VehicleControl() {
    }

    /**
     * Returns the current throttle value.
     *
     * @return throttle in {@code [0.0, 1.0]}.
     */
    public float throttle() {
        return throttle;
    }

    /**
     * Sets the throttle, clamped to {@code [0.0, 1.0]}.
     *
     * @param value desired throttle; values outside the range are
     *              clamped.
     * @return this {@code VehicleControl}, for fluent chaining.
     */
    public VehicleControl throttle(float value) {
        throttle = clamp(value, 0.0f, 1.0f);
        return this;
    }

    /**
     * Returns the current steering value.
     *
     * @return steer in {@code [-1.0, 1.0]}.
     */
    public float steer() {
        return steer;
    }

    /**
     * Sets the steering, clamped to {@code [-1.0, 1.0]} where
     * {@code -1.0} is full left and {@code 1.0} is full right.
     *
     * @param value desired steering; values outside the range are
     *              clamped.
     * @return this {@code VehicleControl}, for fluent chaining.
     */
    public VehicleControl steer(float value) {
        steer = clamp(value, -1.0f, 1.0f);
        return this;
    }

    /**
     * Returns the current brake value.
     *
     * @return brake in {@code [0.0, 1.0]}.
     */
    public float brake() {
        return brake;
    }

    /**
     * Sets the brake, clamped to {@code [0.0, 1.0]}.
     *
     * @param value desired brake; values outside the range are
     *              clamped.
     * @return this {@code VehicleControl}, for fluent chaining.
     */
    public VehicleControl brake(float value) {
        brake = clamp(value, 0.0f, 1.0f);
        return this;
    }

    /**
     * Returns whether the hand brake is engaged.
     *
     * @return {@code true} when the hand brake is engaged.
     */
    public boolean handBrake() {
        return handBrake;
    }

    /**
     * Sets the hand brake flag.
     *
     * @param value when {@code true}, the hand brake is engaged.
     * @return this {@code VehicleControl}, for fluent chaining.
     */
    public VehicleControl handBrake(boolean value) {
        handBrake = value;
        return this;
    }

    /**
     * Returns whether the gearbox is in reverse.
     *
     * @return {@code true} when reverse gear is selected.
     */
    public boolean reverse() {
        return reverse;
    }

    /**
     * Sets the reverse-gear flag.
     *
     * @param value when {@code true}, reverse gear is selected.
     * @return this {@code VehicleControl}, for fluent chaining.
     */
    public VehicleControl reverse(boolean value) {
        reverse = value;
        return this;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
