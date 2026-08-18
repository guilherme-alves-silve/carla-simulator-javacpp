package org.carla.javacpp.api;

/**
 * Callback invoked by a {@link Camera} for every received frame.
 *
 * <p>The callback is invoked from a single dedicated daemon thread
 * per camera. Long-running work should be offloaded to a
 * user-controlled executor to keep the listener responsive.</p>
 *
 * @see Camera#listen(CameraImageListener)
 */
@FunctionalInterface
public interface CameraImageListener {

    /**
     * Handles a single camera frame.
     *
     * @param image the frame just received from the server. The
     *              listener may keep a reference to the image; the
     *              underlying pixel buffer is owned by the
     *              {@code CameraImage} record and stays valid as
     *              long as the reference is alive.
     */
    void onImage(CameraImage image);
}
