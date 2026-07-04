package org.carla.javacpp.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.carla.javacpp.binding.CarlaNative;

public final class BlueprintLibrary extends NativeHandle<CarlaNative.BlueprintLibraryHandle> {
    BlueprintLibrary(CarlaNative.BlueprintLibraryHandle handle) {
        super(handle);
    }

    public List<Blueprint> filter(String pattern) {
        var nativeResult = handle().Filter(pattern);
        try {
            var blueprints = new ArrayList<Blueprint>(Math.toIntExact(nativeResult.Size()));
            for (long i = 0; i < nativeResult.Size(); i++) {
                blueprints.add(new Blueprint(nativeResult.Get(i)));
            }
            return Collections.unmodifiableList(blueprints);
        } finally {
            CarlaNative.DeleteBlueprintListHandle(nativeResult);
        }
    }

    public Blueprint find(String id) {
        var blueprint = handle().Find(id);
        if (blueprint == null || blueprint.isNull()) {
            throw new CarlaException("Blueprint not found: " + id);
        }
        return new Blueprint(blueprint);
    }

    @Override
    protected void release(CarlaNative.BlueprintLibraryHandle handle) {
        CarlaNative.DeleteBlueprintLibraryHandle(handle);
    }
}
