package org.alexdev.redisvanish.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;


@AllArgsConstructor
@Getter
@Accessors(fluent = true)
@Setter
public final class RemoteUser {
    private final UUID uuid;
    private final String name;
    private final String server;
    private final boolean bypass;
    private final boolean vanished;
    private final int vanishLevel;
    private long time;
}
