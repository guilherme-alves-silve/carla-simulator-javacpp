package org.carla.javacpp.api;

/**
 * The {@code WorldSettings} round-trip tests moved to
 * {@code WorldSettingsIT} in {@code src/integration-test/java/}
 * because they depend on the native bridge
 * ({@code jniCarlaNative}). See {@code ValueTypesIT} for the
 * rationale and the canonical {@code mvn -Pnative
 * -Pintegration-tests verify} invocation.
 */
final class WorldSettingsTest {
    private WorldSettingsTest() {
    }
}
