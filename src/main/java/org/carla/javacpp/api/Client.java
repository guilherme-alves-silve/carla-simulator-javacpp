package org.carla.javacpp.api;

import java.time.Duration;

import org.carla.javacpp.binding.CarlaNative;

public final class Client extends NativeHandle<CarlaNative.ClientHandle> {
    public Client(String host, int port) {
        super(new CarlaNative.ClientHandle(host, port));
    }

    public Client setTimeout(Duration timeout) {
        handle().SetTimeoutMillis(timeout.toMillis());
        return this;
    }

    public World getWorld() {
        return new World(handle().GetWorld());
    }

    public String startRecorder(String fileName) {
        return startRecorder(fileName, false);
    }

    public String startRecorder(String fileName, boolean additionalData) {
        return handle().StartRecorder(fileName, additionalData);
    }

    public void stopRecorder() {
        handle().StopRecorder();
    }

    public String showRecorderFileInfo(String fileName, boolean showAll) {
        return handle().ShowRecorderFileInfo(fileName, showAll);
    }

    public String showRecorderCollisions(String fileName, char type1, char type2) {
        return handle().ShowRecorderCollisions(fileName, RecorderActorType.toNative(type1), RecorderActorType.toNative(type2));
    }

    public String showRecorderActorsBlocked(String fileName, double minTime, double minDistance) {
        return handle().ShowRecorderActorsBlocked(fileName, minTime, minDistance);
    }

    public String replayFile(
        String fileName,
        double start,
        double duration,
        long followId,
        boolean replaySensors,
        Transform offset
    ) {
        return handle().ReplayFile(fileName, start, duration, followId, replaySensors, offset.toNative());
    }

    public String replayFile(String fileName, double start, double duration, long followId) {
        return replayFile(
            fileName,
            start,
            duration,
            followId,
            true,
            new Transform(new Location(0.0, 0.0, 0.0), new Rotation(0.0, 0.0, 0.0)));
    }

    public void stopReplayer(boolean keepActors) {
        handle().StopReplayer(keepActors);
    }

    public void setReplayerTimeFactor(double timeFactor) {
        handle().SetReplayerTimeFactor(timeFactor);
    }

    public void setReplayerIgnoreHero(boolean ignoreHero) {
        handle().SetReplayerIgnoreHero(ignoreHero);
    }

    public void setReplayerIgnoreSpectator(boolean ignoreSpectator) {
        handle().SetReplayerIgnoreSpectator(ignoreSpectator);
    }

    @Override
    protected void release(CarlaNative.ClientHandle handle) {
        CarlaNative.DeleteClientHandle(handle);
    }
}
