package org.alexdev.redisvanish.vanish;

import lombok.RequiredArgsConstructor;
import org.alexdev.redisvanish.RedisVanish;
import org.alexdev.redisvanish.data.User;
import org.alexdev.redisvanish.data.VanishLevel;
import org.alexdev.redisvanish.hook.UnlimitedNameTagsHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class VanishManager {

    private final RedisVanish plugin;

    public boolean isVanished(User user) {
        return user.isVanished(plugin.getConfigManager().getConfig().getServerType());
    }

    public boolean isVanished(Player player) {
        return isVanished(plugin.getUserManager().getUser(player));
    }

    public void hidePlayer(Player player) {
        if (!isVanished(player)) {
            return;
        }

        plugin.getHook(UnlimitedNameTagsHook.class).ifPresent(hook -> hook.hidePlayer(player));

        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
            if (onlinePlayer.equals(player)) {
                return;
            }

            if (!canSee(onlinePlayer, player)) {
                onlinePlayer.hidePlayer(plugin, player);
            }
        }));
    }

    public void showPlayer(Player player) {
        plugin.getHook(UnlimitedNameTagsHook.class).ifPresent(hook -> hook.showPlayer(player));

        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
            if (onlinePlayer.equals(player)) {
                return;
            }

            if (canSee(onlinePlayer, player)) {
                onlinePlayer.showPlayer(plugin, player);
            }
        }));
    }

    public boolean canSee(@NotNull Player player, @NotNull Player target) {
        if (player.hasPermission("redisvanish.bypass")) {
//            System.out.println("Bypassing vanish check for " + player.getUsername() + " on " + target.getUsername());
            return true;
        }

        if (!isVanished(target)) {
//            System.out.println("Target " + target.getUsername() + " is not vanished, can see");
            return true;
        }

        Optional<VanishLevel> targetVanishLevel = getVanishLevel(target);

        if (targetVanishLevel.isEmpty()) {
            plugin.getLogger().warning("Target " + target.getName() + " has no vanish level, this should not happen");
            return true;
        }

        Optional<VanishLevel> playerVanishLevel = getVanishLevel(player);

        if (playerVanishLevel.isEmpty()) {
//            System.out.println("Player " + player.getUsername() + " has no vanish level, can't see");
            return false;
        }

        int targetOrder = getOrder(targetVanishLevel.get());
        int playerOrder = getOrder(playerVanishLevel.get());

//        System.out.println("Checking if " + player.getUsername() + " can see " + target.getUsername() + ". Can see: " + (playerOrder >= targetOrder));

        return playerOrder >= targetOrder;
    }

    public Optional<VanishLevel> getVanishLevel(@NotNull Player player) {
        for (VanishLevel vanishLevel : plugin.getRedis().getVanishLevels().values()) {
            if (player.hasPermission(vanishLevel.permission())) {
                return Optional.of(vanishLevel);
            }
        }

        return Optional.empty();
    }

    private int getOrder(@NotNull VanishLevel vanishLevel) {
        return plugin.getRedis().getVanishLevels().entrySet().stream()
                .filter(entry -> entry.getValue().equals(vanishLevel))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(0);
    }
}
