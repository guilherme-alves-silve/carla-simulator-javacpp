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

    @Override
    protected void release(CarlaNative.ClientHandle handle) {
        CarlaNative.DeleteClientHandle(handle);
    }
}
