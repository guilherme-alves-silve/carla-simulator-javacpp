package org.carla.javacpp.binding;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.annotation.ByRef;
import org.bytedeco.javacpp.annotation.ByVal;
import org.bytedeco.javacpp.annotation.Cast;
import org.bytedeco.javacpp.annotation.Namespace;
import org.bytedeco.javacpp.annotation.NoOffset;
import org.bytedeco.javacpp.annotation.Platform;
import org.bytedeco.javacpp.annotation.Properties;
import org.bytedeco.javacpp.annotation.StdString;

@Properties(
    value = @Platform(
        include = {"CarlaBridge.h", "CarlaBridge.cpp"},
        link = {
            "carla_client",
            "rpc",
            "Detour",
            "DetourCrowd",
            "DetourTileCache",
            "Recast",
            "Shlwapi"
        },
        compiler = "cpp17"
    ))
public final class CarlaNative {
    static {
        Loader.load();
    }

    private CarlaNative() {
    }

    private static void load() {
        Loader.load();
    }

    @Namespace("carlajava")
    @NoOffset
    public static class LocationValue extends Pointer {
        static {
            CarlaNative.load();
        }

        public LocationValue() {
            allocate();
        }

        private native void allocate();

        public native double x();
        public native LocationValue x(double value);

        public native double y();
        public native LocationValue y(double value);

        public native double z();
        public native LocationValue z(double value);
    }

    @Namespace("carlajava")
    @NoOffset
    public static class RotationValue extends Pointer {
        static {
            CarlaNative.load();
        }

        public RotationValue() {
            allocate();
        }

        private native void allocate();

        public native double pitch();
        public native RotationValue pitch(double value);

        public native double yaw();
        public native RotationValue yaw(double value);

        public native double roll();
        public native RotationValue roll(double value);
    }

    @Namespace("carlajava")
    @NoOffset
    public static class TransformValue extends Pointer {
        static {
            CarlaNative.load();
        }

        public TransformValue() {
            allocate();
        }

        private native void allocate();

        @ByRef
        public native LocationValue location();
        public native TransformValue location(LocationValue value);

        @ByRef
        public native RotationValue rotation();
        public native TransformValue rotation(RotationValue value);
    }

    @Namespace("carlajava")
    @NoOffset
    public static class WorldSettingsValue extends Pointer {
        static {
            CarlaNative.load();
        }

        public WorldSettingsValue() {
            allocate();
        }

        private native void allocate();

        public native boolean synchronous_mode();
        public native WorldSettingsValue synchronous_mode(boolean value);

        public native boolean no_rendering_mode();
        public native WorldSettingsValue no_rendering_mode(boolean value);

        public native boolean has_fixed_delta_seconds();
        public native WorldSettingsValue has_fixed_delta_seconds(boolean value);

        public native double fixed_delta_seconds();
        public native WorldSettingsValue fixed_delta_seconds(double value);
    }

    @Namespace("carlajava")
    @NoOffset
    public static class WeatherParametersValue extends Pointer {
        static {
            CarlaNative.load();
        }

        public WeatherParametersValue() {
            allocate();
        }

        private native void allocate();

        public native float cloudiness();
        public native WeatherParametersValue cloudiness(float value);

        public native float precipitation();
        public native WeatherParametersValue precipitation(float value);

        public native float precipitation_deposits();
        public native WeatherParametersValue precipitation_deposits(float value);

        public native float wind_intensity();
        public native WeatherParametersValue wind_intensity(float value);

        public native float sun_azimuth_angle();
        public native WeatherParametersValue sun_azimuth_angle(float value);

        public native float sun_altitude_angle();
        public native WeatherParametersValue sun_altitude_angle(float value);

        public native float fog_density();
        public native WeatherParametersValue fog_density(float value);

        public native float fog_distance();
        public native WeatherParametersValue fog_distance(float value);

        public native float fog_falloff();
        public native WeatherParametersValue fog_falloff(float value);

        public native float wetness();
        public native WeatherParametersValue wetness(float value);

        public native float scattering_intensity();
        public native WeatherParametersValue scattering_intensity(float value);

        public native float mie_scattering_scale();
        public native WeatherParametersValue mie_scattering_scale(float value);

        public native float rayleigh_scattering_scale();
        public native WeatherParametersValue rayleigh_scattering_scale(float value);

