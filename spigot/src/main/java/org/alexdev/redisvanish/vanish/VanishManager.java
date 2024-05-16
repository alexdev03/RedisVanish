package org.alexdev.redisvanish.vanish;

import net.md_5.bungee.api.ChatMessageType;
import org.alexdev.redisvanish.RedisVanish;
import org.alexdev.redisvanish.data.RemoteUser;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@SuppressWarnings("unused")
public class VanishManager {

    private final RedisVanish plugin;

    public VanishManager(RedisVanish plugin) {
        this.plugin = plugin;
        this.vanishActionbar();
    }

    public boolean isVanished(User user) {
        if (user == null) return false;
        return user.isVanished(plugin.getConfigManager().getConfig().getServerType());
    }

    public boolean hasProperty(User user, @NotNull VanishProperty property) {
        if (user == null) return false;
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
        if (!Bukkit.isPrimaryThread()) {
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

//            System.out.println("Checking if " + onlinePlayer.getName() + " is vanished : " + isVanished(onlinePlayer) + " and can see " + onlinePlayer.getName() + " : " + canSee(player, onlinePlayer));

            if (isVanished(onlinePlayer) && !canSee(player, onlinePlayer)) {
                if (true || player.getName().equals("AlexDev_")) {
//                    System.out.println("Hiding " + onlinePlayer.getName() + " from " + player.getName());
                }
                player.hidePlayer(plugin, onlinePlayer);
            }
        }
    }

    public boolean canSee(@NotNull Player player, @NotNull RemoteUser remoteUser) {
        if (player.hasPermission("redisvanish.bypass")) {
            return true;
        }

        if (!remoteUser.vanished()) {
            return true;
        }

        final Optional<VanishLevel> targetVanishLevel = getVanishLevel(remoteUser.vanishLevel());
        if (targetVanishLevel.isEmpty()) {
            plugin.getLogger().warning("Remote user " + remoteUser.name() + " has no vanish level, this should not happen (level: " + remoteUser.vanishLevel() + ")");
            return true;
        }

        return getVanishLevel(player).filter(level -> checkVanishLevels(level, targetVanishLevel.get())).isPresent();
    }

    public boolean canSee(@NotNull Player player, @NotNull String target) {
        final Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer != null) {
            return canSee(player, targetPlayer);
        }

        final Optional<RemoteUser> remoteUser = plugin.getUserManager().getRemoteUser(target);
        if (remoteUser.isEmpty()) {
            if (!plugin.getPlayerListener().isJustQuit(target)) {
                plugin.getLogger().warning("Remote user " + target + " has no data in Redis, this should not happen for target");
                return false;
            }
            return true;
        }

        return canSee(player, remoteUser.get());
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

        final Optional<VanishLevel> targetVanishLevel = getVanishLevel(target);

        if (targetVanishLevel.isEmpty()) {
            plugin.getLogger().warning("Target " + target.getName() + " has no vanish level, this should not happen");
            return true;
        }

        final Optional<VanishLevel> playerVanishLevel = getVanishLevel(player);

        //            System.out.println("Player " + player.getName() + " has no vanish level, can't see");
        return playerVanishLevel.filter(level -> checkVanishLevels(level, targetVanishLevel.get())).isPresent();

    }

    private boolean checkVanishLevels(VanishLevel player, VanishLevel target) {
        int targetOrder = getOrder(target);
        int playerOrder = getOrder(player);


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

    public Optional<VanishLevel> getRemoveVanishLevel(@NotNull String name) {
        Optional<RemoteUser> remoteUser = plugin.getUserManager().getRemoteUser(name);
        if (remoteUser.isEmpty()) {
            return Optional.empty();
        }
        return getVanishLevel(remoteUser.get().vanishLevel());
    }

    public Optional<VanishLevel> getRemoveVanishLevel(@NotNull RemoteUser remoteUser) {
        return getVanishLevel(remoteUser.vanishLevel());
    }

    public Optional<VanishLevel> getVanishLevel(int index) {
        return plugin.getRedis().getVanishLevels().entrySet().stream()
                .filter(entry -> entry.getKey() == index)
                .map(Map.Entry::getValue)
                .findFirst();
    }

    public int getOrder(@NotNull VanishLevel vanishLevel) {
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

        if (!hasProperty(user, VanishProperty.NIGHT_VISION)) {
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
                            plugin.getConfigManager().getMessages().getMessage("vanishedActionbar"));

                }
            }
        }, 0, 20);
    }

    public List<String> cleanStringList(Player player, List<String> list) {
        User user = plugin.getUserManager().getUser(player);

        List<String> newList = new ArrayList<>(list);

        Optional<VanishLevel> vanishLevel = getVanishLevel(player);

        newList.removeIf(s -> {
            Optional<RemoteUser> remoteUser = plugin.getUserManager().getRemoteUser(s);
            if (remoteUser.isEmpty()) return false;

            if (vanishLevel.isEmpty() && remoteUser.get().vanished()) return true;

            if (vanishLevel.isPresent() && remoteUser.get().vanished()) {
                Optional<VanishLevel> targetVanishLevel = getVanishLevel(remoteUser.get().vanishLevel());
                return targetVanishLevel.filter(level -> !checkVanishLevels(vanishLevel.get(), level)).isPresent();

            }

            return false;
        });

        return newList;
    }
}
