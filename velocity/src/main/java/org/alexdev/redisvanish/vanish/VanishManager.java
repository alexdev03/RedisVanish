package org.alexdev.redisvanish.vanish;

import com.google.common.collect.Maps;
import com.velocitypowered.api.proxy.Player;
import org.alexdev.redisvanish.RedisVanish;
import org.alexdev.redisvanish.data.User;
import org.alexdev.redisvanish.data.VanishLevel;
import org.alexdev.redisvanish.hook.VelocitabHook;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;


public class VanishManager {

    private final RedisVanish plugin;
    private final Map<Integer, VanishLevel> vanishLevels;

    public VanishManager(RedisVanish plugin) {
        this.plugin = plugin;
        this.vanishLevels = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
        this.loadVanishLevels();
    }

    public void loadVanishLevels() {
        vanishLevels.clear();
        vanishLevels.putAll(plugin.getConfigManager().getConfig().getVanishLevels());
    }

    public boolean canSee(@NotNull Player player, @NotNull Player target) {

        if (player.hasPermission("redisvanish.bypass")) {
//            System.out.println("Bypassing vanish check for " + player.getUsername() + " on " + target.getUsername());
            return true;
        }

        if (!isVanished(target)) {
            return true;
        }


        Optional<VanishLevel> targetVanishLevel = getVanishLevel(target);

        if (targetVanishLevel.isEmpty()) {
            plugin.getLogger().warn("Target " + target.getUsername() + " has no vanish level, this should not happen");
            return true;
        }

        Optional<VanishLevel> playerVanishLevel = getVanishLevel(player);

        if (playerVanishLevel.isEmpty()) {
//            System.out.println("Player " + player.getUsername() + " has no vanish level, can't see");
            return false;
        }

        int targetOrder = getOrder(targetVanishLevel.get());
        int playerOrder = getOrder(playerVanishLevel.get());

//        System.out.println("Checking if " + player.getUsername() + " can see " + target.getUsername() + ". Can see: " + (playerOrder >= targetOrder) + " | " + playerOrder + " >= " + targetOrder);

        return playerOrder >= targetOrder;
    }

    public boolean isVanished(@NotNull Player player) {
        final User targetUser = plugin.getUserManager().getUser(player);

        final String currentServer = getServer(player);

        if (currentServer.isEmpty()) return false;

        return targetUser.isVanished(currentServer);
    }

    @NotNull
    public String getServerGroup(@NotNull Player player, @NotNull String origin) {
        var hook = plugin.getHook(VelocitabHook.class);
        return hook
                .map(velocitabHook -> velocitabHook.getGroupName(origin))
                .orElse(origin);
    }

    @NotNull
    public Map<User, String> getUserServerMap(@NotNull Collection<User> users) {
        final Map<User, String> userServerMap = Maps.newHashMap();

        users.forEach(user -> {
            final Optional<Player> player = plugin.getServer().getPlayer(user.uuid());
            if (player.isEmpty()) {
                return;
            }
            String server = getServer(player.get());
            if(!server.isEmpty()) {
                userServerMap.put(user, server);
            }
        });

        return userServerMap;
    }

    @NotNull
    public String getServer(@NotNull Player player) {
        var hook = plugin.getHook(VelocitabHook.class);
        return hook
                .map(velocitabHook -> {
                    String server = velocitabHook.getCurrentGroup(player);
                    return server.isEmpty() ? plugin.getPlayerListener().getServer(player.getUniqueId()).orElse("") : server;
                })
                .orElseGet(() -> player.getCurrentServer().map(s -> s.getServer().getServerInfo().getName())
                        .orElse(""));
    }

    public Optional<VanishLevel> getVanishLevel(@NotNull Player player) {
        for (VanishLevel vanishLevel : vanishLevels.values()) {
            if (player.hasPermission(vanishLevel.permission())) {
                return Optional.of(vanishLevel);
            }
        }

        return Optional.empty();
    }

    private int getOrder(@NotNull VanishLevel vanishLevel) {
        return vanishLevels.entrySet().stream()
                .filter(entry -> entry.getValue().equals(vanishLevel))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(0);
    }


    public void vanish(@NotNull Player player) {
        User user = plugin.getUserManager().getUser(player);
        setVanish(user, true, player);
        plugin.getHook(VelocitabHook.class).ifPresent(hook -> hook.vanish(player));
    }

    public void unVanish(@NotNull Player player) {
        User user = plugin.getUserManager().getUser(player);
        setVanish(user, false, player);
        plugin.getHook(VelocitabHook.class).ifPresent(hook -> hook.unVanish(player));
    }

    private void setVanish(User user, boolean vanished, Player player) {
        String currentServer = getServer(player);
        user.setVanished(currentServer, vanished);
        plugin.getRedis().saveUser(user);
        plugin.getRedis().sendUserUpdate(user);
    }


}
