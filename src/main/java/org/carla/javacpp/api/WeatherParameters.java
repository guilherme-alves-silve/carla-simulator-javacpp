package org.carla.javacpp.api;

import org.carla.javacpp.binding.CarlaNative;

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

    public static WeatherParameters clearNoon() {
        return new WeatherParameters(5f, 0f, 0f, 10f, -1f, 45f, 2f, 0.75f, 0.1f, 0f, 1f, 0.03f, 0.0331f, 0f);
    }

    public WeatherParameters cloudiness(float value) {
        return new WeatherParameters(value, precipitation, precipitationDeposits, windIntensity, sunAzimuthAngle,
            sunAltitudeAngle, fogDensity, fogDistance, fogFalloff, wetness, scatteringIntensity,
            mieScatteringScale, rayleighScatteringScale, dustStorm);
    }

    public WeatherParameters precipitation(float value) {
        return new WeatherParameters(cloudiness, value, precipitationDeposits, windIntensity, sunAzimuthAngle,
            sunAltitudeAngle, fogDensity, fogDistance, fogFalloff, wetness, scatteringIntensity,
            mieScatteringScale, rayleighScatteringScale, dustStorm);
    }

    public WeatherParameters sunAzimuthAngle(float value) {
        return new WeatherParameters(cloudiness, precipitation, precipitationDeposits, windIntensity, value,
            sunAltitudeAngle, fogDensity, fogDistance, fogFalloff, wetness, scatteringIntensity,
            mieScatteringScale, rayleighScatteringScale, dustStorm);
    }

    public WeatherParameters sunAltitudeAngle(float value) {
        return new WeatherParameters(cloudiness, precipitation, precipitationDeposits, windIntensity, sunAzimuthAngle,
            value, fogDensity, fogDistance, fogFalloff, wetness, scatteringIntensity,
            mieScatteringScale, rayleighScatteringScale, dustStorm);
    }
}
