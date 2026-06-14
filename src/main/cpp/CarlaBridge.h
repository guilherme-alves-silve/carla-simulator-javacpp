#pragma once

#include <cstddef>
#include <cstdint>
#include <condition_variable>
#include <memory>
#include <mutex>
#include <string>
#include <vector>

#include <carla/Memory.h>

namespace carla {
namespace client {
class Actor;
class ActorList;
class BlueprintLibrary;
class ActorBlueprint;
class Client;
class Sensor;
class World;
} // namespace client
namespace geom {
class Transform;
} // namespace geom
} // namespace carla

namespace carlajava {

struct LocationValue {
  double x;
  double y;
  double z;
};

struct RotationValue {
  double pitch;
  double yaw;
  double roll;
};

struct TransformValue {
  LocationValue location;
  RotationValue rotation;
};

struct WorldSettingsValue {
  bool synchronous_mode;
  bool no_rendering_mode;
  bool has_fixed_delta_seconds;
  double fixed_delta_seconds;
};

struct WeatherParametersValue {
  float cloudiness;
  float precipitation;
  float precipitation_deposits;
  float wind_intensity;
  float sun_azimuth_angle;
  float sun_altitude_angle;
  float fog_density;
  float fog_distance;
  float fog_falloff;
  float wetness;
  float scattering_intensity;
  float mie_scattering_scale;
  float rayleigh_scattering_scale;
  float dust_storm;
};

class ClientHandle {
public:
  ClientHandle(const std::string &host, uint16_t port);
  ~ClientHandle();

  void SetTimeoutMillis(long long timeout_millis);
  class WorldHandle *GetWorld() const;
  std::string StartRecorder(const std::string &name, bool additional_data) const;
  void StopRecorder() const;
  std::string ShowRecorderFileInfo(const std::string &name, bool show_all) const;
  std::string ShowRecorderCollisions(const std::string &name, char type1, char type2) const;
  std::string ShowRecorderActorsBlocked(const std::string &name,
                                        double min_time,
                                        double min_distance) const;
  std::string ReplayFile(const std::string &name,
                         double start,
                         double duration,
                         uint32_t follow_id,
                         bool replay_sensors,
                         const TransformValue &offset) const;
  void StopReplayer(bool keep_actors) const;
  void SetReplayerTimeFactor(double time_factor) const;
  void SetReplayerIgnoreHero(bool ignore_hero) const;
  void SetReplayerIgnoreSpectator(bool ignore_spectator) const;

private:
  std::unique_ptr<carla::client::Client> client_;
};

class WorldHandle {
public:
  explicit WorldHandle(carla::client::World world);
  ~WorldHandle();

  std::string GetMapName() const;
  class BlueprintLibraryHandle *GetBlueprintLibrary() const;
  class ActorListHandle *GetActors() const;
  class TransformListHandle *GetSpawnPoints() const;
  WorldSettingsValue GetSettings() const;
  uint64_t ApplySettings(const WorldSettingsValue &settings, long long timeout_millis) const;
  uint64_t Tick(long long timeout_millis) const;
  WeatherParametersValue GetWeather() const;
  void SetWeather(const WeatherParametersValue &weather) const;
  class ActorHandle *SpawnActor(const class BlueprintHandle &blueprint,
                                const TransformValue &transform) const;
  class ActorHandle *TrySpawnActor(const class BlueprintHandle &blueprint,
                                   const TransformValue &transform) const;
  class CameraSensorHandle *SpawnRgbCamera(const class ActorHandle &parent,
                                           const TransformValue &transform,
                                           int width,
                                           int height,
                                           double fov) const;
  class CollisionSensorHandle *SpawnCollisionSensor(const class ActorHandle &parent,
                                                    const TransformValue &transform) const;
  class LidarSensorHandle *SpawnLidar(const class ActorHandle &parent,
                                      const TransformValue &transform,
                                      int channels,
                                      double range,
                                      int points_per_second,
                                      double rotation_frequency,
                                      double upper_fov,
                                      double lower_fov) const;

private:
  std::unique_ptr<carla::client::World> world_;
};

class BlueprintHandle {
public:
  explicit BlueprintHandle(carla::client::ActorBlueprint blueprint);
  ~BlueprintHandle();

