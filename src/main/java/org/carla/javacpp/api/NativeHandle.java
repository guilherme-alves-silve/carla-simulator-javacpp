package org.carla.javacpp.api;

import org.bytedeco.javacpp.Pointer;

abstract class NativeHandle<T extends Pointer> implements AutoCloseable {
    private T handle;

    NativeHandle(T handle) {
        this.handle = requireHandle(handle);
    }

    final T handle() {
        if (handle == null) {
            throw new CarlaException(getClass().getSimpleName() + " is already closed");
        }
        return handle;
    }

    final boolean isClosed() {
        return handle == null;
    }

    @Override
    public final void close() {
        if (handle != null) {
            release(handle);
            handle = null;
        }
    }

    protected abstract void release(T handle);

    private T requireHandle(T candidate) {
        if (candidate == null || candidate.isNull()) {
            throw new CarlaException("CARLA returned an empty native handle");
        }
        return candidate;
    }
}
