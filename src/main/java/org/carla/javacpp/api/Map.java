package org.carla.javacpp.api;

import org.carla.javacpp.binding.CarlaNative;

/**
 * Wrapper over the CARLA C++ {@code Map} object.
 *
 * <p>The map describes the road network of the current simulation:
 * lanes, waypoints, junctions, spawn points, and so on. It is the
 * starting point for any custom route planning code that wants to
 * walk the road graph from Java.</p>
 *
 * <p>The class is {@link AutoCloseable}; closing it releases the
 * native map handle. A map is obtained from {@link World#getMap()}
 * and is independent of the world that produced it.</p>
 *
 * @see World#getMap()
 * @see Waypoint
 */
public final class Map extends NativeHandle<CarlaNative.MapHandle> {

    Map(CarlaNative.MapHandle handle) {
        super(handle);
    }

    /**
     * Returns the name of the map (for example {@code "Town01"} or
     * {@code "Town10HD_Opt"}).
     *
     * @return the map name.
     * @throws CarlaException if the map is already closed.
     */
    public String getName() {
        return handle().GetName();
    }

    /**
     * Looks up the {@link Waypoint} closest to a given location,
     * projecting the location onto the road network.
     *
     * <p>This is a convenience overload that delegates to
     * {@link #getWaypoint(Location, boolean)} with
     * {@code projectToRoad = true}.</p>
     *
     * @param location world location to project.
     * @return the closest waypoint, or {@code null} if the
     *         projection failed (for example, the location is too
     *         far from any road).
     * @throws CarlaException if the map is already closed.
     */
    public Waypoint getWaypoint(Location location) {
        return getWaypoint(location, true);
    }

    /**
     * Looks up the {@link Waypoint} closest to a given location.
     *
     * @param location       world location to project.
     * @param projectToRoad  when {@code true}, the location is
     *                       projected onto the nearest lane before
     *                       the waypoint is returned. When
     *                       {@code false}, only a waypoint that
     *                       already contains the location is
     *                       accepted, and {@code null} is returned
     *                       otherwise.
     * @return the matching waypoint, or {@code null} if no
     *         waypoint satisfies the request.
     * @throws CarlaException if the map is already closed.
     */
    public Waypoint getWaypoint(Location location, boolean projectToRoad) {
        var waypoint = handle().GetWaypoint(location.toNative(), projectToRoad);
        return waypoint == null || waypoint.isNull() ? null : new Waypoint(waypoint);
    }

    /**
     * Generates a regularly-spaced grid of waypoints covering every
     * drivable lane in the map.
     *
     * <p>The result is useful as a starting point for global route
     * planners: the returned list is a snapshot and is safe to
     * iterate even after the map has been closed.</p>
     *
     * @param distance spacing between consecutive waypoints along
     *                 the lane, in meters. Smaller values yield more
     *                 waypoints; values close to {@code 0.0} may
     *                 produce a very large list.
     * @return a new {@link WaypointList} containing every waypoint.
     * @throws CarlaException          if the map is already closed.
     * @throws IllegalArgumentException if {@code distance} is
     *                                  non-positive.
     */
    public WaypointList generateWaypoints(double distance) {
        return new WaypointList(handle().GenerateWaypoints(distance));
    }

    /**
     * Releases the underlying native map handle.
     *
     * <p>Called automatically by {@link #close()}; user code should
     * not invoke this method directly.</p>
     *
     * @param handle the native handle to release. Must be non-null.
     */
    @Override
    protected void release(CarlaNative.MapHandle handle) {
        CarlaNative.DeleteMapHandle(handle);
    }
}
