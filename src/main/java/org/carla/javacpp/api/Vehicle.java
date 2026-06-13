package org.carla.javacpp.api;

import org.carla.javacpp.binding.CarlaNative;

public final class Vehicle extends Actor {
    Vehicle(CarlaNative.ActorHandle handle) {
        super(handle);
    }

    public void applyControl(VehicleControl control) {
        handle().ApplyVehicleControl(
            control.throttle(),
            control.steer(),
            control.brake(),
            control.handBrake(),
            control.reverse());
    }

    public void setAutopilot(boolean enabled) {
        setAutopilot(enabled, 8000);
    }

    public void setAutopilot(boolean enabled, int trafficManagerPort) {
        handle().SetAutopilot(enabled, trafficManagerPort);
    }
}
