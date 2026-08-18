package org.carla.javacpp.api;

/**
 * A single collision event detected by a {@link CollisionSensor}.
 *
 * <p>The event identifies the parent actor and the actor it
 * collided with, and reports the normal impulse of the collision
 * in world coordinates. The instance is immutable.</p>
 *
 * @param frame             simulation frame id, as reported by the
 *                          server.
 * @param timestamp         simulation timestamp of the event, in
 *                          seconds since the start of the
 *                          simulation.
 * @param actorId           id of the parent actor (the actor the
 *                          sensor is attached to).
 * @param otherActorId      id of the actor the parent collided
 *                          with.
 * @param otherActorTypeId  type id of the other actor (for
 *                          example {@code "vehicle.tesla.model3"}).
 * @param normalImpulseX    {@code x} component of the collision
 *                          normal impulse.
 * @param normalImpulseY    {@code y} component of the collision
 *                          normal impulse.
 * @param normalImpulseZ    {@code z} component of the collision
 *                          normal impulse.
 * @see CollisionSensor#pollEvent(long)
 * @see CollisionSensor#listen(CollisionEventListener)
 */
public record CollisionEvent(
    long frame,
    double timestamp,
    long actorId,
    long otherActorId,
    String otherActorTypeId,
    double normalImpulseX,
    double normalImpulseY,
    double normalImpulseZ
) {

    /**
     * Returns the magnitude of the normal impulse vector.
     *
     * <p>This is a shortcut for
     * {@code sqrt(x*x + y*y + z*z)} over the three normal impulse
     * components; it is useful for quickly classifying the
     * severity of a collision.</p>
     *
     * @return the impulse magnitude, in newton-seconds; non-negative.
     */
    public double normalImpulseLength() {
        return Math.sqrt(
            normalImpulseX * normalImpulseX
                + normalImpulseY * normalImpulseY
                + normalImpulseZ * normalImpulseZ);
    }
}
