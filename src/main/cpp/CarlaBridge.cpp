#include "CarlaBridge.h"

#include <chrono>
#include <cstring>
#include <stdexcept>
#include <string>

#include <carla/client/Actor.h>
#include <carla/client/ActorList.h>
#include <carla/client/ActorBlueprint.h>
#include <carla/client/BlueprintLibrary.h>
#include <carla/client/Client.h>
#include <carla/client/Map.h>
#include <carla/client/Sensor.h>
#include <carla/client/Vehicle.h>
#include <carla/client/World.h>
#include <carla/geom/Location.h>
#include <carla/geom/Rotation.h>
#include <carla/geom/Transform.h>
#include <carla/rpc/ActorId.h>
#include <carla/rpc/EpisodeSettings.h>
#include <carla/rpc/VehicleControl.h>
#include <carla/rpc/WeatherParameters.h>
#include <carla/sensor/SensorData.h>
#include <carla/sensor/data/CollisionEvent.h>
#include <carla/sensor/data/Image.h>
#include <carla/sensor/data/LidarMeasurement.h>

#include <boost/shared_ptr.hpp>

namespace carlajava {
namespace {

carla::geom::Transform ToCarlaTransform(const TransformValue &value) {
  return carla::geom::Transform(
      carla::geom::Location(
          static_cast<float>(value.location.x),
          static_cast<float>(value.location.y),
          static_cast<float>(value.location.z)),
      carla::geom::Rotation(
          static_cast<float>(value.rotation.pitch),
          static_cast<float>(value.rotation.yaw),
          static_cast<float>(value.rotation.roll)));
}

TransformValue FromCarlaTransform(const carla::geom::Transform &transform) {
  TransformValue value{};
  value.location.x = transform.location.x;
  value.location.y = transform.location.y;
  value.location.z = transform.location.z;
  value.rotation.pitch = transform.rotation.pitch;
  value.rotation.yaw = transform.rotation.yaw;
  value.rotation.roll = transform.rotation.roll;
  return value;
}

WorldSettingsValue FromCarlaSettings(const carla::rpc::EpisodeSettings &settings) {
  WorldSettingsValue value{};
  value.synchronous_mode = settings.synchronous_mode;
  value.no_rendering_mode = settings.no_rendering_mode;
  value.has_fixed_delta_seconds = static_cast<bool>(settings.fixed_delta_seconds);
  value.fixed_delta_seconds = settings.fixed_delta_seconds ? *settings.fixed_delta_seconds : 0.0;
  return value;
}

carla::rpc::EpisodeSettings ToCarlaSettings(const WorldSettingsValue &value) {
  carla::rpc::EpisodeSettings settings;
  settings.synchronous_mode = value.synchronous_mode;
  settings.no_rendering_mode = value.no_rendering_mode;
  if (value.has_fixed_delta_seconds && value.fixed_delta_seconds > 0.0) {
    settings.fixed_delta_seconds = value.fixed_delta_seconds;
  } else {
    settings.fixed_delta_seconds = boost::optional<double>{};
  }
  return settings;
}

WeatherParametersValue FromCarlaWeather(const carla::rpc::WeatherParameters &weather) {
  WeatherParametersValue value{};
  value.cloudiness = weather.cloudiness;
  value.precipitation = weather.precipitation;
  value.precipitation_deposits = weather.precipitation_deposits;
  value.wind_intensity = weather.wind_intensity;
  value.sun_azimuth_angle = weather.sun_azimuth_angle;
  value.sun_altitude_angle = weather.sun_altitude_angle;
  value.fog_density = weather.fog_density;
  value.fog_distance = weather.fog_distance;
  value.fog_falloff = weather.fog_falloff;
  value.wetness = weather.wetness;
  value.scattering_intensity = weather.scattering_intensity;
  value.mie_scattering_scale = weather.mie_scattering_scale;
  value.rayleigh_scattering_scale = weather.rayleigh_scattering_scale;
  value.dust_storm = weather.dust_storm;
  return value;
}

carla::rpc::WeatherParameters ToCarlaWeather(const WeatherParametersValue &value) {
  return carla::rpc::WeatherParameters(
      value.cloudiness,
      value.precipitation,
      value.precipitation_deposits,
      value.wind_intensity,
      value.sun_azimuth_angle,
      value.sun_altitude_angle,
      value.fog_density,
      value.fog_distance,
      value.fog_falloff,
      value.wetness,
      value.scattering_intensity,
      value.mie_scattering_scale,
      value.rayleigh_scattering_scale,
      value.dust_storm);
}

carla::client::ActorBlueprint GetFirstBlueprint(const carla::client::World &world,
                                                const std::string &id) {
  auto library = world.GetBlueprintLibrary();
  auto blueprints = library->Filter(id);
  if (blueprints->empty()) {
    throw std::runtime_error("CARLA blueprint not found: " + id);
  }
  return (*blueprints)[0];
}

} // namespace

ClientHandle::ClientHandle(const std::string &host, uint16_t port)
    : client_(std::make_unique<carla::client::Client>(host, port)) {}

ClientHandle::~ClientHandle() = default;

void ClientHandle::SetTimeoutMillis(long long timeout_millis) {
  client_->SetTimeout(std::chrono::milliseconds(timeout_millis));
}

WorldHandle *ClientHandle::GetWorld() const {
  return new WorldHandle(client_->GetWorld());
}

std::string ClientHandle::StartRecorder(const std::string &name, bool additional_data) const {
  return client_->StartRecorder(name, additional_data);
}

void ClientHandle::StopRecorder() const {
  client_->StopRecorder();
}

std::string ClientHandle::ShowRecorderFileInfo(const std::string &name, bool show_all) const {
  return client_->ShowRecorderFileInfo(name, show_all);
}

std::string ClientHandle::ShowRecorderCollisions(const std::string &name,
                                                 char type1,
                                                 char type2) const {
  return client_->ShowRecorderCollisions(name, type1, type2);
}

std::string ClientHandle::ShowRecorderActorsBlocked(const std::string &name,
                                                    double min_time,
                                                    double min_distance) const {
  return client_->ShowRecorderActorsBlocked(name, min_time, min_distance);
}

std::string ClientHandle::ReplayFile(const std::string &name,
                                     double start,
                                     double duration,
                                     uint32_t follow_id,
                                     bool replay_sensors,
                                     const TransformValue &offset) const {
  return client_->ReplayFile(
      name,
      start,
      duration,
      follow_id,
      replay_sensors,
      ToCarlaTransform(offset));
}

void ClientHandle::StopReplayer(bool keep_actors) const {
  client_->StopReplayer(keep_actors);
}

void ClientHandle::SetReplayerTimeFactor(double time_factor) const {
  client_->SetReplayerTimeFactor(time_factor);
}

void ClientHandle::SetReplayerIgnoreHero(bool ignore_hero) const {
  client_->SetReplayerIgnoreHero(ignore_hero);
}

void ClientHandle::SetReplayerIgnoreSpectator(bool ignore_spectator) const {
  client_->SetReplayerIgnoreSpectator(ignore_spectator);
}

WorldHandle::WorldHandle(carla::client::World world)
    : world_(std::make_unique<carla::client::World>(std::move(world))) {}

WorldHandle::~WorldHandle() = default;

std::string WorldHandle::GetMapName() const {
  return world_->GetMap()->GetName();
}

BlueprintLibraryHandle *WorldHandle::GetBlueprintLibrary() const {
  return new BlueprintLibraryHandle(world_->GetBlueprintLibrary());
}

ActorListHandle *WorldHandle::GetActors() const {
  std::vector<carla::SharedPtr<carla::client::Actor>> actors;
  for (auto &&actor : *world_->GetActors()) {
    actors.push_back(actor);
  }
  return new ActorListHandle(std::move(actors));
}

TransformListHandle *WorldHandle::GetSpawnPoints() const {
  std::vector<TransformValue> transforms;
  const auto &spawn_points = world_->GetMap()->GetRecommendedSpawnPoints();
  transforms.reserve(spawn_points.size());
  for (const auto &spawn_point : spawn_points) {
    transforms.push_back(FromCarlaTransform(spawn_point));
  }
  return new TransformListHandle(std::move(transforms));
}

WorldSettingsValue WorldHandle::GetSettings() const {
  return FromCarlaSettings(world_->GetSettings());
}

uint64_t WorldHandle::ApplySettings(const WorldSettingsValue &settings,
                                    long long timeout_millis) const {
  return world_->ApplySettings(
      ToCarlaSettings(settings),
      std::chrono::milliseconds(timeout_millis));
}

uint64_t WorldHandle::Tick(long long timeout_millis) const {
  return world_->Tick(std::chrono::milliseconds(timeout_millis));
}

WeatherParametersValue WorldHandle::GetWeather() const {
  return FromCarlaWeather(world_->GetWeather());
}

void WorldHandle::SetWeather(const WeatherParametersValue &weather) const {
  world_->SetWeather(ToCarlaWeather(weather));
}

ActorHandle *WorldHandle::SpawnActor(const BlueprintHandle &blueprint,
                                     const TransformValue &transform) const {
  auto actor = world_->SpawnActor(blueprint.Get(), ToCarlaTransform(transform));
  if (!actor) {
    throw std::runtime_error("CARLA returned an empty actor handle");
  }
  return new ActorHandle(actor);
}

ActorHandle *WorldHandle::TrySpawnActor(const BlueprintHandle &blueprint,
                                        const TransformValue &transform) const {
  auto actor = world_->TrySpawnActor(blueprint.Get(), ToCarlaTransform(transform));
  if (!actor) {
    return nullptr;
  }
  return new ActorHandle(actor);
}

CameraSensorHandle *WorldHandle::SpawnRgbCamera(const ActorHandle &parent,
                                                const TransformValue &transform,
                                                int width,
                                                int height,
                                                double fov) const {
  auto camera_blueprint = GetFirstBlueprint(*world_, "sensor.camera.rgb");
  camera_blueprint.SetAttribute("image_size_x", std::to_string(width));
  camera_blueprint.SetAttribute("image_size_y", std::to_string(height));
  camera_blueprint.SetAttribute("fov", std::to_string(fov));

  auto actor = world_->SpawnActor(
      camera_blueprint,
      ToCarlaTransform(transform),
      parent.Get().get());
  if (!actor) {
    throw std::runtime_error("CARLA returned an empty camera sensor handle");
  }
  return new CameraSensorHandle(actor);
}

CollisionSensorHandle *WorldHandle::SpawnCollisionSensor(const ActorHandle &parent,
                                                         const TransformValue &transform) const {
  auto blueprint = GetFirstBlueprint(*world_, "sensor.other.collision");
  auto actor = world_->SpawnActor(
      blueprint,
      ToCarlaTransform(transform),
      parent.Get().get());
  if (!actor) {
    throw std::runtime_error("CARLA returned an empty collision sensor handle");
  }
  return new CollisionSensorHandle(actor);
}

LidarSensorHandle *WorldHandle::SpawnLidar(const ActorHandle &parent,
                                           const TransformValue &transform,
                                           int channels,
                                           double range,
                                           int points_per_second,
                                           double rotation_frequency,
                                           double upper_fov,
                                           double lower_fov) const {
  auto blueprint = GetFirstBlueprint(*world_, "sensor.lidar.ray_cast");
  blueprint.SetAttribute("channels", std::to_string(channels));
  blueprint.SetAttribute("range", std::to_string(range));
  blueprint.SetAttribute("points_per_second", std::to_string(points_per_second));
  blueprint.SetAttribute("rotation_frequency", std::to_string(rotation_frequency));
  blueprint.SetAttribute("upper_fov", std::to_string(upper_fov));
  blueprint.SetAttribute("lower_fov", std::to_string(lower_fov));

  auto actor = world_->SpawnActor(
      blueprint,
      ToCarlaTransform(transform),
      parent.Get().get());
  if (!actor) {
    throw std::runtime_error("CARLA returned an empty lidar sensor handle");
  }
  return new LidarSensorHandle(actor);
}

BlueprintHandle::BlueprintHandle(carla::client::ActorBlueprint blueprint)
    : blueprint_(std::make_unique<carla::client::ActorBlueprint>(std::move(blueprint))) {}

BlueprintHandle::~BlueprintHandle() = default;

std::string BlueprintHandle::GetId() const {
  return blueprint_->GetId();
}

void BlueprintHandle::SetAttribute(const std::string &key, const std::string &value) {
  blueprint_->SetAttribute(key, value);
}

const carla::client::ActorBlueprint &BlueprintHandle::Get() const {
  return *blueprint_;
}

BlueprintLibraryHandle::BlueprintLibraryHandle(carla::SharedPtr<carla::client::BlueprintLibrary> library)
    : library_(std::move(library)) {}

BlueprintLibraryHandle::~BlueprintLibraryHandle() = default;

BlueprintListHandle *BlueprintLibraryHandle::Filter(const std::string &pattern) const {
  std::vector<BlueprintHandle *> blueprints;
  auto filtered = library_->Filter(pattern);
  for (const auto &blueprint : *filtered) {
    blueprints.push_back(new BlueprintHandle(blueprint));
  }
  return new BlueprintListHandle(std::move(blueprints));
}

BlueprintHandle *BlueprintLibraryHandle::Find(const std::string &id) const {
  const auto *blueprint = library_->Find(id);
  if (!blueprint) {
    return nullptr;
  }
  return new BlueprintHandle(*blueprint);
}

BlueprintListHandle::BlueprintListHandle(std::vector<BlueprintHandle *> blueprints)
    : blueprints_(std::move(blueprints)) {}

BlueprintListHandle::~BlueprintListHandle() = default;

size_t BlueprintListHandle::Size() const {
  return blueprints_.size();
}

BlueprintHandle *BlueprintListHandle::Get(size_t index) const {
  if (index >= blueprints_.size()) {
    throw std::out_of_range("blueprint index out of range");
  }
  return blueprints_[index];
}

ActorHandle::ActorHandle(carla::SharedPtr<carla::client::Actor> actor)
    : actor_(std::move(actor)) {}

ActorHandle::~ActorHandle() = default;

uint32_t ActorHandle::GetId() const {
  return actor_->GetId();
}

std::string ActorHandle::GetTypeId() const {
  return actor_->GetTypeId();
}

TransformValue ActorHandle::GetTransform() const {
  return FromCarlaTransform(actor_->GetTransform());
}

bool ActorHandle::Destroy() const {
  return actor_->Destroy();
}

void ActorHandle::SetAutopilot(bool enabled, uint16_t traffic_manager_port) {
  auto vehicle = boost::dynamic_pointer_cast<carla::client::Vehicle>(actor_);
  if (!vehicle) {
    throw std::runtime_error("actor is not a CARLA vehicle");
  }
  vehicle->SetAutopilot(enabled, traffic_manager_port);
}

void ActorHandle::ApplyVehicleControl(float throttle,
                                      float steer,
                                      float brake,
                                      bool hand_brake,
                                      bool reverse) {
  auto vehicle = boost::dynamic_pointer_cast<carla::client::Vehicle>(actor_);
  if (!vehicle) {
    throw std::runtime_error("actor is not a CARLA vehicle");
  }

  carla::rpc::VehicleControl control;
  control.throttle = throttle;
  control.steer = steer;
  control.brake = brake;
  control.hand_brake = hand_brake;
  control.reverse = reverse;
  vehicle->ApplyControl(control);
}

const carla::SharedPtr<carla::client::Actor> &ActorHandle::Get() const {
  return actor_;
}

TransformListHandle::TransformListHandle(std::vector<TransformValue> transforms)
    : transforms_(std::move(transforms)) {}

TransformListHandle::~TransformListHandle() = default;

size_t TransformListHandle::Size() const {
  return transforms_.size();
}

TransformValue TransformListHandle::Get(size_t index) const {
  if (index >= transforms_.size()) {
    throw std::out_of_range("transform index out of range");
  }
  return transforms_[index];
}

CameraImageHandle::CameraImageHandle(size_t frame,
                                     double timestamp,
                                     uint32_t width,
                                     uint32_t height,
                                     std::vector<uint8_t> bgra)
    : frame_(frame),
      timestamp_(timestamp),
      width_(width),
      height_(height),
      bgra_(std::move(bgra)) {}

CameraImageHandle::~CameraImageHandle() = default;

size_t CameraImageHandle::GetFrame() const {
  return frame_;
}

double CameraImageHandle::GetTimestamp() const {
  return timestamp_;
}

uint32_t CameraImageHandle::GetWidth() const {
  return width_;
}

uint32_t CameraImageHandle::GetHeight() const {
  return height_;
}

size_t CameraImageHandle::Size() const {
  return bgra_.size();
}

const uint8_t *CameraImageHandle::Data() const {
  return bgra_.data();
}

CameraSensorHandle::CameraSensorHandle(carla::SharedPtr<carla::client::Actor> actor)
    : actor_(std::move(actor)),
      sensor_(boost::dynamic_pointer_cast<carla::client::Sensor>(actor_)) {
  if (!sensor_) {
    throw std::runtime_error("spawned actor is not a CARLA sensor");
  }

  sensor_->Listen([this](carla::SharedPtr<carla::sensor::SensorData> data) {
    auto image = boost::dynamic_pointer_cast<carla::sensor::data::Image>(data);
    if (!image) {
      return;
    }

    FrameData frame;
    frame.frame = image->GetFrame();
    frame.timestamp = image->GetTimestamp();
    frame.width = image->GetWidth();
    frame.height = image->GetHeight();
    frame.bgra.resize(image->size() * sizeof(carla::sensor::data::Color));
    std::memcpy(frame.bgra.data(), image->data(), frame.bgra.size());

    {
      std::lock_guard<std::mutex> lock(mutex_);
      latest_frame_ = std::move(frame);
      has_frame_ = true;
    }
    condition_.notify_one();
  });
}

CameraSensorHandle::~CameraSensorHandle() {
  if (sensor_ && sensor_->IsListening()) {
    sensor_->Stop();
  }
}

CameraImageHandle *CameraSensorHandle::PollImage(long long timeout_millis) {
  std::unique_lock<std::mutex> lock(mutex_);
  const bool ready = condition_.wait_for(
      lock,
      std::chrono::milliseconds(timeout_millis),
      [this] { return has_frame_; });
  if (!ready) {
    return nullptr;
  }

  has_frame_ = false;
  FrameData frame = latest_frame_;
  return new CameraImageHandle(
      frame.frame,
      frame.timestamp,
      frame.width,
      frame.height,
      std::move(frame.bgra));
}

uint32_t CameraSensorHandle::GetId() const {
  return actor_->GetId();
}

bool CameraSensorHandle::Destroy() const {
  return actor_->Destroy();
}

CollisionEventHandle::CollisionEventHandle(size_t frame,
                                           double timestamp,
                                           uint32_t actor_id,
                                           uint32_t other_actor_id,
                                           std::string other_actor_type_id,
                                           double normal_impulse_x,
                                           double normal_impulse_y,
                                           double normal_impulse_z)
    : frame_(frame),
      timestamp_(timestamp),
      actor_id_(actor_id),
      other_actor_id_(other_actor_id),
      other_actor_type_id_(std::move(other_actor_type_id)),
      normal_impulse_x_(normal_impulse_x),
      normal_impulse_y_(normal_impulse_y),
      normal_impulse_z_(normal_impulse_z) {}

CollisionEventHandle::~CollisionEventHandle() = default;

size_t CollisionEventHandle::GetFrame() const {
  return frame_;
}

double CollisionEventHandle::GetTimestamp() const {
  return timestamp_;
}

uint32_t CollisionEventHandle::GetActorId() const {
  return actor_id_;
}

uint32_t CollisionEventHandle::GetOtherActorId() const {
  return other_actor_id_;
}

std::string CollisionEventHandle::GetOtherActorTypeId() const {
  return other_actor_type_id_;
}

double CollisionEventHandle::GetNormalImpulseX() const {
  return normal_impulse_x_;
}

double CollisionEventHandle::GetNormalImpulseY() const {
  return normal_impulse_y_;
}

double CollisionEventHandle::GetNormalImpulseZ() const {
  return normal_impulse_z_;
}

CollisionSensorHandle::CollisionSensorHandle(carla::SharedPtr<carla::client::Actor> actor)
    : actor_(std::move(actor)),
      sensor_(boost::dynamic_pointer_cast<carla::client::Sensor>(actor_)) {
  if (!sensor_) {
    throw std::runtime_error("spawned actor is not a CARLA sensor");
  }

  sensor_->Listen([this](carla::SharedPtr<carla::sensor::SensorData> data) {
    auto collision = boost::dynamic_pointer_cast<carla::sensor::data::CollisionEvent>(data);
    if (!collision) {
      return;
    }

    auto actor = collision->GetActor();
    auto other_actor = collision->GetOtherActor();
    auto impulse = collision->GetNormalImpulse();

    EventData event;
    event.frame = collision->GetFrame();
    event.timestamp = collision->GetTimestamp();
    event.actor_id = actor ? actor->GetId() : 0u;
    event.other_actor_id = other_actor ? other_actor->GetId() : 0u;
    event.other_actor_type_id = other_actor ? other_actor->GetTypeId() : "";
    event.normal_impulse_x = impulse.x;
    event.normal_impulse_y = impulse.y;
    event.normal_impulse_z = impulse.z;

    {
      std::lock_guard<std::mutex> lock(mutex_);
      latest_event_ = std::move(event);
      has_event_ = true;
    }
    condition_.notify_one();
  });
}

CollisionSensorHandle::~CollisionSensorHandle() {
  if (sensor_ && sensor_->IsListening()) {
    sensor_->Stop();
  }
}

CollisionEventHandle *CollisionSensorHandle::PollEvent(long long timeout_millis) {
  std::unique_lock<std::mutex> lock(mutex_);
  const bool ready = condition_.wait_for(
      lock,
      std::chrono::milliseconds(timeout_millis),
      [this] { return has_event_; });
  if (!ready) {
    return nullptr;
  }

  has_event_ = false;
  EventData event = latest_event_;
  return new CollisionEventHandle(
      event.frame,
      event.timestamp,
      event.actor_id,
      event.other_actor_id,
      event.other_actor_type_id,
      event.normal_impulse_x,
      event.normal_impulse_y,
      event.normal_impulse_z);
}

uint32_t CollisionSensorHandle::GetId() const {
  return actor_->GetId();
}

bool CollisionSensorHandle::Destroy() const {
  return actor_->Destroy();
}

LidarMeasurementHandle::LidarMeasurementHandle(size_t frame,
                                               double timestamp,
                                               float horizontal_angle,
                                               uint32_t channel_count,
                                               std::vector<float> points)
    : frame_(frame),
      timestamp_(timestamp),
      horizontal_angle_(horizontal_angle),
      channel_count_(channel_count),
      points_(std::move(points)) {}

LidarMeasurementHandle::~LidarMeasurementHandle() = default;

size_t LidarMeasurementHandle::GetFrame() const {
  return frame_;
}

double LidarMeasurementHandle::GetTimestamp() const {
  return timestamp_;
}

float LidarMeasurementHandle::GetHorizontalAngle() const {
  return horizontal_angle_;
}

uint32_t LidarMeasurementHandle::GetChannelCount() const {
  return channel_count_;
}

size_t LidarMeasurementHandle::PointCount() const {
  return points_.size() / 4u;
}

size_t LidarMeasurementHandle::Size() const {
  return points_.size();
}

const float *LidarMeasurementHandle::Data() const {
  return points_.data();
}

LidarSensorHandle::LidarSensorHandle(carla::SharedPtr<carla::client::Actor> actor)
    : actor_(std::move(actor)),
      sensor_(boost::dynamic_pointer_cast<carla::client::Sensor>(actor_)) {
  if (!sensor_) {
    throw std::runtime_error("spawned actor is not a CARLA sensor");
  }

  sensor_->Listen([this](carla::SharedPtr<carla::sensor::SensorData> data) {
    auto lidar = boost::dynamic_pointer_cast<carla::sensor::data::LidarMeasurement>(data);
    if (!lidar) {
      return;
    }

    MeasurementData measurement;
    measurement.frame = lidar->GetFrame();
    measurement.timestamp = lidar->GetTimestamp();
    measurement.horizontal_angle = lidar->GetHorizontalAngle();
    measurement.channel_count = lidar->GetChannelCount();
    measurement.points.reserve(lidar->size() * 4u);
    for (const auto &detection : *lidar) {
      measurement.points.push_back(detection.point.x);
      measurement.points.push_back(detection.point.y);
      measurement.points.push_back(detection.point.z);
      measurement.points.push_back(detection.intensity);
    }

    {
      std::lock_guard<std::mutex> lock(mutex_);
      latest_measurement_ = std::move(measurement);
      has_measurement_ = true;
    }
    condition_.notify_one();
  });
}

LidarSensorHandle::~LidarSensorHandle() {
  if (sensor_ && sensor_->IsListening()) {
    sensor_->Stop();
  }
}

LidarMeasurementHandle *LidarSensorHandle::PollMeasurement(long long timeout_millis) {
  std::unique_lock<std::mutex> lock(mutex_);
  const bool ready = condition_.wait_for(
      lock,
      std::chrono::milliseconds(timeout_millis),
      [this] { return has_measurement_; });
  if (!ready) {
    return nullptr;
  }

  has_measurement_ = false;
  MeasurementData measurement = latest_measurement_;
  return new LidarMeasurementHandle(
      measurement.frame,
      measurement.timestamp,
      measurement.horizontal_angle,
      measurement.channel_count,
      std::move(measurement.points));
}

uint32_t LidarSensorHandle::GetId() const {
  return actor_->GetId();
}

bool LidarSensorHandle::Destroy() const {
  return actor_->Destroy();
}

ActorListHandle::ActorListHandle(std::vector<carla::SharedPtr<carla::client::Actor>> actors)
    : actors_(std::move(actors)) {}

ActorListHandle::~ActorListHandle() = default;

size_t ActorListHandle::Size() const {
  return actors_.size();
}

ActorHandle *ActorListHandle::Get(size_t index) const {
  if (index >= actors_.size()) {
    throw std::out_of_range("actor index out of range");
  }
  return new ActorHandle(actors_[index]);
}

void DeleteClientHandle(ClientHandle *handle) {
  delete handle;
}

void DeleteWorldHandle(WorldHandle *handle) {
  delete handle;
}

void DeleteBlueprintLibraryHandle(BlueprintLibraryHandle *handle) {
  delete handle;
}

void DeleteBlueprintListHandle(BlueprintListHandle *handle) {
  delete handle;
}

void DeleteBlueprintHandle(BlueprintHandle *handle) {
  delete handle;
}

void DeleteActorHandle(ActorHandle *handle) {
  delete handle;
}

void DeleteActorListHandle(ActorListHandle *handle) {
  delete handle;
}

void DeleteTransformListHandle(TransformListHandle *handle) {
  delete handle;
}

void DeleteCameraSensorHandle(CameraSensorHandle *handle) {
  delete handle;
}

void DeleteCameraImageHandle(CameraImageHandle *handle) {
  delete handle;
}

void DeleteCollisionSensorHandle(CollisionSensorHandle *handle) {
  delete handle;
}

void DeleteCollisionEventHandle(CollisionEventHandle *handle) {
  delete handle;
}

void DeleteLidarSensorHandle(LidarSensorHandle *handle) {
  delete handle;
}

void DeleteLidarMeasurementHandle(LidarMeasurementHandle *handle) {
  delete handle;
}

} // namespace carlajava
