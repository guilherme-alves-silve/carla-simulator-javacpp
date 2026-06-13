package org.carla.javacpp.api;

import org.carla.javacpp.binding.CarlaNative;

public final class Blueprint extends NativeHandle<CarlaNative.BlueprintHandle> {
    Blueprint(CarlaNative.BlueprintHandle handle) {
        super(handle);
    }

    public String getId() {
        return handle().GetId();
    }

    public Blueprint setAttribute(String key, String value) {
        handle().SetAttribute(key, value);
        return this;
    }

    @Override
    protected void release(CarlaNative.BlueprintHandle handle) {
        CarlaNative.DeleteBlueprintHandle(handle);
    }
}
