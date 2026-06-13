package org.carla.javacpp.api;

import java.util.ArrayList;
import java.util.List;

import org.carla.javacpp.binding.CarlaNative;

public final class World extends NativeHandle<CarlaNative.WorldHandle> {
    World(CarlaNative.WorldHandle handle) {
        super(handle);
    }

    public String getMapName() {
        return handle().GetMapName();
    }

    public BlueprintLibrary getBlueprintLibrary() {
        return new BlueprintLibrary(handle().GetBlueprintLibrary());
    }

    public ActorList getActors() {
        return new ActorList(handle().GetActors());
    }

    public List<Transform> getSpawnPoints() {
        CarlaNative.TransformListHandle nativeResult = handle().GetSpawnPoints();
        try {
            List<Transform> spawnPoints = new ArrayList<>(Math.toIntExact(nativeResult.Size()));
            for (long i = 0; i < nativeResult.Size(); i++) {
                spawnPoints.add(Transform.fromNative(nativeResult.Get(i)));
            }
            return spawnPoints;
        } finally {
            CarlaNative.DeleteTransformListHandle(nativeResult);
        }
    }

    public WorldSettings getSettings() {
        return WorldSettings.fromNative(handle().GetSettings());
    }

    public long applySettings(WorldSettings settings) {
        return handle().ApplySettings(settings.toNative(), 5_000);
    }

    public long applySettings(WorldSettings settings, long timeoutMillis) {
        return handle().ApplySettings(settings.toNative(), timeoutMillis);
    }

    public long tick() {
        return tick(5_000);
    }

    public long tick(long timeoutMillis) {
        return handle().Tick(timeoutMillis);
    }

    public WeatherParameters getWeather() {
        return WeatherParameters.fromNative(handle().GetWeather());
    }

    public void setWeather(WeatherParameters weather) {
        handle().SetWeather(weather.toNative());
    }

    public Actor spawnActor(Blueprint blueprint, Transform transform) {
        return new Actor(handle().SpawnActor(blueprint.handle(), transform.toNative()));
    }

    public Actor trySpawnActor(Blueprint blueprint, Transform transform) {
        CarlaNative.ActorHandle actor = handle().TrySpawnActor(blueprint.handle(), transform.toNative());
        return actor == null || actor.isNull() ? null : new Actor(actor);
    }

    public Vehicle spawnVehicle(Blueprint blueprint, Transform transform) {
        return new Vehicle(handle().SpawnActor(blueprint.handle(), transform.toNative()));
    }

    public Vehicle trySpawnVehicle(Blueprint blueprint, Transform transform) {
        CarlaNative.ActorHandle actor = handle().TrySpawnActor(blueprint.handle(), transform.toNative());
        return actor == null || actor.isNull() ? null : new Vehicle(actor);
    }

    public Camera spawnRgbCamera(Actor parent, Transform transform, int width, int height, double fov) {
        return new Camera(handle().SpawnRgbCamera(parent.handle(), transform.toNative(), width, height, fov));
    }

    public CollisionSensor spawnCollisionSensor(Actor parent, Transform transform) {
        return new CollisionSensor(handle().SpawnCollisionSensor(parent.handle(), transform.toNative()));
    }

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

    @Override
    protected void release(CarlaNative.WorldHandle handle) {
        CarlaNative.DeleteWorldHandle(handle);
    }
}
