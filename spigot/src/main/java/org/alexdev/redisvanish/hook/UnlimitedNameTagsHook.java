package org.alexdev.redisvanish.hook;

import org.alexdev.libraries.annotations.NotNull;
import org.alexdev.redisvanish.RedisVanish;
import org.alexdev.unlimitednametags.api.UNTAPI;
import org.alexdev.unlimitednametags.vanish.VanishIntegration;
import org.bukkit.entity.Player;

public class UnlimitedNameTagsHook implements Hook {

    private final RedisVanish plugin;
    private UNTAPI api;

    public UnlimitedNameTagsHook(RedisVanish plugin) {
        this.plugin = plugin;
    }

    @Override
    public void register() {
        api = UNTAPI.getInstance();

        api.setVanishIntegration(new VanishIntegration() {
            @Override
            public boolean canSee(@NotNull Player player, @NotNull Player player1) {
                return plugin.getVanishManager().canSee(player, player1);
            }

            @Override
            public boolean isVanished(@NotNull Player player) {
                return plugin.getVanishManager().isVanished(player);
            }
        });

        plugin.getLogger().info("Hooked into UnlimitedNameTags");
    }

    @Override
    public void unregister() {

    }

    public void hidePlayer(Player player) {
        api.vanishPlayer(player);
    }

    public void showPlayer(Player player) {
        api.unVanishPlayer(player);
    }
}
