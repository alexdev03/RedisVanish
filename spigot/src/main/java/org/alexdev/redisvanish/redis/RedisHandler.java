package org.alexdev.redisvanish.redis;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import lombok.Getter;
import org.alexdev.redisvanish.RedisVanish;
import org.alexdev.redisvanish.data.RemoteUser;
import org.alexdev.redisvanish.data.User;
import org.alexdev.redisvanish.data.VanishLevel;
import org.alexdev.redisvanish.redis.data.RedisKeys;
import org.alexdev.redisvanish.redis.data.RedisPubSub;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;

public class RedisHandler extends RedisImplementation {

    private final RedisVanish plugin;
    @Getter
    private final Map<Integer, VanishLevel> vanishLevels;

    public RedisHandler(RedisClient lettuceRedisClient, int size, RedisVanish plugin) {
        super(lettuceRedisClient, size);
        this.plugin = plugin;
        this.vanishLevels = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
        this.subscribe();
        this.loadVanishLevels();
        this.requestVanishLevels();
        this.publishTask();
    }

    private void subscribe() {
        getPubSubConnection(c -> {
            c.addListener(new RedisPubSub<>() {
                @Override
                public void message(String channel, String message) {
                    final JsonObject object = gson.fromJson(message, JsonObject.class);
                    final String targetServer = object.get("server").getAsString();
                    if (isNotSameServerType(targetServer)) {
                        return;
                    }
                    final JsonArray users = object.getAsJsonArray("users");
                    users.forEach(user -> {
                        final JsonObject userObject = user.getAsJsonObject();
                        final String server = userObject.get("server").getAsString();
                        if (isNotSameServerType(server)) {
                            return;
                        }
                        final User u = gson.fromJson(userObject.getAsJsonObject("user"), User.class);
                        plugin.getUserManager().addUser(u);
                    });
                }
            });
            c.async().subscribe(RedisKeys.USER_SET_CACHE.getKey());
        });
        getPubSubConnection(c -> {
            c.addListener(new RedisPubSub<>() {
                @Override
                public void message(String channel, String message) {
                    final JsonObject object = gson.fromJson(message, JsonObject.class);
                    final String server = object.get("server").getAsString();
                    if (isNotSameServerType(server)) {
                        return;
                    }
                    final User user = gson.fromJson(object.getAsJsonObject("user"), User.class);
                    plugin.getUserManager().addUser(user);
                }
            });
            c.async().subscribe(RedisKeys.USER_JOIN.getKey());
        });

        getPubSubConnection(c -> {
            c.addListener(new RedisPubSub<>() {
                @Override
                public void message(String channel, String message) {
                    final JsonObject object = gson.fromJson(message, JsonObject.class);
                    final String server = object.get("server").getAsString();
                    if (isNotSameServerType(server)) {
                        return;
                    }
                    final UUID uuid = UUID.fromString(object.get("uuid").getAsString());
                    plugin.getUserManager().removeUser(uuid);
                }
            });
            c.async().subscribe(RedisKeys.USER_LEAVE.getKey());
        });


        getPubSubConnection(c -> {
            c.addListener(new RedisPubSub<>() {
                @Override
                public void message(String channel, String message) {
                    try {
                        User user = gson.fromJson(message, User.class);
                        plugin.getUserManager().replaceUser(user);
//                        plugin.getUserManager().prepareRemoteUser(user);
                    } catch (Exception ex) {
                        plugin.getLogger().log(Level.SEVERE, "Error parsing user update", ex);
                    }
                }
            });
            c.async().subscribe(RedisKeys.USER_UPDATE.getKey());
        });

        getPubSubConnection(c -> {
            c.addListener(new RedisPubSub<>() {
                @Override
                public void message(String channel, String message) {
                    Map<Integer, VanishLevel> newLevels = deserialize(message);
                    vanishLevels.clear();
                    vanishLevels.putAll(newLevels);
                }
            });
            c.async().subscribe(RedisKeys.VANISH_LEVELS_UPDATE.getKey());
        });

        getPubSubConnection((StatefulRedisPubSubConnection<String, String> c) -> {
            c.addListener(new RedisPubSub<>() {
                @Override
                public void message(String channel, String message) {
                    try {
                        final JsonObject object = gson.fromJson(message, JsonObject.class);

                        final int type = object.get("type").getAsInt();

                        if (type == 1) {
                            final RemoteUser user = gson.fromJson(object.get("user"), RemoteUser.class);
                            plugin.getUserManager().addRemoteUser(user);
                        } else if (type == 2) {
                            final String group = object.get("group").getAsString();
                            if (!plugin.getConfigManager().getConfig().getServerType().equals(group)) return;
                            final UUID uuid = UUID.fromString(object.get("uuid").getAsString());
                            plugin.getUserManager().removeRemoteUser(uuid);
                        } else if (type == 3) {
                            final List<UUID> users = gson.fromJson(object.get("users"), new TypeToken<List<UUID>>() {
                            }.getType());

                            users.forEach(plugin.getUserManager()::removeRemoteUser);
                        }
                    } catch (Exception ex) {
                        plugin.getLogger().log(Level.SEVERE, "Error parsing remote user update", ex);
                    }
                }
            });
            c.async().subscribe(RedisKeys.REMOTE_USER_UPDATE.getKey());
        });
    }

