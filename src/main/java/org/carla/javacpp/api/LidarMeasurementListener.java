package org.carla.javacpp.api;

@FunctionalInterface
public interface LidarMeasurementListener {
    void onMeasurement(LidarMeasurement measurement);
}
