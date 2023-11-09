package org.alexdev.redisvanish.redis;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.lettuce.core.RedisClient;
import org.alexdev.redisvanish.RedisVanish;
import org.alexdev.redisvanish.data.User;
import org.alexdev.redisvanish.redis.data.RedisKeys;
import org.alexdev.redisvanish.redis.data.RedisPubSub;


public class RedisImpl extends RedisImplementation {

    private final RedisVanish plugin;
    private final Gson gson;

    public RedisImpl(RedisClient lettuceRedisClient, int size, RedisVanish plugin) {
        super(lettuceRedisClient, size);
        this.plugin = plugin;
        this.gson = new Gson();
        this.publishVanishLevels();
        this.subscribe();
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
                    User user = gson.fromJson(message, User.class);
                    plugin.getUserManager().replaceUser(user);
                }
            });
            c.async().subscribe(RedisKeys.USER_UPDATE.getKey());
        });
    }
}
