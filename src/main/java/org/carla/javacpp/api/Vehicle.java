package org.carla.javacpp.api;

import org.carla.javacpp.binding.CarlaNative;

/**
 * A vehicle actor: a four-wheeled agent that can be driven manually
 * or handed off to CARLA's traffic manager.
 *
 * <p>This is a typed view over {@link Actor}; the underlying native
 * handle is the same, but {@code Vehicle} exposes vehicle-specific
 * operations such as autopilot control.</p>
 *
 * @see World#spawnVehicle(Blueprint, Transform)
 * @see World#trySpawnVehicle(Blueprint, Transform)
 */
public final class Vehicle extends Actor {

    Vehicle(CarlaNative.ActorHandle handle) {
        super(handle);
    }

    /**
     * Sends a vehicle control command to this vehicle.
     *
     * <p>The control values are taken from {@code control.throttle()},
     * {@code control.steer()}, {@code control.brake()},
     * {@code control.handBrake()} and {@code control.reverse()}, each
     * already clamped to their valid range by
     * {@link VehicleControl}.</p>
     *
     * @param control structured vehicle control; must be non-null.
     * @throws CarlaException          if the vehicle is already
     *                                  closed.
     * @throws IllegalArgumentException if {@code control} is
     *                                  {@code null}.
     */
    public void applyControl(VehicleControl control) {
        handle().ApplyVehicleControl(
            control.throttle(),
            control.steer(),
            control.brake(),
            control.handBrake(),
            control.reverse());
    }

    /**
     * Toggles CARLA's built-in traffic manager, using the default
     * port {@code 8000}.
     *
     * <p>When autopilot is enabled, the vehicle follows the traffic
     * manager's planned route and obeys traffic rules. User-supplied
     * {@link #applyControl(VehicleControl)} commands are ignored as
     * long as autopilot is on.</p>
     *
     * @param enabled when {@code true}, autopilot is engaged; when
     *                {@code false}, it is disengaged and the vehicle
     *                can be controlled manually again.
     * @throws CarlaException if the vehicle is already closed.
     * @see #setAutopilot(boolean, int)
     */
    public void setAutopilot(boolean enabled) {
        setAutopilot(enabled, 8000);
    }

    /**
     * Toggles CARLA's built-in traffic manager, talking to it on a
     * custom port.
     *
     * @param enabled             when {@code true}, autopilot is
     *                            engaged.
     * @param trafficManagerPort  UDP port the traffic manager is
     *                            listening on. The default CARLA
     *                            installation uses port {@code 8000}.
     * @throws CarlaException if the vehicle is already closed or the
     *                        traffic manager cannot be reached on the
     *                        given port.
     */
    public void setAutopilot(boolean enabled, int trafficManagerPort) {
        handle().SetAutopilot(enabled, trafficManagerPort);
    }
}