  std::string GetId() const;
  void SetAttribute(const std::string &key, const std::string &value);
  const carla::client::ActorBlueprint &Get() const;

private:
  std::unique_ptr<carla::client::ActorBlueprint> blueprint_;
};

class BlueprintLibraryHandle {
public:
  explicit BlueprintLibraryHandle(carla::SharedPtr<carla::client::BlueprintLibrary> library);
  ~BlueprintLibraryHandle();

  class BlueprintListHandle *Filter(const std::string &pattern) const;
  class BlueprintHandle *Find(const std::string &id) const;

private:
  carla::SharedPtr<carla::client::BlueprintLibrary> library_;
};

class BlueprintListHandle {
public:
  explicit BlueprintListHandle(std::vector<BlueprintHandle *> blueprints);
  ~BlueprintListHandle();

  size_t Size() const;
  BlueprintHandle *Get(size_t index) const;

private:
  std::vector<BlueprintHandle *> blueprints_;
};

class ActorHandle {
public:
  explicit ActorHandle(carla::SharedPtr<carla::client::Actor> actor);
  ~ActorHandle();

  uint32_t GetId() const;
  std::string GetTypeId() const;
  TransformValue GetTransform() const;
  bool Destroy() const;
  void SetAutopilot(bool enabled, uint16_t traffic_manager_port);
  void ApplyVehicleControl(float throttle,
                           float steer,
                           float brake,
                           bool hand_brake,
                           bool reverse);
  const carla::SharedPtr<carla::client::Actor> &Get() const;

private:
  carla::SharedPtr<carla::client::Actor> actor_;
};

class TransformListHandle {
public:
  explicit TransformListHandle(std::vector<TransformValue> transforms);
  ~TransformListHandle();

  size_t Size() const;
  TransformValue Get(size_t index) const;

private:
  std::vector<TransformValue> transforms_;
};

class CameraImageHandle {
public:
  CameraImageHandle(size_t frame, double timestamp, uint32_t width, uint32_t height,
                    std::vector<uint8_t> bgra);
  ~CameraImageHandle();

  size_t GetFrame() const;
  double GetTimestamp() const;
  uint32_t GetWidth() const;
  uint32_t GetHeight() const;
  size_t Size() const;
  const uint8_t *Data() const;

private:
  size_t frame_;
  double timestamp_;
  uint32_t width_;
  uint32_t height_;
  std::vector<uint8_t> bgra_;
};

class CameraSensorHandle {
public:
  explicit CameraSensorHandle(carla::SharedPtr<carla::client::Actor> actor);
  ~CameraSensorHandle();

  CameraImageHandle *PollImage(long long timeout_millis);
  uint32_t GetId() const;
  bool Destroy() const;

private:
  struct FrameData {
    size_t frame;
    double timestamp;
    uint32_t width;
    uint32_t height;
    std::vector<uint8_t> bgra;
  };

  carla::SharedPtr<carla::client::Actor> actor_;
  carla::SharedPtr<carla::client::Sensor> sensor_;
  std::mutex mutex_;
  std::condition_variable condition_;
  bool has_frame_ = false;
  FrameData latest_frame_{};
};

class CollisionEventHandle {
public:
  CollisionEventHandle(size_t frame,
                       double timestamp,
                       uint32_t actor_id,
                       uint32_t other_actor_id,
                       std::string other_actor_type_id,
                       double normal_impulse_x,
                       double normal_impulse_y,
                       double normal_impulse_z);
  ~CollisionEventHandle();

