package org.carla.javacpp.api;

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
    public double normalImpulseLength() {
        return Math.sqrt(
            normalImpulseX * normalImpulseX
                + normalImpulseY * normalImpulseY
                + normalImpulseZ * normalImpulseZ);
    }
}
