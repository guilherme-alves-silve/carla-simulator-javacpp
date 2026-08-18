package org.carla.javacpp.api;

import java.util.AbstractList;

import org.carla.javacpp.binding.CarlaNative;

/**
 * Snapshot of every actor currently alive in a {@link World}.
 *
 * <p>The list is backed by a native handle obtained from a single
 * RPC call. The {@link Actor} objects returned by {@link #get(int)}
 * are independent wrappers; closing the list does not destroy the
 * actors it referenced.</p>
 *
 * <p>The class is {@link AutoCloseable}: close it as soon as the
 * snapshot is no longer needed so the native memory is released
 * promptly.</p>
 */
public final class ActorList extends AbstractList<Actor> implements AutoCloseable {

    private final CarlaNative.ActorListHandle handle;

    ActorList(CarlaNative.ActorListHandle handle) {
        if (handle == null || handle.isNull()) {
            throw new CarlaException("CARLA returned an empty actor list");
        }
        this.handle = handle;
    }

    /**
     * {@inheritDoc}
     *
     * @param index index in {@code [0, size())}.
     * @return a new {@link Actor} wrapper. The wrapper is independent
     *         from the list; closing the list does not destroy the
     *         underlying actor.
     */
    @Override
    public Actor get(int index) {
        return new Actor(handle.Get(index));
    }

    /**
     * {@inheritDoc}
     *
     * @return the number of actors in the snapshot.
     */
    @Override
    public int size() {
        return Math.toIntExact(handle.Size());
    }

    /**
     * Releases the underlying native actor list handle.
     *
     * <p>Idempotent: closing the list more than once is a no-op.</p>
     */
    @Override
    public void close() {
        CarlaNative.DeleteActorListHandle(handle);
    }
}