  size_t GetFrame() const;
  double GetTimestamp() const;
  uint32_t GetActorId() const;
  uint32_t GetOtherActorId() const;
  std::string GetOtherActorTypeId() const;
  double GetNormalImpulseX() const;
  double GetNormalImpulseY() const;
  double GetNormalImpulseZ() const;

private:
  size_t frame_;
  double timestamp_;
  uint32_t actor_id_;
  uint32_t other_actor_id_;
  std::string other_actor_type_id_;
  double normal_impulse_x_;
  double normal_impulse_y_;
  double normal_impulse_z_;
};

class CollisionSensorHandle {
public:
  explicit CollisionSensorHandle(carla::SharedPtr<carla::client::Actor> actor);
  ~CollisionSensorHandle();

  CollisionEventHandle *PollEvent(long long timeout_millis);
  uint32_t GetId() const;
  bool Destroy() const;

private:
  struct EventData {
    size_t frame;
    double timestamp;
    uint32_t actor_id;
    uint32_t other_actor_id;
    std::string other_actor_type_id;
    double normal_impulse_x;
    double normal_impulse_y;
    double normal_impulse_z;
  };

  carla::SharedPtr<carla::client::Actor> actor_;
  carla::SharedPtr<carla::client::Sensor> sensor_;
  std::mutex mutex_;
  std::condition_variable condition_;
  bool has_event_ = false;
  EventData latest_event_{};
};

class LidarMeasurementHandle {
public:
  LidarMeasurementHandle(size_t frame,
                         double timestamp,
                         float horizontal_angle,
                         uint32_t channel_count,
                         std::vector<float> points);
  ~LidarMeasurementHandle();

  size_t GetFrame() const;
  double GetTimestamp() const;
  float GetHorizontalAngle() const;
  uint32_t GetChannelCount() const;
  size_t PointCount() const;
  size_t Size() const;
  const float *Data() const;

private:
  size_t frame_;
  double timestamp_;
  float horizontal_angle_;
  uint32_t channel_count_;
  std::vector<float> points_;
};

class LidarSensorHandle {
public:
  explicit LidarSensorHandle(carla::SharedPtr<carla::client::Actor> actor);
  ~LidarSensorHandle();

  LidarMeasurementHandle *PollMeasurement(long long timeout_millis);
  uint32_t GetId() const;
  bool Destroy() const;

private:
  struct MeasurementData {
    size_t frame;
    double timestamp;
    float horizontal_angle;
    uint32_t channel_count;
    std::vector<float> points;
  };

  carla::SharedPtr<carla::client::Actor> actor_;
  carla::SharedPtr<carla::client::Sensor> sensor_;
  std::mutex mutex_;
  std::condition_variable condition_;
  bool has_measurement_ = false;
  MeasurementData latest_measurement_{};
};

class ActorListHandle {
public:
  explicit ActorListHandle(std::vector<carla::SharedPtr<carla::client::Actor>> actors);
  ~ActorListHandle();

  size_t Size() const;
  ActorHandle *Get(size_t index) const;

private:
  std::vector<carla::SharedPtr<carla::client::Actor>> actors_;
};

void DeleteClientHandle(ClientHandle *handle);
void DeleteWorldHandle(WorldHandle *handle);
void DeleteBlueprintLibraryHandle(BlueprintLibraryHandle *handle);
void DeleteBlueprintListHandle(BlueprintListHandle *handle);
void DeleteBlueprintHandle(BlueprintHandle *handle);
void DeleteActorHandle(ActorHandle *handle);
void DeleteActorListHandle(ActorListHandle *handle);
void DeleteTransformListHandle(TransformListHandle *handle);
void DeleteCameraSensorHandle(CameraSensorHandle *handle);
void DeleteCameraImageHandle(CameraImageHandle *handle);
void DeleteCollisionSensorHandle(CollisionSensorHandle *handle);
void DeleteCollisionEventHandle(CollisionEventHandle *handle);
void DeleteLidarSensorHandle(LidarSensorHandle *handle);
void DeleteLidarMeasurementHandle(LidarMeasurementHandle *handle);

} // namespace carlajava
