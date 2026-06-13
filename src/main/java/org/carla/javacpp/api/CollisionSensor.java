package org.carla.javacpp.api;

import org.carla.javacpp.binding.CarlaNative;

public final class CollisionSensor extends NativeHandle<CarlaNative.CollisionSensorHandle> {
    private volatile boolean listening;
    private Thread listenerThread;

    CollisionSensor(CarlaNative.CollisionSensorHandle handle) {
        super(handle);
    }

    public long getId() {
        return handle().GetId();
    }

    public CollisionEvent pollEvent(long timeoutMillis) {
        CarlaNative.CollisionEventHandle event = handle().PollEvent(timeoutMillis);
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

    public synchronized void listen(CollisionEventListener listener) {
        if (listening) {
            throw new CarlaException("CollisionSensor is already listening");
        }
        listening = true;
        listenerThread = new Thread(() -> runListener(listener), "carla-collision-listener-" + getId());
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public synchronized void stop() {
        listening = false;
        if (listenerThread != null) {
            listenerThread.interrupt();
            listenerThread = null;
        }
    }

    public boolean destroy() {
        return handle().Destroy();
    }

    @Override
    protected void release(CarlaNative.CollisionSensorHandle handle) {
        stop();
        CarlaNative.DeleteCollisionSensorHandle(handle);
    }

    private void runListener(CollisionEventListener listener) {
        while (listening && !isClosed()) {
            CollisionEvent event = pollEvent(250);
            if (event != null) {
                listener.onCollision(event);
            }
        }
    }
}
