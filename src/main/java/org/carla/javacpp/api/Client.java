package org.carla.javacpp.api;

import java.time.Duration;

import org.carla.javacpp.binding.CarlaNative;

/**
 * Entry point of the Java facade for the CARLA simulator.
 *
 * <p>A {@code Client} wraps the native {@code carla::client::Client} exposed
 * by the CARLA C++ client SDK and connects to a running CARLA server over
 * TCP. The class is {@link AutoCloseable}: when it goes out of scope (for
 * instance, in a try-with-resources block) the underlying native handle is
 * released.</p>
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 * try (var client = new Client("localhost", 2000)) {
 *     client.setTimeout(Duration.ofSeconds(10));
 *     try (var world = client.getWorld()) {
 *         // interact with the simulation
 *     }
 * }
 * }</pre>
 *
 * <p>Instances of this class are not thread-safe; use one {@code Client}
 * per thread, or synchronize externally when sharing an instance.</p>
 *
 * @see World
 * @see CarlaException
 */
public final class Client extends NativeHandle<CarlaNative.ClientHandle> {

    /**
     * Creates a new client and connects to a CARLA server.
     *
     * <p>This call is non-blocking: the connection is fully established on
     * first use of the returned handle (for example, {@link #getWorld()}).
     * The native constructor itself will throw a {@link CarlaException} if
     * the server is unreachable or refuses the connection immediately.</p>
     *
     * @param host CARLA server host (for example {@code "localhost"} or
     *             {@code "127.0.0.1"}).
     * @param port CARLA server TCP port. The default CARLA port is
     *             {@code 2000}.
     * @throws CarlaException if the native handle cannot be created.
     */
    public Client(String host, int port) {
        super(new CarlaNative.ClientHandle(host, port));
    }

    /**
     * Sets the network timeout used for every RPC call performed by this
     * client.
     *
     * @param timeout timeout duration; must be non-null and strictly
     *                positive. Sub-millisecond precision is rounded down to
     *                whole milliseconds, matching the underlying C++ API.
     * @return this {@code Client}, for fluent chaining.
     * @throws CarlaException          if the client is already closed.
     * @throws IllegalArgumentException if {@code timeout} is {@code null}
     *                                  or non-positive.
     */
    public Client setTimeout(Duration timeout) {
        handle().SetTimeoutMillis(timeout.toMillis());
        return this;
    }

    /**
     * Returns the {@link World} currently loaded on the server.
     *
     * <p>The returned wrapper is independent of this client; closing the
     * client does not invalidate the world. However, once the world is
     * closed it can no longer be used, even through a fresh client.</p>
     *
     * @return a new {@link World} backed by the current server world.
     * @throws CarlaException if the client is already closed or the
     *                        underlying RPC fails.
     */
    public World getWorld() {
        return new World(handle().GetWorld());
    }

    /**
     * Starts a server-side recording with the default options.
     *
     * <p>This is a convenience overload of
     * {@link #startRecorder(String, boolean)} that passes
     * {@code additionalData = false}.</p>
     *
     * @param fileName target file name on the server host. The path is
     *                 resolved relative to the CARLA server working
     *                 directory, or absolute if an absolute path is given.
     * @return the absolute path written to by the server, useful for
     *         later inspection with {@link #showRecorderFileInfo(String, boolean)}
     *         or replay.
     * @throws CarlaException if the client is already closed, a recording
     *                        is already in progress, or the server
     *                        refuses the call.
     */
    public String startRecorder(String fileName) {
        return startRecorder(fileName, false);
    }

    /**
     * Starts a server-side recording of the simulation.
     *
     * @param fileName       target file name on the server host. The path
     *                       is resolved relative to the CARLA server
     *                       working directory, or absolute if an absolute
     *                       path is given.
     * @param additionalData when {@code true}, additional per-frame data
     *                       (such as snapshots of relevant actors) is
     *                       written alongside the recording. This makes
     *                       the file larger but enables richer post-mortem
     *                       analysis.
     * @return the absolute path written to by the server.
     * @throws CarlaException if the client is already closed, a recording
     *                        is already in progress, or the server
     *                        refuses the call.
     */
    public String startRecorder(String fileName, boolean additionalData) {
        return handle().StartRecorder(fileName, additionalData);
    }

    /**
     * Stops the current server-side recording, if any.
     *
     * <p>Calling this method when no recording is active is a no-op on the
     * server side and does not throw.</p>
     *
     * @throws CarlaException if the client is already closed.
     */
    public void stopRecorder() {
        handle().StopRecorder();
    }

    /**
     * Retrieves textual information about a previously recorded file.
     *
     * @param fileName name of the recording file on the server host.
     * @param showAll  when {@code true}, every recorded frame is included
     *                 in the output; when {@code false}, only a summary
     *                 is returned.
     * @return a multi-line text description of the recording contents.
     * @throws CarlaException if the client is already closed, the file
     *                        cannot be read, or the RPC fails.
     */
    public String showRecorderFileInfo(String fileName, boolean showAll) {
        return handle().ShowRecorderFileInfo(fileName, showAll);
    }

    /**
     * Extracts collision events from a recording.
     *
     * <p>The two {@code type1} and {@code type2} parameters act as a
     * filter: only collisions where the actor types match (in either
     * order) are returned. Use the constants in
     * {@link RecorderActorType} ({@link RecorderActorType#VEHICLE},
     * {@link RecorderActorType#WALKER}, etc.) to make the call
     * self-documenting.</p>
     *
     * @param fileName name of the recording file on the server host.
     * @param type1    first actor type to match (case-insensitive).
     * @param type2    second actor type to match (case-insensitive).
     * @return a multi-line text listing of matching collision events.
     * @throws CarlaException if the client is already closed or the file
     *                        cannot be read.
     * @see RecorderActorType
     */
    public String showRecorderCollisions(String fileName, char type1, char type2) {
        return handle().ShowRecorderCollisions(fileName, RecorderActorType.toNative(type1), RecorderActorType.toNative(type2));
    }

