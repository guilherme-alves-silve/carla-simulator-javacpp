package org.carla.javacpp.api;

import org.carla.javacpp.binding.CarlaNative;

public final class LidarSensor extends NativeHandle<CarlaNative.LidarSensorHandle> {
    private volatile boolean listening;
    private Thread listenerThread;

    LidarSensor(CarlaNative.LidarSensorHandle handle) {
        super(handle);
    }

    public long getId() {
        return handle().GetId();
    }

    public LidarMeasurement pollMeasurement(long timeoutMillis) {
        CarlaNative.LidarMeasurementHandle measurement = handle().PollMeasurement(timeoutMillis);
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

    public synchronized void listen(LidarMeasurementListener listener) {
        if (listening) {
            throw new CarlaException("LidarSensor is already listening");
        }
        listening = true;
        listenerThread = new Thread(() -> runListener(listener), "carla-lidar-listener-" + getId());
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
    protected void release(CarlaNative.LidarSensorHandle handle) {
        stop();
        CarlaNative.DeleteLidarSensorHandle(handle);
    }

    private void runListener(LidarMeasurementListener listener) {
        while (listening && !isClosed()) {
            LidarMeasurement measurement = pollMeasurement(250);
            if (measurement != null) {
                listener.onMeasurement(measurement);
            }
        }
    }
}
