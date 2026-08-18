package org.carla.javacpp.api;

import org.carla.javacpp.binding.CarlaNative;

/**
 * A point on the road network of a {@link Map}.
 *
 * <p>A {@code Waypoint} is bound to a single lane, road and section,
 * and exposes operations to walk the road graph forward and
 * backward, jump to adjacent lanes, and read the geometric and
 * topological information of the underlying road segment.</p>
 *
 * <p>The class is {@link AutoCloseable}; closing it releases the
 * native waypoint handle. The returned values from
 * {@link #next(double)}, {@link #previous(double)},
 * {@link #getRight()} and {@link #getLeft()} are independent
 * wrappers and must be managed separately.</p>
 */
public final class Waypoint extends NativeHandle<CarlaNative.WaypointHandle> {

    Waypoint(CarlaNative.WaypointHandle handle) {
        super(handle);
    }

    /**
     * Returns the unique id of this waypoint within the current map
     * load.
     *
     * @return the waypoint id; non-negative.
     * @throws CarlaException if the waypoint is already closed.
     */
    public long getId() {
        return handle().GetId();
    }

    /**
     * Returns the id of the road this waypoint belongs to.
     *
     * @return the road id; non-negative.
     * @throws CarlaException if the waypoint is already closed.
     */
    public int getRoadId() {
        return handle().GetRoadId();
    }

    /**
     * Returns the id of the road section this waypoint belongs to.
     *
     * <p>A road may be split into multiple sections, typically at
     * junctions. Together with {@link #getRoadId()} and
     * {@link #getLaneId()} this uniquely identifies the waypoint
     * inside the OpenDRIVE representation of the map.</p>
     *
     * @return the section id; non-negative.
     * @throws CarlaException if the waypoint is already closed.
     */
    public int getSectionId() {
        return handle().GetSectionId();
    }

    /**
     * Returns the lane id of this waypoint.
     *
     * <p>Lane ids are signed: positive ids are lanes going in the
     * direction of the road, negative ids are lanes in the opposite
     * direction. The center lane has id {@code 0}.</p>
     *
     * @return the lane id; {@code 0} for the center lane.
     * @throws CarlaException if the waypoint is already closed.
     */
    public int getLaneId() {
        return handle().GetLaneId();
    }

    /**
     * Returns the arc length, in meters, from the start of the
     * current lane section to this waypoint.
     *
     * @return the distance in meters; non-negative.
     * @throws CarlaException if the waypoint is already closed.
     */
    public double getDistance() {
        return handle().GetDistance();
    }

    /**
     * Returns the world transform of this waypoint.
     *
     * <p>The transform's location is the geographic position of the
     * waypoint, and the rotation is the orientation of the lane at
     * that point.</p>
     *
     * @return the world transform.
     * @throws CarlaException if the waypoint is already closed.
     */
    public Transform getTransform() {
        return Transform.fromNative(handle().GetTransform());
    }

    /**
     * Returns whether this waypoint is part of a junction.
     *
     * @return {@code true} if the waypoint lies inside a junction,
     *         {@code false} otherwise.
     * @throws CarlaException if the waypoint is already closed.
     */
    public boolean isJunction() {
        return handle().IsJunction();
    }

    /**
     * Returns the width of the lane at this waypoint, in meters.
     *
     * @return the lane width in meters; strictly positive.
     * @throws CarlaException if the waypoint is already closed.
     */
    public double getLaneWidth() {
        return handle().GetLaneWidth();
    }

    /**
     * Returns the waypoints reachable by moving forward along the
     * current lane.
     *
     * <p>The implementation walks the road graph using the given
     * step size, stopping at lane endings and at junction
     * boundaries.</p>
     *
     * @param distance step size, in meters. Must be positive; very
     *                 large values may return an empty list when
     *                 the lane ends sooner.
     * @return a new {@link WaypointList} of forward waypoints, in
     *         order. May be empty when the lane has no forward
     *         continuation.
     * @throws CarlaException          if the waypoint is already
     *                                  closed.
     * @throws IllegalArgumentException if {@code distance} is
     *                                  non-positive.
     */
    public WaypointList next(double distance) {
        return new WaypointList(handle().Next(distance));
    }

    /**
     * Returns the waypoints reachable by moving backward along the
     * current lane.
     *
     * @param distance step size, in meters. Must be positive.
     * @return a new {@link WaypointList} of backward waypoints, in
     *         order. May be empty when the lane has no backward
     *         continuation.
     * @throws CarlaException          if the waypoint is already
     *                                  closed.
     * @throws IllegalArgumentException if {@code distance} is
     *                                  non-positive.
     */
    public WaypointList previous(double distance) {
        return new WaypointList(handle().Previous(distance));
    }

    /**
     * Returns the waypoint on the right adjacent lane, if any.
     *
     * @return the waypoint on the right lane at the same road
     *         position, or {@code null} when there is no lane to
     *         the right (for example, on the outermost lane).
     * @throws CarlaException if the waypoint is already closed.
     */
    public Waypoint getRight() {
        var waypoint = handle().GetRight();
        return waypoint == null || waypoint.isNull() ? null : new Waypoint(waypoint);
    }

    /**
     * Returns the waypoint on the left adjacent lane, if any.
     *
     * @return the waypoint on the left lane at the same road
     *         position, or {@code null} when there is no lane to
     *         the left (for example, on the outermost lane).
     * @throws CarlaException if the waypoint is already closed.
     */
    public Waypoint getLeft() {
        var waypoint = handle().GetLeft();
        return waypoint == null || waypoint.isNull() ? null : new Waypoint(waypoint);
    }

    /**
     * Releases the underlying native waypoint handle.
     *
     * <p>Called automatically by {@link #close()}; user code should
     * not invoke this method directly.</p>
     *
     * @param handle the native handle to release. Must be non-null.
     */
    @Override
    protected void release(CarlaNative.WaypointHandle handle) {
        CarlaNative.DeleteWaypointHandle(handle);
    }
}