    public void sendRemoteUser(@NotNull RemoteUser user) {
        JsonObject object = new JsonObject();
        object.addProperty("type", 1);
        object.add("user", gson.toJsonTree(user));
        object.addProperty("time", System.currentTimeMillis());
        this.getConnectionAsync(connection -> {
            connection.hset(RedisKeys.REMOTE_USER.getKey(), user.uuid().toString(), object.toString());
            return connection.publish(RedisKeys.REMOTE_USER_UPDATE.getKey(), object.toString());
        });
    }

    private boolean isNotSameServerType(String server) {
        return !server.isEmpty() && !plugin.getConfigManager().getConfig().getServerType().equals(server);
    }

    public void requestUserCache() {
        this.getConnectionAsync(connection -> connection.publish(RedisKeys.USER_CACHE_REQUEST.getKey(), plugin.getConfigManager().getConfig().getServerType()));
    }

    public void requestVanishLevels() {
        this.getConnectionAsync(connection -> connection.publish(RedisKeys.VANISH_LEVELS_REQUEST.getKey(), ""));
    }

    private void loadVanishLevels() {
        getConnectionAsync(c -> c.get(RedisKeys.VANISH_LEVELS.getKey()).thenAccept(result -> {
            if (result == null) {
                return;
            }

            Map<Integer, VanishLevel> newLevels = deserialize(result);
            vanishLevels.clear();
            vanishLevels.putAll(newLevels);
            plugin.getLogger().info("Loaded " + vanishLevels.size() + " vanish levels");
        })).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "Error loading vanish levels", ex);
            return null;
        });
    }

    private Map<Integer, VanishLevel> deserialize(@NotNull String json) {
        Type empMapType = new TypeToken<Map<Integer, VanishLevel>>() {
        }.getType();
        return gson.fromJson(json, empMapType);
    }

    public CompletionStage<List<RemoteUser>> getRemoteUsers() {
        return getConnectionAsync(connection -> connection.hgetall(RedisKeys.REMOTE_USER.getKey())
                .thenApply(map -> gson.fromJson(map.values().toString(),
                        new TypeToken<List<RemoteUser>>() {
                        }.getType())));
    }

    public void publishRemoteUsers(List<RemoteUser> remoteUsers) {
        remoteUsers.forEach(this::sendRemoteUser);
    }

    public void publishTask() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            plugin.getUserManager().getLoadedRemoteUsers().forEach(r -> {
                r.time(System.currentTimeMillis());
                sendRemoteUser(r);
            });
        }, 0, 20 * 15);
    }


}
