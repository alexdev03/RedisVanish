package org.alexdev.redisvanish.data;

import com.velocitypowered.api.proxy.Player;
import org.alexdev.redisvanish.RedisVanish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {

    private final RedisVanish plugin;
    private final Map<UUID, User> users;

    public UserManager(RedisVanish plugin) {
        this.plugin = plugin;
        this.users = new ConcurrentHashMap<>();
        this.loadLocalUsers();
    }

    private void loadLocalUsers() {
        users.clear();
        final Set<CompletableFuture<User>> futures = ConcurrentHashMap.newKeySet();
        plugin.getServer().getAllPlayers().forEach(player -> futures.add(plugin.getRedis().loadUser(player.getUniqueId()).thenApply(user -> {
            if (user == null) {
                user = new User(player.getUniqueId(), player.getUsername());
                plugin.getRedis().saveUser(user);
            }

            addUser(user);
            return user;
        }).toCompletableFuture()));

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
            plugin.getLogger().info("Loaded " + users.size() + " users");
            sendUsersToServer(null);
        });
    }

    /**
     * Sends the users to the server.
     *
     * @param server the target server to send the users (nullable to send to all servers)
     */
    public void sendUsersToServer(@Nullable String server) {
        plugin.getRedis().sendLoadedUsers(plugin.getVanishManager().getUserServerMap(users.values()), server);
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
