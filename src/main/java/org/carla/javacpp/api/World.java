package org.carla.javacpp.api;

import java.util.ArrayList;
import java.util.List;

import org.carla.javacpp.binding.CarlaNative;

/**
 * Represents the current simulated world on the CARLA server.
 *
 * <p>A {@code World} exposes the high-level objects needed to drive a
 * CARLA simulation: the map, the blueprint library, the actors that
 * are currently alive, the recommended spawn points, and convenience
 * methods to spawn vehicles and sensors. The class is
 * {@link AutoCloseable}; closing it releases the underlying native
 * handle but does not affect the simulation itself.</p>
 *
 * <p>Instances are obtained from {@link Client#getWorld()} and are
 * cheap to create. The same world can be queried concurrently from
 * multiple threads as long as each thread uses its own reference.</p>
 *
 * @see Client
 * @see Map
 * @see Actor
 */
public final class World extends NativeHandle<CarlaNative.WorldHandle> {

    World(CarlaNative.WorldHandle handle) {
        super(handle);
    }

    /**
     * Returns the name of the currently loaded map (for example
     * {@code "Town01"} or {@code "Town10HD_Opt"}).
     *
     * @return the map name as reported by the server.
     * @throws CarlaException if the world is already closed.
     */
    public String getMapName() {
        return handle().GetMapName();
    }

    /**
     * Returns the {@link Map} associated with this world.
     *
     * <p>The map is independent of the world: the returned object can
     * outlive the world it came from and must be closed
     * independently.</p>
     *
     * @return a new {@link Map} handle.
     * @throws CarlaException if the world is already closed.
     */
    public Map getMap() {
        return new Map(handle().GetMap());
    }

    /**
     * Returns the server-side blueprint library, used to enumerate and
     * look up the actor types that can be spawned on this map.
     *
     * @return a new {@link BlueprintLibrary} handle.
     * @throws CarlaException if the world is already closed.
     */
    public BlueprintLibrary getBlueprintLibrary() {
        return new BlueprintLibrary(handle().GetBlueprintLibrary());
    }

    /**
     * Returns every actor currently alive in the world.
     *
     * <p>The returned {@link ActorList} is a snapshot; iterating over
     * it is safe even if the world changes between snapshots, and the
     * list must be closed when no longer needed to release the
     * underlying native memory.</p>
     *
     * @return a new {@link ActorList}.
     * @throws CarlaException if the world is already closed.
     */
    public ActorList getActors() {
        return new ActorList(handle().GetActors());
    }

    /**
     * Returns the recommended spawn points for vehicles on this map.
     *
     * <p>The list is a snapshot of the transforms registered in the
     * current map. Each {@link Transform} is a value object; closing
     * the world does not invalidate them.</p>
     *
     * @return an immutable list of spawn point transforms, possibly
     *         empty for maps without predefined spawn points.
     * @throws CarlaException if the world is already closed.
     */
    public List<Transform> getSpawnPoints() {
        var nativeResult = handle().GetSpawnPoints();
        try {
            var spawnPoints = new ArrayList<Transform>(Math.toIntExact(nativeResult.Size()));
            for (long i = 0; i < nativeResult.Size(); i++) {
                spawnPoints.add(Transform.fromNative(nativeResult.Get(i)));
            }
            return spawnPoints;
        } finally {
            CarlaNative.DeleteTransformListHandle(nativeResult);
        }
    }

    /**
     * Returns the current simulation settings.
     *
     * @return the active {@link WorldSettings} (synchronous mode, no
     *         rendering flag, and fixed delta seconds when enabled).
     * @throws CarlaException if the world is already closed.
     */
    public WorldSettings getSettings() {
        return WorldSettings.fromNative(handle().GetSettings());
    }

    /**
     * Applies new simulation settings using a default 5 second timeout.
     *
     * @param settings new settings to push to the server.
     * @return the id of the settings frame acknowledged by the server.
     * @throws CarlaException if the world is already closed, the
     *                        settings are rejected, or the timeout
     *                        elapses.
     * @see #applySettings(WorldSettings, long)
     */
    public long applySettings(WorldSettings settings) {
        return handle().ApplySettings(settings.toNative(), 5_000);
    }

