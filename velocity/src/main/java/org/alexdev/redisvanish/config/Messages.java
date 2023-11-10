package org.alexdev.redisvanish.config;

import de.exlll.configlib.Configuration;
import lombok.SneakyThrows;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@Configuration()
@SuppressWarnings({"unused", "FieldMayBeFinal"})
public class Messages {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private String vanished = "&7You have been vanished!";
    private String unVanished = "&7You have been unVanished!";
    private String vanishedOther = "&7You have vanished &e%player%&7!";
    private String unVanishedOther = "&7You have unVanished &e%player%&7!";
    private String noPermission = "&cYou don't have permission to execute this command!";
    private String mustBeAPlayer = "&cYou must be a player to execute this command!";
    private String playerNotFound = "&cCannot find the player &e%player%&c!";
    private String noVanishLevel = "&cYou don't have a vanish level!";
    private String noVanishLevelOther = "&cThe user &e%player% &cdoesn't have a vanish level!";
    private String debugMessage = "&7Debug: &e%message%&7!";
    private String reloaded = "&aThe plugin has been reloaded!";
    private String error = "&cAn error occurred while executing the command! Error: %error%";


    public List<String> color(final List<String> toColor) {
        return toColor.stream().map(this::color).toList();
    }

    public List<Component> colorComponent(final List<String> toColor) {
        return toColor.stream().map(this::colorComponent).toList();
    }


    public String color(final String text) {

        return LegacyComponentSerializer.legacySection()
                .serialize(LEGACY.deserialize(text));

    }

    public Component colorComponent(final String text) {
        return LEGACY.deserialize(text).decoration(TextDecoration.ITALIC, false);
    }


    @SneakyThrows({SecurityException.class, IllegalArgumentException.class, IllegalAccessException.class})
    public void sendMessage(final Audience sender, final String message, final String... placeholders) {
        if (Arrays.stream(getClass().getDeclaredFields()).noneMatch(field -> field.getName().equals(message))) {
            sender.sendMessage(colorComponent(message));
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

        sender.sendMessage(colorComponent(mess));
    }

    @SneakyThrows
    public String getMessage(final String message, final String... placeholders) {
        final Field field = this.getClass().getDeclaredField(message);
        String mess = (String) field.get(this);

        if (placeholders.length % 2 == 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                mess = mess.replace(placeholders[i], placeholders[i + 1]);
            }
        }

        return color(mess);
    }


}
