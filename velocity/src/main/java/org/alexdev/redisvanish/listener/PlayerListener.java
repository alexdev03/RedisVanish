package org.alexdev.redisvanish.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
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
//            plugin.getRedis().sendUserJoin(user);
        });
    }

    @Subscribe(order = PostOrder.LATE)
    public void onPlayerQuit(DisconnectEvent e) {
        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            final User user = plugin.getUserManager().getUser(e.getPlayer());
            plugin.getUserManager().removeUser(user);
            plugin.getRedis().sendUserLeave(user, null);
            server.remove(e.getPlayer().getUniqueId());
            lastGroup.remove(e.getPlayer().getUniqueId());
        }).delay(100, TimeUnit.MILLISECONDS).schedule();
    }

    @Subscribe(order = PostOrder.LAST)
    public void onConnect(ServerPreConnectEvent e) {
//        final String serverName = e.getServer().getServerInfo().getName();
        final Optional<RegisteredServer> serverInfo = e.getResult().getServer();
        if (serverInfo.isEmpty()) {
            return;
        }
        final String serverName = serverInfo.get().getServerInfo().getName();
        server.put(e.getPlayer().getUniqueId(), serverName);

        String previousGroup = lastGroup.get(e.getPlayer().getUniqueId());
        String currentGroup = plugin.getVanishManager().getServerGroup(e.getPlayer(), serverName);
        lastGroup.put(e.getPlayer().getUniqueId(), currentGroup);

        final User user = plugin.getUserManager().getUser(e.getPlayer());

//        if (user == null) {
//            plugin.getLogger().error("User is null for " + e.getPlayer().getUsername());
//            return;
//        }

        if (previousGroup == null) {
            plugin.getRedis().sendUserJoin(user, currentGroup);
            return;
        }

        if (previousGroup.equals(currentGroup)) {
            return;
        }

//        plugin.getRedis().removeRemoteUser(e.getPlayer().getUniqueId(), previousGroup);
        plugin.getRedis().sendUserLeave(user, previousGroup);
        plugin.getRedis().sendUserJoin(user, currentGroup);
    }


}
