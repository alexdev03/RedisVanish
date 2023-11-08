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
        super(6, "§bImpostazioni della Vanish");
        this.plugin = plugin;
        this.player = player;
        applyPanes();
    }


    private void applyPanes() {
        User user = plugin.getUserManager().getUser(player);
        StaticPane pane = new StaticPane(0, 0, 9, 6);

        pane.setOnClick(event -> event.setCancelled(true));

        pane.fillWith(new ItemStackBuilder(Material.WHITE_STAINED_GLASS_PANE).setDisplayName(" ").build());

        ItemStack close = new ItemStackBuilder(Material.BARRIER).setDisplayName("§aChiudi").build();

        pane.addItem(new GuiItem(close, c -> {
            c.getWhoClicked().closeInventory();
        } ), 0, 5);

        ItemStack pickup = new ItemStackBuilder(Material.HOPPER).setDisplayName("§aRaccolta items in vanish")
                .setLore("§7Attuale: " + (plugin.getVanishManager().hasProperty(user, VanishProperty.PICKUP) ? "§aAttivo" : "§cDisattivo")).build();

        ItemStack damageOthers = new ItemStackBuilder(Material.NETHERITE_SWORD).setDisplayName("§aDanni agli altri in vanish")
                .setLore("§7Attuale: " + (plugin.getVanishManager().hasProperty(user, VanishProperty.DAMAGE_OTHERS) ? "§aAttivo" : "§cDisattivo")).build();

        ItemStack damageMe = new ItemStackBuilder(Material.POTION).setDisplayName("§aDanni in vanish")
                .setLore("§7Attuale: " + (plugin.getVanishManager().hasProperty(user, VanishProperty.DAMAGE_ME) ? "§aAttivo" : "§cDisattivo")).build();

        ItemStack nightVision = new ItemStackBuilder(Material.NETHERITE_HELMET).setDisplayName("§aVisione notturna in vanish")
                .setLore("§7Attuale: " + (plugin.getVanishManager().hasProperty(user, VanishProperty.NIGHT_VISION) ? "§aAttivo" : "§cDisattivo")).build();

        ItemStack doubleShift = new ItemStackBuilder(Material.DIAMOND_BOOTS).setDisplayName("§aDoppio shift per passare in spectator")
                .setLore("§7Attuale: " + (plugin.getVanishManager().hasProperty(user, VanishProperty.DOUBLE_SHIFT) ? "§aAttivo" : "§cDisattivo")).build();

        ItemStack actionbar = new ItemStackBuilder(Material.PAPER).setDisplayName("§aActionbar in vanish")
                .setLore("§7Attuale: " + (plugin.getVanishManager().hasProperty(user, VanishProperty.ACTION_BAR) ? "§aAttivo" : "§cDisattivo")).build();

        ItemStack playerInventory = new ItemStackBuilder(Material.CHEST).setDisplayName("§aTasto destro per aprire l'inventario del giocatore")
                .setLore("§7Attuale: " + (plugin.getVanishManager().hasProperty(user, VanishProperty.PLAYER_INVENTORY) ? "§aAttivo" : "§cDisattivo")).build();

        ItemStack silent = new ItemStackBuilder(Material.BOOK).setDisplayName("§aSilent")
                .setLore("§7Attuale: " + (plugin.getVanishManager().hasProperty(user, VanishProperty.SILENT) ? "§aAttivo" : "§cDisattivo")).build();

        ItemStack wrongCommands = new ItemStackBuilder(Material.BOOK).setDisplayName("§aComandi errati")
                .setLore("§7Attuale: " + (plugin.getVanishManager().hasProperty(user, VanishProperty.WRONG_COMMANDS) ? "§aAttivo" : "§cDisattivo")).build();


        pane.addItem(new GuiItem(pickup, c -> {
            plugin.getVanishManager().toggleProperty(user, VanishProperty.PICKUP);
            c.getWhoClicked().closeInventory();
            update(user);
            plugin.getInventoryManager().openVanishGui(player);
        } ), 1, 1);

        pane.addItem(new GuiItem(damageOthers, c -> {
            plugin.getVanishManager().toggleProperty(user, VanishProperty.DAMAGE_OTHERS);
            c.getWhoClicked().closeInventory();
            update(user);
            plugin.getInventoryManager().openVanishGui(player);
        } ), 3, 1);

        pane.addItem(new GuiItem(damageMe, c -> {
            plugin.getVanishManager().toggleProperty(user, VanishProperty.DAMAGE_ME);
            c.getWhoClicked().closeInventory();
            update(user);            plugin.getInventoryManager().openVanishGui(player);
        } ), 5, 1);

        pane.addItem(new GuiItem(nightVision, c -> {
            plugin.getVanishManager().toggleProperty(user, VanishProperty.NIGHT_VISION);
            c.getWhoClicked().closeInventory();
            plugin.getVanishManager().applyEffects(user);
            update(user);
            plugin.getInventoryManager().openVanishGui(player);
        } ), 7, 1);

        pane.addItem(new GuiItem(doubleShift, c -> {
            plugin.getVanishManager().toggleProperty(user, VanishProperty.DOUBLE_SHIFT);
            c.getWhoClicked().closeInventory();
            update(user);
            plugin.getInventoryManager().openVanishGui(player);
        } ), 1, 3);

        pane.addItem(new GuiItem(actionbar, c -> {
            plugin.getVanishManager().toggleProperty(user, VanishProperty.ACTION_BAR);
            c.getWhoClicked().closeInventory();
            update(user);
            plugin.getInventoryManager().openVanishGui(player);
        } ), 3, 3);

        pane.addItem(new GuiItem(playerInventory, c -> {
            plugin.getVanishManager().toggleProperty(user, VanishProperty.PLAYER_INVENTORY);
            c.getWhoClicked().closeInventory();
            update(user);
            plugin.getInventoryManager().openVanishGui(player);
        } ), 5, 3);

        pane.addItem(new GuiItem(silent, c -> {
            plugin.getVanishManager().toggleProperty(user, VanishProperty.SILENT);
            c.getWhoClicked().closeInventory();
            update(user);
            plugin.getInventoryManager().openVanishGui(player);
        } ), 7, 3);

        pane.addItem(new GuiItem(wrongCommands, c -> {
            plugin.getVanishManager().toggleProperty(user, VanishProperty.WRONG_COMMANDS);
            c.getWhoClicked().closeInventory();
            update(user);
            plugin.getInventoryManager().openVanishGui(player);
        } ), 3, 5);



        addPane(pane);

    }

    private void update(User user) {
        plugin.getRedis().saveUser(user);
        plugin.getRedis().sendUserUpdate(user);
    }

}
