package org.carla.javacpp.api;

import org.carla.javacpp.binding.CarlaNative;

/**
 * Collision sensor attached to an actor.
 *
 * <p>The sensor fires whenever the parent actor is involved in a
 * collision. Events can be consumed either by polling with
 * {@link #pollEvent(long)} or by registering a listener with
 * {@link #listen(CollisionEventListener)} that is invoked from a
 * dedicated daemon thread.</p>
 *
 * <p>The class is {@link AutoCloseable}; closing the sensor stops
 * the listener thread, destroys the underlying server-side sensor,
 * and releases the native handle.</p>
 *
 * @see CollisionEvent
 * @see CollisionEventListener
 */
public final class CollisionSensor extends NativeHandle<CarlaNative.CollisionSensorHandle> {

    private volatile boolean listening;
    private Thread listenerThread;

    CollisionSensor(CarlaNative.CollisionSensorHandle handle) {
        super(handle);
    }

    /**
     * Returns the unique id of this collision sensor.
     *
     * @return the sensor id; non-negative.
     * @throws CarlaException if the sensor is already closed.
     */
    public long getId() {
        return handle().GetId();
    }

    /**
     * Polls the sensor for the next collision event, blocking up to
     * the given timeout.
     *
     * <p>Each call returns a single event; successive calls return
     * successive events. If no event occurs within the timeout,
     * {@code null} is returned.</p>
     *
     * @param timeoutMillis maximum time to wait, in milliseconds.
     *                      Must be non-negative.
     * @return the next {@link CollisionEvent}, or {@code null} when
     *         the timeout elapses without a new event.
     * @throws CarlaException if the sensor is already closed.
     */
    public CollisionEvent pollEvent(long timeoutMillis) {
        var event = handle().PollEvent(timeoutMillis);
        if (event == null || event.isNull()) {
            return null;
        }

        try {
            return new CollisionEvent(
                event.GetFrame(),
                event.GetTimestamp(),
                event.GetActorId(),
                event.GetOtherActorId(),
                event.GetOtherActorTypeId(),
                event.GetNormalImpulseX(),
                event.GetNormalImpulseY(),
                event.GetNormalImpulseZ());
        } finally {
            CarlaNative.DeleteCollisionEventHandle(event);
        }
    }

    /**
     * Starts a background thread that pulls events from the sensor
     * and forwards them to the given listener.
     *
     * <p>The listener is invoked from a single dedicated daemon
     * thread named {@code carla-collision-listener-<id>}; the
     * thread is stopped by {@link #stop()}, by {@link #close()}, or
     * when the sensor is destroyed on the server side. Calling
     * {@code listen} while a previous listener is still active
     * throws {@link CarlaException}.</p>
     *
     * @param listener callback invoked once per received event; must
     *                 be non-null.
     * @throws CarlaException          if the sensor is already
     *                                  closed, a listener is already
     *                                  running, or {@code listener}
     *                                  is {@code null}.
     */
    public synchronized void listen(CollisionEventListener listener) {
        if (listening) {
            throw new CarlaException("CollisionSensor is already listening");
        }
        listening = true;
        listenerThread = new Thread(() -> runListener(listener), "carla-collision-listener-" + getId());
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * Stops the background listener thread started by
     * {@link #listen(CollisionEventListener)}.
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
     * Releases the underlying native collision sensor handle.
     *
     * <p>Stops the listener thread first so the background loop
     * exits before the native resource is freed.</p>
     *
     * @param handle the native handle to release. Must be non-null.
     */
    @Override
    protected void release(CarlaNative.CollisionSensorHandle handle) {
        stop();
        CarlaNative.DeleteCollisionSensorHandle(handle);
    }

    private void runListener(CollisionEventListener listener) {
        while (listening && !isClosed()) {
            var event = pollEvent(250);
            if (event != null) {
                listener.onCollision(event);
            }
        }
    }
}
