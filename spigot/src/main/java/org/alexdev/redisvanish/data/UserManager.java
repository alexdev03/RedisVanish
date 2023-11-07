package org.alexdev.redisvanish.data;

import org.alexdev.redisvanish.RedisVanish;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {

    private final RedisVanish plugin;
    private final Map<UUID, User> users;

    public UserManager(RedisVanish plugin) {
        this.plugin = plugin;
        this.users = new ConcurrentHashMap<>();
        loadLocalUsers();
    }

    private void loadLocalUsers() {
        users.clear();
        Bukkit.getOnlinePlayers().forEach(player -> {
            plugin.getRedis().loadUser(player.getUniqueId()).thenAccept(user -> {
                if (user == null) {
                    plugin.getLogger().warning("User " + player.getName() + " has no data in Redis, this should not happen");
                    return;
                }

                addUser(user);
            });
        });
    }

    @NotNull
    public User getUser(@NotNull Player player) {
        return users.get(player.getUniqueId());
    }

    public void addUser(@NotNull User user) {
        users.put(user.uuid(), user);
    }

    public void removeUser(@NotNull User user) {
        users.remove(user.uuid());
    }

    public void removeUser(@NotNull UUID uuid) {
        users.remove(uuid);
    }

    public void replaceUser(@NotNull User user) {
        boolean wasVanished = false;
        final User old = users.get(user.uuid());
        users.put(user.uuid(), user);

        if (old != null) {
            wasVanished = old.isVanished(plugin.getConfigManager().getConfig().getServerType());
        }

        boolean isVanished = user.isVanished(plugin.getConfigManager().getConfig().getServerType());

        if (wasVanished != isVanished) {
            if (isVanished) {
                plugin.getVanishManager().hidePlayer(Bukkit.getPlayer(user.uuid()));
            } else {
                plugin.getVanishManager().showPlayer(Bukkit.getPlayer(user.uuid()));
            }
        }

    }
}
