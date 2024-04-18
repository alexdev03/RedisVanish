package org.alexdev.redisvanish.data;

import lombok.Getter;
import org.alexdev.redisvanish.RedisVanish;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class UserManager {

    private final RedisVanish plugin;
    @Getter
    private final Map<UUID, User> users;
    @Getter
    private final Map<UUID, RemoteUser> remoteUsers;
    @Getter
    private final List<RemoteUser> loadedRemoteUsers;

    public UserManager(RedisVanish plugin) {
        this.plugin = plugin;
        this.users = new ConcurrentHashMap<>();
        this.remoteUsers = new ConcurrentHashMap<>();
        this.loadedRemoteUsers = new CopyOnWriteArrayList<>();
        plugin.getRedis().requestUserCache();
//        this.loadLocalUsers();
        this.loadRemoteUsers();
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
                prepareRemoteUser(user);
                plugin.getVanishManager().hidePlayer(player);
            });
        });
    }

    private void loadRemoteUsers() {
        remoteUsers.clear();
        plugin.getRedis().getRemoteUsers().thenAccept(remoteUsers -> {
            if (remoteUsers == null) {
                return;
            }

            remoteUsers.forEach(this::addRemoteUser);
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
        loadedRemoteUsers.removeIf(remoteUser -> remoteUser.uuid().equals(user.uuid()));
    }

    public void removeUser(@NotNull UUID uuid) {
        users.remove(uuid);
        loadedRemoteUsers.removeIf(remoteUser -> remoteUser.uuid().equals(uuid));
    }

    public void replaceUser(@NotNull User user) {
        boolean wasVanished;
        final User old = users.get(user.uuid());

        final Player player = Bukkit.getPlayer(user.uuid());

        if (player==null) {
            return;
        }

        users.put(user.uuid(), user);

        if (old != null) {
            wasVanished = old.isVanished(plugin.getConfigManager().getConfig().getServerType());
        } else {
            wasVanished = false;
        }

        boolean isVanished = user.isVanished(plugin.getConfigManager().getConfig().getServerType());

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {

        }, 5);

        if (wasVanished != isVanished) {
            if (isVanished) {
                plugin.getVanishManager().hidePlayer(player);
            } else {
                plugin.getVanishManager().showPlayer(player);
            }
        }

    }

    public void addRemoteUser(@NotNull RemoteUser user) {
        remoteUsers.put(user.uuid(), user);
    }

    public void removeRemoteUser(@NotNull RemoteUser user) {
        remoteUsers.remove(user.uuid());
    }

    public void removeRemoteUser(@NotNull UUID uuid) {
        remoteUsers.remove(uuid);
    }

    public void replaceRemoteUser(@NotNull RemoteUser user) {
        remoteUsers.put(user.uuid(), user);
    }

    public Optional<RemoteUser> getRemoteUser(@NotNull UUID uuid) {
        return Optional.ofNullable(remoteUsers.get(uuid));
    }

    public Optional<RemoteUser> getRemoteUser(@NotNull String name) {
        return remoteUsers.values().stream().filter(user -> user.name().equalsIgnoreCase(name)).findFirst();
    }

    public void clearCache() {
        loadLocalUsers();
    }

    public void prepareRemoteUser(@NotNull User user) {
        try {
            Player player = Bukkit.getPlayer(user.uuid());
            if (player == null) {
//                plugin.getLogger().warning("Player " + user.name() + " is not online, cannot prepare remote user");
                return;
            }

            final boolean hasPermission = player.hasPermission("redisvanish.bypass");
            final boolean isVanished = plugin.getVanishManager().isVanished(user);
            final Optional<VanishLevel> vanishLevel = plugin.getVanishManager().getVanishLevel(player);
            final int vanishLevelInt = vanishLevel.map(v -> plugin.getRedis().getVanishLevels()
                    .entrySet().stream()
                    .filter(entry -> entry.getValue().equals(v))
                    .map(Map.Entry::getKey)
                    .findFirst().orElse(-1)).orElse(-1);


            RemoteUser remoteUser = new RemoteUser(user.uuid(), user.name(), hasPermission, isVanished, vanishLevelInt, System.currentTimeMillis());
            plugin.getRedis().sendRemoteUser(remoteUser);
            loadedRemoteUsers.add(remoteUser);
        } catch (Throwable e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Error preparing remote user", e);
        }
    }
}
