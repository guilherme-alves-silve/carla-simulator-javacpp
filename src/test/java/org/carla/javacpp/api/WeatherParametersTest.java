package org.carla.javacpp.api;

/**
 * The {@code WeatherParameters} round-trip test moved to
 * {@code WeatherParametersIT} in {@code src/integration-test/java/}
 * because it depends on the native bridge
 * ({@code jniCarlaNative}). See {@code ValueTypesIT} for the
 * rationale and the canonical {@code mvn -Pnative
 * -Pintegration-tests verify} invocation.
 */
final class WeatherParametersTest {
    private WeatherParametersTest() {
    }
}
