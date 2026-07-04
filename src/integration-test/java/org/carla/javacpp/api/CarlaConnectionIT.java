package org.carla.javacpp.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.Test;

final class CarlaConnectionIT {
    @Test
    void connectsAndReadsWorld() {
        try (var client = new Client("localhost", 2000)) {
            client.setTimeout(Duration.ofSeconds(10));
            try (var world = client.getWorld()) {
                assertNotNull(world.getMapName());
                assertFalse(world.getSpawnPoints().isEmpty());

                try (var map = world.getMap()) {
                    assertNotNull(map.getName());

                    var waypoint = map.getWaypoint(world.getSpawnPoints().get(0).location());
                    assertNotNull(waypoint);
                    assertTrue(waypoint.getRoadId() >= 0);

                    try (var next = waypoint.next(5.0)) {
                        assertFalse(next.isEmpty());
                    }
                }
            }
        }
    }
}
