package org.carla.javacpp.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;

import org.junit.jupiter.api.Test;

final class CarlaConnectionIT {
    @Test
    void connectsAndReadsWorld() {
        try (Client client = new Client("localhost", 2000)) {
            client.setTimeout(Duration.ofSeconds(10));
            try (World world = client.getWorld()) {
                assertNotNull(world.getMapName());
                assertFalse(world.getSpawnPoints().isEmpty());
            }
        }
    }
}
