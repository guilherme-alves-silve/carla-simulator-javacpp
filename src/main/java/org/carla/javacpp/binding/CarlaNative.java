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
