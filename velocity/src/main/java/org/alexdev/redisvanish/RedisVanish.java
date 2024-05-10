package org.alexdev.redisvanish;

import com.google.inject.Inject;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import io.lettuce.core.RedisClient;
import lombok.Getter;
import org.alexdev.redisvanish.commands.RedisVanishCommand;
import org.alexdev.redisvanish.config.ConfigManager;
import org.alexdev.redisvanish.data.UserManager;
import org.alexdev.redisvanish.hook.Hook;
import org.alexdev.redisvanish.hook.VelocitabHook;
import org.alexdev.redisvanish.listener.PlayerListener;
import org.alexdev.redisvanish.redis.RedisImpl;
import org.alexdev.redisvanish.vanish.VanishManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Plugin(id = "redisvanish", dependencies = {
        @Dependency(id = "velocitab", optional = true)
})
public class RedisVanish {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private UserManager userManager;
    private final Map<String, Hook> hooks;
    private final ConfigManager configManager;
    private final VanishManager vanishManager;
    private RedisImpl redis;
    private PlayerListener playerListener;

    @Inject
    public RedisVanish(@NotNull ProxyServer server, @NotNull Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.hooks = new ConcurrentHashMap<>();
        this.configManager = new ConfigManager(this);
        this.vanishManager = new VanishManager(this);
    }

    @Subscribe
    public void onProxyInitialization(@NotNull ProxyInitializeEvent event) {
        redis = new RedisImpl(RedisClient.create(configManager.getConfig().getRedisUri()), 10, this);
        this.userManager = new UserManager(this);
        playerListener = new PlayerListener(this);
        server.getEventManager().register(this, playerListener);
        loadHooks();
        registerCommands();
        logger.info("Successfully enabled RedisVanish");
    }

    private void loadHooks() {
        loadVelocitabHook();
    }

    private void loadVelocitabHook() {
        if (server.getPluginManager().isLoaded("velocitab")) {
            VelocitabHook velocitabHook = new VelocitabHook(this);
            velocitabHook.register();
            hooks.put("Velocitab", velocitabHook);
        }
    }

    private void registerCommands() {
        final BrigadierCommand command = new RedisVanishCommand(this).getCommand();
        server.getCommandManager().register(
                server.getCommandManager().metaBuilder(command).aliases("vanish").plugin(this).build(),
                command
        );
    }

    @Subscribe
    public void onProxyShutdown(@NotNull ProxyShutdownEvent event) {
//        hooks.values().forEach(Hook::unregister);
        redis.close();
        logger.info("Successfully disabled RedisVanish");
    }

    public <T> Optional<T> getHook(Class<T> clazz) {
        return hooks.values().stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .findFirst();
    }
}
