package org.alexdev.redisvanish.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import lombok.Getter;
import org.alexdev.redisvanish.RedisVanish;
import org.alexdev.redisvanish.data.User;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class PlayerListener {

    private final RedisVanish plugin;
    @Getter
    private final Map<UUID, String> server;
    @Getter
    private final Map<UUID, String> lastGroup;

    public PlayerListener(RedisVanish plugin) {
        this.plugin = plugin;
        this.server = new ConcurrentHashMap<>();
        this.lastGroup = new ConcurrentHashMap<>();
        this.populateMap();
    }

    private void populateMap() {
        plugin.getServer().getAllPlayers().forEach(player -> {
            Optional<ServerConnection> serverConnection = player.getCurrentServer();
            if (serverConnection.isEmpty()) {
                return;
            }
            server.put(player.getUniqueId(), serverConnection.get().getServerInfo().getName());
        });
    }

    public Optional<String> getServer(@NotNull UUID uuid) {
        return Optional.ofNullable(server.get(uuid));
    }

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

    @Subscribe(order = PostOrder.LATE)
    public void onPlayerQuit(DisconnectEvent e) {
        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            plugin.getUserManager().removeUser(plugin.getUserManager().getUser(e.getPlayer()));
            server.remove(e.getPlayer().getUniqueId());
        }).delay(100, TimeUnit.MILLISECONDS).schedule();
    }

    @Subscribe
    public void onConnect(ServerConnectedEvent e){
        server.put(e.getPlayer().getUniqueId(), e.getServer().getServerInfo().getName());

        String previousGroup = lastGroup.get(e.getPlayer().getUniqueId());
        String currentGroup = plugin.getVanishManager().getServer(e.getPlayer());
        lastGroup.put(e.getPlayer().getUniqueId(), currentGroup);

        if (previousGroup == null) {
            return;
        }

        if (previousGroup.equals(currentGroup)) {
            return;
        }

        plugin.getRedis().removeRemoteUser(e.getPlayer().getUniqueId(), previousGroup);
    }




}
