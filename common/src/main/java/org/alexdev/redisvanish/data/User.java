package org.alexdev.redisvanish.data;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public record User(@NotNull UUID uuid, @NotNull String name, @NotNull Map<String, VanishContainer> vanishLevels) {

    public User(@NotNull UUID uuid, @NotNull String name) {
        this(uuid, name, new ConcurrentHashMap<>());
    }

    public boolean isVanished(@NotNull String server) {
        final VanishContainer container = vanishLevels.get(server);
        return container != null && container.isVanished();
    }

    public void setVanished(@NotNull String server, boolean vanished) {
        VanishContainer container = vanishLevels.get(server);

        if (container == null) {
            container = new VanishContainer(new ConcurrentHashMap<>());
            vanishLevels.put(server, container);
        }

        container.setVanished(vanished);
    }

    public boolean hasProperty(@NotNull VanishProperty vanishProperty, @NotNull String server) {
        final VanishContainer container = vanishLevels.get(server);
        return container != null && container.properties().getOrDefault(vanishProperty, false);
    }

    public void setProperty(@NotNull VanishProperty vanishProperty, String server, boolean value) {
        VanishContainer container = vanishLevels.get(server);

        if (container == null) {
            container = new VanishContainer(new ConcurrentHashMap<>());
            vanishLevels.put(server, container);
        }

        container.properties().put(vanishProperty, value);
    }
}
