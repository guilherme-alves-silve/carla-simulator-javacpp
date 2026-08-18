package org.carla.javacpp.api;

import org.carla.javacpp.binding.CarlaNative;

/**
 * LIDAR sensor attached to an actor.
 *
 * <p>The sensor streams point clouds representing the surrounding
 * 3D scene. Each point carries an intensity value in addition to
 * its {@code (x, y, z)} coordinates. Measurements can be consumed
 * either by polling with {@link #pollMeasurement(long)} or by
 * registering a listener with
 * {@link #listen(LidarMeasurementListener)} that is invoked from a
 * dedicated daemon thread.</p>
 *
 * <p>The class is {@link AutoCloseable}; closing the sensor stops
 * the listener thread, destroys the underlying server-side sensor,
 * and releases the native handle.</p>
 *
 * @see LidarMeasurement
 * @see LidarMeasurementListener
 * @see LidarSensorOptions
 */
public final class LidarSensor extends NativeHandle<CarlaNative.LidarSensorHandle> {

    private volatile boolean listening;
    private Thread listenerThread;

    LidarSensor(CarlaNative.LidarSensorHandle handle) {
        super(handle);
    }

    /**
     * Returns the unique id of this LIDAR sensor.
     *
     * @return the sensor id; non-negative.
     * @throws CarlaException if the sensor is already closed.
     */
    public long getId() {
        return handle().GetId();
    }

    /**
     * Polls the sensor for the next point cloud, blocking up to the
     * given timeout.
     *
     * <p>Each call returns a single cloud; successive calls return
     * successive clouds. If no cloud is produced within the
     * timeout, {@code null} is returned.</p>
     *
     * @param timeoutMillis maximum time to wait, in milliseconds.
     *                      Must be non-negative.
     * @return the next {@link LidarMeasurement}, or {@code null}
     *         when the timeout elapses without a new cloud.
     * @throws CarlaException if the sensor is already closed.
     */
    public LidarMeasurement pollMeasurement(long timeoutMillis) {
        var measurement = handle().PollMeasurement(timeoutMillis);
        if (measurement == null || measurement.isNull()) {
            return null;
        }

        try {
            int size = Math.toIntExact(measurement.Size());
            float[] points = new float[size];
            measurement.Data().get(points);
            return new LidarMeasurement(
                measurement.GetFrame(),
                measurement.GetTimestamp(),
                measurement.GetHorizontalAngle(),
                measurement.GetChannelCount(),
                points);
        } finally {
            CarlaNative.DeleteLidarMeasurementHandle(measurement);
        }
    }

    /**
     * Starts a background thread that pulls measurements from the
     * sensor and forwards them to the given listener.
     *
     * <p>The listener is invoked from a single dedicated daemon
     * thread named {@code carla-lidar-listener-<id>}; the thread is
     * stopped by {@link #stop()}, by {@link #close()}, or when the
     * sensor is destroyed on the server side. Calling
     * {@code listen} while a previous listener is still active
     * throws {@link CarlaException}.</p>
     *
     * @param listener callback invoked once per received
     *                 measurement; must be non-null. The callback
     *                 runs on a background thread and must not
     *                 block indefinitely, since it gates the
     *                 reception of further clouds.
     * @throws CarlaException          if the sensor is already
     *                                  closed, a listener is already
     *                                  running, or {@code listener}
     *                                  is {@code null}.
     */
    public synchronized void listen(LidarMeasurementListener listener) {
        if (listening) {
            throw new CarlaException("LidarSensor is already listening");
        }
        listening = true;
        listenerThread = new Thread(() -> runListener(listener), "carla-lidar-listener-" + getId());
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * Stops the background listener thread started by
     * {@link #listen(LidarMeasurementListener)}.
     *
     * <p>This method is idempotent: calling it when no listener is
     * running is a no-op.</p>
     */
    public synchronized void stop() {
        listening = false;
        if (listenerThread != null) {
            listenerThread.interrupt();
            listenerThread = null;
        }
    }

    /**
     * Destroys the sensor on the server side.
     *
     * @return {@code true} if the server confirmed the destruction,
     *         {@code false} if the sensor was already invalid.
     * @throws CarlaException if the sensor is already closed on the
     *                        Java side.
     */
    public boolean destroy() {
        return handle().Destroy();
    }

    /**
     * Releases the underlying native LIDAR handle.
     *
     * <p>Stops the listener thread first so the background loop
     * exits before the native resource is freed.</p>
     *
     * @param handle the native handle to release. Must be non-null.
     */
    @Override
    protected void release(CarlaNative.LidarSensorHandle handle) {
        stop();
        CarlaNative.DeleteLidarSensorHandle(handle);
    }

    private void runListener(LidarMeasurementListener listener) {
        while (listening && !isClosed()) {
            var measurement = pollMeasurement(250);
            if (measurement != null) {
                listener.onMeasurement(measurement);
            }
        }
    }
}
