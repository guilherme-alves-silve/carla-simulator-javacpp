package org.carla.javacpp.api;

import org.carla.javacpp.binding.CarlaNative;

/**
 * A blueprint describing an actor type and its initial attributes.
 *
 * <p>Blueprints are the recipe for spawning actors. A blueprint
 * carries the type id (for example {@code "vehicle.tesla.model3"})
 * and a set of attributes that can be customized before passing the
 * blueprint to {@link World#spawnActor(Blueprint, Transform)}.</p>
 *
 * <p>The class is {@link AutoCloseable}; the underlying native
 * blueprint is released by {@link #close()}.</p>
 *
 * @see BlueprintLibrary
 * @see World#spawnActor(Blueprint, Transform)
 */
public final class Blueprint extends NativeHandle<CarlaNative.BlueprintHandle> {

    Blueprint(CarlaNative.BlueprintHandle handle) {
        super(handle);
    }

    /**
     * Returns the type id of this blueprint.
     *
     * <p>The id is the same string used to filter the blueprint
     * library (for example {@code "vehicle.*"}), and uniquely
     * identifies the actor type this blueprint describes.</p>
     *
     * @return the blueprint type id.
     * @throws CarlaException if the blueprint is already closed.
     */
    public String getId() {
        return handle().GetId();
    }

    /**
     * Sets the value of an attribute on this blueprint.
     *
     * <p>Attributes are typed and validated server-side; passing a
     * value that does not match the expected type, or an unknown
     * attribute key, results in a {@link CarlaException} from the
     * RPC call.</p>
     *
     * @param key   name of the attribute (for example
     *              {@code "role_name"} or {@code "color"}).
     * @param value new value for the attribute, as a string. The
     *              server parses the string according to the
     *              attribute's declared type.
     * @return this {@code Blueprint}, for fluent chaining.
     * @throws CarlaException          if the blueprint is already
     *                                  closed or the attribute
     *                                  change is rejected.
     * @throws IllegalArgumentException if {@code key} or
     *                                  {@code value} is
     *                                  {@code null}.
     */
    public Blueprint setAttribute(String key, String value) {
        handle().SetAttribute(key, value);
        return this;
    }

    /**
     * Releases the underlying native blueprint handle.
     *
     * <p>Called automatically by {@link #close()}; user code should
     * not invoke this method directly.</p>
     *
     * @param handle the native handle to release. Must be non-null.
     */
    @Override
    protected void release(CarlaNative.BlueprintHandle handle) {
        CarlaNative.DeleteBlueprintHandle(handle);
    }
}
