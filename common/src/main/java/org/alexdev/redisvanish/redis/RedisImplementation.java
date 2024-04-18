package org.alexdev.redisvanish.redis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.lettuce.core.RedisClient;
import org.alexdev.redisvanish.data.User;
import org.alexdev.redisvanish.redis.data.RedisAbstract;
import org.alexdev.redisvanish.redis.data.RedisKeys;
import org.alexdev.redisvanish.serializer.UserSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public class RedisImplementation extends RedisAbstract {

    protected final Gson gson;

    public RedisImplementation(RedisClient lettuceRedisClient, int size) {
        super(lettuceRedisClient, size);
        this.gson = createGson();
    }

    private Gson createGson() {
        Type userType = new TypeToken<User>() {}.getType();
        return new GsonBuilder()
                .registerTypeAdapter(userType, new UserSerializer())
                .create();
    }

    public CompletionStage<@Nullable User> loadUser(@NotNull UUID uuid) {
        return this.getConnectionAsync(connection -> connection.hget(RedisKeys.USER.getKey(), uuid.toString()))
                .thenApply(result -> gson.fromJson(result, User.class));
    }

    public void saveUser(@NotNull User user) {
        this.getConnectionAsync(connection -> connection.hset(RedisKeys.USER.getKey(), user.uuid().toString(), gson.toJson(user)));
    }

    public void sendUserUpdate(@NotNull User user) {
        this.getConnectionAsync(connection -> connection.publish(RedisKeys.USER_UPDATE.getKey(), gson.toJson(user)));
    }



}
