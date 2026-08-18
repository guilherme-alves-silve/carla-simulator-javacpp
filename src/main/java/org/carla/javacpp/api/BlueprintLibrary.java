package org.carla.javacpp.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.carla.javacpp.binding.CarlaNative;

/**
 * Server-side library of every available blueprint for the current
 * map.
 *
 * <p>The library is a flat collection indexed by id. The two main
 * lookup operations are:</p>
 * <ul>
 *     <li>{@link #filter(String)} — wildcard match, useful for
 *         enumerating all blueprints of a category
 *         ({@code "vehicle.*"}, {@code "sensor.*"});</li>
 *     <li>{@link #find(String)} — exact id match, useful when the
 *         caller already knows the blueprint id.</li>
 * </ul>
 *
 * <p>The class is {@link AutoCloseable}; the native library handle
 * is released by {@link #close()}.</p>
 *
 * @see Blueprint
 * @see World#getBlueprintLibrary()
 */
public final class BlueprintLibrary extends NativeHandle<CarlaNative.BlueprintLibraryHandle> {

    BlueprintLibrary(CarlaNative.BlueprintLibraryHandle handle) {
        super(handle);
    }

    /**
     * Returns every blueprint whose id matches the given wildcard
     * pattern.
     *
     * <p>The pattern syntax matches the underlying CARLA
     * implementation: simple {@code *} wildcards are supported, and
     * the rest of the string is matched literally.</p>
     *
     * @param pattern wildcard pattern, for example
     *                {@code "vehicle.*"} or
     *                {@code "vehicle.tesla.*"}.
     * @return an unmodifiable list of matching blueprints. The list
     *         is independent of the library and the returned
     *         {@code Blueprint} objects must be closed individually
     *         when no longer needed.
     * @throws CarlaException          if the library is already
     *                                  closed or the pattern is
     *                                  invalid.
     * @throws IllegalArgumentException if {@code pattern} is
     *                                  {@code null}.
     */
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

    /**
     * Looks up a single blueprint by its exact id.
     *
     * @param id blueprint id, for example
     *           {@code "vehicle.tesla.model3"}.
     * @return the matching {@link Blueprint}. The returned object
     *         must be closed individually when no longer needed.
     * @throws CarlaException          if the library is already
     *                                  closed or no blueprint with
     *                                  the given id exists.
     * @throws IllegalArgumentException if {@code id} is
     *                                  {@code null}.
     */
    public Blueprint find(String id) {
        var blueprint = handle().Find(id);
        if (blueprint == null || blueprint.isNull()) {
            throw new CarlaException("Blueprint not found: " + id);
        }
        return new Blueprint(blueprint);
    }

    /**
     * Releases the underlying native blueprint library handle.
     *
     * <p>Called automatically by {@link #close()}; user code should
     * not invoke this method directly.</p>
     *
     * @param handle the native handle to release. Must be non-null.
     */
    @Override
    protected void release(CarlaNative.BlueprintLibraryHandle handle) {
        CarlaNative.DeleteBlueprintLibraryHandle(handle);
    }
}
