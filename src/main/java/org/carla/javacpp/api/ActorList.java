package org.carla.javacpp.api;

import java.util.AbstractList;

import org.carla.javacpp.binding.CarlaNative;

public final class ActorList extends AbstractList<Actor> implements AutoCloseable {
    private final CarlaNative.ActorListHandle handle;

    ActorList(CarlaNative.ActorListHandle handle) {
        if (handle == null || handle.isNull()) {
            throw new CarlaException("CARLA returned an empty actor list");
        }
        this.handle = handle;
    }

    @Override
    public Actor get(int index) {
        return new Actor(handle.Get(index));
    }

    @Override
    public int size() {
        return Math.toIntExact(handle.Size());
    }

    @Override
    public void close() {
        CarlaNative.DeleteActorListHandle(handle);
    }
}