        public native float dust_storm();
        public native WeatherParametersValue dust_storm(float value);
    }

    @Namespace("carlajava")
    @NoOffset
    public static class ClientHandle extends Pointer {
        static {
            CarlaNative.load();
        }

        public ClientHandle(String host, int port) {
            allocate(host, port);
        }

        private native void allocate(@StdString String host, @Cast("uint16_t") int port);

        public native void SetTimeoutMillis(long timeoutMillis);

        public native WorldHandle GetWorld();

        public native @StdString String StartRecorder(@StdString String name, boolean additionalData);

        public native void StopRecorder();

        public native @StdString String ShowRecorderFileInfo(@StdString String name, boolean showAll);

        public native @StdString String ShowRecorderCollisions(@StdString String name, byte type1, byte type2);

        public native @StdString String ShowRecorderActorsBlocked(
            @StdString String name,
            double minTime,
            double minDistance);

        public native @StdString String ReplayFile(
            @StdString String name,
            double start,
            double duration,
            @Cast("uint32_t") long followId,
            boolean replaySensors,
            @ByRef TransformValue offset);

        public native void StopReplayer(boolean keepActors);

        public native void SetReplayerTimeFactor(double timeFactor);

        public native void SetReplayerIgnoreHero(boolean ignoreHero);

        public native void SetReplayerIgnoreSpectator(boolean ignoreSpectator);
    }

    @Namespace("carlajava")
    @NoOffset
    public static class WorldHandle extends Pointer {
        static {
            CarlaNative.load();
        }

        public native @StdString String GetMapName();

        public native BlueprintLibraryHandle GetBlueprintLibrary();

        public native ActorListHandle GetActors();

        public native TransformListHandle GetSpawnPoints();

        @ByVal
        public native WorldSettingsValue GetSettings();

        public native long ApplySettings(@ByRef WorldSettingsValue settings, long timeoutMillis);

        public native long Tick(long timeoutMillis);

        @ByVal
        public native WeatherParametersValue GetWeather();

        public native void SetWeather(@ByRef WeatherParametersValue weather);

        public native ActorHandle SpawnActor(@ByRef BlueprintHandle blueprint, @ByRef TransformValue transform);

        public native ActorHandle TrySpawnActor(@ByRef BlueprintHandle blueprint, @ByRef TransformValue transform);

        public native CameraSensorHandle SpawnRgbCamera(
            @ByRef ActorHandle parent,
            @ByRef TransformValue transform,
            int width,
            int height,
            double fov);

        public native CollisionSensorHandle SpawnCollisionSensor(
            @ByRef ActorHandle parent,
            @ByRef TransformValue transform);

        public native LidarSensorHandle SpawnLidar(
            @ByRef ActorHandle parent,
            @ByRef TransformValue transform,
            int channels,
            double range,
            int pointsPerSecond,
            double rotationFrequency,
            double upperFov,
            double lowerFov);
    }

    @Namespace("carlajava")
    @NoOffset
    public static class BlueprintHandle extends Pointer {
        static {
            CarlaNative.load();
        }

        public native @StdString String GetId();

        public native void SetAttribute(@StdString String key, @StdString String value);
    }

    @Namespace("carlajava")
    @NoOffset
    public static class BlueprintLibraryHandle extends Pointer {
        static {
            CarlaNative.load();
        }

        public native BlueprintListHandle Filter(@StdString String pattern);

        public native BlueprintHandle Find(@StdString String id);
    }

    @Namespace("carlajava")
    @NoOffset
    public static class BlueprintListHandle extends Pointer {
        static {
            CarlaNative.load();
        }

        public native long Size();

        public native BlueprintHandle Get(long index);
    }

    @Namespace("carlajava")
    @NoOffset
    public static class ActorHandle extends Pointer {
        static {
            CarlaNative.load();
        }

        public native @Cast("uint32_t") long GetId();

        public native @StdString String GetTypeId();

        @ByVal
        public native TransformValue GetTransform();

        public native boolean Destroy();

        public native void SetAutopilot(boolean enabled, @Cast("uint16_t") int trafficManagerPort);

        public native void ApplyVehicleControl(
            float throttle,
            float steer,
            float brake,
            boolean handBrake,
            boolean reverse);
    }

