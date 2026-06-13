package org.carla.javacpp.api;

public class CarlaException extends RuntimeException {
    public CarlaException(String message) {
        super(message);
    }

    public CarlaException(String message, Throwable cause) {
        super(message, cause);
    }
}