    /**
     * Applies new simulation settings, blocking until the server
     * acknowledges the change or the timeout elapses.
     *
     * @param settings     new settings to push to the server. Must be
     *                     non-null.
     * @param timeoutMillis maximum time, in milliseconds, to wait for
     *                      the server to acknowledge the new settings.
     * @return the id of the settings frame acknowledged by the server.
     * @throws CarlaException          if the world is already closed,
     *                                  the settings are rejected, or
     *                                  the timeout elapses.
     * @throws IllegalArgumentException if {@code settings} is
     *                                  {@code null} or
     *                                  {@code timeoutMillis <= 0}.
     */
    public long applySettings(WorldSettings settings, long timeoutMillis) {
        return handle().ApplySettings(settings.toNative(), timeoutMillis);
    }

    /**
     * Advances the simulation by a single step using a 5 second
     * timeout.
     *
     * <p>This call is meaningful only when the world is in synchronous
     * mode (see {@link WorldSettings#synchronousMode(boolean)}).
     * Otherwise the server ignores the tick request and the call
     * returns immediately.</p>
     *
     * @return the frame id of the tick acknowledged by the server.
     * @throws CarlaException if the world is already closed.
     */
    public long tick() {
        return tick(5_000);
    }

    /**
     * Advances the simulation by a single step, blocking until the
     * server acknowledges the tick or the timeout elapses.
     *
     * @param timeoutMillis maximum time, in milliseconds, to wait for
     *                      the server to acknowledge the tick.
     * @return the frame id of the tick acknowledged by the server.
     * @throws CarlaException if the world is already closed or the
     *                        timeout elapses.
     */
    public long tick(long timeoutMillis) {
        return handle().Tick(timeoutMillis);
    }

    /**
     * Returns the current weather parameters in effect.
     *
     * @return the active {@link WeatherParameters}.
     * @throws CarlaException if the world is already closed.
     */
    public WeatherParameters getWeather() {
        return WeatherParameters.fromNative(handle().GetWeather());
    }

    /**
     * Replaces the current weather with the given parameters.
     *
     * <p>The change is applied immediately and affects every actor in
     * the simulation.</p>
     *
     * @param weather new weather parameters; must be non-null.
     * @throws CarlaException          if the world is already closed.
     * @throws IllegalArgumentException if {@code weather} is
     *                                  {@code null}.
     */
    public void setWeather(WeatherParameters weather) {
        handle().SetWeather(weather.toNative());
    }

    /**
     * Spawns an actor in the world from the given blueprint and
     * transform.
     *
     * <p>This call blocks until the server confirms the spawn. It
     * throws if the spawn position is invalid (for example, inside
     * another actor or on an unwalkable surface for a walker); use
     * {@link #trySpawnActor(Blueprint, Transform)} to fall back to
     * {@code null} instead.</p>
     *
     * @param blueprint blueprint that describes the actor type and its
     *                  initial attributes; usually obtained from
     *                  {@link BlueprintLibrary#filter(String)} or
     *                  {@link BlueprintLibrary#find(String)}.
     * @param transform initial world transform (location and rotation).
     * @return a new {@link Actor} bound to the spawned native actor.
     * @throws CarlaException if the world is already closed or the
     *                        spawn fails.
     */
    public Actor spawnActor(Blueprint blueprint, Transform transform) {
        return new Actor(handle().SpawnActor(blueprint.handle(), transform.toNative()));
    }

    /**
     * Attempts to spawn an actor, returning {@code null} instead of
     * throwing when the spawn fails.
     *
     * @param blueprint blueprint describing the actor type.
     * @param transform initial world transform.
     * @return the new {@link Actor}, or {@code null} if the server
     *         rejected the spawn.
     * @throws CarlaException if the world is already closed.
     */
    public Actor trySpawnActor(Blueprint blueprint, Transform transform) {
        var actor = handle().TrySpawnActor(blueprint.handle(), transform.toNative());
        return actor == null || actor.isNull() ? null : new Actor(actor);
    }

