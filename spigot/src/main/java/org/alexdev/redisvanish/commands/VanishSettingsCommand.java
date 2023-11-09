package org.alexdev.redisvanish.commands;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import lombok.RequiredArgsConstructor;
import org.alexdev.redisvanish.RedisVanish;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class VanishSettingsCommand {

    private final RedisVanish plugin;


    @Command(name = "", desc = "Edit vanish settings")
    public void vanishSettings(@Sender Player player) {
        plugin.getInventoryManager().openVanishGui(player);
    }

    @Command(name = "debug" , desc = "Debug")
    public void debug(@Sender CommandSender player) {
        plugin.getUserManager().getRemoteUsers().forEach((uuid, user) -> {
            player.sendMessage("UUID: " + uuid + " | " + user.name() + " | " + user.vanishLevel() + " | " + user.bypass());
        });
    }

}
