package org.carla.javacpp.api;

import java.util.AbstractList;

import org.carla.javacpp.binding.CarlaNative;

public final class WaypointList extends AbstractList<Waypoint> implements AutoCloseable {
    private final CarlaNative.WaypointListHandle handle;

    WaypointList(CarlaNative.WaypointListHandle handle) {
        if (handle == null || handle.isNull()) {
            throw new CarlaException("CARLA returned an empty waypoint list");
        }
        this.handle = handle;
    }

    @Override
    public Waypoint get(int index) {
        return new Waypoint(handle.Get(index));
    }

    @Override
    public int size() {
        return Math.toIntExact(handle.Size());
    }

    @Override
    public void close() {
        CarlaNative.DeleteWaypointListHandle(handle);
    }
}
