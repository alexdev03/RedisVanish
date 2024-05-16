package org.alexdev.redisvanish.hook.papi;

import org.alexdev.redisvanish.RedisVanish;
import org.alexdev.redisvanish.hook.Hook;

public class PlaceholderAPIHook implements Hook {

    private final RedisVanish plugin;
    private Placeholders placeholders;

    public PlaceholderAPIHook(RedisVanish plugin) {
        this.plugin = plugin;
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
