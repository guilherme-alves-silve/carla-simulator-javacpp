package org.carla.javacpp.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class LidarMeasurementTest {
    private static final long FRAME = 3;
    private static final double TIMESTAMP = 0.25;
    private static final float HORIZONTAL_ANGLE = 90.0f;
    private static final int CHANNEL_COUNT = 2;
    private static final float FIRST_POINT_X = 1.0f;
    private static final float FIRST_POINT_Y = 2.0f;
    private static final float FIRST_POINT_Z = 3.0f;
    private static final float FIRST_POINT_INTENSITY = 0.4f;
    private static final float SECOND_POINT_X = -1.0f;
    private static final float SECOND_POINT_Y = -2.0f;
    private static final float SECOND_POINT_Z = -3.0f;
    private static final float SECOND_POINT_INTENSITY = 0.8f;
    private static final int EXPECTED_POINT_COUNT = 2;
    private static final int FIRST_POINT_INDEX = 0;
    private static final int SECOND_POINT_INDEX = 1;

    @Test
    void exposesPointLayoutAsXyzIntensityTuples() {
        var measurement = new LidarMeasurement(
            FRAME,
            TIMESTAMP,
            HORIZONTAL_ANGLE,
            CHANNEL_COUNT,
            new float[] {
                FIRST_POINT_X, FIRST_POINT_Y, FIRST_POINT_Z, FIRST_POINT_INTENSITY,
                SECOND_POINT_X, SECOND_POINT_Y, SECOND_POINT_Z, SECOND_POINT_INTENSITY
            });

        assertEquals(EXPECTED_POINT_COUNT, measurement.pointCount());
        assertEquals(FIRST_POINT_X, measurement.x(FIRST_POINT_INDEX));
        assertEquals(FIRST_POINT_Y, measurement.y(FIRST_POINT_INDEX));
        assertEquals(FIRST_POINT_Z, measurement.z(FIRST_POINT_INDEX));
        assertEquals(FIRST_POINT_INTENSITY, measurement.intensity(FIRST_POINT_INDEX));
        assertEquals(SECOND_POINT_X, measurement.x(SECOND_POINT_INDEX));
        assertEquals(SECOND_POINT_Y, measurement.y(SECOND_POINT_INDEX));
        assertEquals(SECOND_POINT_Z, measurement.z(SECOND_POINT_INDEX));
        assertEquals(SECOND_POINT_INTENSITY, measurement.intensity(SECOND_POINT_INDEX));
    }
}
