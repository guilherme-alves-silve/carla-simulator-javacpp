package org.carla.javacpp.api;

/**
 * Callback invoked by a {@link LidarSensor} for every received
 * point cloud.
 *
 * <p>The callback is invoked from a single dedicated daemon thread
 * per sensor. Long-running work should be offloaded to a
 * user-controlled executor, since blocking this thread gates the
 * reception of further clouds.</p>
 *
 * @see LidarSensor#listen(LidarMeasurementListener)
 */
@FunctionalInterface
public interface LidarMeasurementListener {

    /**
     * Handles a single LIDAR measurement.
     *
     * @param measurement the point cloud just received from the
     *                    server. The listener may keep a reference
     *                    to the measurement; the underlying point
     *                    buffer is owned by the
     *                    {@code LidarMeasurement} record.
     */
    void onMeasurement(LidarMeasurement measurement);
}
