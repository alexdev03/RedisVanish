package org.alexdev.redisvanish.vanish;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.alexdev.redisvanish.RedisVanish;
import org.alexdev.redisvanish.data.User;
import org.alexdev.redisvanish.data.VanishLevel;
import org.alexdev.redisvanish.data.VanishProperty;
import org.alexdev.redisvanish.hook.UnlimitedNameTagsHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;


@SuppressWarnings("unused")
public class VanishManager {

    private final RedisVanish plugin;

    public VanishManager(RedisVanish plugin) {
        this.plugin = plugin;
        this.vanishActionbar();
    }

    public boolean isVanished(@NotNull User user) {
        return user.isVanished(plugin.getConfigManager().getConfig().getServerType());
    }

    public boolean hasProperty(@NotNull User user, @NotNull VanishProperty property) {
        return user.hasProperty(property, plugin.getConfigManager().getConfig().getServerType());
    }

    public void setProperty(@NotNull User user, @NotNull VanishProperty property, boolean value) {
        user.setProperty(property, plugin.getConfigManager().getConfig().getServerType(), value);
    }

    public boolean isVanished(@NotNull Player player) {
        return isVanished(plugin.getUserManager().getUser(player));
    }

    public boolean hasProperty(@NotNull Player player, @NotNull VanishProperty property) {
        return hasProperty(plugin.getUserManager().getUser(player), property);
    }

    public void setProperty(@NotNull Player player, @NotNull VanishProperty property, boolean value) {
        setProperty(plugin.getUserManager().getUser(player), property, value);
    }


    public void hidePlayer(@NotNull Player player) {
        if(!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> hidePlayer(player));
            return;
        }

        if (!isVanished(player)) {
            return;
        }

        player.setMetadata("vanished", new FixedMetadataValue(plugin, true));

        Bukkit.getScheduler().runTaskLater(plugin, () -> applyEffects(plugin.getUserManager().getUser(player)), 5L);

        plugin.getHook(UnlimitedNameTagsHook.class).ifPresent(hook -> hook.hidePlayer(player));

        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
            if (onlinePlayer.equals(player)) {
                return;
            }

            if (!canSee(onlinePlayer, player)) {
                onlinePlayer.hidePlayer(plugin, player);
            }
        });
    }

    public void showPlayer(@NotNull Player player) {
        player.removeMetadata("vanished", plugin);

        Bukkit.getScheduler().runTaskLater(plugin, () -> applyEffects(plugin.getUserManager().getUser(player)), 5L);

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

    public void updateVanished(Player player) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.equals(player)) {
                continue;
            }

            if (isVanished(onlinePlayer) && !canSee(player, onlinePlayer)) {
                player.hidePlayer(plugin, onlinePlayer);
            }
        }
    }

    public boolean canSee(@NotNull Player player, @NotNull Player target) {
        if (player.hasPermission("redisvanish.bypass")) {
//            System.out.println("Bypassing vanish check for " + player.getUsername() + " on " + target.getUsername());
            return true;
        }

        if (!isVanished(target)) {
//            System.out.println("Target " + target.getName() + " is not vanished, can see");
            return true;
        }

        Optional<VanishLevel> targetVanishLevel = getVanishLevel(target);

        if (targetVanishLevel.isEmpty()) {
            plugin.getLogger().warning("Target " + target.getName() + " has no vanish level, this should not happen");
            return true;
        }

        Optional<VanishLevel> playerVanishLevel = getVanishLevel(player);

        if (playerVanishLevel.isEmpty()) {
//            System.out.println("Player " + player.getName() + " has no vanish level, can't see");
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

    public void toggleProperty(User user, VanishProperty vanishProperty) {
        setProperty(user, vanishProperty, !hasProperty(user, vanishProperty));
    }

    public void applyEffects(User user) {
        Player player = Bukkit.getPlayer(user.uuid());

        if (player == null) {
            return;
        }

        if(!hasProperty(user, VanishProperty.NIGHT_VISION)) {
            return;
        }

        if (isVanished(user)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
        } else {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
    }

    private void vanishActionbar() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (isVanished(player) && hasProperty(player, VanishProperty.ACTION_BAR)) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            new TextComponent("§7§oYou are currently vanished"));

                }
            }
        }, 0, 20);
    }
}
