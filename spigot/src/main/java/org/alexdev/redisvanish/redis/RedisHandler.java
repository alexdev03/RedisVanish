package org.alexdev.redisvanish.redis;

import com.google.gson.reflect.TypeToken;
import io.lettuce.core.RedisClient;
import lombok.Getter;
import org.alexdev.redisvanish.RedisVanish;
import org.alexdev.redisvanish.data.User;
import org.alexdev.redisvanish.data.VanishLevel;
import org.alexdev.redisvanish.redis.data.RedisKeys;
import org.alexdev.redisvanish.redis.data.RedisPubSub;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;

@SuppressWarnings("unchecked")
public class RedisHandler extends RedisImplementation {

    private final RedisVanish plugin;
    @Getter
    private final Map<Integer, VanishLevel> vanishLevels;

    public RedisHandler(RedisClient lettuceRedisClient, int size, RedisVanish plugin) {
        super(lettuceRedisClient, size);
        this.plugin = plugin;
        this.vanishLevels = new ConcurrentSkipListMap<>();
        this.subscribe();
        this.loadVanishLevels();
    }

    private void subscribe() {
        getPubSubConnection(c -> {
            c.addListener(new RedisPubSub<>() {
                @Override
                public void message(String channel, String message) {
                    try {
                        User user = gson.fromJson(message, User.class);
                        plugin.getUserManager().replaceUser(user);
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
        Type empMapType = new TypeToken<Map<Integer, VanishLevel>>() {}.getType();
        return gson.fromJson(json, empMapType);
    }



}
