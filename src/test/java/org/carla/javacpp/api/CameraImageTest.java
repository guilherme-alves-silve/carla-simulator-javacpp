package org.carla.javacpp.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class CameraImageTest {
    private static final long FRAME = 10;
    private static final double TIMESTAMP = 1.5;
    private static final int IMAGE_WIDTH = 2;
    private static final int IMAGE_HEIGHT = 1;
    private static final byte[] TWO_BGR_PIXELS = {
        0x03, 0x02, 0x01, 0x00,
        0x30, 0x20, 0x10, 0x00
    };
    private static final int FIRST_PIXEL_ARGB = 0xFF010203;
    private static final int SECOND_PIXEL_ARGB = 0xFF102030;

    @Test
    void convertsBgraPixelsToArgbBufferedImage() {
        var image = new CameraImage(
            FRAME,
            TIMESTAMP,
            IMAGE_WIDTH,
            IMAGE_HEIGHT,
            TWO_BGR_PIXELS);

        var buffered = image.toBufferedImage();

        assertEquals(IMAGE_WIDTH, buffered.getWidth());
        assertEquals(IMAGE_HEIGHT, buffered.getHeight());
        assertEquals(FIRST_PIXEL_ARGB, buffered.getRGB(0, 0));
        assertEquals(SECOND_PIXEL_ARGB, buffered.getRGB(1, 0));
    }
}
