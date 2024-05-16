package org.alexdev.redisvanish.listener;

import com.google.common.collect.Sets;
import org.alexdev.redisvanish.RedisVanish;
import org.alexdev.redisvanish.data.User;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Set;

public class PlayerListener implements Listener {

    private final RedisVanish plugin;
    private final Set<String> justQuit;

    public PlayerListener(RedisVanish plugin) {
        this.plugin = plugin;
        this.justQuit = Sets.newConcurrentHashSet();
    }

    @EventHandler
    private void onJoin(AsyncPlayerPreLoginEvent e) {
        plugin.getRedis().loadUser(e.getUniqueId()).thenAccept(user -> {
            if (user == null) {
                plugin.getLogger().warning("User " + e.getName() + " has no data in Redis, this should not happen");
                return;
            }

            plugin.getUserManager().addUser(user);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onJoin(PlayerLoginEvent e) {
        final User user = plugin.getUserManager().getUser(e.getPlayer());
        plugin.getUserManager().prepareRemoteUser(user, e.getPlayer());

//        plugin.getVanishManager().updateVanished(e.getPlayer());

        if (!plugin.getVanishManager().isVanished(user)) {
            return;
        }

        plugin.getVanishManager().hidePlayer(e.getPlayer());
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        User user = plugin.getUserManager().getUser(e.getPlayer());

        plugin.getVanishManager().updateVanished(e.getPlayer());


        if (plugin.getVanishManager().isVanished(user)) {
            e.setJoinMessage(null);
        }

//        plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getUserManager().prepareRemoteUser(user), 1L);
    }

//    @EventHandler
//    private void onQuit(PlayerQuitEvent e) {
//        User user = plugin.getUserManager().getUser(e.getPlayer());
//
//        if (plugin.getVanishManager().isVanished(user)) {
//            e.setQuitMessage(null);
//        }
//        Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getUserManager().removeUser(e.getPlayer().getUniqueId()), 5L);
//    }

    @EventHandler
    private void onQuit(PlayerQuitEvent e) {
        justQuit.add(e.getPlayer().getName());

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> justQuit.remove(e.getPlayer().getName()), 4 * 20L);
    }

    public boolean isJustQuit(String name) {
        return justQuit.contains(name);
    }

}
