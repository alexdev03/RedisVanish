package org.alexdev.redisvanish.hook.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.alexdev.redisvanish.RedisVanish;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Placeholders extends PlaceholderExpansion {

    private final RedisVanish plugin;

    public Placeholders(RedisVanish plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "redisvanish";
    }

    @Override
    public @NotNull String getAuthor() {
        return "AlexDev_";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";
        if (params.equalsIgnoreCase("vanished")) {
            return plugin.getVanishManager().isVanished(player) ? "True" : "False";
        } else if (params.equalsIgnoreCase("vanished_tab")) {
            return plugin.getVanishManager().isVanished(player) ? plugin.getConfigManager().getMessages().getColoredMessage("vanishTabPlaceholder") : "";
        }

        return null;
    }

}
