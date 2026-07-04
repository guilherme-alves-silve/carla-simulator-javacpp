package org.carla.javacpp.api;

import org.carla.javacpp.binding.CarlaNative;

public final class Map extends NativeHandle<CarlaNative.MapHandle> {
    Map(CarlaNative.MapHandle handle) {
        super(handle);
    }

    public String getName() {
        return handle().GetName();
    }

    public Waypoint getWaypoint(Location location) {
        return getWaypoint(location, true);
    }

    public Waypoint getWaypoint(Location location, boolean projectToRoad) {
        var waypoint = handle().GetWaypoint(location.toNative(), projectToRoad);
        return waypoint == null || waypoint.isNull() ? null : new Waypoint(waypoint);
    }

    public WaypointList generateWaypoints(double distance) {
        return new WaypointList(handle().GenerateWaypoints(distance));
    }

    @Override
    protected void release(CarlaNative.MapHandle handle) {
        CarlaNative.DeleteMapHandle(handle);
    }
}
