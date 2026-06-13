package org.carla.javacpp.api;

public record LidarMeasurement(
    long frame,
    double timestamp,
    float horizontalAngle,
    int channelCount,
    float[] points
) {
    public int pointCount() {
        return points.length / 4;
    }

    public float x(int index) {
        return points[index * 4];
    }

    public float y(int index) {
        return points[index * 4 + 1];
    }

    public float z(int index) {
        return points[index * 4 + 2];
    }

    public float intensity(int index) {
        return points[index * 4 + 3];
    }
}
