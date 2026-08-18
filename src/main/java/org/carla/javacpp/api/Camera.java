package org.carla.javacpp.api;

import org.carla.javacpp.binding.CarlaNative;

/**
 * RGB camera sensor attached to an actor in the simulation.
 *
 * <p>A {@code Camera} produces BGRA frames at the server's frame
 * rate. Frames can be consumed either by polling with
 * {@link #pollImage(long)} or by registering a listener with
 * {@link #listen(CameraImageListener)} that is invoked from a
 * dedicated daemon thread.</p>
 *
 * <p>The class is {@link AutoCloseable}; closing the camera stops
 * the listener thread and releases the native handle. The associated
 * server-side sensor is also destroyed by this call, so any
 * subsequent use of the camera throws
 * {@link CarlaException}.</p>
 *
 * @see CameraImage
 * @see CameraImageListener
 */
public final class Camera extends NativeHandle<CarlaNative.CameraSensorHandle> {

    private volatile boolean listening;
    private Thread listenerThread;

    Camera(CarlaNative.CameraSensorHandle handle) {
        super(handle);
    }

    /**
     * Returns the unique id of this camera sensor.
     *
     * @return the sensor id; non-negative.
     * @throws CarlaException if the camera is already closed.
     */
    public long getId() {
        return handle().GetId();
    }

    /**
     * Polls the camera for the next available frame, blocking up to
     * the given timeout.
     *
     * <p>Each call returns a single frame; successive calls return
     * successive frames. If no frame is produced within the
     * timeout, {@code null} is returned.</p>
     *
     * @param timeoutMillis maximum time to wait, in milliseconds.
     *                      Must be non-negative.
     * @return the next {@link CameraImage}, or {@code null} when
     *         the timeout elapses without a new frame.
     * @throws CarlaException if the camera is already closed.
     */
    public CameraImage pollImage(long timeoutMillis) {
        var image = handle().PollImage(timeoutMillis);
        if (image == null || image.isNull()) {
            return null;
        }

        try {
            int size = Math.toIntExact(image.Size());
            byte[] bgra = new byte[size];
            image.Data().get(bgra);
            return new CameraImage(
                image.GetFrame(),
                image.GetTimestamp(),
                image.GetWidth(),
                image.GetHeight(),
                bgra);
        } finally {
            CarlaNative.DeleteCameraImageHandle(image);
        }
    }

    /**
     * Starts a background thread that pulls frames from the camera
     * and forwards them to the given listener.
     *
     * <p>The listener is invoked from a single dedicated daemon
     * thread named {@code carla-camera-listener-<id>}; the thread
     * is stopped by {@link #stop()}, by {@link #close()}, or when
     * the camera is destroyed on the server side. Calling
     * {@code listen} while a previous listener is still active
     * throws {@link CarlaException}.</p>
     *
     * @param listener callback invoked once per received frame; must
     *                 be non-null. The callback runs on a
     *                 background thread and must not block
     *                 indefinitely.
     * @throws CarlaException          if the camera is already
     *                                  closed, a listener is already
     *                                  running, or {@code listener}
     *                                  is {@code null}.
     */
    public synchronized void listen(CameraImageListener listener) {
        if (listening) {
            throw new CarlaException("Camera is already listening");
        }
        listening = true;
        listenerThread = new Thread(() -> runListener(listener), "carla-camera-listener-" + getId());
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * Stops the background listener thread started by
     * {@link #listen(CameraImageListener)}.
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
     * <p>After this call returns, the camera is invalid and any
     * subsequent method invocation throws
     * {@link CarlaException}. The Java-side handle can still be
     * closed with {@link #close()}, but doing so is a no-op on
     * the server side.</p>
     *
     * @return {@code true} if the server confirmed the destruction,
     *         {@code false} if the camera was already invalid.
     * @throws CarlaException if the camera is already closed on the
     *                        Java side.
     */
    public boolean destroy() {
        return handle().Destroy();
    }

    /**
     * Releases the underlying native camera handle.
     *
     * <p>Stops the listener thread first so the background loop
     * exits before the native resource is freed.</p>
     *
     * @param handle the native handle to release. Must be non-null.
     */
    @Override
    protected void release(CarlaNative.CameraSensorHandle handle) {
        stop();
        CarlaNative.DeleteCameraSensorHandle(handle);
    }

    private void runListener(CameraImageListener listener) {
        while (listening && !isClosed()) {
            var image = pollImage(250);
            if (image != null) {
                listener.onImage(image);
            }
        }
    }
}
