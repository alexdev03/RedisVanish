package org.alexdev.redisvanish.hook;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import net.william278.velocitab.api.VelocitabAPI;
import net.william278.velocitab.vanish.VanishIntegration;
import org.alexdev.redisvanish.RedisVanish;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Getter
public class VelocitabHook extends Hook {

    private VelocitabAPI velocitabAPI;
    private final List<Player> justQuit;

    public VelocitabHook(RedisVanish plugin) {
        super(plugin);
        this.justQuit = new CopyOnWriteArrayList<>();
        plugin.getServer().getEventManager().register(plugin, this);
    }

    public void register() {
        this.velocitabAPI = VelocitabAPI.getInstance();
        this.velocitabAPI.setVanishIntegration(new VanishIntegration() {
            @Override
            public boolean canSee(String s, String s1) {
                Optional<Player> player = getPlayer(s);
                Optional<Player> target = getPlayer(s1);

                if (player.isPresent() && target.isPresent()) {
//                    System.out.println("Checking if " + player.get().getUsername() + " can see " + target.get().getUsername() + ". Can see: " + plugin.getVanishManager().canSee(player.get(), target.get()));
                    return plugin.getVanishManager().canSee(player.get(), target.get());
                }

//                System.out.println(player.isPresent() + " " + target.isPresent());

                return true;
            }

            @Override
            public boolean isVanished(String s) {
                Optional<Player> player = plugin.getServer().getPlayer(s);
                return player.filter(value -> plugin.getVanishManager().isVanished(value)).isPresent();
            }
        });

        plugin.getServer().getEventManager().register(plugin, this);

        plugin.getLogger().info("Hooked into Velocitab");
    }

    private Optional<Player> getPlayer(String name) {
        Optional<Player> player = plugin.getServer().getPlayer(name);
        if (player.isEmpty()) {
            return justQuit.stream().filter(value -> value.getUsername().equalsIgnoreCase(name)).findFirst();
        } else {
            return player;
        }
    }

    @Override
    public void unregister() {

    }

    public void vanish(@NotNull Player player) {
        plugin.getServer().getScheduler().buildTask(plugin, () -> this.velocitabAPI.vanishPlayer(player)).delay(500, TimeUnit.MILLISECONDS).schedule();
    }

    public void unVanish(@NotNull Player player) {
        plugin.getServer().getScheduler().buildTask(plugin, () -> this.velocitabAPI.unVanishPlayer(player)).delay(500, TimeUnit.MILLISECONDS).schedule();
    }

    @NotNull
    public String getCurrentGroup(@NotNull Player player) {
        return velocitabAPI.getServerGroup(player).name();
    }

    @Subscribe
    public void onPlayerQuit(DisconnectEvent event) {
        justQuit.add(event.getPlayer());
        plugin.getServer().getScheduler().buildTask(plugin, () -> justQuit.remove(event.getPlayer())).delay(250, TimeUnit.MILLISECONDS).schedule();
    }

}
