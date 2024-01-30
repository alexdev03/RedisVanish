package org.alexdev.redisvanish.listener;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.FakeChannelUtil;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPlayer;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.github.retrooper.packetevents.injector.SpigotChannelInjector;
import io.github.retrooper.packetevents.util.FoliaCompatUtil;
import lombok.RequiredArgsConstructor;
import org.alexdev.redisvanish.RedisVanish;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;


@RequiredArgsConstructor
public class PacketEventsListener extends PacketListenerAbstract {

    private final RedisVanish plugin;

    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(plugin));
        //Are all listeners read only?
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false)
                .checkForUpdates(true)
                .bStats(true);
        PacketEvents.getAPI().load();
    }

    public void onEnable() {
        PacketEvents.getAPI().getEventManager().registerListener(this);
        PacketEvents.getAPI().init();
        inject();
    }

    private void inject() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            SpigotChannelInjector injector = (SpigotChannelInjector) PacketEvents.getAPI().getInjector();

            User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
            if (user == null) {
                //We did not inject this user
                Object channel = PacketEvents.getAPI().getPlayerManager().getChannel(player);
                //Check if it is a fake connection...
                if (!FakeChannelUtil.isFakeChannel(channel)) {
                    //Kick them, if they are not a fake player.
                    FoliaCompatUtil.runTaskForEntity(player, plugin, () -> player.kickPlayer("PacketEvents 2.0 failed to inject"), null, 0);
                }
                return;
            }

            // Set bukkit player object in the injectors
            injector.updatePlayer(user, player);
        });
    }

    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.SPAWN_PLAYER) {
            handlePlayerSpawn(event);
        }
    }

    private void handlePlayerSpawn(@NotNull PacketSendEvent event) {
        final WrapperPlayServerSpawnPlayer packet = new WrapperPlayServerSpawnPlayer(event);

        final Optional<Player> target = Optional.ofNullable(Bukkit.getPlayer(packet.getUUID()));

        if (target.isEmpty()) {
            return;
        }

        final Player player = Bukkit.getPlayer(event.getUser().getUUID());

        if (player == null) {
            return;
        }

        if (plugin.getVanishManager().isVanished(target.get()) && !plugin.getVanishManager().canSee(player, target.get())) {
            event.setCancelled(true);
        }



    }
}
