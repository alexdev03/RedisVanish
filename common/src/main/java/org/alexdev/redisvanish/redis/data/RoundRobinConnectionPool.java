package org.alexdev.redisvanish.redis.data;

import io.lettuce.core.api.StatefulRedisConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class RoundRobinConnectionPool<K,V> {
    private final AtomicInteger next = new AtomicInteger(0);
    private final List<StatefulRedisConnection<K,V>> elements;
    private final Supplier<StatefulRedisConnection<K,V>> statefulRedisConnectionSupplier;

    public RoundRobinConnectionPool(Supplier<StatefulRedisConnection<K,V>> statefulRedisConnectionSupplier, int poolSize) {
        this.statefulRedisConnectionSupplier = statefulRedisConnectionSupplier;
        this.elements = new ArrayList<>(poolSize);
        for(int i = 0; i < poolSize; i++) {
            elements.add(statefulRedisConnectionSupplier.get());
        }
    }

    public void expandPool(int expandBy) {
        if(expandBy <= 0)
            throw new IllegalArgumentException("expandBy must be greater than 0");
        for(int i = 0; i < expandBy; i++) {
            elements.add(statefulRedisConnectionSupplier.get());
        }
    }

    public StatefulRedisConnection<K, V> get() {
        int index = next.getAndIncrement() % elements.size();
        StatefulRedisConnection<K, V> connection = elements.get(index);
        if (connection != null)
            if (connection.isOpen())
                return connection;
        connection = statefulRedisConnectionSupplier.get();
        elements.set(index, connection);
        return connection;
    }
}
