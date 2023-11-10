package org.alexdev.redisvanish.config;

import de.exlll.configlib.Configuration;
import de.themoep.minedown.MineDown;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration()
@SuppressWarnings({"unused", "FieldMayBeFinal"})
public class Messages {
    private String title = "&bVanish settings";
    private String close = "&cClose";
    private String itemPickup = "&aItem pickup in vanish";
    private String damageOthers = "&aDamage to others in vanish";
    private String damageMe = "&aDamage in vanish";
    private String nightVision = "&aNight vision in vanish";
    private String doubleShift = "&aDouble shift to switch to spectator";
    private String actionbar = "&aActionbar in vanish";
    private String playerInventory = "&aRight click to open player's inventory";
    private String silent = "&aSilent";
    private String wrongCommands = "&aWrong commands";
    private String current = "&7Current: ";
    private String active = "&aActive";
    private String inactive = "&cInactive";
    private String vanishedActionbar = "&7You are currently &evanished&7!";
    public String reloaded = "&aThe plugin has been reloaded!";
    public String error = "&cAn error occurred while executing the command! Error: %error%";
    public String vanishTabPlaceholder = "&7[&bV&7]";

    private final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");


    public BaseComponent[] color(String text) {
        return new MineDown(text).replace().toComponent();
    }

    @SneakyThrows({SecurityException.class, IllegalArgumentException.class, IllegalAccessException.class})
    public void sendMessage(final CommandSender sender, final String message, final String... placeholders) {
        if (Arrays.stream(getClass().getDeclaredFields()).noneMatch(field -> field.getName().equals(message))) {
            sender.spigot().sendMessage(color(message));
            return;
        }

        final Field field = Arrays.stream(getClass().getDeclaredFields()).filter(f -> f.getName().equals(message)).findFirst().get();
        String mess = (String) field.get(this);

        if (placeholders.length % 2 == 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                mess = mess.replace(placeholders[i], placeholders[i + 1]);
            }
        }

        if (mess.isEmpty()) return;

        if (sender instanceof Player) {
            sender.spigot().sendMessage(color(mess));
        } else {
            sender.sendMessage(mess);
        }

    }

    public BaseComponent[] getMessage(String message) {
        return color(getRawMessage(message));
    }

    @SneakyThrows
    public String getRawMessage(String message) {
        Optional<Field> field = Arrays.stream(getClass().getDeclaredFields()).filter(f -> f.getName().equals(message)).findFirst();
        if (field.isEmpty()) {
            return message;
        }

        final Field fieldObj = field.get();
        return (String) fieldObj.get(this);
    }

    public String getColoredMessage(String message) {
        return colorString(getRawMessage(message));
    }

    public String colorString(String text) {
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

}
