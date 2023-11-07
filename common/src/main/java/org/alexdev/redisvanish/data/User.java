package org.alexdev.redisvanish.data;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public record User(UUID uuid, String name, Map<String, Boolean> vanishLevels) {

    public User(UUID uuid, String name) {
        this(uuid, name, new ConcurrentHashMap<>());
    }

    public boolean isVanished(String server) {
        return vanishLevels.getOrDefault(server, false);
    }

    public void setVanished(String server, boolean vanished) {
        vanishLevels.put(server, vanished);
    }
}
