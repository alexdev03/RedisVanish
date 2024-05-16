package org.alexdev.redisvanish.commands;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.OptArg;
import com.jonahseguin.drink.annotation.Sender;
import lombok.RequiredArgsConstructor;
import org.alexdev.redisvanish.RedisVanish;
import org.alexdev.redisvanish.data.RemoteUser;
import org.alexdev.redisvanish.data.VanishLevel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

@RequiredArgsConstructor
public class RedisVanishSpigotCommand {

    private final RedisVanish plugin;

    @Command(name = "debug" , desc = "Debug")
    public void debug(@Sender CommandSender player) {
        plugin.getUserManager().getRemoteUsersByUUID().forEach((uuid, user) -> {
            player.sendMessage("UUID: " + uuid + " | " + user.name() + " | " + user.vanishLevel() + " | " + user.bypass());
        });
    }

    @Command(name = "test" , desc = "Test")
    public void test(@Sender Player target, Player current, @OptArg Player target2) {
        if (target2 == null) {
            target2 = target;
        }
        final boolean canSee = plugin.getVanishManager().canSee(current, target2);
        final Optional<VanishLevel> playerVanishLevel = plugin.getVanishManager().getVanishLevel(current);
        final Optional<VanishLevel> targetVanishLevel = plugin.getVanishManager().getVanishLevel(target2);
        target.sendMessage("Can see: " + canSee);
        target.sendMessage("Player vanish level: " + playerVanishLevel.map(l -> l.name() + " " + plugin.getVanishManager().getOrder(l)).orElse("null 0"));
        target.sendMessage("Target vanish level: " + targetVanishLevel.map(l -> l.name() + " " + plugin.getVanishManager().getOrder(l)).orElse("null 0"));
    }

    @Command(name = "testRemote" , desc = "Test remote")
    public void testRemote(@Sender CommandSender target, Player current, RemoteUser remoteUser) {
        if (remoteUser == null) {
            target.sendMessage("Remote user is null");
            return;
        }
        final Optional<VanishLevel> playerVanishLevel = plugin.getVanishManager().getVanishLevel(current);
        final Optional<VanishLevel> targetVanishLevel = plugin.getVanishManager().getRemoveVanishLevel(remoteUser);
        final boolean canSee = plugin.getVanishManager().canSee(current, remoteUser);
        target.sendMessage("Can see: " + canSee);
        target.sendMessage("Player vanish level: " + playerVanishLevel.map(l -> l.name() + " " + plugin.getVanishManager().getOrder(l)).orElse("null 0"));
        target.sendMessage("Target vanish level: " + targetVanishLevel.map(l -> l.name() + " " + plugin.getVanishManager().getOrder(l)).orElse("null 0"));
        target.sendMessage("Remote user server: " + remoteUser.server());
    }

}
