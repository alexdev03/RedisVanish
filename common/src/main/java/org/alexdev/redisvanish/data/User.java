package org.alexdev.redisvanish.data;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public record User(UUID uuid, String name, Map<String, VanishContainer> vanishLevels) {

    public User(UUID uuid, String name) {
        this(uuid, name, new ConcurrentHashMap<>());
    }

    public boolean isVanished(String server) {
        VanishContainer container = vanishLevels.get(server);
        return container != null && container.isVanished();
    }

    public void setVanished(String server, boolean vanished) {
        VanishContainer container = vanishLevels.get(server);

        if (container == null) {
            container = new VanishContainer(new ConcurrentHashMap<>());
            vanishLevels.put(server, container);
        }

        container.setVanished(vanished);
    }

    public boolean hasProperty(VanishProperty vanishProperty, String server) {
        VanishContainer container = vanishLevels.get(server);
        return container != null && container.properties().getOrDefault(vanishProperty, false);
    }

    public void setProperty(VanishProperty vanishProperty, String server, boolean value) {
        VanishContainer container = vanishLevels.get(server);

        if (container == null) {
            container = new VanishContainer(new ConcurrentHashMap<>());
            vanishLevels.put(server, container);
        }

        container.properties().put(vanishProperty, value);
    }
}
