package org.carla.javacpp.api;

public final class VehicleControl {
    private float throttle;
    private float steer;
    private float brake;
    private boolean handBrake;
    private boolean reverse;

    public float throttle() {
        return throttle;
    }

    public VehicleControl throttle(float value) {
        throttle = clamp(value, 0.0f, 1.0f);
        return this;
    }

    public float steer() {
        return steer;
    }

    public VehicleControl steer(float value) {
        steer = clamp(value, -1.0f, 1.0f);
        return this;
    }

    public float brake() {
        return brake;
    }

    public VehicleControl brake(float value) {
        brake = clamp(value, 0.0f, 1.0f);
        return this;
    }

    public boolean handBrake() {
        return handBrake;
    }

    public VehicleControl handBrake(boolean value) {
        handBrake = value;
        return this;
    }

    public boolean reverse() {
        return reverse;
    }

    public VehicleControl reverse(boolean value) {
        reverse = value;
        return this;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
