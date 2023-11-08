package org.alexdev.redisvanish.hook;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.alexdev.redisvanish.RedisVanish;
import org.alexdev.redisvanish.data.VanishProperty;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashSet;
import java.util.Set;

public class ProtocolLibHook extends Hook implements Listener {

    private final RedisVanish plugin;
    private final Set<Location> locations;

    public ProtocolLibHook(RedisVanish plugin) {
        super(plugin);
        this.plugin = plugin;
        this.locations = new HashSet<>();
    }

    @Override
    public void register() {
        containersAnimationListener();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("Hooked into ProtocolLib");
    }

    @Override
    public void unregister() {
        ProtocolLibrary.getProtocolManager().removePacketListeners(plugin);
    }

    private boolean isInVanish(Player player) {
        return plugin.getVanishManager().isVanished(player);
    }

    @EventHandler
    private void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        if (!isInVanish(player)) return;
        if (!plugin.getVanishManager().hasProperty(player, VanishProperty.SILENT)) return;

        if (e.getClickedBlock() == null) return;

        Material type = e.getClickedBlock().getType();

        if (!type.name().contains("CHEST") && !type.name().contains("BARREL") && !type.name().contains("SHULKER"))
            return;

        Location location = e.getClickedBlock().getLocation();


        locations.add(location);

    }

    @EventHandler
    private void onPlace(BlockPlaceEvent e) {
        if (!notContainsLocation(e.getBlock().getLocation())) {
            locations.remove(e.getBlock().getLocation());
        }
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent e) {
        if (!isInVanish((Player) e.getPlayer())) return;
        if (!plugin.getVanishManager().hasProperty((Player) e.getPlayer(), VanishProperty.SILENT)) return;

        if (e.getInventory().getLocation() == null) return;

        Location location = e.getInventory().getLocation().clone();

        if (notContainsLocation(location)) return;

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {

            Block block = location.clone().getBlock();

            Inventory inventory = block.getState() instanceof Container ? ((Container) block.getState()).getInventory() : null;

            if (inventory != null) {
                if (!inventory.getViewers().isEmpty()) return;
            }

            removeLocation(location);
        }, 20L);
    }

    private void removeLocation(Location location) {
        locations.removeIf(loc -> loc.getWorld() != null
                && loc.getWorld().equals(location.getWorld())
                && loc.getBlockX() == location.getBlockX()
                && loc.getBlockY() == location.getBlockY()
                && loc.getBlockZ() == location.getBlockZ());
    }

    private void containersAnimationListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.NAMED_SOUND_EFFECT, PacketType.Play.Server.BLOCK_ACTION, PacketType.Play.Server.BLOCK_CHANGE) {
                    @Override
                    public void onPacketSending(PacketEvent event) {

                        if (isInVanish(event.getPlayer()) || event.getPlayer().hasPermission("redisvanish.vanish")) {
                            return;
                        }

                        PacketContainer packet = event.getPacket();

                        if (packet.getType() == PacketType.Play.Server.NAMED_SOUND_EFFECT) {
                            String sound = packet.getSoundEffects().read(0).name().toLowerCase();
                            if (!sound.contains("chest") && !sound.contains("door") && !sound.contains("shulker") && !sound.contains("barrel")) {
                                return;
                            }
                            int z = packet.getIntegers().read(2) / 8;
                            int x = packet.getIntegers().read(0) / 8;
                            int y = packet.getIntegers().read(1) / 8;

                            Location location = new Location(event.getPlayer().getWorld(), x, y, z);

                            if (notContainsLocation(location) && notFoundAround(location)) {
                                return;
                            }

                            event.setCancelled(true);

                        } else if (packet.getType() == PacketType.Play.Server.BLOCK_ACTION) {
                            Material material = packet.getBlocks().read(0);
                            if (material != Material.CHEST && material != Material.TRAPPED_CHEST && !material.name().contains("SHULKER") && material != Material.ENDER_CHEST && material != Material.BARREL) {
                                return;
                            }

                            Location location = packet.getBlockPositionModifier().read(0).toLocation(event.getPlayer().getWorld());

                            location = location.add(0, 0, 1);

                            if (notContainsLocation(location) && notFoundAround(location)) {
                                return;
                            }


                            event.setCancelled(true);
                        } else if (packet.getType() == PacketType.Play.Server.BLOCK_CHANGE) {
                            Material material = packet.getBlockPositionModifier().read(0).toLocation(event.getPlayer().getWorld()).getBlock().getType();

                            if (material != Material.CHEST && material != Material.TRAPPED_CHEST && material != Material.SHULKER_BOX && material != Material.ENDER_CHEST && material != Material.BARREL) {
                                return;
                            }

                            Location location = packet.getBlockPositionModifier().read(0).toLocation(event.getPlayer().getWorld());

                            if (notContainsLocation(location) && notFoundAround(location)) {
                                return;
                            }

                            event.setCancelled(true);
                        }
                    }


                });
    }

    private boolean notFoundAround(Location location) {
        for (Location loc : locations) {
            if (loc.getWorld().equals(location.getWorld()) && loc.distance(location) <= 2) {
                return false;
            }
        }
        return true;
    }

    private boolean notContainsLocation(Location location) {
        for (Location loc : locations) {
            if (loc.getWorld() != null
                    && loc.getWorld().equals(location.getWorld())
                    && loc.getBlockX() == location.getBlockX()
                    && loc.getBlockY() == location.getBlockY()
                    && loc.getBlockZ() == location.getBlockZ()) {
                return false;
            }
        }
        return true;
    }
}
