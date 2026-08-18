package org.carla.javacpp.api;

import java.util.AbstractList;

import org.carla.javacpp.binding.CarlaNative;

/**
 * Snapshot of a sequence of {@link Waypoint} objects.
 *
 * <p>The list is backed by a native handle obtained from a single
 * RPC call (for example, {@link Waypoint#next(double)} or
 * {@link Map#generateWaypoints(double)}). The {@link Waypoint}
 * objects returned by {@link #get(int)} are independent wrappers;
 * closing the list does not invalidate them.</p>
 *
 * <p>The class is {@link AutoCloseable}: close it as soon as the
 * snapshot is no longer needed so the native memory is released
 * promptly.</p>
 */
public final class WaypointList extends AbstractList<Waypoint> implements AutoCloseable {

    private final CarlaNative.WaypointListHandle handle;

    WaypointList(CarlaNative.WaypointListHandle handle) {
        if (handle == null || handle.isNull()) {
            throw new CarlaException("CARLA returned an empty waypoint list");
        }
        this.handle = handle;
    }

    /**
     * {@inheritDoc}
     *
     * @param index index in {@code [0, size())}.
     * @return a new {@link Waypoint} wrapper. The wrapper is
     *         independent from the list; closing the list does not
     *         affect the underlying waypoints.
     */
    @Override
    public Waypoint get(int index) {
        return new Waypoint(handle.Get(index));
    }

    /**
     * {@inheritDoc}
     *
     * @return the number of waypoints in the snapshot.
     */
    @Override
    public int size() {
        return Math.toIntExact(handle.Size());
    }

    /**
     * Releases the underlying native waypoint list handle.
     *
     * <p>Idempotent: closing the list more than once is a no-op.</p>
     */
    @Override
    public void close() {
        CarlaNative.DeleteWaypointListHandle(handle);
    }
}
