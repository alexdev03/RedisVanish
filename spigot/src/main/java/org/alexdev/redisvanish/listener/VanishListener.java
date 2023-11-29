package org.alexdev.redisvanish.listener;

import org.alexdev.redisvanish.RedisVanish;
import org.alexdev.redisvanish.data.VanishProperty;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VanishListener implements Listener {

    private final RedisVanish plugin;
    private final List<UUID> sneakGamemode;
    private final Map<UUID, GameMode> gameModeCache;
    private final Map<UUID, String> preventWrongCommands;

    public VanishListener(RedisVanish plugin) {
        this.plugin = plugin;
        this.sneakGamemode = new ArrayList<>();
        this.gameModeCache = new HashMap<>();
        this.preventWrongCommands = new ConcurrentHashMap<>();
    }

    private boolean isInVanish(Player player) {
        return plugin.getVanishManager().isVanished(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerChat(AsyncPlayerChatEvent e) {
        if (!isInVanish(e.getPlayer())) return;
        if (!plugin.getVanishManager().hasProperty(e.getPlayer(), VanishProperty.WRONG_COMMANDS)) return;
        String message = e.getMessage();
        if (!message.startsWith("7")) return;
        if (preventWrongCommands.getOrDefault(e.getPlayer().getUniqueId(), "").equalsIgnoreCase(message)) {
            preventWrongCommands.remove(e.getPlayer().getUniqueId());
            return;
        }
        preventWrongCommands.put(e.getPlayer().getUniqueId(), message);
        e.getPlayer().sendMessage("§cWrite the command again to execute it.");
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (isInVanish(player)) {
            player.setAllowFlight(true);
            player.setFlying(true);
            e.setJoinMessage("");
        }
    }

    @EventHandler
    private void onPickUp(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        if (!isInVanish(player)) return;
        if (plugin.getVanishManager().hasProperty(player, VanishProperty.PICKUP)) return;
        e.setCancelled(true);
    }


    @EventHandler
    private void onShift(PlayerToggleSneakEvent e) {
        Player player = e.getPlayer();
        if (isInVanish(player)) {
            if (!e.isSneaking()) return;

            if (!plugin.getVanishManager().hasProperty(player, VanishProperty.DOUBLE_SHIFT)) return;

            if (!sneakGamemode.contains(player.getUniqueId())) {
                sneakGamemode.add(player.getUniqueId());
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    sneakGamemode.remove(player.getUniqueId());
                }, 10L);
            } else {
                if (player.getGameMode().equals(GameMode.SPECTATOR)) {
                    player.setGameMode(gameModeCache.getOrDefault(player.getUniqueId(), GameMode.SURVIVAL));
                } else {
                    gameModeCache.put(player.getUniqueId(), player.getGameMode());
                    player.setGameMode(GameMode.SPECTATOR);
                }
                sneakGamemode.remove(player.getUniqueId());
            }
        }
    }


    @EventHandler
    private void entityDamageByVanish(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player player)) return;
        if(player.hasMetadata("NPC")) return;
        if (!isInVanish(player)) return;
        if (plugin.getVanishManager().hasProperty(player, VanishProperty.DAMAGE_OTHERS)) return;
        e.setCancelled(true);
    }

    @EventHandler
    private void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        if(player.hasMetadata("NPC")) return;
        if (!isInVanish(player)) return;
        if (plugin.getVanishManager().hasProperty(player, VanishProperty.DAMAGE_ME)) return;
        e.setCancelled(true);
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEntityEvent e) {
        if (!isInVanish(e.getPlayer())) return;
        if(e.getRightClicked().hasMetadata("NPC")) return;
        if (!plugin.getVanishManager().hasProperty(e.getPlayer(), VanishProperty.PLAYER_INVENTORY)) return;

        if (e.getRightClicked() instanceof Player target) {
            e.getPlayer().openInventory(target.getInventory());
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onTabComplete(TabCompleteEvent e) {
        if (!(e.getSender() instanceof Player player)) return;
        if (player.hasPermission("redisvanish.bypass")) return;

        String command = e.getBuffer();
        if (command.isEmpty()) return;

        command = command.substring(1).split(" ")[0];

        String finalCommand = command;
        if (plugin.getConfigManager().getConfig().getCommandsToClean().stream()
                .anyMatch(c -> c.equalsIgnoreCase(finalCommand))) {
            e.setCompletions(plugin.getVanishManager().cleanStringList(player, e.getCompletions()));
        }

    }
}
