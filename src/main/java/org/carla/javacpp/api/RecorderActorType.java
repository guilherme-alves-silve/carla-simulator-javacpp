package org.carla.javacpp.api;

/**
 * Type tags accepted by
 * {@link Client#showRecorderCollisions(String, char, char)} when
 * filtering collision events from a recording.
 *
 * <p>The constants are simple {@code char} values that map to the
 * underlying CARLA tags. The class is non-instantiable.</p>
 *
 * @see Client#showRecorderCollisions(String, char, char)
 */
public final class RecorderActorType {

    /** Wildcard actor type: any actor. */
    public static final char ALL = 'a';

    /** Vehicle actor (cars, trucks, motorcycles, bikes). */
    public static final char VEHICLE = 'v';

    /** Walker (pedestrian) actor. */
    public static final char WALKER = 'w';

    /** Traffic light actor. */
    public static final char TRAFFIC_LIGHT = 't';

    /** Any other actor type not covered by the constants above. */
    public static final char OTHER = 'o';

    private RecorderActorType() {
    }

    /**
     * Marshals a {@code char} tag to its native counterpart.
     *
     * <p>Package-private: only the bridge uses this method. The
     * input is matched case-insensitively.</p>
     *
     * @param value one of the {@code ALL}, {@code VEHICLE},
     *              {@code WALKER}, {@code TRAFFIC_LIGHT} or
     *              {@code OTHER} constants.
     * @return the corresponding native byte.
     * @throws IllegalArgumentException if {@code value} is not one
     *                                  of the supported tags.
     */
    static byte toNative(char value) {
        return switch (Character.toLowerCase(value)) {
            case ALL -> (byte) ALL;
            case VEHICLE -> (byte) VEHICLE;
            case WALKER -> (byte) WALKER;
            case TRAFFIC_LIGHT -> (byte) TRAFFIC_LIGHT;
            case OTHER -> (byte) OTHER;
            default -> throw new IllegalArgumentException("Unsupported recorder actor type: " + value);
        };
    }
}