    /**
     * Lists actors that were considered "blocked" during a recording.
     *
     * <p>An actor is reported as blocked when it stays within a small
     * radius for a sustained period of time, which is usually a sign of
     * traffic congestion or a stuck vehicle.</p>
     *
     * @param fileName   name of the recording file on the server host.
     * @param minTime    minimum duration (in seconds) the actor must
     *                   stay blocked to be reported.
     * @param minDistance minimum distance (in meters) the actor must
     *                    stay within to be considered blocked.
     * @return a multi-line text listing of blocked actors.
     * @throws CarlaException if the client is already closed or the file
     *                        cannot be read.
     */
    public String showRecorderActorsBlocked(String fileName, double minTime, double minDistance) {
        return handle().ShowRecorderActorsBlocked(fileName, minTime, minDistance);
    }

    /**
     * Replays a recording on the server, with full control over the
     * replay window, the followed actor, sensor data, and an extra
     * positional offset applied to every replayed actor.
     *
     * @param fileName      name of the recording file on the server
     *                      host.
     * @param start         elapsed time (in seconds) at which to begin
     *                      replaying.
     * @param duration      how long (in seconds) to replay. Pass
     *                      {@code 0.0} to replay the full recording from
     *                      {@code start}.
     * @param followId      id of the actor that the spectator should
     *                      follow, or {@code 0} to leave the spectator
     *                      alone.
     * @param replaySensors when {@code true}, sensor data recorded in
     *                      the file is replayed too; when {@code false},
     *                      only the actor transforms are replayed.
     * @param offset        extra transform applied on top of the recorded
     *                      actor transforms; useful to shift the whole
     *                      replayed scene.
     * @return a textual status message from the server.
     * @throws CarlaException if the client is already closed, a replay
     *                        is already running, or the recording cannot
     *                        be opened.
     */
    public String replayFile(
        String fileName,
        double start,
        double duration,
        long followId,
        boolean replaySensors,
        Transform offset
    ) {
        return handle().ReplayFile(fileName, start, duration, followId, replaySensors, offset.toNative());
    }

    /**
     * Replays a recording with default replay options.
     *
     * <p>This convenience overload replays sensor data, uses the original
     * actor positions (no offset), and does not follow any specific
     * actor.</p>
     *
     * @param fileName name of the recording file on the server host.
     * @param start    elapsed time (in seconds) at which to begin
     *                 replaying.
     * @param duration how long (in seconds) to replay. Pass {@code 0.0}
     *                 to replay the full recording from {@code start}.
     * @param followId id of the actor that the spectator should follow,
     *                 or {@code 0} to leave the spectator alone.
     * @return a textual status message from the server.
     * @throws CarlaException if the client is already closed, a replay
     *                        is already running, or the recording cannot
     *                        be opened.
     */
    public String replayFile(String fileName, double start, double duration, long followId) {
        return replayFile(
            fileName,
            start,
            duration,
            followId,
            true,
            new Transform(new Location(0.0, 0.0, 0.0), new Rotation(0.0, 0.0, 0.0)));
    }

    /**
     * Stops an ongoing replay.
     *
     * @param keepActors when {@code true}, the actors that were created
     *                   on the server to perform the replay are kept
     *                   alive after stopping, so the simulation can
     *                   continue from the current state; when
     *                   {@code false}, they are destroyed and the
     *                   server is restored to its pre-replay state.
     * @throws CarlaException if the client is already closed.
     */
    public void stopReplayer(boolean keepActors) {
        handle().StopReplayer(keepActors);
    }

    /**
     * Sets the time factor of an ongoing replay.
     *
     * <p>Values greater than {@code 1.0} speed up the replay; values
     * between {@code 0.0} and {@code 1.0} slow it down. A value of
     * {@code 0.0} effectively pauses it.</p>
     *
     * @param timeFactor new time factor; must be non-negative.
     * @throws CarlaException if the client is already closed.
     */
    public void setReplayerTimeFactor(double timeFactor) {
        handle().SetReplayerTimeFactor(timeFactor);
    }

    /**
     * Configures whether the replayer should ignore the hero vehicle
     * recorded in the file.
     *
     * <p>This is useful when running user code that controls its own
     * vehicle, to prevent the replayed hero from interfering with the
     * live simulation.</p>
     *
     * @param ignoreHero when {@code true}, the recorded hero vehicle is
     *                   skipped during replay.
     * @throws CarlaException if the client is already closed.
     */
    public void setReplayerIgnoreHero(boolean ignoreHero) {
        handle().SetReplayerIgnoreHero(ignoreHero);
    }

    /**
     * Configures whether the replayer should ignore the recorded
     * spectator camera movements.
     *
     * @param ignoreSpectator when {@code true}, the replayed spectator
     *                        camera is not applied, leaving the
     *                        observer's current view untouched.
     * @throws CarlaException if the client is already closed.
     */
    public void setReplayerIgnoreSpectator(boolean ignoreSpectator) {
        handle().SetReplayerIgnoreSpectator(ignoreSpectator);
    }

    /**
     * Releases the underlying native client handle.
     *
     * <p>Called automatically by {@link #close()}; user code should not
     * invoke this method directly.</p>
     *
     * @param handle the native handle to release. Must be non-null.
     */
    @Override
    protected void release(CarlaNative.ClientHandle handle) {
        CarlaNative.DeleteClientHandle(handle);
    }
}
