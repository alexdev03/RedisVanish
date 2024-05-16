package org.alexdev.redisvanish.hook;

import dev.unnm3d.redischat.api.RedisChatAPI;
import dev.unnm3d.redischat.api.VanishIntegration;
import org.alexdev.libraries.annotations.NotNull;
import org.alexdev.redisvanish.RedisVanish;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RedisChatHook implements Hook {

    private final RedisVanish plugin;
    private RedisChatAPI api;
    private VanishIntegration vanishIntegration;

    public RedisChatHook(RedisVanish plugin) {
        this.plugin = plugin;
    }

    @Override
    public void register() {
        api = RedisChatAPI.getAPI();

        if (api == null) {
            plugin.getLogger().warning("RedisChat found, but API is null");
            return;
        }

        vanishIntegration = new VanishIntegration() {
            @Override
            public boolean canSee(@NotNull CommandSender sender, @NotNull String target) {
                if (!(sender instanceof Player player)) return true;
                return plugin.getVanishManager().canSee(player, target);
            }

            @Override
            public boolean isVanished(Player player) {
                return plugin.getVanishManager().isVanished(player);
            }
        };

        api.addVanishIntegration(vanishIntegration);

        plugin.getLogger().info("Hooked into RedisChat");
    }

    @Override
    public void unregister() {
        api.removeVanishIntegration(vanishIntegration);
    }
}