    @Namespace("carlajava")
    @NoOffset
    public static class TransformListHandle extends Pointer {
        static {
            CarlaNative.load();
        }

        public native long Size();

        @ByVal
        public native TransformValue Get(long index);
    }

    @Namespace("carlajava")
    @NoOffset
    public static class ActorListHandle extends Pointer {
        static {
            CarlaNative.load();
        }

        public native long Size();

        public native ActorHandle Get(long index);
    }

    @Namespace("carlajava")
    @NoOffset
    public static class CameraSensorHandle extends Pointer {
        static {
            CarlaNative.load();
        }

        public native CameraImageHandle PollImage(long timeoutMillis);

        public native @Cast("uint32_t") long GetId();

        public native boolean Destroy();
    }

    @Namespace("carlajava")
    @NoOffset
    public static class CameraImageHandle extends Pointer {
        static {
            CarlaNative.load();
        }

        public native long GetFrame();

        public native double GetTimestamp();

        public native @Cast("uint32_t") int GetWidth();

        public native @Cast("uint32_t") int GetHeight();

        public native long Size();

        @Cast("const uint8_t*")
        public native BytePointer Data();
    }

    @Namespace("carlajava")
    @NoOffset
    public static class CollisionSensorHandle extends Pointer {
        static {
            CarlaNative.load();
        }

        public native CollisionEventHandle PollEvent(long timeoutMillis);

        public native @Cast("uint32_t") long GetId();

        public native boolean Destroy();
    }

    @Namespace("carlajava")
    @NoOffset
    public static class CollisionEventHandle extends Pointer {
        static {
            CarlaNative.load();
        }

        public native long GetFrame();

        public native double GetTimestamp();

        public native @Cast("uint32_t") long GetActorId();

        public native @Cast("uint32_t") long GetOtherActorId();

        public native @StdString String GetOtherActorTypeId();

        public native double GetNormalImpulseX();

        public native double GetNormalImpulseY();

        public native double GetNormalImpulseZ();
    }

    @Namespace("carlajava")
    @NoOffset
    public static class LidarSensorHandle extends Pointer {
        static {
            CarlaNative.load();
        }

        public native LidarMeasurementHandle PollMeasurement(long timeoutMillis);

        public native @Cast("uint32_t") long GetId();

        public native boolean Destroy();
    }

    @Namespace("carlajava")
    @NoOffset
    public static class LidarMeasurementHandle extends Pointer {
        static {
            CarlaNative.load();
        }

        public native long GetFrame();

        public native double GetTimestamp();

        public native float GetHorizontalAngle();

        public native @Cast("uint32_t") int GetChannelCount();

        public native long PointCount();

        public native long Size();

        @Cast("const float*")
        public native FloatPointer Data();
    }

    @Namespace("carlajava")
    public static native void DeleteClientHandle(ClientHandle handle);

    @Namespace("carlajava")
    public static native void DeleteWorldHandle(WorldHandle handle);

    @Namespace("carlajava")
    public static native void DeleteBlueprintLibraryHandle(BlueprintLibraryHandle handle);

    @Namespace("carlajava")
    public static native void DeleteBlueprintListHandle(BlueprintListHandle handle);

    @Namespace("carlajava")
    public static native void DeleteBlueprintHandle(BlueprintHandle handle);

    @Namespace("carlajava")
    public static native void DeleteActorHandle(ActorHandle handle);

    @Namespace("carlajava")
    public static native void DeleteActorListHandle(ActorListHandle handle);

    @Namespace("carlajava")
    public static native void DeleteTransformListHandle(TransformListHandle handle);

    @Namespace("carlajava")
    public static native void DeleteCameraSensorHandle(CameraSensorHandle handle);

    @Namespace("carlajava")
    public static native void DeleteCameraImageHandle(CameraImageHandle handle);

    @Namespace("carlajava")
    public static native void DeleteCollisionSensorHandle(CollisionSensorHandle handle);

    @Namespace("carlajava")
    public static native void DeleteCollisionEventHandle(CollisionEventHandle handle);

    @Namespace("carlajava")
    public static native void DeleteLidarSensorHandle(LidarSensorHandle handle);

    @Namespace("carlajava")
    public static native void DeleteLidarMeasurementHandle(LidarMeasurementHandle handle);
}
