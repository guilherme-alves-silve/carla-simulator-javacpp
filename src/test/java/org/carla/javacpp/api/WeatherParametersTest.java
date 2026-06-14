package org.carla.javacpp.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class WeatherParametersTest {
    @Test
    void convertsToAndFromNativeWeather() {
        var weather = WeatherParameters.clearNoon()
            .cloudiness(75.0f)
            .precipitation(30.0f)
            .sunAzimuthAngle(120.0f)
            .sunAltitudeAngle(15.0f);

        var copy = WeatherParameters.fromNative(weather.toNative());

        assertEquals(75.0f, copy.cloudiness());
        assertEquals(30.0f, copy.precipitation());
        assertEquals(120.0f, copy.sunAzimuthAngle());
        assertEquals(15.0f, copy.sunAltitudeAngle());
    }
}
