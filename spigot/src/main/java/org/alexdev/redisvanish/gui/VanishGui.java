package org.alexdev.redisvanish.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import org.alexdev.redisvanish.RedisVanish;
import org.alexdev.redisvanish.data.User;
import org.alexdev.redisvanish.data.VanishProperty;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class VanishGui extends ChestGui {

    private final RedisVanish plugin;
    private final Player player;

    public VanishGui(RedisVanish plugin, Player player) {
        super(6, plugin.getConfigManager().getMessages().colorString(plugin.getConfigManager().getMessages().getRawMessage("title")));
        this.plugin = plugin;
        this.player = player;
        applyPanes();
    }

    private String getMsg(String message) {
        return plugin.getConfigManager().getMessages().getRawMessage(message);
    }


    private void applyPanes() {
        User user = plugin.getUserManager().getUser(player);
        StaticPane pane = new StaticPane(0, 0, 9, 6);
        pane.setOnClick(event -> event.setCancelled(true));
        pane.fillWith(new ItemStackBuilder(Material.WHITE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        ItemStack close = new ItemStackBuilder(Material.BARRIER).setDisplayName(getMsg("close")).build();
        pane.addItem(new GuiItem(close, c -> {
            c.getWhoClicked().closeInventory();
        }), 0, 5);

        String current = getMsg("current");
        String active = getMsg("active");
        String inactive = getMsg("inactive");

        ItemStack pickup = new ItemStackBuilder(Material.HOPPER).setDisplayName(getMsg("itemPickup"))
                .setLore(current + (plugin.getVanishManager().hasProperty(user, VanishProperty.PICKUP) ? active : inactive)).build();
        ItemStack damageOthers = new ItemStackBuilder(Material.NETHERITE_SWORD).setDisplayName(getMsg("damageOthers"))
                .setLore(current + (plugin.getVanishManager().hasProperty(user, VanishProperty.DAMAGE_OTHERS) ? active : inactive)).build();
        ItemStack damageMe = new ItemStackBuilder(Material.POTION).setDisplayName(getMsg("damageMe"))
                .setLore(current + (plugin.getVanishManager().hasProperty(user, VanishProperty.DAMAGE_ME) ? active : inactive)).build();
        ItemStack nightVision = new ItemStackBuilder(Material.NETHERITE_HELMET).setDisplayName(getMsg("nightVision"))
                .setLore(current + (plugin.getVanishManager().hasProperty(user, VanishProperty.NIGHT_VISION) ? active : inactive)).build();
        ItemStack doubleShift = new ItemStackBuilder(Material.DIAMOND_BOOTS).setDisplayName(getMsg("doubleShift"))
                .setLore(current + (plugin.getVanishManager().hasProperty(user, VanishProperty.DOUBLE_SHIFT) ? active : inactive)).build();
        ItemStack actionbar = new ItemStackBuilder(Material.PAPER).setDisplayName(getMsg("actionbar"))
                .setLore(current + (plugin.getVanishManager().hasProperty(user, VanishProperty.ACTION_BAR) ? active : inactive)).build();
        ItemStack playerInventory = new ItemStackBuilder(Material.CHEST).setDisplayName(getMsg("playerInventory"))
                .setLore(current + (plugin.getVanishManager().hasProperty(user, VanishProperty.PLAYER_INVENTORY) ? active : inactive)).build();
        ItemStack silent = new ItemStackBuilder(Material.BOOK).setDisplayName(getMsg("silent"))
                .setLore(current + (plugin.getVanishManager().hasProperty(user, VanishProperty.SILENT) ? active : inactive)).build();
        ItemStack wrongCommands = new ItemStackBuilder(Material.BOOK).setDisplayName(getMsg("wrongCommands"))
                .setLore(current + (plugin.getVanishManager().hasProperty(user, VanishProperty.WRONG_COMMANDS) ? active : inactive)).build();

        pane.addItem(new GuiItem(pickup, c -> {
            plugin.getVanishManager().toggleProperty(user, VanishProperty.PICKUP);
            c.getWhoClicked().closeInventory();
            update(user);
            plugin.getInventoryManager().openVanishGui(player);
        }), 1, 1);
        pane.addItem(new GuiItem(damageOthers, c -> {
            plugin.getVanishManager().toggleProperty(user, VanishProperty.DAMAGE_OTHERS);
            c.getWhoClicked().closeInventory();
            update(user);
            plugin.getInventoryManager().openVanishGui(player);
        }), 3, 1);
        pane.addItem(new GuiItem(damageMe, c -> {
            plugin.getVanishManager().toggleProperty(user, VanishProperty.DAMAGE_ME);
            c.getWhoClicked().closeInventory();
            update(user);
            plugin.getInventoryManager().openVanishGui(player);
        }), 5, 1);
        pane.addItem(new GuiItem(nightVision, c -> {
            plugin.getVanishManager().toggleProperty(user, VanishProperty.NIGHT_VISION);
            c.getWhoClicked().closeInventory();
            plugin.getVanishManager().applyEffects(user);
            update(user);
            plugin.getInventoryManager().openVanishGui(player);
        }), 7, 1);
        pane.addItem(new GuiItem(doubleShift, c -> {
            plugin.getVanishManager().toggleProperty(user, VanishProperty.DOUBLE_SHIFT);
            c.getWhoClicked().closeInventory();
            update(user);
            plugin.getInventoryManager().openVanishGui(player);
        }), 1, 3);
        pane.addItem(new GuiItem(actionbar, c -> {
            plugin.getVanishManager().toggleProperty(user, VanishProperty.ACTION_BAR);
            c.getWhoClicked().closeInventory();
            update(user);
            plugin.getInventoryManager().openVanishGui(player);
        }), 3, 3);
        pane.addItem(new GuiItem(playerInventory, c -> {
            plugin.getVanishManager().toggleProperty(user, VanishProperty.PLAYER_INVENTORY);
            c.getWhoClicked().closeInventory();
            update(user);
            plugin.getInventoryManager().openVanishGui(player);
        }), 5, 3);
        pane.addItem(new GuiItem(silent, c -> {
            plugin.getVanishManager().toggleProperty(user, VanishProperty.SILENT);
            c.getWhoClicked().closeInventory();
            update(user);
            plugin.getInventoryManager().openVanishGui(player);
        }), 7, 3);
        pane.addItem(new GuiItem(wrongCommands, c -> {
            plugin.getVanishManager().toggleProperty(user, VanishProperty.WRONG_COMMANDS);
            c.getWhoClicked().closeInventory();
            update(user);
            plugin.getInventoryManager().openVanishGui(player);
        }), 3, 5);

        addPane(pane);
    }

    private void update(User user) {
        plugin.getRedis().saveUser(user);
        plugin.getRedis().sendUserUpdate(user);
    }

}
