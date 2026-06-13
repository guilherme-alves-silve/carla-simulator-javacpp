package org.carla.javacpp.api;

@FunctionalInterface
public interface CollisionEventListener {
    void onCollision(CollisionEvent event);
}
