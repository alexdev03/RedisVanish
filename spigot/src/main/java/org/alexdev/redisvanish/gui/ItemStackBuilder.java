package org.alexdev.redisvanish.gui;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class ItemStackBuilder {

    protected ItemStack result;
    protected final ItemMeta itemMeta;
    private final Plugin plugin;

    public ItemStackBuilder(ItemStack result) {
        Material material = result.getType();
        if (!this.isMaterialAllowed(material)) {
            throw new IllegalArgumentException(String.format("Cannot instantiate %s with resulting ItemStack of Material '%s'!", this.getClass().getSimpleName(), material.name()));
        }
        plugin = JavaPlugin.getProvidingPlugin(this.getClass());

        this.result = result;
        this.itemMeta = result.getItemMeta();
    }

    public ItemStackBuilder(ItemStack result, boolean bypass) {
        Material material = result.getType();

        this.result = result;
        this.itemMeta = result.getItemMeta();
        plugin = JavaPlugin.getProvidingPlugin(this.getClass());
    }

    public ItemStackBuilder(ConfigurationSection section) {
        if (section.getItemStack("") == null || Objects.requireNonNull(section.getItemStack("")).getItemMeta() == null) {
            throw new IllegalArgumentException("ItemStack cannot be null!");
        }
        itemMeta = section.getItemStack("").getItemMeta();

        plugin = JavaPlugin.getProvidingPlugin(this.getClass());

    }

    private void addPersistentData(Map<String, String> persistentData) {
        for (Map.Entry<String, String> entry : persistentData.entrySet()) {
            NamespacedKey key = new NamespacedKey(plugin, entry.getKey());
            itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, entry.getValue());
        }
    }

    public ConfigurationSection createSection(FileConfiguration config, String name, ItemStack itemStack) {
        ConfigurationSection section = config.createSection(name);

        section.set("", itemStack);

        if (true) return section;


        section.set("material", itemStack.getType().getKey().getKey());

        section.set("amount", itemStack.getAmount());

        if (!itemStack.hasItemMeta()) return section;

        if (itemStack.getItemMeta().hasDisplayName()) section.set("name", itemStack.getItemMeta().getDisplayName());

        if (itemStack.getItemMeta().hasLore()) section.set("lore", itemStack.getItemMeta().getLore());

        if (itemStack.getItemMeta().hasCustomModelData())
            section.set("customModelData", itemStack.getItemMeta().getCustomModelData());

        if (itemStack.getItemMeta().hasEnchants())
            section.set("enchants", itemStack.getItemMeta().getEnchants().keySet().stream().map((enchant -> enchant.getKey().getKey() + ":" + itemStack.getItemMeta().getEnchants().get(enchant))).collect(Collectors.toList()));

        if (itemStack.getItemMeta().getPersistentDataContainer().getKeys().size() > 0) {
            Map<String, String> persistentData = new HashMap<>();
            for (NamespacedKey key : itemStack.getItemMeta().getPersistentDataContainer().getKeys()) {
                persistentData.put(key.getKey(), itemStack.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING));
            }
            section.set("persistentData", persistentData);
        }

        return section;
    }


    public Material getMaterial() {
        return result.getType();
    }


    public ItemStackBuilder(Material material, int amount, short durability) {
        this(new ItemStack(material, amount, durability));
    }

    public ItemStackBuilder(Material material, int amount) {
        this(new ItemStack(material, amount));
    }

    public ItemStackBuilder(Material material, short durability) {
        this(new ItemStack(material, 1, durability));
    }

    public ItemStackBuilder(Material material) {
        this(new ItemStack(material));
    }

    public ItemStackBuilder setCustomModelData(int num) {
        itemMeta.setCustomModelData(num);
        return this;
    }

    /**
     * Enchant string must be in this format: ENCHANT_NAME:LEVEL
     *
     * @param enchants
     * @return
     */

    public ItemStackBuilder addEnchants(List<String> enchants) {
        for (String enchant : enchants) {
            String[] enchantSplit = enchant.split(":");
            addEnchantment(Enchantment.getByKey(NamespacedKey.fromString(enchantSplit[0])), Integer.parseInt(enchantSplit[1]));
        }
        return this;
    }


    protected boolean isMaterialAllowed(Material material) {
        return !material.equals(Material.AIR);
    }

    /*
     * Start Builder Methods
     */

    public ItemStackBuilder setMaterial(Material material) {
        if (!this.isMaterialAllowed(material)) {
            throw new IllegalArgumentException(String.format("Cannot set Material of resulting ItemStack to '%s'!", material.name()));
        }

        this.result.setType(material);

        return this;
    }

    public ItemStackBuilder setAmount(int amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("Cannot set amount of resulting ItemStack to less than 1!");
        }

        this.result.setAmount(amount);

        return this;
    }

    public ItemStackBuilder setDurability(short durability) {
        short maxDurability = this.result.getType().getMaxDurability();
        if (durability < 0) {
            durability += maxDurability;
        }

        if (durability < 0 || durability > maxDurability) {
            throw new IllegalArgumentException(String.format("Cannot set durability of resulting ItemStack to less than 0 or greater than %d!", maxDurability));
        }

        this.result.setDurability(durability);

        return this;
    }


    public ItemStackBuilder addEnchantment(Enchantment enchantment, int level, boolean unsafe) {
        this.itemMeta.addEnchant(enchantment, level, unsafe);

        return this;
    }

    public ItemStackBuilder addEnchantment(Enchantment enchantment, int level) {
        return this.addEnchantment(enchantment, level, false);
    }

    public ItemStackBuilder addEnchantment(Enchantment enchantment, boolean unsafe) {
        return this.addEnchantment(enchantment, 1, unsafe);
    }

    public ItemStackBuilder addEnchantment(Enchantment enchantment) {
        return this.addEnchantment(enchantment, 1, false);
    }

    public ItemStackBuilder removeEnchantment(Enchantment enchantment) {
        this.itemMeta.removeEnchant(enchantment);

        return this;
    }

    public ItemStackBuilder removeEnchantments(Collection<Enchantment> enchantments) {
        enchantments.forEach(this::removeEnchantment);

        return this;
    }

    public ItemStackBuilder removeEnchantments(Enchantment... enchantments) {
        return (enchantments.length == 0) ? this.removeEnchantments(this.itemMeta.getEnchants().keySet()) : this.removeEnchantments(Arrays.asList(enchantments));
    }

    public ItemStackBuilder setDisplayName(String displayName) {
        this.itemMeta.setDisplayName(color(displayName));

        return this;
    }

    public ItemStackBuilder setLore(List<String> lore) {
        this.itemMeta.setLore(lore.stream().map(this::color).collect(Collectors.toList()));

        return this;
    }


    private final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");


    public String color(String text) {
        if (text == null) return "";

        text = ChatColor.translateAlternateColorCodes('&', text);

        final char colorChar = ChatColor.COLOR_CHAR;

        final Matcher matcher = hexPattern.matcher(text);
        final StringBuffer buffer = new StringBuffer(text.length() + 4 * 8);

        while (matcher.find()) {
            final String group = matcher.group(1);

            matcher.appendReplacement(buffer, colorChar + "x"
                    + colorChar + group.charAt(0) + colorChar + group.charAt(1)
                    + colorChar + group.charAt(2) + colorChar + group.charAt(3)
                    + colorChar + group.charAt(4) + colorChar + group.charAt(5));
        }

        text = matcher.appendTail(buffer).toString();

        return text;
    }


    public ItemStackBuilder setPlaceholder(String placeholder, String replace) {


        if (this.itemMeta.hasLore()) {
            //this.itemMeta.lore(this.itemMeta.lore().stream().map(s -> Utils.componentColor(((TextComponent)s).content().replaceAll(placeholder, replace))).collect(Collectors.toList()));
            this.itemMeta.setLore(this.itemMeta.getLore().stream().map(tmp -> color(tmp.replaceAll(placeholder, replace))).collect(Collectors.toList()));
        }

        if (this.itemMeta.hasDisplayName())
            this.itemMeta.setDisplayName(color((this.itemMeta.getDisplayName())).replaceAll(placeholder, replace));

        return this;
    }

    public ItemStackBuilder setFixedPlaceholder(String placeholder, String replace) {

        if (placeholder.isEmpty() || replace.isEmpty()) return this;

        if (this.itemMeta.hasLore()) {
            //this.itemMeta.lore(this.itemMeta.lore().stream().map(s -> Utils.componentColor(((TextComponent)s).content().replaceAll(placeholder, replace))).collect(Collectors.toList()));
//            Map<Integer, Integer> map = new HashMap<>();
//            for (int i = 0; i < this.itemMeta.getLore().size(); i++) {
//                if (this.itemMeta.getLore().get(i).contains(placeholder)) {
//                    map.put(i, this.itemMeta.getLore().get(i).indexOf(placeholder));
//                    break;
//                }
//            }
//            if (!indexes.isEmpty()) {
//                int tmp = 0;
//
//                int start
//
//                List<String> fix = Utils.handleDescription(, replace);
//
//                for (int i = 0; i < indexes.size(); i++) {
//
//                }
//            }
            this.itemMeta.setLore(this.itemMeta.getLore().stream().map(tmp -> color(tmp.replaceAll(placeholder, replace))).collect(Collectors.toList()));
        }

        if (this.itemMeta.hasDisplayName())
            this.itemMeta.setDisplayName(color((this.itemMeta.getDisplayName())).replaceAll(placeholder, replace));

        return this;
    }

    public ItemStackBuilder setLore(String... lore) {
        return this.setLore(Arrays.asList(lore));
    }

    public ItemStackBuilder addLore(List<String> lore) {
        if (this.itemMeta.hasLore()) {
            lore = lore.stream().map(l -> color(l)).collect(Collectors.toList());
        }


        return this.setLore(lore);
    }

    public ItemStackBuilder addLore(List<String> lore, int index) {
        if (this.itemMeta.hasLore()) {
            lore.addAll(this.itemMeta.getLore());
        }


        return this.setLore(lore);
    }

    public ItemStackBuilder addLore(String... lore) {
        return this.addLore(Arrays.asList(lore));
    }

    public ItemStackBuilder addLore(int index, String... lore) {
        return this.addLore(Arrays.asList(lore));
    }

    public ItemStackBuilder setPlayerHead(OfflinePlayer player) {

        if (getMaterial() != Material.PLAYER_HEAD)
            throw new IllegalArgumentException("Cannot set player head on non-player head item!");

        SkullMeta meta = (SkullMeta) itemMeta;

        meta.setOwningPlayer(player);

        return this;
    }

    public ItemStackBuilder setData(String key, String value) {
        PersistentDataContainer container = this.itemMeta.getPersistentDataContainer();
        container.set(new NamespacedKey(plugin, key), PersistentDataType.STRING, value);

        return this;
    }

    public ItemStackBuilder setData(String key, int value) {
        PersistentDataContainer container = this.itemMeta.getPersistentDataContainer();
        container.set(new NamespacedKey(plugin, key), PersistentDataType.INTEGER, value);

        return this;
    }

    public boolean isData(String key) {
        PersistentDataContainer container = this.itemMeta.getPersistentDataContainer();
        return container.has(new NamespacedKey(plugin, key), PersistentDataType.STRING);
    }


    public ItemStackBuilder makeGlowing() {

        this.itemMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
        this.itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        return this;
    }


    /*
     * Finish Builder Methods
     */

    public final ItemStack build() {
        this.result.setItemMeta(this.itemMeta);

//        if(nbt.size() > 0) { //For nbt
//            NBTItem nbtItem = new NBTItem(result);
//            nbt.forEach(nbtItem::setString);
//            result = nbtItem.getItem();
//        }

        return this.result;
    }

    public int getAmount() {
        return this.result.getAmount();
    }
}

