package org.alexdev.redisvanish.redis;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.lettuce.core.RedisClient;
import org.alexdev.redisvanish.RedisVanish;
import org.alexdev.redisvanish.data.User;
import org.alexdev.redisvanish.redis.data.RedisKeys;
import org.jetbrains.annotations.NotNull;


public class RedisImpl extends RedisImplementation {

    private final RedisVanish plugin;
    private final Gson gson;

    public RedisImpl(RedisClient lettuceRedisClient, int size, RedisVanish plugin) {
        super(lettuceRedisClient, size);
        this.plugin = plugin;
        this.gson = new Gson();
        this.publishVanishLevels();
    }

    public void saveUser(@NotNull User user) {
        this.getConnectionAsync(connection -> connection.hset(RedisKeys.USER.getKey(), user.uuid().toString(), gson.toJson(user)));
    }

    public void sendUserUpdate(@NotNull User user) {
        this.getConnectionAsync(connection -> connection.publish(RedisKeys.USER_UPDATE.getKey(), gson.toJson(user)));
    }

    public void publishVanishLevels() {
        this.getConnectionAsync(connection -> {
            JsonObject object = gson.toJsonTree(plugin.getConfigManager().getConfig().getVanishLevels()).getAsJsonObject();
            connection.set(RedisKeys.VANISH_LEVELS.getKey(), object.toString());
            return connection.publish(RedisKeys.VANISH_LEVELS_UPDATE.getKey(), object.toString());
        });
    }


}
