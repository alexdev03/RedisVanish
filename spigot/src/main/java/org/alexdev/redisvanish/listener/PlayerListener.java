package org.alexdev.redisvanish.listener;

import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
public class PlayerListener implements Listener {

    private final RedisVanish plugin;

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

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onJoin(PlayerLoginEvent e) {
        User user = plugin.getUserManager().getUser(e.getPlayer());

        plugin.getVanishManager().updateVanished(e.getPlayer());

        if (!plugin.getVanishManager().isVanished(user)) {
            return;
        }

        plugin.getVanishManager().hidePlayer(e.getPlayer());
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        User user = plugin.getUserManager().getUser(e.getPlayer());

        if (plugin.getVanishManager().isVanished(user)) {
            e.setJoinMessage(null);
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getUserManager().prepareRemoteUser(user), 1L);
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent e) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getUserManager().removeUser(e.getPlayer().getUniqueId()), 5L);
        plugin.getRedis().removeRemoteUser(e.getPlayer().getUniqueId());
    }

}
