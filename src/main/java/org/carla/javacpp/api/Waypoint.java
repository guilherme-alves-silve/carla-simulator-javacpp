package org.carla.javacpp.api;

import org.carla.javacpp.binding.CarlaNative;

public final class Waypoint extends NativeHandle<CarlaNative.WaypointHandle> {
    Waypoint(CarlaNative.WaypointHandle handle) {
        super(handle);
    }

    public long getId() {
        return handle().GetId();
    }

    public int getRoadId() {
        return handle().GetRoadId();
    }

    public int getSectionId() {
        return handle().GetSectionId();
    }

    public int getLaneId() {
        return handle().GetLaneId();
    }

    public double getDistance() {
        return handle().GetDistance();
    }

    public Transform getTransform() {
        return Transform.fromNative(handle().GetTransform());
    }

    public boolean isJunction() {
        return handle().IsJunction();
    }

    public double getLaneWidth() {
        return handle().GetLaneWidth();
    }

    public WaypointList next(double distance) {
        return new WaypointList(handle().Next(distance));
    }

    public WaypointList previous(double distance) {
        return new WaypointList(handle().Previous(distance));
    }

    public Waypoint getRight() {
        var waypoint = handle().GetRight();
        return waypoint == null || waypoint.isNull() ? null : new Waypoint(waypoint);
    }

    public Waypoint getLeft() {
        var waypoint = handle().GetLeft();
        return waypoint == null || waypoint.isNull() ? null : new Waypoint(waypoint);
    }

    @Override
    protected void release(CarlaNative.WaypointHandle handle) {
        CarlaNative.DeleteWaypointHandle(handle);
    }
}
