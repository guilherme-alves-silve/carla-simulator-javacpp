package org.carla.javacpp.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Native-bridge round-trip tests for {@link WeatherParameters}.
 * Runs the JavaCPP {@code Loader.load()} under the hood, so the
 * {@code jniCarlaNative} shared library must be on the classpath.
 * Build it with the {@code native} profile and enable the
 * integration-test source set with the {@code integration-tests}
 * profile:
 *
 * <pre>{@code
 * mvn -Pnative -Pintegration-tests verify
 * }</pre>
 *
 * No running CARLA simulator is required for this class.
 */
final class WeatherParametersIT {
    private static final float CLOUDINESS = 75.0f;
    private static final float PRECIPITATION = 30.0f;
    private static final float PRECIPITATION_DEPOSITS = 12.0f;
    private static final float WIND_INTENSITY = 44.0f;
    private static final float SUN_AZIMUTH_ANGLE = 120.0f;
    private static final float SUN_ALTITUDE_ANGLE = 15.0f;
    private static final float FOG_DENSITY = 3.0f;
    private static final float FOG_DISTANCE = 25.0f;
    private static final float FOG_FALLOFF = 0.5f;
    private static final float WETNESS = 22.0f;
    private static final float SCATTERING_INTENSITY = 0.7f;
    private static final float MIE_SCATTERING_SCALE = 0.1f;
    private static final float RAYLEIGH_SCATTERING_SCALE = 0.2f;
    private static final float DUST_STORM = 4.0f;

    @Test
    void convertsToAndFromNativeWeather() {
        var weather = WeatherParameters.clearNoon()
            .cloudiness(CLOUDINESS)
            .precipitation(PRECIPITATION)
            .precipitationDeposits(PRECIPITATION_DEPOSITS)
            .windIntensity(WIND_INTENSITY)
            .sunAzimuthAngle(SUN_AZIMUTH_ANGLE)
            .sunAltitudeAngle(SUN_ALTITUDE_ANGLE)
            .fogDensity(FOG_DENSITY)
            .fogDistance(FOG_DISTANCE)
            .fogFalloff(FOG_FALLOFF)
            .wetness(WETNESS)
            .scatteringIntensity(SCATTERING_INTENSITY)
            .mieScatteringScale(MIE_SCATTERING_SCALE)
            .rayleighScatteringScale(RAYLEIGH_SCATTERING_SCALE)
            .dustStorm(DUST_STORM);

        var copy = WeatherParameters.fromNative(weather.toNative());

        assertEquals(CLOUDINESS, copy.cloudiness());
        assertEquals(PRECIPITATION, copy.precipitation());
        assertEquals(PRECIPITATION_DEPOSITS, copy.precipitationDeposits());
        assertEquals(WIND_INTENSITY, copy.windIntensity());
        assertEquals(SUN_AZIMUTH_ANGLE, copy.sunAzimuthAngle());
        assertEquals(SUN_ALTITUDE_ANGLE, copy.sunAltitudeAngle());
        assertEquals(FOG_DENSITY, copy.fogDensity());
        assertEquals(FOG_DISTANCE, copy.fogDistance());
        assertEquals(FOG_FALLOFF, copy.fogFalloff());
        assertEquals(WETNESS, copy.wetness());
        assertEquals(SCATTERING_INTENSITY, copy.scatteringIntensity());
        assertEquals(MIE_SCATTERING_SCALE, copy.mieScatteringScale());
        assertEquals(RAYLEIGH_SCATTERING_SCALE, copy.rayleighScatteringScale());
        assertEquals(DUST_STORM, copy.dustStorm());
    }
}
