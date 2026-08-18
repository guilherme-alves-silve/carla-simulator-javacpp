package org.carla.javacpp.api;

import org.bytedeco.javacpp.Pointer;

/**
 * Common base class for every wrapper that holds a single native
 * pointer.
 *
 * <p>The class encapsulates the lifetime management of the
 * underlying {@link Pointer}: it refuses to expose the pointer
 * after the wrapper is closed, validates the pointer at
 * construction time, and delegates the actual native release to
 * the {@link #release(Pointer)} method implemented by each
 * subclass.</p>
 *
 * <p>Package-private: only the {@code org.carla.javacpp.api}
 * subclasses use this base class, but they expose it indirectly
 * through {@link AutoCloseable}.</p>
 *
 * @param <T> the concrete {@link Pointer} type wrapped by this
 *            handle.
 */
abstract class NativeHandle<T extends Pointer> implements AutoCloseable {

    private T handle;

    NativeHandle(T handle) {
        this.handle = requireHandle(handle);
    }

    /**
     * Returns the underlying native pointer.
     *
     * @return the live native pointer.
     * @throws CarlaException if the handle is already closed.
     */
    final T handle() {
        if (handle == null) {
            throw new CarlaException(getClass().getSimpleName() + " is already closed");
        }
        return handle;
    }

    /**
     * Returns whether {@link #close()} has already been called on
     * this handle.
     *
     * @return {@code true} when the native pointer has been
     *         released, {@code false} otherwise.
     */
    final boolean isClosed() {
        return handle == null;
    }

    /**
     * Closes the handle, releasing the underlying native resource
     * through the subclass <code>release</code> hook.
     *
     * <p>Idempotent: calling {@code close()} more than once is a
     * no-op, and the method is safe to use in try-with-resources
     * blocks.</p>
     */
    @Override
    public final void close() {
        if (handle != null) {
            release(handle);
            handle = null;
        }
    }

    /**
     * Releases the underlying native pointer.
     *
     * <p>Implemented by every subclass to call the appropriate
     * <code>Delete*Handle</code> native function.</p>
     *
     * @param handle the native pointer to release; always non-null
     *               when called from {@link #close()}.
     */
    protected abstract void release(T handle);

    private T requireHandle(T candidate) {
        if (candidate == null || candidate.isNull()) {
            throw new CarlaException("CARLA returned an empty native handle");
        }
        return candidate;
    }
}
