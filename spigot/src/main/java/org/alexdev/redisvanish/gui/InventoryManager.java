package org.alexdev.redisvanish.gui;

import org.alexdev.redisvanish.RedisVanish;
import org.bukkit.entity.Player;

public class InventoryManager {

    private final RedisVanish plugin;

    public InventoryManager(RedisVanish plugin) {
        this.plugin = plugin;
    }

    public void openVanishGui(Player player) {
        VanishGui vanishGui = new VanishGui(plugin, player);

        vanishGui.show(player);
    }


}
