package org.carla.javacpp.api;

public final class RecorderActorType {
    public static final char ALL = 'a';
    public static final char VEHICLE = 'v';
    public static final char WALKER = 'w';
    public static final char TRAFFIC_LIGHT = 't';
    public static final char OTHER = 'o';

    private RecorderActorType() {
    }

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
