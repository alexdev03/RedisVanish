package org.alexdev.redisvanish.redis;

import com.google.gson.Gson;
import io.lettuce.core.RedisClient;
import org.alexdev.redisvanish.data.User;
import org.alexdev.redisvanish.redis.data.RedisAbstract;
import org.alexdev.redisvanish.redis.data.RedisKeys;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

public class RedisImplementation extends RedisAbstract {

    protected final Gson gson;

    public RedisImplementation(RedisClient lettuceRedisClient, int size) {
        super(lettuceRedisClient, size);
        this.gson = new Gson();
    }

    public CompletionStage<@Nullable User> loadUser(@NotNull UUID uuid) {
        return this.getConnectionAsync(connection -> connection.hget(RedisKeys.USER.getKey(), uuid.toString()))
                .thenApply(result -> gson.fromJson(result, User.class));
    }

}
