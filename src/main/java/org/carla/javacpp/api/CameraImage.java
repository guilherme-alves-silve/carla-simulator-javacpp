package org.carla.javacpp.api;

import java.awt.image.BufferedImage;

/**
 * A single BGRA frame received from a {@link Camera}.
 *
 * <p>The pixel buffer is a tightly packed, top-to-bottom,
 * left-to-right byte array of length
 * {@code width * height * 4}. The order inside each pixel is
 * {@code B, G, R, A}.</p>
 *
 * <p>The instance is immutable; the underlying {@code byte[]}
 * stays valid for the lifetime of the {@code CameraImage} and is
 * not copied when the value is consumed.</p>
 *
 * @param frame     simulation frame id, as reported by the server.
 * @param timestamp simulation timestamp of the frame, in seconds
 *                  since the start of the simulation.
 * @param width     image width in pixels.
 * @param height    image height in pixels.
 * @param bgra      raw BGRA pixel buffer; length is
 *                  {@code width * height * 4}.
 * @see Camera#pollImage(long)
 * @see Camera#listen(CameraImageListener)
 */
public record CameraImage(
    long frame,
    double timestamp,
    int width,
    int height,
    byte[] bgra
) {

    /**
     * Converts this frame to a {@link BufferedImage} for display or
     * further processing.
     *
     * <p>The returned image is a fresh {@code TYPE_INT_ARGB}
     * instance that does not share storage with the underlying
     * {@code bgra} buffer, so the {@code CameraImage} can be
     * released without invalidating the image.</p>
     *
     * @return a new {@code BufferedImage} with the same width,
     *         height and pixel content.
     */
    public BufferedImage toBufferedImage() {
        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = (y * width + x) * 4;
                int b = bgra[i] & 0xFF;
                int g = bgra[i + 1] & 0xFF;
                int r = bgra[i + 2] & 0xFF;
                int argb = (0xFF << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(x, y, argb);
            }
        }

        return image;
    }
}
