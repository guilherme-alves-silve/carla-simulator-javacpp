import argparse
import random
import time

import carla


def main():
    parser = argparse.ArgumentParser(description="Minimal CARLA Python smoke test.")
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=2000)
    parser.add_argument("--timeout", type=float, default=10.0)
    parser.add_argument("--seconds", type=float, default=5.0)
    args = parser.parse_args()

    actor = None
    client = carla.Client(args.host, args.port)
    client.set_timeout(args.timeout)

    world = client.get_world()
    print("Connected to CARLA")
    print("Map:", world.get_map().name)

    blueprint_library = world.get_blueprint_library()
    vehicle_bp = blueprint_library.find("vehicle.lincoln.mkz_2020")
    spawn_points = world.get_map().get_spawn_points()

    if not spawn_points:
        raise RuntimeError("Current map has no spawn points")

    actor = world.try_spawn_actor(vehicle_bp, random.choice(spawn_points))
    if actor is None:
        raise RuntimeError("Could not spawn test vehicle")

    print("Spawned actor:", actor.id, actor.type_id)
    actor.set_autopilot(True)
    time.sleep(args.seconds)

    location = actor.get_location()
    print("Final location: x={:.2f} y={:.2f} z={:.2f}".format(location.x, location.y, location.z))

    actor.destroy()
    print("Destroyed actor")


if __name__ == "__main__":
    main()
