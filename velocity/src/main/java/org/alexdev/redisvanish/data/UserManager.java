package org.alexdev.redisvanish.data;

import com.velocitypowered.api.proxy.Player;
import org.alexdev.redisvanish.RedisVanish;
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
        plugin.getServer().getAllPlayers().forEach(player -> {
            plugin.getRedis().loadUser(player.getUniqueId()).thenApply(user -> {
                if (user == null) {
                    user = new User(player.getUniqueId(), player.getUsername());
                    plugin.getRedis().saveUser(user);
                }

                addUser(user);
                return user;
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


    public void replaceUser(User user) {
        users.put(user.uuid(), user);
    }
}
