package org.carla.javacpp.api;

import org.carla.javacpp.binding.CarlaNative;

public final class Camera extends NativeHandle<CarlaNative.CameraSensorHandle> {
    private volatile boolean listening;
    private Thread listenerThread;

    Camera(CarlaNative.CameraSensorHandle handle) {
        super(handle);
    }

    public long getId() {
        return handle().GetId();
    }

    public CameraImage pollImage(long timeoutMillis) {
        CarlaNative.CameraImageHandle image = handle().PollImage(timeoutMillis);
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

    public synchronized void listen(CameraImageListener listener) {
        if (listening) {
            throw new CarlaException("Camera is already listening");
        }
        listening = true;
        listenerThread = new Thread(() -> runListener(listener), "carla-camera-listener-" + getId());
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
    protected void release(CarlaNative.CameraSensorHandle handle) {
        stop();
        CarlaNative.DeleteCameraSensorHandle(handle);
    }

    private void runListener(CameraImageListener listener) {
        while (listening && !isClosed()) {
            CameraImage image = pollImage(250);
            if (image != null) {
                listener.onImage(image);
            }
        }
    }
}
