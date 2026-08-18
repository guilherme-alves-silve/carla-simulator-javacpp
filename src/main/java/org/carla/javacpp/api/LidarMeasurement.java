package org.carla.javacpp.api;

/**
 * A single LIDAR point cloud produced by a {@link LidarSensor}.
 *
 * <p>The cloud is encoded as a tightly packed {@code float[]}
 * array of length {@code 4 * pointCount}. Each group of four
 * consecutive floats represents a single point in the order
 * {@code (x, y, z, intensity)}.</p>
 *
 * <p>Use {@link #pointCount()} to know how many points are
 * available, and {@link #x(int)}, {@link #y(int)}, {@link #z(int)}
 * and {@link #intensity(int)} to access each individual
 * component.</p>
 *
 * @param frame            simulation frame id, as reported by the
 *                         server.
 * @param timestamp        simulation timestamp of the cloud, in
 *                         seconds since the start of the
 *                         simulation.
 * @param horizontalAngle  current rotation of the LIDAR around
 *                         the vertical axis, in radians, in the
 *                         range {@code [0, 2π)}.
 * @param channelCount     number of vertical layers contained in
 *                         this cloud.
 * @param points           raw point buffer; length is
 *                         {@code 4 * pointCount}.
 * @see LidarSensor#pollMeasurement(long)
 * @see LidarSensor#listen(LidarMeasurementListener)
 */
public record LidarMeasurement(
    long frame,
    double timestamp,
    float horizontalAngle,
    int channelCount,
    float[] points
) {

    /**
     * Returns the number of points in this cloud.
     *
     * @return {@code points.length / 4}; the result is always an
     *         integer because the constructor of the JNI layer
     *         guarantees a multiple-of-four buffer.
     */
    public int pointCount() {
        return points.length / 4;
    }

    /**
     * Returns the {@code x} coordinate of the point at the given
     * index.
     *
     * @param index point index, in {@code [0, pointCount())}.
     * @return the {@code x} coordinate, in meters.
     * @throws ArrayIndexOutOfBoundsException if {@code index} is
     *         outside the valid range.
     */
    public float x(int index) {
        return points[index * 4];
    }

    /**
     * Returns the {@code y} coordinate of the point at the given
     * index.
     *
     * @param index point index, in {@code [0, pointCount())}.
     * @return the {@code y} coordinate, in meters.
     * @throws ArrayIndexOutOfBoundsException if {@code index} is
     *         outside the valid range.
     */
    public float y(int index) {
        return points[index * 4 + 1];
    }

    /**
     * Returns the {@code z} coordinate of the point at the given
     * index.
     *
     * @param index point index, in {@code [0, pointCount())}.
     * @return the {@code z} coordinate, in meters.
     * @throws ArrayIndexOutOfBoundsException if {@code index} is
     *         outside the valid range.
     */
    public float z(int index) {
        return points[index * 4 + 2];
    }

    /**
     * Returns the intensity of the point at the given index.
     *
     * @param index point index, in {@code [0, pointCount())}.
     * @return the intensity, in {@code [0, 1]}.
     * @throws ArrayIndexOutOfBoundsException if {@code index} is
     *         outside the valid range.
     */
    public float intensity(int index) {
        return points[index * 4 + 3];
    }
}
