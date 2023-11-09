package org.alexdev.redisvanish.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.VelocityBrigadierMessage;
import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.alexdev.redisvanish.RedisVanish;
import org.alexdev.redisvanish.data.VanishLevel;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@RequiredArgsConstructor
public final class RedisVanishCommand {

    private final RedisVanish plugin;


    @NotNull
    public BrigadierCommand getCommand() {
        final LiteralArgumentBuilder<CommandSource> builder = LiteralArgumentBuilder
                .<CommandSource>literal("redisvanish")
                .executes(ctx -> {

                    if (!(ctx.getSource() instanceof Player player)) {
                        ctx.getSource().sendMessage(Component.text("You must be a player to use this command!"));
                        return Command.SINGLE_SUCCESS;
                    }

                    if (!player.hasPermission("redisvanish.vanish")) {
                        player.sendMessage(Component.text("You do not have permission to use this command!"));
                        return Command.SINGLE_SUCCESS;
                    }

                    if (plugin.getVanishManager().isVanished(player)) {
                        plugin.getVanishManager().unVanish(player);
                        player.sendMessage(Component.text("You have been unvanished!"));
                    } else {
                        plugin.getVanishManager().vanish(player);
                        player.sendMessage(Component.text("You have been vanished!"));
                    }

                    return Command.SINGLE_SUCCESS;
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("name", StringArgumentType.word())
                        .suggests((ctx, builder1) -> {
                            String input = (ctx.getArguments().containsKey("name") ? StringArgumentType.getString(ctx, "name") : "").toLowerCase();
                            plugin.getServer().getAllPlayers().stream()
                                    .map(Player::getUsername)
                                    .filter(u -> input.isEmpty() || u.toLowerCase().startsWith(input))
                                    .forEach(u -> builder1.suggest(u, VelocityBrigadierMessage.tooltip(MiniMessage.miniMessage().deserialize("<rainbow>" + u))));
                            return builder1.buildFuture();
                        })
                        .executes(ctx -> {
                            if (!(ctx.getSource() instanceof Player player)) {
                                ctx.getSource().sendMessage(Component.text("You must be a player to use this command!"));
                                return Command.SINGLE_SUCCESS;
                            }

                            if (!player.hasPermission("redisvanish.vanish.others")) {
                                player.sendMessage(Component.text("You do not have permission to use this command!"));
                                return Command.SINGLE_SUCCESS;
                            }

                            String name = StringArgumentType.getString(ctx, "name");
                            Optional<Player> target = plugin.getServer().getPlayer(name);

                            if (target.isEmpty()) {
                                player.sendMessage(Component.text("Player not found!"));
                                return Command.SINGLE_SUCCESS;
                            }

                            if (plugin.getVanishManager().isVanished(target.get())) {
                                plugin.getVanishManager().unVanish(target.get());
                                player.sendMessage(Component.text("You have unvanished " + target.get().getUsername()));
                            } else {
                                plugin.getVanishManager().vanish(target.get());
                                player.sendMessage(Component.text("You have vanished " + target.get().getUsername()));
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("reload")
                        .requires(src -> src.hasPermission("redisvanish.command.reload"))
                        .executes(ctx -> {
                            plugin.getConfigManager().reload();
                            plugin.getRedis().publishVanishLevels();
                            ctx.getSource().sendMessage(Component.text("Velocitab has been reloaded!"));
                            return Command.SINGLE_SUCCESS;
                        })
                )
                //debug
                .then(LiteralArgumentBuilder.<CommandSource>literal("debug")
                        .requires(src -> src.hasPermission("redisvanish.command.debug"))
                        .executes(ctx -> {
                            if (!(ctx.getSource() instanceof Player player)) {
                                ctx.getSource().sendMessage(Component.text("You must be a player to use this command!"));
                                return Command.SINGLE_SUCCESS;
                            }

                            Optional<VanishLevel> vanishLevel = plugin.getVanishManager().getVanishLevel(player);

                            if (vanishLevel.isEmpty()) {
                                ctx.getSource().sendMessage(Component.text("You do not have a vanish level!"));
                                return Command.SINGLE_SUCCESS;
                            }

                            ctx.getSource().sendMessage(Component.text("Vanish level: " + vanishLevel.get().name() + " (" + vanishLevel.get().permission() + ")"
                                    + " vanished: " + plugin.getVanishManager().isVanished(player)));

                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("name", StringArgumentType.word())
                                .suggests((ctx, builder1) -> {
                                    String input = (ctx.getArguments().containsKey("name") ? StringArgumentType.getString(ctx, "name") : "").toLowerCase();
                                    plugin.getServer().getAllPlayers().stream()
                                            .map(Player::getUsername)
                                            .filter(u -> input.isEmpty() || u.toLowerCase().startsWith(input))
                                            .forEach(u -> builder1.suggest(u, VelocityBrigadierMessage.tooltip(MiniMessage.miniMessage().deserialize("<rainbow>" + u))));
                                    return builder1.buildFuture();
                                })
                                .executes(ctx -> {
                                    String name = StringArgumentType.getString(ctx, "name");
                                    Optional<Player> target = plugin.getServer().getPlayer(name);

                                    if (target.isEmpty()) {
                                        ctx.getSource().sendMessage(Component.text("Player not found!"));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    Optional<VanishLevel> vanishLevel = plugin.getVanishManager().getVanishLevel(target.get());

                                    if (vanishLevel.isEmpty()) {
                                        ctx.getSource().sendMessage(Component.text("Target does not have a vanish level!"));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    ctx.getSource().sendMessage(Component.text("Vanish level: " + vanishLevel.get().name() + " (" + vanishLevel.get().permission() + ")"
                                            + " vanished: " + plugin.getVanishManager().isVanished(target.get())));

                                    return Command.SINGLE_SUCCESS;
                                })

                ));

        return new BrigadierCommand(builder);
    }
}
