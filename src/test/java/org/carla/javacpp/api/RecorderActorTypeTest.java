package org.carla.javacpp.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

final class RecorderActorTypeTest {
    @Test
    void acceptsSupportedTypes() {
        assertEquals((byte) 'a', RecorderActorType.toNative('a'));
        assertEquals((byte) 'v', RecorderActorType.toNative('V'));
        assertEquals((byte) 'w', RecorderActorType.toNative('w'));
        assertEquals((byte) 't', RecorderActorType.toNative('T'));
        assertEquals((byte) 'o', RecorderActorType.toNative('o'));
    }

    @Test
    void rejectsUnsupportedTypes() {
        assertThrows(IllegalArgumentException.class, () -> RecorderActorType.toNative('x'));
    }
}
