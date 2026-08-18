package org.carla.javacpp.api;

import org.carla.javacpp.binding.CarlaNative;

/**
 * Represents any actor currently alive in the simulation: a vehicle,
 * a walker, a sensor, a traffic light, and so on.
 *
 * <p>{@code Actor} is the most general wrapper over a CARLA native
 * actor. Subclasses such as {@link Vehicle} expose actor-type
 * specific operations. The class is {@link AutoCloseable}; the
 * native actor is destroyed automatically on close, which makes it
 * convenient to use in try-with-resources blocks.</p>
 *
 * @see Vehicle
 * @see World#spawnActor(Blueprint, Transform)
 */
public class Actor extends NativeHandle<CarlaNative.ActorHandle> {

    Actor(CarlaNative.ActorHandle handle) {
        super(handle);
    }

    /**
     * Returns the unique numeric id assigned by the server to this
     * actor. The id is unique within the current simulation run and
     * can be used to refer to the actor from other APIs.
     *
     * @return the actor id; non-negative.
     * @throws CarlaException if the actor is already closed.
     */
    public long getId() {
        return handle().GetId();
    }

    /**
     * Returns the type id of the blueprint used to spawn this actor
     * (for example {@code "vehicle.tesla.model3"} or
     * {@code "sensor.camera.rgb"}).
     *
     * @return the blueprint type id.
     * @throws CarlaException if the actor is already closed.
     */
    public String getTypeId() {
        return handle().GetTypeId();
    }

    /**
     * Returns the current world transform of the actor.
     *
     * @return the actor's current location and rotation.
     * @throws CarlaException if the actor is already closed.
     */
    public Transform getTransform() {
        return Transform.fromNative(handle().GetTransform());
    }

    /**
     * Returns the current world location of the actor.
     *
     * <p>This is a convenience shortcut for
     * {@code getTransform().location()}; both calls perform a single
     * round-trip to the server, but this method lets the caller stay
     * focused on the spatial component.</p>
     *
     * @return the actor's current location.
     * @throws CarlaException if the actor is already closed.
     */
    public Location getLocation() {
        return getTransform().location();
    }

    /**
     * Destroys the actor on the server, releasing every native
     * resource tied to it (including attached sensors).
     *
     * <p>After this call returns, the actor is invalid and any
     * subsequent method invocation throws
     * {@link CarlaException}. It is safe to call {@link #close()}
     * afterward to release the Java-side handle, but the call is
     * idempotent: closing an already destroyed actor does not throw
     * a second time.</p>
     *
     * @return {@code true} if the server confirmed the destruction,
     *         {@code false} if the actor was already invalid on the
     *         server side.
     * @throws CarlaException if the actor is already closed on the
     *                        Java side.
     */
    public boolean destroy() {
        return handle().Destroy();
    }

    /**
     * Sends a low-level vehicle control command directly to this
     * actor, bypassing the {@link Vehicle} wrapper.
     *
     * <p>Use this overload when the caller holds an {@code Actor}
     * reference (for example, after querying the world with
     * {@link World#getActors()}) and does not want to cast to
     * {@link Vehicle} just to drive the simulation.</p>
     *
     * @param throttle  accelerator pedal position, in {@code [0.0, 1.0]}.
     * @param steer     steering wheel position, in {@code [-1.0, 1.0]}
     *                  where {@code -1.0} is full left and
     *                  {@code 1.0} is full right.
     * @param brake     brake pedal position, in {@code [0.0, 1.0]}.
     * @param handBrake when {@code true}, the hand brake is engaged.
     * @param reverse   when {@code true}, the gearbox is in reverse.
     * @throws CarlaException if the actor is already closed or is not
     *                        a vehicle on the server side.
     */
    public void applyVehicleControl(float throttle, float steer, float brake, boolean handBrake, boolean reverse) {
        handle().ApplyVehicleControl(throttle, steer, brake, handBrake, reverse);
    }

    /**
     * Sends a vehicle control command built from a
     * {@link VehicleControl} builder.
     *
     * <p>Identical to calling
     * {@link #applyVehicleControl(float, float, float, boolean, boolean)}
     * with the values of {@code control}.</p>
     *
     * @param control structured vehicle control; must be non-null.
     * @throws CarlaException          if the actor is already closed
     *                                  or is not a vehicle.
     * @throws IllegalArgumentException if {@code control} is
     *                                  {@code null}.
     */
    public void applyVehicleControl(VehicleControl control) {
        handle().ApplyVehicleControl(
            control.throttle(),
            control.steer(),
            control.brake(),
            control.handBrake(),
            control.reverse());
    }

    /**
     * Releases the underlying native actor handle.
     *
     * <p>Called automatically by {@link #close()}; user code should
     * not invoke this method directly.</p>
     *
     * @param handle the native handle to release. Must be non-null.
     */
    @Override
    protected void release(CarlaNative.ActorHandle handle) {
        CarlaNative.DeleteActorHandle(handle);
    }
}
