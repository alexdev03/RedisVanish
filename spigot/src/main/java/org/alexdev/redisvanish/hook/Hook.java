package org.alexdev.redisvanish.hook;

import lombok.RequiredArgsConstructor;
import org.alexdev.redisvanish.RedisVanish;

@RequiredArgsConstructor
public abstract class Hook {

    protected final RedisVanish plugin;

    public abstract void register();

    public abstract void unregister();

}
