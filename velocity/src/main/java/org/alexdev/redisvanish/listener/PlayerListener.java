package org.alexdev.redisvanish.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import lombok.RequiredArgsConstructor;
import org.alexdev.redisvanish.RedisVanish;
import org.alexdev.redisvanish.data.User;

@RequiredArgsConstructor
public class PlayerListener {

    private final RedisVanish plugin;


    @Subscribe
    public void onPlayerJoin(PostLoginEvent e) {
        plugin.getRedis().loadUser(e.getPlayer().getUniqueId()).thenAccept(user -> {
            if (user == null) {
                user = new User(e.getPlayer().getUniqueId(), e.getPlayer().getUsername());
                plugin.getRedis().saveUser(user);
            }

            plugin.getUserManager().addUser(user);
        });
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerQuit(DisconnectEvent e) {
        plugin.getUserManager().removeUser(plugin.getUserManager().getUser(e.getPlayer()));
    }


}
