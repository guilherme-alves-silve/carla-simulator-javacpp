package org.carla.javacpp.api;

import java.awt.image.BufferedImage;

public record CameraImage(
    long frame,
    double timestamp,
    int width,
    int height,
    byte[] bgra
) {
    public BufferedImage toBufferedImage() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

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