    /**
     * Spawns a vehicle from a vehicle blueprint.
     *
     * <p>Functionally equivalent to {@link #spawnActor(Blueprint, Transform)}
     * but returns a more specific {@link Vehicle} so vehicle-only APIs
     * (such as {@link Vehicle#setAutopilot(boolean)}) are available
     * without a cast.</p>
     *
     * @param blueprint vehicle blueprint (for example
     *                  {@code vehicle.tesla.model3}).
     * @param transform initial transform.
     * @return a new {@link Vehicle}.
     * @throws CarlaException if the spawn fails.
     */
    public Vehicle spawnVehicle(Blueprint blueprint, Transform transform) {
        return new Vehicle(handle().SpawnActor(blueprint.handle(), transform.toNative()));
    }

    /**
     * Attempts to spawn a vehicle, returning {@code null} when the
     * server refuses the spawn.
     *
     * @param blueprint vehicle blueprint.
     * @param transform initial transform.
     * @return a new {@link Vehicle}, or {@code null} if the spawn was
     *         rejected.
     * @throws CarlaException if the world is already closed.
     */
    public Vehicle trySpawnVehicle(Blueprint blueprint, Transform transform) {
        var actor = handle().TrySpawnActor(blueprint.handle(), transform.toNative());
        return actor == null || actor.isNull() ? null : new Vehicle(actor);
    }

    /**
     * Spawns an RGB camera sensor attached to the given parent actor.
     *
     * <p>The camera runs on the server and produces BGRA frames that
     * can be polled with {@link Camera#pollImage(long)} or consumed
     * asynchronously via {@link Camera#listen(CameraImageListener)}.</p>
     *
     * @param parent    actor to attach the camera to; usually a
     *                  vehicle. The transform is relative to the
     *                  parent.
     * @param transform local transform of the camera relative to
     *                  {@code parent}.
     * @param width     image width in pixels. Must be positive.
     * @param height    image height in pixels. Must be positive.
     * @param fov       horizontal field of view in degrees. Must be
     *                  strictly between 0 and 180.
     * @return a new {@link Camera}.
     * @throws CarlaException if the world is already closed, the
     *                        parent is invalid, or the sensor cannot
     *                        be created.
     */
    public Camera spawnRgbCamera(Actor parent, Transform transform, int width, int height, double fov) {
        return new Camera(handle().SpawnRgbCamera(parent.handle(), transform.toNative(), width, height, fov));
    }

    /**
     * Spawns a collision sensor attached to the given parent actor.
     *
     * <p>The sensor fires whenever the parent is involved in a
     * collision. Use {@link CollisionSensor#listen(CollisionEventListener)}
     * for asynchronous notifications or
     * {@link CollisionSensor#pollEvent(long)} for manual polling.</p>
     *
     * @param parent    actor to attach the sensor to.
     * @param transform local transform relative to {@code parent}.
     * @return a new {@link CollisionSensor}.
     * @throws CarlaException if the world is already closed or the
     *                        sensor cannot be created.
     */
    public CollisionSensor spawnCollisionSensor(Actor parent, Transform transform) {
        return new CollisionSensor(handle().SpawnCollisionSensor(parent.handle(), transform.toNative()));
    }

    /**
     * Spawns a LIDAR sensor attached to the given parent actor.
     *
     * @param parent    actor to attach the sensor to.
     * @param transform local transform relative to {@code parent}.
     * @param options   LIDAR configuration; see {@link LidarSensorOptions}
     *                  for defaults.
     * @return a new {@link LidarSensor}.
     * @throws CarlaException if the world is already closed, the
     *                        options are invalid, or the sensor cannot
     *                        be created.
     */
    public LidarSensor spawnLidar(
        Actor parent,
        Transform transform,
        LidarSensorOptions options
    ) {
        return new LidarSensor(handle().SpawnLidar(
            parent.handle(),
            transform.toNative(),
            options.channels(),
            options.range(),
            options.pointsPerSecond(),
            options.rotationFrequency(),
            options.upperFov(),
            options.lowerFov()));
    }

    /**
     * Releases the underlying native world handle.
     *
     * <p>Called automatically by {@link #close()}; user code should not
     * invoke this method directly.</p>
     *
     * @param handle the native handle to release. Must be non-null.
     */
    @Override
    protected void release(CarlaNative.WorldHandle handle) {
        CarlaNative.DeleteWorldHandle(handle);
    }
}
