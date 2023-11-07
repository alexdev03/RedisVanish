package org.alexdev.redisvanish.listener;

import lombok.RequiredArgsConstructor;
import org.alexdev.redisvanish.RedisVanish;
import org.alexdev.redisvanish.data.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public class PlayerListener implements Listener {

    private final RedisVanish plugin;

    @EventHandler
    private void onJoin(PlayerLoginEvent e) {
        plugin.getRedis().loadUser(e.getPlayer().getUniqueId()).thenAccept(user -> {
            if (user == null) {
               plugin.getLogger().warning("User " + e.getPlayer().getName() + " has no data in Redis, this should not happen");
               return;
            }

            plugin.getUserManager().addUser(user);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onJoin(PlayerJoinEvent e) {
        User user = plugin.getUserManager().getUser(e.getPlayer());

        if (!plugin.getVanishManager().isVanished(user)) {
            return;
        }

        e.setJoinMessage(null);
        plugin.getVanishManager().hidePlayer(e.getPlayer());
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent e) {
        plugin.getUserManager().removeUser(e.getPlayer().getUniqueId());
    }

}
