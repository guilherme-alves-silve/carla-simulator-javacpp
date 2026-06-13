package org.carla.javacpp.api;

import org.carla.javacpp.binding.CarlaNative;

public class Actor extends NativeHandle<CarlaNative.ActorHandle> {
    Actor(CarlaNative.ActorHandle handle) {
        super(handle);
    }

    public long getId() {
        return handle().GetId();
    }

    public String getTypeId() {
        return handle().GetTypeId();
    }

    public Transform getTransform() {
        return Transform.fromNative(handle().GetTransform());
    }

    public boolean destroy() {
        return handle().Destroy();
    }

    public void applyVehicleControl(float throttle, float steer, float brake, boolean handBrake, boolean reverse) {
        handle().ApplyVehicleControl(throttle, steer, brake, handBrake, reverse);
    }

    public void applyVehicleControl(VehicleControl control) {
        handle().ApplyVehicleControl(
            control.throttle(),
            control.steer(),
            control.brake(),
            control.handBrake(),
            control.reverse());
    }

    @Override
    protected void release(CarlaNative.ActorHandle handle) {
        CarlaNative.DeleteActorHandle(handle);
    }
}
