package org.alexdev.redisvanish;

import io.lettuce.core.RedisClient;
import lombok.Getter;
import org.alexdev.redisvanish.config.ConfigManager;
import org.alexdev.redisvanish.data.UserManager;
import org.alexdev.redisvanish.hook.Hook;
import org.alexdev.redisvanish.hook.UnlimitedNameTagsHook;
import org.alexdev.redisvanish.listener.PlayerListener;
import org.alexdev.redisvanish.redis.RedisHandler;
import org.alexdev.redisvanish.vanish.VanishManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public final class RedisVanish extends JavaPlugin {

    private ConfigManager configManager;
    private RedisHandler redis;
    private UserManager userManager;
    private VanishManager vanishManager;
    private List<Hook> hooks;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        redis = new RedisHandler(RedisClient.create(configManager.getConfig().getRedisUri()), 10, this);
        userManager = new UserManager(this);
        vanishManager = new VanishManager(this);
        loadHooks();
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        getLogger().info("RedisVanish has been enabled!");
    }

    private void loadHooks() {
        hooks = new CopyOnWriteArrayList<>();

        if (Bukkit.getPluginManager().isPluginEnabled("UnlimitedNameTags")) {
            UnlimitedNameTagsHook hook = new UnlimitedNameTagsHook(this);
            hook.register();
            hooks.add(hook);
        }
    }

    public  <H extends Hook> Optional<H> getHook(@NotNull Class<H> hookType) {
        return hooks.stream()
                .filter(hook -> hook.getClass().equals(hookType))
                .map(hookType::cast)
                .findFirst();
    }

    @Override
    public void onDisable() {
        redis.close();
        getLogger().info("RedisVanish has been disabled!");
    }
}
