package org.carla.javacpp.api;

/**
 * Unchecked exception thrown by every API of the Java facade when
 * something goes wrong on the native side or when an invalid
 * argument is detected locally.
 *
 * <p>Typical causes include:</p>
 * <ul>
 *     <li>a CARLA RPC failure (for example, the server is
 *         unreachable, an actor is destroyed, a sensor times
 *         out);</li>
 *     <li>a native call returns a {@code null} or empty pointer
 *         where a non-null value is required (for example,
 *         {@link BlueprintLibrary#find(String)} when the id does
 *         not exist, or {@link Map#getWaypoint(Location, boolean)}
 *         when the projection fails);</li>
 *     <li>an attempt to use a wrapper that has already been
 *         closed.</li>
 * </ul>
 *
 * <p>The exception is unchecked because recovering from a CARLA
 * RPC failure is rarely meaningful at the call site; in most cases
 * the caller should propagate the error and abort the
 * simulation.</p>
 */
public class CarlaException extends RuntimeException {

    /**
     * Creates a new exception with the given message.
     *
     * @param message human-readable description of the failure.
     */
    public CarlaException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with the given message and underlying
     * cause.
     *
     * @param message human-readable description of the failure.
     * @param cause   underlying cause, typically a native error
     *                surfaced through JavaCPP.
     */
    public CarlaException(String message, Throwable cause) {
        super(message, cause);
    }
}
