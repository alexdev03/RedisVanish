package org.alexdev.redisvanish.hook.papi;

import org.alexdev.redisvanish.RedisVanish;
import org.alexdev.redisvanish.hook.Hook;

public class PlaceholderAPIHook extends Hook {

    private Placeholders placeholders;

    public PlaceholderAPIHook(RedisVanish plugin) {
        super(plugin);
    }

    @Override
    public void register() {
        this.placeholders = new Placeholders(plugin);
        this.placeholders.register();
    }

    @Override
    public void unregister() {
        this.placeholders.unregister();
    }
}
