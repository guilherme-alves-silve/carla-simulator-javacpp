package org.carla.javacpp.api;

/**
 * Callback invoked by a {@link CollisionSensor} for every received
 * collision event.
 *
 * <p>The callback is invoked from a single dedicated daemon thread
 * per sensor. Long-running work should be offloaded to a
 * user-controlled executor to keep the listener responsive.</p>
 *
 * @see CollisionSensor#listen(CollisionEventListener)
 */
@FunctionalInterface
public interface CollisionEventListener {

    /**
     * Handles a single collision event.
     *
     * @param event the event just received from the server.
     */
    void onCollision(CollisionEvent event);
}
