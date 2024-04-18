package org.alexdev.redisvanish.redis;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.lettuce.core.RedisClient;
import org.alexdev.redisvanish.RedisVanish;
import org.alexdev.redisvanish.data.User;
import org.alexdev.redisvanish.redis.data.RedisKeys;
import org.alexdev.redisvanish.redis.data.RedisPubSub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;


public class RedisImpl extends RedisImplementation {

    private final RedisVanish plugin;
    private final Gson gson;

    public RedisImpl(RedisClient lettuceRedisClient, int size, RedisVanish plugin) {
        super(lettuceRedisClient, size);
        this.plugin = plugin;
        this.gson = new Gson();
        this.publishVanishLevels();
        this.subscribe();
//        this.cleanTask();
    }

    public void publishVanishLevels() {
        this.getConnectionAsync(connection -> {
            JsonObject object = gson.toJsonTree(plugin.getConfigManager().getConfig().getVanishLevels()).getAsJsonObject();
            connection.set(RedisKeys.VANISH_LEVELS.getKey(), object.toString());
            return connection.publish(RedisKeys.VANISH_LEVELS_UPDATE.getKey(), object.toString());
        });
    }

    private void subscribe() {
        getPubSubConnection(c -> {
            c.addListener(new RedisPubSub<>() {
                @Override
                public void message(String channel, String message) {

                }
            });
            c.async().subscribe(RedisKeys.VANISH_LEVELS_REQUEST.getKey());
        });
        getPubSubConnection(c -> {
            c.addListener(new RedisPubSub<>() {
                @Override
                public void message(String channel, String message) {
                    final User user = gson.fromJson(message, User.class);
                    plugin.getUserManager().replaceUser(user);
                }
            });
            c.async().subscribe(RedisKeys.USER_UPDATE.getKey());
        });
        //reply to backends requesting user cache
        getPubSubConnection(c -> {
            c.addListener(new RedisPubSub<>() {
                @Override
                public void message(String channel, String message) {
                    plugin.getUserManager().sendUsersToServer(message);
                }
            });
            c.async().subscribe(RedisKeys.USER_CACHE_REQUEST.getKey());
        });
    }

    public void removeRemoteUser(@NotNull UUID uuid, @NotNull String server) {
        JsonObject object = new JsonObject();
        object.addProperty("type", 2);
        object.addProperty("uuid", uuid.toString());
        object.addProperty("group", server);
        this.getConnectionAsync(connection -> {
            connection.hdel(RedisKeys.REMOTE_USER.getKey(), uuid.toString());
            return connection.publish(RedisKeys.REMOTE_USER_UPDATE.getKey(), object.toString());
        });
    }

    public void sendLoadedUsers(@NotNull Map<User, String> users, @Nullable String target) {
        final JsonObject object = new JsonObject();
        object.addProperty("server", target == null ? "" : target);
        final JsonArray array = new JsonArray();
        users.forEach((user, server) -> {
            final JsonObject userObject = new JsonObject();
            userObject.addProperty("server", server);
            userObject.add("user", gson.toJsonTree(user));
            array.add(userObject);
        });
        object.add("users", array);
        this.getConnectionAsync(connection -> connection.publish(RedisKeys.USER_SET_CACHE.getKey(), object.toString()));
    }

    public void sendVanishLevels() {
        this.getConnectionAsync(connection -> connection.get(RedisKeys.VANISH_LEVELS.getKey()).thenAccept(levels -> {
            if (levels == null) {
                return;
            }
            JsonObject object = gson.fromJson(levels, JsonObject.class);
            object.addProperty("server", "");
            connection.publish(RedisKeys.VANISH_LEVELS_UPDATE.getKey(), object.toString());
        }));
    }

    public void sendUserJoin(@NotNull User user, @NotNull String server) {
        final JsonObject object = new JsonObject();
        object.addProperty("server", server);
        object.add("user", gson.toJsonTree(user));
        this.getConnectionAsync(connection -> connection.publish(RedisKeys.USER_JOIN.getKey(), object.toString()));
    }

    /**
     * Sends a user leave event to the Redis server.
     *
     * @param user   the user who left
     * @param server the server the user left from (nullable if disconnected from the network)
     */
    public void sendUserLeave(@NotNull User user, @Nullable String server) {
        final JsonObject object = new JsonObject();
        object.addProperty("server", server == null ? "" : server);
        object.addProperty("uuid", user.uuid().toString());
        this.getConnectionAsync(connection -> connection.publish(RedisKeys.USER_LEAVE.getKey(), object.toString()));
    }

    public void cleanTask() {
        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            this.getConnectionAsync(connection -> connection.hgetall(RedisKeys.REMOTE_USER.getKey()).thenAccept(map -> {
                final List<UUID> toRemove = new ArrayList<>();
                map.forEach((key, value) -> {
                    JsonObject object = gson.fromJson(value, JsonObject.class);
                    long time = object.get("time").getAsLong();
                    if (System.currentTimeMillis() - time > TimeUnit.SECONDS.toMillis(30)) {
                        toRemove.add(UUID.fromString(key));
                    }
                });

                if (toRemove.isEmpty()) return;

                getConnectionAsync(connection2 -> {
                    toRemove.forEach(uuid -> {
                        connection2.hdel(RedisKeys.REMOTE_USER.getKey(), uuid.toString());
                    });
                    JsonObject object = new JsonObject();
                    object.addProperty("type", 3);
                    object.add("users", gson.toJsonTree(toRemove, new com.google.gson.reflect.TypeToken<List<UUID>>() {
                    }.getType()));
                    return connection2.publish(RedisKeys.REMOTE_USER_UPDATE.getKey(), object.toString());
                });

            }));
        }).repeat(30, TimeUnit.SECONDS).schedule();
    }
}
