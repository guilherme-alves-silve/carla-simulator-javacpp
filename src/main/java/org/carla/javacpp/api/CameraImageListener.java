package org.carla.javacpp.api;

@FunctionalInterface
public interface CameraImageListener {
    void onImage(CameraImage image);
}
