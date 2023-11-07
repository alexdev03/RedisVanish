package org.alexdev.redisvanish.hook;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import net.william278.velocitab.Velocitab;
import net.william278.velocitab.api.VelocitabAPI;
import net.william278.velocitab.player.TabPlayer;
import net.william278.velocitab.vanish.VanishIntegration;
import org.alexdev.redisvanish.RedisVanish;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Getter
public class VelocitabHook extends Hook {

    private VelocitabAPI velocitabAPI;

    public VelocitabHook(RedisVanish plugin) {
        super(plugin);
    }

    public void register() {
        this.velocitabAPI = VelocitabAPI.getInstance();
        this.velocitabAPI.setVanishIntegration(new VanishIntegration() {
            @Override
            public boolean canSee(String s, String s1) {
                Optional<Player> player = plugin.getServer().getPlayer(s);
                Optional<Player> target = plugin.getServer().getPlayer(s1);

                if (player.isPresent() && target.isPresent()) {
//                    System.out.println("Checking if " + player.get().getUsername() + " can see " + target.get().getUsername()
//                            + ". Can see: " + plugin.getVanishManager().canSee(player.get(), target.get()));
                    return plugin.getVanishManager().canSee(player.get(), target.get());
                }

                return false;
            }

            @Override
            public boolean isVanished(String s) {
                Optional<Player> player = plugin.getServer().getPlayer(s);
                return player.filter(value -> plugin.getVanishManager().isVanished(value)).isPresent();
            }
        });

        plugin.getLogger().info("Hooked into Velocitab");
    }

    @Override
    public void unregister() {

    }

    public void vanish(@NotNull Player player) {
        this.velocitabAPI.vanishPlayer(player);
    }

    public void unVanish(@NotNull Player player) {
        this.velocitabAPI.unVanishPlayer(player);
    }

    @NotNull
    public String getCurrentGroup(@NotNull Player player) {
        Optional<TabPlayer> tabPlayer = velocitabAPI.getTabList().getTabPlayer(player);
        if (tabPlayer.isEmpty()) {
            return "";
        }

        Optional<?> velocitabOptional = plugin.getServer().getPluginManager().getPlugin("velocitab").flatMap(PluginContainer::getInstance);

        if(velocitabOptional.isEmpty() || !(velocitabOptional.get() instanceof Velocitab velocitab)) {
            return "";
        }

        return tabPlayer.get().getServerGroup(velocitab);
    }
}
