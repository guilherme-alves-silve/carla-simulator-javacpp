package org.carla.javacpp.api;

import org.carla.javacpp.binding.CarlaNative;

/**
 * Immutable description of the weather currently in effect in the
 * simulation.
 *
 * <p>All fields are floating point values in the {@code [0, 100]}
 * range, except for the sun angles (in degrees), the fog distance
 * (in meters), and the various scattering scales (unitless). The
 * exact semantics of each field match the underlying CARLA
 * weather model.</p>
 *
 * <p>The class is a record; use the {@code with*(...)} copy
 * builders to derive modified parameters, then push them with
 * {@link World#setWeather(WeatherParameters)}.</p>
 *
 * @see World#getWeather()
 * @see World#setWeather(WeatherParameters)
 */
public record WeatherParameters(
    float cloudiness,
    float precipitation,
    float precipitationDeposits,
    float windIntensity,
    float sunAzimuthAngle,
    float sunAltitudeAngle,
    float fogDensity,
    float fogDistance,
    float fogFalloff,
    float wetness,
    float scatteringIntensity,
    float mieScatteringScale,
    float rayleighScatteringScale,
    float dustStorm
) {

    /**
     * Marshals these parameters to their native counterpart for use
     * in JNI calls.
     *
     * @return a fresh native value object representing these
     *         parameters.
     */
    public CarlaNative.WeatherParametersValue toNative() {
        return new CarlaNative.WeatherParametersValue()
            .cloudiness(cloudiness)
            .precipitation(precipitation)
            .precipitation_deposits(precipitationDeposits)
            .wind_intensity(windIntensity)
            .sun_azimuth_angle(sunAzimuthAngle)
            .sun_altitude_angle(sunAltitudeAngle)
            .fog_density(fogDensity)
            .fog_distance(fogDistance)
            .fog_falloff(fogFalloff)
            .wetness(wetness)
            .scattering_intensity(scatteringIntensity)
            .mie_scattering_scale(mieScatteringScale)
            .rayleigh_scattering_scale(rayleighScatteringScale)
            .dust_storm(dustStorm);
    }

    /**
     * Builds a {@code WeatherParameters} from a native value object
     * returned by the JNI layer.
     *
     * <p>Package-private: only the bridge uses this constructor.</p>
     *
     * @param value native value to convert; must be non-null.
     * @return a new {@code WeatherParameters} with the same fields
     *         as {@code value}.
     */
    static WeatherParameters fromNative(CarlaNative.WeatherParametersValue value) {
        return new WeatherParameters(
            value.cloudiness(),
            value.precipitation(),
            value.precipitation_deposits(),
            value.wind_intensity(),
            value.sun_azimuth_angle(),
            value.sun_altitude_angle(),
            value.fog_density(),
            value.fog_distance(),
            value.fog_falloff(),
            value.wetness(),
            value.scattering_intensity(),
            value.mie_scattering_scale(),
            value.rayleigh_scattering_scale(),
            value.dust_storm());
    }

    /**
     * Returns a {@code WeatherParameters} snapshot representing a
     * clear noon scene, useful as a starting point for custom
     * weather presets.
     *
     * @return a fresh clear-noon preset.
     */
    public static WeatherParameters clearNoon() {
        return new WeatherParameters(5f, 0f, 0f, 10f, -1f, 45f, 2f, 0.75f, 0.1f, 0f, 1f, 0.03f, 0.0331f, 0f);
    }

    /**
     * Returns a copy of these parameters with the cloudiness
     * replaced.
     *
     * @param value new cloudiness, in {@code [0, 100]}.
     * @return a new {@code WeatherParameters}.
     */
    public WeatherParameters cloudiness(float value) {
        return new WeatherParameters(value, precipitation, precipitationDeposits, windIntensity, sunAzimuthAngle,
            sunAltitudeAngle, fogDensity, fogDistance, fogFalloff, wetness, scatteringIntensity,
            mieScatteringScale, rayleighScatteringScale, dustStorm);
    }

    /**
     * Returns a copy of these parameters with the precipitation
     * replaced.
     *
     * @param value new precipitation, in {@code [0, 100]}.
     * @return a new {@code WeatherParameters}.
     */
    public WeatherParameters precipitation(float value) {
        return new WeatherParameters(cloudiness, value, precipitationDeposits, windIntensity, sunAzimuthAngle,
            sunAltitudeAngle, fogDensity, fogDistance, fogFalloff, wetness, scatteringIntensity,
            mieScatteringScale, rayleighScatteringScale, dustStorm);
    }

    /**
     * Returns a copy of these parameters with the precipitation
     * deposits replaced.
     *
     * @param value new precipitation deposits, in {@code [0, 100]}.
     * @return a new {@code WeatherParameters}.
     */
    public WeatherParameters precipitationDeposits(float value) {
        return new WeatherParameters(cloudiness, precipitation, value, windIntensity, sunAzimuthAngle,
            sunAltitudeAngle, fogDensity, fogDistance, fogFalloff, wetness, scatteringIntensity,
            mieScatteringScale, rayleighScatteringScale, dustStorm);
    }

    /**
     * Returns a copy of these parameters with the wind intensity
     * replaced.
     *
     * @param value new wind intensity, in {@code [0, 100]}.
     * @return a new {@code WeatherParameters}.
     */
    public WeatherParameters windIntensity(float value) {
        return new WeatherParameters(cloudiness, precipitation, precipitationDeposits, value, sunAzimuthAngle,
            sunAltitudeAngle, fogDensity, fogDistance, fogFalloff, wetness, scatteringIntensity,
            mieScatteringScale, rayleighScatteringScale, dustStorm);
    }

    /**
     * Returns a copy of these parameters with the sun azimuth angle
     * replaced.
     *
     * @param value new sun azimuth angle, in degrees
     *              {@code [0, 360)}.
     * @return a new {@code WeatherParameters}.
     */
    public WeatherParameters sunAzimuthAngle(float value) {
        return new WeatherParameters(cloudiness, precipitation, precipitationDeposits, windIntensity, value,
            sunAltitudeAngle, fogDensity, fogDistance, fogFalloff, wetness, scatteringIntensity,
            mieScatteringScale, rayleighScatteringScale, dustStorm);
    }

    /**
     * Returns a copy of these parameters with the sun altitude angle
     * replaced.
     *
     * @param value new sun altitude angle, in degrees
     *              {@code [-90, 90]}.
     * @return a new {@code WeatherParameters}.
     */
    public WeatherParameters sunAltitudeAngle(float value) {
        return new WeatherParameters(cloudiness, precipitation, precipitationDeposits, windIntensity, sunAzimuthAngle,
            value, fogDensity, fogDistance, fogFalloff, wetness, scatteringIntensity,
            mieScatteringScale, rayleighScatteringScale, dustStorm);
    }

    /**
     * Returns a copy of these parameters with the fog density
     * replaced.
     *
     * @param value new fog density, in {@code [0, 100]}.
     * @return a new {@code WeatherParameters}.
     */
    public WeatherParameters fogDensity(float value) {
        return new WeatherParameters(cloudiness, precipitation, precipitationDeposits, windIntensity, sunAzimuthAngle,
            sunAltitudeAngle, value, fogDistance, fogFalloff, wetness, scatteringIntensity,
            mieScatteringScale, rayleighScatteringScale, dustStorm);
    }

    /**
     * Returns a copy of these parameters with the fog distance
     * replaced.
     *
     * @param value new fog distance, in meters.
     * @return a new {@code WeatherParameters}.
     */
    public WeatherParameters fogDistance(float value) {
        return new WeatherParameters(cloudiness, precipitation, precipitationDeposits, windIntensity, sunAzimuthAngle,
            sunAltitudeAngle, fogDensity, value, fogFalloff, wetness, scatteringIntensity,
            mieScatteringScale, rayleighScatteringScale, dustStorm);
    }

    /**
     * Returns a copy of these parameters with the fog falloff
     * replaced.
     *
     * @param value new fog falloff, in {@code [0, 100]}.
     * @return a new {@code WeatherParameters}.
     */
    public WeatherParameters fogFalloff(float value) {
        return new WeatherParameters(cloudiness, precipitation, precipitationDeposits, windIntensity, sunAzimuthAngle,
            sunAltitudeAngle, fogDensity, fogDistance, value, wetness, scatteringIntensity,
            mieScatteringScale, rayleighScatteringScale, dustStorm);
    }

    /**
     * Returns a copy of these parameters with the wetness replaced.
     *
     * @param value new wetness, in {@code [0, 100]}.
     * @return a new {@code WeatherParameters}.
     */
    public WeatherParameters wetness(float value) {
        return new WeatherParameters(cloudiness, precipitation, precipitationDeposits, windIntensity, sunAzimuthAngle,
            sunAltitudeAngle, fogDensity, fogDistance, fogFalloff, value, scatteringIntensity,
            mieScatteringScale, rayleighScatteringScale, dustStorm);
    }

    /**
     * Returns a copy of these parameters with the scattering
     * intensity replaced.
     *
     * @param value new scattering intensity, in {@code [0, 100]}.
     * @return a new {@code WeatherParameters}.
     */
    public WeatherParameters scatteringIntensity(float value) {
        return new WeatherParameters(cloudiness, precipitation, precipitationDeposits, windIntensity, sunAzimuthAngle,
            sunAltitudeAngle, fogDensity, fogDistance, fogFalloff, wetness, value,
            mieScatteringScale, rayleighScatteringScale, dustStorm);
    }

    /**
     * Returns a copy of these parameters with the Mie scattering
     * scale replaced.
     *
     * @param value new Mie scattering scale.
     * @return a new {@code WeatherParameters}.
     */
    public WeatherParameters mieScatteringScale(float value) {
        return new WeatherParameters(cloudiness, precipitation, precipitationDeposits, windIntensity, sunAzimuthAngle,
            sunAltitudeAngle, fogDensity, fogDistance, fogFalloff, wetness, scatteringIntensity,
            value, rayleighScatteringScale, dustStorm);
    }

    /**
     * Returns a copy of these parameters with the Rayleigh
     * scattering scale replaced.
     *
     * @param value new Rayleigh scattering scale.
     * @return a new {@code WeatherParameters}.
     */
    public WeatherParameters rayleighScatteringScale(float value) {
        return new WeatherParameters(cloudiness, precipitation, precipitationDeposits, windIntensity, sunAzimuthAngle,
            sunAltitudeAngle, fogDensity, fogDistance, fogFalloff, wetness, scatteringIntensity,
            mieScatteringScale, value, dustStorm);
    }

    /**
     * Returns a copy of these parameters with the dust storm flag
     * replaced.
     *
     * @param value new dust storm value, in {@code [0, 100]}.
     * @return a new {@code WeatherParameters}.
     */
    public WeatherParameters dustStorm(float value) {
        return new WeatherParameters(cloudiness, precipitation, precipitationDeposits, windIntensity, sunAzimuthAngle,
            sunAltitudeAngle, fogDensity, fogDistance, fogFalloff, wetness, scatteringIntensity,
            mieScatteringScale, rayleighScatteringScale, value);
    }
}
